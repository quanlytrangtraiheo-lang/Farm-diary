package com.example.ui.pig

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FarmAmber
import com.example.ui.theme.FarmAmberContainer
import com.example.ui.theme.FarmBlue
import com.example.ui.theme.FarmBlueContainer
import com.example.ui.theme.FarmGreenContainer
import com.example.ui.theme.FarmGreenPrimary
import com.example.ui.theme.FarmOutline
import com.example.ui.theme.FarmPurple
import com.example.ui.theme.FarmPurpleContainer
import com.example.ui.theme.FarmTextPrimary
import com.example.ui.theme.FarmTextSecondary

/**
 * Mô hình dữ liệu cơ cấu số lượng các loại heo trong trang trại:
 * 1. Nái bầu (Pregnant sows)
 * 2. Nái nuôi con (Nursing / Lactating sows)
 * 3. Nái chờ phối (Waiting for mating / Weaned sows)
 * 4. Heo con theo mẹ (Suckling piglets)
 * 5. Heo thịt (Meat / Grower-Finisher pigs)
 */
data class FarmHerdStructure(
    val pregnantSows: Int = 0,       // Nái bầu
    val lactatingSows: Int = 0,      // Nái nuôi con
    val waitingMatingSows: Int = 0,  // Nái chờ phối
    val sucklingPiglets: Int = 0,    // Heo con theo mẹ
    val meatPigs: Int = 0,           // Heo thịt
    val totalHeads: Int = 0
)

/**
 * Thẻ hiển thị tổng hợp cơ cấu đàn heo theo 5 loại heo chính trong trại.
 */
@Composable
fun FarmHerdSummarySection(
    summary: FarmHerdStructure,
    modifier: Modifier = Modifier,
    onCategorySelected: ((String) -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("farm_herd_summary_card"),
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
            // Header: Tiêu đề và tổng quy mô đàn
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
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            tint = FarmGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "SỐ LƯỢNG CÁC LOẠI HEO",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = FarmTextPrimary,
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Cơ cấu 5 nhóm đàn trong trại",
                            fontSize = 11.sp,
                            color = FarmTextSecondary,
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = FarmGreenContainer,
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(
                        text = "TỔNG: ${summary.totalHeads} CON",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = FarmGreenPrimary,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // Hàng 1 (2 cột): Nái Bầu & Nái Nuôi Con
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HerdTypeItemCard(
                    title = "Nái bầu",
                    count = summary.pregnantSows,
                    subtitle = "Đang mang thai",
                    icon = Icons.Default.Pets,
                    containerColor = FarmAmberContainer,
                    accentColor = FarmAmber,
                    testTag = "herd_category_pregnant",
                    modifier = Modifier.weight(1f),
                    onClick = { onCategorySelected?.invoke("MANG_THAI") }
                )

                HerdTypeItemCard(
                    title = "Nái nuôi con",
                    count = summary.lactatingSows,
                    subtitle = "Đang cho bú",
                    icon = Icons.Default.Spa,
                    containerColor = FarmGreenContainer,
                    accentColor = FarmGreenPrimary,
                    testTag = "herd_category_lactating",
                    modifier = Modifier.weight(1f),
                    onClick = { onCategorySelected?.invoke("NUOI_CON") }
                )
            }

            // Hàng 2 (2 cột): Nái Chờ Phối & Heo Con Theo Mẹ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HerdTypeItemCard(
                    title = "Nái chờ phối",
                    count = summary.waitingMatingSows,
                    subtitle = "Cai sữa & Hậu bị",
                    icon = Icons.Default.HourglassTop,
                    containerColor = FarmPurpleContainer,
                    accentColor = FarmPurple,
                    testTag = "herd_category_waiting",
                    modifier = Modifier.weight(1f),
                    onClick = { onCategorySelected?.invoke("CHO_PHOI") }
                )

                HerdTypeItemCard(
                    title = "Heo con theo mẹ",
                    count = summary.sucklingPiglets,
                    subtitle = "Nuôi chuồng đẻ",
                    icon = Icons.Default.ChildCare,
                    containerColor = Color(0xFFFCE4EC),
                    accentColor = Color(0xFFC2185B),
                    testTag = "herd_category_suckling",
                    modifier = Modifier.weight(1f),
                    onClick = { onCategorySelected?.invoke("HEO_CON") }
                )
            }

            // Hàng 3 (Full width): Heo Thịt (Quy mô lớn)
            HerdTypeWideCard(
                title = "Heo thịt thương phẩm",
                count = summary.meatPigs,
                subtitle = "Cấp dưỡng & Chuẩn bị xuất bán",
                icon = Icons.Default.Warehouse,
                containerColor = FarmBlueContainer,
                accentColor = FarmBlue,
                testTag = "herd_category_meat",
                modifier = Modifier.fillMaxWidth(),
                onClick = { onCategorySelected?.invoke("HEO_THIT") }
            )
        }
    }
}

@Composable
private fun HerdTypeWideCard(
    title: String,
    count: Int,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    accentColor: Color,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = FarmTextPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = FarmTextSecondary,
                        maxLines = 1
                    )
                }
            }

            Text(
                text = "$count con",
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = accentColor
            )
        }
    }
}

@Composable
private fun HerdTypeItemCard(
    title: String,
    count: Int,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    accentColor: Color,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = FarmTextPrimary,
                    maxLines = 1
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = "$count con",
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = accentColor
            )

            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = FarmTextSecondary,
                maxLines = 1
            )
        }
    }
}
