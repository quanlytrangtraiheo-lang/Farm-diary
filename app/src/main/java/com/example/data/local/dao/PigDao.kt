package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.PigEntity
import com.example.data.model.SowReproductiveStatus
import com.example.data.model.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * Giao diện Data Access Object (DAO) cho thực thể Heo (Nái/Thịt).
 * Hỗ trợ truy vấn phản ứng (reactive Flow) và các thao tác ghi dữ liệu (suspend).
 */
@Dao
interface PigDao {
    @Query("SELECT * FROM pigs ORDER BY tagCode ASC")
    fun getAllPigs(): Flow<List<PigEntity>>

    // Truy vấn riêng cho Heo Nái cá thể (isBatch = 0)
    @Query("SELECT * FROM pigs WHERE isBatch = 0 ORDER BY tagCode ASC")
    fun getAllSows(): Flow<List<PigEntity>>

    // Truy vấn riêng cho Lô Heo Thịt (isBatch = 1)
    @Query("SELECT * FROM pigs WHERE isBatch = 1 ORDER BY tagCode ASC")
    fun getAllGrowerBatches(): Flow<List<PigEntity>>

    @Query("SELECT * FROM pigs WHERE isBatch = 0 AND status = :status ORDER BY tagCode ASC")
    fun getSowsByStatus(status: SowReproductiveStatus): Flow<List<PigEntity>>

    @Query("SELECT * FROM pigs WHERE location = :location ORDER BY tagCode ASC")
    fun getPigsByLocation(location: String): Flow<List<PigEntity>>

    @Query("SELECT * FROM pigs WHERE tagCode LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%' ORDER BY tagCode ASC")
    fun searchPigs(query: String): Flow<List<PigEntity>>

    @Query("SELECT * FROM pigs WHERE id = :id")
    fun getPigById(id: String): Flow<PigEntity?>

    @Query("SELECT * FROM pigs WHERE id = :id LIMIT 1")
    suspend fun getPigByIdOnce(id: String): PigEntity?

    @Query("SELECT * FROM pigs WHERE tagCode = :tagCode LIMIT 1")
    suspend fun getPigByTagCode(tagCode: String): PigEntity?

    @Query("SELECT COUNT(*) FROM pigs")
    fun getTotalPigCount(): Flow<Int>

    @Query("SELECT SUM(quantity) FROM pigs")
    fun getTotalHeadCount(): Flow<Int?>

    @Query("SELECT * FROM pigs WHERE syncStatus != :syncedStatus")
    suspend fun getUnsyncedPigs(syncedStatus: SyncStatus = SyncStatus.SYNCED): List<PigEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPig(pig: PigEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPigs(pigs: List<PigEntity>)

    @Update
    suspend fun updatePig(pig: PigEntity)

    @Delete
    suspend fun deletePig(pig: PigEntity)

    @Query("DELETE FROM pigs WHERE id = :id")
    suspend fun deletePigById(id: String)

    @Query("SELECT COUNT(*) FROM pigs WHERE syncStatus != :syncedStatus")
    fun getPendingSyncCount(syncedStatus: SyncStatus = SyncStatus.SYNCED): Flow<Int>

    @Query("UPDATE pigs SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: SyncStatus)

    @Query("UPDATE pigs SET syncStatus = :syncedStatus WHERE syncStatus != :syncedStatus")
    suspend fun markAllAsSynced(syncedStatus: SyncStatus = SyncStatus.SYNCED)
}
