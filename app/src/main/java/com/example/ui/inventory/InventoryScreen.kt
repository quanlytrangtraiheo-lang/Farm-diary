package com.example.ui.inventory

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.InventoryItemEntity
import com.example.data.local.entity.InventoryTransactionEntity
import com.example.data.local.entity.SystemParameterEntity
import com.example.ui.components.OutdoorInputField
import com.example.ui.components.PickerOption
import com.example.ui.components.QuickPickerBottomSheet
import com.example.ui.components.QuickPickerField
import com.example.ui.pig.formatNumberWithSeparator
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
import com.example.ui.theme.LocalOutdoorMode
import com.example.ui.theme.OutdoorBorder
import com.example.ui.theme.OutdoorCardBorder
import com.example.ui.theme.OutdoorDivider
import com.example.ui.theme.OutdoorFocusBorder
import com.example.ui.theme.OutdoorInputFill
import com.example.ui.theme.OutdoorLabelText
import com.example.ui.theme.OutdoorModeManager
import com.example.ui.theme.OutdoorOverallBackground
import com.example.ui.theme.OutdoorPlaceholder
import com.example.ui.theme.OutdoorRequiredStar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var showAddItemDialog by remember { mutableStateOf(false) }
    var presetInboundItem by remember { mutableStateOf<MasterInventoryItemUi?>(null) }
    var presetOutboundItem by remember { mutableStateOf<MasterInventoryItemUi?>(null) }
    val isOutdoor = LocalOutdoorMode.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isOutdoor) OutdoorOverallBackground else FarmBackground)
            .testTag("inventory_screen")
    ) {
        // Thông báo Toast / Banner
        state.userMessage?.let { msg ->
            Surface(
                color = if (msg.contains("⚠️")) FarmAmberContainer else FarmGreenContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = msg,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (msg.contains("⚠️")) Color(0xFFE65100) else FarmGreenPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { viewModel.clearUserMessage() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng", tint = FarmTextSecondary)
                    }
                }
            }
        }

        // Header Bar: Tiêu đề & Nút Thêm Danh mục Hàng hóa
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "KHO VẬT TƯ & THỨC ĂN",
                    fontSize = OutdoorModeManager.scaledSp(20f, isOutdoor),
                    fontWeight = FontWeight.Black,
                    color = OutdoorLabelText,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Liên kết Tham số hệ thống • Cập nhật tự động thời gian thực",
                    fontSize = OutdoorModeManager.scaledSp(12f, isOutdoor),
                    color = OutdoorPlaceholder,
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                onClick = { showAddItemDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = OutdoorFocusBorder),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .height(44.dp)
                    .testTag("inventory_add_item_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Thêm Mặt Hàng", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Subtabs Navigation: [Tồn kho] - [Nhập kho] - [Xuất kho]
        TabRow(
            selectedTabIndex = state.selectedSubTab.ordinal,
            containerColor = OutdoorInputFill,
            contentColor = OutdoorFocusBorder,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[state.selectedSubTab.ordinal]),
                    color = OutdoorFocusBorder,
                    height = 3.dp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (isOutdoor) 2.dp else 1.dp,
                    color = if (isOutdoor) OutdoorCardBorder else OutdoorDivider
                )
        ) {
            InventorySubTab.values().forEach { subTab ->
                val isSelected = state.selectedSubTab == subTab
                Tab(
                    selected = isSelected,
                    onClick = { viewModel.selectSubTab(subTab) },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = when (subTab) {
                                    InventorySubTab.STOCK -> Icons.Default.Inventory2
                                    InventorySubTab.INBOUND -> Icons.Default.ArrowDownward
                                    InventorySubTab.OUTBOUND -> Icons.Default.ArrowUpward
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) OutdoorFocusBorder else OutdoorPlaceholder
                            )
                            Text(
                                text = subTab.label,
                                fontSize = OutdoorModeManager.scaledSp(14f, isOutdoor),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) OutdoorFocusBorder else OutdoorPlaceholder
                            )
                        }
                    },
                    modifier = Modifier.testTag("inventory_subtab_${subTab.name.lowercase()}")
                )
            }
        }

        // Subtab Content
        when (state.selectedSubTab) {
            InventorySubTab.STOCK -> {
                StockListSubtab(
                    state = state,
                    onCategoryFilterSelected = { viewModel.selectCategoryFilter(it) },
                    onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
                    onQuickInbound = { item ->
                        presetInboundItem = item
                        viewModel.selectSubTab(InventorySubTab.INBOUND)
                    },
                    onQuickOutbound = { item ->
                        presetOutboundItem = item
                        viewModel.selectSubTab(InventorySubTab.OUTBOUND)
                    }
                )
            }

            InventorySubTab.INBOUND -> {
                InboundFormSubtab(
                    masterItems = state.masterLinkedItems,
                    presetItem = presetInboundItem,
                    onSaveInbound = { itemId, qty, unitPrice, date, supplier, notes ->
                        viewModel.recordInbound(itemId, qty, unitPrice, date, supplier, notes)
                        presetInboundItem = null
                    }
                )
            }

            InventorySubTab.OUTBOUND -> {
                OutboundFormSubtab(
                    masterItems = state.masterLinkedItems,
                    barns = state.barns,
                    presetItem = presetOutboundItem,
                    onSaveOutbound = { itemId, qty, targetBarn, date, notes ->
                        viewModel.recordOutbound(itemId, qty, targetBarn, date, notes)
                        presetOutboundItem = null
                    }
                )
            }
        }
    }

    if (showAddItemDialog) {
        AddNewItemDialog(
            onDismiss = { showAddItemDialog = false },
            onConfirm = { name, cat, unit, initialStock, unitPrice, minAlert, notes ->
                viewModel.addNewItem(name, cat, unit, initialStock, unitPrice, minAlert, notes)
                showAddItemDialog = false
            }
        )
    }
}

// =============================================================================
// SUBTAB A: BẢNG TỔN KHO HIỆN THỜI (Ghép Master Data với Real-time Stock)
// =============================================================================
@Composable
private fun StockListSubtab(
    state: InventoryUiState,
    onCategoryFilterSelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onQuickInbound: (MasterInventoryItemUi) -> Unit,
    onQuickOutbound: (MasterInventoryItemUi) -> Unit
) {
    val isOutdoor = LocalOutdoorMode.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Thẻ Thống kê Tổng quan (Tài chính & Cảnh báo an toàn)
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tổng giá trị tồn kho
                Card(
                    modifier = Modifier.weight(1.5f),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = OutdoorInputFill),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isOutdoor) 2.dp else 1.5.dp,
                        color = if (isOutdoor) OutdoorCardBorder else OutdoorBorder
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isOutdoor) 0.dp else 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "TỔNG GIÁ TRỊ TỒN KHO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = OutdoorPlaceholder
                        )
                        Text(
                            text = InventoryViewModel.formatCurrency(state.totalStockValue),
                            fontSize = OutdoorModeManager.scaledSp(18f, isOutdoor),
                            fontWeight = FontWeight.Black,
                            color = OutdoorFocusBorder
                        )
                        Text(
                            text = "∑ (Số lượng tồn × Đơn giá)",
                            fontSize = 11.sp,
                            color = OutdoorPlaceholder
                        )
                    }
                }

                // Cảnh báo an toàn
                Card(
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.emergencyCount > 0) FarmRedContainer else if (state.warningCount > 0) FarmAmberContainer else OutdoorInputFill
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isOutdoor) 2.dp else 1.5.dp,
                        color = if (state.emergencyCount > 0) FarmRed else if (state.warningCount > 0) FarmAmber else OutdoorBorder
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isOutdoor) 0.dp else 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "CẢNH BÁO TỒN KHO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (state.emergencyCount > 0) FarmRed else OutdoorPlaceholder
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${state.emergencyCount + state.warningCount}",
                                fontSize = OutdoorModeManager.scaledSp(20f, isOutdoor),
                                fontWeight = FontWeight.Black,
                                color = if (state.emergencyCount > 0) FarmRed else FarmAmber
                            )
                            Text(
                                text = " mặt hàng",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = OutdoorLabelText
                            )
                        }
                        Text(
                            text = if (state.emergencyCount > 0) "${state.emergencyCount} mã khẩn cấp" else if (state.warningCount > 0) "${state.warningCount} mã sắp hết" else "✓ Đủ tồn an toàn",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (state.emergencyCount > 0) FarmRed else if (state.warningCount > 0) FarmAmber else FarmGreenPrimary
                        )
                    }
                }
            }
        }

        // 2. Bộ lọc Loại hàng hóa (Tất cả, Cám, Thuốc, Vật tư)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filterCategories = listOf(
                    "ALL" to "Tất cả (${state.masterLinkedItems.size})",
                    InventoryItemEntity.CATEGORY_FEED to "Cám (${state.masterLinkedItems.count { it.category == InventoryItemEntity.CATEGORY_FEED }})",
                    InventoryItemEntity.CATEGORY_MEDICINE to "Thuốc (${state.masterLinkedItems.count { it.category == InventoryItemEntity.CATEGORY_MEDICINE }})",
                    InventoryItemEntity.CATEGORY_SUPPLIES to "Vật tư (${state.masterLinkedItems.count { it.category == InventoryItemEntity.CATEGORY_SUPPLIES }})"
                )

                filterCategories.forEach { (catKey, catLabel) ->
                    val isSelected = state.selectedCategoryFilter == catKey
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) OutdoorFocusBorder else OutdoorInputFill,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 2.dp else 1.5.dp,
                            color = if (isSelected) OutdoorFocusBorder else OutdoorBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onCategoryFilterSelected(catKey) }
                            .testTag("filter_category_$catKey")
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = catLabel,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isSelected) Color.White else OutdoorLabelText
                            )
                        }
                    }
                }
            }
        }

        // 3. Ô Tìm kiếm nhanh theo Mã hàng hóa hoặc Tên hàng
        item {
            OutdoorInputField(
                label = "Tìm kiếm trong kho",
                value = state.searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = "Nhập tên hàng (vd: Cám nái, Amoxicillin) hoặc mã...",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = OutdoorLabelText,
                        modifier = Modifier.size(22.dp)
                    )
                },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Xóa", tint = OutdoorLabelText)
                        }
                    }
                },
                testTag = "inventory_search_input"
            )
        }

        // 4. Danh sách Mặt hàng Tồn kho
        if (state.filteredMasterItems.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Không tìm thấy mặt hàng nào phù hợp",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OutdoorPlaceholder
                    )
                }
            }
        } else {
            items(state.filteredMasterItems, key = { it.masterId }) { item ->
                MasterStockItemRow(
                    item = item,
                    onInboundClick = { onQuickInbound(item) },
                    onOutboundClick = { onQuickOutbound(item) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Thẻ hiển thị một mặt hàng tồn kho kết nối với Master Data.
 */
@Composable
private fun MasterStockItemRow(
    item: MasterInventoryItemUi,
    onInboundClick: () -> Unit,
    onOutboundClick: () -> Unit
) {
    val isOutdoor = LocalOutdoorMode.current

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = OutdoorInputFill),
        border = androidx.compose.foundation.BorderStroke(
            width = if (item.isEmergency) 2.5.dp else if (item.isWarning) 2.dp else if (isOutdoor) 2.dp else 1.5.dp,
            color = when {
                item.isEmergency -> FarmRed
                item.isWarning -> FarmAmber
                isOutdoor -> OutdoorCardBorder
                else -> OutdoorBorder
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isOutdoor) 0.dp else 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("stock_item_${item.masterId}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Hàng Header: Mã Master Data + Tên hàng + Badge Cảnh báo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Icon phân loại
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when (item.category) {
                                    InventoryItemEntity.CATEGORY_FEED -> Color(0xFFFFF3E0)
                                    InventoryItemEntity.CATEGORY_MEDICINE -> Color(0xFFE8F5E9)
                                    else -> Color(0xFFE3F2FD)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (item.category) {
                                InventoryItemEntity.CATEGORY_FEED -> Icons.Default.Grain
                                InventoryItemEntity.CATEGORY_MEDICINE -> Icons.Default.Medication
                                else -> Icons.Default.Build
                            },
                            contentDescription = null,
                            tint = when (item.category) {
                                InventoryItemEntity.CATEGORY_FEED -> FarmAmber
                                InventoryItemEntity.CATEGORY_MEDICINE -> FarmGreenPrimary
                                else -> OutdoorFocusBorder
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "[${item.masterCode}]",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = OutdoorFocusBorder
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = InventoryItemEntity.getCategoryLabel(item.category),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = OutdoorPlaceholder
                            )
                        }
                        Text(
                            text = item.name,
                            fontSize = OutdoorModeManager.scaledSp(15f, isOutdoor),
                            fontWeight = FontWeight.Bold,
                            color = OutdoorLabelText
                        )
                    }
                }

                // Trạng thái cảnh báo tồn kho
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when {
                        item.isEmergency -> FarmRedContainer
                        item.isWarning -> FarmAmberContainer
                        else -> FarmGreenContainer
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = when {
                            item.isEmergency -> FarmRed
                            item.isWarning -> FarmAmber
                            else -> FarmGreenPrimary
                        }
                    )
                ) {
                    Text(
                        text = when {
                            item.isEmergency -> "⚠️ KHẨN CẤP"
                            item.isWarning -> "SẮP HẾT"
                            else -> "✓ AN TOÀN"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            item.isEmergency -> FarmRed
                            item.isWarning -> Color(0xFFD84315)
                            else -> FarmGreenPrimary
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(thickness = 1.dp, color = OutdoorDivider)

            // Thông số Số lượng tồn, Đơn vị tính, Thành tiền
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Số lượng tồn hiện thời:", fontSize = 12.sp, color = OutdoorPlaceholder)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${item.currentStock}",
                            fontSize = OutdoorModeManager.scaledSp(20f, isOutdoor),
                            fontWeight = FontWeight.Black,
                            color = if (item.isEmergency) FarmRed else OutdoorLabelText
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.unit,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = OutdoorLabelText,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                    Text(
                        text = "Mức an toàn tối thiểu: ${item.minStockThreshold} ${item.unit}",
                        fontSize = 11.sp,
                        color = OutdoorPlaceholder
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Giá trị tồn kho:", fontSize = 12.sp, color = OutdoorPlaceholder)
                    Text(
                        text = InventoryViewModel.formatCurrency(item.totalStockValue),
                        fontSize = OutdoorModeManager.scaledSp(16f, isOutdoor),
                        fontWeight = FontWeight.Bold,
                        color = OutdoorFocusBorder
                    )
                    Text(
                        text = "ĐG: ${InventoryViewModel.formatCurrency(item.lastUnitPrice)}",
                        fontSize = 11.sp,
                        color = OutdoorPlaceholder
                    )
                }
            }

            // Đối với Cám (FEED): Hiển thị dự báo số ngày sử dụng còn lại
            if (item.category == InventoryItemEntity.CATEGORY_FEED && item.daysRemaining != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (item.daysRemaining <= item.safetyDaysThreshold) Color(0xFFFFF8E1) else Color(0xFFF1F8E9),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (item.daysRemaining <= item.safetyDaysThreshold) FarmAmber else FarmGreenPrimary.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Grain,
                                contentDescription = null,
                                tint = if (item.daysRemaining <= item.safetyDaysThreshold) FarmAmber else FarmGreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Dự báo cám tồn: ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = OutdoorLabelText
                            )
                            Text(
                                text = "Đủ dùng ~${item.daysRemaining} ngày",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = if (item.daysRemaining <= 1) FarmRed else if (item.daysRemaining <= item.safetyDaysThreshold) Color(0xFFD84315) else FarmGreenPrimary
                            )
                        }

                        Text(
                            text = "Ngưỡng an toàn: ${item.safetyDaysThreshold} ngày",
                            fontSize = 11.sp,
                            color = OutdoorPlaceholder,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Hàng nút thao tác nhanh: Nhập kho & Xuất kho
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onInboundClick,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, OutdoorFocusBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = OutdoorFocusBorder, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nhập Kho", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OutdoorFocusBorder)
                }

                Button(
                    onClick = onOutboundClick,
                    enabled = item.currentStock > 0f,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FarmAmber,
                        disabledContainerColor = Color(0xFFE2E8F0)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (item.currentStock > 0f) "Xuất Kho" else "Hết Hàng",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// =============================================================================
// SUBTAB B: LẬP PHIẾU NHẬP KHO (INBOUND)
// - Truy vấn danh mục từ Tham số hệ thống
// - Tự động điền Đơn vị tính và gợi ý đơn giá tham chiếu
// - Tự động tính Thành tiền
// - Cập nhật ngược lại Tồn kho và Đơn vị tham chiếu trong Tham số hệ thống
// =============================================================================
@Composable
private fun InboundFormSubtab(
    masterItems: List<MasterInventoryItemUi>,
    presetItem: MasterInventoryItemUi? = null,
    onSaveInbound: (itemId: String, qty: Float, unitPrice: Long, date: Long, supplier: String, notes: String) -> Unit
) {
    val isOutdoor = LocalOutdoorMode.current
    var selectedCategory by remember { mutableStateOf(presetItem?.category ?: InventoryItemEntity.CATEGORY_FEED) }

    val categoryFilteredItems = remember(masterItems, selectedCategory) {
        masterItems.filter { it.category == selectedCategory }
    }

    var selectedItem by remember(categoryFilteredItems, presetItem) {
        mutableStateOf(presetItem ?: categoryFilteredItems.firstOrNull())
    }

    val todayStr = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }
    var inboundDateStr by remember { mutableStateOf(todayStr) }
    var quantityStr by remember { mutableStateOf("50") }
    var unitPriceStr by remember(selectedItem) {
        mutableStateOf(selectedItem?.defaultPrice?.toString() ?: selectedItem?.lastUnitPrice?.toString() ?: "320000")
    }
    var supplierOrInvoice by remember { mutableStateOf("Công ty CP Chăn Nuôi Việt Nam") }
    var notes by remember { mutableStateOf("Nhập bổ sung định kỳ tuần") }

    var showItemPicker by remember { mutableStateOf(false) }

    val quantity = quantityStr.toFloatOrNull() ?: 0f
    val unitPrice = unitPriceStr.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val totalPrice = (quantity * unitPrice).toLong()

    val itemPickerOptions = remember(categoryFilteredItems) {
        categoryFilteredItems.map { itm ->
            PickerOption(
                id = itm.masterId,
                title = "${itm.name} [${itm.masterCode}]",
                subtitle = "Tồn hiện tại: ${itm.currentStock} ${itm.unit} • Giá tham chiếu: ${InventoryViewModel.formatCurrency(itm.defaultPrice)}",
                badge = itm.unit,
                badgeColor = FarmGreenContainer,
                badgeTextColor = FarmGreenPrimary,
                extraData = itm
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("inbound_form"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "LẬP PHIẾU NHẬP KHO (INBOUND)",
            fontSize = OutdoorModeManager.scaledSp(16f, isOutdoor),
            fontWeight = FontWeight.Bold,
            color = OutdoorFocusBorder
        )

        // 1. Ngày nhập kho
        OutdoorInputField(
            label = "Ngày nhập kho (dd/MM/yyyy)",
            value = inboundDateStr,
            onValueChange = { inboundDateStr = it },
            isRequired = true
        )

        // 2. Chọn Loại hàng hóa (Cám, Thuốc, Vật tư)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Loại hàng hóa (Tham số hệ thống):",
                fontSize = OutdoorModeManager.scaledSp(15f, isOutdoor),
                fontWeight = FontWeight.SemiBold,
                color = OutdoorLabelText
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categories = listOf(
                    InventoryItemEntity.CATEGORY_FEED to "Cám",
                    InventoryItemEntity.CATEGORY_MEDICINE to "Thuốc",
                    InventoryItemEntity.CATEGORY_SUPPLIES to "Vật tư"
                )
                categories.forEach { (catKey, catLabel) ->
                    val isSelected = selectedCategory == catKey
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) OutdoorFocusBorder else OutdoorInputFill,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 2.dp else 1.5.dp,
                            color = if (isSelected) OutdoorFocusBorder else OutdoorBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedCategory = catKey
                                selectedItem = masterItems.firstOrNull { it.category == catKey }
                            }
                            .testTag("inbound_cat_$catKey")
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = catLabel,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isSelected) Color.White else OutdoorLabelText
                            )
                        }
                    }
                }
            }
        }

        // 3. Bộ chọn Tên hàng hóa từ Master Data
        QuickPickerField(
            label = "Tên mặt hàng (Kế thừa từ Tham số hệ thống)",
            value = selectedItem?.let { "${it.name} [${it.masterCode}] (ĐVT: ${it.unit})" } ?: "Chạm để chọn mặt hàng...",
            icon = Icons.Default.Inventory2,
            isRequired = true,
            onClick = { showItemPicker = true },
            testTag = "inbound_item_picker"
        )

        // 4. Số lượng nhập & Đơn giá thực nhập (Gợi ý sẵn giá tham chiếu)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutdoorInputField(
                label = "Số lượng (${selectedItem?.unit ?: "ĐVT"})",
                value = quantityStr,
                onValueChange = { quantityStr = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isRequired = true,
                modifier = Modifier.weight(1f),
                testTag = "inbound_qty_input"
            )

            OutdoorInputField(
                label = "Đơn giá thực nhập (VNĐ)",
                value = formatNumberWithSeparator(unitPriceStr),
                onValueChange = { unitPriceStr = it.filter { ch -> ch.isDigit() } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isRequired = true,
                modifier = Modifier.weight(1.3f),
                testTag = "inbound_price_input"
            )
        }

        // 5. Khung THÀNH TIỀN (Số lượng × Đơn giá)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = FarmGreenContainer),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, FarmGreenPrimary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "THÀNH TIỀN (TỔNG CHI PHÍ NHẬP):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = FarmGreenPrimary
                )
                Text(
                    text = InventoryViewModel.formatCurrency(totalPrice),
                    fontSize = OutdoorModeManager.scaledSp(22f, isOutdoor),
                    fontWeight = FontWeight.Black,
                    color = FarmGreenPrimary
                )
                Text(
                    text = "✓ Tự động cộng tồn kho tức thời & cập nhật đơn giá tham chiếu mới vào Tham số hệ thống",
                    fontSize = 12.sp,
                    color = FarmGreenPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 6. Nhà cung cấp & Ghi chú
        OutdoorInputField(
            label = "Nhà cung cấp / Số hóa đơn",
            value = supplierOrInvoice,
            onValueChange = { supplierOrInvoice = it },
            placeholder = "vd: Công ty CP Chăn Nuôi VN - HĐ #1029",
            testTag = "inbound_supplier_input"
        )

        OutdoorInputField(
            label = "Ghi chú phiếu nhập",
            value = notes,
            onValueChange = { notes = it },
            placeholder = "Ghi chú lô hàng, hạn dùng...",
            testTag = "inbound_notes_input"
        )

        // Nút Lưu Phiếu Nhập
        Button(
            onClick = {
                val itm = selectedItem
                if (itm != null && quantity > 0f && unitPrice > 0L) {
                    val parsedDate = try {
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(inboundDateStr)?.time ?: System.currentTimeMillis()
                    } catch (e: Exception) {
                        System.currentTimeMillis()
                    }
                    onSaveInbound(itm.inventoryItemId, quantity, unitPrice, parsedDate, supplierOrInvoice, notes)
                }
            },
            enabled = selectedItem != null && quantity > 0f && unitPrice > 0L,
            colors = ButtonDefaults.buttonColors(containerColor = OutdoorFocusBorder),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("inbound_submit_button")
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Lưu Phiếu Nhập Kho", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }

    if (showItemPicker) {
        QuickPickerBottomSheet(
            title = "Chọn Mặt Hàng Nhập Kho (Tham số hệ thống)",
            searchPlaceholder = "Tìm theo tên hoặc mã hàng...",
            options = itemPickerOptions,
            selectedId = selectedItem?.masterId,
            onSelect = { opt ->
                val itm = opt.extraData as? MasterInventoryItemUi
                if (itm != null) {
                    selectedItem = itm
                    unitPriceStr = itm.defaultPrice.toString()
                }
            },
            onDismiss = { showItemPicker = false }
        )
    }
}

// =============================================================================
// SUBTAB C: LẬP PHIẾU XUẤT KHO (OUTBOUND)
// - RÀNG BUỘC TỒN KHO: Chỉ kích hoạt và hiển thị các mặt hàng có current_stock > 0
// - KIỂM TRA XUẤT VƯỢT: Nếu Số lượng xuất > current_stock -> hiển thị cảnh báo đỏ và KHÓA nút Lưu
// - Giảm tồn kho tức thời & Phân bổ chi phí cho Chuồng nhận
// =============================================================================
@Composable
private fun OutboundFormSubtab(
    masterItems: List<MasterInventoryItemUi>,
    barns: List<SystemParameterEntity>,
    presetItem: MasterInventoryItemUi? = null,
    onSaveOutbound: (itemId: String, qty: Float, targetBarn: String, date: Long, notes: String) -> Unit
) {
    val isOutdoor = LocalOutdoorMode.current
    var selectedCategory by remember { mutableStateOf(presetItem?.category ?: InventoryItemEntity.CATEGORY_FEED) }

    // RÀNG BUỘC TỒN KHO: Chỉ hiển thị các mặt hàng trong danh mục có current_stock > 0
    val availableItems = remember(masterItems, selectedCategory) {
        masterItems.filter { it.category == selectedCategory && it.currentStock > 0f }
    }

    var selectedItem by remember(availableItems, presetItem) {
        mutableStateOf(presetItem ?: availableItems.firstOrNull())
    }

    val todayStr = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }
    var outboundDateStr by remember { mutableStateOf(todayStr) }
    var quantityStr by remember { mutableStateOf("10") }
    var targetBarn by remember { mutableStateOf("Khu Chuồng Bầu 1") }
    var purposeOrNotes by remember { mutableStateOf("Cấp cám & vật tư chuồng nuôi") }

    var showItemPicker by remember { mutableStateOf(false) }
    var showBarnPicker by remember { mutableStateOf(false) }

    val currentStock = selectedItem?.currentStock ?: 0f
    val enteredQty = quantityStr.toFloatOrNull() ?: 0f

    // KIỂM TRA XUẤT VƯỢT (CHẶN XUẤT ÂM):
    val isOverStock = enteredQty > currentStock
    val isInvalidQty = enteredQty <= 0f || isOverStock

    val itemPickerOptions = remember(availableItems) {
        availableItems.map { itm ->
            PickerOption(
                id = itm.masterId,
                title = "${itm.name} [${itm.masterCode}]",
                subtitle = "Tồn kho: ${itm.currentStock} ${itm.unit} • ĐG bình quân: ${InventoryViewModel.formatCurrency(itm.averageUnitPrice)}",
                badge = "Tồn: ${itm.currentStock} ${itm.unit}",
                badgeColor = FarmGreenContainer,
                badgeTextColor = FarmGreenPrimary,
                extraData = itm
            )
        }
    }

    val barnPickerOptions = remember(barns) {
        if (barns.isNotEmpty()) {
            barns.map { barn ->
                PickerOption(
                    id = barn.id,
                    title = barn.name,
                    subtitle = barn.description.ifBlank { "Mã chuồng: ${barn.code}" }
                )
            }
        } else {
            listOf(
                PickerOption("B1", "Khu Chuồng Bầu 1", "Nái mang thai kỳ 1"),
                PickerOption("B2", "Khu Chuồng Bầu 2", "Nái mang thai kỳ 2"),
                PickerOption("B3", "Khu Chuồng Đẻ 4", "Nái đẻ và nuôi heo con"),
                PickerOption("B4", "Khu Cai Sữa & Chờ Phối B", "Heo con cai sữa & nái chờ phối"),
                PickerOption("B5", "Khu Chuồng Thịt 1", "Heo thịt giai đoạn 1 (30-70kg)"),
                PickerOption("B6", "Khu Chuồng Thịt 2 & 3", "Heo thịt giai đoạn 2 (70-110kg)")
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("outbound_form"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "LẬP PHIẾU XUẤT KHO (OUTBOUND)",
            fontSize = OutdoorModeManager.scaledSp(16f, isOutdoor),
            fontWeight = FontWeight.Bold,
            color = FarmAmber
        )

        // 1. Ngày xuất kho
        OutdoorInputField(
            label = "Ngày xuất kho (dd/MM/yyyy)",
            value = outboundDateStr,
            onValueChange = { outboundDateStr = it },
            isRequired = true
        )

        // 2. Chọn Nhóm hàng xuất
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Nhóm hàng xuất:",
                fontSize = OutdoorModeManager.scaledSp(15f, isOutdoor),
                fontWeight = FontWeight.SemiBold,
                color = OutdoorLabelText
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categories = listOf(
                    InventoryItemEntity.CATEGORY_FEED to "Cám",
                    InventoryItemEntity.CATEGORY_MEDICINE to "Thuốc",
                    InventoryItemEntity.CATEGORY_SUPPLIES to "Vật tư"
                )
                categories.forEach { (catKey, catLabel) ->
                    val isSelected = selectedCategory == catKey
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) FarmAmber else OutdoorInputFill,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 2.dp else 1.5.dp,
                            color = if (isSelected) FarmAmber else OutdoorBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedCategory = catKey
                                selectedItem = masterItems.firstOrNull { it.category == catKey && it.currentStock > 0f }
                            }
                            .testTag("outbound_cat_$catKey")
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = catLabel,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isSelected) Color.White else OutdoorLabelText
                            )
                        }
                    }
                }
            }
        }

        // 3. Tên hàng hóa xuất (RÀNG BUỘC: Chỉ hiển thị tồn > 0)
        QuickPickerField(
            label = "Mặt hàng xuất (Ràng buộc: Tồn kho > 0)",
            value = selectedItem?.let { "${it.name} [${it.masterCode}] (Còn tồn: ${it.currentStock} ${it.unit})" } ?: "Chưa có mặt hàng nào còn tồn trong nhóm này",
            icon = Icons.Default.Inventory2,
            isRequired = true,
            onClick = { if (availableItems.isNotEmpty()) showItemPicker = true },
            testTag = "outbound_item_picker"
        )

        // 4. Số lượng xuất & KIỂM TRA XUẤT VƯỢT
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            OutdoorInputField(
                label = "Số lượng xuất (${selectedItem?.unit ?: "ĐVT"})",
                value = quantityStr,
                onValueChange = { quantityStr = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isRequired = true,
                isError = isOverStock,
                errorMessage = if (isOverStock) "⚠️ CHẶN XUẤT ÂM: Số lượng xuất ($enteredQty) vượt quá tồn kho khả dụng ($currentStock ${selectedItem?.unit})!" else null,
                testTag = "outbound_qty_input"
            )

            if (!isOverStock && selectedItem != null) {
                Text(
                    text = "✓ Khả dụng trong kho: $currentStock ${selectedItem?.unit} • Sau khi xuất còn lại: ${(currentStock - enteredQty).coerceAtLeast(0f)} ${selectedItem?.unit}",
                    fontSize = 12.sp,
                    color = FarmGreenPrimary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        // 5. Chuồng nhận (Liên kết danh mục BARN trong Tham số hệ thống)
        QuickPickerField(
            label = "Chuồng nhận vật tư / cám (Hạch toán chi phí chuồng)",
            value = targetBarn,
            icon = Icons.Default.Build,
            isRequired = true,
            onClick = { showBarnPicker = true },
            testTag = "outbound_barn_picker"
        )

        // 6. Mục đích sử dụng / Ghi chú
        OutdoorInputField(
            label = "Mục đích sử dụng / Ghi chú",
            value = purposeOrNotes,
            onValueChange = { purposeOrNotes = it },
            placeholder = "vd: Dùng cho đàn nái đẻ lứa 2, điều trị tiêu chảy...",
            testTag = "outbound_purpose_input"
        )

        // Nút Lưu Phiếu Xuất (BỊ KHÓA NẾU XUẤT VƯỢT TỒN KHO)
        Button(
            onClick = {
                val itm = selectedItem
                if (itm != null && !isInvalidQty) {
                    val parsedDate = try {
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(outboundDateStr)?.time ?: System.currentTimeMillis()
                    } catch (e: Exception) {
                        System.currentTimeMillis()
                    }
                    onSaveOutbound(itm.inventoryItemId, enteredQty, targetBarn, parsedDate, purposeOrNotes)
                }
            },
            enabled = selectedItem != null && !isInvalidQty,
            colors = ButtonDefaults.buttonColors(
                containerColor = FarmAmber,
                disabledContainerColor = Color(0xFFCBD5E1)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("outbound_submit_button")
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isOverStock) "ĐÃ KHÓA: VƯỢT TỒN KHO" else "Lưu Phiếu Xuất Kho",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    if (showItemPicker) {
        QuickPickerBottomSheet(
            title = "Chọn Mặt Hàng Xuất Kho (Tồn > 0)",
            searchPlaceholder = "Tìm mặt hàng còn tồn...",
            options = itemPickerOptions,
            selectedId = selectedItem?.masterId,
            onSelect = { opt ->
                selectedItem = opt.extraData as? MasterInventoryItemUi
            },
            onDismiss = { showItemPicker = false }
        )
    }

    if (showBarnPicker) {
        QuickPickerBottomSheet(
            title = "Chọn Chuồng Nhận Hàng (Hạch toán chi phí)",
            searchPlaceholder = "Tìm chuồng...",
            options = barnPickerOptions,
            selectedId = targetBarn,
            onSelect = { targetBarn = it.title },
            onDismiss = { showBarnPicker = false }
        )
    }
}

// =============================================================================
// DIALOG THÊM MẶT HÀNG MỚI (ĐỒNG BỘ CẢ THAM SỐ HỆ THỐNG VÀ KHO)
// =============================================================================
@Composable
private fun AddNewItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, cat: String, unit: String, initialStock: Float, unitPrice: Long, minAlert: Float, notes: String) -> Unit
) {
    val isOutdoor = LocalOutdoorMode.current
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(InventoryItemEntity.CATEGORY_FEED) }
    var unit by remember { mutableStateOf("Bao 25kg") }
    var initialStockStr by remember { mutableStateOf("0") }
    var unitPriceStr by remember { mutableStateOf("320000") }
    var minAlertStr by remember { mutableStateOf("10") }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = OutdoorInputFill),
            border = androidx.compose.foundation.BorderStroke(
                width = if (isOutdoor) 2.dp else 1.5.dp,
                color = if (isOutdoor) OutdoorCardBorder else OutdoorBorder
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Thêm Mặt Hàng Danh Mục Mới",
                    fontSize = OutdoorModeManager.scaledSp(18f, isOutdoor),
                    fontWeight = FontWeight.Bold,
                    color = OutdoorLabelText
                )

                Text(
                    text = "Mặt hàng sẽ được liên kết trực tiếp vào Tham số hệ thống & Bảng Tồn kho.",
                    fontSize = 12.sp,
                    color = OutdoorPlaceholder
                )

                // Nhóm hàng
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Nhóm hàng:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OutdoorLabelText)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val categories = listOf(
                            InventoryItemEntity.CATEGORY_FEED to "Cám",
                            InventoryItemEntity.CATEGORY_MEDICINE to "Thuốc",
                            InventoryItemEntity.CATEGORY_SUPPLIES to "Vật tư"
                        )
                        categories.forEach { (catKey, catLabel) ->
                            val isSelected = selectedCategory == catKey
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) OutdoorFocusBorder else OutdoorInputFill,
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, if (isSelected) OutdoorFocusBorder else OutdoorBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedCategory = catKey
                                        unit = when (catKey) {
                                            InventoryItemEntity.CATEGORY_FEED -> "Bao 25kg"
                                            InventoryItemEntity.CATEGORY_MEDICINE -> "Lọ 100ml"
                                            else -> "Cái"
                                        }
                                    }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = catLabel,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isSelected) Color.White else OutdoorLabelText
                                    )
                                }
                            }
                        }
                    }
                }

                OutdoorInputField(
                    label = "Tên hàng hóa (Master Data)",
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "vd: Cám nái hậu bị, Kháng sinh Enrofloxacin...",
                    isRequired = true,
                    testTag = "add_item_name_input"
                )

                OutdoorInputField(
                    label = "Đơn vị tính (ĐVT)",
                    value = unit,
                    onValueChange = { unit = it },
                    placeholder = "Bao 25kg, Lọ 100ml, Liều, Cái...",
                    isRequired = true,
                    testTag = "add_item_unit_input"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutdoorInputField(
                        label = "Số lượng tồn ban đầu",
                        value = initialStockStr,
                        onValueChange = { initialStockStr = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        testTag = "add_item_stock_input"
                    )

                    OutdoorInputField(
                        label = "Đơn giá tham chiếu (VNĐ)",
                        value = formatNumberWithSeparator(unitPriceStr),
                        onValueChange = { unitPriceStr = it.filter { ch -> ch.isDigit() } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.3f),
                        testTag = "add_item_price_input"
                    )
                }

                OutdoorInputField(
                    label = "Mức tồn an toàn tối thiểu (Ngưỡng cảnh báo)",
                    value = minAlertStr,
                    onValueChange = { minAlertStr = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    placeholder = "ví dụ: 10 bao",
                    testTag = "add_item_min_alert_input"
                )

                OutdoorInputField(
                    label = "Ghi chú kỹ thuật / Nhà cung cấp",
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = "Nhà sản xuất, cách bảo quản...",
                    testTag = "add_item_notes_input"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, OutdoorBorder),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Hủy", fontWeight = FontWeight.Bold, color = OutdoorLabelText)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (name.isNotBlank() && unit.isNotBlank()) {
                                onConfirm(
                                    name.trim(),
                                    selectedCategory,
                                    unit.trim(),
                                    initialStockStr.toFloatOrNull() ?: 0f,
                                    unitPriceStr.filter { it.isDigit() }.toLongOrNull() ?: 0L,
                                    minAlertStr.toFloatOrNull() ?: 5f,
                                    notes.trim()
                                )
                            }
                        },
                        enabled = name.isNotBlank() && unit.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = OutdoorFocusBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("add_item_confirm_button")
                    ) {
                        Text("Lưu Danh Mục & Kho", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
