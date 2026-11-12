package com.example.data.model

enum class UserRole(val label: String) {
    MANAGER("Quản lý trại"),
    TECHNICIAN("Kỹ thuật viên")
}

data class UserProfile(
    val name: String = "Nguyễn Văn Tuấn",
    val email: String = "quanlytrangtraiheo@gmail.com",
    val role: UserRole = UserRole.MANAGER,
    val farmName: String = "Trại Heo Việt Xanh (Cơ sở 1)"
)
