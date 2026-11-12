package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.InventoryItemEntity
import com.example.data.local.entity.InventoryTransactionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Giao diện Data Access Object (DAO) cho Kho hàng (Inventory):
 * - Quản lý tồn kho các mặt hàng: Cám, Thuốc/Vaccine, Vật tư
 * - Lịch sử giao dịch nhập/xuất kho (Inbound / Outbound Transactions)
 */
@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_items ORDER BY category ASC, name ASC")
    fun getAllItems(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE category = :category ORDER BY name ASC")
    fun getItemsByCategory(category: String): Flow<List<InventoryItemEntity>>

    // Cảnh báo hàng tồn dưới ngưỡng an toàn (Low stock alert)
    @Query("SELECT * FROM inventory_items WHERE currentStock <= minAlertStock ORDER BY currentStock ASC")
    fun getLowStockItems(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchItems(query: String): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    suspend fun getItemById(id: String): InventoryItemEntity?

    @Query("SELECT * FROM inventory_items WHERE name = :name LIMIT 1")
    suspend fun getItemByName(name: String): InventoryItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<InventoryItemEntity>)

    @Update
    suspend fun updateItem(item: InventoryItemEntity)

    @Delete
    suspend fun deleteItem(item: InventoryItemEntity)

    @Query("SELECT COUNT(*) FROM inventory_items")
    suspend fun countItems(): Int

    // ==========================================
    // Giao dịch kho (Inbound & Outbound)
    // ==========================================

    @Query("SELECT * FROM inventory_transactions ORDER BY date DESC, createdAt DESC")
    fun getAllTransactions(): Flow<List<InventoryTransactionEntity>>

    @Query("SELECT * FROM inventory_transactions WHERE type = :type ORDER BY date DESC, createdAt DESC")
    fun getTransactionsByType(type: String): Flow<List<InventoryTransactionEntity>>

    @Query("SELECT * FROM inventory_transactions WHERE itemId = :itemId ORDER BY date DESC")
    fun getTransactionsByItem(itemId: String): Flow<List<InventoryTransactionEntity>>

    @Query("SELECT * FROM inventory_transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<InventoryTransactionEntity>>

    @Query("SELECT * FROM inventory_transactions ORDER BY date DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int = 20): Flow<List<InventoryTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: InventoryTransactionEntity)
}
