package com.example.ui.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

/**
 * Định nghĩa các màn hình và sơ đồ định tuyến (Navigation Routes) của ứng dụng quản lý trang trại heo.
 */
sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Trang chủ")
    object Herd : Screen("herd", "Đàn heo")
    object PigDetail : Screen("pig_detail/{pigId}", "Chi tiết cá thể heo") {
        const val ARG_PIG_ID = "pigId"

        fun createRoute(pigId: String): String = "pig_detail/$pigId"

        val arguments = listOf(
            navArgument(ARG_PIG_ID) {
                type = NavType.StringType
                nullable = false
            }
        )
    }
    object Tasks : Screen("tasks", "Công việc")
    object Inventory : Screen("inventory", "Kho")
    object Reports : Screen("reports", "Báo cáo")
    object Settings : Screen("settings", "Cài đặt")
    object SyncLogs : Screen("sync_logs", "Nhật ký đồng bộ")
}
