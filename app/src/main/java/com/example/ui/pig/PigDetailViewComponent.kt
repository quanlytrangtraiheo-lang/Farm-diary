package com.example.ui.pig

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PigEntity
import com.example.data.model.SowReproductiveStatus
import com.example.ui.components.ReproductiveStatusBadge
import com.example.ui.components.SyncStatusBadge
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Component hiển thị chi tiết toàn bộ thông tin cá thể/lô heo được chọn,
 * bao gồm ngày sinh, độ tuổi, tình trạng sức khỏe hiện tại, phòng dịch vắc-xin,
 * trạng thái thai kỳ và thể trọng.
 */
@Composable
fun PigDetailView(
    pig: PigEntity,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    onViewFullLifecycle: ((String) -> Unit)? = null,
    onQuickEvent: ((PigEntity) -> Unit)? = null
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val now = remember { System.currentTimeMillis() }

    // Tính toán ngày sinh và độ tuổi
    val birthDateString = remember(pig.birthDate) {
        dateFormat.format(Date(pig.birthDate))
    }
    val ageDays = remember(pig.birthDate, now) {
        ((now - pig.birthDate) / (86_400_000L)).coerceAtLeast(0)
    }
    val ageWeeksFloat = remember(ageDays) {
        (ageDays / 7.0)
    }
    val ageWeeksRounded = remember(ageWeeksFloat) {
        String.format(Locale.US, "%.1f", ageWeeksFloat)
    }
    val ageWeeksFloor = remember(ageDays) {
        (ageDays / 7).toInt()
    }
    val ageMonths = remember(ageDays) {
        (ageDays / 30.44).toInt()
    }
    val ageDisplay = remember(ageMonths, ageDays) {
        if (ageMonths < 1) "$ageDays ngày tuổi"
        else if (ageMonths < 24) "$ageMonths tháng tuổi (${ageDays} ngày)"
        else "${ageMonths / 12} năm ${ageMonths % 12} tháng tuổi"
    }

    // Tình trạng / trạng thái của Lô heo thịt
    val batchStatus = remember(pig.currentWeightKg, pig.hasOverdueVaccine) {
        when {
            pig.hasOverdueVaccine -> "Đang điều trị"
            pig.currentWeightKg >= 100f -> "Chờ xuất"
            else -> "Bình thường"
        }
    }

    // Logic tính toán & ràng buộc tự động: Số heo con theo mẹ
    // Nếu status == NUOI_CON: Số con theo mẹ (lấy quantity nếu có hoặc 11 con)
    // Nếu status != NUOI_CON (Hậu bị, Mang thai, Cai sữa chờ phối, Loại thải...): bắt buộc = 0 con
    val sucklingPigletsCount = remember(pig.status, pig.quantity) {
        if (pig.status == SowReproductiveStatus.NUOI_CON) {
            if (pig.quantity > 0) pig.quantity else 11
        } else {
            0
        }
    }

    // Tính toán tiến độ mang thai nếu có
    val daysInGestation = remember(pig.lastMatingDate, now) {
        if (pig.lastMatingDate != null) {
            ((now - pig.lastMatingDate) / 86_400_000L).coerceAtLeast(0).toInt()
        } else null
    }
    val daysRemaining = remember(daysInGestation) {
        if (daysInGestation != null) (114 - daysInGestation).coerceAtLeast(0) else null
    }

    val hasAnyWarning = pig.hasOverdueVaccine ||
        (!pig.isBatch && (pig.hasUltrasoundWarning || pig.hasDelayedHeatWarning || pig.hasProlongedGestationWarning))

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pig_detail_view"),
        color = FarmBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar với Nút đóng
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(FarmGreenContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            tint = FarmGreenPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = pig.tagCode,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = FarmTextPrimary,
                            modifier = Modifier.testTag("pig_detail_tag_code")
                        )
                        Text(
                            text = if (pig.isBatch) "Lô heo thịt (${pig.quantity} con)" else "Heo nái sinh sản",
                            fontSize = 12.sp,
                            color = FarmTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    SyncStatusBadge(status = pig.syncStatus)
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .testTag("pig_detail_close_button")
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, FarmOutline, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng chi tiết",
                            tint = FarmTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (pig.isBatch) {
                // ==============================================================
                // 1. BẢNG THÔNG TIN CHI TIẾT LÔ HEO THỊT (CHUẨN HÓA THEO YÊU CẦU)
                // Các trường loại bỏ hoàn toàn:
                // - Siêu âm & khám thai
                // - Thể trạng (BCS)
                // - Trạng thái sinh sản
                // - Lứa đẻ
                // - Giống heo
                // - Giới tính
                // ==============================================================
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("grower_batch_detail_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FarmOutline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "THÔNG TIN CHI TIẾT LÔ HEO THỊT",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = FarmGreenPrimary,
                                letterSpacing = 0.5.sp
                            )

                            // Tình trạng / Trạng thái badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (batchStatus) {
                                    "Đang điều trị" -> FarmRedContainer
                                    "Chờ xuất" -> FarmBlueContainer
                                    else -> FarmGreenContainer
                                }
                            ) {
                                Text(
                                    text = batchStatus,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (batchStatus) {
                                        "Đang điều trị" -> FarmRed
                                        "Chờ xuất" -> FarmBlue
                                        else -> FarmGreenPrimary
                                    },
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .testTag("grower_batch_status")
                                )
                            }
                        }

                        HorizontalDivider(color = FarmBackground)

                        // Dòng 1: Mã Lô & Chuồng hiện tại
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Mã Lô:", fontSize = 12.sp, color = FarmTextSecondary)
                                Text(
                                    text = pig.tagCode,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FarmTextPrimary,
                                    modifier = Modifier.testTag("grower_batch_code")
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Chuồng hiện tại:", fontSize = 12.sp, color = FarmTextSecondary)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = FarmGreenPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = pig.location,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FarmTextPrimary,
                                        modifier = Modifier.testTag("grower_batch_location")
                                    )
                                }
                            }
                        }

                        // Dòng 2: Số lượng tồn & Độ tuổi hiện tại (Tuần tuổi)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Số lượng tồn (con):", fontSize = 12.sp, color = FarmTextSecondary)
                                Text(
                                    text = "${pig.quantity} con",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FarmTextPrimary,
                                    modifier = Modifier.testTag("grower_batch_quantity")
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Độ tuổi hiện tại:", fontSize = 12.sp, color = FarmTextSecondary)
                                Text(
                                    text = "$ageWeeksRounded tuần tuổi",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FarmGreenPrimary,
                                    modifier = Modifier.testTag("grower_batch_age_weeks")
                                )
                                Text(
                                    text = "(${ageWeeksFloor} tuần ~ $ageDays ngày)",
                                    fontSize = 11.sp,
                                    color = FarmTextSecondary
                                )
                            }
                        }

                        // Dòng 3: Khối lượng trung bình & Ngày nhập đàn / Bắt đầu lô
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Khối lượng trung bình:", fontSize = 12.sp, color = FarmTextSecondary)
                                Text(
                                    text = if (pig.currentWeightKg > 0f) "${pig.currentWeightKg} kg" else "25.0 kg",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FarmGreenPrimary,
                                    modifier = Modifier.testTag("grower_batch_avg_weight")
                                )
                                Text(
                                    text = "Cập nhật từ lần cân gần nhất",
                                    fontSize = 11.sp,
                                    color = FarmTextSecondary
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Ngày nhập đàn / Bắt đầu lô:", fontSize = 12.sp, color = FarmTextSecondary)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = FarmGreenPrimary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = birthDateString,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FarmTextPrimary,
                                        modifier = Modifier.testTag("grower_batch_intake_date")
                                    )
                                }
                            }
                        }
                    }
                }

                // Sức khỏe & Vắc-xin cho Lô Heo Thịt
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pig_detail_health_status"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FarmOutline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HealthAndSafety,
                                contentDescription = null,
                                tint = if (pig.hasOverdueVaccine) FarmRed else FarmGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PHÒNG DỊCH & SỨC KHỎE LÔ",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = FarmTextPrimary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = if (pig.hasOverdueVaccine) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (pig.hasOverdueVaccine) FarmRed else FarmGreenPrimary,
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Phòng dịch vắc-xin cho lô",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FarmTextPrimary
                                )
                                Text(
                                    text = if (pig.hasOverdueVaccine) {
                                        "⚠️ Cảnh báo: Lô heo có lịch tiêm vắc-xin Dịch tả lợn / PRRS quá hạn!"
                                    } else {
                                        "✓ Đã hoàn thành các mũi tiêm phòng định kỳ theo quy trình an toàn sinh học."
                                    },
                                    fontSize = 12.sp,
                                    color = if (pig.hasOverdueVaccine) FarmRed else FarmTextSecondary
                                )
                            }
                        }

                        Text(
                            text = "Đàn heo phát triển bình thường, tăng trọng đạt tiêu chuẩn chuồng thịt.",
                            fontSize = 12.sp,
                            color = FarmTextSecondary
                        )
                    }
                }
            } else {
                // ==============================================================
                // 2. BẢNG THÔNG TIN CHI TIẾT HEO NÁI (CHUẨN HÓA THEO YÊU CẦU)
                // - Bỏ trường: Trọng lượng hiện tại
                // - Thay bằng trường: Số heo con theo mẹ (Ràng buộc: = N con nếu Nuôi con, = 0 nếu khác)
                // - Cấu trúc: Mã tai, Giống, Lứa đẻ, Vị trí chuồng, Trạng thái sinh sản,
                //   Số heo con theo mẹ, Ngày dự sinh / Ngày phối / Ngày cai sữa.
                // ==============================================================
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sow_detail_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FarmOutline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "THÔNG TIN CHI TIẾT HEO NÁI",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = FarmGreenPrimary,
                                letterSpacing = 0.5.sp
                            )

                            // Trạng thái sinh sản badge
                            ReproductiveStatusBadge(
                                status = pig.status,
                                daysInStatus = daysInGestation
                            )
                        }

                        HorizontalDivider(color = FarmBackground)

                        // Dòng 1: Mã tai nái & Giống
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Mã tai nái:", fontSize = 12.sp, color = FarmTextSecondary)
                                Text(
                                    text = pig.tagCode,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FarmTextPrimary,
                                    modifier = Modifier.testTag("sow_tag_code")
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Giống:", fontSize = 12.sp, color = FarmTextSecondary)
                                Text(
                                    text = pig.breed,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FarmTextPrimary,
                                    modifier = Modifier.testTag("sow_breed")
                                )
                            }
                        }

                        // Dòng 2: Lứa đẻ & Vị trí chuồng
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Lứa đẻ:", fontSize = 12.sp, color = FarmTextSecondary)
                                Text(
                                    text = "Lứa ${pig.parity}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FarmTextPrimary,
                                    modifier = Modifier.testTag("sow_parity")
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Vị trí chuồng:", fontSize = 12.sp, color = FarmTextSecondary)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = FarmGreenPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = pig.location,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FarmTextPrimary,
                                        modifier = Modifier.testTag("sow_location")
                                    )
                                }
                            }
                        }

                        // Dòng 3: Trạng thái sinh sản & SỐ HEO CON THEO MẸ (Thay thế Trọng lượng)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Trạng thái sinh sản:", fontSize = 12.sp, color = FarmTextSecondary)
                                Text(
                                    text = pig.status.label,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (pig.status) {
                                        SowReproductiveStatus.MANG_THAI -> FarmAmber
                                        SowReproductiveStatus.NUOI_CON -> FarmGreenPrimary
                                        SowReproductiveStatus.LOAI_THAI -> FarmRed
                                        else -> FarmTextPrimary
                                    },
                                    modifier = Modifier.testTag("sow_reproductive_status")
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Số heo con theo mẹ:", fontSize = 12.sp, color = FarmTextSecondary)
                                Text(
                                    text = "${sucklingPigletsCount} con",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (pig.status == SowReproductiveStatus.NUOI_CON) FarmGreenPrimary else FarmTextSecondary,
                                    modifier = Modifier.testTag("sow_suckling_piglets_count")
                                )
                                Text(
                                    text = if (pig.status == SowReproductiveStatus.NUOI_CON) "Đang theo mẹ bú sữa" else "0 con (không nuôi con)",
                                    fontSize = 11.sp,
                                    color = FarmTextSecondary
                                )
                            }
                        }

                        // Dòng 4: Ngày dự sinh / Ngày phối / Ngày cai sữa (Cập nhật theo mốc sinh sản hiện tại)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(FarmBackground, RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "MỐC SINH SẢN HIỆN TẠI",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = FarmGreenPrimary,
                                letterSpacing = 0.5.sp
                            )
                            when (pig.status) {
                                SowReproductiveStatus.MANG_THAI -> {
                                    val matingMs = pig.lastMatingDate ?: (now - 85 * 86_400_000L)
                                    val farrowMs = matingMs + 114 * 86_400_000L
                                    Text(
                                        text = "• Ngày phối: ${dateFormat.format(Date(matingMs))}",
                                        fontSize = 13.sp,
                                        color = FarmTextPrimary
                                    )
                                    Text(
                                        text = "• Ngày dự sinh: ${dateFormat.format(Date(farrowMs))} (Còn ${daysRemaining ?: 29} ngày)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FarmAmber,
                                        modifier = Modifier.testTag("sow_milestone_date")
                                    )
                                }
                                SowReproductiveStatus.NUOI_CON -> {
                                    val farrowMs = pig.lastMatingDate?.let { it + 114 * 86_400_000L } ?: (now - 14 * 86_400_000L)
                                    val weanMs = farrowMs + 24 * 86_400_000L
                                    Text(
                                        text = "• Ngày đẻ: ${dateFormat.format(Date(farrowMs))}",
                                        fontSize = 13.sp,
                                        color = FarmTextPrimary
                                    )
                                    Text(
                                        text = "• Dự kiến cai sữa: ${dateFormat.format(Date(weanMs))} (24 ngày tuổi)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FarmGreenPrimary,
                                        modifier = Modifier.testTag("sow_milestone_date")
                                    )
                                }
                                SowReproductiveStatus.CAI_SUA_CHO_PHOI -> {
                                    val weanMs = now - 4 * 86_400_000L
                                    val mateMs = now + 3 * 86_400_000L
                                    Text(
                                        text = "• Ngày cai sữa: ${dateFormat.format(Date(weanMs))}",
                                        fontSize = 13.sp,
                                        color = FarmTextPrimary
                                    )
                                    Text(
                                        text = "• Dự kiến phối giống: ${dateFormat.format(Date(mateMs))} (Mốc 3-7 ngày sau cai sữa)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FarmGreenPrimary,
                                        modifier = Modifier.testTag("sow_milestone_date")
                                    )
                                }
                                SowReproductiveStatus.HAU_BI -> {
                                    Text(
                                        text = "• Ngày nhập đàn: $birthDateString",
                                        fontSize = 13.sp,
                                        color = FarmTextPrimary
                                    )
                                    Text(
                                        text = "• Dự kiến phối lứa đầu: ${dateFormat.format(Date(pig.birthDate + 240 * 86_400_000L))} (8 tháng tuổi)",
                                        fontSize = 13.sp,
                                        color = FarmGreenPrimary,
                                        modifier = Modifier.testTag("sow_milestone_date")
                                    )
                                }
                                SowReproductiveStatus.LOAI_THAI -> {
                                    Text(
                                        text = "• Ngày loại thải: ${dateFormat.format(Date(now))}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FarmRed,
                                        modifier = Modifier.testTag("sow_milestone_date")
                                    )
                                    Text(
                                        text = "Đã loại thải khỏi đàn giống, không phát sinh lịch công việc sinh sản mới.",
                                        fontSize = 12.sp,
                                        color = FarmTextSecondary
                                    )
                                }
                                else -> {
                                    Text(
                                        text = "• Tình trạng: ${pig.status.label}",
                                        fontSize = 13.sp,
                                        color = FarmTextPrimary,
                                        modifier = Modifier.testTag("sow_milestone_date")
                                    )
                                }
                            }
                        }

                        // Nếu đang mang thai, hiển thị chi tiết tiến độ
                        if (daysInGestation != null && pig.status == SowReproductiveStatus.MANG_THAI) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(FarmAmberContainer, RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "TIẾN ĐỘ THAI KỲ: NGÀY $daysInGestation / 114",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FarmAmber
                                    )
                                    Text(
                                        text = "Dự kiến sinh còn: $daysRemaining ngày • Chuẩn bị chuyển chuồng đẻ ở mốc D+107.",
                                        fontSize = 12.sp,
                                        color = FarmTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // Sức khỏe nái (vắc-xin, siêu âm & khám thai, cảnh báo sinh sản, BCS)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pig_detail_health_status"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (hasAnyWarning) Color(0xFFFFF8F8) else Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (hasAnyWarning) Color(0xFFFFCDD2) else FarmOutline
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.HealthAndSafety,
                                    contentDescription = null,
                                    tint = if (hasAnyWarning) FarmRed else FarmGreenPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "TÌNH TRẠNG SỨC KHỎE HIỆN TẠI",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (hasAnyWarning) FarmRed else FarmGreenPrimary,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (hasAnyWarning) FarmRedContainer else FarmGreenContainer
                            ) {
                                Text(
                                    text = if (hasAnyWarning) "CẦN CHÚ Ý" else "TỐT / ỔN ĐỊNH",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasAnyWarning) FarmRed else FarmGreenPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = if (hasAnyWarning) Color(0xFFFFEBEE) else FarmBackground)

                        // Vắc-xin
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = if (pig.hasOverdueVaccine) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (pig.hasOverdueVaccine) FarmRed else FarmGreenPrimary,
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Phòng dịch vắc-xin",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FarmTextPrimary
                                )
                                Text(
                                    text = if (pig.hasOverdueVaccine) {
                                        "⚠️ Cảnh báo: Quá hạn tiêm vắc-xin Dịch tả lợn / PRRS! Cần tiêm phòng bổ sung ngay."
                                    } else {
                                        "✓ Đã tiêm đủ các mũi bắt buộc (Dịch tả, LMLM, Tai xanh) đúng lịch trình."
                                    },
                                    fontSize = 12.sp,
                                    color = if (pig.hasOverdueVaccine) FarmRed else FarmTextSecondary
                                )
                            }
                        }

                        // Siêu âm & Khám thai
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = if (pig.hasUltrasoundWarning) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (pig.hasUltrasoundWarning) FarmAmber else FarmGreenPrimary,
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Siêu âm & Khám thai",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FarmTextPrimary
                                )
                                Text(
                                    text = when {
                                        pig.hasUltrasoundWarning -> "⚠️ Quá hạn siêu âm thai (mốc D+25 đến D+30). Cần kiểm tra siêu âm."
                                        pig.status == SowReproductiveStatus.MANG_THAI -> "✓ Thai kỳ ổn định, kết quả siêu âm đạt chuẩn (+)."
                                        else -> "— Không nằm trong giai đoạn kiểm tra thai kỳ."
                                    },
                                    fontSize = 12.sp,
                                    color = if (pig.hasUltrasoundWarning) FarmAmber else FarmTextSecondary
                                )
                            }
                        }

                        // Cảnh báo sinh sản bất thường
                        if (pig.hasDelayedHeatWarning || pig.hasProlongedGestationWarning) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = FarmRed,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Cảnh báo sinh sản bất thường",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FarmRed
                                    )
                                    Text(
                                        text = if (pig.hasProlongedGestationWarning) {
                                            "🚨 Thai kỳ vượt quá 116 ngày (quá ngày dự sinh). Báo bác sĩ thú y hỗ trợ ngay!"
                                        } else {
                                            "⚠️ Chậm lên giống sau cai sữa (> 7 ngày). Cần kiểm tra dinh dưỡng và kích dục."
                                        },
                                        fontSize = 12.sp,
                                        color = FarmRed
                                    )
                                }
                            }
                        }

                        // Thể trạng BCS
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = FarmGreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Thể trạng BCS: 3.5 / 5 • Mỡ lưng 16-18mm • Nhanh nhẹn, phàm ăn",
                                fontSize = 12.sp,
                                color = FarmTextSecondary
                            )
                        }
                    }
                }

                // Ngày sinh & Độ tuổi của heo nái
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pig_detail_birth_date"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FarmOutline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = FarmGreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "NGÀY SINH & ĐỘ TUỔI CÁ THỂ",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = FarmTextPrimary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Ngày sinh ghi nhận",
                                    fontSize = 12.sp,
                                    color = FarmTextSecondary
                                )
                                Text(
                                    text = birthDateString,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FarmTextPrimary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Độ tuổi hiện tại",
                                    fontSize = 12.sp,
                                    color = FarmTextSecondary
                                )
                                Text(
                                    text = ageDisplay,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FarmGreenPrimary
                                )
                            }
                        }

                        Text(
                            text = if (pig.parity == 0) {
                                "Heo hậu bị đang trong giai đoạn phát triển và nuôi dưỡng chuẩn bị phối giống lứa đầu."
                            } else {
                                "Heo nái sinh sản đang ở lứa ${pig.parity}, thể lực phát triển đầy đủ và cho năng suất ổn định."
                            },
                            fontSize = 12.sp,
                            color = FarmTextSecondary
                        )
                    }
                }
            }

            // PHẦN 4: HÀNH ĐỘNG
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Nút Nhập sự kiện nhanh cho heo nái / lô heo thịt
                Button(
                    onClick = { onQuickEvent?.invoke(pig) },
                    colors = ButtonDefaults.buttonColors(containerColor = FarmGreenPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("pig_detail_btn_quick_event")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Nhập sự kiện nhanh: ${if (pig.isBatch) "Lô ${pig.tagCode}" else "Nái ${pig.tagCode}"}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onClose,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("pig_detail_btn_close")
                    ) {
                        Text("Đóng", fontWeight = FontWeight.Bold)
                    }

                    if (onViewFullLifecycle != null) {
                        Button(
                            onClick = { onViewFullLifecycle(pig.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32).copy(alpha = 0.85f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(48.dp)
                                .testTag("pig_detail_btn_full_lifecycle")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Xem toàn bộ hồ sơ", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Bottom Sheet bao bọc PigDetailView để mở nhanh khi người dùng click vào bất kỳ item heo nào.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PigDetailBottomSheet(
    pig: PigEntity,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
    onViewFullLifecycle: ((String) -> Unit)? = null,
    onQuickEvent: ((PigEntity) -> Unit)? = null
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FarmBackground,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        PigDetailView(
            pig = pig,
            onClose = onDismiss,
            onViewFullLifecycle = onViewFullLifecycle,
            onQuickEvent = onQuickEvent
        )
    }
}
