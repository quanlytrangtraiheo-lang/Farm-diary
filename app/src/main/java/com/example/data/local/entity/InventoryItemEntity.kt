package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Thực thể lưu trữ hàng tồn kho thực tế trong trang trại:
 * - Cám (FEED): Cám nái mang thai, Cám nuôi con, Cám tập ăn, Cám vỗ béo...
 * - Thuốc (MEDICINE): Iron Dextran, Kháng sinh Amoxicillin, Vắc-xin, Sát trùng...
 * - Vật tư (SUPPLIES): Kim tiêm, Xilanh, Thẻ tai, Đèn úm...
 */
@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val category: String, // "FEED", "MEDICINE", "SUPPLIES"
    val unit: String, // "Bao 25kg", "Lọ 100ml", "Cái", "Hộp 100 kim"
    val currentStock: Float, // Số lượng tồn thực tế
    val lastUnitPrice: Long, // Đơn giá nhập gần nhất (VNĐ)
    val averageUnitPrice: Long, // Đơn giá bình quân gia quyền (VNĐ)
    val minAlertStock: Float = 5f, // Ngưỡng cảnh báo sắp hết hàng
    val notes: String = "",
    val systemParamId: String? = null, // Liên kết danh mục Tham số hệ thống (nếu có)
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Tổng giá trị tồn kho của mặt hàng = Số lượng tồn * Đơn giá bình quân
     */
    val totalStockValue: Long
        get() = (currentStock * averageUnitPrice).toLong()

    /**
     * Cảnh báo hết hàng nếu lượng tồn nhỏ hơn hoặc bằng ngưỡng tối thiểu
     */
    val isLowStock: Boolean
        get() = currentStock <= minAlertStock

    companion object {
        const val CATEGORY_FEED = "FEED"
        const val CATEGORY_MEDICINE = "MEDICINE"
        const val CATEGORY_SUPPLIES = "SUPPLIES"

        fun getCategoryLabel(category: String): String = when (category) {
            CATEGORY_FEED -> "Cám"
            CATEGORY_MEDICINE -> "Thuốc"
            CATEGORY_SUPPLIES -> "Vật tư"
            else -> category
        }
    }
}
