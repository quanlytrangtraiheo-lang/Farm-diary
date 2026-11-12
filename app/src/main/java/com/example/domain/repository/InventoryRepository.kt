package com.example.domain.repository

import com.example.data.local.entity.InventoryItemEntity
import com.example.data.local.entity.InventoryTransactionEntity
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    val allItems: Flow<List<InventoryItemEntity>>
    val allTransactions: Flow<List<InventoryTransactionEntity>>

    fun getItemsByCategory(category: String): Flow<List<InventoryItemEntity>>
    suspend fun getItemById(id: String): InventoryItemEntity?

    suspend fun recordInbound(
        itemId: String,
        quantity: Float,
        unitPrice: Long,
        date: Long,
        supplierOrInvoice: String = "",
        notes: String = ""
    ): Result<InventoryTransactionEntity>

    suspend fun recordOutbound(
        itemId: String,
        quantity: Float,
        targetBarn: String,
        date: Long,
        purposeOrNotes: String = ""
    ): Result<InventoryTransactionEntity>

    suspend fun addNewItem(
        name: String,
        category: String,
        unit: String,
        initialStock: Float,
        unitPrice: Long,
        minAlertStock: Float = 5f,
        notes: String = ""
    ): Result<InventoryItemEntity>

    suspend fun updateItem(item: InventoryItemEntity)
    suspend fun deleteItem(item: InventoryItemEntity)
}
