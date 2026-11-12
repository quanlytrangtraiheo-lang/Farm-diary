package com.example.domain.feed

import com.example.data.local.entity.InventoryItemEntity
import com.example.data.local.entity.PigEntity
import com.example.data.local.entity.SystemParameterEntity
import com.example.data.model.SowReproductiveStatus

/**
 * Chi tiết mức tiêu thụ của một nhóm heo sinh lý đối với loại cám cụ thể
 */
data class FeedGroupUsage(
    val stageName: String,
    val weekNumber: Int,
    val headCount: Int,
    val rationPerHeadKg: Double,
    val totalUsageKg: Double
)

/**
 * Kết quả tính toán nhu cầu cám và dự báo tồn kho cho 1 loại cám
 */
data class FeedDemandResult(
    val feedCode: String,
    val feedName: String,
    val dailyDemandKg: Double,       // E_ngày(X): Tổng kg ăn trong ngày
    val stockBags: Int,              // Số bao tồn thực tế
    val stockKg: Double,             // Lượng tồn kho (kg) = Số bao * kg/bao
    val daysRemaining: Int,          // Số ngày sử dụng còn lại = stockKg / dailyDemandKg
    val isCritical: Boolean,         // Cảnh báo khẩn cấp (ngày dùng < safetyDays hoặc tồn < minStock) -> màu đỏ
    val isWarning: Boolean,          // Cảnh báo đặt hàng (ngày dùng < 7 ngày) -> màu cam
    val groupUsages: List<FeedGroupUsage> = emptyList(),
    val safetyDaysThreshold: Int = 3,
    val minStockThreshold: Double = 20.0,
    val suggestedOrderBags: Int = 0,
    val suggestedOrderKg: Double = 0.0
)

/**
 * Kết quả phân tích tổng thể toàn trại
 */
data class FarmFeedAnalyticsResult(
    val totalDailyConsumptionKg: Double,
    val totalStockKg: Double,
    val totalStockBags: Int,
    val overallDaysRemaining: Int,
    val feedResults: List<FeedDemandResult>
)

object FeedRationCalculator {

    /**
     * Phân tích trọng lượng mỗi bao (kg) từ chuỗi đơn vị của kho.
     * Ví dụ: "Bao 25kg" -> 25f, "Bao 40kg" -> 40f, "Bao 10kg" -> 10f, "Tấn" -> 1000f, "Kg" -> 1f
     */
    fun parseBagWeightKg(unit: String): Double {
        val lower = unit.lowercase()
        return when {
            lower.contains("40") -> 40.0
            lower.contains("25") -> 25.0
            lower.contains("10") -> 10.0
            lower.contains("50") -> 50.0
            lower.contains("tấn") || lower.contains("tan") -> 1000.0
            lower.contains("kg") -> 1.0
            else -> 25.0 // Mặc định bao chuẩn 25kg
        }
    }

    /**
     * Thuật toán tự động quét đàn và tính lượng cám tiêu thụ theo thời gian thực (Real-time Ration Engine)
     */
    fun calculate(
        pigs: List<PigEntity>,
        rationMatrix: List<SystemParameterEntity>,
        inventoryFeedItems: List<InventoryItemEntity>,
        now: Long = System.currentTimeMillis()
    ): FarmFeedAnalyticsResult {
        val rations = if (rationMatrix.any { it.category == SystemParameterEntity.CATEGORY_FEED_RATION }) {
            rationMatrix.filter { it.category == SystemParameterEntity.CATEGORY_FEED_RATION }
        } else {
            getDefaultRationMatrix()
        }

        // =========================================================================
        // BƯỚC 1: Quét số lượng và xác định tuần sinh lý của từng cá thể/lô heo
        // =========================================================================

        val pigStageGroups = mutableListOf<PigPhysiologicalGroup>()

        // 1. Nái mang thai (MANG_THAI)
        val pregnantSows = pigs.filter { !it.isBatch && it.status == SowReproductiveStatus.MANG_THAI }
        pregnantSows.forEach { sow ->
            val matingDate = sow.lastMatingDate ?: (now - 35 * 86_400_000L)
            val gestationDays = ((now - matingDate) / 86_400_000L).coerceAtLeast(0L)
            val week = ((gestationDays / 7).toInt() + 1).coerceIn(1, 16)
            pigStageGroups.add(
                PigPhysiologicalGroup(
                    stage = SystemParameterEntity.STAGE_PREGNANT_SOW,
                    stageLabel = "Nái mang thai",
                    week = week,
                    headCount = 1
                )
            )
        }

        // 2. Nái nuôi con (NUOI_CON)
        val lactatingSows = pigs.filter { !it.isBatch && it.status == SowReproductiveStatus.NUOI_CON }
        lactatingSows.forEach { sow ->
            val farrowDate = sow.lastMatingDate?.let { it + 114 * 86_400_000L } ?: (now - 14 * 86_400_000L)
            val nursingDays = ((now - farrowDate) / 86_400_000L).coerceAtLeast(0L)
            val week = ((nursingDays / 7).toInt() + 1).coerceIn(1, 4)
            pigStageGroups.add(
                PigPhysiologicalGroup(
                    stage = SystemParameterEntity.STAGE_LACTATING_SOW,
                    stageLabel = "Nái nuôi con",
                    week = week,
                    headCount = 1
                )
            )
        }

        // 3. Nái chờ phối (CAI_SUA_CHO_PHOI hoặc HAU_BI) -> Cố định Tuần = 1
        val waitingSows = pigs.filter { !it.isBatch && (it.status == SowReproductiveStatus.CAI_SUA_CHO_PHOI || it.status == SowReproductiveStatus.HAU_BI) }
        if (waitingSows.isNotEmpty()) {
            pigStageGroups.add(
                PigPhysiologicalGroup(
                    stage = SystemParameterEntity.STAGE_WEANED_WAITING_SOW,
                    stageLabel = "Nái chờ phối",
                    week = 1,
                    headCount = waitingSows.size
                )
            )
        }

        // 4. Heo con theo mẹ -> Cố định Tuần = 1 (Tính theo số nái nuôi con * bình quân 11 con/nái)
        val sucklingPigletsCount = lactatingSows.size * 11
        if (sucklingPigletsCount > 0) {
            pigStageGroups.add(
                PigPhysiologicalGroup(
                    stage = SystemParameterEntity.STAGE_SUCKLING_PIGLET,
                    stageLabel = "Heo con theo mẹ",
                    week = 1,
                    headCount = sucklingPigletsCount
                )
            )
        }

        // 5. Heo thịt (isBatch = true)
        val growerBatches = pigs.filter { it.isBatch }
        growerBatches.forEach { batch ->
            val ageDays = ((now - batch.birthDate) / 86_400_000L).coerceAtLeast(0L)
            val week = ((ageDays / 7).toInt() + 1).coerceIn(4, 24)
            pigStageGroups.add(
                PigPhysiologicalGroup(
                    stage = SystemParameterEntity.STAGE_GROWER_FINISHER,
                    stageLabel = "Heo thịt",
                    week = week,
                    headCount = batch.quantity
                )
            )
        }

        // Nếu trại chưa có dữ liệu nái/thịt cụ thể, sử dụng mô phỏng đàn chuẩn 1,250 con
        if (pigStageGroups.isEmpty()) {
            pigStageGroups.addAll(getSimulatedHerdGroups())
        }

        // =========================================================================
        // BƯỚC 2: Tổng hợp nhu cầu cám theo từng loại cám trong ngày (E_ngày)
        // E_ngày(X) = sum(Số lượng heo * Định mức kg cám/ngày)
        // =========================================================================

        val feedUsageMap = mutableMapOf<String, MutableList<FeedGroupUsage>>()
        val feedNameMap = mutableMapOf<String, String>()

        pigStageGroups.forEach { group ->
            // Tìm định mức khớp nhất trong Ration Matrix theo stage & week
            val matchedRation = findBestRation(group.stage, group.week, rations)
            if (matchedRation != null) {
                val feedCode = matchedRation.subType.ifEmpty { "FEED_GENERAL" }
                val feedName = matchedRation.secondaryValue.ifEmpty { matchedRation.name }
                feedNameMap[feedCode] = feedName

                val rationPerHead = matchedRation.numericValue
                val totalForGroup = group.headCount * rationPerHead

                val usages = feedUsageMap.getOrPut(feedCode) { mutableListOf() }
                usages.add(
                    FeedGroupUsage(
                        stageName = group.stageLabel,
                        weekNumber = group.week,
                        headCount = group.headCount,
                        rationPerHeadKg = rationPerHead,
                        totalUsageKg = totalForGroup
                    )
                )
            }
        }

        // =========================================================================
        // BƯỚC 3: Liên kết tính toán tồn kho và số ngày dùng còn lại ở Trang chủ
        // Số ngày sử dụng còn lại (X) = Tổng lượng cám tồn kho (kg) / E_ngày(X)
        // =========================================================================

        val results = mutableListOf<FeedDemandResult>()
        var totalDailyKg = 0.0
        var totalStockKg = 0.0
        var totalStockBags = 0

        // Danh sách tất cả loại cám cần hiển thị (bao gồm cả trong kho và trong khẩu phần)
        val allFeedCodes = (feedUsageMap.keys + inventoryFeedItems.map { it.systemParamId ?: it.name }).distinct()

        val feedParams = rationMatrix.filter { it.category == SystemParameterEntity.CATEGORY_FEED }

        allFeedCodes.forEach { feedCode ->
            val usages = feedUsageMap[feedCode] ?: emptyList()
            val dailyDemand = usages.sumOf { it.totalUsageKg }
            val feedName = feedNameMap[feedCode]
                ?: inventoryFeedItems.find { it.systemParamId == feedCode || it.name == feedCode }?.name
                ?: getFeedDisplayName(feedCode)

            // Tìm mặt hàng tồn kho tương ứng
            val matchedInvItem = inventoryFeedItems.find {
                it.systemParamId == feedCode ||
                it.name.contains(feedName, ignoreCase = true) ||
                feedName.contains(it.name, ignoreCase = true) ||
                (feedCode.contains("MANG_THAI") && (it.name.contains("bầu", ignoreCase = true) || it.name.contains("mang thai", ignoreCase = true))) ||
                (feedCode.contains("NUOI_CON") && (it.name.contains("đẻ", ignoreCase = true) || it.name.contains("nuôi con", ignoreCase = true))) ||
                (feedCode.contains("TAP_AN") && it.name.contains("tập ăn", ignoreCase = true)) ||
                (feedCode.contains("CAI_SUA") && it.name.contains("cai sữa", ignoreCase = true)) ||
                (feedCode.contains("VO_BEO") && (it.name.contains("vỗ béo", ignoreCase = true) || it.name.contains("thịt", ignoreCase = true)))
            }

            val matchedParam = feedParams.find {
                it.code == feedCode ||
                it.id == matchedInvItem?.systemParamId ||
                it.name.equals(feedName, ignoreCase = true) ||
                it.name.contains(feedName, ignoreCase = true) ||
                feedName.contains(it.name, ignoreCase = true) ||
                (feedCode.contains("MANG_THAI") && (it.code.contains("MANG_THAI") || it.name.contains("bầu", ignoreCase = true))) ||
                (feedCode.contains("NUOI_CON") && (it.code.contains("NUOI_CON") || it.name.contains("đẻ", ignoreCase = true) || it.name.contains("nuôi con", ignoreCase = true))) ||
                (feedCode.contains("TAP_AN") && (it.code.contains("TAP_AN") || it.name.contains("tập ăn", ignoreCase = true))) ||
                (feedCode.contains("CAI_SUA") && (it.code.contains("CAI_SUA") || it.name.contains("cai sữa", ignoreCase = true))) ||
                (feedCode.contains("VO_BEO") && (it.code.contains("VO_BEO") || it.name.contains("vỗ béo", ignoreCase = true) || it.name.contains("thịt", ignoreCase = true)))
            }

            val safetyDays = matchedParam?.safetyDaysThreshold ?: 3
            val minStock = matchedParam?.minStockThreshold ?: 20.0

            val bagWeight = if (matchedInvItem != null) parseBagWeightKg(matchedInvItem.unit) else 25.0
            val bags = matchedInvItem?.currentStock?.toInt() ?: getDefaultBagsForFeed(feedCode)
            val stockKg = (matchedInvItem?.currentStock?.toDouble() ?: bags.toDouble()) * bagWeight

            val daysRemaining = if (dailyDemand > 0.0) {
                (stockKg / dailyDemand).toInt()
            } else {
                999
            }

            // Quy tắc cảnh báo:
            // Cảnh báo khẩn cấp (Đỏ): ngày dùng < ngưỡng ngày an toàn HOẶC tồn kho < mức tồn tối thiểu
            val isCritical = daysRemaining < safetyDays || (matchedParam != null && bags < minStock.toInt())
            val warningDaysThreshold = maxOf(safetyDays + 4, 7)
            val isWarning = !isCritical && daysRemaining < warningDaysThreshold

            // Gợi ý số lượng cần mua thêm (bao/kg) để đạt tồn kho an toàn
            val targetDays = maxOf(safetyDays + 7, 10)
            val neededStockKg = (dailyDemand * targetDays) + (minStock * bagWeight)
            val neededBags = kotlin.math.ceil(neededStockKg / bagWeight).toInt()
            val suggestedBags = if (isCritical || isWarning) {
                (neededBags - bags).coerceAtLeast(minStock.toInt())
            } else {
                0
            }
            val suggestedKg = suggestedBags * bagWeight

            totalDailyKg += dailyDemand
            totalStockKg += stockKg
            totalStockBags += bags

            results.add(
                FeedDemandResult(
                    feedCode = feedCode,
                    feedName = feedName,
                    dailyDemandKg = dailyDemand,
                    stockBags = bags,
                    stockKg = stockKg,
                    daysRemaining = daysRemaining,
                    isCritical = isCritical,
                    isWarning = isWarning,
                    groupUsages = usages,
                    safetyDaysThreshold = safetyDays,
                    minStockThreshold = minStock,
                    suggestedOrderBags = suggestedBags,
                    suggestedOrderKg = suggestedKg
                )
            )
        }

        // Sắp xếp các loại cám có nguy cơ hết hàng trước lên đầu
        results.sortBy { it.daysRemaining }

        val overallDays = if (totalDailyKg > 0.0) {
            (totalStockKg / totalDailyKg).toInt()
        } else {
            15
        }

        return FarmFeedAnalyticsResult(
            totalDailyConsumptionKg = totalDailyKg,
            totalStockKg = totalStockKg,
            totalStockBags = totalStockBags,
            overallDaysRemaining = overallDays,
            feedResults = results
        )
    }

    /**
     * Tìm định mức khẩu phần phù hợp nhất theo nhóm sinh lý và tuần tuổi.
     */
    private fun findBestRation(
        stage: String,
        week: Int,
        rations: List<SystemParameterEntity>
    ): SystemParameterEntity? {
        val stageRations = rations.filter { it.code == stage }
        if (stageRations.isEmpty()) return null

        // Ưu tiên khớp chính xác tuần
        val exact = stageRations.find { it.displayOrder == week }
        if (exact != null) return exact

        // Hoặc lấy tuần gần nhất <= tuần hiện tại
        return stageRations.filter { it.displayOrder <= week }.maxByOrNull { it.displayOrder }
            ?: stageRations.minByOrNull { it.displayOrder }
    }

    /**
     * Ma trận định mức khẩu phần mặc định theo chuẩn dinh dưỡng chăn nuôi công nghiệp
     */
    fun getDefaultRationMatrix(): List<SystemParameterEntity> {
        return listOf(
            // 1. Nái mang thai (16 tuần)
            SystemParameterEntity(
                id = "ration-preg-1",
                category = SystemParameterEntity.CATEGORY_FEED_RATION,
                name = "Nái mang thai (Tuần 1 - 12)",
                code = SystemParameterEntity.STAGE_PREGNANT_SOW,
                description = "Giai đoạn chửa kỳ 1 & 2 • Duy trì thể trạng",
                unit = "kg/ngày",
                displayOrder = 1,
                subType = "CAM_NAI_MANG_THAI",
                secondaryValue = "Cám hỗn hợp nái mang thai",
                numericValue = 2.0,
                statusText = "Tuần 1 - 12"
            ),
            SystemParameterEntity(
                id = "ration-preg-2",
                category = SystemParameterEntity.CATEGORY_FEED_RATION,
                name = "Nái mang thai (Tuần 13 - 16)",
                code = SystemParameterEntity.STAGE_PREGNANT_SOW,
                description = "Giai đoạn chửa kỳ cuối • Tăng ăn phát triển thai",
                unit = "kg/ngày",
                displayOrder = 13,
                subType = "CAM_NAI_MANG_THAI",
                secondaryValue = "Cám hỗn hợp nái mang thai",
                numericValue = 3.0,
                statusText = "Tuần 13 - 16"
            ),

            // 2. Nái nuôi con (1 - 4 tuần)
            SystemParameterEntity(
                id = "ration-lact-1",
                category = SystemParameterEntity.CATEGORY_FEED_RATION,
                name = "Nái nuôi con (Tuần 1)",
                code = SystemParameterEntity.STAGE_LACTATING_SOW,
                description = "Sau khi sinh • Kích thích tiết sữa",
                unit = "kg/ngày",
                displayOrder = 1,
                subType = "CAM_NAI_NUOI_CON",
                secondaryValue = "Cám nái nuôi con cao sản",
                numericValue = 4.0,
                statusText = "Tuần 1"
            ),
            SystemParameterEntity(
                id = "ration-lact-2",
                category = SystemParameterEntity.CATEGORY_FEED_RATION,
                name = "Nái nuôi con (Tuần 2)",
                code = SystemParameterEntity.STAGE_LACTATING_SOW,
                description = "Đỉnh tiết sữa cao điểm",
                unit = "kg/ngày",
                displayOrder = 2,
                subType = "CAM_NAI_NUOI_CON",
                secondaryValue = "Cám nái nuôi con cao sản",
                numericValue = 6.0,
                statusText = "Tuần 2"
            ),
            SystemParameterEntity(
                id = "ration-lact-3",
                category = SystemParameterEntity.CATEGORY_FEED_RATION,
                name = "Nái nuôi con (Tuần 3 - 4)",
                code = SystemParameterEntity.STAGE_LACTATING_SOW,
                description = "Giai đoạn cho con bú mạnh trước cai sữa",
                unit = "kg/ngày",
                displayOrder = 3,
                subType = "CAM_NAI_NUOI_CON",
                secondaryValue = "Cám nái nuôi con cao sản",
                numericValue = 6.5,
                statusText = "Tuần 3 - 4"
            ),

            // 3. Nái chờ phối (Tuần = 1)
            SystemParameterEntity(
                id = "ration-wait-1",
                category = SystemParameterEntity.CATEGORY_FEED_RATION,
                name = "Nái chờ phối",
                code = SystemParameterEntity.STAGE_WEANED_WAITING_SOW,
                description = "Kích thích lên giống nhanh sau cai sữa",
                unit = "kg/ngày",
                displayOrder = 1,
                subType = "CAM_NAI_MANG_THAI",
                secondaryValue = "Cám hỗn hợp nái mang thai",
                numericValue = 2.2,
                statusText = "Cố định tuần 1"
            ),

            // 4. Heo con theo mẹ (Tuần = 1)
            SystemParameterEntity(
                id = "ration-piglet-1",
                category = SystemParameterEntity.CATEGORY_FEED_RATION,
                name = "Heo con theo mẹ",
                code = SystemParameterEntity.STAGE_SUCKLING_PIGLET,
                description = "Tập ăn dặm từ ngày tuổi thứ 14",
                unit = "kg/ngày",
                displayOrder = 1,
                subType = "CAM_HEO_CON_TAP_AN",
                secondaryValue = "Cám tập ăn heo con sơ sinh",
                numericValue = 0.25,
                statusText = "Cố định tuần 1"
            ),

            // 5. Heo thịt (Tuần 4 đến 24)
            SystemParameterEntity(
                id = "ration-grower-1",
                category = SystemParameterEntity.CATEGORY_FEED_RATION,
                name = "Heo thịt GĐ 1 (Tuần 4 - 8)",
                code = SystemParameterEntity.STAGE_GROWER_FINISHER,
                description = "Giai đoạn sau cai sữa đến 30kg",
                unit = "kg/ngày",
                displayOrder = 4,
                subType = "CAM_SAU_CAI_SUA",
                secondaryValue = "Cám heo con sau cai sữa",
                numericValue = 0.9,
                statusText = "Tuần 4 - 8"
            ),
            SystemParameterEntity(
                id = "ration-grower-2",
                category = SystemParameterEntity.CATEGORY_FEED_RATION,
                name = "Heo thịt GĐ 2 (Tuần 9 - 16)",
                code = SystemParameterEntity.STAGE_GROWER_FINISHER,
                description = "Giai đoạn lớn nhanh 30kg - 70kg",
                unit = "kg/ngày",
                displayOrder = 9,
                subType = "CAM_THIT_VO_BEO",
                secondaryValue = "Cám vỗ béo heo thịt",
                numericValue = 1.8,
                statusText = "Tuần 9 - 16"
            ),
            SystemParameterEntity(
                id = "ration-grower-3",
                category = SystemParameterEntity.CATEGORY_FEED_RATION,
                name = "Heo thịt GĐ 3 (Tuần 17 - 24)",
                code = SystemParameterEntity.STAGE_GROWER_FINISHER,
                description = "Giai đoạn xuất bán 70kg - 115kg",
                unit = "kg/ngày",
                displayOrder = 17,
                subType = "CAM_THIT_VO_BEO",
                secondaryValue = "Cám vỗ béo heo thịt",
                numericValue = 2.8,
                statusText = "Tuần 17 - 24"
            )
        )
    }

    private fun getSimulatedHerdGroups(): List<PigPhysiologicalGroup> {
        return listOf(
            PigPhysiologicalGroup(SystemParameterEntity.STAGE_PREGNANT_SOW, "Nái mang thai", 8, 140),
            PigPhysiologicalGroup(SystemParameterEntity.STAGE_PREGNANT_SOW, "Nái mang thai", 14, 30),
            PigPhysiologicalGroup(SystemParameterEntity.STAGE_LACTATING_SOW, "Nái nuôi con", 2, 45),
            PigPhysiologicalGroup(SystemParameterEntity.STAGE_WEANED_WAITING_SOW, "Nái chờ phối", 1, 25),
            PigPhysiologicalGroup(SystemParameterEntity.STAGE_SUCKLING_PIGLET, "Heo con theo mẹ", 1, 495),
            PigPhysiologicalGroup(SystemParameterEntity.STAGE_GROWER_FINISHER, "Heo thịt", 6, 200),
            PigPhysiologicalGroup(SystemParameterEntity.STAGE_GROWER_FINISHER, "Heo thịt", 12, 350),
            PigPhysiologicalGroup(SystemParameterEntity.STAGE_GROWER_FINISHER, "Heo thịt", 19, 120)
        )
    }

    private fun getDefaultBagsForFeed(feedCode: String): Int {
        return when (feedCode) {
            "CAM_NAI_MANG_THAI" -> 120
            "CAM_NAI_NUOI_CON" -> 85
            "CAM_HEO_CON_TAP_AN" -> 50
            "CAM_SAU_CAI_SUA" -> 95
            "CAM_THIT_VO_BEO" -> 200
            else -> 80
        }
    }

    private fun getFeedDisplayName(feedCode: String): String {
        return when (feedCode) {
            "CAM_NAI_MANG_THAI" -> "Cám hỗn hợp nái mang thai"
            "CAM_NAI_NUOI_CON" -> "Cám nái nuôi con cao sản"
            "CAM_HEO_CON_TAP_AN" -> "Cám tập ăn heo con sơ sinh"
            "CAM_SAU_CAI_SUA" -> "Cám heo con sau cai sữa"
            "CAM_THIT_VO_BEO" -> "Cám vỗ béo heo thịt"
            else -> feedCode
        }
    }

    private data class PigPhysiologicalGroup(
        val stage: String,
        val stageLabel: String,
        val week: Int,
        val headCount: Int
    )
}
