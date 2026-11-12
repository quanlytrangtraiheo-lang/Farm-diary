package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.theme.LocalOutdoorMode
import com.example.ui.theme.OutdoorBorder
import com.example.ui.theme.OutdoorCardBorder
import com.example.ui.theme.OutdoorModeManager
import com.example.ui.theme.OutdoorModeToggleButton
import com.example.ui.theme.OutdoorOverallBackground
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToHerd: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onQuickAdd: () -> Unit,
    onNavigateToInventory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var showAlertDetails by remember { mutableStateOf(false) }
    val isOutdoor = LocalOutdoorMode.current
    val listState = rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 30 }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isOutdoor) OutdoorOverallBackground else FarmBackground)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Bar tự động cuộn thu gọn (Collapsible Top Bar)
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = if (isScrolled) 2.dp else 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        if (isScrolled) {
                            Text(
                                text = "TRANG TRẠI",
                                fontSize = OutdoorModeManager.scaledSp(16f, isOutdoor),
                                fontWeight = FontWeight.Black,
                                color = FarmTextPrimary
                            )
                        } else {
                            Text(
                                text = "NHẬT KÝ TRANG TRẠI",
                                fontSize = OutdoorModeManager.scaledSp(22f, isOutdoor),
                                fontWeight = FontWeight.Black,
                                color = FarmTextPrimary,
                                letterSpacing = 0.5.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (state.isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                                    contentDescription = null,
                                    tint = if (state.isOnline) FarmGreenPrimary else FarmAmber,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (state.isOnline) "Trực tuyến • Sẵn sàng" else "Offline-First • Lưu máy",
                                    fontSize = OutdoorModeManager.scaledSp(11f, isOutdoor),
                                    color = FarmTextSecondary
                                )
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutdoorModeToggleButton()

                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier
                                .testTag("home_notification_button")
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(
                                    width = if (isOutdoor) 2.dp else 1.dp,
                                    color = if (isOutdoor) OutdoorCardBorder else FarmOutline,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Thông báo",
                                tint = FarmGreenPrimary
                            )
                        }
                    }
                }
            }

            // Top Stat Cards: Tập trung Tổng đàn, Lượng cám hôm nay, Lãi lũy kế
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Tổng đàn
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_card_total_pigs")
                            .clickable { onNavigateToHerd() },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "TỔNG ĐÀN",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = FarmTextSecondary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = String.format("%,d", state.totalPigs),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = FarmTextPrimary
                                )
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(FarmGreenContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Pets,
                                        contentDescription = null,
                                        tint = FarmGreenPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 2. Lượng cám hôm nay
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_card_feed_daily"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "CÁM HÔM NAY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = FarmTextSecondary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${String.format(Locale.US, "%.2f", state.feedAnalytics.totalDailyFeedKg / 1000.0)}T",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = FarmAmber
                                )
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(FarmAmberContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Restaurant,
                                        contentDescription = null,
                                        tint = FarmAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 3. Lãi lũy kế
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_card_top_profit"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FarmGreenPrimary.copy(alpha = 0.4f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "LÃI LŨY KẾ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = FarmGreenPrimary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "+${String.format(Locale.US, "%.1f", state.financialBalance.accumulatedProfit / 1_000_000.0)}M",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1B5E20)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(FarmGreenPrimary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Cảnh báo Card
            if (state.alertCount > 0) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_card_alerts")
                            .clickable { showAlertDetails = !showAlertDetails },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = FarmRedContainer),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "CẢNH BÁO TRANG TRẠI",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FarmRed
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = FarmRed,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${state.alertCount} cảnh báo cần xử lý",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = FarmRed
                                        )
                                    }
                                }

                                Text(
                                    text = if (showAlertDetails) "Thu gọn ▲" else "Xem chi tiết ▼",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FarmRed
                                )
                            }

                            if (showAlertDetails) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // 1. CẢNH BÁO TỒN KHO CÁM CHẠM NGƯỠNG AN TOÀN (KHẨN CẤP / ĐỎ)
                                    state.criticalFeedAlerts.forEach { feed ->
                                        Surface(
                                            color = Color.White,
                                            shape = RoundedCornerShape(10.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, FarmRed.copy(alpha = 0.5f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
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
                                                        Box(
                                                            modifier = Modifier
                                                                .background(FarmRed, RoundedCornerShape(4.dp))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(
                                                                text = "CÁM KHẨN CẤP",
                                                                color = Color.White,
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                        Text(
                                                            text = feed.feedName,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp,
                                                            color = FarmTextPrimary
                                                        )
                                                    }
                                                    Text(
                                                        text = "Còn ${feed.daysRemaining} ngày ăn",
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 13.sp,
                                                        color = FarmRed
                                                    )
                                                }

                                                Text(
                                                    text = "Tồn kho: ${feed.stockBags} bao (${feed.stockKg.toInt()} kg) • Ngưỡng an toàn: ${feed.safetyDaysThreshold} ngày / Tối thiểu ${feed.minStockThreshold.toInt()} bao",
                                                    fontSize = 11.sp,
                                                    color = FarmTextSecondary
                                                )

                                                // Dòng gợi ý số lượng cần mua thêm + Nút chuyển nhanh sang Kho
                                                Surface(
                                                    color = Color(0xFFFFF3E0),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column {
                                                            Text(
                                                                text = "💡 Gợi ý mua thêm:",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color(0xFFE65100)
                                                            )
                                                            Text(
                                                                text = "${feed.suggestedOrderBags} bao (~${String.format(Locale.US, "%.2f", feed.suggestedOrderKg / 1000.0)} tấn)",
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color = Color(0xFFBF360C)
                                                            )
                                                        }

                                                        Button(
                                                            onClick = onNavigateToInventory,
                                                            colors = ButtonDefaults.buttonColors(
                                                                containerColor = FarmGreenPrimary
                                                            ),
                                                            shape = RoundedCornerShape(8.dp),
                                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                                                horizontal = 10.dp,
                                                                vertical = 4.dp
                                                            ),
                                                            modifier = Modifier.height(32.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.ShoppingCart,
                                                                contentDescription = null,
                                                                tint = Color.White,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(
                                                                text = "Nhập kho ngay",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color.White
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // 2. CẢNH BÁO HEO CẦN XỬ LÝ
                                    state.warningPigs.take(4).forEach { pig ->
                                        Surface(
                                            color = Color.White,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text(
                                                        text = pig.tagCode,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                    Text(
                                                        text = when {
                                                            pig.hasOverdueVaccine -> "Quá hạn tiêm vắc-xin Dịch tả"
                                                            pig.hasUltrasoundWarning -> "Quá ngày siêu âm thai (D+35)"
                                                            pig.hasDelayedHeatWarning -> "Cai sữa quá 7 ngày chưa lên giống"
                                                            else -> "Cần kiểm tra sức khỏe"
                                                        },
                                                        color = FarmRed,
                                                        fontSize = 12.sp
                                                    )
                                                }
                                                Text(
                                                    text = pig.location,
                                                    fontSize = 11.sp,
                                                    color = FarmTextSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 1. PHẦN THỐNG KÊ LƯỢNG CÁM ĂN (DAILY FEED ANALYTICS)
            item {
                DailyFeedAnalyticsSection(analytics = state.feedAnalytics)
            }

            // 2. BẢNG CÂN ĐỐI TÀI CHÍNH & LỢI NHUẬN LŨY KẾ
            item {
                FinancialBalanceSection(balance = state.financialBalance)
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Round Green Action FAB
        FloatingActionButton(
            onClick = onQuickAdd,
            containerColor = FarmGreenPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("home_fab_add")
                .size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Thêm nhanh",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
