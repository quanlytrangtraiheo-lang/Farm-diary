package com.example.data.repository

import com.example.data.local.dao.SyncLogDao
import com.example.data.local.entity.SyncLogEntity
import com.example.data.model.SyncStatus
import com.example.sync.SyncManager
import kotlinx.coroutines.flow.Flow

class SyncRepository(
    private val syncLogDao: SyncLogDao,
    private val syncManager: SyncManager
) {
    val syncLogs: Flow<List<SyncLogEntity>> = syncLogDao.getAllLogs()
    val syncState = syncManager.syncState
    val isAutoSync = syncManager.isAutoSyncEnabled
    val isOnline = syncManager.isOnline
    val lastSyncTime = syncManager.lastSyncTime

    fun toggleAutoSync(enabled: Boolean) = syncManager.toggleAutoSync(enabled)
    fun toggleOnline(online: Boolean) = syncManager.toggleOnlineMode(online)
    fun triggerSync(onComplete: ((Boolean) -> Unit)? = null) = syncManager.triggerSync(onComplete = onComplete)

    suspend fun resolveConflictManually(logId: String) {
        // Mark log as resolved
        val log = SyncLogEntity(
            id = logId,
            timestamp = System.currentTimeMillis(),
            timeFormatted = "Vừa xong",
            title = "Đã giải quyết xung đột",
            subDetail = "Quản lý đã phê duyệt",
            syncStatus = SyncStatus.SYNCED,
            hasConflict = false
        )
        syncLogDao.insertLog(log)
    }
}
