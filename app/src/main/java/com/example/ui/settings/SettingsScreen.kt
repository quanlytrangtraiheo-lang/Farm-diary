package com.example.ui.settings

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.SystemParameterEntity
import com.example.sync.SyncState
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

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToSyncLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    var showParameterDialog by remember { mutableStateOf(false) }
    var editingParameter by remember { mutableStateOf<SystemParameterEntity?>(null) }

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
                    text = "CÀI ĐẶT",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = FarmTextPrimary,
                    letterSpacing = 0.5.sp
                )
            }

            // User notification message banner if present
            if (state.userMessage != null) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = FarmGreenContainer
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = state.userMessage ?: "",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = FarmGreenPrimary
                            )
                            IconButton(
                                onClick = { viewModel.clearUserMessage() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Đóng",
                                    tint = FarmGreenPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Section: TÀI KHOẢN (PDF Page 14)
            item {
                Text(
                    text = "TÀI KHOẢN",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = FarmTextSecondary
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_account_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(FarmGreenContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = FarmGreenPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = state.userProfile.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FarmTextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(FarmBlueContainer)
                                        .clickable { viewModel.switchRole() }
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "${state.userProfile.role.label} (chạm để đổi)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FarmBlue
                                    )
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Đăng xuất",
                                tint = FarmRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Đăng xuất",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = FarmRed
                            )
                        }
                    }
                }
            }

            // Section: THAM SỐ HỆ THỐNG (Chuồng nuôi, Nguyên nhân loại thải, Vaccine)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "THAM SỐ HỆ THỐNG",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = FarmTextSecondary
                    )

                    Text(
                        text = "Quản lý danh mục cơ sở",
                        fontSize = 11.sp,
                        color = FarmGreenPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_system_parameters_card"),
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
                        // Swipeable / Horizontal Scrollable Tabs: 6 Tiểu mục tham số dùng chung + Chuồng nuôi + Loại thải + Khẩu phần cám
                        val paramTabs = listOf(
                            Triple(SystemParameterEntity.CATEGORY_BOAR, "Heo đực & Tinh", Icons.Default.Pets),
                            Triple(SystemParameterEntity.CATEGORY_STAFF, "Nhân viên", Icons.Default.People),
                            Triple(SystemParameterEntity.CATEGORY_BREED, "Giống heo", Icons.Default.Fingerprint),
                            Triple(SystemParameterEntity.CATEGORY_FEED, "Thức ăn", Icons.Default.Inventory2),
                            Triple(SystemParameterEntity.CATEGORY_FEED_RATION, "Khẩu phần cám", Icons.Default.Tune),
                            Triple(SystemParameterEntity.CATEGORY_MEDICINE, "Thuốc", Icons.Default.MedicalServices),
                            Triple(SystemParameterEntity.CATEGORY_SUPPLIES, "Vật tư", Icons.Default.Build),
                            Triple(SystemParameterEntity.CATEGORY_BARN, "Chuồng nuôi", Icons.Default.Home),
                            Triple(SystemParameterEntity.CATEGORY_CULLING_REASON, "Loại thải", Icons.Default.Warning)
                        )

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(paramTabs) { (catKey, catLabel, catIcon) ->
                                val isSelected = state.selectedParameterTab == catKey
                                val count = when (catKey) {
                                    SystemParameterEntity.CATEGORY_BOAR -> state.boarParameters.size
                                    SystemParameterEntity.CATEGORY_STAFF -> state.staffParameters.size
                                    SystemParameterEntity.CATEGORY_BREED -> state.breedParameters.size
                                    SystemParameterEntity.CATEGORY_FEED -> state.feedParameters.size
                                    SystemParameterEntity.CATEGORY_FEED_RATION -> state.feedRationParameters.size
                                    SystemParameterEntity.CATEGORY_MEDICINE -> state.medicineParameters.size
                                    SystemParameterEntity.CATEGORY_SUPPLIES -> state.suppliesParameters.size
                                    SystemParameterEntity.CATEGORY_BARN -> state.barnParameters.size
                                    SystemParameterEntity.CATEGORY_CULLING_REASON -> state.cullingParameters.size
                                    else -> 0
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) FarmGreenContainer else FarmBackground)
                                        .border(
                                            1.dp,
                                            if (isSelected) FarmGreenPrimary else FarmOutline,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { viewModel.selectParameterTab(catKey) }
                                        .padding(vertical = 10.dp, horizontal = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = catIcon,
                                            contentDescription = null,
                                            tint = if (isSelected) FarmGreenPrimary else FarmTextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = catLabel,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) FarmGreenPrimary else FarmTextPrimary
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) FarmGreenPrimary else Color.White
                                        ) {
                                            Text(
                                                text = "$count",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else FarmTextSecondary,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Thanh tìm kiếm nhanh danh mục
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            placeholder = { Text("Tìm kiếm mã, tên, giống, vai trò...", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = FarmTextSecondary, modifier = Modifier.size(18.dp))
                            },
                            trailingIcon = {
                                if (state.searchQuery.isNotBlank()) {
                                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = "Xóa", tint = FarmTextSecondary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = FarmBackground,
                                unfocusedContainerColor = FarmBackground
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        )

                        HorizontalDivider(color = FarmBackground)

                        // Header của nhóm tham số được chọn + Nút Thêm mới
                        val (currentCategoryTitle, currentList) = when (state.selectedParameterTab) {
                            SystemParameterEntity.CATEGORY_BOAR -> Pair("Danh mục Heo đực & Tinh giống", state.boarParameters)
                            SystemParameterEntity.CATEGORY_STAFF -> Pair("Danh mục Nhân viên & Kỹ thuật viên", state.staffParameters)
                            SystemParameterEntity.CATEGORY_BREED -> Pair("Danh mục Giống heo nuôi trại", state.breedParameters)
                            SystemParameterEntity.CATEGORY_FEED -> Pair("Danh mục Thức ăn & Cám", state.feedParameters)
                            SystemParameterEntity.CATEGORY_FEED_RATION -> Pair("Định mức Khẩu phần cám hàng ngày", state.feedRationParameters)
                            SystemParameterEntity.CATEGORY_MEDICINE -> Pair("Danh mục Thuốc & Vắc-xin", state.medicineParameters)
                            SystemParameterEntity.CATEGORY_SUPPLIES -> Pair("Danh mục Vật tư & Dụng cụ", state.suppliesParameters)
                            SystemParameterEntity.CATEGORY_BARN -> Pair("Danh mục Chuồng nuôi", state.barnParameters)
                            SystemParameterEntity.CATEGORY_CULLING_REASON -> Pair("Danh mục Nguyên nhân loại thải", state.cullingParameters)
                            else -> Pair("Tham số hệ thống", emptyList())
                        }

                        val filteredList = currentList.filter { item ->
                            val q = state.searchQuery.trim()
                            q.isBlank() ||
                                item.name.contains(q, ignoreCase = true) ||
                                item.code.contains(q, ignoreCase = true) ||
                                item.subType.contains(q, ignoreCase = true) ||
                                item.secondaryValue.contains(q, ignoreCase = true) ||
                                item.description.contains(q, ignoreCase = true)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = currentCategoryTitle,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FarmTextPrimary
                                )
                                Text(
                                    text = "Hiển thị ${filteredList.size}/${currentList.size} mục",
                                    fontSize = 11.sp,
                                    color = FarmTextSecondary
                                )
                            }

                            Button(
                                onClick = {
                                    editingParameter = null
                                    showParameterDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = FarmGreenPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .testTag("btn_add_system_parameter")
                                    .height(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Thêm mới", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (filteredList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (state.searchQuery.isNotBlank()) "Không tìm thấy kết quả phù hợp với '${state.searchQuery}'" else "Chưa có dữ liệu cho mục này. Nhấn 'Thêm mới' để nhập.",
                                    fontSize = 13.sp,
                                    color = FarmTextSecondary
                                )
                            }
                        } else if (state.selectedParameterTab == SystemParameterEntity.CATEGORY_FEED_RATION) {
                            // Bảng dữ liệu Khẩu phần cám tối ưu ngoài chuồng: Cố định cột Loại heo, cuộn ngang các cột còn lại
                            FeedRationMatrixTableView(
                                rationList = filteredList,
                                onEdit = {
                                    editingParameter = it
                                    showParameterDialog = true
                                },
                                onDelete = { viewModel.deleteSystemParameter(it) }
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                filteredList.forEach { param ->
                                    when (state.selectedParameterTab) {
                                        SystemParameterEntity.CATEGORY_BOAR -> {
                                            BoarMasterCard(
                                                parameter = param,
                                                onEdit = {
                                                    editingParameter = param
                                                    showParameterDialog = true
                                                },
                                                onDelete = { viewModel.deleteSystemParameter(param) },
                                                onDoseDelta = { delta -> viewModel.updateBoarDoseStock(param, delta) }
                                            )
                                        }
                                        SystemParameterEntity.CATEGORY_STAFF -> {
                                            StaffMasterCard(
                                                parameter = param,
                                                onEdit = {
                                                    editingParameter = param
                                                    showParameterDialog = true
                                                },
                                                onDelete = { viewModel.deleteSystemParameter(param) }
                                            )
                                        }
                                        SystemParameterEntity.CATEGORY_BREED -> {
                                            BreedMasterCard(
                                                parameter = param,
                                                onEdit = {
                                                    editingParameter = param
                                                    showParameterDialog = true
                                                },
                                                onDelete = { viewModel.deleteSystemParameter(param) }
                                            )
                                        }
                                        SystemParameterEntity.CATEGORY_FEED -> {
                                            FeedMasterCard(
                                                parameter = param,
                                                onEdit = {
                                                    editingParameter = param
                                                    showParameterDialog = true
                                                },
                                                onDelete = { viewModel.deleteSystemParameter(param) }
                                            )
                                        }
                                        SystemParameterEntity.CATEGORY_MEDICINE -> {
                                            MedicineMasterCard(
                                                parameter = param,
                                                onEdit = {
                                                    editingParameter = param
                                                    showParameterDialog = true
                                                },
                                                onDelete = { viewModel.deleteSystemParameter(param) }
                                            )
                                        }
                                        SystemParameterEntity.CATEGORY_FEED_RATION -> {
                                            FeedRationMasterCard(
                                                parameter = param,
                                                onEdit = {
                                                    editingParameter = param
                                                    showParameterDialog = true
                                                },
                                                onDelete = { viewModel.deleteSystemParameter(param) }
                                            )
                                        }
                                        SystemParameterEntity.CATEGORY_SUPPLIES -> {
                                            SupplyMasterCard(
                                                parameter = param,
                                                onEdit = {
                                                    editingParameter = param
                                                    showParameterDialog = true
                                                },
                                                onDelete = { viewModel.deleteSystemParameter(param) }
                                            )
                                        }
                                        else -> {
                                            BarnAndCullMasterCard(
                                                parameter = param,
                                                onEdit = {
                                                    editingParameter = param
                                                    showParameterDialog = true
                                                },
                                                onDelete = { viewModel.deleteSystemParameter(param) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section: ĐỒNG BỘ & SAO LƯU (PDF Page 14)
            item {
                Text(
                    text = "ĐỒNG BỘ & SAO LƯU",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = FarmTextSecondary
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_sync_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Toggle Tự động đồng bộ (Page 14)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tự động đồng bộ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = FarmTextPrimary
                            )
                            Switch(
                                checked = state.isAutoSync,
                                onCheckedChange = { viewModel.toggleAutoSync(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = FarmGreenPrimary,
                                    checkedTrackColor = FarmGreenContainer
                                ),
                                modifier = Modifier.testTag("settings_switch_autosync")
                            )
                        }

                        // Phím Đồng bộ ngay kích thước lớn (Page 14)
                        Button(
                            onClick = { viewModel.triggerSync() },
                            colors = ButtonDefaults.buttonColors(containerColor = FarmGreenPrimary),
                            shape = RoundedCornerShape(14.dp),
                            enabled = state.syncState !is SyncState.Syncing,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("settings_btn_sync_now")
                        ) {
                            if (state.syncState is SyncState.Syncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Đang đồng bộ dữ liệu...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Đồng bộ ngay", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Text(
                            text = "Lần cuối: 10:30 Hôm nay (Cơ chế Last-Write-Wins & Role-based)",
                            fontSize = 12.sp,
                            color = FarmTextSecondary,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        // Nút Sao lưu dữ liệu & Nhật ký đồng bộ (Page 14)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onNavigateToSyncLogs,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("settings_btn_sync_logs")
                            ) {
                                Text("Nhật ký đồng bộ", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { /* Export local backup file */ },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text("Sao lưu dữ liệu", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Section: HỆ THỐNG & TÙY CHỌN (PDF Page 14)
            item {
                Text(
                    text = "HỆ THỐNG & TÙY CHỌN",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = FarmTextSecondary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingsRowItem(
                            icon = Icons.Default.Security,
                            title = "Quyền & Người dùng",
                            subtitle = "Phân công tổ kỹ thuật và quản lý"
                        )
                        SettingsRowItem(
                            icon = Icons.Default.Language,
                            title = "Tùy chọn ứng dụng",
                            subtitle = "Ngôn ngữ, Giao diện (High Contrast)"
                        )
                        SettingsRowItem(
                            icon = Icons.Default.Info,
                            title = "Hỗ trợ & Thông tin",
                            subtitle = "Phiên bản v1.2.0 (Offline-First Edition)",
                            showDivider = false
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // Hộp thoại Thêm/Sửa Tham số Hệ Thống theo từng tiểu mục (Subtab)
        if (showParameterDialog) {
            MasterDataInputDialog(
                category = state.selectedParameterTab,
                initialParameter = editingParameter,
                onDismiss = { showParameterDialog = false },
                onSave = { name, code, desc, unit, subType, secondaryValue, numVal, statusText, safetyDays, minStock ->
                    if (editingParameter != null) {
                        viewModel.updateSystemParameter(
                            editingParameter!!.copy(
                                name = name,
                                code = code,
                                description = desc,
                                unit = unit,
                                subType = subType,
                                secondaryValue = secondaryValue,
                                numericValue = numVal,
                                statusText = statusText,
                                safetyDaysThreshold = safetyDays,
                                minStockThreshold = minStock
                            )
                        )
                    } else {
                        viewModel.addSystemParameter(
                            category = state.selectedParameterTab,
                            name = name,
                            code = code,
                            description = desc,
                            unit = unit,
                            subType = subType,
                            secondaryValue = secondaryValue,
                            numericValue = numVal,
                            statusText = statusText,
                            safetyDaysThreshold = safetyDays,
                            minStockThreshold = minStock
                        )
                    }
                    showParameterDialog = false
                }
            )
        }
    }
}

@Composable
fun SettingsRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    showDivider: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp) // Minimum touch target >= 48dp
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = FarmGreenPrimary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FarmTextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = FarmTextSecondary
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFFA5B2A5)
        )
    }
}
