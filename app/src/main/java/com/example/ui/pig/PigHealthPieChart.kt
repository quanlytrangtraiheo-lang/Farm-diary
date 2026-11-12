package com.example.ui.pig

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FarmAmber
import com.example.ui.theme.FarmGreenContainer
import com.example.ui.theme.FarmGreenPrimary
import com.example.ui.theme.FarmOutline
import com.example.ui.theme.FarmPurple
import com.example.ui.theme.FarmRed
import com.example.ui.theme.FarmTextPrimary
import com.example.ui.theme.FarmTextSecondary

/**
 * Mô hình dữ liệu phân bổ tình trạng sức khỏe đàn heo.
 */
data class HealthStatusSlice(
    val id: String,
    val label: String,
    val count: Int,
    val percentage: Float,
    val color: Color,
    val description: String
)

data class HealthDistribution(
    val totalCount: Int = 0,
    val healthyCount: Int = 0,
    val overdueVaccineCount: Int = 0,
    val ultrasoundWarningCount: Int = 0,
    val reproductiveWarningCount: Int = 0,
    val slices: List<HealthStatusSlice> = emptyList()
)

/**
 * Thẻ tóm tắt sức khỏe đàn heo tích hợp biểu đồ tròn (Pie/Donut Chart)
 * trực quan hóa phân bổ trạng thái sức khỏe: Khỏe mạnh, Quá hạn vắc-xin,
 * Cần siêu âm, Cảnh báo sinh sản.
 */
@Composable
fun PigHealthSummaryCard(
    distribution: HealthDistribution,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pig_health_summary_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, FarmOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Tiêu đề phần tổng quan
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
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(FarmGreenContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = null,
                            tint = FarmGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "PHÂN BỔ SỨC KHỎE ĐÀN HEO",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = FarmTextPrimary,
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Tỷ lệ sức khỏe & Cảnh báo rủi ro thú y",
                            fontSize = 11.sp,
                            color = FarmTextSecondary,
                            maxLines = 1
                        )
                    }
                }

                // Tỷ lệ an toàn tổng thể
                val safePercentage = if (distribution.totalCount > 0) {
                    ((distribution.healthyCount.toFloat() / distribution.totalCount) * 100f).toInt()
                } else 100

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (safePercentage >= 80) FarmGreenContainer else Color(0xFFFEE2E2),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(
                        text = "$safePercentage% AN TOÀN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (safePercentage >= 80) FarmGreenPrimary else Color(0xFFDC2626),
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // Nội dung Biểu đồ tròn và Chú giải (Legend)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Pie Chart Component
                Box(
                    modifier = Modifier
                        .size(136.dp)
                        .testTag("pig_health_pie_chart"),
                    contentAlignment = Alignment.Center
                ) {
                    HealthStatusDonutCanvas(
                        distribution = distribution,
                        modifier = Modifier.size(136.dp)
                    )

                    // Nội dung trung tâm của Donut Chart
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${distribution.totalCount}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = FarmTextPrimary,
                            lineHeight = 24.sp
                        )
                        Text(
                            text = "Cá thể/Lô",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FarmTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Chú giải chi tiết (Legend) với tông màu cảnh báo rủi ro rõ rệt
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val activeSlices = if (distribution.slices.isNotEmpty()) {
                        distribution.slices
                    } else {
                        listOf(
                            HealthStatusSlice("h", "Khỏe mạnh", distribution.healthyCount, 100f, Color(0xFF15803D), "Ổn định"),
                            HealthStatusSlice("v", "Quá hạn vắc-xin", distribution.overdueVaccineCount, 0f, Color(0xFFDC2626), "Cần tiêm ngay"),
                            HealthStatusSlice("u", "Cần siêu âm", distribution.ultrasoundWarningCount, 0f, Color(0xFFD97706), "Chờ khám thai"),
                            HealthStatusSlice("r", "Cảnh báo sinh sản", distribution.reproductiveWarningCount, 0f, Color(0xFFBE123C), "Theo dõi gấp")
                        )
                    }

                    activeSlices.forEach { slice ->
                        HealthLegendItem(
                            slice = slice,
                            totalCount = distribution.totalCount
                        )
                    }
                }
            }
        }
    }
}

/**
 * Vẽ biểu đồ Donut / Pie Chart phân bổ sức khỏe đàn heo với animation mềm mại trên Canvas Jetpack Compose.
 */
@Composable
fun HealthStatusDonutCanvas(
    distribution: HealthDistribution,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val strokeWidth = 20.dp
    val strokeWidthPx = with(density) { strokeWidth.toPx() }

    // Hiệu ứng animate mượt khi tải dữ liệu
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(distribution.totalCount) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    val activeSlices = remember(distribution) {
        val nonZero = distribution.slices.filter { it.count > 0 }
        if (nonZero.isNotEmpty()) nonZero
        else listOf(
            HealthStatusSlice("empty", "Chưa có dữ liệu", 1, 100f, Color(0xFFE0E5E0), "Trống")
        )
    }

    Canvas(modifier = modifier) {
        val diameter = size.minDimension - strokeWidthPx
        val radius = diameter / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val topLeft = Offset(center.x - radius, center.y - radius)
        val arcSize = Size(diameter, diameter)

        var startAngle = -90f
        val total = if (distribution.totalCount > 0) distribution.totalCount.toFloat() else 1f

        activeSlices.forEach { slice ->
            val sliceRatio = slice.count.toFloat() / total
            val sweepAngle = sliceRatio * 360f * animationProgress.value

            drawArc(
                color = slice.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt)
            )

            startAngle += sweepAngle
        }
    }
}

@Composable
private fun HealthLegendItem(
    slice: HealthStatusSlice,
    totalCount: Int
) {
    val pct = if (totalCount > 0) {
        ((slice.count.toFloat() / totalCount) * 100f).toInt()
    } else 0

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(slice.color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = slice.label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = FarmTextPrimary,
                maxLines = 1
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${slice.count}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = FarmTextPrimary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = slice.color.copy(alpha = 0.12f)
            ) {
                Text(
                    text = "$pct%",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = slice.color,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}
