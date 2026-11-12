package com.example.domain

import com.example.data.local.entity.TaskEntity
import com.example.data.model.SyncStatus
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import com.example.data.model.TaskType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object AutoTaskPipeline {
    private const val DAY_MS = 86_400_000L

    /**
     * Generates standard reproductive pipeline tasks for a sow from mating date D (PDF Page 16).
     */
    fun generateTasksForMatingCycle(
        sowTag: String,
        location: String,
        matingDate: Long,
        assignedTo: String = "Kỹ thuật viên chuồng"
    ): List<TaskEntity> {
        val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        val defaultTime = timeFmt.format(Date())

        return listOf(
            // D + 18 to D + 24: Kiểm tra lốc sảy chu kỳ 1
            TaskEntity(
                id = UUID.randomUUID().toString(),
                title = "Kiểm tra lốc sảy chu kỳ 1",
                taskType = TaskType.CHECK_HEAT_1,
                dueDate = matingDate + (21 * DAY_MS),
                timeLabel = "08:30",
                assignedTo = assignedTo,
                status = TaskStatus.TODO,
                priority = TaskPriority.NORMAL,
                referenceId = sowTag,
                locationInfo = location,
                note = "Phát hiện nái có biểu hiện động dục lại hay không (Mốc D+18 đến D+24)",
                syncStatus = SyncStatus.PENDING_SYNC
            ),
            // D + 28 to D + 35: Siêu âm thai
            TaskEntity(
                id = UUID.randomUUID().toString(),
                title = "Siêu âm thai nái",
                taskType = TaskType.ULTRASOUND,
                dueDate = matingDate + (30 * DAY_MS),
                timeLabel = "09:30",
                assignedTo = assignedTo,
                status = TaskStatus.TODO,
                priority = TaskPriority.HIGH,
                referenceId = sowTag,
                locationInfo = location,
                note = "Xác nhận đậu thai bằng máy siêu âm xách tay (Mốc D+28 đến D+35)",
                syncStatus = SyncStatus.PENDING_SYNC
            ),
            // D + 38 to D + 42: Kiểm tra lốc sảy chu kỳ 2
            TaskEntity(
                id = UUID.randomUUID().toString(),
                title = "Kiểm tra lốc sảy chu kỳ 2",
                taskType = TaskType.CHECK_HEAT_2,
                dueDate = matingDate + (40 * DAY_MS),
                timeLabel = "10:00",
                assignedTo = assignedTo,
                status = TaskStatus.TODO,
                priority = TaskPriority.NORMAL,
                referenceId = sowTag,
                locationInfo = location,
                note = "Rà soát lần cuối trước khi chuyển chế độ ăn nuôi thai (Mốc D+38 đến D+42)",
                syncStatus = SyncStatus.PENDING_SYNC
            ),
            // D + 107: Chuyển chuồng đẻ & Vệ sinh nái
            TaskEntity(
                id = UUID.randomUUID().toString(),
                title = "Chuyển chuồng đẻ & Vệ sinh nái",
                taskType = TaskType.FARROWING_TRANSFER,
                dueDate = matingDate + (107 * DAY_MS),
                timeLabel = "14:00",
                assignedTo = assignedTo,
                status = TaskStatus.TODO,
                priority = TaskPriority.HIGH,
                referenceId = sowTag,
                locationInfo = location,
                note = "Tắm sát trùng nái, chuẩn bị ô úm heo con trước khi đẻ 7 ngày",
                syncStatus = SyncStatus.PENDING_SYNC
            ),
            // D + 114: Giám sát sinh con (Trực đẻ)
            TaskEntity(
                id = UUID.randomUUID().toString(),
                title = "Giám sát sinh con (Trực đẻ)",
                taskType = TaskType.FARROWING_MONITOR,
                dueDate = matingDate + (114 * DAY_MS),
                timeLabel = "00:00",
                assignedTo = assignedTo,
                status = TaskStatus.TODO,
                priority = TaskPriority.HIGH,
                referenceId = sowTag,
                locationInfo = location,
                note = "Ngày dự sinh chuẩn (D+114 ngày). Trực đẻ, hỗ trợ cắt rốn, lau nhớt và cho bú sữa đầu",
                syncStatus = SyncStatus.PENDING_SYNC
            ),
            // D + 114 + 21: Dự kiến cai sữa
            TaskEntity(
                id = UUID.randomUUID().toString(),
                title = "Dự kiến cai sữa bầy con",
                taskType = TaskType.WEANING_PLAN,
                dueDate = matingDate + ((114 + 21) * DAY_MS),
                timeLabel = "09:00",
                assignedTo = assignedTo,
                status = TaskStatus.TODO,
                priority = TaskPriority.NORMAL,
                referenceId = sowTag,
                locationInfo = location,
                note = "Tính toán lịch tách mẹ chuyển nái về chuồng bầu dưỡng sức",
                syncStatus = SyncStatus.PENDING_SYNC
            )
        )
    }
}
