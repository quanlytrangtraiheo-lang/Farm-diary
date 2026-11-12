package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.SyncStatus

/**
 * Thực thể lưu trữ các Sự kiện nghiệp vụ trang trại (Farm Events):
 * - Phối giống (Mating / Insemination)
 * - Kiểm tra thai (Pregnancy Ultrasound / Diagnosis)
 * - Cai sữa (Weaning)
 * - Tiêm phòng & Tiêm thuốc (Vaccination / Medication / Iron Injection)
 * - Chuyển chuồng (Barn Transfer / Movement)
 */
@Entity(
    tableName = "farm_events",
    foreignKeys = [
        ForeignKey(
            entity = PigEntity::class,
            parentColumns = ["id"],
            childColumns = ["pigId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["pigId"])]
)
data class EventEntity(
    @PrimaryKey
    val id: String,
    val pigId: String,
    val eventType: String,
    val eventDate: Long,
    val description: String,
    val metrics: String = "",
    val recordedByUserId: String = "Nguyễn Văn Tuấn",
    val targetBarn: String = "",             // Chuồng chuyển đến (Sự kiện Chuyển chuồng)
    val dosageOrMedicine: String = "",       // Tên thuốc & Liều lượng (Sự kiện Tiêm)
    val pregnancyResult: String = "",        // Kết quả khám thai: Đậu thai (+), Không đậu (-), Nghi ngờ (?)
    val pigCountAffected: Int = 1,           // Số lượng con liên quan
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val updatedAt: Long = System.currentTimeMillis(),
    val version: Int = 1
) {
    companion object {
        const val TYPE_MATING = "PHOI_GIONG"                     // 1. Phối giống
        const val TYPE_PREGNANCY_CHECK = "KIEM_TRA_THAI"         // 2. Kiểm tra thai
        const val TYPE_WEANING = "CAI_SUA"                       // 3. Cai sữa
        const val TYPE_VACCINATION = "TIEM_PHONG"                // 4. Tiêm (Vaccine/Sắt/Kháng sinh)
        const val TYPE_TRANSFER = "CHUYEN_CHUONG"                // 5. Chuyển chuồng
        const val TYPE_FARROWING = "SINH_DE"                     // Sự kiện đẻ
        const val TYPE_CULLING = "LOAI_THAI"                     // Sự kiện loại thải
        const val TYPE_WEIGHING = "CAN_TRONG_LUONG"              // Sự kiện cân heo

        fun getEventLabel(type: String): String = when (type) {
            TYPE_MATING -> "Phối giống"
            TYPE_PREGNANCY_CHECK -> "Kiểm tra thai"
            TYPE_WEANING -> "Cai sữa"
            TYPE_VACCINATION -> "Tiêm phòng / Tiêm thuốc"
            TYPE_TRANSFER -> "Chuyển chuồng"
            TYPE_FARROWING -> "Sinh đẻ"
            TYPE_CULLING -> "Loại thải"
            TYPE_WEIGHING -> "Cân trọng lượng"
            else -> type
        }
    }
}
