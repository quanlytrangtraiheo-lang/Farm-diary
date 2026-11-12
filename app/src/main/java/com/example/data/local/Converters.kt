package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.SowReproductiveStatus
import com.example.data.model.SyncStatus
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import com.example.data.model.TaskType

class Converters {
    @TypeConverter
    fun fromSyncStatus(value: SyncStatus?): String? = value?.name

    @TypeConverter
    fun toSyncStatus(value: String?): SyncStatus =
        value?.let { runCatching { SyncStatus.valueOf(it) }.getOrNull() } ?: SyncStatus.SYNCED

    @TypeConverter
    fun fromSowStatus(value: SowReproductiveStatus?): String? = value?.name

    @TypeConverter
    fun toSowStatus(value: String?): SowReproductiveStatus =
        value?.let { runCatching { SowReproductiveStatus.valueOf(it) }.getOrNull() } ?: SowReproductiveStatus.HAU_BI

    @TypeConverter
    fun fromTaskType(value: TaskType?): String? = value?.name

    @TypeConverter
    fun toTaskType(value: String?): TaskType =
        value?.let { runCatching { TaskType.valueOf(it) }.getOrNull() } ?: TaskType.GENERAL

    @TypeConverter
    fun fromTaskStatus(value: TaskStatus?): String? = value?.name

    @TypeConverter
    fun toTaskStatus(value: String?): TaskStatus =
        value?.let { runCatching { TaskStatus.valueOf(it) }.getOrNull() } ?: TaskStatus.TODO

    @TypeConverter
    fun fromTaskPriority(value: TaskPriority?): String? = value?.name

    @TypeConverter
    fun toTaskPriority(value: String?): TaskPriority =
        value?.let { runCatching { TaskPriority.valueOf(it) }.getOrNull() } ?: TaskPriority.NORMAL
}
