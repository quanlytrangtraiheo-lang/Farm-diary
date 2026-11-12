package com.example.ui.pig

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PigEntity
import com.example.data.local.entity.SystemParameterEntity
import com.example.data.model.SowReproductiveStatus
import com.example.ui.theme.FarmGreenContainer
import com.example.ui.theme.FarmGreenPrimary
import com.example.ui.theme.FarmOutline
import com.example.ui.theme.FarmTextPrimary
import com.example.ui.theme.FarmTextSecondary

/**
 * Phân loại nhóm chuồng nuôi theo logic quản trị trang trại:
 * 1. Chuồng phối: 2 cột 3D gồm Nái mang thai (xanh dương) & Nái chờ phối (cam)
 * 2. Chuồng đẻ: 3 cột 3D gồm Nái mang thai (bầu chờ đẻ - xanh da trời), Nái nuôi con (xanh lá), Heo con theo mẹ (tím)
 * 3. Chuồng thịt: 1 cột 3D duy nhất đại diện cho tổng Heo thịt trong chuồng đó (teal)
 */
enum class BarnCategoryType(val label: String) {
    MATING_GESTATION("Chuồng Phối / Bầu"),
    FARROWING("Chuồng Đẻ"),
    GROWER("Chuồng Thịt")
}

/**
 * Dữ liệu của một cột 3D con trong cụm chuồng
 */
data class BarnBarSubSegment(
    val label: String,
    val count: Int,
    val color: Color
) {
    val topFacetColor: Color
        get() = color.copy(
            red = (color.red * 1.25f).coerceAtMost(1f),
            green = (color.green * 1.25f).coerceAtMost(1f),
            blue = (color.blue * 1.25f).coerceAtMost(1f)
        )

    val sideExtrusionColor: Color
        get() = color.copy(
            red = color.red * 0.70f,
            green = color.green * 0.70f,
            blue = color.blue * 0.70f
        )
}

/**
 * Mô hình dữ liệu hiển thị trên Trục hoành (X-Axis) cho từng chuồng
 */
data class Barn3DColumnData(
    val barnId: String,
    val barnName: String,
    val barnCode: String,
    val barnType: BarnCategoryType,
    val totalCount: Int,
    val subBars: List<BarnBarSubSegment>
)

object BarnDistributionCalculator {

    fun detectBarnType(barn: SystemParameterEntity): BarnCategoryType {
        val name = barn.name.lowercase()
        val code = barn.code.lowercase()
        val desc = barn.description.lowercase()

        return when {
            name.contains("thịt") || name.contains("thit") || code.contains("thit") || code.contains("grower") || desc.contains("thịt") ->
                BarnCategoryType.GROWER
            name.contains("đẻ") || name.contains("de") || code.contains("de") || code.contains("farrow") || desc.contains("đẻ") ->
                BarnCategoryType.FARROWING
            else ->
                BarnCategoryType.MATING_GESTATION
        }
    }

    fun matchPigToBarn(pig: PigEntity, barnNameOrCode: String): Boolean {
        val loc = pig.location.trim().lowercase()
        val target = barnNameOrCode.trim().lowercase()
        if (loc.isEmpty() || target.isEmpty()) return false

        if (loc == target || loc.contains(target) || target.contains(loc)) return true

        // Chuẩn hóa loại bỏ các từ thông dụng để khớp chính xác
        val cleanLoc = loc.replace("khu", "").replace("chuồng", "").trim()
        val cleanTarget = target.replace("khu", "").replace("chuồng", "").trim()
        if (cleanLoc.isNotEmpty() && cleanTarget.isNotEmpty() && (cleanLoc.contains(cleanTarget) || cleanTarget.contains(cleanLoc))) {
            return true
        }

        // Khớp theo từ khóa đặc thù
        if (target.contains("bầu") && loc.contains("bầu")) return true
        if (target.contains("phối") && (loc.contains("phối") || loc.contains("cai sữa"))) return true
        if (target.contains("đẻ") && loc.contains("đẻ")) return true
        if (target.contains("thịt 1") && loc.contains("thịt 1")) return true
        if ((target.contains("thịt 2") || target.contains("thịt 3")) && (loc.contains("thịt 2") || loc.contains("thịt 3"))) return true

        return false
    }

    /**
     * Tính toán cơ cấu đàn phân rã theo từng chuồng nuôi thực tế từ cơ sở dữ liệu
     */
    fun calculate(
        pigs: List<PigEntity>,
        barnParams: List<SystemParameterEntity>
    ): List<Barn3DColumnData> {
        val effectiveBarns = if (barnParams.isNotEmpty()) barnParams else getDefaultBarns()

        return effectiveBarns.map { barn ->
            val barnType = detectBarnType(barn)
            val matchingPigs = pigs.filter { matchPigToBarn(it, barn.name) || matchPigToBarn(it, barn.code) }

            val subBars = when (barnType) {
                BarnCategoryType.MATING_GESTATION -> {
                    // Chuồng phối: 2 cột gồm Nái mang thai (xanh dương) và Nái chờ phối (cam)
                    val pregnantCount = matchingPigs.filter { it.status == SowReproductiveStatus.MANG_THAI }.sumOf { it.quantity }
                    val waitingCount = matchingPigs.filter {
                        it.status == SowReproductiveStatus.CAI_SUA_CHO_PHOI || it.status == SowReproductiveStatus.HAU_BI
                    }.sumOf { it.quantity }

                    listOf(
                        BarnBarSubSegment("Nái mang thai", pregnantCount, Color(0xFF1976D2)),
                        BarnBarSubSegment("Nái chờ phối", waitingCount, Color(0xFFFB8C00))
                    )
                }

                BarnCategoryType.FARROWING -> {
                    // Chuồng đẻ: 3 cột gồm Nái mang thai (bầu chờ đẻ), Nái nuôi con (xanh lá), Heo con theo mẹ (tím)
                    val pregnantPendingCount = matchingPigs.filter { it.status == SowReproductiveStatus.MANG_THAI }.sumOf { it.quantity }
                    val lactatingCount = matchingPigs.filter { it.status == SowReproductiveStatus.NUOI_CON && !it.isBatch }.sumOf { it.quantity }

                    val directPiglets = matchingPigs.filter {
                        it.tagCode.contains("heo con", ignoreCase = true) ||
                        it.breed.contains("heo con", ignoreCase = true) ||
                        (it.isBatch && it.status == SowReproductiveStatus.NUOI_CON)
                    }.sumOf { it.quantity }

                    val sucklingPiglets = if (directPiglets > 0) directPiglets else (lactatingCount * 11)

                    listOf(
                        BarnBarSubSegment("Nái mang thai", pregnantPendingCount, Color(0xFF42A5F5)),
                        BarnBarSubSegment("Nái nuôi con", lactatingCount, Color(0xFF43A047)),
                        BarnBarSubSegment("Heo con theo mẹ", sucklingPiglets, Color(0xFF8E24AA))
                    )
                }

                BarnCategoryType.GROWER -> {
                    // Chuồng thịt: Chỉ 1 cột duy nhất đại diện cho tổng Heo thịt trong chuồng đó
                    val growerCount = matchingPigs.sumOf { it.quantity }
                    listOf(
                        BarnBarSubSegment("Heo thịt", growerCount, Color(0xFF00897B))
                    )
                }
            }

            val total = subBars.sumOf { it.count }

            Barn3DColumnData(
                barnId = barn.id,
                barnName = barn.name,
                barnCode = barn.code,
                barnType = barnType,
                totalCount = total,
                subBars = subBars
            )
        }
    }

    fun getDefaultBarns(): List<SystemParameterEntity> {
        return listOf(
            SystemParameterEntity("barn-1", SystemParameterEntity.CATEGORY_BARN, "Chuồng Phối 1", "CHUONG_PHOI_1", "Nái phối & mang thai kỳ 1", "", 1),
            SystemParameterEntity("barn-2", SystemParameterEntity.CATEGORY_BARN, "Chuồng Phối 2", "CHUONG_PHOI_2", "Nái mang thai kỳ 2", "", 2),
            SystemParameterEntity("barn-3", SystemParameterEntity.CATEGORY_BARN, "Chuồng Đẻ 1", "CHUONG_DE_1", "Nái đẻ và nuôi heo con", "", 3),
            SystemParameterEntity("barn-4", SystemParameterEntity.CATEGORY_BARN, "Chuồng Đẻ 2", "CHUONG_DE_2", "Nái đẻ và nuôi heo con", "", 4),
            SystemParameterEntity("barn-5", SystemParameterEntity.CATEGORY_BARN, "Chuồng Thịt 1", "CHUONG_THIT_1", "Heo thịt thương phẩm giai đoạn 1", "", 5),
            SystemParameterEntity("barn-6", SystemParameterEntity.CATEGORY_BARN, "Chuồng Thịt 2", "CHUONG_THIT_2", "Heo thịt xuất bán giai đoạn 2", "", 6)
        )
    }
}

/**
 * Biểu đồ 3D Phân bố chuồng heo với các cột 3D Isometric nổi khối (Pseudo-3D Isometric Bars)
 * Hỗ trợ trục hoành động theo Tham số hệ thống và tương tác Touch Drill-down
 */
@Composable
fun BarnDistribution3DSection(
    distribution: List<Barn3DColumnData>,
    selectedBarn: String?,
    onBarnSelected: (String) -> Unit,
    onClearBarnFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Tìm giá trị max count để chuẩn hóa tỷ lệ chiều cao các cột 3D
    val maxBarCount = distribution.flatMap { it.subBars }.maxOfOrNull { it.count }?.coerceAtLeast(10) ?: 100

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("barn_distribution_3d_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, FarmOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Tiêu đề + Chế độ lọc Drill-down
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(end = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(FarmGreenContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warehouse,
                            contentDescription = null,
                            tint = FarmGreenPrimary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "MẬT ĐỘ THEO CHUỒNG",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = FarmTextPrimary,
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Trục hoành chuồng • Chạm vào cột để lọc chi tiết",
                            fontSize = 11.sp,
                            color = FarmTextSecondary,
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF1F8E9),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(
                        text = "${distribution.size} CHUỒNG",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF33691E),
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // Legend: Chú thích 5 nhóm màu quy ước
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendBadge(color = Color(0xFF1976D2), label = "Nái mang thai")
                LegendBadge(color = Color(0xFFFB8C00), label = "Nái chờ phối")
                LegendBadge(color = Color(0xFF43A047), label = "Nái nuôi con")
                LegendBadge(color = Color(0xFF8E24AA), label = "Heo con theo mẹ")
                LegendBadge(color = Color(0xFF00897B), label = "Heo thịt")
            }

            // Thanh trạng thái Drill-down khi có chuồng được chọn
            if (selectedBarn != null) {
                val selectedData = distribution.find { it.barnName == selectedBarn || it.barnCode == selectedBarn }
                Surface(
                    color = FarmGreenContainer,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FarmGreenPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FilterAlt,
                                contentDescription = null,
                                tint = FarmGreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Đang lọc: ${selectedData?.barnName ?: selectedBarn} (${selectedData?.totalCount ?: 0} con)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = FarmGreenPrimary
                            )
                        }

                        IconButton(
                            onClick = onClearBarnFilter,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Bỏ lọc",
                                tint = FarmGreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Trục biểu đồ 3D cuộn ngang: Trục hoành động hoàn toàn theo danh sách chuồng trong Tham số hệ thống
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                distribution.forEach { barnData ->
                    val isSelected = selectedBarn == barnData.barnName || selectedBarn == barnData.barnCode

                    Barn3DColumnCluster(
                        columnData = barnData,
                        maxCount = maxBarCount,
                        isSelected = isSelected,
                        onClick = { onBarnSelected(barnData.barnName) }
                    )
                }
            }
        }
    }
}

/**
 * Một cụm cột 3D đại diện cho 1 chuồng nuôi
 */
@Composable
private fun Barn3DColumnCluster(
    columnData: Barn3DColumnData,
    maxCount: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clusterWidth = when (columnData.subBars.size) {
        1 -> 76.dp
        2 -> 96.dp
        else -> 118.dp
    }

    Column(
        modifier = modifier
            .width(clusterWidth)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) FarmGreenContainer.copy(alpha = 0.5f) else Color.Transparent)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) FarmGreenPrimary else FarmOutline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Tổng số con trên đỉnh cụm cột
        Text(
            text = "${columnData.totalCount}",
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isSelected) FarmGreenPrimary else FarmTextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Vùng Canvas vẽ các cột 3D Isometric với chiều cao tinh gọn 96dp và thanh dữ liệu dày dặn
        Canvas(
            modifier = Modifier
                .width(clusterWidth - 8.dp)
                .height(96.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val depthX = 6.dp.toPx()
            val depthY = 4.dp.toPx()
            val availableHeight = canvasHeight - depthY - 6.dp.toPx()

            val subBarCount = columnData.subBars.size
            val barSpacing = 3.dp.toPx()
            val totalSpacing = (subBarCount - 1) * barSpacing
            val barWidth = ((canvasWidth - depthX - totalSpacing) / subBarCount).coerceAtLeast(14.dp.toPx())

            // Đường trục cơ sở (Baseline)
            drawLine(
                color = Color(0xFFCFD8DC),
                start = Offset(0f, canvasHeight),
                end = Offset(canvasWidth, canvasHeight),
                strokeWidth = 1.5.dp.toPx()
            )

            // Vẽ từng cột 3D trong cụm
            columnData.subBars.forEachIndexed { index, subBar ->
                val x = index * (barWidth + barSpacing)
                val normalizedHeight = if (maxCount > 0) {
                    ((subBar.count.toFloat() / maxCount.toFloat()) * availableHeight).coerceIn(6.dp.toPx(), availableHeight)
                } else {
                    6.dp.toPx()
                }
                val y = canvasHeight - normalizedHeight

                // 1. Mặt trước (Front Facet)
                drawRect(
                    color = subBar.color,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, normalizedHeight)
                )

                // 2. Mặt trên phối cảnh nghiêng (Top Rhombus Facet)
                val topPath = Path().apply {
                    moveTo(x, y)
                    lineTo(x + barWidth, y)
                    lineTo(x + barWidth + depthX, y - depthY)
                    lineTo(x + depthX, y - depthY)
                    close()
                }
                drawPath(topPath, color = subBar.topFacetColor)

                // 3. Mặt bên phải tạo chiều sâu bóng đổ (Right Extrusion Facet)
                val sidePath = Path().apply {
                    moveTo(x + barWidth, y)
                    lineTo(x + barWidth + depthX, y - depthY)
                    lineTo(x + barWidth + depthX, y + normalizedHeight - depthY)
                    lineTo(x + barWidth, y + normalizedHeight)
                    close()
                }
                drawPath(sidePath, color = subBar.sideExtrusionColor)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Nhãn trục hoành (Tên chuồng)
        Text(
            text = columnData.barnName,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
            color = if (isSelected) FarmGreenPrimary else FarmTextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 13.sp
        )

        // Phân loại chuồng
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = when (columnData.barnType) {
                BarnCategoryType.MATING_GESTATION -> Color(0xFFE3F2FD)
                BarnCategoryType.FARROWING -> Color(0xFFE8F5E9)
                BarnCategoryType.GROWER -> Color(0xFFE0F2F1)
            },
            modifier = Modifier.padding(top = 2.dp)
        ) {
            Text(
                text = when (columnData.barnType) {
                    BarnCategoryType.MATING_GESTATION -> "Phối/Bầu"
                    BarnCategoryType.FARROWING -> "Đẻ"
                    BarnCategoryType.GROWER -> "Thịt"
                },
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = when (columnData.barnType) {
                    BarnCategoryType.MATING_GESTATION -> Color(0xFF1565C0)
                    BarnCategoryType.FARROWING -> Color(0xFF2E7D32)
                    BarnCategoryType.GROWER -> Color(0xFF00695C)
                },
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }
    }
}

@Composable
private fun LegendBadge(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = FarmTextSecondary
        )
    }
}
