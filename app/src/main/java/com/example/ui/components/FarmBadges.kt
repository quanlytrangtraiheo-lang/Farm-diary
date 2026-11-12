package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SowReproductiveStatus
import com.example.data.model.SyncStatus
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import com.example.ui.theme.FarmAmber
import com.example.ui.theme.FarmAmberContainer
import com.example.ui.theme.FarmBlue
import com.example.ui.theme.FarmBlueContainer
import com.example.ui.theme.FarmGreenContainer
import com.example.ui.theme.FarmGreenPrimary
import com.example.ui.theme.FarmPurple
import com.example.ui.theme.FarmPurpleContainer
import com.example.ui.theme.FarmRed
import com.example.ui.theme.FarmRedContainer

@Composable
fun SyncStatusBadge(
    status: SyncStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, fgColor, icon, text) = when (status) {
        SyncStatus.SYNCED -> Quadruple(
            FarmGreenContainer,
            FarmGreenPrimary,
            Icons.Default.CloudDone,
            "Đã đồng bộ"
        )
        SyncStatus.PENDING_SYNC -> Quadruple(
            FarmAmberContainer,
            FarmAmber,
            Icons.Default.Cloud,
            "Chờ đồng bộ"
        )
        SyncStatus.SYNCING -> Quadruple(
            FarmBlueContainer,
            FarmBlue,
            Icons.Default.Cloud,
            "Đang gửi..."
        )
        SyncStatus.SYNC_FAILED -> Quadruple(
            FarmRedContainer,
            FarmRed,
            Icons.Default.Warning,
            "Cần xử lý"
        )
        SyncStatus.PENDING_DELETE -> Quadruple(
            FarmRedContainer,
            FarmRed,
            Icons.Default.Warning,
            "Chờ xóa"
        )
    }

    Row(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = fgColor,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            color = fgColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ReproductiveStatusBadge(
    status: SowReproductiveStatus,
    daysInStatus: Int? = null,
    modifier: Modifier = Modifier
) {
    val (bgColor, fgColor) = when (status) {
        SowReproductiveStatus.MANG_THAI -> Pair(FarmAmberContainer, FarmAmber)
        SowReproductiveStatus.NUOI_CON -> Pair(FarmGreenContainer, FarmGreenPrimary)
        SowReproductiveStatus.HAU_BI -> Pair(FarmBlueContainer, FarmBlue)
        SowReproductiveStatus.CAI_SUA_CHO_PHOI -> Pair(FarmPurpleContainer, FarmPurple)
        SowReproductiveStatus.LOAI_THAI -> Pair(FarmRedContainer, FarmRed)
        SowReproductiveStatus.DANG_NUOI -> Pair(FarmGreenContainer, FarmGreenPrimary)
    }

    val label = if (daysInStatus != null && daysInStatus > 0 && status == SowReproductiveStatus.MANG_THAI) {
        "${status.shortLabel} (D+$daysInStatus)"
    } else {
        status.shortLabel
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = fgColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TaskPriorityBadge(
    priority: TaskPriority,
    modifier: Modifier = Modifier
) {
    if (priority == TaskPriority.HIGH) {
        Box(
            modifier = modifier
                .background(FarmRedContainer, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = "Ưu tiên cao",
                color = FarmRed,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TaskStatusBadge(
    status: TaskStatus,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val (bgColor, fgColor, text) = when (status) {
        TaskStatus.TODO -> Triple(Color(0xFFE0E5E0), Color(0xFF556055), "Chưa làm")
        TaskStatus.IN_PROGRESS -> Triple(FarmBlue, Color.White, "Đang làm")
        TaskStatus.DONE -> Triple(FarmGreenPrimary, Color.White, "Đã xong")
        TaskStatus.OVERDUE -> Triple(FarmRed, Color.White, "Quá hạn")
    }

    Row(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (status == TaskStatus.DONE) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = fgColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = text,
            color = fgColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
