package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.SyncStatus

@Entity(tableName = "sync_logs")
data class SyncLogEntity(
    @PrimaryKey
    val id: String,
    val timestamp: Long,
    val timeFormatted: String, // e.g. "10:38"
    val title: String,         // e.g. "Cập nhật Heo Nái NY-089"
    val subDetail: String,     // e.g. "Phối giống"
    val syncStatus: SyncStatus,
    val hasConflict: Boolean = false,
    val conflictDetail: String? = null
)
