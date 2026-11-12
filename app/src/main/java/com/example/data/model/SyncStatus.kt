package com.example.data.model

enum class SyncStatus(val label: String) {
    SYNCED("Đã đồng bộ"),
    PENDING_SYNC("Chờ đồng bộ"),
    SYNCING("Đang đồng bộ"),
    SYNC_FAILED("Lỗi đồng bộ"),
    PENDING_DELETE("Chờ xóa")
}
