package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.SowReproductiveStatus
import com.example.data.model.SyncStatus

/**
 * Thực thể chính đại diện cho Heo (Nái cá thể hoặc Lô heo thịt) trong cơ sở dữ liệu Room.
 * Tuân thủ kiến trúc Clean Architecture:
 * - isBatch = false: Heo nái cá thể (Quản lý theo mã tai, lứa đẻ, trạng thái sinh sản, ngày phối, ngày dự sinh)
 * - isBatch = true: Lô heo thịt (Quản lý theo số lượng con, tuần tuổi, chuồng thịt, khối lượng TB)
 */
@Entity(
    tableName = "pigs",
    indices = [
        Index(value = ["tagCode"]),
        Index(value = ["location"]),
        Index(value = ["status"]),
        Index(value = ["isBatch"]),
        Index(value = ["syncStatus"])
    ]
)
data class PigEntity(
    @PrimaryKey
    val id: String,
    val tagCode: String,               // Mã định danh / Số tai (Nái: N001) hoặc Mã lô (Thịt: LT001)
    val birthDate: Long = System.currentTimeMillis(), // Ngày sinh của heo hoặc ngày bắt đầu lô
    val isBatch: Boolean = false,      // Phân biệt: Nuôi theo lô thịt (true) hoặc Nái cá thể (false)
    val quantity: Int = 1,             // Số lượng con (Nái: 1 con, Lô thịt: 50-200 con)
    val breed: String,                 // Giống heo (Landrace, Yorkshire, Duroc, Pietrain, F1, F2...)
    val gender: String,                // Giới tính (Cái, Đực, Hỗn hợp)
    val status: SowReproductiveStatus, // Trạng thái sinh sản / sinh trưởng
    val location: String,              // Vị trí chuồng / ô nuôi
    val parity: Int = 0,               // Lứa đẻ hiện tại (Lứa 0 = Hậu bị, Lứa 1, 2...)
    val currentCycleId: String? = null,// Khóa ngoại trỏ đến chu kỳ phối hiện tại
    val currentWeightKg: Float = 0f,   // Trọng lượng hiện tại hoặc TB của lô (kg)
    val lastMatingDate: Long? = null,  // Ngày phối giống gần nhất (mốc D)
    val expectedFarrowingDate: Long? = null, // Ngày dự sinh (D + 114 ngày)
    val hasOverdueVaccine: Boolean = false,
    val hasUltrasoundWarning: Boolean = false,
    val hasDelayedHeatWarning: Boolean = false,
    val hasProlongedGestationWarning: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val updatedAt: Long = System.currentTimeMillis(),
    val version: Int = 1,
    val conflictLog: String? = null
) {
    /**
     * Xác định đây là heo nái cá thể
     */
    val isSow: Boolean
        get() = !isBatch

    /**
     * Xác định đây là lô heo thịt
     */
    val isGrowerBatch: Boolean
        get() = isBatch

    /**
     * Tuần tuổi tính toán từ ngày sinh / nhập đàn đến hiện tại
     */
    val ageWeeks: Double
        get() = ((System.currentTimeMillis() - birthDate) / (86_400_000.0 * 7.0)).coerceAtLeast(0.0)

    companion object {
        const val TYPE_SOW = "HEO_NAI"
        const val TYPE_GROWER_BATCH = "LO_HEO_THIT"
    }
}
