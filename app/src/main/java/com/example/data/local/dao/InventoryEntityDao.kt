package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.InventoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) cho Bảng Tồn kho hiện thời (Inventory).
 */
@Dao
interface InventoryEntityDao {
    @Query("SELECT * FROM inventories ORDER BY currentStock ASC")
    fun getAllInventories(): Flow<List<InventoryEntity>>

    @Query("SELECT * FROM inventories WHERE itemId = :itemId LIMIT 1")
    fun getInventoryByItemId(itemId: String): Flow<InventoryEntity?>

    @Query("SELECT * FROM inventories WHERE itemId = :itemId LIMIT 1")
    suspend fun getInventoryByIdOnce(itemId: String): InventoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventory(inventory: InventoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllInventories(inventories: List<InventoryEntity>)

    @Update
    suspend fun updateInventory(inventory: InventoryEntity)

    @Query("UPDATE inventories SET currentStock = :currentStock, stockValue = :stockValue, lastUnitPrice = :lastPrice, averageUnitPrice = :avgPrice, updatedAt = :updatedAt WHERE itemId = :itemId")
    suspend fun updateStock(
        itemId: String,
        currentStock: Float,
        stockValue: Long,
        lastPrice: Long,
        avgPrice: Long,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Delete
    suspend fun deleteInventory(inventory: InventoryEntity)

    @Query("SELECT COUNT(*) FROM inventories")
    suspend fun countInventories(): Int
}
