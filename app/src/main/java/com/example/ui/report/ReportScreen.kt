package com.example.ui.report

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FarmBackground
import com.example.ui.theme.FarmBlue
import com.example.ui.theme.FarmBlueContainer
import com.example.ui.theme.FarmGreenContainer
import com.example.ui.theme.FarmGreenPrimary
import com.example.ui.theme.FarmOutline
import com.example.ui.theme.FarmTextPrimary
import com.example.ui.theme.FarmTextSecondary

@Composable
fun ReportScreen(
    viewModel: ReportViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FarmBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "BÁO CÁO",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = FarmTextPrimary,
                    letterSpacing = 0.5.sp
                )
            }

            // Period Filter Bar (PDF Page 12: Tháng này, Quý này, Toàn bộ, Theo lô)
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ReportPeriod.values()) { period ->
                        val isSelected = state.period == period
                        Box(
                            modifier = Modifier
                                .testTag("report_chip_${period.name.lowercase()}")
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) FarmGreenPrimary else Color.White)
                                .border(
                                    1.dp,
                                    if (isSelected) FarmGreenPrimary else FarmOutline,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { viewModel.onPeriodSelected(period) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = period.label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else FarmTextPrimary
                            )
                        }
                    }
                }
            }

            // Section: CHỈ SỐ CHÍNH (PDF Page 12)
            item {
                Text(
                    text = "CHỈ SỐ CHÍNH",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = FarmTextSecondary
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card TĂNG TRƯỞNG (Page 12)
                    Card(
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("report_card_growth"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(FarmBlueContainer, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "TĂNG TRƯỞNG",
                                    color = FarmBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Trọng lượng TB: ${state.averageWeightKg} kg",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = FarmTextPrimary
                            )
                            Text(
                                text = "+${state.adgDailyGainKg} kg/ngày (ADG)\nTăng trưởng ổn định",
                                fontSize = 12.sp,
                                color = FarmGreenPrimary,
                                lineHeight = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Card FCR (Page 12)
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("report_card_fcr"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFEFEFEF), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "FCR",
                                    color = Color(0xFF555555),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "${state.fcrIndex}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = FarmTextPrimary
                            )
                            Text(
                                text = "tiêu thụ cám",
                                fontSize = 12.sp,
                                color = FarmTextSecondary
                            )
                        }
                    }
                }
            }

            // Additional Breeding KPIs Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Tỷ lệ thụ thai", fontSize = 11.sp, color = FarmTextSecondary)
                            Text(
                                "${state.conceptionRate}%",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = FarmGreenPrimary
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Cai sữa/nái/năm", fontSize = 11.sp, color = FarmTextSecondary)
                            Text(
                                "${state.weanedPigletsPerSowPerYear} con",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = FarmTextPrimary
                            )
                        }
                    }
                }
            }

            // Section: BIỂU ĐỒ (PDF Page 12)
            item {
                Text(
                    text = "BIỂU ĐỒ",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = FarmTextSecondary
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("report_chart_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "BIỂU ĐỒ TĂNG TRỌNG (30 ngày qua)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = FarmTextPrimary
                        )

                        GrowthWeightChart(dataPoints = state.weightTrendData)
                    }
                }
            }

            // Section: CHI TIẾT (PDF Page 12)
            item {
                Text(
                    text = "CHI TIẾT LỊCH SỬ CÂN",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = FarmTextSecondary
                )
            }

            items(state.recentWeighingRecords) { record ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = record.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = FarmTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${record.timeLabel} • Đạt ${record.weightStr} (${record.gainStr})",
                                fontSize = 13.sp,
                                color = FarmTextSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(FarmGreenContainer, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = record.evaluation,
                                color = FarmGreenPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}
