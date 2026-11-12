package com.example.data.repository

import com.example.data.local.dao.InventoryDao
import com.example.data.local.entity.InventoryItemEntity
import com.example.data.local.entity.InventoryTransactionEntity
import com.example.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class InventoryRepositoryImpl(
    private val inventoryDao: InventoryDao
) : InventoryRepository {

    override val allItems: Flow<List<InventoryItemEntity>> = inventoryDao.getAllItems()
    override val allTransactions: Flow<List<InventoryTransactionEntity>> = inventoryDao.getAllTransactions()

    override fun getItemsByCategory(category: String): Flow<List<InventoryItemEntity>> {
        return inventoryDao.getItemsByCategory(category)
    }

    override suspend fun getItemById(id: String): InventoryItemEntity? {
        return inventoryDao.getItemById(id)
    }

    override suspend fun recordInbound(
        itemId: String,
        quantity: Float,
        unitPrice: Long,
        date: Long,
        supplierOrInvoice: String,
        notes: String
    ): Result<InventoryTransactionEntity> {
        val item = inventoryDao.getItemById(itemId)
            ?: return Result.failure(IllegalArgumentException("Không tìm thấy mặt hàng trong kho (ID: $itemId)"))

        if (quantity <= 0f) {
            return Result.failure(IllegalArgumentException("Số lượng nhập phải lớn hơn 0"))
        }

        // Tính đơn giá bình quân gia quyền mới:
        // Giá trị kho mới = (Tồn cũ * Đơn giá BQ cũ) + (SL nhập * Đơn giá nhập)
        // Đơn giá BQ mới = Giá trị kho mới / (Tồn cũ + SL nhập)
        val oldStock = item.currentStock.coerceAtLeast(0f)
        val newStock = oldStock + quantity
        val oldValue = (oldStock * item.averageUnitPrice).toLong()
        val inValue = (quantity * unitPrice).toLong()
        val newAvgPrice = if (newStock > 0f) {
            ((oldValue + inValue) / newStock).toLong()
        } else {
            unitPrice
        }

        val updatedItem = item.copy(
            currentStock = newStock,
            lastUnitPrice = unitPrice,
            averageUnitPrice = newAvgPrice,
            updatedAt = System.currentTimeMillis()
        )
        inventoryDao.updateItem(updatedItem)

        val transaction = InventoryTransactionEntity(
            id = UUID.randomUUID().toString(),
            type = InventoryTransactionEntity.TYPE_IN,
            itemId = item.id,
            itemName = item.name,
            category = item.category,
            unit = item.unit,
            quantity = quantity,
            unitPrice = unitPrice,
            totalPrice = inValue,
            targetBarn = "",
            supplierOrNotes = supplierOrInvoice.ifBlank { notes },
            date = date,
            createdAt = System.currentTimeMillis()
        )
        inventoryDao.insertTransaction(transaction)

        return Result.success(transaction)
    }

    override suspend fun recordOutbound(
        itemId: String,
        quantity: Float,
        targetBarn: String,
        date: Long,
        purposeOrNotes: String
    ): Result<InventoryTransactionEntity> {
        val item = inventoryDao.getItemById(itemId)
            ?: return Result.failure(IllegalArgumentException("Không tìm thấy mặt hàng trong kho (ID: $itemId)"))

        if (quantity <= 0f) {
            return Result.failure(IllegalArgumentException("Số lượng xuất phải lớn hơn 0"))
        }

        // RÀNG BUỘC CHẶN XUẤT ÂM:
        if (quantity > item.currentStock) {
            return Result.failure(
                IllegalArgumentException("Chặn xuất âm: Số lượng xuất ($quantity ${item.unit}) vượt quá số lượng tồn hiện tại (${item.currentStock} ${item.unit})!")
            )
        }

        val newStock = (item.currentStock - quantity).coerceAtLeast(0f)
        val unitPrice = item.averageUnitPrice
        val totalPrice = (quantity * unitPrice).toLong()

        val updatedItem = item.copy(
            currentStock = newStock,
            updatedAt = System.currentTimeMillis()
        )
        inventoryDao.updateItem(updatedItem)

        val transaction = InventoryTransactionEntity(
            id = UUID.randomUUID().toString(),
            type = InventoryTransactionEntity.TYPE_OUT,
            itemId = item.id,
            itemName = item.name,
            category = item.category,
            unit = item.unit,
            quantity = quantity,
            unitPrice = unitPrice,
            totalPrice = totalPrice,
            targetBarn = targetBarn,
            supplierOrNotes = purposeOrNotes,
            date = date,
            createdAt = System.currentTimeMillis()
        )
        inventoryDao.insertTransaction(transaction)

        return Result.success(transaction)
    }

    override suspend fun addNewItem(
        name: String,
        category: String,
        unit: String,
        initialStock: Float,
        unitPrice: Long,
        minAlertStock: Float,
        notes: String
    ): Result<InventoryItemEntity> {
        val existing = inventoryDao.getItemByName(name.trim())
        if (existing != null) {
            return Result.failure(IllegalArgumentException("Mặt hàng '${name.trim()}' đã có trong kho!"))
        }

        val item = InventoryItemEntity(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            category = category,
            unit = unit.trim(),
            currentStock = initialStock.coerceAtLeast(0f),
            lastUnitPrice = unitPrice,
            averageUnitPrice = unitPrice,
            minAlertStock = minAlertStock,
            notes = notes.trim(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        inventoryDao.insertItem(item)

        if (initialStock > 0f) {
            val initTx = InventoryTransactionEntity(
                id = UUID.randomUUID().toString(),
                type = InventoryTransactionEntity.TYPE_IN,
                itemId = item.id,
                itemName = item.name,
                category = item.category,
                unit = item.unit,
                quantity = initialStock,
                unitPrice = unitPrice,
                totalPrice = (initialStock * unitPrice).toLong(),
                targetBarn = "",
                supplierOrNotes = "Tạo mặt hàng mới ban đầu",
                date = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis()
            )
            inventoryDao.insertTransaction(initTx)
        }

        return Result.success(item)
    }

    override suspend fun updateItem(item: InventoryItemEntity) {
        inventoryDao.updateItem(item.copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun deleteItem(item: InventoryItemEntity) {
        inventoryDao.deleteItem(item)
    }
}
