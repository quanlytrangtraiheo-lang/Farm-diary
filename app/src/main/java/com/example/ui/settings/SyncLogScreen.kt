package com.example.ui.settings

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
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import com.example.data.local.entity.SyncLogEntity
import com.example.sync.SyncState
import com.example.ui.components.SyncStatusBadge
import com.example.ui.theme.FarmBackground
import com.example.ui.theme.FarmBlue
import com.example.ui.theme.FarmGreenContainer
import com.example.ui.theme.FarmGreenPrimary
import com.example.ui.theme.FarmOutline
import com.example.ui.theme.FarmRed
import com.example.ui.theme.FarmTextPrimary
import com.example.ui.theme.FarmTextSecondary

@Composable
fun SyncLogScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val state by viewModel.uiState.collectAsState()
    var selectedConflictLog by remember { mutableStateOf<SyncLogEntity?>(null) }

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
            // Header Bar (PDF Page 22)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .testTag("sync_log_back_button")
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
                        text = "NHẬT KÝ ĐỒNG BỘ",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = FarmTextPrimary
                    )
                }
            }

            // Card: TRẠNG THÁI HIỆN TẠI (PDF Page 22)
            item {
                Text(
                    text = "TRẠNG THÁI HIỆN TẠI",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = FarmTextSecondary
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sync_current_status_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(FarmGreenContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = FarmGreenPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (state.isOnline) "Đã kết nối" else "Ngoại tuyến (Offline)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.isOnline) FarmGreenPrimary else FarmRed
                                )
                                Text(
                                    text = if (state.isAutoSync) "Đang tự động đồng bộ ngầm" else "Đang tắt tự động đồng bộ",
                                    fontSize = 13.sp,
                                    color = FarmTextSecondary
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Lần đồng bộ cuối: 10:45 Hôm nay",
                                fontSize = 13.sp,
                                color = FarmTextSecondary
                            )

                            Button(
                                onClick = { viewModel.triggerSync() },
                                colors = ButtonDefaults.buttonColors(containerColor = FarmGreenContainer),
                                shape = RoundedCornerShape(12.dp),
                                enabled = state.syncState !is SyncState.Syncing,
                                modifier = Modifier.testTag("sync_btn_sync_now")
                            ) {
                                if (state.syncState is SyncState.Syncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = FarmGreenPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = null,
                                        tint = FarmGreenPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = "Đồng bộ ngay",
                                    color = FarmGreenPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Progress indicator if syncing
                        if (state.syncState is SyncState.Syncing) {
                            val syncingState = state.syncState as SyncState.Syncing
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = syncingState.message,
                                    fontSize = 12.sp,
                                    color = FarmBlue
                                )
                            }
                        }
                    }
                }
            }

            // Section: LỊCH SỬ ĐỒNG BỘ - DANH SÁCH THAY ĐỔI (PDF Page 22)
            item {
                Column {
                    Text(
                        text = "LỊCH SỬ ĐỒNG BỘ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = FarmTextSecondary
                    )
                    Text(
                        text = "DANH SÁCH THAY ĐỔI",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = FarmTextPrimary
                    )
                }
            }

            items(state.syncLogs) { log ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sync_log_item_${log.id}")
                        .clickable {
                            if (log.hasConflict) {
                                selectedConflictLog = log
                            }
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = log.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = FarmTextPrimary
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "${log.timeFormatted} - ${log.subDetail}",
                                fontSize = 13.sp,
                                color = FarmTextSecondary
                            )
                            if (log.hasConflict && log.conflictDetail != null) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Nhấn để giải quyết xung đột",
                                    fontSize = 11.sp,
                                    color = FarmRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        SyncStatusBadge(status = log.syncStatus)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // Conflict Resolution Dialog (PDF Page 20)
        selectedConflictLog?.let { conflictLog ->
            AlertDialog(
                onDismissRequest = { selectedConflictLog = null },
                title = {
                    Text(
                        text = "Giải Quyết Xung Đột Dữ Liệu",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Bản ghi: ${conflictLog.title}",
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Chi tiết: ${conflictLog.conflictDetail ?: "Có sự khác biệt ngày siêu âm thai giữa Quản lý và Kỹ thuật viên."}",
                            fontSize = 13.sp,
                            color = FarmTextSecondary
                        )
                        Text(
                            text = "Chiến lược Role-Based & Last-Write-Wins: Bạn đang đăng nhập với quyền ${state.userProfile.role.label}. Bạn có muốn phê duyệt và ghi đè bản mới nhất lên máy chủ?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = FarmGreenPrimary
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.resolveConflict(conflictLog.id)
                            selectedConflictLog = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FarmGreenPrimary)
                    ) {
                        Text("Phê duyệt & Đồng bộ")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { selectedConflictLog = null }) {
                        Text("Đóng")
                    }
                }
            )
        }
    }
}
