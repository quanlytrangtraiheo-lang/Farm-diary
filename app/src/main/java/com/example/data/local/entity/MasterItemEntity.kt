package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Thực thể Danh mục gốc (Master Data trong Tham số hệ thống).
 * Định nghĩa các mặt hàng dùng chung toàn trang trại để kết nối với Tab Kho:
 * - Cám (FEED): Cám nái bầu, Cám nái đẻ, Cám heo thịt...
 * - Thuốc (MED): Vắc-xin, Kháng sinh, Bổ trợ...
 * - Vật tư (SUPPLY): Xilanh, Thẻ tai, Đèn úm...
 */
@Entity(tableName = "master_items")
data class MasterItemEntity(
    @PrimaryKey
    val itemId: String,              // UUID hoặc Mã tự sinh (vd: CAM-NB-01, VTM-02)
    val itemType: String,            // FEED (Cám) | MED (Thuốc) | SUPPLY (Vật tư)
    val name: String,                // Tên hàng hóa (Cám nái bầu, Vắc-xin Dịch tả...)
    val unit: String,                // Đơn vị tính (Bao 25kg, Liều, Lọ 100ml, Cái...)
    val defaultPrice: Long,          // Đơn giá tham chiếu (VNĐ)
    val minStockAlert: Float = 10f,  // Mức tồn an toàn / Ngưỡng ngày dùng tối thiểu
    val code: String = "",           // Mã danh mục (vd: CAM_NAI_MANG_THAI)
    val description: String = "",    // Ghi chú kỹ thuật, quy cách đóng gói
    val safetyDaysThreshold: Int = 3,// Ngưỡng ngày dùng an toàn
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_FEED = "FEED"
        const val TYPE_MED = "MED"
        const val TYPE_SUPPLY = "SUPPLY"

        fun getTypeLabel(type: String): String = when (type) {
            TYPE_FEED -> "Cám"
            TYPE_MED -> "Thuốc"
            TYPE_SUPPLY -> "Vật tư"
            else -> type
        }
    }
}

typealias MasterItem = MasterItemEntity
