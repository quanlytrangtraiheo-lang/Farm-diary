package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow

/**
 * Giao diện Data Access Object (DAO) cho các Sự kiện nghiệp vụ:
 * Phối giống, Kiểm tra thai, Cai sữa, Tiêm, Chuyển chuồng.
 */
@Dao
interface EventDao {
    @Query("SELECT * FROM farm_events ORDER BY eventDate DESC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM farm_events WHERE pigId = :pigId ORDER BY eventDate DESC")
    fun getEventsForPig(pigId: String): Flow<List<EventEntity>>

    @Query("SELECT * FROM farm_events WHERE eventType = :eventType ORDER BY eventDate DESC")
    fun getEventsByType(eventType: String): Flow<List<EventEntity>>

    // 1. Sự kiện Phối giống
    @Query("SELECT * FROM farm_events WHERE eventType = 'PHOI_GIONG' ORDER BY eventDate DESC")
    fun getMatingEvents(): Flow<List<EventEntity>>

    // 2. Sự kiện Kiểm tra thai
    @Query("SELECT * FROM farm_events WHERE eventType = 'KIEM_TRA_THAI' ORDER BY eventDate DESC")
    fun getPregnancyCheckEvents(): Flow<List<EventEntity>>

    // 3. Sự kiện Cai sữa
    @Query("SELECT * FROM farm_events WHERE eventType = 'CAI_SUA' ORDER BY eventDate DESC")
    fun getWeaningEvents(): Flow<List<EventEntity>>

    // 4. Sự kiện Tiêm (Vaccine, sắt, kháng sinh)
    @Query("SELECT * FROM farm_events WHERE eventType = 'TIEM_PHONG' ORDER BY eventDate DESC")
    fun getVaccinationEvents(): Flow<List<EventEntity>>

    // 5. Sự kiện Chuyển chuồng
    @Query("SELECT * FROM farm_events WHERE eventType = 'CHUYEN_CHUONG' ORDER BY eventDate DESC")
    fun getTransferEvents(): Flow<List<EventEntity>>

    // Truy vấn theo khoảng thời gian
    @Query("SELECT * FROM farm_events WHERE eventDate BETWEEN :startDate AND :endDate ORDER BY eventDate DESC")
    fun getEventsByDateRange(startDate: Long, endDate: Long): Flow<List<EventEntity>>

    // Sự kiện gần đây
    @Query("SELECT * FROM farm_events ORDER BY eventDate DESC LIMIT :limit")
    fun getRecentEvents(limit: Int = 20): Flow<List<EventEntity>>

    @Query("SELECT * FROM farm_events WHERE id = :id LIMIT 1")
    suspend fun getEventById(id: String): EventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    @Query("DELETE FROM farm_events WHERE id = :id")
    suspend fun deleteEventById(id: String)
}
