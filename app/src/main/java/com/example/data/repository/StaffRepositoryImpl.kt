package com.example.data.repository

import com.example.data.local.dao.StaffDao
import com.example.data.local.entity.StaffEntity
import com.example.domain.repository.StaffRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Triển khai Data Layer của StaffRepository, tương tác trực tiếp với StaffDao trong Room Database.
 */
class StaffRepositoryImpl(
    private val staffDao: StaffDao
) : StaffRepository {

    override val allStaff: Flow<List<StaffEntity>>
        get() = staffDao.getAllStaff()

    override val activeStaff: Flow<List<StaffEntity>>
        get() = staffDao.getActiveStaff()

    override fun getStaffByRole(role: String): Flow<List<StaffEntity>> {
        return staffDao.getStaffByRole(role)
    }

    override fun searchStaff(query: String): Flow<List<StaffEntity>> {
        return staffDao.searchStaff(query)
    }

    override fun getStaffById(id: String): Flow<StaffEntity?> {
        return staffDao.getStaffById(id)
    }

    override suspend fun getStaffByIdOnce(id: String): StaffEntity? {
        return staffDao.getStaffByIdOnce(id)
    }

    override suspend fun getStaffByCode(code: String): StaffEntity? {
        return staffDao.getStaffByCode(code)
    }

    override suspend fun addStaff(
        code: String,
        fullName: String,
        phoneNumber: String,
        role: String,
        department: String,
        status: String
    ): String {
        val id = "staff-${UUID.randomUUID()}"
        val now = System.currentTimeMillis()
        val staff = StaffEntity(
            id = id,
            code = code,
            fullName = fullName,
            phoneNumber = phoneNumber,
            role = role,
            department = department,
            status = status,
            joinDate = now,
            createdAt = now,
            updatedAt = now
        )
        staffDao.insertStaff(staff)
        return id
    }

    override suspend fun updateStaff(staff: StaffEntity) {
        staffDao.updateStaff(staff.copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun deleteStaff(staff: StaffEntity) {
        staffDao.deleteStaff(staff)
    }

    override suspend fun deleteStaffById(id: String) {
        staffDao.deleteStaffById(id)
    }
}
