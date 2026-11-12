package com.example.data.model

enum class SowReproductiveStatus(val label: String, val shortLabel: String) {
    HAU_BI("Hậu bị chờ phối", "Hậu bị"),
    MANG_THAI("Đang mang thai", "Mang thai"),
    NUOI_CON("Đang nuôi con (cho bú)", "Nuôi con"),
    CAI_SUA_CHO_PHOI("Cai sữa chờ phối lại", "Cai sữa"),
    LOAI_THAI("Loại thải / Xuất bán", "Loại thải"),
    DANG_NUOI("Đang nuôi lớn", "Đang nuôi")
}
