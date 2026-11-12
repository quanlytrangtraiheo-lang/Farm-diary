package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Thực thể lưu trữ danh sách Nhân viên trong cơ sở dữ liệu Room.
 * Phục vụ phân công kỹ thuật viên phối giống, bác sĩ thú y chữa bệnh,
 * người phụ trách các ô chuồng và ghi nhận người thực hiện sự kiện.
 */
@Entity(tableName = "staff_members")
data class StaffEntity(
    @PrimaryKey
    val id: String,
    val code: String,                  // Mã nhân viên (ví dụ: NV-001)
    val fullName: String,              // Họ và tên nhân viên
    val phoneNumber: String,           // Số điện thoại liên hệ
    val role: String,                  // Chức vụ / Vai trò: "Quản lý", "Kỹ thuật viên", "Công nhân", "Thú y"
    val department: String = "",       // Khu vực phụ trách: "Khu nái đẻ", "Khu bầu", "Khu thịt", "Kho vận"
    val status: String = STATUS_ACTIVE,// Trạng thái: "Đang làm việc", "Đã nghỉ việc"
    val joinDate: Long = System.currentTimeMillis(), // Ngày vào làm
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isActive: Boolean
        get() = status == STATUS_ACTIVE

    companion object {
        const val STATUS_ACTIVE = "Đang làm việc"
        const val STATUS_INACTIVE = "Đã nghỉ việc"

        const val ROLE_MANAGER = "Quản lý"
        const val ROLE_TECHNICIAN = "Kỹ thuật viên"
        const val ROLE_WORKER = "Công nhân"
        const val ROLE_VET = "Thú y"
    }
}
