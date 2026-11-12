package com.example.data.model

enum class TaskType(val title: String) {
    VACCINE("Tiêm vắc-xin"),
    ULTRASOUND("Siêu âm thai"),
    CHECK_HEAT_1("Kiểm tra lốc sảy CK 1"),
    CHECK_HEAT_2("Kiểm tra lốc sảy CK 2"),
    FARROWING_TRANSFER("Chuyển chuồng đẻ & Vệ sinh nái"),
    FARROWING_MONITOR("Giám sát sinh con (Trực đẻ)"),
    WEANING_PLAN("Dự kiến cai sữa"),
    CLEAN_DISINFECT("Vệ sinh sát trùng"),
    WEIGHT_CHECK("Cân trọng lượng"),
    FEED_ORDER("Nhập cám / Thức ăn"),
    GENERAL("Nhiệm vụ chung")
}

enum class TaskStatus(val label: String) {
    TODO("Chưa làm"),
    IN_PROGRESS("Đang làm"),
    DONE("Đã xong"),
    OVERDUE("Quá hạn")
}

enum class TaskPriority(val label: String) {
    HIGH("Ưu tiên cao"),
    NORMAL("Bình thường")
}
