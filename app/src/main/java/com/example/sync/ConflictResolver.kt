package com.example.sync

import com.example.data.local.entity.PigEntity
import com.example.data.model.SyncStatus
import com.example.data.model.UserRole

data class ConflictResult(
    val resolvedPig: PigEntity,
    val isConflictDetected: Boolean,
    val resolutionNote: String
)

object ConflictResolver {
    /**
     * Resolves conflict between client record and simulated remote record
     * based on Last-Write-Wins (updatedAt timestamp) and Role-Based authority (PDF Page 20).
     */
    fun resolve(
        clientPig: PigEntity,
        serverPig: PigEntity,
        currentUserRole: UserRole
    ): ConflictResult {
        // If client record is newer than server
        if (clientPig.updatedAt > serverPig.updatedAt) {
            return ConflictResult(
                resolvedPig = clientPig.copy(
                    syncStatus = SyncStatus.SYNCED,
                    version = maxOf(clientPig.version, serverPig.version) + 1,
                    conflictLog = null
                ),
                isConflictDetected = false,
                resolutionNote = "Client mới hơn: Cập nhật thành công lên hệ thống (LWW)"
            )
        }

        // If server is newer or equal, check user role
        return when (currentUserRole) {
            UserRole.MANAGER -> {
                // Manager role has override authority
                ConflictResult(
                    resolvedPig = clientPig.copy(
                        syncStatus = SyncStatus.SYNCED,
                        updatedAt = System.currentTimeMillis(),
                        version = serverPig.version + 1,
                        conflictLog = null
                    ),
                    isConflictDetected = true,
                    resolutionNote = "Quyền Quản lý: Đã ghi đè dữ liệu lên Server thành công"
                )
            }
            UserRole.TECHNICIAN -> {
                // Technician cannot blindly overwrite manager/server data
                ConflictResult(
                    resolvedPig = clientPig.copy(
                        syncStatus = SyncStatus.SYNC_FAILED,
                        conflictLog = "Dữ liệu máy chủ mới hơn. Cần Quản lý phê duyệt hoặc đồng bộ lại từ máy chủ."
                    ),
                    isConflictDetected = true,
                    resolutionNote = "Xung đột: Máy chủ có bản cập nhật mới hơn từ Quản lý"
                )
            }
        }
    }
}
