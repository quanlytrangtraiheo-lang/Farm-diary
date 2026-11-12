package com.example.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.InventoryItemEntity
import com.example.data.local.entity.PigEntity
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import com.example.data.model.TaskType
import com.example.data.repository.SyncRepository
import com.example.data.repository.TaskRepository
import com.example.domain.repository.InventoryRepository
import com.example.domain.repository.PigRepository
import com.example.data.local.entity.SystemParameterEntity
import com.example.domain.repository.SystemParameterRepository
import com.example.domain.feed.FeedRationCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FeedConsumptionItem(
    val name: String,
    val code: String,
    val amountKg: Double,
    val percentage: Float,
    val colorHex: Long,
    val barnTarget: String,
    val statusNote: String,
    val isAbnormal: Boolean = false
)

data class FeedForecastItem(
    val feedName: String,
    val stockBags: Int,
    val stockKg: Double,
    val dailyUsageKg: Double,
    val daysRemaining: Int,
    val isNearDepletion: Boolean = false,
    val isCritical: Boolean = false,
    val safetyDaysThreshold: Int = 3,
    val minStockThreshold: Double = 20.0,
    val suggestedOrderBags: Int = 0,
    val suggestedOrderKg: Double = 0.0,
    val feedCode: String = ""
)

data class DailyFeedAnalytics(
    val totalDailyFeedKg: Double = 1920.0,
    val fcrIndex: Double = 2.42,
    val feedConsumptionList: List<FeedConsumptionItem> = defaultFeedConsumption(),
    val totalStockKg: Double = 28500.0,
    val totalStockBags: Int = 1140,
    val dailyConsumptionKg: Double = 1900.0,
    val estimatedDaysRemaining: Int = 15,
    val reorderThresholdDays: Int = 7,
    val feedLineForecasts: List<FeedForecastItem> = defaultFeedForecasts()
)

data class FinancialBalance(
    val breedingPigCost: Double = 280_000_000.0,
    val feedCost: Double = 475_000_000.0,
    val medicineVaccineCost: Double = 58_500_000.0,
    val suppliesCost: Double = 33_000_000.0,
    val totalCost: Double = 846_500_000.0,
    val meatRevenue: Double = 1_120_000_000.0,
    val breedingRevenue: Double = 85_000_000.0,
    val totalRevenue: Double = 1_205_000_000.0,
    val accumulatedProfit: Double = 358_500_000.0,
    val profitMarginPercent: Double = 29.75
)

data class HomeUiState(
    val totalPigs: Int = 1250,
    val pendingTasksCount: Int = 8,
    val alertCount: Int = 3,
    val warningPigs: List<PigEntity> = emptyList(),
    val criticalFeedAlerts: List<FeedForecastItem> = emptyList(),
    val isAutoSync: Boolean = true,
    val isOnline: Boolean = true,
    val feedAnalytics: DailyFeedAnalytics = DailyFeedAnalytics(),
    val financialBalance: FinancialBalance = FinancialBalance()
)

private fun defaultFeedConsumption(): List<FeedConsumptionItem> {
    return listOf(
        FeedConsumptionItem(
            name = "Cám nái bầu",
            code = "CAM_NAI_MANG_THAI",
            amountKg = 350.0,
            percentage = 18.2f,
            colorHex = 0xFF1976D2,
            barnTarget = "Khu Bầu 1 & 2",
            statusNote = "Bình thường",
            isAbnormal = false
        ),
        FeedConsumptionItem(
            name = "Cám nái đẻ & nuôi con",
            code = "CAM_NAI_NUOI_CON",
            amountKg = 280.0,
            percentage = 14.6f,
            colorHex = 0xFFFB8C00,
            barnTarget = "Khu Đẻ 1 & 2",
            statusNote = "Bình thường",
            isAbnormal = false
        ),
        FeedConsumptionItem(
            name = "Cám heo thịt GĐ 1",
            code = "CAM_THIT_GD1",
            amountKg = 320.0,
            percentage = 16.7f,
            colorHex = 0xFF8E24AA,
            barnTarget = "Chuồng Thịt 1 (Cai sữa - 30kg)",
            statusNote = "Bình thường",
            isAbnormal = false
        ),
        FeedConsumptionItem(
            name = "Cám heo thịt GĐ 2",
            code = "CAM_THIT_GD2",
            amountKg = 620.0,
            percentage = 32.3f,
            colorHex = 0xFF00897B,
            barnTarget = "Chuồng Thịt 2 & 3 (30 - 70kg)",
            statusNote = "Ô Thịt 2 giảm bất thường -12% cần kiểm tra!",
            isAbnormal = true
        ),
        FeedConsumptionItem(
            name = "Cám heo thịt GĐ 3",
            code = "CAM_THIT_GD3",
            amountKg = 350.0,
            percentage = 18.2f,
            colorHex = 0xFF43A047,
            barnTarget = "Chuồng Thịt 4 (70kg - Xuất bán)",
            statusNote = "Bình thường",
            isAbnormal = false
        )
    )
}

private fun defaultFeedForecasts(): List<FeedForecastItem> {
    return listOf(
        FeedForecastItem(
            feedName = "Cám nái bầu",
            stockBags = 328,
            stockKg = 8200.0,
            dailyUsageKg = 350.0,
            daysRemaining = 23,
            isNearDepletion = false
        ),
        FeedForecastItem(
            feedName = "Cám nái đẻ & nuôi con",
            stockBags = 180,
            stockKg = 4500.0,
            dailyUsageKg = 280.0,
            daysRemaining = 16,
            isNearDepletion = false
        ),
        FeedForecastItem(
            feedName = "Cám heo thịt GĐ 1",
            stockBags = 200,
            stockKg = 5000.0,
            dailyUsageKg = 320.0,
            daysRemaining = 15,
            isNearDepletion = false
        ),
        FeedForecastItem(
            feedName = "Cám heo thịt GĐ 2",
            stockBags = 272,
            stockKg = 6800.0,
            dailyUsageKg = 620.0,
            daysRemaining = 11,
            isNearDepletion = true
        ),
        FeedForecastItem(
            feedName = "Cám heo thịt GĐ 3",
            stockBags = 160,
            stockKg = 4000.0,
            dailyUsageKg = 350.0,
            daysRemaining = 11,
            isNearDepletion = true
        )
    )
}

class HomeViewModel(
    private val pigRepository: PigRepository,
    private val taskRepository: TaskRepository,
    private val syncRepository: SyncRepository,
    private val inventoryRepository: InventoryRepository? = null,
    private val systemParameterRepository: SystemParameterRepository? = null
) : ViewModel() {

    private val createdFeedTaskCodes = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()

    private val inventoryItemsFlow = inventoryRepository?.allItems
        ?: MutableStateFlow<List<InventoryItemEntity>>(emptyList())

    private val systemParametersFlow = systemParameterRepository?.getAllParameters()
        ?: MutableStateFlow<List<SystemParameterEntity>>(emptyList())

    private val syncFlow = combine(
        syncRepository.isOnline,
        syncRepository.isAutoSync
    ) { isOnline, isAutoSync ->
        Pair(isOnline, isAutoSync)
    }

    private val pigsAndHeadFlow = combine(
        pigRepository.allPigs,
        pigRepository.totalHeadCount
    ) { pigs, head ->
        Pair(pigs, head)
    }

    private val feedResourcesFlow = combine(
        inventoryItemsFlow,
        systemParametersFlow
    ) { inv, params ->
        Pair(inv, params)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        pigsAndHeadFlow,
        taskRepository.allTasks,
        syncFlow,
        feedResourcesFlow
    ) { pigPair, tasks, syncPair, feedResources ->
        val (pigs, totalHead) = pigPair
        val (isOnline, isAutoSync) = syncPair
        val (inventoryItems, systemParams) = feedResources

        val warningPigs = pigs.filter {
            it.hasOverdueVaccine || it.hasUltrasoundWarning || it.hasDelayedHeatWarning || it.hasProlongedGestationWarning
        }
        val pendingTasks = tasks.filter { it.status != TaskStatus.DONE }
        val effectiveHeadCount = totalHead ?: pigs.sumOf { it.quantity }.let { if (it > 0) it else 1250 }

        // Kết nối tính toán khẩu phần ăn và dự báo tồn kho theo thời gian thực (FeedRationCalculator)
        val feedItemsInStock = inventoryItems.filter { it.category == InventoryItemEntity.CATEGORY_FEED }
        val rationResult = FeedRationCalculator.calculate(
            pigs = pigs,
            rationMatrix = systemParams,
            inventoryFeedItems = feedItemsInStock
        )

        val feedColors = listOf(0xFF1976D2, 0xFFFB8C00, 0xFF43A047, 0xFF8E24AA, 0xFFE53935)
        val feedLineForecasts = rationResult.feedResults.map { demand ->
            FeedForecastItem(
                feedName = demand.feedName,
                stockBags = demand.stockBags,
                stockKg = demand.stockKg,
                dailyUsageKg = demand.dailyDemandKg,
                daysRemaining = demand.daysRemaining,
                isNearDepletion = demand.isCritical || demand.isWarning,
                isCritical = demand.isCritical,
                safetyDaysThreshold = demand.safetyDaysThreshold,
                minStockThreshold = demand.minStockThreshold,
                suggestedOrderBags = demand.suggestedOrderBags,
                suggestedOrderKg = demand.suggestedOrderKg,
                feedCode = demand.feedCode
            )
        }

        // Danh sách các loại cám chạm ngưỡng Khẩn cấp (Đỏ)
        val criticalFeedAlerts = feedLineForecasts.filter { it.isCritical }

        // Tự động chèn công việc vào danh sách Công việc hôm nay khi cám rơi vào mức Khẩn cấp (Đỏ)
        criticalFeedAlerts.forEach { feed ->
            val alreadyHasTask = tasks.any { task ->
                (task.referenceId == "FEED_${feed.feedCode}" || task.referenceId == feed.feedCode || task.title.contains(feed.feedName)) &&
                task.status != TaskStatus.DONE
            }
            if (!alreadyHasTask && createdFeedTaskCodes.add(feed.feedCode)) {
                viewModelScope.launch {
                    taskRepository.addTask(
                        title = "Nhập gấp ${feed.feedName} - Tồn chỉ còn đủ ăn ${feed.daysRemaining} ngày",
                        taskType = TaskType.FEED_ORDER,
                        dueDate = System.currentTimeMillis(),
                        timeLabel = "07:00",
                        assignedTo = "Quản lý kho / Kỹ thuật viên",
                        priority = TaskPriority.HIGH,
                        referenceId = "FEED_${feed.feedCode}",
                        locationInfo = "Kho Cám & Silo",
                        note = "Tồn kho: ${feed.stockBags} bao (${feed.stockKg.toInt()} kg). Ngưỡng an toàn: ${feed.safetyDaysThreshold} ngày / ${feed.minStockThreshold.toInt()} bao. Gợi ý nhập thêm: ${feed.suggestedOrderBags} bao (${feed.suggestedOrderKg.toInt()} kg)."
                    )
                }
            }
        }

        val feedConsumptionList = rationResult.feedResults.mapIndexed { index, demand ->
            val percentage = if (rationResult.totalDailyConsumptionKg > 0) {
                ((demand.dailyDemandKg / rationResult.totalDailyConsumptionKg) * 100).toFloat()
            } else 0f
            FeedConsumptionItem(
                name = demand.feedName,
                code = demand.feedCode,
                amountKg = demand.dailyDemandKg,
                percentage = percentage,
                colorHex = feedColors.getOrElse(index) { 0xFF607D8B },
                barnTarget = when (demand.feedCode) {
                    "CAM_NAI_MANG_THAI" -> "Khu Bầu 1 & 2"
                    "CAM_NAI_NUOI_CON" -> "Khu Đẻ 1 & 2"
                    "CAM_HEO_CON_TAP_AN" -> "Chuồng Đẻ / Úm"
                    "CAM_THIT_GD1" -> "Chuồng Thịt 1"
                    "CAM_THIT_GD2" -> "Chuồng Thịt 2 & 3"
                    "CAM_THIT_GD3" -> "Chuồng Xuất Bán"
                    else -> "Toàn trại"
                },
                statusNote = if (demand.isCritical) "Cảnh báo khẩn cấp (< ${demand.safetyDaysThreshold} ngày)" else if (demand.isWarning) "Cảnh báo đặt hàng (< 7 ngày)" else "Bình thường",
                isAbnormal = demand.isCritical
            )
        }

        val feedAnalytics = DailyFeedAnalytics(
            totalDailyFeedKg = rationResult.totalDailyConsumptionKg,
            fcrIndex = 2.42,
            feedConsumptionList = feedConsumptionList,
            totalStockKg = rationResult.totalStockKg,
            totalStockBags = rationResult.totalStockBags,
            dailyConsumptionKg = rationResult.totalDailyConsumptionKg,
            estimatedDaysRemaining = rationResult.overallDaysRemaining,
            reorderThresholdDays = 7,
            feedLineForecasts = feedLineForecasts
        )

        // Bảng cân đối tài chính
        val breedingPigCost = 280_000_000.0
        val feedCost = 475_000_000.0
        val medicineCost = 58_500_000.0
        val suppliesCost = 33_000_000.0
        val totalCost = breedingPigCost + feedCost + medicineCost + suppliesCost
        val meatRevenue = 1_120_000_000.0
        val breedingRevenue = 85_000_000.0
        val totalRevenue = meatRevenue + breedingRevenue
        val accumulatedProfit = totalRevenue - totalCost
        val profitMargin = if (totalRevenue > 0) (accumulatedProfit / totalRevenue) * 100 else 0.0

        val financialBalance = FinancialBalance(
            breedingPigCost = breedingPigCost,
            feedCost = feedCost,
            medicineVaccineCost = medicineCost,
            suppliesCost = suppliesCost,
            totalCost = totalCost,
            meatRevenue = meatRevenue,
            breedingRevenue = breedingRevenue,
            totalRevenue = totalRevenue,
            accumulatedProfit = accumulatedProfit,
            profitMarginPercent = profitMargin
        )

        // Tổng cảnh báo gồm cảnh báo heo bất thường và cảnh báo cám chạm ngưỡng an toàn
        val totalAlerts = warningPigs.size + criticalFeedAlerts.size

        HomeUiState(
            totalPigs = effectiveHeadCount,
            pendingTasksCount = pendingTasks.size,
            alertCount = totalAlerts,
            warningPigs = warningPigs,
            criticalFeedAlerts = criticalFeedAlerts,
            isAutoSync = isAutoSync,
            isOnline = isOnline,
            feedAnalytics = feedAnalytics,
            financialBalance = financialBalance
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )
}
