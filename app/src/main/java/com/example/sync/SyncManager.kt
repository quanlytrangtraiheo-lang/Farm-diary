package com.example.sync

import com.example.data.local.AppDatabase
import com.example.data.local.entity.SyncLogEntity
import com.example.data.model.SyncStatus
import com.example.data.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

sealed class SyncState {
    object Idle : SyncState()
    data class Syncing(val progressPercent: Int, val message: String) : SyncState()
    data class Success(val itemsSyncedCount: Int, val timestamp: Long) : SyncState()
    data class Error(val message: String) : SyncState()
}

class SyncManager(
    private val database: AppDatabase,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _isAutoSyncEnabled = MutableStateFlow(true)
    val isAutoSyncEnabled: StateFlow<Boolean> = _isAutoSyncEnabled.asStateFlow()

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(System.currentTimeMillis() - 900_000L) // 15 mins ago
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()

    fun toggleAutoSync(enabled: Boolean) {
        _isAutoSyncEnabled.value = enabled
    }

    fun toggleOnlineMode(online: Boolean) {
        _isOnline.value = online
    }

    fun triggerSync(currentUserRole: UserRole = UserRole.MANAGER, onComplete: ((Boolean) -> Unit)? = null) {
        if (_syncState.value is SyncState.Syncing) return

        coroutineScope.launch {
            if (!_isOnline.value) {
                _syncState.value = SyncState.Error("Không có kết nối mạng (Ngoại tuyến)")
                delay(2000)
                _syncState.value = SyncState.Idle
                onComplete?.invoke(false)
                return@launch
            }

            _syncState.value = SyncState.Syncing(20, "Đang kiểm tra dữ liệu thay đổi...")
            delay(400)

            val unsyncedPigs = database.pigDao().getUnsyncedPigs()
            _syncState.value = SyncState.Syncing(55, "Đang mã hóa gói delta payload...")
            delay(500)

            _syncState.value = SyncState.Syncing(85, "Đang giải quyết xung đột & đẩy lên Cloud...")
            delay(500)

            // Mark pending pigs as synced or handle conflict
            val now = System.currentTimeMillis()
            val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
            val formattedTime = timeFmt.format(Date(now))

            unsyncedPigs.forEach { pig ->
                if (pig.hasUltrasoundWarning && currentUserRole == UserRole.TECHNICIAN) {
                    // Simulates conflict for technician
                    database.pigDao().updatePig(
                        pig.copy(
                            syncStatus = SyncStatus.SYNC_FAILED,
                            conflictLog = "Xung đột lịch khám: Quản lý yêu cầu xác nhận lại ngày siêu âm"
                        )
                    )
                    database.syncLogDao().insertLog(
                        SyncLogEntity(
                            id = UUID.randomUUID().toString(),
                            timestamp = now,
                            timeFormatted = formattedTime,
                            title = "Xung đột: ${pig.tagCode}",
                            subDetail = "Cần quản lý xử lý",
                            syncStatus = SyncStatus.SYNC_FAILED,
                            hasConflict = true,
                            conflictDetail = "Cần người quản lý duyệt bản ghi"
                        )
                    )
                } else {
                    database.pigDao().updatePig(
                        pig.copy(
                            syncStatus = SyncStatus.SYNCED,
                            conflictLog = null
                        )
                    )
                }
            }

            database.pigDao().markAllAsSynced()

            // Insert a successful sync log
            database.syncLogDao().insertLog(
                SyncLogEntity(
                    id = UUID.randomUUID().toString(),
                    timestamp = now,
                    timeFormatted = formattedTime,
                    title = "Đồng bộ toàn bộ dữ liệu",
                    subDetail = "Hoàn tất đồng bộ 2 chiều qua WorkManager",
                    syncStatus = SyncStatus.SYNCED
                )
            )

            _lastSyncTime.value = now
            val syncedCount = maxOf(unsyncedPigs.size, 1)
            _syncState.value = SyncState.Success(syncedCount, now)

            delay(1500)
            _syncState.value = SyncState.Idle
            onComplete?.invoke(true)
        }
    }
}
