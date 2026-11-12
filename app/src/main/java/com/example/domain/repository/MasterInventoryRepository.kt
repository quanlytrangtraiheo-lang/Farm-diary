package com.example.domain.repository

import com.example.data.local.entity.InventoryEntity
import com.example.data.local.entity.IssueEntity
import com.example.data.local.entity.MasterItemEntity
import com.example.data.local.entity.ReceiptEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface quản lý mô hình MasterItem - Receipt - Issue - Inventory
 * phục vụ kiến trúc Offline-First cho trang trại chăn nuôi heo.
 */
interface MasterInventoryRepository {
    val allMasterItems: Flow<List<MasterItemEntity>>
    val allReceipts: Flow<List<ReceiptEntity>>
    val allIssues: Flow<List<IssueEntity>>
    val allInventories: Flow<List<InventoryEntity>>

    fun getMasterItemsByType(type: String): Flow<List<MasterItemEntity>>
    fun getReceiptsByItemId(itemId: String): Flow<List<ReceiptEntity>>
    fun getIssuesByItemId(itemId: String): Flow<List<IssueEntity>>
    fun getInventoryByItemId(itemId: String): Flow<InventoryEntity?>

    suspend fun recordReceipt(
        itemId: String,
        quantity: Float,
        importPrice: Long,
        date: Long,
        supplier: String = "",
        note: String = ""
    ): Result<ReceiptEntity>

    suspend fun recordIssue(
        itemId: String,
        quantity: Float,
        targetBarnId: String,
        date: Long,
        note: String = ""
    ): Result<IssueEntity>

    suspend fun createMasterItem(
        itemId: String,
        itemType: String,
        name: String,
        unit: String,
        defaultPrice: Long,
        minStockAlert: Float = 10f,
        code: String = "",
        description: String = "",
        safetyDaysThreshold: Int = 3,
        initialStock: Float = 0f
    ): Result<MasterItemEntity>
}
