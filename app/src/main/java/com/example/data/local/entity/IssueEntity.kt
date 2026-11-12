package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Thực thể Phiếu Xuất kho (Issue) trong kiến trúc Offline-First.
 * Ghi nhận mỗi lần xuất hàng phục vụ chuồng nuôi, kế thừa trực tiếp item_id từ MasterItem (Khóa ngoại Foreign Key).
 */
@Entity(
    tableName = "issues",
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
        Index(value = ["date"]),
        Index(value = ["targetBarnId"])
    ]
)
data class IssueEntity(
    @PrimaryKey
    val issueId: String,             // Mã phiếu xuất (PK)
    val itemId: String,              // Mã hàng hóa (FK -> MasterItem)
    val date: Long,                  // Ngày xuất kho
    val quantity: Float,             // Số lượng xuất
    val targetBarnId: String,        // Chuồng nhận hàng (Chuồng Bầu, Chuồng Đẻ, Chuồng Thịt...)
    val note: String = "",           // Ghi chú mục đích dùng
    val unitPrice: Long = 0L,        // Đơn giá xuất (VNĐ)
    val totalCost: Long = (quantity * unitPrice).toLong(), // Phân bổ chi phí hoạt động chuồng
    val createdAt: Long = System.currentTimeMillis()
)

typealias Issue = IssueEntity
