package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Thực thể lưu trữ lịch sử giao dịch kho (Nhập - Xuất):
 * - TYPE_IN: Phiếu Nhập kho (Tăng số lượng tồn kho, lưu đơn giá)
 * - TYPE_OUT: Phiếu Xuất kho (Giảm số lượng tồn kho, ràng buộc không xuất âm, ghi nhận chuồng nhận)
 */
@Entity(tableName = "inventory_transactions")
data class InventoryTransactionEntity(
    @PrimaryKey
    val id: String,
    val type: String, // "IN" (Nhập kho), "OUT" (Xuất kho)
    val itemId: String,
    val itemName: String,
    val category: String, // "FEED", "MEDICINE", "SUPPLIES"
    val unit: String,
    val quantity: Float,
    val unitPrice: Long, // Đơn giá VNĐ
    val totalPrice: Long, // Thành tiền VNĐ = quantity * unitPrice
    val targetBarn: String = "", // Chuồng nhận khi xuất kho (Chuồng Bầu, Chuồng Đẻ, Chuồng Thịt...)
    val supplierOrNotes: String = "", // Tên nhà cung cấp, số HĐ (khi nhập) hoặc mục đích sử dụng (khi xuất)
    val date: Long, // Ngày thực hiện giao dịch
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_IN = "IN"
        const val TYPE_OUT = "OUT"
    }
}
