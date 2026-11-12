package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Thực thể Bảng Tồn kho hiện thời (Current Inventory Stock) trong kiến trúc Offline-First.
 * Quản lý số lượng tồn kho tức thời:
 * - Số lượng tồn = Sum(Nhập) - Sum(Xuất)
 * - Giá trị tồn = Số lượng tồn * Đơn giá bình quân/gần nhất
 * Kế thừa trực tiếp item_id từ MasterItem (Khóa chính PK đồng thời là Khóa ngoại FK).
 */
@Entity(
    tableName = "inventories",
    foreignKeys = [
        ForeignKey(
            entity = MasterItemEntity::class,
            parentColumns = ["itemId"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["itemId"])
    ]
)
data class InventoryEntity(
    @PrimaryKey
    val itemId: String,              // Mã hàng hóa (PK & FK -> MasterItem)
    val currentStock: Float,         // Số lượng tồn
    val stockValue: Long,            // Giá trị tồn = currentStock * lastUnitPrice
    val lastUnitPrice: Long,         // Giá nhập gần nhất (VNĐ)
    val averageUnitPrice: Long = lastUnitPrice, // Đơn giá bình quân gia quyền
    val updatedAt: Long = System.currentTimeMillis()
)

typealias Inventory = InventoryEntity
