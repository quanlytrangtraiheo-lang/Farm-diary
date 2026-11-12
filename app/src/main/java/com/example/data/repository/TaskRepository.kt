package com.example.data.repository

import com.example.data.local.dao.TaskDao
import com.example.data.local.entity.TaskEntity
import com.example.data.model.SyncStatus
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import com.example.data.model.TaskType
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class TaskRepository(private val taskDao: TaskDao) {

    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val pendingCount: Flow<Int> = taskDao.getPendingTaskCount()

    fun getTasksByStatus(status: TaskStatus): Flow<List<TaskEntity>> =
        taskDao.getTasksByStatus(status)

    suspend fun addTask(
        title: String,
        taskType: TaskType,
        dueDate: Long,
        timeLabel: String,
        assignedTo: String,
        priority: TaskPriority,
        referenceId: String,
        locationInfo: String,
        note: String = ""
    ) {
        val task = TaskEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            taskType = taskType,
            dueDate = dueDate,
            timeLabel = timeLabel,
            assignedTo = assignedTo,
            status = TaskStatus.TODO,
            priority = priority,
            referenceId = referenceId,
            locationInfo = locationInfo,
            note = note,
            syncStatus = SyncStatus.PENDING_SYNC,
            updatedAt = System.currentTimeMillis()
        )
        taskDao.insertTask(task)
    }

    suspend fun updateTaskStatus(taskId: String, newStatus: TaskStatus) {
        taskDao.updateTaskStatus(taskId, newStatus)
    }

    suspend fun updateTask(task: TaskEntity) {
        taskDao.updateTask(
            task.copy(
                syncStatus = SyncStatus.PENDING_SYNC,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteTask(task: TaskEntity) {
        taskDao.deleteTask(task)
    }
}
