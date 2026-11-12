package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Thực thể Phiếu Nhập kho (Receipt) trong kiến trúc Offline-First.
 * Ghi nhận mỗi lần nhập hàng vào kho, kế thừa trực tiếp item_id từ MasterItem (Khóa ngoại Foreign Key).
 */
@Entity(
    tableName = "receipts",
    foreignKeys = [
        ForeignKey(
            entity = MasterItemEntity::class,
            parentColumns = ["itemId"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["itemId"]),
        Index(value = ["date"])
    ]
)
data class ReceiptEntity(
    @PrimaryKey
    val receiptId: String,           // Mã phiếu nhập (PK)
    val itemId: String,              // Mã hàng hóa (FK -> MasterItem)
    val date: Long,                  // Ngày nhập kho
    val quantity: Float,             // Số lượng nhập
    val importPrice: Long,           // Đơn giá thực nhập (VNĐ)
    val totalCost: Long = (quantity * importPrice).toLong(), // Thành tiền
    val supplier: String = "",       // Nhà cung cấp / Số hóa đơn
    val note: String = "",           // Ghi chú phiếu nhập
    val createdAt: Long = System.currentTimeMillis()
)

typealias Receipt = ReceiptEntity
