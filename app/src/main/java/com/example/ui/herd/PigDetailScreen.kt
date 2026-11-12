package com.example.ui.herd

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.data.local.entity.BreedingCycleEntity
import com.example.data.local.entity.PigEntity
import com.example.data.model.SowReproductiveStatus
import com.example.ui.components.LargeNumpad
import com.example.ui.theme.FarmAmber
import com.example.ui.theme.FarmAmberContainer
import com.example.ui.theme.FarmBackground
import com.example.ui.theme.FarmGreenContainer
import com.example.ui.theme.FarmGreenPrimary
import com.example.ui.theme.FarmOutline
import com.example.ui.theme.FarmRed
import com.example.ui.theme.FarmRedContainer
import com.example.ui.theme.FarmTextPrimary
import com.example.ui.theme.FarmTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PigDetailScreen(
    pig: PigEntity,
    viewModel: HerdViewModel,
    pigViewModel: com.example.ui.pig.PigViewModel? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val cycles by viewModel.getCyclesForSow(pig.id).collectAsState(initial = emptyList())
    val events by viewModel.getEventsForPig(pig.id).collectAsState(initial = emptyList())

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = if (pig.isBatch) {
        listOf("Theo dõi lô", "Sức khỏe", "Cân nặng", "Công việc")
    } else {
        listOf("Sinh sản", "Sức khỏe", "Cân nặng", "Công việc")
    }

    var showFarrowingSheet by remember { mutableStateOf(false) }
    var showMatingDialog by remember { mutableStateOf(false) }
    var showUltrasoundDialog by remember { mutableStateOf(false) }

    // Fast event states
    var showFastEventMenu by remember { mutableStateOf(false) }
    var activeFastEventDialog by remember { mutableStateOf<com.example.ui.pig.FastEventType?>(null) }

    // Calculations
    val now = System.currentTimeMillis()
    val matingDate = pig.lastMatingDate ?: (now - (85 * 86_400_000L))
    val daysInGestation = ((now - matingDate) / 86_400_000L).coerceAtLeast(0).toInt()
    val totalGestationDays = 114
    val daysRemaining = (totalGestationDays - daysInGestation).coerceAtLeast(0)
    val progress = (daysInGestation.toFloat() / totalGestationDays.toFloat()).coerceIn(0f, 1f)

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val birthDateString = remember(pig.birthDate) {
        dateFormat.format(Date(pig.birthDate))
    }
    val ageDays = remember(pig.birthDate, now) {
        ((now - pig.birthDate) / (86_400_000L)).coerceAtLeast(0)
    }
    val ageWeeksFloat = remember(ageDays) {
        ageDays / 7.0
    }
    val ageWeeksRounded = remember(ageWeeksFloat) {
        String.format(Locale.US, "%.1f", ageWeeksFloat)
    }
    val ageWeeksFloor = remember(ageDays) {
        (ageDays / 7).toInt()
    }

    val batchStatus = remember(pig.currentWeightKg, pig.hasOverdueVaccine) {
        when {
            pig.hasOverdueVaccine -> "Đang điều trị"
            pig.currentWeightKg >= 100f -> "Chờ xuất"
            else -> "Bình thường"
        }
    }

    val sucklingPigletsCount = remember(pig.status, pig.quantity) {
        if (pig.status == SowReproductiveStatus.NUOI_CON) {
            if (pig.quantity > 0) pig.quantity else 11
        } else {
            0
        }
    }

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
            // Header Bar (PDF Page 18)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .testTag("pig_detail_back_button")
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, FarmOutline, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Quay lại",
                                tint = FarmGreenPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (pig.isBatch) "CHI TIẾT LÔ HEO THỊT" else "CHI TIẾT HEO NÁI",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = FarmTextPrimary
                        )
                    }

                    // Nút Nhập sự kiện nhanh trên thanh Header
                    Button(
                        onClick = { showFastEventMenu = true },
                        colors = ButtonDefaults.buttonColors(containerColor = FarmGreenPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_detail_quick_event_header")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Nhập sự kiện",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Fixed Header Card (Chuẩn hóa theo yêu cầu người dùng)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pig_detail_header_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    if (pig.isBatch) {
                        // ==============================================================
                        // 1. CẤU TRÚC THẺ CHI TIẾT LÔ HEO THỊT
                        // Các trường loại bỏ hoàn toàn:
                        // Siêu âm & khám thai, Thể trạng BCS, Trạng thái sinh sản, Lứa đẻ,
                        // Giống heo, Giới tính.
                        // Trường độ tuổi: Tính theo Tuần tuổi.
                        // ==============================================================
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(
                                        text = "Mã Lô: ${pig.tagCode}",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = FarmTextPrimary,
                                        modifier = Modifier.testTag("grower_batch_code")
                                    )
                                    Text(
                                        text = "Chuồng hiện tại: ${pig.location}",
                                        fontSize = 14.sp,
                                        color = FarmTextSecondary,
                                        modifier = Modifier.testTag("grower_batch_location")
                                    )
                                }

                                // Tình trạng / Trạng thái badge
                                Box(
                                    modifier = Modifier
                                        .background(
                                            when (batchStatus) {
                                                "Đang điều trị" -> FarmRedContainer
                                                "Chờ xuất" -> FarmAmberContainer
                                                else -> FarmGreenContainer
                                            },
                                            RoundedCornerShape(10.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = batchStatus,
                                        color = when (batchStatus) {
                                            "Đang điều trị" -> FarmRed
                                            "Chờ xuất" -> FarmAmber
                                            else -> FarmGreenPrimary
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.testTag("grower_batch_status")
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Số lượng tồn: ${pig.quantity} con",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = FarmTextPrimary,
                                    modifier = Modifier.testTag("grower_batch_quantity")
                                )
                                Text(
                                    text = "Độ tuổi: $ageWeeksRounded tuần tuổi",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FarmGreenPrimary,
                                    modifier = Modifier.testTag("grower_batch_age_weeks")
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Khối lượng TB: ${if (pig.currentWeightKg > 0f) "${pig.currentWeightKg} kg" else "25.0 kg"}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FarmGreenPrimary,
                                    modifier = Modifier.testTag("grower_batch_avg_weight")
                                )
                                Text(
                                    text = "Ngày nhập đàn: $birthDateString",
                                    fontSize = 13.sp,
                                    color = FarmTextSecondary,
                                    modifier = Modifier.testTag("grower_batch_intake_date")
                                )
                            }
                        }
                    } else {
                        // ==============================================================
                        // 2. CẤU TRÚC THẺ CHI TIẾT HEO NÁI
                        // Loại bỏ / Thay thế:
                        // Bỏ trường Trọng lượng hiện tại.
                        // Thay bằng trường: Số heo con theo mẹ (Ràng buộc: = N con nếu Nuôi con, = 0 nếu khác).
                        // Cấu trúc: Mã tai, Giống, Lứa đẻ, Vị trí chuồng, Trạng thái sinh sản,
                        // Số heo con theo mẹ, Ngày dự sinh / Ngày phối / Ngày cai sữa.
                        // ==============================================================
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(
                                        text = "Mã tai nái: ${pig.tagCode}",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = FarmTextPrimary,
                                        modifier = Modifier.testTag("sow_tag_code")
                                    )
                                    Text(
                                        text = "Giống: ${pig.breed}",
                                        fontSize = 14.sp,
                                        color = FarmTextSecondary,
                                        modifier = Modifier.testTag("sow_breed")
                                    )
                                }

                                // Trạng thái sinh sản Badge
                                Box(
                                    modifier = Modifier
                                        .background(
                                            when (pig.status) {
                                                SowReproductiveStatus.MANG_THAI -> FarmAmberContainer
                                                SowReproductiveStatus.NUOI_CON -> FarmGreenContainer
                                                SowReproductiveStatus.LOAI_THAI -> FarmRedContainer
                                                else -> FarmBackground
                                            },
                                            RoundedCornerShape(10.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = when (pig.status) {
                                            SowReproductiveStatus.MANG_THAI -> "MANG THAI (Ngày $daysInGestation)"
                                            SowReproductiveStatus.NUOI_CON -> "NUÔI CON"
                                            SowReproductiveStatus.CAI_SUA_CHO_PHOI -> "CAI SỮA CHỜ PHỐI"
                                            SowReproductiveStatus.HAU_BI -> "HẬU BỊ"
                                            SowReproductiveStatus.LOAI_THAI -> "LOẠI THẢI"
                                            else -> "ĐANG NUÔI"
                                        },
                                        color = when (pig.status) {
                                            SowReproductiveStatus.MANG_THAI -> FarmAmber
                                            SowReproductiveStatus.NUOI_CON -> FarmGreenPrimary
                                            SowReproductiveStatus.LOAI_THAI -> FarmRed
                                            else -> FarmTextPrimary
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.testTag("sow_status_badge")
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Lứa đẻ: Lứa ${pig.parity}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = FarmTextPrimary,
                                    modifier = Modifier.testTag("sow_parity")
                                )
                                Text(
                                    text = "Vị trí chuồng: ${pig.location}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = FarmTextPrimary,
                                    modifier = Modifier.testTag("sow_location")
                                )
                            }

                            // SỐ HEO CON THEO MẸ (Thay thế trường Trọng lượng)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Số heo con theo mẹ: ${sucklingPigletsCount} con",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (pig.status == SowReproductiveStatus.NUOI_CON) FarmGreenPrimary else FarmTextSecondary,
                                    modifier = Modifier.testTag("sow_suckling_piglets_count")
                                )
                                Text(
                                    text = when (pig.status) {
                                        SowReproductiveStatus.MANG_THAI -> "Dự sinh: còn $daysRemaining ngày"
                                        SowReproductiveStatus.NUOI_CON -> "Dự cai sữa: 24 ngày tuổi"
                                        SowReproductiveStatus.CAI_SUA_CHO_PHOI -> "Chờ phối giống"
                                        SowReproductiveStatus.HAU_BI -> "Chờ phối lứa 1"
                                        SowReproductiveStatus.LOAI_THAI -> "Đã loại thải"
                                        else -> "Đang theo dõi"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = FarmGreenPrimary,
                                    modifier = Modifier.testTag("sow_milestone_date")
                                )
                            }

                            // Gestation Progress Bar (Chỉ hiển thị cho nái đang mang thai)
                            if (pig.status == SowReproductiveStatus.MANG_THAI) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Tiến độ mang thai: Ngày $daysInGestation / $totalGestationDays",
                                            fontSize = 12.sp,
                                            color = FarmTextSecondary
                                        )
                                        Text(
                                            text = "Còn $daysRemaining ngày",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = FarmGreenPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = FarmGreenPrimary,
                                        trackColor = Color(0xFFE8EFE8)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Tabs Row
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    divider = {},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Tab(
                            selected = isSelected,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) FarmGreenPrimary else FarmTextSecondary
                                )
                            }
                        )
                    }
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    if (pig.isBatch) {
                        // TAB 1 (LÔ HEO THỊT): THEO DÕI LÔ
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "NHẬT KÝ & THEO DÕI LÔ HEO THỊT",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FarmTextPrimary
                                    )

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(FarmBackground, RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Mã Lô:", fontSize = 13.sp, color = FarmTextSecondary)
                                                Text(pig.tagCode, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Số lượng hiện diện:", fontSize = 13.sp, color = FarmTextSecondary)
                                                Text("${pig.quantity} con", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Độ tuổi tuần:", fontSize = 13.sp, color = FarmTextSecondary)
                                                Text("$ageWeeksRounded tuần tuổi", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FarmGreenPrimary)
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Tăng trọng dự báo (ADG):", fontSize = 13.sp, color = FarmTextSecondary)
                                                Text("~785 g/con/ngày", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Dự kiến xuất bán:", fontSize = 13.sp, color = FarmTextSecondary)
                                                Text("Khi đạt 105 - 110 kg", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FarmAmber)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Action Buttons cho Lô heo thịt
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                activeFastEventDialog = com.example.ui.pig.FastEventType.GROWER_WEIGH
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = FarmGreenPrimary),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Cân lô", fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                activeFastEventDialog = com.example.ui.pig.FastEventType.GROWER_TREATMENT
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Điều trị", fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                pigViewModel?.recordRelocation(pig.tagCode, System.currentTimeMillis(), "Chuồng Thịt 02", "Chuyển ô vỗ béo")
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Chuyển chuồng", fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                activeFastEventDialog = com.example.ui.pig.FastEventType.GROWER_SALE
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = FarmAmber),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Xuất bán", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // TAB 1 (HEO NÁI): SINH SẢN (Timeline & Reproductive details)
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "LỊCH SỬ SINH SẢN",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FarmTextPrimary
                                    )

                                    val activeCycle = cycles.find { it.cycleStatus == "IN_PROGRESS" }
                                        ?: cycles.firstOrNull()

                                    if (activeCycle != null) {
                                        TimelineStepItem(
                                            title = "Phối giống: ${activeCycle.boarCode}",
                                            subtitle = dateFormat.format(Date(activeCycle.matingDate)),
                                            isCompleted = true,
                                            isLast = false
                                        )

                                        TimelineStepItem(
                                            title = "Siêu âm thai: ${activeCycle.ultrasoundResult ?: "Đạt (+)"}",
                                            subtitle = "Xác nhận thai kỳ ổn định",
                                            isCompleted = activeCycle.ultrasoundResult != null || activeCycle.parity >= 1,
                                            isLast = false
                                        )

                                        TimelineStepItem(
                                            title = "Dự kiến đẻ: ${dateFormat.format(Date(activeCycle.expectedFarrowingDate))}",
                                            subtitle = "Còn $daysRemaining ngày",
                                            isCompleted = activeCycle.actualFarrowingDate != null,
                                            isPending = activeCycle.actualFarrowingDate == null,
                                            isLast = true
                                        )
                                    }

                                    val previousCycles = cycles.filter { it.cycleStatus == "COMPLETED" }
                                    previousCycles.forEach { prev ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(FarmBackground, RoundedCornerShape(12.dp))
                                                .padding(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Pets,
                                                        contentDescription = null,
                                                        tint = FarmGreenPrimary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text(
                                                            text = "Lứa ${prev.parity}: Đã đẻ ${prev.bornAlive} con",
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = FarmTextPrimary
                                                        )
                                                        Text(
                                                            text = "Cai sữa: ${prev.weaningDate?.let { dateFormat.format(Date(it)) } ?: "15/07/2026"}",
                                                            fontSize = 12.sp,
                                                            color = FarmTextSecondary
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { showFarrowingSheet = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = FarmGreenPrimary),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("pig_detail_btn_farrowing")
                                        ) {
                                            Text("Ghi nhận đẻ", fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = { showMatingDialog = true },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("pig_detail_btn_mating")
                                        ) {
                                            Text("Kỳ phối mới", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // TAB 2: SỨC KHỎE
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = if (pig.isBatch) "PHÒNG DỊCH & SỨC KHỎE LÔ" else "HỒ SƠ SỨC KHỎE & THÚ Y NÁI",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FarmTextPrimary
                                )
                                Text(
                                    text = if (pig.isBatch) {
                                        "• Vắc-xin Dịch tả lợn cổ điển: Đã tiêm phòng đầy đủ\n• Vắc-xin Tai xanh (PRRS) & LMLM: Đúng lịch trình\n• Tình trạng đàn: Khỏe mạnh, ăn khỏe, tăng trọng đều"
                                    } else {
                                        "• Vắc-xin Dịch tả: Đã tiêm nhắc lại ngày 12/06/2026\n• Vắc-xin Tai xanh (PRRS): Đã tiêm phòng định kỳ\n• Thể trạng BCS: 3.5 / 5 (Đạt chuẩn nái sinh sản)"
                                    },
                                    fontSize = 14.sp,
                                    color = FarmTextSecondary,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }
                2 -> {
                    // TAB 3: CÂN NẶNG / THỂ TRỌNG
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (pig.isBatch) "THEO DÕI THỂ TRỌNG LÔ HEO THỊT" else "THỂ TRẠNG NÁI & CON THEO MẸ",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FarmTextPrimary
                                )
                                if (pig.isBatch) {
                                    Text(
                                        text = "Khối lượng trung bình: ${if (pig.currentWeightKg > 0f) "${pig.currentWeightKg} kg" else "25.0 kg"}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = FarmGreenPrimary
                                    )
                                    Text(
                                        text = "Lịch sử cân: Nhập đàn 22 kg -> Cân gần nhất ${if (pig.currentWeightKg > 0f) "${pig.currentWeightKg} kg" else "25.0 kg"}",
                                        fontSize = 13.sp,
                                        color = FarmTextSecondary
                                    )
                                } else {
                                    Text(
                                        text = "Số heo con theo mẹ: ${sucklingPigletsCount} con",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (pig.status == SowReproductiveStatus.NUOI_CON) FarmGreenPrimary else FarmTextSecondary
                                    )
                                    Text(
                                        text = "Thể trạng BCS: 3.5 / 5 (Chuẩn) • Mỡ lưng P2: 17.5 mm",
                                        fontSize = 13.sp,
                                        color = FarmTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // TAB 4: CÔNG VIỆC
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "CÔNG VIỆC LIÊN QUAN",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FarmTextPrimary
                                )
                                Text(
                                    text = if (pig.isBatch) {
                                        "• Tiêm vắc-xin nhắc lại đợt 2 cho lô thịt\n• Cân mẫu 20% cá thể định kỳ 15 ngày\n• Chuẩn bị lịch xuất bán khi lô đạt 110kg"
                                    } else {
                                        "• Siêu âm thai xác nhận đậu (Đã xong)\n• Chuyển chuồng đẻ & Vệ sinh nái (D+107)\n• Giám sát sinh con / Trực đẻ (D+114)"
                                    },
                                    fontSize = 14.sp,
                                    color = FarmTextSecondary,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // Bottom Sheet: Ghi nhận đẻ with LargeNumpad (PDF Page 17)
        if (showFarrowingSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showFarrowingSheet = false },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                FarrowingRecordSheetContent(
                    sowTag = pig.tagCode,
                    onSave = { total, alive, still, mum, weight ->
                        val activeCycle = cycles.firstOrNull { it.cycleStatus == "IN_PROGRESS" }
                        val cycleId = activeCycle?.cycleId ?: "cycle-${pig.id}"
                        viewModel.recordFarrowing(
                            sowId = pig.id,
                            cycleId = cycleId,
                            actualDate = System.currentTimeMillis(),
                            totalBorn = total,
                            bornAlive = alive,
                            stillborn = still,
                            mummies = mum,
                            averageBirthWeightKg = weight
                        )
                        showFarrowingSheet = false
                    },
                    onClose = { showFarrowingSheet = false }
                )
            }
        }

        // Dialog: Thêm kỳ phối giống mới (PDF Page 15-16)
        if (showMatingDialog) {
            AddMatingDialog(
                sow = pig,
                onDismiss = { showMatingDialog = false },
                onConfirm = { parity, boarCode, matingDateMs ->
                    viewModel.addMatingCycle(
                        sowId = pig.id,
                        parity = parity,
                        matingDate = matingDateMs,
                        boarCode = boarCode
                    )
                    showMatingDialog = false
                }
            )
        }

        // Fast Event Menu in Detail Screen (Heo Nái / Lô Heo Thịt)
        if (showFastEventMenu) {
            com.example.ui.pig.FastEventActionMenuBottomSheet(
                onDismiss = { showFastEventMenu = false },
                initialCategoryIndex = if (pig.isBatch) 2 else 0,
                onSelectEvent = { selectedType ->
                    showFastEventMenu = false
                    when (selectedType) {
                        com.example.ui.pig.FastEventType.PIGLET_VACCINE -> {
                            pigViewModel?.recordPigletVaccine(pig.tagCode, System.currentTimeMillis(), pig.quantity, "Mycoplasma", "2ml/con")
                        }
                        com.example.ui.pig.FastEventType.PIGLET_TAIL_DOCK -> {
                            pigViewModel?.recordTailDocking(pig.tagCode, System.currentTimeMillis(), pig.quantity, "Cắt đuôi bấm nanh")
                        }
                        com.example.ui.pig.FastEventType.GROWER_RELOCATE -> {
                            pigViewModel?.recordRelocation(pig.tagCode, System.currentTimeMillis(), "Khu Vỗ Béo B-02", "Chuyển chuồng vỗ béo")
                        }
                        else -> {
                            activeFastEventDialog = selectedType
                        }
                    }
                }
            )
        }

        // Form Đẻ
        if (activeFastEventDialog == com.example.ui.pig.FastEventType.SOW_FARROWING) {
            com.example.ui.pig.FarrowingEventDialog(
                availablePigs = listOf(pig),
                onDismiss = { activeFastEventDialog = null },
                onSave = { sowId, farrowDate, total, alive, still, mummies, weight, notes ->
                    pigViewModel?.recordFarrowing(
                        sowId = sowId,
                        farrowingDate = farrowDate,
                        totalBorn = total,
                        bornAlive = alive,
                        stillborn = still,
                        mummies = mummies,
                        litterWeightKg = weight,
                        notes = notes,
                        onSuccess = { activeFastEventDialog = null }
                    ) ?: run { activeFastEventDialog = null }
                }
            )
        }

        // Form Tiêm sắt
        if (activeFastEventDialog == com.example.ui.pig.FastEventType.PIGLET_IRON) {
            com.example.ui.pig.IronInjectionDialog(
                availableBatches = listOf(pig),
                onDismiss = { activeFastEventDialog = null },
                onSave = { tag, date, qty, drug, dose ->
                    pigViewModel?.recordIronInjection(
                        pigletTag = tag,
                        injectionDate = date,
                        quantity = qty,
                        drugName = drug,
                        dosageMl = dose,
                        onSuccess = { activeFastEventDialog = null }
                    ) ?: run { activeFastEventDialog = null }
                }
            )
        }

        // Form Cai sữa
        if (activeFastEventDialog in listOf(com.example.ui.pig.FastEventType.SOW_WEANING, com.example.ui.pig.FastEventType.PIGLET_WEANING)) {
            com.example.ui.pig.WeaningEventDialog(
                availableBatches = listOf(pig),
                onDismiss = { activeFastEventDialog = null },
                onSave = { tag, date, weanedQty, avgWeight, sowTag ->
                    pigViewModel?.recordWeaning(
                        pigletTag = tag,
                        weaningDate = date,
                        weanedQuantity = weanedQty,
                        averageWeightKg = avgWeight,
                        sowTag = sowTag,
                        onSuccess = { activeFastEventDialog = null }
                    ) ?: run { activeFastEventDialog = null }
                }
            )
        }

        // Form Heo thịt (Nhập đàn, Cân, Điều trị, Xuất bán)
        if (activeFastEventDialog in listOf(
                com.example.ui.pig.FastEventType.GROWER_INTAKE,
                com.example.ui.pig.FastEventType.GROWER_WEIGH,
                com.example.ui.pig.FastEventType.GROWER_TREATMENT,
                com.example.ui.pig.FastEventType.GROWER_SALE
            )
        ) {
            val sub = activeFastEventDialog ?: com.example.ui.pig.FastEventType.GROWER_INTAKE
            com.example.ui.pig.GrowerEventDialog(
                initialSubEvent = sub,
                availableBatches = listOf(pig),
                onDismiss = { activeFastEventDialog = null },
                onIntake = { code, date, qty, weight, loc ->
                    pigViewModel?.recordBatchIntake(code, date, qty, weight, loc) { activeFastEventDialog = null }
                },
                onWeigh = { code, date, count, weight ->
                    pigViewModel?.recordWeighing(code, date, count, weight) { _, _, _ -> activeFastEventDialog = null }
                },
                onTreatment = { code, date, sick, disease, drug ->
                    pigViewModel?.recordTreatment(code, date, sick, disease, drug) { activeFastEventDialog = null }
                },
                onSale = { code, date, qty, weight, buyer ->
                    pigViewModel?.recordBatchSale(code, date, qty, weight, buyer) { activeFastEventDialog = null }
                }
            )
        }

        // Form Loại thải heo nái
        if (activeFastEventDialog == com.example.ui.pig.FastEventType.SOW_CULLING) {
            com.example.ui.pig.SowCullingDialog(
                availablePigs = listOf(pig),
                onDismiss = { activeFastEventDialog = null },
                onSave = { tag, date, reason, weight, price, buyer ->
                    pigViewModel?.recordSowCull(tag, date, reason, weight, price, buyer) {
                        activeFastEventDialog = null
                        onBack()
                    } ?: run { activeFastEventDialog = null }
                }
            )
        }

        // Form Loại thải heo con
        if (activeFastEventDialog == com.example.ui.pig.FastEventType.PIGLET_CULLING) {
            com.example.ui.pig.PigletCullingDialog(
                availableBatches = listOf(pig),
                onDismiss = { activeFastEventDialog = null },
                onSave = { tag, date, qty, reason, weight, disposition ->
                    pigViewModel?.recordPigletCull(tag, date, qty, reason, weight, disposition) {
                        activeFastEventDialog = null
                    } ?: run { activeFastEventDialog = null }
                }
            )
        }

        // Form Loại thải heo thịt
        if (activeFastEventDialog == com.example.ui.pig.FastEventType.GROWER_CULLING) {
            com.example.ui.pig.GrowerCullingDialog(
                availableBatches = listOf(pig),
                onDismiss = { activeFastEventDialog = null },
                onSave = { batch, date, qty, reason, weight, price, buyer ->
                    pigViewModel?.recordGrowerCull(batch, date, qty, reason, weight, price, buyer) {
                        activeFastEventDialog = null
                    } ?: run { activeFastEventDialog = null }
                }
            )
        }

        // Form Phối giống
        if (activeFastEventDialog == com.example.ui.pig.FastEventType.SOW_MATING) {
            com.example.ui.pig.MatingEventDialog(
                availablePigs = listOf(pig),
                onDismiss = { activeFastEventDialog = null },
                onSave = { sowTag, date, boar, tech, notes ->
                    pigViewModel?.recordMating(sowTag, date, boar, tech, notes) {
                        activeFastEventDialog = null
                    } ?: run { activeFastEventDialog = null }
                }
            )
        }

        // Form Khám thai
        if (activeFastEventDialog == com.example.ui.pig.FastEventType.SOW_PREGNANCY_CHECK) {
            com.example.ui.pig.PregnancyCheckDialog(
                availablePigs = listOf(pig),
                onDismiss = { activeFastEventDialog = null },
                onSave = { sowTag, date, isPregnant ->
                    pigViewModel?.recordPregnancyCheck(sowTag, date, isPregnant) {
                        activeFastEventDialog = null
                    } ?: run { activeFastEventDialog = null }
                }
            )
        }

        // Chuyển chuồng nái
        if (activeFastEventDialog == com.example.ui.pig.FastEventType.SOW_RELOCATE) {
            com.example.ui.pig.SowRelocationDialog(
                availablePigs = listOf(pig),
                onDismiss = { activeFastEventDialog = null },
                onSave = { sowTag, moveDate, fromLocation, toLocation, notes ->
                    pigViewModel?.recordSowRelocation(sowTag, moveDate, fromLocation, toLocation, notes) {
                        activeFastEventDialog = null
                    } ?: run { activeFastEventDialog = null }
                }
            )
        }

        // Chuyển chuồng thịt
        if (activeFastEventDialog == com.example.ui.pig.FastEventType.GROWER_RELOCATE) {
            com.example.ui.pig.GrowerRelocationDialog(
                availableBatches = listOf(pig),
                onDismiss = { activeFastEventDialog = null },
                onSave = { fromPen, toPen, moveDate, qty, totalWeightKg, avgAgeDays, batchCode, notes ->
                    pigViewModel?.recordGrowerRelocation(fromPen, toPen, moveDate, qty, totalWeightKg, avgAgeDays, batchCode, notes) {
                        activeFastEventDialog = null
                    } ?: run { activeFastEventDialog = null }
                }
            )
        }

        // Can thiệp heo con
        if (activeFastEventDialog == com.example.ui.pig.FastEventType.PIGLET_CARE) {
            com.example.ui.pig.PigletCareEventDialog(
                availableBatches = listOf(pig),
                onDismiss = { activeFastEventDialog = null },
                onSave = { batchTag, date, pType, vName, tech, dosage, notes ->
                    pigViewModel?.recordPigletCare(batchTag, date, pType, vName, tech, dosage, notes) {
                        activeFastEventDialog = null
                    } ?: run { activeFastEventDialog = null }
                }
            )
        }

        // Nhập nái mới
        if (activeFastEventDialog == com.example.ui.pig.FastEventType.SOW_INTAKE) {
            com.example.ui.pig.NewSowIntakeDialog(
                availablePigs = listOf(pig),
                onDismiss = { activeFastEventDialog = null },
                onSave = { tagCode, breed, birthDate, parity, location, notes ->
                    pigViewModel?.recordNewSowIntake(tagCode, breed, birthDate, parity, location, notes) {
                        activeFastEventDialog = null
                    } ?: run { activeFastEventDialog = null }
                }
            )
        }
    }
}

private fun Float.ifZero(default: Float): Float = if (this <= 0f) default else this

@Composable
fun TimelineStepItem(
    title: String,
    subtitle: String,
    isCompleted: Boolean,
    isPending: Boolean = false,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCompleted) FarmGreenPrimary
                        else if (isPending) FarmAmber
                        else Color(0xFFD0D7D0)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.HourglassTop,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(38.dp)
                        .background(if (isCompleted) FarmGreenPrimary else Color(0xFFD0D7D0))
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 16.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = FarmTextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = FarmTextSecondary
            )
        }
    }
}

/**
 * Bottom Sheet for Farrowing Record with Industrial Large Numpad (PDF Page 17)
 */
@Composable
fun FarrowingRecordSheetContent(
    sowTag: String,
    onSave: (total: Int, alive: Int, still: Int, mum: Int, avgWeight: Float) -> Unit,
    onClose: () -> Unit
) {
    var activeField by remember { mutableStateOf(0) } // 0: total, 1: alive, 2: still, 3: mum, 4: avg weight
    var totalText by remember { mutableStateOf("12") }
    var aliveText by remember { mutableStateOf("11") }
    var stillbornText by remember { mutableStateOf("1") }
    var mummiesText by remember { mutableStateOf("0") }
    var weightText by remember { mutableStateOf("1.45") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "GHI NHẬN ĐẺ - $sowTag",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = FarmTextPrimary
            )
            Text(
                text = "Đóng",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = FarmGreenPrimary,
                modifier = Modifier.clickable { onClose() }
            )
        }

        Text(
            text = "Nhập nhanh chỉ trong 10-15 giây ngay tại chuồng đẻ:",
            fontSize = 13.sp,
            color = FarmTextSecondary
        )

        // Fields Selector Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val fieldLabels = listOf(
                Pair("Tổng con", totalText),
                Pair("Con sống", aliveText),
                Pair("Chết lưu", stillbornText),
                Pair("Con gỗ", mummiesText),
                Pair("TL (kg)", weightText)
            )

            fieldLabels.forEachIndexed { index, pair ->
                val isSelected = activeField == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) FarmGreenContainer else Color(0xFFF2F4F2))
                        .border(
                            1.dp,
                            if (isSelected) FarmGreenPrimary else FarmOutline,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { activeField = index }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = pair.first,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = FarmTextSecondary
                        )
                        Text(
                            text = pair.second.ifBlank { "0" },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) FarmGreenPrimary else FarmTextPrimary
                        )
                    }
                }
            }
        }

        // Industrial Numpad
        LargeNumpad(
            onDigitClick = { digit ->
                when (activeField) {
                    0 -> totalText = if (totalText == "0") digit else totalText + digit
                    1 -> aliveText = if (aliveText == "0") digit else aliveText + digit
                    2 -> stillbornText = if (stillbornText == "0") digit else stillbornText + digit
                    3 -> mummiesText = if (mummiesText == "0") digit else mummiesText + digit
                    4 -> weightText = if (weightText == "0" && digit != ".") digit else weightText + digit
                }
            },
            onBackspaceClick = {
                when (activeField) {
                    0 -> totalText = totalText.dropLast(1)
                    1 -> aliveText = aliveText.dropLast(1)
                    2 -> stillbornText = stillbornText.dropLast(1)
                    3 -> mummiesText = mummiesText.dropLast(1)
                    4 -> weightText = weightText.dropLast(1)
                }
            },
            onClearClick = {
                when (activeField) {
                    0 -> totalText = ""
                    1 -> aliveText = ""
                    2 -> stillbornText = ""
                    3 -> mummiesText = ""
                    4 -> weightText = ""
                }
            }
        )

        Button(
            onClick = {
                val total = totalText.toIntOrNull() ?: 0
                val alive = aliveText.toIntOrNull() ?: 0
                val still = stillbornText.toIntOrNull() ?: 0
                val mum = mummiesText.toIntOrNull() ?: 0
                val weight = weightText.toFloatOrNull() ?: 1.4f
                onSave(total, alive, still, mum, weight)
            },
            colors = ButtonDefaults.buttonColors(containerColor = FarmGreenPrimary),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("farrowing_submit_button")
        ) {
            Text("Lưu kết quả đẻ (Offline)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Dialog to add a new mating cycle (PDF Page 15-16)
 */
@Composable
fun AddMatingDialog(
    sow: PigEntity,
    onDismiss: () -> Unit,
    onConfirm: (parity: Int, boarCode: String, matingDateMs: Long) -> Unit
) {
    var boarCode by remember { mutableStateOf("Đực Duroc D-01") }
    var parity by remember { mutableStateOf((sow.parity + 1).toString()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Thêm kỳ phối giống (Lứa mới)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = FarmTextPrimary
            )
            Text(
                text = "Hệ thống tự động kích hoạt Auto-Task Pipeline chèn 6 lịch nhắc siêu âm, trực đẻ và cai sữa vào máy.",
                fontSize = 13.sp,
                color = FarmTextSecondary
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Lứa thứ mấy:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FarmBackground, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text("Lứa $parity", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Mã đực phối / Lô tinh dịch:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FarmBackground, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(boarCode, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                    Text("Hủy")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val p = parity.toIntOrNull() ?: (sow.parity + 1)
                        onConfirm(p, boarCode, System.currentTimeMillis())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FarmGreenPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Lưu kỳ phối")
                }
            }
        }
    }
}
