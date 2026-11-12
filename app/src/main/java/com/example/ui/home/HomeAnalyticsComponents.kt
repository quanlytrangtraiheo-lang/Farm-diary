package com.example.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.settings.formatVnd
import com.example.ui.theme.FarmAmber
import com.example.ui.theme.FarmAmberContainer
import com.example.ui.theme.FarmBackground
import com.example.ui.theme.FarmBlue
import com.example.ui.theme.FarmBlueContainer
import com.example.ui.theme.FarmGreenContainer
import com.example.ui.theme.FarmGreenPrimary
import com.example.ui.theme.FarmOutline
import com.example.ui.theme.FarmRed
import com.example.ui.theme.FarmRedContainer
import com.example.ui.theme.FarmTextPrimary
import com.example.ui.theme.FarmTextSecondary
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * Phần Thống kê Lượng cám ăn (Daily Feed Analytics):
 * - 3D Chart phân bổ thức ăn trong ngày giữa 5 dòng cám chuyên biệt
 * - Phát hiện biến động lượng ăn bất thường giữa các ô chuồng
 * - Dự báo Cám tồn & Ngày sử dụng dựa trên tốc độ ăn trung bình
 */
@Composable
fun DailyFeedAnalyticsSection(
    analytics: DailyFeedAnalytics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_section_feed_analytics"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        var selectedChartTab by remember { mutableStateOf(0) } // 0: Lượng ăn trong ngày | 1: Cám tồn & Ngày dùng

        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tiêu đề & Chỉ số FCR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(FarmAmberContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = FarmAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "PHÂN TÍCH & DỰ BÁO CÁM",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = FarmTextPrimary
                        )
                        Text(
                            text = "Feed Analytics & Silo Forecast",
                            fontSize = 11.sp,
                            color = FarmTextSecondary
                        )
                    }
                }

                Surface(
                    color = FarmGreenContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "FCR: ${String.format(Locale.US, "%.2f", analytics.fcrIndex)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = FarmGreenPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Thanh chuyển đổi Biểu đồ linh hoạt (Chart Carousel / Switcher)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(FarmBackground)
                    .border(1.dp, FarmOutline.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedChartTab = 0 }
                        .testTag("tab_feed_consumption"),
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedChartTab == 0) FarmGreenPrimary else Color.Transparent
                ) {
                    Text(
                        text = "1. Lượng ăn trong ngày",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedChartTab == 0) Color.White else FarmTextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 7.dp)
                    )
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedChartTab = 1 }
                        .testTag("tab_feed_stock"),
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedChartTab == 1) FarmGreenPrimary else Color.Transparent
                ) {
                    Text(
                        text = "2. Cám tồn & Ngày dùng",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedChartTab == 1) Color.White else FarmTextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 7.dp)
                    )
                }
            }

            if (selectedChartTab == 0) {
                // TAB 1: LƯỢNG CÁM ĂN TRONG NGÀY
                // Thanh tóm tắt gọn gàng
                Surface(
                    color = FarmBackground,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Tổng tiêu thụ:", fontSize = 11.sp, color = FarmTextSecondary)
                            Text(
                                text = "${String.format(Locale.US, "%,.0f", analytics.totalDailyFeedKg)} kg",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = FarmTextPrimary
                            )
                        }
                        Text(
                            text = "≈ ${String.format(Locale.US, "%.1f", analytics.totalDailyFeedKg / 25.0)} bao (25kg)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = FarmAmber
                        )
                    }
                }

                // Thanh đo Thực tế vs. Định mức (Target Bar) thay thế biểu đồ tròn 3D không hiệu quả
                FeedTargetComparisonBar(
                    actualKg = analytics.totalDailyFeedKg,
                    targetKg = 500.0,
                    modifier = Modifier.fillMaxWidth()
                )

                // Phân bổ các dòng cám (Lọc các dòng có tiêu thụ > 0 kg và ẩn các dòng 0 kg vào nút mở rộng)
                val activeFeedItems = remember(analytics.feedConsumptionList) {
                    analytics.feedConsumptionList.filter { it.amountKg > 0 }
                }
                val zeroFeedItems = remember(analytics.feedConsumptionList) {
                    analytics.feedConsumptionList.filter { it.amountKg <= 0 }
                }
                var showZeroFeeds by remember { mutableStateOf(false) }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DANH SÁCH CÁM TIÊU THỤ TRONG NGÀY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FarmTextSecondary,
                            letterSpacing = 0.5.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = FarmGreenContainer
                        ) {
                            Text(
                                text = "${activeFeedItems.size} loại phát sinh",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = FarmGreenPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Danh sách cám có phát sinh tiêu thụ (>0 kg)
                    activeFeedItems.forEach { item ->
                        FeedConsumptionRowItem(item = item)
                    }

                    // Nút mở rộng/thu gọn danh sách cám 0 kg chưa dùng
                    if (zeroFeedItems.isNotEmpty()) {
                        Surface(
                            onClick = { showZeroFeeds = !showZeroFeeds },
                            shape = RoundedCornerShape(8.dp),
                            color = FarmBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, FarmOutline.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_toggle_zero_feed_items")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (!showZeroFeeds) "+ Xem ${zeroFeedItems.size} loại cám chưa dùng (0 kg) ▾" else "- Thu gọn ${zeroFeedItems.size} loại cám chưa dùng ▴",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FarmTextSecondary
                                )
                            }
                        }

                        if (showZeroFeeds) {
                            zeroFeedItems.forEach { item ->
                                FeedConsumptionRowItem(item = item, isZero = true)
                            }
                        }
                    }
                }
            } else {
                // TAB 2: DỰ BÁO CÁM TỒN & NGÀY SỬ DỤNG
                FeedStockForecastSection(analytics = analytics)
            }
        }
    }
}

/**
 * Biểu đồ 3D Donut Chart mô phỏng phối cảnh nghiêng (Isometric / Pseudo-3D Perspective)
 * có độ dày thành khối (Extrusion depth), màu bóng đổ và tâm hiển thị khối lượng tiêu thụ.
 */
@Composable
fun DailyFeed3DChart(
    feedItems: List<FeedConsumptionItem>,
    totalDailyKg: Double,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.testTag("feed_3d_chart"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (feedItems.isEmpty()) return@Canvas

            val chartWidth = size.width * 0.82f
            val chartHeight = size.height * 0.58f
            val depth = 28.dp.toPx()
            val left = (size.width - chartWidth) / 2f
            val top = (size.height - chartHeight - depth) / 2f

            val total = feedItems.sumOf { it.amountKg }.toFloat().coerceAtLeast(1f)
            var currentAngle = 0f

            // Vòng 1: Vẽ độ dày khối 3D (Extrusion layer hướng xuống dưới)
            feedItems.forEach { item ->
                val sweep = (item.amountKg.toFloat() / total) * 360f
                val color = Color(item.colorHex)
                val darkColor = color.copy(
                    red = color.red * 0.72f,
                    green = color.green * 0.72f,
                    blue = color.blue * 0.72f
                )

                // Chỉ vẽ thành biên cho các góc quay về phía người nhìn (0 đến 180 độ)
                draw3DSliceExtrusion(
                    left = left,
                    top = top,
                    width = chartWidth,
                    height = chartHeight,
                    depth = depth,
                    startAngle = currentAngle,
                    sweepAngle = sweep,
                    color = darkColor
                )

                currentAngle += sweep
            }

            // Vòng 2: Vẽ mặt trên biểu đồ (Top surface)
            currentAngle = 0f
            feedItems.forEach { item ->
                val sweep = (item.amountKg.toFloat() / total) * 360f
                val color = Color(item.colorHex)

                drawArc(
                    color = color,
                    startAngle = currentAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    topLeft = Offset(left, top),
                    size = Size(chartWidth, chartHeight)
                )

                currentAngle += sweep
            }

            // Vòng 3: Khoét tâm 3D Donut Hole với phối cảnh
            val holeWidth = chartWidth * 0.52f
            val holeHeight = chartHeight * 0.52f
            val holeLeft = left + (chartWidth - holeWidth) / 2f
            val holeTop = top + (chartHeight - holeHeight) / 2f

            // Đáy lỗ trong tạo bóng 3D
            drawOval(
                color = Color(0xFFF1F5F0),
                topLeft = Offset(holeLeft, holeTop + (depth * 0.35f)),
                size = Size(holeWidth, holeHeight)
            )

            // Mặt lỗ trong
            drawOval(
                color = Color.White,
                topLeft = Offset(holeLeft, holeTop),
                size = Size(holeWidth, holeHeight)
            )

            // Viền nhẹ cho tâm
            drawOval(
                color = FarmOutline.copy(alpha = 0.6f),
                topLeft = Offset(holeLeft, holeTop),
                size = Size(holeWidth, holeHeight),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        // Nhãn khối lượng ở tâm Donut
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(
                text = "${String.format(Locale.US, "%.2f", totalDailyKg / 1000.0)}",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = FarmTextPrimary
            )
            Text(
                text = "Tấn / ngày",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = FarmTextSecondary
            )
        }
    }
}

/**
 * Vẽ diện thành khối 3D cho lát cắt của biểu đồ tròn nghiêng.
 */
private fun DrawScope.draw3DSliceExtrusion(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    depth: Float,
    startAngle: Float,
    sweepAngle: Float,
    color: Color
) {
    val steps = 18
    val angleStep = sweepAngle / steps
    val cx = left + width / 2f
    val cy = top + height / 2f
    val rx = width / 2f
    val ry = height / 2f

    val path = Path()
    var isFirst = true

    for (i in 0..steps) {
        val angleDeg = startAngle + i * angleStep
        val angleRad = Math.toRadians(angleDeg.toDouble())
        // Chỉ vẽ bề mặt hướng xuống phía người nhìn
        val sinVal = sin(angleRad).toFloat()
        if (sinVal >= -0.05f) {
            val px = cx + rx * cos(angleRad).toFloat()
            val py = cy + ry * sinVal
            if (isFirst) {
                path.moveTo(px, py)
                isFirst = false
            } else {
                path.lineTo(px, py)
            }
        }
    }

    if (!isFirst) {
        for (i in steps downTo 0) {
            val angleDeg = startAngle + i * angleStep
            val angleRad = Math.toRadians(angleDeg.toDouble())
            val sinVal = sin(angleRad).toFloat()
            if (sinVal >= -0.05f) {
                val px = cx + rx * cos(angleRad).toFloat()
                val py = cy + ry * sinVal + depth
                path.lineTo(px, py)
            }
        }
        path.close()
        drawPath(path = path, color = color, style = Fill)
    }
}

/**
 * Hiển thị 1 dòng cám chuyên biệt kèm cảnh báo biến động bất thường
 */
/**
 * Thanh đo Thực tế vs. Định mức (Target Bar):
 * Trực quan hóa tương quan giữa Lượng cám ăn thực tế và Định mức dinh dưỡng tiêu chuẩn (500 kg),
 * cảnh báo mạnh mẽ khi thực tế vượt quá định mức nhằm ngăn chặn thất thoát hoặc lãng phí cám.
 */
@Composable
fun FeedTargetComparisonBar(
    actualKg: Double,
    targetKg: Double = 500.0,
    modifier: Modifier = Modifier
) {
    val effectiveTarget = if (targetKg > 0) targetKg else 500.0
    val ratio = actualKg / effectiveTarget
    val percent = (ratio * 100).toInt()
    val isOverDouble = actualKg >= effectiveTarget * 2
    val isOverTarget = actualKg > effectiveTarget

    Surface(
        color = if (isOverDouble) Color(0xFFFFF1F2) else FarmBackground,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isOverDouble) Color(0xFFFECDD3) else FarmOutline
        ),
        modifier = modifier.fillMaxWidth().testTag("feed_target_comparison_bar")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header số liệu so sánh
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TIÊU THỤ THỰC TẾ VS. ĐỊNH MỨC",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isOverDouble) Color(0xFFBE123C) else FarmTextSecondary,
                    letterSpacing = 0.5.sp
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isOverDouble) Color(0xFFDC2626) else if (isOverTarget) FarmAmber else FarmGreenPrimary
                ) {
                    Text(
                        text = if (isOverDouble) "VƯỢT $percent% ĐỊNH MỨC" else "$percent%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Thanh đo ngang tỉ lệ (Target Bar)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(Color(0xFFE5E7EB))
            ) {
                // Tỷ lệ thực tế trên thanh: chuẩn 500kg đặt ở mốc 43% thanh đo
                val targetFractionOnBar = 0.43f
                val fillFraction = ((actualKg / (effectiveTarget * 2.3f)).toFloat()).coerceIn(0.08f, 1f)

                // Thanh thực tế
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fillFraction)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(15.dp))
                        .background(
                            if (isOverDouble) Brush.horizontalGradient(
                                listOf(Color(0xFFEA580C), Color(0xFFDC2626))
                            ) else if (isOverTarget) Brush.horizontalGradient(
                                listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                            ) else Brush.horizontalGradient(
                                listOf(FarmGreenPrimary, Color(0xFF2E7D32))
                            )
                        )
                )

                // Vạch chỉ tiêu chuẩn (500 kg)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(targetFractionOnBar)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Box(
                        modifier = Modifier
                            .width(2.5.dp)
                            .fillMaxHeight()
                            .background(Color(0xFF1E293B))
                    )
                }

                // Nhãn chữ bên trong thanh đo
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Định mức: ${String.format(Locale.US, "%,.0f", effectiveTarget)} kg",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Thực tế: ${String.format(Locale.US, "%,.0f", actualKg)} kg",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            // Dòng cảnh báo nổi bật khi Thực tế vượt định mức
            if (isOverDouble) {
                Surface(
                    color = Color(0xFFFEE2E2),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "THỰC TẾ > ĐỊNH MỨC GẤP ĐÔI (CHỈ TIÊU: ${effectiveTarget.toInt()} KG)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF991B1B)
                            )
                            Text(
                                text = "Vọt lên ${String.format(Locale.US, "%,.0f", actualKg)} kg (vượt $percent%). Nguy cơ thất thoát, cấp thừa cám hoặc rò rỉ máng ăn!",
                                fontSize = 10.sp,
                                color = Color(0xFFB91C1C)
                            )
                        }
                    }
                }
            } else if (isOverTarget) {
                Text(
                    text = "⚠️ Tiêu thụ vượt định mức ${actualKg - effectiveTarget} kg. Cần theo dõi chỉ số FCR.",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FarmAmber
                )
            } else {
                Text(
                    text = "✓ Tiêu thụ cám đạt chuẩn theo định mức dinh dưỡng chuồng.",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = FarmGreenPrimary
                )
            }
        }
    }
}

/**
 * Hiển thị 1 dòng cám chuyên biệt kèm cảnh báo biến động bất thường
 */
@Composable
private fun FeedConsumptionRowItem(
    item: FeedConsumptionItem,
    isZero: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (item.isAbnormal) FarmRedContainer.copy(alpha = 0.5f) else if (isZero) Color(0xFFF9FAFB) else FarmBackground,
        shape = RoundedCornerShape(10.dp),
        border = if (isZero) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)) else null,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(if (isZero) Color(0xFF9CA3AF) else Color(item.colorHex), CircleShape)
                )
                Column {
                    Text(
                        text = item.name,
                        fontSize = 13.sp,
                        fontWeight = if (isZero) FontWeight.Medium else FontWeight.Bold,
                        color = if (isZero) Color(0xFF6B7280) else FarmTextPrimary
                    )
                    Text(
                        text = "${item.barnTarget} • ${item.statusNote}",
                        fontSize = 11.sp,
                        color = if (item.isAbnormal) FarmRed else Color(0xFF9CA3AF),
                        fontWeight = if (item.isAbnormal) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${String.format(Locale.US, "%,.0f", item.amountKg)} kg",
                    fontSize = 13.sp,
                    fontWeight = if (isZero) FontWeight.Medium else FontWeight.ExtraBold,
                    color = if (isZero) Color(0xFF9CA3AF) else FarmTextPrimary
                )
                Text(
                    text = "${String.format(Locale.US, "%.1f", item.percentage)}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isZero) Color(0xFF9CA3AF) else Color(item.colorHex)
                )
            }
        }
    }
}

/**
 * Dự báo Cám tồn & Ngày sử dụng:
 * Hiển thị song song số bao/tấn còn trong silo/kho và số ngày sử dụng ước tính còn lại:
 * Số ngày = Lượng cám tồn / Lượng tiêu thụ/ngày
 */
@Composable
private fun FeedStockForecastSection(
    analytics: DailyFeedAnalytics,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory,
                    contentDescription = null,
                    tint = FarmGreenPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "DỰ BÁO CÁM TỒN & NGÀY SỬ DỤNG",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = FarmTextPrimary
                )
            }

            Surface(
                color = if (analytics.estimatedDaysRemaining <= analytics.reorderThresholdDays) FarmRedContainer else FarmGreenContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (analytics.estimatedDaysRemaining <= analytics.reorderThresholdDays) "Cần đặt hàng" else "Kho an toàn",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (analytics.estimatedDaysRemaining <= analytics.reorderThresholdDays) FarmRed else FarmGreenPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        // Thẻ hiển thị song song Tồn kho & Tốc độ tiêu thụ
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Tồn kho hiện tại
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = FarmBlueContainer.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "TỒN SILO / KHO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = FarmBlue
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.1f", analytics.totalStockKg / 1000.0)} Tấn",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = FarmTextPrimary
                    )
                    Text(
                        text = "≈ ${analytics.totalStockBags} bao (25kg)",
                        fontSize = 11.sp,
                        color = FarmTextSecondary
                    )
                }
            }

            // Tốc độ tiêu thụ
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = FarmAmberContainer.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "ĂN TRUNG BÌNH/NGÀY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = FarmAmber
                    )
                    Text(
                        text = "${String.format(Locale.US, "%,.0f", analytics.dailyConsumptionKg)} kg",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = FarmTextPrimary
                    )
                    Text(
                        text = "≈ ${(analytics.dailyConsumptionKg / 25.0).toInt()} bao / ngày",
                        fontSize = 11.sp,
                        color = FarmTextSecondary
                    )
                }
            }
        }

        // Phân loại mức độ cảnh báo tồn kho toàn trại
        val isOverallCritical = analytics.estimatedDaysRemaining < 3
        val isOverallWarning = analytics.estimatedDaysRemaining < 7

        val (overallBg, overallFg, overallTag) = when {
            isOverallCritical -> Triple(FarmRedContainer, FarmRed, "Khẩn cấp nhập kho")
            isOverallWarning -> Triple(FarmAmberContainer, FarmAmber, "Cần đặt hàng")
            else -> Triple(FarmGreenContainer, FarmGreenPrimary, "Kho an toàn")
        }

        // Hero Card: Số ngày sử dụng còn lại theo công thức: Số ngày = Lượng tồn / Tiêu thụ
        Surface(
            color = overallBg,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "ƯỚC TÍNH SỬ DỤNG CÒN LẠI",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = overallFg
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.White
                        ) {
                            Text(
                                text = overallTag,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = overallFg,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Công thức: ${String.format(Locale.US, "%,.0f", analytics.totalStockKg)}kg ÷ ${String.format(Locale.US, "%,.0f", analytics.dailyConsumptionKg)}kg/ngày",
                        fontSize = 11.sp,
                        color = FarmTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = when {
                            isOverallCritical -> "🚨 Cảnh báo khẩn cấp (< 3 ngày)! Làm ngay phiếu Nhập kho trong tab Kho."
                            isOverallWarning -> "⚠️ Đã chạm ngưỡng đệm an toàn (< 7 ngày)! Chủ động đặt hàng ngay hôm nay."
                            else -> "Chủ động đặt hàng trước khi hết (Dự kiến đặt sau ${analytics.estimatedDaysRemaining - analytics.reorderThresholdDays} ngày)."
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = FarmTextPrimary
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = "${analytics.estimatedDaysRemaining}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = overallFg
                    )
                    Text(
                        text = "NGÀY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = overallFg
                    )
                }
            }
        }

        // Chi tiết ngày dùng cho từng loại cám
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            analytics.feedLineForecasts.forEach { forecast ->
                val isItemCritical = forecast.daysRemaining < 3
                val isItemWarning = forecast.daysRemaining < 7
                val itemColor = when {
                    isItemCritical -> FarmRed
                    isItemWarning -> FarmAmber
                    else -> FarmGreenPrimary
                }
                val itemContainer = when {
                    isItemCritical -> FarmRedContainer
                    isItemWarning -> FarmAmberContainer
                    else -> FarmBackground
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(itemColor, CircleShape)
                        )
                        Text(
                            text = forecast.feedName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FarmTextPrimary
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tồn ${String.format(Locale.US, "%.1f", forecast.stockKg / 1000.0)}T (${forecast.stockBags} bao)",
                            fontSize = 11.sp,
                            color = FarmTextSecondary
                        )
                        Surface(
                            color = itemContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Còn ${forecast.daysRemaining} ngày",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = itemColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun formatMillionVnd(amount: Double): String {
    val tr = amount / 1_000_000.0
    return if (tr >= 1.0) "${String.format(Locale.US, "%.0f", tr)} TR" else formatVnd(amount)
}

fun formatShortVnd(amount: Double): String {
    val billion = amount / 1_000_000_000.0
    val million = amount / 1_000_000.0
    return when {
        billion >= 1.0 -> "${String.format(Locale.US, "%.3f", billion).trimEnd('0').trimEnd('.')} Tỷ"
        million >= 1.0 -> "${String.format(Locale.US, "%.1f", million).trimEnd('0').trimEnd('.')} Tr"
        else -> formatVnd(amount)
    }
}

@Composable
fun CompactCostCard(
    label: String,
    value: String,
    percent: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = FarmTextSecondary,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    text = percent,
                    fontSize = 10.sp,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = FarmTextPrimary
            )
        }
    }
}

/**
 * Báo Cáo Kết Quả Kinh Doanh & Dòng Tiền (Gộp 3 khối thành 1 báo cáo chuẩn mực):
 * Hàng 1 (Tổng quan 3 chỉ số): Doanh thu (1.205 Tỷ) - Chi phí (846.5 Tr) = Lãi ròng (+358.5 Tr)
 * Hàng 2 (Cơ cấu chi phí): Giữ nguyên thanh tỷ lệ 4 màu và 4 chip chi phí trực quan
 * Hàng 3 (Chi tiết nguồn thu): Rút gọn 2 dòng doanh thu bán heo thịt và giống
 */
@Composable
fun FinancialBalanceSection(
    balance: FinancialBalance,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_section_financial_balance"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, FarmOutline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header tiêu đề & Biên lãi ròng
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(end = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(FarmGreenContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = FarmGreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "BÁO CÁO KẾT QUẢ KINH DOANH",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = FarmTextPrimary,
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Dòng tiền & Lãi ròng đợt nuôi",
                            fontSize = 11.sp,
                            color = FarmTextSecondary,
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    color = FarmGreenContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(
                        text = "Biên lãi: +${String.format(Locale.US, "%.1f", balance.profitMarginPercent)}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = FarmGreenPrimary,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // HÀNG 1: TỔNG QUAN 3 CHỈ SỐ (DOANH THU - CHI PHÍ = LÃI RÒNG)
            Surface(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, FarmOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Doanh thu
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "DOANH THU",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = FarmTextSecondary
                            )
                            Text(
                                text = formatShortVnd(balance.totalRevenue),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = FarmGreenPrimary
                            )
                            Text(
                                text = formatVnd(balance.totalRevenue),
                                fontSize = 10.sp,
                                color = FarmTextSecondary,
                                maxLines = 1
                            )
                        }

                        // Dấu trừ
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0xFFE2E8F0), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "−",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF475569)
                            )
                        }

                        // 2. Chi phí
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "CHI PHÍ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = FarmTextSecondary
                            )
                            Text(
                                text = formatShortVnd(balance.totalCost),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = FarmRed
                            )
                            Text(
                                text = formatVnd(balance.totalCost),
                                fontSize = 10.sp,
                                color = FarmTextSecondary,
                                maxLines = 1
                            )
                        }

                        // Dấu bằng
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0xFFE2E8F0), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "=",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF475569)
                            )
                        }

                        // 3. Lãi ròng
                        Column(
                            modifier = Modifier.weight(1.2f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "LÃI RÒNG",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B5E20)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    color = FarmGreenPrimary,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "DƯƠNG",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "+${formatShortVnd(balance.accumulatedProfit)}",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1B5E20)
                            )
                            Text(
                                text = "+${formatVnd(balance.accumulatedProfit)}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // HÀNG 2: CƠ CẤU 4 NGUỒN CHI PHÍ CHÍNH
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CƠ CẤU 4 KHOẢN MỤC CHI PHÍ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = FarmTextSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = formatVnd(balance.totalCost),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = FarmRed
                    )
                }

                // Thanh tỷ lệ 4 màu trực quan
                CostSegmentedBar(balance = balance)

                // Lưới 4 chip chi phí 2x2
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CompactCostCard(
                            modifier = Modifier.weight(1f),
                            label = "1. Giống/Hậu bị",
                            value = formatMillionVnd(balance.breedingPigCost),
                            percent = "${((balance.breedingPigCost / balance.totalCost) * 100).toInt()}%",
                            color = Color(0xFF1976D2)
                        )
                        CompactCostCard(
                            modifier = Modifier.weight(1f),
                            label = "2. Cám thức ăn",
                            value = formatMillionVnd(balance.feedCost),
                            percent = "${((balance.feedCost / balance.totalCost) * 100).toInt()}%",
                            color = Color(0xFFFB8C00)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CompactCostCard(
                            modifier = Modifier.weight(1f),
                            label = "3. Thuốc/Vaccine",
                            value = formatMillionVnd(balance.medicineVaccineCost),
                            percent = "${((balance.medicineVaccineCost / balance.totalCost) * 100).toInt()}%",
                            color = Color(0xFF8E24AA)
                        )
                        CompactCostCard(
                            modifier = Modifier.weight(1f),
                            label = "4. Vật tư chuồng",
                            value = formatMillionVnd(balance.suppliesCost),
                            percent = "${((balance.suppliesCost / balance.totalCost) * 100).toInt()}%",
                            color = Color(0xFF00897B)
                        )
                    }
                }
            }

            // HÀNG 3: CHI TIẾT NGUỒN DOANH THU XUẤT CHUỒNG (RÚT GỌN 2 DÒNG NHỎ GỌN)
            Surface(
                color = FarmBackground,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CHI TIẾT NGUỒN THU XUẤT BÁN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FarmTextSecondary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = formatVnd(balance.totalRevenue),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = FarmGreenPrimary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "• Bán heo thịt thương phẩm (160 con, 115kg @ 61k)",
                            fontSize = 11.sp,
                            color = FarmTextPrimary
                        )
                        Text(
                            text = formatVnd(balance.meatRevenue),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FarmTextPrimary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "• Bán heo giống / hậu bị / con cai sữa",
                            fontSize = 11.sp,
                            color = FarmTextPrimary
                        )
                        Text(
                            text = formatVnd(balance.breedingRevenue),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FarmTextPrimary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Thanh tiến độ phân rã 4 nguồn chi phí
 */
@Composable
private fun CostSegmentedBar(
    balance: FinancialBalance,
    modifier: Modifier = Modifier
) {
    val total = balance.totalCost.coerceAtLeast(1.0)
    val w1 = (balance.breedingPigCost / total).toFloat()
    val w2 = (balance.feedCost / total).toFloat()
    val w3 = (balance.medicineVaccineCost / total).toFloat()
    val w4 = (balance.suppliesCost / total).toFloat()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
    ) {
        Box(modifier = Modifier.weight(w1).height(10.dp).background(Color(0xFF1976D2)))
        Box(modifier = Modifier.weight(w2).height(10.dp).background(Color(0xFFFB8C00)))
        Box(modifier = Modifier.weight(w3).height(10.dp).background(Color(0xFF8E24AA)))
        Box(modifier = Modifier.weight(w4).height(10.dp).background(Color(0xFF00897B)))
    }
}

/**
 * Hiển thị từng khoản mục chi phí với màu sắc và tỷ trọng %
 */
@Composable
private fun CostDetailRow(
    label: String,
    amount: Double,
    percentage: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = FarmTextPrimary
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatVnd(amount),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = FarmTextPrimary
            )
            Text(
                text = "(${String.format(Locale.US, "%.1f", percentage)}%)",
                fontSize = 11.sp,
                color = FarmTextSecondary
            )
        }
    }
}
