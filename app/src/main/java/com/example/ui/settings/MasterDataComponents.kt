package com.example.ui.settings

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.text.TextStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.SystemParameterEntity
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
import java.text.DecimalFormat

/**
 * Định dạng tiền tệ VNĐ có phân cách hàng nghìn.
 */
fun formatVnd(amount: Double): String {
    val formatter = DecimalFormat("#,###")
    return "${formatter.format(amount.toLong()).replace(',', '.')} VNĐ"
}

/**
 * Card hiển thị Heo đực giống & Lô tinh phối
 */
@Composable
fun BoarMasterCard(
    parameter: SystemParameterEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDoseDelta: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(FarmBackground)
            .border(1.dp, FarmOutline, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header: Mã & Trạng thái & Nút thao tác
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White
                    ) {
                        Text(
                            text = parameter.code,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FarmGreenPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    val (statusBg, statusFg) = when (parameter.statusText) {
                        "Đang hoạt động" -> Pair(FarmGreenContainer, FarmGreenPrimary)
                        "Đã hết" -> Pair(FarmAmberContainer, FarmAmber)
                        else -> Pair(FarmRedContainer, FarmRed)
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusBg
                    ) {
                        Text(
                            text = parameter.statusText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusFg,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Sửa", tint = FarmGreenPrimary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Xóa", tint = FarmRed, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Tên đực / Lô tinh
            Text(
                text = parameter.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = FarmTextPrimary
            )

            // Thuộc tính: Giống & Nguồn gốc
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (parameter.secondaryValue.isNotBlank()) {
                    Text(
                        text = "Giống: ${parameter.secondaryValue}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FarmTextSecondary
                    )
                }

                if (parameter.subType.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = FarmBlueContainer
                    ) {
                        Text(
                            text = parameter.subType,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = FarmBlue,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Điều chỉnh số lượng liều tinh tồn kho
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(1.dp, FarmOutline, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Liều tồn: ${parameter.numericValue.toInt()} liều",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (parameter.numericValue > 0) FarmGreenPrimary else FarmAmber
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = { onDoseDelta(-1) },
                        modifier = Modifier.size(28.dp),
                        enabled = parameter.numericValue > 0
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Giảm", tint = FarmRed, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = { onDoseDelta(1) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Tăng", tint = FarmGreenPrimary, modifier = Modifier.size(16.dp))
                    }
                }
            }

            if (parameter.description.isNotBlank()) {
                Text(
                    text = parameter.description,
                    fontSize = 12.sp,
                    color = FarmTextSecondary
                )
            }
        }
    }
}

/**
 * Card hiển thị Nhân sự & Kỹ thuật viên
 */
@Composable
fun StaffMasterCard(
    parameter: SystemParameterEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(FarmBackground)
            .border(1.dp, FarmOutline, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Avatar chữ cái đầu
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(FarmGreenContainer),
                    contentAlignment = Alignment.Center
                ) {
                    val initial = parameter.name.trim().takeLast(1).uppercase()
                    Text(
                        text = initial.ifBlank { "N" },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = FarmGreenPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = parameter.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = FarmTextPrimary
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White
                        ) {
                            Text(
                                text = parameter.code,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = FarmGreenPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Vai trò & Trạng thái
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (parameter.subType.isNotBlank()) {
                            val roleBg = when {
                                parameter.subType.contains("Quản lý") -> FarmBlueContainer
                                parameter.subType.contains("Kỹ thuật") -> FarmGreenContainer
                                parameter.subType.contains("Thú y") -> Color(0xFFF3E5F5)
                                else -> FarmAmberContainer
                            }
                            val roleFg = when {
                                parameter.subType.contains("Quản lý") -> FarmBlue
                                parameter.subType.contains("Kỹ thuật") -> FarmGreenPrimary
                                parameter.subType.contains("Thú y") -> Color(0xFF7B1FA2)
                                else -> FarmAmber
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = roleBg
                            ) {
                                Text(
                                    text = parameter.subType,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = roleFg,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (parameter.secondaryValue.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = FarmTextSecondary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = parameter.secondaryValue,
                                    fontSize = 11.sp,
                                    color = FarmTextSecondary
                                )
                            }
                        }
                    }

                    if (parameter.description.isNotBlank()) {
                        Text(
                            text = parameter.description,
                            fontSize = 11.sp,
                            color = FarmTextSecondary
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Sửa", tint = FarmGreenPrimary, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Xóa", tint = FarmRed, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

/**
 * Card hiển thị Giống heo
 */
@Composable
fun BreedMasterCard(
    parameter: SystemParameterEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(FarmBackground)
            .border(1.dp, FarmOutline, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = parameter.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = FarmTextPrimary
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White
                    ) {
                        Text(
                            text = parameter.code,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = FarmGreenPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (parameter.subType.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = FarmGreenContainer
                        ) {
                            Text(
                                text = parameter.subType,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = FarmGreenPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Sửa", tint = FarmGreenPrimary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Xóa", tint = FarmRed, modifier = Modifier.size(16.dp))
                    }
                }
            }

            if (parameter.description.isNotBlank()) {
                Text(
                    text = parameter.description,
                    fontSize = 12.sp,
                    color = FarmTextSecondary
                )
            }
        }
    }
}

/**
 * Card hiển thị Thức ăn (Cám)
 */
@Composable
fun FeedMasterCard(
    parameter: SystemParameterEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(FarmBackground)
            .border(1.dp, FarmOutline, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = parameter.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = FarmTextPrimary
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White
                    ) {
                        Text(
                            text = parameter.code,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = FarmGreenPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Sửa", tint = FarmGreenPrimary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Xóa", tint = FarmRed, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (parameter.secondaryValue.isNotBlank()) {
                        Text(
                            text = "NCC: ${parameter.secondaryValue}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FarmTextSecondary
                        )
                    }
                    if (parameter.unit.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.White
                        ) {
                            Text(
                                text = parameter.unit,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = FarmGreenPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (parameter.numericValue > 0) {
                    Text(
                        text = formatVnd(parameter.numericValue),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = FarmGreenPrimary
                    )
                }
            }

            // Cấu hình Ngưỡng an toàn (Ngưỡng ngày an toàn & Mức tồn kho tối thiểu)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(1.dp, FarmOutline, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Ngưỡng an toàn:",
                        fontSize = 11.sp,
                        color = FarmTextSecondary
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = FarmAmberContainer
                    ) {
                        Text(
                            text = "${parameter.safetyDaysThreshold} ngày",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FarmAmber,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Tồn tối thiểu:",
                        fontSize = 11.sp,
                        color = FarmTextSecondary
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = FarmBlueContainer
                    ) {
                        Text(
                            text = "${parameter.minStockThreshold.toInt()} ${if (parameter.unit.contains("kg", ignoreCase = true)) "bao" else parameter.unit.ifBlank { "bao" }}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FarmBlue,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (parameter.description.isNotBlank()) {
                Text(
                    text = parameter.description,
                    fontSize = 12.sp,
                    color = FarmTextSecondary
                )
            }
        }
    }
}

/**
 * Card hiển thị Thuốc & Vắc-xin
 */
@Composable
fun MedicineMasterCard(
    parameter: SystemParameterEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(FarmBackground)
            .border(1.dp, FarmOutline, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = parameter.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = FarmTextPrimary
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White
                    ) {
                        Text(
                            text = parameter.code,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = FarmGreenPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (parameter.subType.isNotBlank()) {
                        val typeColor = when (parameter.subType) {
                            "Vaccine" -> FarmBlue
                            "Kháng sinh" -> FarmRed
                            "Sát trùng" -> FarmAmber
                            else -> FarmGreenPrimary
                        }
                        val typeContainer = when (parameter.subType) {
                            "Vaccine" -> FarmBlueContainer
                            "Kháng sinh" -> FarmRedContainer
                            "Sát trùng" -> FarmAmberContainer
                            else -> FarmGreenContainer
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = typeContainer
                        ) {
                            Text(
                                text = parameter.subType,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = typeColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Sửa", tint = FarmGreenPrimary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Xóa", tint = FarmRed, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (parameter.unit.isNotBlank()) {
                    Text(
                        text = "ĐVT: ${parameter.unit}",
                        fontSize = 12.sp,
                        color = FarmTextSecondary
                    )
                }

                if (parameter.numericValue > 0) {
                    Text(
                        text = formatVnd(parameter.numericValue),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = FarmGreenPrimary
                    )
                }
            }

            if (parameter.description.isNotBlank()) {
                Text(
                    text = parameter.description,
                    fontSize = 12.sp,
                    color = FarmTextSecondary
                )
            }
        }
    }
}

/**
 * Card hiển thị Vật tư chuồng trại
 */
@Composable
fun SupplyMasterCard(
    parameter: SystemParameterEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(FarmBackground)
            .border(1.dp, FarmOutline, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = parameter.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = FarmTextPrimary
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White
                    ) {
                        Text(
                            text = parameter.code,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = FarmGreenPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (parameter.statusText == "Hết hàng") FarmRedContainer else FarmGreenContainer
                    ) {
                        Text(
                            text = parameter.statusText.ifBlank { "Còn hàng" },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (parameter.statusText == "Hết hàng") FarmRed else FarmGreenPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Sửa", tint = FarmGreenPrimary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Xóa", tint = FarmRed, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (parameter.unit.isNotBlank()) {
                    Text(
                        text = "ĐVT: ${parameter.unit}",
                        fontSize = 12.sp,
                        color = FarmTextSecondary
                    )
                }

                if (parameter.numericValue > 0) {
                    Text(
                        text = formatVnd(parameter.numericValue),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = FarmGreenPrimary
                    )
                }
            }

            if (parameter.description.isNotBlank()) {
                Text(
                    text = parameter.description,
                    fontSize = 12.sp,
                    color = FarmTextSecondary
                )
            }
        }
    }
}

/**
 * Card hiển thị Chuồng nuôi & Nguyên nhân loại thải
 */
@Composable
fun BarnAndCullMasterCard(
    parameter: SystemParameterEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FarmBackground)
            .border(1.dp, FarmOutline, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = parameter.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = FarmTextPrimary
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White
                    ) {
                        Text(
                            text = parameter.code,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = FarmGreenPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (parameter.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = parameter.description,
                        fontSize = 12.sp,
                        color = FarmTextSecondary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Sửa", tint = FarmGreenPrimary, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Xóa", tint = FarmRed, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

/**
 * Card hiển thị Định mức Khẩu phần cám theo tuần tuổi & nhóm sinh lý
 */
@Composable
fun FeedRationMasterCard(
    parameter: SystemParameterEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(FarmBackground)
            .border(1.dp, FarmOutline, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header: Nhóm sinh lý & Tuần chu kỳ & Thao tác
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val (stageColor, stageBg) = when (parameter.code) {
                        SystemParameterEntity.STAGE_PREGNANT_SOW -> Pair(FarmBlue, FarmBlueContainer)
                        SystemParameterEntity.STAGE_LACTATING_SOW -> Pair(FarmRed, FarmRedContainer)
                        SystemParameterEntity.STAGE_WEANED_WAITING_SOW -> Pair(FarmAmber, FarmAmberContainer)
                        SystemParameterEntity.STAGE_SUCKLING_PIGLET -> Pair(Color(0xFF8E24AA), Color(0xFFF3E5F5))
                        else -> Pair(FarmGreenPrimary, FarmGreenContainer)
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = stageBg
                    ) {
                        Text(
                            text = parameter.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = stageColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White
                    ) {
                        Text(
                            text = if (parameter.statusText.isNotBlank()) parameter.statusText else "Tuần ${parameter.displayOrder}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FarmTextSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Sửa", tint = FarmGreenPrimary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Xóa", tint = FarmRed, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Loại cám và Định mức ăn
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(1.dp, FarmOutline, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Loại cám sử dụng",
                        fontSize = 11.sp,
                        color = FarmTextSecondary
                    )
                    Text(
                        text = parameter.secondaryValue.ifBlank { parameter.subType },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = FarmTextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = FarmGreenContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${parameter.numericValue} kg",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = FarmGreenPrimary
                        )
                        Text(
                            text = "/ngày/con",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = FarmGreenPrimary
                        )
                    }
                }
            }

            if (parameter.description.isNotBlank()) {
                Text(
                    text = parameter.description,
                    fontSize = 12.sp,
                    color = FarmTextSecondary
                )
            }
        }
    }
}

/**
 * Bảng dữ liệu Khẩu phần cám với Sticky First Column (Loại heo),
 * các cột còn lại cuộn ngang (horizontal scroll),
 * số liệu ngắn gọn (ví dụ 2.0, 3.0, 6.5) với đơn vị kg/ngày đặt ở tiêu đề cột,
 * đảm bảo chiều cao hàng tối thiểu 52dp và touch target >= 48dp cho thao tác ngoài chuồng khi đeo găng.
 */
@Composable
fun FeedRationMatrixTableView(
    rationList: List<SystemParameterEntity>,
    onEdit: (SystemParameterEntity) -> Unit,
    onDelete: (SystemParameterEntity) -> Unit
) {
    val horizontalScrollState = rememberScrollState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("feed_ration_matrix_table"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, FarmOutline)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE2E8F0))
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sticky Header: Loại heo
                Box(
                    modifier = Modifier
                        .width(115.dp)
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "LOẠI HEO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = FarmTextPrimary
                    )
                }

                // Sticky separator
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(20.dp)
                        .background(Color(0xFFCBD5E1))
                )

                // Scrollable Headers
                Row(
                    modifier = Modifier
                        .horizontalScroll(horizontalScrollState)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(105.dp)
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "TUẦN TUỔI",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = FarmTextPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(135.dp)
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "LOẠI CÁM",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = FarmTextPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(110.dp)
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ĐỊNH MỨC\n(kg/ngày)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = FarmTextPrimary,
                            lineHeight = 13.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(85.dp)
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "THAO TÁC",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = FarmTextPrimary
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFCBD5E1), thickness = 1.dp)

            // Table Data Rows
            rationList.forEachIndexed { index, param ->
                val (stageColor, stageBg) = when (param.code) {
                    SystemParameterEntity.STAGE_PREGNANT_SOW -> Pair(FarmBlue, FarmBlueContainer)
                    SystemParameterEntity.STAGE_LACTATING_SOW -> Pair(FarmRed, FarmRedContainer)
                    SystemParameterEntity.STAGE_WEANED_WAITING_SOW -> Pair(FarmAmber, FarmAmberContainer)
                    SystemParameterEntity.STAGE_SUCKLING_PIGLET -> Pair(Color(0xFF8E24AA), Color(0xFFF3E5F5))
                    else -> Pair(FarmGreenPrimary, FarmGreenContainer)
                }

                val rowBg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(rowBg)
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sticky Column: Loại heo
                    Box(
                        modifier = Modifier
                            .width(115.dp)
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = stageBg
                        ) {
                            Text(
                                text = param.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = stageColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                maxLines = 1
                            )
                        }
                    }

                    // Sticky vertical divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(Color(0xFFE2E8F0))
                    )

                    // Scrollable columns for this row
                    Row(
                        modifier = Modifier
                            .horizontalScroll(horizontalScrollState)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tuần tuổi
                        Box(
                            modifier = Modifier
                                .width(105.dp)
                                .padding(horizontal = 6.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = if (param.statusText.isNotBlank()) param.statusText else "Tuần ${param.displayOrder}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = FarmTextPrimary
                            )
                        }

                        // Loại cám
                        Box(
                            modifier = Modifier
                                .width(135.dp)
                                .padding(horizontal = 6.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = param.secondaryValue.ifBlank { param.subType },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = FarmTextPrimary,
                                maxLines = 1
                            )
                        }

                        // Định mức (kg/ngày) - số gọn
                        Box(
                            modifier = Modifier
                                .width(110.dp)
                                .padding(horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = FarmGreenContainer
                            ) {
                                Text(
                                    text = String.format(java.util.Locale.US, "%.1f", param.numericValue),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = FarmGreenPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Thao tác (Edit / Delete) - touch target >= 48dp
                        Row(
                            modifier = Modifier
                                .width(85.dp)
                                .padding(horizontal = 2.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { onEdit(param) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Sửa định mức",
                                    tint = FarmGreenPrimary,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            IconButton(
                                onClick = { onDelete(param) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Xóa định mức",
                                    tint = FarmRed,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }

                if (index < rationList.size - 1) {
                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.8.dp)
                }
            }
        }
    }
}


// =========================================================================
// BẢNG MÀU & ĐỘ TƯƠNG PHẢN CHUẨN TIẾP CẬN WCAG CHO MASTER DATA DIALOG
// =========================================================================
private val DialogLabelColor = Color(0xFF374151) // Slate-700 (~9.5:1 contrast trên nền trắng)
private val DialogRequiredStarColor = Color(0xFFDC2626) // Red-600 (Dấu bắt buộc *)
private val DialogInputTextColor = Color(0xFF111827) // Gray-900 (Đậm nét, dễ nhận biết dữ liệu thực)
private val DialogPlaceholderColor = Color(0xFF9CA3AF) // Gray-400 (Văn bản gợi ý khi ô trống)
private val DialogHelperTextColor = Color(0xFF6B7280) // Gray-500 (Văn bản giải thích 12px)
private val DialogBorderDefault = Color(0xFFD1D5DB) // Gray-300 (Viền mặc định)
private val DialogBorderFocus = Color(0xFF16A34A) // Green-600 (Viền khi chạm vào/focus)
private val DialogSaveButtonBg = Color(0xFF166534) // Green-800 / Forest Green (Tỷ lệ tương phản 6.3:1)
private val DialogCancelButtonContent = Color(0xFF4B5563) // Gray-600

@Composable
private fun ParameterFieldLabel(
    title: String,
    isRequired: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium, // font-weight: 500
            color = DialogLabelColor
        )
        if (isRequired) {
            Text(
                text = "*",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = DialogRequiredStarColor
            )
        }
    }
}

@Composable
private fun parameterTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = DialogInputTextColor,
    unfocusedTextColor = DialogInputTextColor,
    disabledTextColor = DialogInputTextColor.copy(alpha = 0.6f),
    cursorColor = DialogBorderFocus,
    focusedBorderColor = DialogBorderFocus,
    unfocusedBorderColor = DialogBorderDefault,
    disabledBorderColor = DialogBorderDefault.copy(alpha = 0.5f),
    focusedPlaceholderColor = DialogPlaceholderColor,
    unfocusedPlaceholderColor = DialogPlaceholderColor,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    disabledContainerColor = Color(0xFFF9FAFB)
)

/**
 * Hộp thoại tạo mới / chỉnh sửa tham số hệ thống thích ứng theo từng tiểu mục (Subtab).
 * Tuân thủ tiêu chuẩn tương phản WCAG (labels >= 4.5:1, button >= 6.3:1).
 */
@Composable
fun MasterDataInputDialog(
    category: String,
    initialParameter: SystemParameterEntity?,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        code: String,
        description: String,
        unit: String,
        subType: String,
        secondaryValue: String,
        numericValue: Double,
        statusText: String,
        safetyDaysThreshold: Int,
        minStockThreshold: Double
    ) -> Unit
) {
    val categoryTitle = when (category) {
        SystemParameterEntity.CATEGORY_BOAR -> "Heo đực & Tinh"
        SystemParameterEntity.CATEGORY_STAFF -> "Nhân viên"
        SystemParameterEntity.CATEGORY_BREED -> "Giống heo"
        SystemParameterEntity.CATEGORY_FEED -> "Thức ăn (Cám)"
        SystemParameterEntity.CATEGORY_MEDICINE -> "Thuốc & Vắc-xin"
        SystemParameterEntity.CATEGORY_SUPPLIES -> "Vật tư"
        SystemParameterEntity.CATEGORY_BARN -> "Chuồng nuôi"
        SystemParameterEntity.CATEGORY_CULLING_REASON -> "Nguyên nhân loại thải"
        SystemParameterEntity.CATEGORY_FEED_RATION -> "Khẩu phần cám"
        else -> "Tham số"
    }

    var name by remember {
        mutableStateOf(
            initialParameter?.name ?: when (category) {
                SystemParameterEntity.CATEGORY_FEED_RATION -> "Nái mang thai"
                else -> ""
            }
        )
    }
    var code by remember {
        mutableStateOf(
            initialParameter?.code ?: when (category) {
                SystemParameterEntity.CATEGORY_FEED_RATION -> SystemParameterEntity.STAGE_PREGNANT_SOW
                else -> ""
            }
        )
    }
    var description by remember { mutableStateOf(initialParameter?.description ?: "") }
    var unit by remember {
        mutableStateOf(
            initialParameter?.unit ?: when (category) {
                SystemParameterEntity.CATEGORY_FEED_RATION -> "kg/ngày/con"
                else -> ""
            }
        )
    }
    var subType by remember {
        mutableStateOf(
            initialParameter?.subType ?: when (category) {
                SystemParameterEntity.CATEGORY_BOAR -> "Tại trại"
                SystemParameterEntity.CATEGORY_STAFF -> "Kỹ thuật viên"
                SystemParameterEntity.CATEGORY_BREED -> "Nái nền sinh sản"
                SystemParameterEntity.CATEGORY_MEDICINE -> "Kháng sinh"
                SystemParameterEntity.CATEGORY_FEED_RATION -> "CAM_NAI_MANG_THAI"
                else -> ""
            }
        )
    }
    var secondaryValue by remember {
        mutableStateOf(
            initialParameter?.secondaryValue ?: when (category) {
                SystemParameterEntity.CATEGORY_BOAR -> "Duroc"
                SystemParameterEntity.CATEGORY_FEED -> "CP Feed"
                SystemParameterEntity.CATEGORY_FEED_RATION -> "Cám nái bầu (CP 566)"
                else -> ""
            }
        )
    }
    var numericValueText by remember {
        mutableStateOf(
            if (initialParameter != null && initialParameter.numericValue > 0) {
                if (category == SystemParameterEntity.CATEGORY_BOAR) {
                    initialParameter.numericValue.toInt().toString()
                } else if (category == SystemParameterEntity.CATEGORY_FEED_RATION) {
                    initialParameter.numericValue.toString()
                } else {
                    initialParameter.numericValue.toLong().toString()
                }
            } else if (category == SystemParameterEntity.CATEGORY_FEED_RATION) {
                "2.0"
            } else ""
        )
    }
    var statusText by remember {
        mutableStateOf(
            initialParameter?.statusText ?: when (category) {
                SystemParameterEntity.CATEGORY_STAFF -> "Đang làm việc"
                SystemParameterEntity.CATEGORY_SUPPLIES -> "Còn hàng"
                SystemParameterEntity.CATEGORY_FEED_RATION -> "Tuần 1 - 12"
                else -> "Đang hoạt động"
            }
        )
    }
    var safetyDaysThresholdText by remember {
        mutableStateOf(
            (initialParameter?.safetyDaysThreshold ?: 3).toString()
        )
    }
    var minStockThresholdText by remember {
        mutableStateOf(
            (initialParameter?.minStockThreshold ?: 20.0).toInt().toString()
        )
    }

    var isNameError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tiêu đề & nút đóng
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialParameter == null) "Thêm $categoryTitle" else "Sửa $categoryTitle",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DialogInputTextColor
                    )

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng",
                            tint = DialogCancelButtonContent
                        )
                    }
                }

                // Field 1: Tên chính (Bắt buộc *)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val labelTitle = when (category) {
                        SystemParameterEntity.CATEGORY_BOAR -> "Tên đực giống / Lô liều tinh"
                        SystemParameterEntity.CATEGORY_STAFF -> "Họ và tên nhân viên"
                        SystemParameterEntity.CATEGORY_BREED -> "Tên giống heo"
                        SystemParameterEntity.CATEGORY_FEED -> "Tên loại cám"
                        SystemParameterEntity.CATEGORY_MEDICINE -> "Tên thuốc / Vắc-xin"
                        SystemParameterEntity.CATEGORY_SUPPLIES -> "Tên vật tư"
                        else -> "Tên danh mục"
                    }
                    ParameterFieldLabel(title = "$labelTitle:", isRequired = true)
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            isNameError = it.isBlank()
                            if (code.isBlank() && it.isNotBlank()) {
                                code = it.trim().replace(" ", "_").uppercase().take(12)
                            }
                        },
                        isError = isNameError,
                        colors = parameterTextFieldColors(),
                        textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = DialogInputTextColor),
                        placeholder = { Text("Nhập $labelTitle...", color = DialogPlaceholderColor, fontSize = 13.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (isNameError) {
                        Text("Vui lòng không để trống trường này", fontSize = 11.sp, color = DialogRequiredStarColor)
                    }
                }

                // Field 2: Mã ký hiệu (Không bắt buộc)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ParameterFieldLabel(title = "Mã ký hiệu / Mã viết tắt:", isRequired = false)
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.uppercase() },
                        colors = parameterTextFieldColors(),
                        textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = DialogInputTextColor),
                        placeholder = { Text("Ví dụ: CAM_01, TH_02...", color = DialogPlaceholderColor, fontSize = 13.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Cấu hình linh hoạt theo từng Subtab
                when (category) {
                    SystemParameterEntity.CATEGORY_BOAR -> {
                        // Giống & Nguồn gốc
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ParameterFieldLabel(title = "Giống heo đực:", isRequired = false)
                                OutlinedTextField(
                                    value = secondaryValue,
                                    onValueChange = { secondaryValue = it },
                                    colors = parameterTextFieldColors(),
                                    textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = DialogInputTextColor),
                                    placeholder = { Text("Duroc, Pietrain...", color = DialogPlaceholderColor, fontSize = 13.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ParameterFieldLabel(title = "Nguồn gốc:", isRequired = false)
                                OutlinedTextField(
                                    value = subType,
                                    onValueChange = { subType = it },
                                    colors = parameterTextFieldColors(),
                                    textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = DialogInputTextColor),
                                    placeholder = { Text("Tại trại / Nhập ngoại", color = DialogPlaceholderColor, fontSize = 13.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Số lượng liều tồn & Trạng thái
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ParameterFieldLabel(title = "Số liều tồn kho:", isRequired = false)
                                OutlinedTextField(
                                    value = numericValueText,
                                    onValueChange = { numericValueText = it.filter { ch -> ch.isDigit() } },
                                    colors = parameterTextFieldColors(),
                                    textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = DialogInputTextColor),
                                    placeholder = { Text("Ví dụ: 25", color = DialogPlaceholderColor, fontSize = 13.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ParameterFieldLabel(title = "Trạng thái:", isRequired = false)
                                OutlinedTextField(
                                    value = statusText,
                                    onValueChange = { statusText = it },
                                    colors = parameterTextFieldColors(),
                                    textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = DialogInputTextColor),
                                    placeholder = { Text("Đang hoạt động, Đã hết...", color = DialogPlaceholderColor, fontSize = 13.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    SystemParameterEntity.CATEGORY_STAFF -> {
                        // SĐT & Chức vụ
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ParameterFieldLabel(title = "Số điện thoại:", isRequired = false)
                                OutlinedTextField(
                                    value = secondaryValue,
                                    onValueChange = { secondaryValue = it },
                                    colors = parameterTextFieldColors(),
                                    textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = DialogInputTextColor),
                                    placeholder = { Text("09xx xxx xxx", color = DialogPlaceholderColor, fontSize = 13.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ParameterFieldLabel(title = "Chức vụ / Vai trò:", isRequired = false)
                                OutlinedTextField(
                                    value = subType,
                                    onValueChange = { subType = it },
                                    colors = parameterTextFieldColors(),
                                    textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = DialogInputTextColor),
                                    placeholder = { Text("Quản lý, Kỹ thuật viên...", color = DialogPlaceholderColor, fontSize = 13.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    SystemParameterEntity.CATEGORY_BREED -> {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            ParameterFieldLabel(title = "Phân nhóm khai thác:", isRequired = false)
                            OutlinedTextField(
                                value = subType,
                                onValueChange = { subType = it },
                                colors = parameterTextFieldColors(),
                                textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = DialogInputTextColor),
                                placeholder = { Text("Nái nền sinh sản, Heo thịt, Đực giống...", color = DialogPlaceholderColor, fontSize = 13.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    SystemParameterEntity.CATEGORY_FEED -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ParameterFieldLabel(title = "Nhà cung cấp:", isRequired = false)
                                OutlinedTextField(
                                    value = secondaryValue,
                                    onValueChange = { secondaryValue = it },
                                    colors = parameterTextFieldColors(),
                                    textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = DialogInputTextColor),
                                    placeholder = { Text("CP Feed, Cargill...", color = DialogPlaceholderColor, fontSize = 13.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ParameterFieldLabel(title = "Đơn vị tính (ĐVT):", isRequired = false)
                                OutlinedTextField(
                                    value = unit,
                                    onValueChange = { unit = it },
                                    colors = parameterTextFieldColors(),
                                    textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = DialogInputTextColor),
                                    placeholder = { Text("Bao 25kg, Kg...", color = DialogPlaceholderColor, fontSize = 13.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            ParameterFieldLabel(title = "Đơn giá (VNĐ):", isRequired = false)
                            OutlinedTextField(
                                value = numericValueText,
                                onValueChange = { numericValueText = it.filter { ch -> ch.isDigit() } },
                                colors = parameterTextFieldColors(),
                                textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = DialogInputTextColor),
                                placeholder = { Text("Ví dụ: 350000", color = DialogPlaceholderColor, fontSize = 13.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Cấu hình Ngưỡng an toàn cho loại cám
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ParameterFieldLabel(title = "Ngưỡng ngày an toàn (ngày):", isRequired = false)
                                OutlinedTextField(
                                    value = safetyDaysThresholdText,
                                    onValueChange = { safetyDaysThresholdText = it.filter { ch -> ch.isDigit() } },
                                    colors = parameterTextFieldColors(),
                                    textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = DialogInputTextColor),
                                    placeholder = { Text("Mặc định: 3 ngày", color = DialogPlaceholderColor, fontSize = 13.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = "Tùy chỉnh theo chu kỳ đặt hàng (ví dụ: xa đặt 5 ngày, gần đặt 2 ngày)",
                                    fontSize = 12.sp,
                                    color = DialogHelperTextColor
                                )
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ParameterFieldLabel(title = "Tồn tối thiểu (Bao/Kg):", isRequired = false)
                                OutlinedTextField(
                                    value = minStockThresholdText,
                                    onValueChange = { minStockThresholdText = it.filter { ch -> ch.isDigit() } },
                                    colors = parameterTextFieldColors(),
                                    textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = DialogInputTextColor),
                                    placeholder = { Text("Mặc định: 20 bao", color = DialogPlaceholderColor, fontSize = 13.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = "Ngưỡng cứng dự phòng sự cố vận chuyển",
                                    fontSize = 12.sp,
                                    color = DialogHelperTextColor
                                )
                            }
                        }
                    }

                    SystemParameterEntity.CATEGORY_MEDICINE -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ParameterFieldLabel(title = "Phân loại:", isRequired = false)
                                OutlinedTextField(
                                    value = subType,
                                    onValueChange = { subType = it },
                                    colors = parameterTextFieldColors(),
                                    textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = DialogInputTextColor),
                                    placeholder = { Text("Vaccine, Kháng sinh, Bổ trợ...", color = DialogPlaceholderColor, fontSize = 13.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ParameterFieldLabel(title = "Đơn vị tính:", isRequired = false)
                                OutlinedTextField(
                                    value = unit,
                                    onValueChange = { unit = it },
                                    colors = parameterTextFieldColors(),
                                    textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = DialogInputTextColor),
                                    placeholder = { Text("ml, Lọ, Liều, Chai...", color = DialogPlaceholderColor, fontSize = 13.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            ParameterFieldLabel(title = "Đơn giá (VNĐ):", isRequired = false)
                            OutlinedTextField(
                                value = numericValueText,
                                onValueChange = { numericValueText = it.filter { ch -> ch.isDigit() } },
                                colors = parameterTextFieldColors(),
                                textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = DialogInputTextColor),
                                placeholder = { Text("Ví dụ: 125000", color = DialogPlaceholderColor, fontSize = 13.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    SystemParameterEntity.CATEGORY_SUPPLIES -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ParameterFieldLabel(title = "Đơn vị tính:", isRequired = false)
                                OutlinedTextField(
                                    value = unit,
                                    onValueChange = { unit = it },
                                    colors = parameterTextFieldColors(),
                                    textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = DialogInputTextColor),
                                    placeholder = { Text("Cái, Hộp, Cuộn...", color = DialogPlaceholderColor, fontSize = 13.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ParameterFieldLabel(title = "Đơn giá (VNĐ):", isRequired = false)
                                OutlinedTextField(
                                    value = numericValueText,
                                    onValueChange = { numericValueText = it.filter { ch -> ch.isDigit() } },
                                    colors = parameterTextFieldColors(),
                                    textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = DialogInputTextColor),
                                    placeholder = { Text("Ví dụ: 45000", color = DialogPlaceholderColor, fontSize = 13.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    SystemParameterEntity.CATEGORY_FEED_RATION -> {
                        // 1. Nhóm sinh lý heo
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            ParameterFieldLabel(title = "Nhóm sinh lý:", isRequired = true)
                            val stages = listOf(
                                Triple(SystemParameterEntity.STAGE_PREGNANT_SOW, "Nái mang thai", "CAM_NAI_MANG_THAI"),
                                Triple(SystemParameterEntity.STAGE_LACTATING_SOW, "Nái nuôi con", "CAM_NAI_NUOI_CON"),
                                Triple(SystemParameterEntity.STAGE_WEANED_WAITING_SOW, "Nái chờ phối", "CAM_NAI_MANG_THAI"),
                                Triple(SystemParameterEntity.STAGE_SUCKLING_PIGLET, "Heo con theo mẹ", "CAM_HEO_CON_TAP_AN"),
                                Triple(SystemParameterEntity.STAGE_GROWER_FINISHER, "Heo thịt", "CAM_THIT_GD2")
                            )

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(stages) { (stCode, stLabel, defaultFeedCode) ->
                                    val isSelected = code == stCode
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) FarmGreenContainer else Color(0xFFF3F4F6),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) DialogBorderFocus else DialogBorderDefault
                                        ),
                                        modifier = Modifier.clickable {
                                            code = stCode
                                            name = stLabel
                                            subType = defaultFeedCode
                                            when (stCode) {
                                                SystemParameterEntity.STAGE_PREGNANT_SOW -> {
                                                    secondaryValue = "Cám nái bầu (CP 566)"
                                                    statusText = "Tuần 1 - 12"
                                                    numericValueText = "2.0"
                                                }
                                                SystemParameterEntity.STAGE_LACTATING_SOW -> {
                                                    secondaryValue = "Cám nái đẻ (CP 567)"
                                                    statusText = "Tuần 1 - 4"
                                                    numericValueText = "5.5"
                                                }
                                                SystemParameterEntity.STAGE_WEANED_WAITING_SOW -> {
                                                    secondaryValue = "Cám nái bầu (CP 566)"
                                                    statusText = "Cố định tuần 1"
                                                    numericValueText = "2.5"
                                                }
                                                SystemParameterEntity.STAGE_SUCKLING_PIGLET -> {
                                                    secondaryValue = "Cám heo con tập ăn (CP 550)"
                                                    statusText = "Cố định tuần 1"
                                                    numericValueText = "0.35"
                                                }
                                                SystemParameterEntity.STAGE_GROWER_FINISHER -> {
                                                    secondaryValue = "Cám vỗ béo heo thịt (CP 554)"
                                                    statusText = "Tuần 11 - 18"
                                                    numericValueText = "2.2"
                                                }
                                            }
                                        }
                                    ) {
                                        Text(
                                            text = stLabel,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) DialogSaveButtonBg else DialogInputTextColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Tuần tuổi / Tuần chu kỳ
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            val isFixedWeek = code == SystemParameterEntity.STAGE_WEANED_WAITING_SOW ||
                                code == SystemParameterEntity.STAGE_SUCKLING_PIGLET
                            ParameterFieldLabel(
                                title = if (isFixedWeek) "Tuần chu kỳ (Mặc định cố định):" else "Tuần tuổi / Tuần chu kỳ mang thai/nuôi con:",
                                isRequired = false
                            )
                            OutlinedTextField(
                                value = statusText,
                                onValueChange = { if (!isFixedWeek) statusText = it },
                                enabled = !isFixedWeek,
                                colors = parameterTextFieldColors(),
                                textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = DialogInputTextColor),
                                placeholder = { Text("Ví dụ: Tuần 1 - 12 hoặc Tuần 4", color = DialogPlaceholderColor, fontSize = 13.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // 3. Loại cám sử dụng
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            ParameterFieldLabel(title = "Loại cám sử dụng:", isRequired = true)
                            OutlinedTextField(
                                value = secondaryValue,
                                onValueChange = { secondaryValue = it },
                                colors = parameterTextFieldColors(),
                                textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = DialogInputTextColor),
                                placeholder = { Text("Cám nái bầu, Cám nái đẻ, Cám heo con...", color = DialogPlaceholderColor, fontSize = 13.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // 4. Định mức kg cám / ngày / con
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ParameterFieldLabel(title = "Định mức (kg/ngày/con):", isRequired = true)
                                OutlinedTextField(
                                    value = numericValueText,
                                    onValueChange = { numericValueText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                    colors = parameterTextFieldColors(),
                                    textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = DialogInputTextColor),
                                    placeholder = { Text("Ví dụ: 2.0", color = DialogPlaceholderColor, fontSize = 13.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ParameterFieldLabel(title = "Đơn vị:", isRequired = false)
                                OutlinedTextField(
                                    value = unit,
                                    onValueChange = { unit = it },
                                    colors = parameterTextFieldColors(),
                                    textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = DialogInputTextColor),
                                    placeholder = { Text("kg/ngày/con", color = DialogPlaceholderColor, fontSize = 13.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // Ghi chú / Mô tả kỹ thuật
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val descLabel = when (category) {
                        SystemParameterEntity.CATEGORY_MEDICINE -> "Liều lượng khuyên dùng / Hướng dẫn tiêm"
                        SystemParameterEntity.CATEGORY_BREED -> "Đặc điểm ngoại hình & Năng suất giống"
                        SystemParameterEntity.CATEGORY_BOAR -> "Ghi chú chất lượng tinh & Lịch sử phối"
                        SystemParameterEntity.CATEGORY_FEED_RATION -> "Ghi chú dinh dưỡng & Điều chỉnh thời tiết"
                        else -> "Ghi chú / Mô tả kỹ thuật"
                    }
                    ParameterFieldLabel(title = "$descLabel:", isRequired = false)
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        colors = parameterTextFieldColors(),
                        textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = DialogInputTextColor),
                        placeholder = { Text("Nhập ghi chú kỹ thuật...", color = DialogPlaceholderColor, fontSize = 13.sp) },
                        maxLines = 2,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Nút Hủy & Lưu với độ tương phản chuẩn WCAG
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, DialogCancelButtonContent),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = DialogCancelButtonContent
                        )
                    ) {
                        Text(
                            text = "Hủy",
                            fontWeight = FontWeight.Medium,
                            color = DialogCancelButtonContent
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                isNameError = true
                            } else {
                                val num = numericValueText.toDoubleOrNull() ?: 0.0
                                val safetyDays = safetyDaysThresholdText.toIntOrNull() ?: 3
                                val minStock = minStockThresholdText.toDoubleOrNull() ?: 20.0
                                onSave(
                                    name.trim(),
                                    code.ifBlank { name.take(8).uppercase() }.trim(),
                                    description.trim(),
                                    unit.trim(),
                                    subType.trim(),
                                    secondaryValue.trim(),
                                    num,
                                    statusText.trim(),
                                    safetyDays,
                                    minStock
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DialogSaveButtonBg,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Lưu tham số",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
