package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.MasterItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) cho Danh mục gốc Master Data (MasterItem).
 */
@Dao
interface MasterItemDao {
    @Query("SELECT * FROM master_items WHERE isActive = 1 ORDER BY itemType ASC, name ASC")
    fun getAllMasterItems(): Flow<List<MasterItemEntity>>

    @Query("SELECT * FROM master_items WHERE itemType = :itemType AND isActive = 1 ORDER BY name ASC")
    fun getMasterItemsByType(itemType: String): Flow<List<MasterItemEntity>>

    @Query("SELECT * FROM master_items WHERE itemId = :itemId LIMIT 1")
    suspend fun getMasterItemById(itemId: String): MasterItemEntity?

    @Query("SELECT * FROM master_items WHERE code = :code LIMIT 1")
    suspend fun getMasterItemByCode(code: String): MasterItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMasterItem(item: MasterItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMasterItems(items: List<MasterItemEntity>)

    @Update
    suspend fun updateMasterItem(item: MasterItemEntity)

    @Delete
    suspend fun deleteMasterItem(item: MasterItemEntity)

    @Query("SELECT COUNT(*) FROM master_items")
    suspend fun countMasterItems(): Int
}
