package com.example.domain.repository

import com.example.data.local.entity.StaffEntity
import kotlinx.coroutines.flow.Flow

/**
 * Domain Repository Interface cho Nhân viên theo Clean Architecture.
 * Tách biệt hoàn toàn tầng Domain khỏi tầng triển khai dữ liệu cục bộ (Room DB).
 */
interface StaffRepository {
    val allStaff: Flow<List<StaffEntity>>
    val activeStaff: Flow<List<StaffEntity>>

    fun getStaffByRole(role: String): Flow<List<StaffEntity>>
    fun searchStaff(query: String): Flow<List<StaffEntity>>
    fun getStaffById(id: String): Flow<StaffEntity?>
    suspend fun getStaffByIdOnce(id: String): StaffEntity?
    suspend fun getStaffByCode(code: String): StaffEntity?

    suspend fun addStaff(
        code: String,
        fullName: String,
        phoneNumber: String,
        role: String,
        department: String = "",
        status: String = StaffEntity.STATUS_ACTIVE
    ): String

    suspend fun updateStaff(staff: StaffEntity)
    suspend fun deleteStaff(staff: StaffEntity)
    suspend fun deleteStaffById(id: String)
}
