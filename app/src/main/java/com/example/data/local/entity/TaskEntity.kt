package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.SyncStatus
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import com.example.data.model.TaskType

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val taskType: TaskType,
    val dueDate: Long,
    val timeLabel: String = "08:30",
    val assignedTo: String = "Kỹ thuật viên chuồng",
    val status: TaskStatus = TaskStatus.TODO,
    val priority: TaskPriority = TaskPriority.NORMAL,
    val referenceId: String, // e.g. "Lô LH-204", "Nái NY-089"
    val locationInfo: String, // e.g. "Chuồng 3", "Khu phối"
    val note: String = "",
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val updatedAt: Long = System.currentTimeMillis(),
    val version: Int = 1
)
