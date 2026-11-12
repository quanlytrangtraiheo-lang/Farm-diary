package com.example.data.repository

import com.example.data.local.dao.InventoryEntityDao
import com.example.data.local.dao.IssueDao
import com.example.data.local.dao.MasterItemDao
import com.example.data.local.dao.ReceiptDao
import com.example.data.local.entity.InventoryEntity
import com.example.data.local.entity.IssueEntity
import com.example.data.local.entity.MasterItemEntity
import com.example.data.local.entity.ReceiptEntity
import com.example.domain.repository.MasterInventoryRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class MasterInventoryRepositoryImpl(
    private val masterItemDao: MasterItemDao,
    private val receiptDao: ReceiptDao,
    private val issueDao: IssueDao,
    private val inventoryDao: InventoryEntityDao
) : MasterInventoryRepository {

    override val allMasterItems: Flow<List<MasterItemEntity>> = masterItemDao.getAllMasterItems()
    override val allReceipts: Flow<List<ReceiptEntity>> = receiptDao.getAllReceipts()
    override val allIssues: Flow<List<IssueEntity>> = issueDao.getAllIssues()
    override val allInventories: Flow<List<InventoryEntity>> = inventoryDao.getAllInventories()

    override fun getMasterItemsByType(type: String): Flow<List<MasterItemEntity>> =
        masterItemDao.getMasterItemsByType(type)

    override fun getReceiptsByItemId(itemId: String): Flow<List<ReceiptEntity>> =
        receiptDao.getReceiptsByItemId(itemId)

    override fun getIssuesByItemId(itemId: String): Flow<List<IssueEntity>> =
        issueDao.getIssuesByItemId(itemId)

    override fun getInventoryByItemId(itemId: String): Flow<InventoryEntity?> =
        inventoryDao.getInventoryByItemId(itemId)

    override suspend fun recordReceipt(
        itemId: String,
        quantity: Float,
        importPrice: Long,
        date: Long,
        supplier: String,
        note: String
    ): Result<ReceiptEntity> {
        val masterItem = masterItemDao.getMasterItemById(itemId)
            ?: return Result.failure(IllegalArgumentException("Không tìm thấy hàng hóa MasterItem (ID: $itemId)"))

        if (quantity <= 0f) {
            return Result.failure(IllegalArgumentException("Số lượng nhập phải lớn hơn 0"))
        }

        val receiptId = "REC_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}"
        val totalCost = (quantity * importPrice).toLong()

        val receipt = ReceiptEntity(
            receiptId = receiptId,
            itemId = itemId,
            date = date,
            quantity = quantity,
            importPrice = importPrice,
            totalCost = totalCost,
            supplier = supplier,
            note = note,
            createdAt = System.currentTimeMillis()
        )
        receiptDao.insertReceipt(receipt)

        // Cập nhật hoặc khởi tạo Tồn kho tức thời (Inventory)
        val currentInv = inventoryDao.getInventoryByIdOnce(itemId)
        val oldStock = currentInv?.currentStock?.coerceAtLeast(0f) ?: 0f
        val newStock = oldStock + quantity
        val oldValue = (oldStock * (currentInv?.averageUnitPrice ?: importPrice)).toLong()
        val inValue = totalCost
        val newAvgPrice = if (newStock > 0f) {
            ((oldValue + inValue) / newStock).toLong()
        } else importPrice

        val newStockValue = (newStock * newAvgPrice).toLong()

        val updatedInv = InventoryEntity(
            itemId = itemId,
            currentStock = newStock,
            stockValue = newStockValue,
            lastUnitPrice = importPrice,
            averageUnitPrice = newAvgPrice,
            updatedAt = System.currentTimeMillis()
        )
        inventoryDao.insertInventory(updatedInv)

        // Đồng bộ ngược lại giá tham chiếu MasterItem
        masterItemDao.updateMasterItem(
            masterItem.copy(
                defaultPrice = importPrice,
                updatedAt = System.currentTimeMillis()
            )
        )

        return Result.success(receipt)
    }

    override suspend fun recordIssue(
        itemId: String,
        quantity: Float,
        targetBarnId: String,
        date: Long,
        note: String
    ): Result<IssueEntity> {
        val masterItem = masterItemDao.getMasterItemById(itemId)
            ?: return Result.failure(IllegalArgumentException("Không tìm thấy hàng hóa MasterItem (ID: $itemId)"))

        if (quantity <= 0f) {
            return Result.failure(IllegalArgumentException("Số lượng xuất phải lớn hơn 0"))
        }

        val currentInv = inventoryDao.getInventoryByIdOnce(itemId)
        val currentStock = currentInv?.currentStock ?: 0f

        // RÀNG BUỘC CHẶN XUẤT ÂM
        if (currentInv == null || quantity > currentStock) {
            return Result.failure(
                IllegalArgumentException("Chặn xuất âm: Số lượng xuất ($quantity ${masterItem.unit}) vượt quá tồn kho hiện tại ($currentStock ${masterItem.unit})!")
            )
        }

        val issueId = "ISS_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}"
        val unitPrice = currentInv.averageUnitPrice
        val totalCost = (quantity * unitPrice).toLong()

        val issue = IssueEntity(
            issueId = issueId,
            itemId = itemId,
            date = date,
            quantity = quantity,
            targetBarnId = targetBarnId,
            note = note,
            unitPrice = unitPrice,
            totalCost = totalCost,
            createdAt = System.currentTimeMillis()
        )
        issueDao.insertIssue(issue)

        // Trừ trực tiếp số lượng tồn kho
        val newStock = (currentStock - quantity).coerceAtLeast(0f)
        val newStockValue = (newStock * unitPrice).toLong()

        val updatedInv = currentInv.copy(
            currentStock = newStock,
            stockValue = newStockValue,
            updatedAt = System.currentTimeMillis()
        )
        inventoryDao.insertInventory(updatedInv)

        return Result.success(issue)
    }

    override suspend fun createMasterItem(
        itemId: String,
        itemType: String,
        name: String,
        unit: String,
        defaultPrice: Long,
        minStockAlert: Float,
        code: String,
        description: String,
        safetyDaysThreshold: Int,
        initialStock: Float
    ): Result<MasterItemEntity> {
        val existing = masterItemDao.getMasterItemById(itemId)
        if (existing != null) {
            return Result.failure(IllegalArgumentException("Mã hàng hóa '$itemId' đã tồn tại!"))
        }

        val masterItem = MasterItemEntity(
            itemId = itemId,
            itemType = itemType,
            name = name.trim(),
            unit = unit.trim(),
            defaultPrice = defaultPrice,
            minStockAlert = minStockAlert,
            code = code.trim(),
            description = description.trim(),
            safetyDaysThreshold = safetyDaysThreshold,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        masterItemDao.insertMasterItem(masterItem)

        // Khởi tạo tồn kho ban đầu
        val initInv = InventoryEntity(
            itemId = itemId,
            currentStock = initialStock.coerceAtLeast(0f),
            stockValue = (initialStock.coerceAtLeast(0f) * defaultPrice).toLong(),
            lastUnitPrice = defaultPrice,
            averageUnitPrice = defaultPrice,
            updatedAt = System.currentTimeMillis()
        )
        inventoryDao.insertInventory(initInv)

        if (initialStock > 0f) {
            val receipt = ReceiptEntity(
                receiptId = "REC_INIT_${System.currentTimeMillis()}",
                itemId = itemId,
                date = System.currentTimeMillis(),
                quantity = initialStock,
                importPrice = defaultPrice,
                totalCost = (initialStock * defaultPrice).toLong(),
                supplier = "Tồn kho ban đầu",
                note = "Khởi tạo mặt hàng mới"
            )
            receiptDao.insertReceipt(receipt)
        }

        return Result.success(masterItem)
    }
}
