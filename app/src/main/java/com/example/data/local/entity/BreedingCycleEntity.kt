package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.SyncStatus

@Entity(
    tableName = "breeding_cycles",
    foreignKeys = [
        ForeignKey(
            entity = PigEntity::class,
            parentColumns = ["id"],
            childColumns = ["sowId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sowId"])]
)
data class BreedingCycleEntity(
    @PrimaryKey
    val cycleId: String,
    val sowId: String,
    val parity: Int,
    val matingDate: Long,
    val boarCode: String,
    val ultrasoundResult: String? = null, // "Đạt (+)", "Không đạt (-)"
    val ultrasoundDate: Long? = null,
    val expectedFarrowingDate: Long, // D + 114 days
    val actualFarrowingDate: Long? = null,
    val totalBorn: Int = 0,
    val bornAlive: Int = 0,
    val stillborn: Int = 0,
    val mummies: Int = 0,
    val averageBirthWeightKg: Float = 0f,
    val weaningDate: Long? = null,
    val pigletsWeaned: Int = 0,
    val cycleStatus: String = "IN_PROGRESS", // IN_PROGRESS, COMPLETED, FAILED
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val updatedAt: Long = System.currentTimeMillis(),
    val version: Int = 1
)
