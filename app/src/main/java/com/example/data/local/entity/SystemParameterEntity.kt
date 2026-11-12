package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Thực thể lưu trữ tham số hệ thống cấu hình cho trang trại:
 * - BOAR: Danh mục heo đực giống / Lô tinh phối
 * - STAFF: Danh mục nhân sự, kỹ thuật viên, bác sĩ thú y
 * - BREED: Danh mục các giống heo nuôi tại trại
 * - FEED: Danh mục cám & thức ăn chăn nuôi
 * - MEDICINE: Danh mục thuốc thú y & vắc-xin
 * - SUPPLIES: Danh mục vật tư chuồng trại & dụng cụ
 * - BARN: Danh mục chuồng nuôi / ô nuôi
 * - CULLING_REASON: Danh mục nguyên nhân loại thải heo
 * - VACCINE: Danh mục vắc-xin và phác đồ tiêm
 */
@Entity(tableName = "system_parameters")
data class SystemParameterEntity(
    @PrimaryKey
    val id: String,
    val category: String,          // BOAR, STAFF, BREED, FEED, MEDICINE, SUPPLIES, BARN, CULLING_REASON, VACCINE
    val name: String,              // Tên hiển thị (Tên đực, Họ tên NV, Tên giống, Tên cám, Tên thuốc, Tên vật tư...)
    val code: String,              // Mã tham số (ví dụ: D01, NV01, B_LANDRACE, CAM_552...)
    val description: String = "",  // Ghi chú / mô tả kỹ thuật / liều dùng / đặc điểm
    val unit: String = "",         // Đơn vị tính (Kg, ml, Lọ, Liều, Bao 25kg, Cái, Hộp...)
    val displayOrder: Int = 0,     // Thứ tự hiển thị
    val isActive: Boolean = true,  // Trạng thái đang sử dụng
    val createdAt: Long = System.currentTimeMillis(),
    val subType: String = "",      // Loại thuốc / Chức vụ NV / Nguồn gốc Đực / Phân loại giống
    val secondaryValue: String = "", // Số điện thoại NV / Giống Đực / Nhà cung cấp Cám
    val numericValue: Double = 0.0,  // Đơn giá (VNĐ) hoặc Số lượng liều tinh tồn
    val statusText: String = "Đang hoạt động", // Trạng thái: "Đang hoạt động", "Đã hết", "Hủy", "Đang làm việc", "Đã nghỉ việc", "Còn hàng"...
    val safetyDaysThreshold: Int = 3,  // Ngưỡng ngày an toàn (Mặc định: 3 ngày)
    val minStockThreshold: Double = 20.0 // Mức tồn kho tối thiểu (Kg hoặc Bao, Mặc định: 20 bao)
) {
    companion object {
        const val CATEGORY_BOAR = "BOAR"
        const val CATEGORY_STAFF = "STAFF"
        const val CATEGORY_BREED = "BREED"
        const val CATEGORY_FEED = "FEED"
        const val CATEGORY_MEDICINE = "MEDICINE"
        const val CATEGORY_SUPPLIES = "SUPPLIES"
        const val CATEGORY_BARN = "BARN"
        const val CATEGORY_CULLING_REASON = "CULLING_REASON"
        const val CATEGORY_VACCINE = "VACCINE"
        const val CATEGORY_FEED_RATION = "FEED_RATION"

        // 5 Nhóm sinh lý cấu hình khẩu phần cám
        const val STAGE_PREGNANT_SOW = "NAI_MANG_THAI"
        const val STAGE_LACTATING_SOW = "NAI_NUOI_CON"
        const val STAGE_WEANED_WAITING_SOW = "NAI_CHO_PHOI"
        const val STAGE_SUCKLING_PIGLET = "HEO_CON_THEO_ME"
        const val STAGE_GROWER_FINISHER = "HEO_THIT"
    }
}
