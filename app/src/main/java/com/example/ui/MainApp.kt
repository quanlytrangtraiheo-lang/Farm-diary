package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.herd.AddPigDialog
import com.example.ui.herd.HerdViewModel
import com.example.ui.herd.PigDetailScreen
import com.example.ui.home.HomeScreen
import com.example.ui.home.HomeViewModel
import com.example.ui.inventory.InventoryScreen
import com.example.ui.inventory.InventoryViewModel
import com.example.ui.navigation.Screen
import com.example.ui.pig.PigListScreen
import com.example.ui.pig.PigViewModel
import com.example.ui.report.ReportScreen
import com.example.ui.report.ReportViewModel
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SettingsViewModel
import com.example.ui.settings.SyncLogScreen
import com.example.ui.task.AddTaskDialog
import com.example.ui.task.TaskScreen
import com.example.ui.task.TaskViewModel
import com.example.ui.theme.FarmGreenContainer
import com.example.ui.theme.FarmGreenPrimary
import com.example.ui.theme.FarmTextSecondary
import com.example.ui.theme.LocalOutdoorMode
import com.example.ui.theme.OutdoorBorder
import com.example.ui.theme.OutdoorModeManager

enum class MainTab(val title: String, val icon: ImageVector, val tag: String, val route: String) {
    HOME("Trang chủ", Icons.Default.Home, "tab_home", Screen.Home.route),
    HERD("Đàn heo", Icons.Default.Pets, "tab_herd", Screen.Herd.route),
    TASKS("Công việc", Icons.Default.DateRange, "tab_tasks", Screen.Tasks.route),
    INVENTORY("Kho", Icons.Default.Inventory2, "tab_inventory", Screen.Inventory.route),
    REPORTS("Báo cáo", Icons.Default.Assessment, "tab_reports", Screen.Reports.route),
    SETTINGS("Cài đặt", Icons.Default.Settings, "tab_settings", Screen.Settings.route)
}

@Composable
fun MainApp(
    homeViewModel: HomeViewModel,
    herdViewModel: HerdViewModel,
    pigViewModel: PigViewModel,
    taskViewModel: TaskViewModel,
    reportViewModel: ReportViewModel,
    settingsViewModel: SettingsViewModel,
    inventoryViewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val isTopLevelDestination = MainTab.values().any { it.route == currentRoute }

    var showAddPigDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    val isOutdoor by OutdoorModeManager.isOutdoorMode.collectAsState()
    val herdUiState by herdViewModel.uiState.collectAsState()
    val pigUiState by pigViewModel.uiState.collectAsState()

    CompositionLocalProvider(LocalOutdoorMode provides isOutdoor) {
        Scaffold(
            bottomBar = {
                // Chỉ hiển thị thanh điều hướng đáy khi đang ở các màn hình chính (Top-level tabs)
                if (isTopLevelDestination) {
                    NavigationBar(
                        containerColor = Color.White,
                        tonalElevation = if (isOutdoor) 0.dp else 6.dp,
                        modifier = if (isOutdoor) Modifier.border(BorderStroke(1.5.dp, OutdoorBorder)) else Modifier
                    ) {
                        MainTab.values().forEach { tab ->
                            val isSelected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = FarmGreenPrimary,
                                    selectedTextColor = FarmGreenPrimary,
                                    indicatorColor = FarmGreenContainer,
                                    unselectedIconColor = FarmTextSecondary,
                                    unselectedTextColor = FarmTextSecondary
                                ),
                                modifier = Modifier.testTag(tab.tag)
                            )
                        }
                    }
                }
            },
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            val contentModifier = Modifier.padding(paddingValues)

            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = contentModifier
            ) {
                // 1. Màn hình Trang chủ
                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = homeViewModel,
                        onNavigateToHerd = {
                            navController.navigate(Screen.Herd.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToTasks = {
                            navController.navigate(Screen.Tasks.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToReports = {
                            navController.navigate(Screen.Reports.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToSettings = {
                            navController.navigate(Screen.Settings.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onQuickAdd = { showAddTaskDialog = true },
                        onNavigateToInventory = {
                            navController.navigate(Screen.Inventory.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }

                // 2. Màn hình Danh sách Đàn heo
                composable(Screen.Herd.route) {
                    PigListScreen(
                        viewModel = pigViewModel,
                        onPigSelected = { pigId ->
                            navController.navigate(Screen.PigDetail.createRoute(pigId))
                        },
                        onAddNewPig = { showAddPigDialog = true }
                    )
                }

                // 3. Màn hình Chi tiết cá thể heo (Nhận tham số pigId từ sơ đồ Navigation Compose)
                composable(
                    route = Screen.PigDetail.route,
                    arguments = Screen.PigDetail.arguments
                ) { backStackEntry ->
                    val pigId = backStackEntry.arguments?.getString(Screen.PigDetail.ARG_PIG_ID).orEmpty()
                    val selectedPig = herdUiState.pigs.find { it.id == pigId }
                        ?: pigUiState.pigs.find { it.id == pigId }

                    if (selectedPig != null) {
                        PigDetailScreen(
                            pig = selectedPig,
                            viewModel = herdViewModel,
                            pigViewModel = pigViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Không tìm thấy thông tin heo ($pigId)",
                                color = FarmTextSecondary
                            )
                        }
                    }
                }

                // 4. Màn hình Công việc
                composable(Screen.Tasks.route) {
                    TaskScreen(
                        viewModel = taskViewModel,
                        onAddNewTask = { showAddTaskDialog = true }
                    )
                }

                // 5. Màn hình Kho
                composable(Screen.Inventory.route) {
                    InventoryScreen(
                        viewModel = inventoryViewModel
                    )
                }

                // 6. Màn hình Báo cáo
                composable(Screen.Reports.route) {
                    ReportScreen(
                        viewModel = reportViewModel
                    )
                }

                // 7. Màn hình Cài đặt
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onNavigateToSyncLogs = { navController.navigate(Screen.SyncLogs.route) }
                    )
                }

                // 8. Màn hình Nhật ký đồng bộ
                composable(Screen.SyncLogs.route) {
                    SyncLogScreen(
                        viewModel = settingsViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            // Global Add Pig Dialog
            if (showAddPigDialog) {
                AddPigDialog(
                    onDismiss = { showAddPigDialog = false },
                    onConfirm = { tag, isBatch, qty, breed, gender, status, loc, weight, parity ->
                        herdViewModel.addPig(
                            tagCode = tag,
                            isBatch = isBatch,
                            quantity = qty,
                            breed = breed,
                            gender = gender,
                            status = status,
                            location = loc,
                            currentWeightKg = weight,
                            parity = parity
                        )
                        showAddPigDialog = false
                    }
                )
            }

            // Global Add Task Dialog
            if (showAddTaskDialog) {
                AddTaskDialog(
                    onDismiss = { showAddTaskDialog = false },
                    onConfirm = { title, type, time, ref, loc, priority, note ->
                        taskViewModel.addTask(
                            title = title,
                            taskType = type,
                            timeLabel = time,
                            referenceId = ref,
                            locationInfo = loc,
                            priority = priority,
                            note = note
                        )
                        showAddTaskDialog = false
                    }
                )
            }
        }
    }
}
