package com.example.ui.pig

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PigEntity
import com.example.ui.components.ReproductiveStatusBadge
import com.example.ui.components.SyncStatusBadge
import com.example.ui.theme.FarmAmber
import com.example.ui.theme.FarmBackground
import com.example.ui.theme.FarmGreenContainer
import com.example.ui.theme.FarmGreenPrimary
import com.example.ui.theme.FarmOutline
import com.example.ui.theme.FarmRed
import com.example.ui.theme.FarmTextPrimary
import com.example.ui.theme.FarmTextSecondary
import com.example.ui.theme.LocalOutdoorMode
import com.example.ui.theme.OutdoorCardBorder
import com.example.ui.theme.OutdoorModeManager
import com.example.ui.theme.OutdoorModeToggleButton
import com.example.ui.theme.OutdoorOverallBackground
import com.example.util.PigCsvExporter

/**
 * Jetpack Compose screen that observes the data stream from PigViewModel
 * and renders the list of pigs using LazyColumn.
 * Features:
 * 1. Health Status Pie Chart summary card at top
 * 2. Camera QR Code scanner for ear tag quick identification and lookup
 * 3. Export pig list and health records to CSV file
 * 4. Tag ID search bar with StateFlow filtering
 * 5. Sorting criteria toggle (Tag ID vs Birth date) & direction toggle
 * 6. Item click opening detailed PigDetailBottomSheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PigListScreen(
    viewModel: PigViewModel,
    onPigSelected: (String) -> Unit,
    onAddNewPig: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val sortDirection by viewModel.sortDirection.collectAsState()
    val isOutdoor = LocalOutdoorMode.current

    val listState = rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 30 }
    }

    var showQrScannerDialog by remember { mutableStateOf(false) }
    var showFastEventMenuBottomSheet by remember { mutableStateOf(false) }
    var activeFastEventDialog by remember { mutableStateOf<FastEventType?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val dashboardPagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })

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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Bar tự động cuộn thu gọn (Collapsible Top Bar) - Đảm bảo lọt viền và không tràn
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = if (isScrolled) 44.dp else 56.dp)
                        .padding(vertical = if (isScrolled) 2.dp else 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .padding(end = 8.dp)
                    ) {
                        if (isScrolled) {
                            Text(
                                text = "ĐÀN HEO (${state.totalHeadCount} con)",
                                fontSize = OutdoorModeManager.scaledSp(16f, isOutdoor),
                                fontWeight = FontWeight.Black,
                                color = FarmTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Text(
                                text = "ĐÀN HEO",
                                fontSize = OutdoorModeManager.scaledSp(20f, isOutdoor),
                                fontWeight = FontWeight.Black,
                                color = FarmTextPrimary,
                                letterSpacing = 0.5.sp,
                                maxLines = 1
                            )
                            Text(
                                text = "Tổng đàn: ${state.totalHeadCount} con • ${state.totalRecordsCount} lô/cá thể",
                                fontSize = OutdoorModeManager.scaledSp(11f, isOutdoor),
                                color = FarmTextSecondary,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Hàng nút chức năng Header: Chế độ ngoài trời (icon) & Xuất CSV báo cáo (icon)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutdoorModeToggleButton(iconOnly = true)

                        // Nút Xuất dữ liệu đàn heo & hồ sơ sức khỏe ra tệp CSV
                        IconButton(
                            onClick = {
                                val csvContent = PigCsvExporter.generatePigsCsv(state.pigs)
                                val file = PigCsvExporter.saveCsvToFile(context, csvContent)
                                PigCsvExporter.shareCsvFile(
                                    context = context,
                                    file = file,
                                    chooserTitle = "Xuất dữ liệu đàn heo ra CSV (${state.pigs.size} bản ghi)"
                                )
                            },
                            modifier = Modifier
                                .testTag("pig_export_csv_button")
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, FarmOutline, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Xuất file CSV",
                                tint = FarmGreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 1. Dashboard tổng quan dạng thẻ vuốt trượt ngang (Swiper Cards)
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Thanh chọn nhanh 3 cụm biểu đồ/thống kê
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DashboardPillTab(
                            title = "Cơ cấu 5 nhóm",
                            isSelected = dashboardPagerState.currentPage == 0,
                            onClick = { coroutineScope.launch { dashboardPagerState.animateScrollToPage(0) } },
                            modifier = Modifier.weight(1f)
                        )
                        DashboardPillTab(
                            title = "Mật độ chuồng",
                            isSelected = dashboardPagerState.currentPage == 1,
                            onClick = { coroutineScope.launch { dashboardPagerState.animateScrollToPage(1) } },
                            modifier = Modifier.weight(1f)
                        )
                        DashboardPillTab(
                            title = "Sức khỏe thú y",
                            isSelected = dashboardPagerState.currentPage == 2,
                            onClick = { coroutineScope.launch { dashboardPagerState.animateScrollToPage(2) } },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Khung Swiper chứa 3 thẻ thống kê
                    HorizontalPager(
                        state = dashboardPagerState,
                        modifier = Modifier.fillMaxWidth()
                    ) { page ->
                        when (page) {
                            0 -> FarmHerdSummarySection(
                                summary = state.herdStructure,
                                onCategorySelected = { category ->
                                    when (category) {
                                        "MANG_THAI" -> viewModel.onSearchQueryChanged("MANG_THAI")
                                        "NUOI_CON" -> viewModel.onSearchQueryChanged("NUOI_CON")
                                        "CHO_PHOI" -> viewModel.onSearchQueryChanged("Cai sữa")
                                        "HEO_CON" -> viewModel.onSearchQueryChanged("heo con")
                                        "HEO_THIT" -> viewModel.onSearchQueryChanged("Thịt")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            1 -> BarnDistribution3DSection(
                                distribution = state.barnDistribution,
                                selectedBarn = state.selectedBarnFilter,
                                onBarnSelected = { barnName -> viewModel.onBarnSelected(barnName) },
                                onClearBarnFilter = { viewModel.clearBarnFilter() },
                                modifier = Modifier.fillMaxWidth()
                            )
                            2 -> PigHealthSummaryCard(
                                distribution = state.healthDistribution,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Dấu chấm chỉ báo trang hiện tại (Indicator Dots)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(3) { index ->
                            val isSelected = dashboardPagerState.currentPage == index
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .size(if (isSelected) 18.dp else 6.dp, 5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (isSelected) FarmGreenPrimary else Color(0xFFD1D5DB))
                            )
                        }
                    }
                }
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

            // 2. Ô tìm kiếm theo mã số tai & tích hợp nút quét mã QR
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    textStyle = TextStyle(
                        color = Color(0xFF111827),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    placeholder = {
                        Text(
                            text = "Tìm kiếm theo mã số tai (Tag ID)...",
                            fontSize = 13.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Tìm kiếm theo mã số tai",
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.onSearchQueryChanged("") },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("pig_search_clear_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Xóa tìm kiếm",
                                        tint = Color(0xFF6B7280),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            // Gộp nút quét mã QR vào góc phải của chính ô input tìm kiếm
                            IconButton(
                                onClick = { showQrScannerDialog = true },
                                modifier = Modifier
                                    .size(34.dp)
                                    .testTag("search_bar_qr_icon")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "Quét mã QR thẻ tai",
                                    tint = FarmGreenPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pig_search_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF111827),
                        unfocusedTextColor = Color(0xFF111827),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        cursorColor = Color(0xFF111827),
                        focusedBorderColor = Color(0xFF16A34A),
                        unfocusedBorderColor = Color(0xFFD1D5DB),
                        focusedPlaceholderColor = Color(0xFF9CA3AF),
                        unfocusedPlaceholderColor = Color(0xFF9CA3AF)
                    ),
                    singleLine = true
                )
            }

            // 3. Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(PigFilterType.values()) { filter ->
                        val isSelected = state.activeFilter == filter
                        Box(
                            modifier = Modifier
                                .testTag("pig_filter_chip_${filter.name.lowercase()}")
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) FarmGreenPrimary else Color.White)
                                .border(
                                    1.dp,
                                    if (isSelected) FarmGreenPrimary else FarmOutline,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { viewModel.onFilterSelected(filter) }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = filter.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else FarmTextPrimary
                            )
                        }
                    }
                }
            }

            // 4. Header danh sách làm việc: Bộ đếm kết quả trực quan & nút sắp xếp nhanh
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(
                            text = "DANH SÁCH LÀM VIỆC",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF374151),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = FarmGreenContainer
                        ) {
                            Text(
                                text = "${state.pigs.size} ${if (searchQuery.isNotBlank()) "kết quả" else "cá thể/lô"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = FarmGreenPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Nút chuyển tiêu chí sắp xếp gọn gàng
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, FarmOutline),
                        modifier = Modifier
                            .testTag("pig_sort_toggle_button")
                            .clickable { viewModel.toggleSortOption() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = null,
                                tint = FarmGreenPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${sortOption.label} (${if (sortDirection == SortDirection.ASCENDING) "A-Z" else "Z-A"})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF374151)
                            )
                        }
                    }
                }
            }

            // Empty state when no items match
            if (state.pigs.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            tint = Color(0xFFA5B2A5),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Không tìm thấy heo phù hợp",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = FarmTextSecondary
                        )
                        Text(
                            text = if (searchQuery.isNotBlank()) {
                                "Không có cá thể nào khớp với mã tai \"$searchQuery\""
                            } else {
                                "Thử chọn bộ lọc khác"
                            },
                            fontSize = 13.sp,
                            color = FarmTextSecondary
                        )
                    }
                }
            }

            // Pigs List via LazyColumn with stable keys
            // Each card is clickable, selecting the pig in PigViewModel to display full information
            items(state.pigs, key = { it.id }) { pig ->
                PigItemCard(
                    pig = pig,
                    onClick = {
                        onPigSelected(pig.id)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Action FloatingActionButton (+) mở menu nhập sự kiện nhanh theo nghiệp vụ
        FloatingActionButton(
            onClick = { showFastEventMenuBottomSheet = true },
            containerColor = FarmGreenPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 80.dp)
                .testTag("pig_fab_add")
                .size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Nhập sự kiện nhanh nghiệp vụ",
                modifier = Modifier.size(28.dp)
            )
        }

        // 6. Menu nhập sự kiện nhanh (3-5 thao tác ngoài chuồng)
        if (showFastEventMenuBottomSheet) {
            FastEventActionMenuBottomSheet(
                onDismiss = { showFastEventMenuBottomSheet = false },
                onSelectEvent = { selectedType ->
                    showFastEventMenuBottomSheet = false
                    when (selectedType) {
                        FastEventType.PIGLET_VACCINE -> {
                            val targetBatch = state.pigs.firstOrNull { it.isBatch }?.tagCode ?: "DC20270626-001"
                            viewModel.recordPigletVaccine(targetBatch, System.currentTimeMillis(), 11, "Mycoplasma", "2ml/con")
                        }
                        FastEventType.PIGLET_TAIL_DOCK -> {
                            val targetBatch = state.pigs.firstOrNull { it.isBatch }?.tagCode ?: "DC20270626-001"
                            viewModel.recordTailDocking(targetBatch, System.currentTimeMillis(), 11, "Bấm nanh và cắt đuôi sát trùng")
                        }
                        FastEventType.GROWER_RELOCATE -> {
                            val targetBatch = state.pigs.firstOrNull { it.isBatch }?.tagCode ?: "LT001"
                            viewModel.recordRelocation(targetBatch, System.currentTimeMillis(), "Khu Vỗ Béo B-02", "Chuyển chuồng vỗ béo")
                        }
                        else -> {
                            activeFastEventDialog = selectedType
                        }
                    }
                }
            )
        }

        // 1. Sự kiện: Đẻ (Heo con theo mẹ)
        if (activeFastEventDialog == FastEventType.SOW_FARROWING) {
            FarrowingEventDialog(
                availablePigs = state.pigs,
                onDismiss = { activeFastEventDialog = null },
                onSave = { sowId, farrowDate, total, alive, still, mummies, weight, notes ->
                    viewModel.recordFarrowing(
                        sowId = sowId,
                        farrowingDate = farrowDate,
                        totalBorn = total,
                        bornAlive = alive,
                        stillborn = still,
                        mummies = mummies,
                        litterWeightKg = weight,
                        notes = notes,
                        onSuccess = { activeFastEventDialog = null }
                    )
                }
            )
        }

        // 2. Sự kiện: Phối Giống Heo Nái (Event 1)
        if (activeFastEventDialog == FastEventType.SOW_MATING) {
            MatingEventDialog(
                availablePigs = state.pigs,
                onDismiss = { activeFastEventDialog = null },
                onSave = { sowTag, date, boar, tech, notes ->
                    viewModel.recordMating(
                        sowTag = sowTag,
                        matingDate = date,
                        boarTag = boar,
                        technician = tech,
                        notes = notes,
                        onSuccess = { activeFastEventDialog = null }
                    )
                }
            )
        }

        // 3. Sự kiện: Kiểm Tra Thai (Event 2)
        if (activeFastEventDialog == FastEventType.SOW_PREGNANCY_CHECK) {
            PregnancyCheckDialog(
                availablePigs = state.pigs,
                onDismiss = { activeFastEventDialog = null },
                onSaveResult = { sowTag, date, result ->
                    viewModel.recordPregnancyCheckResult(
                        sowTag = sowTag,
                        checkDate = date,
                        result = result,
                        onSuccess = { activeFastEventDialog = null }
                    )
                }
            )
        }

        // 4. Sự kiện: Cai Sữa Heo Con (Event 3)
        if (activeFastEventDialog == FastEventType.SOW_WEANING || activeFastEventDialog == FastEventType.PIGLET_WEANING) {
            WeaningEventDialog(
                availableBatches = state.pigs,
                onDismiss = { activeFastEventDialog = null },
                onSaveWeaning = { sowTag, targetPen, weanedQty, totalWeight, date ->
                    val avgWeight = if (weanedQty > 0) totalWeight / weanedQty else 7.5f
                    viewModel.recordWeaning(
                        pigletTag = sowTag,
                        weaningDate = date,
                        weanedQuantity = weanedQty,
                        averageWeightKg = avgWeight,
                        sowTag = sowTag,
                        onSuccess = { activeFastEventDialog = null }
                    )
                }
            )
        }

        // 5. Sự kiện: Can Thiệp Heo Con Theo Mẹ (Event 4 - Tiêm sắt, Cắt đuôi, Vaccine)
        if (activeFastEventDialog == FastEventType.PIGLET_CARE) {
            PigletCareEventDialog(
                availableBatches = state.pigs,
                onDismiss = { activeFastEventDialog = null },
                onSave = { batchTag, date, pType, vName, tech, dosage, notes ->
                    viewModel.recordPigletCare(
                        pigletTag = batchTag,
                        date = date,
                        procedureType = pType,
                        vaccineName = vName,
                        tech = tech,
                        dosage = dosage,
                        notes = notes,
                        onSuccess = { activeFastEventDialog = null }
                    )
                }
            )
        }

        // Tiêm sắt đơn
        if (activeFastEventDialog == FastEventType.PIGLET_IRON) {
            IronInjectionDialog(
                availableBatches = state.pigs,
                onDismiss = { activeFastEventDialog = null },
                onSave = { tag, date, qty, drug, dose ->
                    viewModel.recordIronInjection(
                        pigletTag = tag,
                        injectionDate = date,
                        quantity = qty,
                        drugName = drug,
                        dosageMl = dose,
                        onSuccess = { activeFastEventDialog = null }
                    )
                }
            )
        }

        // Vaccine đơn
        if (activeFastEventDialog == FastEventType.PIGLET_VACCINE) {
            PigletCareEventDialog(
                availableBatches = state.pigs,
                onDismiss = { activeFastEventDialog = null },
                onSave = { batchTag, date, pType, vName, tech, dosage, notes ->
                    viewModel.recordPigletVaccine(
                        pigletTag = batchTag,
                        vaccineDate = date,
                        quantity = 11,
                        vaccineName = vName,
                        dosage = dosage,
                        onSuccess = { activeFastEventDialog = null }
                    )
                }
            )
        }

        // Cắt đuôi đơn
        if (activeFastEventDialog == FastEventType.PIGLET_TAIL_DOCK) {
            PigletCareEventDialog(
                availableBatches = state.pigs,
                onDismiss = { activeFastEventDialog = null },
                onSave = { batchTag, date, _, _, _, _, notes ->
                    viewModel.recordTailDocking(
                        pigletTag = batchTag,
                        dockDate = date,
                        quantity = 11,
                        notes = notes,
                        onSuccess = { activeFastEventDialog = null }
                    )
                }
            )
        }

        // 6. Sự kiện: Nhập Lô Heo Thịt (Event 5)
        if (activeFastEventDialog == FastEventType.GROWER_INTAKE) {
            GrowerIntakeDialog(
                availableBatches = state.pigs,
                onDismiss = { activeFastEventDialog = null },
                onSave = { code, date, qty, weight, loc ->
                    viewModel.recordBatchIntake(code, date, qty, weight, loc, onSuccess = { activeFastEventDialog = null })
                }
            )
        }

        // 7. Sự kiện: Chuyển Chuồng Heo Thịt (Event 6)
        if (activeFastEventDialog == FastEventType.GROWER_RELOCATE) {
            GrowerRelocationDialog(
                availableBatches = state.pigs,
                onDismiss = { activeFastEventDialog = null },
                onSave = { fromPen, toPen, moveDate, qty, totalWeightKg, avgAgeDays, batchCode, notes ->
                    viewModel.recordGrowerRelocation(
                        fromPen = fromPen,
                        toPen = toPen,
                        moveDate = moveDate,
                        quantity = qty,
                        totalWeightKg = totalWeightKg,
                        avgAgeDays = avgAgeDays,
                        batchCode = batchCode,
                        notes = notes,
                        onSuccess = { activeFastEventDialog = null }
                    )
                }
            )
        }

        // 8. Sự kiện: Chuyển Chuồng Heo Nái (Event 7)
        if (activeFastEventDialog == FastEventType.SOW_RELOCATE) {
            SowRelocationDialog(
                availablePigs = state.pigs,
                onDismiss = { activeFastEventDialog = null },
                onSave = { sowTag, moveDate, fromLocation, toLocation, notes ->
                    viewModel.recordSowRelocation(
                        sowTag = sowTag,
                        moveDate = moveDate,
                        fromLocation = fromLocation,
                        toLocation = toLocation,
                        notes = notes,
                        onSuccess = { activeFastEventDialog = null }
                    )
                }
            )
        }

        // 9. Sự kiện: Xuất Bán Heo Thịt (Event 8)
        if (activeFastEventDialog == FastEventType.GROWER_SALE) {
            GrowerSaleDialog(
                availableBatches = state.pigs,
                onDismiss = { activeFastEventDialog = null },
                onSaveSale = { batchCode, saleDate, qty, totalWeightKg, salePriceVnd, buyerName ->
                    val avgWeight = if (qty > 0) totalWeightKg / qty else 105f
                    viewModel.recordBatchSale(
                        batchCode = batchCode,
                        saleDate = saleDate,
                        soldQuantity = qty,
                        averageWeightKg = avgWeight,
                        customerName = buyerName,
                        salePriceVnd = salePriceVnd,
                        onSuccess = { activeFastEventDialog = null }
                    )
                }
            )
        }

        // 10. Sự kiện: + Nhập Đàn Heo Nái (Event 9)
        if (activeFastEventDialog == FastEventType.SOW_INTAKE) {
            NewSowIntakeDialog(
                availablePigs = state.pigs,
                onDismiss = { activeFastEventDialog = null },
                onSave = { tagCode, breed, birthDate, parity, location, notes ->
                    viewModel.recordNewSowIntake(
                        tagCode = tagCode,
                        breed = breed,
                        birthDate = birthDate,
                        parity = parity,
                        location = location,
                        notes = notes,
                        onSuccess = { activeFastEventDialog = null }
                    )
                }
            )
        }

        // Cân trọng lượng heo thịt
        if (activeFastEventDialog == FastEventType.GROWER_WEIGH) {
            GrowerEventDialog(
                initialSubEvent = FastEventType.GROWER_WEIGH,
                availableBatches = state.pigs,
                onDismiss = { activeFastEventDialog = null },
                onIntake = { _, _, _, _, _ -> },
                onWeigh = { code, date, count, weight ->
                    viewModel.recordWeighing(code, date, count, weight) { _, _, _ ->
                        activeFastEventDialog = null
                    }
                },
                onTreatment = { _, _, _, _, _ -> },
                onSale = { _, _, _, _, _ -> }
            )
        }

        // Điều trị heo thịt
        if (activeFastEventDialog == FastEventType.GROWER_TREATMENT) {
            GrowerEventDialog(
                initialSubEvent = FastEventType.GROWER_TREATMENT,
                availableBatches = state.pigs,
                onDismiss = { activeFastEventDialog = null },
                onIntake = { _, _, _, _, _ -> },
                onWeigh = { _, _, _, _ -> },
                onTreatment = { code, date, sick, disease, drug ->
                    viewModel.recordTreatment(code, date, sick, disease, drug, onSuccess = { activeFastEventDialog = null })
                },
                onSale = { _, _, _, _, _ -> }
            )
        }

        // Điều trị heo nái
        if (activeFastEventDialog == FastEventType.SOW_TREATMENT) {
            GrowerEventDialog(
                initialSubEvent = FastEventType.GROWER_TREATMENT,
                availableBatches = state.pigs,
                onDismiss = { activeFastEventDialog = null },
                onIntake = { _, _, _, _, _ -> },
                onWeigh = { _, _, _, _ -> },
                onTreatment = { code, date, sick, disease, drug ->
                    viewModel.recordTreatment(code, date, sick, disease, drug, onSuccess = { activeFastEventDialog = null })
                },
                onSale = { _, _, _, _, _ -> }
            )
        }

        // Loại Thải Heo Nái
        if (activeFastEventDialog == FastEventType.SOW_CULLING) {
            SowCullingDialog(
                availablePigs = state.pigs,
                onDismiss = { activeFastEventDialog = null },
                onSave = { tag, date, reason, weight, price, buyer ->
                    viewModel.recordSowCull(
                        sowTag = tag,
                        cullDate = date,
                        reason = reason,
                        weightKg = weight,
                        salePrice = price,
                        buyerName = buyer,
                        onSuccess = { activeFastEventDialog = null }
                    )
                }
            )
        }

        // Loại thải Heo Con
        if (activeFastEventDialog == FastEventType.PIGLET_CULLING) {
            PigletCullingDialog(
                availableBatches = state.pigs,
                onDismiss = { activeFastEventDialog = null },
                onSave = { tag, date, qty, reason, weight, disposition ->
                    viewModel.recordPigletCull(
                        pigletTag = tag,
                        cullDate = date,
                        cullQuantity = qty,
                        reason = reason,
                        averageWeightKg = weight,
                        disposition = disposition,
                        onSuccess = { activeFastEventDialog = null }
                    )
                }
            )
        }

        // Loại thải Heo Thịt
        if (activeFastEventDialog == FastEventType.GROWER_CULLING) {
            GrowerCullingDialog(
                availableBatches = state.pigs,
                onDismiss = { activeFastEventDialog = null },
                onSave = { batch, date, qty, reason, weight, price, buyer ->
                    viewModel.recordGrowerCull(
                        batchCode = batch,
                        cullDate = date,
                        cullQuantity = qty,
                        reason = reason,
                        averageWeightKg = weight,
                        salePrice = price,
                        buyerName = buyer,
                        onSuccess = { activeFastEventDialog = null }
                    )
                }
            )
        }

        // Pig Detail View Bottom Sheet integrated for the selected pig
        // Displays full information including birth date and current health status
        state.selectedPig?.let { pig ->
            PigDetailBottomSheet(
                pig = pig,
                onDismiss = { viewModel.selectPig(null) },
                onViewFullLifecycle = { pigId ->
                    viewModel.selectPig(null)
                    onPigSelected(pigId)
                },
                onQuickEvent = {
                    viewModel.selectPig(null)
                    showFastEventMenuBottomSheet = true
                }
            )
        }

        // Hộp thoại quét mã QR thẻ tai heo bằng Camera
        if (showQrScannerDialog) {
            PigEarTagScannerDialog(
                onDismiss = { showQrScannerDialog = false },
                onTagIdentified = { scannedTag ->
                    showQrScannerDialog = false
                    // Tra cứu heo theo mã quét được
                    viewModel.filterByTagId(scannedTag)
                    // Nếu tìm thấy cá thể heo trong danh sách, mở ngay chi tiết
                    val matchedPig = state.pigs.find { it.tagCode.equals(scannedTag, ignoreCase = true) }
                    if (matchedPig != null) {
                        viewModel.selectPig(matchedPig.id)
                    }
                },
                availableSampleTags = state.pigs.map { it.tagCode }.distinct().take(6)
            )
        }
    }
}

@Composable
fun PigItemCard(
    pig: PigEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 52.dp)
            .testTag("pig_card_${pig.tagCode}")
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Hàng 1: Mã heo • Số lượng / Giới tính --- Căn phải: SyncStatusBadge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = pig.tagCode,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = FarmTextPrimary
                    )
                    Text(
                        text = "•  ${if (pig.isBatch) "${pig.quantity} con" else "${pig.gender} (Lứa ${pig.parity})"}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FarmTextSecondary
                    )
                }

                SyncStatusBadge(status = pig.syncStatus)
            }

            // Hàng 2: Giống • Chuồng nuôi --- Căn phải: ReproductiveStatusBadge + Warning
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${pig.breed}  •  Chuồng: ${pig.location}",
                    fontSize = 12.sp,
                    color = FarmTextSecondary,
                    modifier = Modifier.weight(1f, fill = false),
                    maxLines = 1
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val daysInGestation = if (pig.lastMatingDate != null) {
                        ((System.currentTimeMillis() - pig.lastMatingDate) / 86_400_000L).toInt()
                    } else null

                    ReproductiveStatusBadge(
                        status = pig.status,
                        daysInStatus = daysInGestation
                    )

                    if (pig.hasOverdueVaccine) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFFEBEE),
                            modifier = Modifier.padding(start = 2.dp)
                        ) {
                            Text(
                                text = "⚠️ Tiêm",
                                color = FarmRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    } else if (pig.hasUltrasoundWarning) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFFF3E0),
                            modifier = Modifier.padding(start = 2.dp)
                        ) {
                            Text(
                                text = "⚠️ Siêu âm",
                                color = FarmAmber,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardPillTab(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) FarmGreenPrimary else Color.White,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) FarmGreenPrimary else FarmOutline
        ),
        modifier = modifier.height(30.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 6.dp)) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Color(0xFF4B5563),
                maxLines = 1
            )
        }
    }
}
