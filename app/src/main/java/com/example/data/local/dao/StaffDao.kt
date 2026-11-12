package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.StaffEntity
import kotlinx.coroutines.flow.Flow

/**
 * Giao diện Data Access Object (DAO) cho thực thể Nhân viên (StaffEntity).
 * Hỗ trợ truy vấn phản ứng (reactive Flow) và các thao tác ghi dữ liệu (suspend).
 */
@Dao
interface StaffDao {

    @Query("SELECT * FROM staff_members ORDER BY code ASC")
    fun getAllStaff(): Flow<List<StaffEntity>>

    @Query("SELECT * FROM staff_members WHERE status = 'Đang làm việc' ORDER BY code ASC")
    fun getActiveStaff(): Flow<List<StaffEntity>>

    @Query("SELECT * FROM staff_members WHERE role = :role AND status = 'Đang làm việc' ORDER BY code ASC")
    fun getStaffByRole(role: String): Flow<List<StaffEntity>>

    @Query("SELECT * FROM staff_members WHERE id = :id")
    fun getStaffById(id: String): Flow<StaffEntity?>

    @Query("SELECT * FROM staff_members WHERE id = :id LIMIT 1")
    suspend fun getStaffByIdOnce(id: String): StaffEntity?

    @Query("SELECT * FROM staff_members WHERE code = :code LIMIT 1")
    suspend fun getStaffByCode(code: String): StaffEntity?

    @Query("SELECT * FROM staff_members WHERE fullName LIKE '%' || :query || '%' OR code LIKE '%' || :query || '%' OR phoneNumber LIKE '%' || :query || '%' OR role LIKE '%' || :query || '%' ORDER BY code ASC")
    fun searchStaff(query: String): Flow<List<StaffEntity>>

    @Query("SELECT COUNT(*) FROM staff_members WHERE status = 'Đang làm việc'")
    suspend fun countActiveStaff(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: StaffEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaffList(staffList: List<StaffEntity>)

    @Update
    suspend fun updateStaff(staff: StaffEntity)

    @Delete
    suspend fun deleteStaff(staff: StaffEntity)

    @Query("DELETE FROM staff_members WHERE id = :id")
    suspend fun deleteStaffById(id: String)
}
