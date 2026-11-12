package com.example.ui.pig

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.PigEntity
import com.example.data.model.SowReproductiveStatus
import com.example.ui.components.FarmCatalogs
import com.example.ui.components.PickerOption
import com.example.ui.components.QuickPickerBottomSheet
import com.example.ui.components.QuickPickerField
import com.example.ui.theme.FarmAmber
import com.example.ui.theme.FarmGreenContainer
import com.example.ui.theme.FarmGreenPrimary
import com.example.ui.theme.FarmOutline
import com.example.ui.theme.FarmRed
import com.example.ui.theme.FarmRedContainer
import com.example.ui.theme.FarmTextPrimary
import com.example.ui.theme.FarmTextSecondary
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Định dạng tiền tệ VNĐ có phân cách nghìn
 */
fun formatNumberWithSeparator(input: String): String {
    val digits = input.filter { it.isDigit() }
    if (digits.isEmpty()) return ""
    return try {
        val num = digits.toLong()
        val formatter = DecimalFormat("#,###")
        formatter.format(num).replace(',', '.')
    } catch (e: Exception) {
        digits
    }
}

/**
 * Các loại sự kiện nghiệp vụ trong trang trại (9 sự kiện trọng tâm theo đặc tả)
 */
enum class FastEventType(val title: String, val category: String) {
    // 1. Heo nái
    SOW_INTAKE("+ Nhập đàn nái", "Heo nái"),
    SOW_MATING("Phối giống", "Heo nái"),
    SOW_PREGNANCY_CHECK("Kiểm tra thai", "Heo nái"),
    SOW_RELOCATE("Chuyển chuồng nái", "Heo nái"),
    SOW_FARROWING("Đẻ (Heo con theo mẹ)", "Heo nái"),
    SOW_WEANING("Cai sữa", "Heo nái"),
    SOW_TREATMENT("Điều trị", "Heo nái"),
    SOW_CULLING("Loại thải", "Heo nái"),

    // 2. Heo con
    PIGLET_CARE("Can thiệp theo mẹ", "Heo con"),
    PIGLET_IRON("Tiêm sắt", "Heo con"),
    PIGLET_VACCINE("Vaccine", "Heo con"),
    PIGLET_TAIL_DOCK("Cắt đuôi", "Heo con"),
    PIGLET_WEANING("Cai sữa heo con", "Heo con"),
    PIGLET_CULLING("Loại thải", "Heo con"),

    // 3. Heo thịt
    GROWER_INTAKE("Nhập lô", "Heo thịt"),
    GROWER_RELOCATE("Chuyển chuồng thịt", "Heo thịt"),
    GROWER_SALE("Xuất bán", "Heo thịt"),
    GROWER_WEIGH("Cân trọng lượng", "Heo thịt"),
    GROWER_TREATMENT("Điều trị", "Heo thịt"),
    GROWER_CULLING("Loại thải", "Heo thịt")
}

/**
 * MENU NHẬP SỰ KIỆN NHANH (Khi bấm dấu "+")
 * Phân chia theo 3 nhóm nghiệp vụ: Heo nái, Heo con, Heo thịt.
 * Tối ưu hóa thao tác chạm chọn nhanh bằng 1 tay ngoài chuồng.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FastEventActionMenuBottomSheet(
    onDismiss: () -> Unit,
    onSelectEvent: (FastEventType) -> Unit,
    initialCategoryIndex: Int = 0
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedCategoryIndex by remember { mutableStateOf(initialCategoryIndex.coerceIn(0, 2)) }
    val categories = listOf("Heo nái", "Heo con", "Heo thịt")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("fast_event_menu_bottom_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NHẬP SỰ KIỆN NHANH",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = FarmTextPrimary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "9 sự kiện nghiệp vụ chuẩn hóa • 3-5 thao tác chọn nhanh",
                        fontSize = 12.sp,
                        color = FarmTextSecondary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng", tint = FarmTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab bar 3 nhóm đối tượng
            TabRow(
                selectedTabIndex = selectedCategoryIndex,
                containerColor = Color(0xFFF1F5F1),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedCategoryIndex]),
                        color = FarmGreenPrimary,
                        height = 3.dp
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .padding(2.dp)
            ) {
                categories.forEachIndexed { index, title ->
                    val isSelected = selectedCategoryIndex == index
                    Tab(
                        selected = isSelected,
                        onClick = { selectedCategoryIndex = index },
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

            Spacer(modifier = Modifier.height(16.dp))

            // Danh sách các sự kiện theo nhóm đã chọn
            when (selectedCategoryIndex) {
                0 -> { // HEO NÁI
                    FastEventGroupGrid(
                        events = listOf(
                            FastEventItem(FastEventType.SOW_INTAKE, "+ Nhập đàn nái", Icons.Default.Add, "Nhập nái hậu bị, kiểm tra trùng mã tai"),
                            FastEventItem(FastEventType.SOW_MATING, "Phối giống", Icons.Default.Favorite, "Lên giống, chọn nọc/tinh & sinh 4 việc auto"),
                            FastEventItem(FastEventType.SOW_PREGNANCY_CHECK, "Kiểm tra thai", Icons.Default.Check, "Khám siêu âm (+), (-), (?) D+28..35"),
                            FastEventItem(FastEventType.SOW_RELOCATE, "Chuyển chuồng nái", Icons.Default.SwapHoriz, "Đổi vị trí chuồng mang thai, chuồng đẻ"),
                            FastEventItem(FastEventType.SOW_FARROWING, "Đẻ (Heo con theo mẹ)", Icons.Default.Pets, "Ghi nhận ổ đẻ & tự động tạo đàn theo mẹ"),
                            FastEventItem(FastEventType.SOW_WEANING, "Cai sữa", Icons.Default.SwapHoriz, "Tách mẹ, tự sinh mã đàn con chuẩn hóa"),
                            FastEventItem(FastEventType.SOW_TREATMENT, "Điều trị", Icons.Default.LocalHospital, "Chích thuốc, kháng sinh"),
                            FastEventItem(FastEventType.SOW_CULLING, "Loại thải", Icons.Default.Delete, "Loại nái già, kém sinh sản", isCull = true)
                        ),
                        onSelectEvent = onSelectEvent
                    )
                }
                1 -> { // HEO CON
                    FastEventGroupGrid(
                        events = listOf(
                            FastEventItem(FastEventType.PIGLET_CARE, "Can thiệp theo mẹ (3-in-1)", Icons.Default.MedicalServices, "Tiêm sắt, Cắt đuôi hoặc Vaccine"),
                            FastEventItem(FastEventType.PIGLET_IRON, "Tiêm sắt heo con", Icons.Default.MedicalServices, "D+3 tiêm 1ml Iron Dextran phòng thiếu máu"),
                            FastEventItem(FastEventType.PIGLET_TAIL_DOCK, "Cắt đuôi & bấm nanh", Icons.Default.Pets, "D+7 cắt đuôi, sát trùng Iodine"),
                            FastEventItem(FastEventType.PIGLET_VACCINE, "Vaccine heo con", Icons.Default.LocalHospital, "Suyễn heo, E.coli, Circo PCV2"),
                            FastEventItem(FastEventType.PIGLET_WEANING, "Cai sữa heo con", Icons.Default.SwapHoriz, "D+28 đóng bầy, chuyển sang cai sữa"),
                            FastEventItem(FastEventType.PIGLET_CULLING, "Loại thải", Icons.Default.Delete, "Loại heo con còi cọc, dị tật, dập đè", isCull = true)
                        ),
                        onSelectEvent = onSelectEvent
                    )
                }
                2 -> { // HEO THỊT
                    FastEventGroupGrid(
                        events = listOf(
                            FastEventItem(FastEventType.GROWER_INTAKE, "Nhập lô heo thịt", Icons.Default.Add, "Tự tính ngày tuổi & số lượng tồn đàn con"),
                            FastEventItem(FastEventType.GROWER_RELOCATE, "Chuyển chuồng thịt", Icons.Default.SwapHoriz, "Chuyển ô, ghép đàn & kiểm soát số lượng"),
                            FastEventItem(FastEventType.GROWER_SALE, "Xuất bán", Icons.Default.MonetizationOn, "Đơn giá kg / Tổng tiền & định dạng VNĐ"),
                            FastEventItem(FastEventType.GROWER_WEIGH, "Cân trọng lượng", Icons.Default.Scale, "Tính ADG & dự báo ngày xuất bán"),
                            FastEventItem(FastEventType.GROWER_TREATMENT, "Điều trị", Icons.Default.LocalHospital, "Điều trị tiêu chảy, hô hấp"),
                            FastEventItem(FastEventType.GROWER_CULLING, "Loại thải", Icons.Default.Delete, "Loại heo chấn thương, còi cọc, bại liệt", isCull = true)
                        ),
                        onSelectEvent = onSelectEvent
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

private data class FastEventItem(
    val type: FastEventType,
    val title: String,
    val icon: ImageVector,
    val subtitle: String,
    val isCull: Boolean = false
)

@Composable
private fun FastEventGroupGrid(
    events: List<FastEventItem>,
    onSelectEvent: (FastEventType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        events.forEach { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("fast_event_${item.type.name.lowercase()}")
                    .clickable { onSelectEvent(item.type) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (item.isCull) Color(0xFFFFF3E0) else Color(0xFFF9FAF9)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (item.isCull) FarmAmber.copy(alpha = 0.5f) else FarmOutline
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (item.isCull) FarmAmber.copy(alpha = 0.15f) else FarmGreenContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = if (item.isCull) FarmAmber else FarmGreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.isCull) FarmAmber else FarmTextPrimary
                        )
                        Text(
                            text = item.subtitle,
                            fontSize = 11.sp,
                            color = FarmTextSecondary
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// 1. SỰ KIỆN PHỐI GIỐNG (Mating)
// =============================================================================
@Composable
fun MatingEventDialog(
    availablePigs: List<PigEntity>,
    onDismiss: () -> Unit,
    onSave: (sowTag: String, date: Long, boar: String, tech: String, notes: String) -> Unit
) {
    val sows = availablePigs.filter { !it.isBatch }
    // Bộ lọc nái: ưu tiên nái có trạng thái HAU_BI hoặc CAI_SUA_CHO_PHOI
    val eligibleSows = sows.filter {
        it.status == SowReproductiveStatus.HAU_BI || it.status == SowReproductiveStatus.CAI_SUA_CHO_PHOI
    }.ifEmpty { sows }

    val sowOptions = remember(eligibleSows) {
        eligibleSows.map { sow ->
            PickerOption(
                id = sow.tagCode,
                title = "Nái ${sow.tagCode} • Lứa ${sow.parity}",
                subtitle = "Chuồng: ${sow.location} • Giống: ${sow.breed}",
                badge = sow.status.label,
                badgeColor = if (sow.status == SowReproductiveStatus.HAU_BI) FarmGreenContainer else Color(0xFFE3F2FD),
                badgeTextColor = if (sow.status == SowReproductiveStatus.HAU_BI) FarmGreenPrimary else Color(0xFF1976D2)
            )
        }
    }

    var selectedSowTag by remember { mutableStateOf(sowOptions.firstOrNull()?.id ?: "N001") }
    var selectedBoar by remember { mutableStateOf(FarmCatalogs.BOARS_AND_SEMEN.first().title) }
    var selectedTech by remember { mutableStateOf(FarmCatalogs.TECHNICIANS.first().title) }
    var matingDateStr by remember {
        mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()))
    }
    var notes by remember { mutableStateOf("Lên giống chuẩn, phản xạ đứng im tốt") }

    // Bottom sheet pickers
    var showSowPicker by remember { mutableStateOf(false) }
    var showBoarPicker by remember { mutableStateOf(false) }
    var showTechPicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("dialog_mating_event"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "1. Sự kiện: Phối Giống",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = FarmGreenPrimary
                        )
                        Text(
                            text = "Bộ chọn nhanh nái hậu bị / chờ phối, đực & kỹ thuật viên",
                            fontSize = 11.sp,
                            color = FarmTextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng", tint = FarmTextSecondary)
                    }
                }

                // 1. Mã nái Picker
                QuickPickerField(
                    label = "Mã nái (Hậu bị / Chờ phối)",
                    value = selectedSowTag,
                    icon = Icons.Default.Pets,
                    onClick = { showSowPicker = true },
                    testTag = "mating_sow_picker"
                )

                // 2. Mã đực / Liều tinh Picker
                QuickPickerField(
                    label = "Mã đực phối / Liều tinh",
                    value = selectedBoar,
                    icon = Icons.Default.Favorite,
                    onClick = { showBoarPicker = true },
                    testTag = "mating_boar_picker"
                )

                // 3 & 4. Kỹ thuật viên & Ngày phối trên cùng 1 hàng (2 cột 50-50)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        QuickPickerField(
                            label = "Kỹ thuật viên",
                            value = selectedTech,
                            icon = Icons.Default.Check,
                            onClick = { showTechPicker = true },
                            testTag = "mating_tech_picker"
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = matingDateStr,
                            onValueChange = { matingDateStr = it },
                            label = { Text("Ngày phối") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Ghi chú
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Ghi chú phối giống") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Thông báo tự động
                Surface(
                    color = FarmGreenContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("✓ LOGIC TỰ ĐỘNG SAU KHI LƯU:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FarmGreenPrimary)
                        Text("• Cập nhật trạng thái nái sang: MANG THAI (MANG_THAI)", fontSize = 11.sp, color = FarmGreenPrimary)
                        Text("• Tự động sinh lịch: Kiểm tra lốc sảy (D+18..24)", fontSize = 11.sp, color = FarmGreenPrimary)
                        Text("• Khám / Siêu âm thai (D+28..35)", fontSize = 11.sp, color = FarmGreenPrimary)
                        Text("• Chuẩn bị chuồng đẻ (D+107) & Trực đẻ (D+114)", fontSize = 11.sp, color = FarmGreenPrimary)
                    }
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) { Text("Hủy") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsedDate = try {
                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(matingDateStr)?.time ?: System.currentTimeMillis()
                            } catch (e: Exception) {
                                System.currentTimeMillis()
                            }
                            onSave(selectedSowTag, parsedDate, selectedBoar, selectedTech, notes)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FarmGreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("mating_btn_confirm")
                    ) {
                        Text("Lưu phối giống")
                    }
                }
            }
        }
    }

    if (showSowPicker) {
        QuickPickerBottomSheet(
            title = "Chọn Heo Nái Phối Giống",
            searchPlaceholder = "Tìm theo mã nái, lứa đẻ, chuồng...",
            options = sowOptions,
            selectedId = selectedSowTag,
            onSelect = { selectedSowTag = it.id },
            onDismiss = { showSowPicker = false }
        )
    }

    if (showBoarPicker) {
        QuickPickerBottomSheet(
            title = "Chọn Đực Giống / Liều Tinh",
            searchPlaceholder = "Tìm đực giống Duroc, Pietrain, liều tinh...",
            options = FarmCatalogs.BOARS_AND_SEMEN,
            selectedId = selectedBoar,
            onSelect = { selectedBoar = it.title },
            onDismiss = { showBoarPicker = false }
        )
    }

    if (showTechPicker) {
        QuickPickerBottomSheet(
            title = "Chọn Kỹ Thuật Viên Phối",
            searchPlaceholder = "Tìm nhân viên kỹ thuật...",
            options = FarmCatalogs.TECHNICIANS,
            selectedId = selectedTech,
            onSelect = { selectedTech = it.title },
            onDismiss = { showTechPicker = false }
        )
    }
}

// =============================================================================
// 2. SỰ KIỆN KIỂM TRA THAI (Pregnancy Check)
// =============================================================================
@Composable
fun PregnancyCheckDialog(
    availablePigs: List<PigEntity>,
    onDismiss: () -> Unit,
    onSaveResult: ((sowTag: String, date: Long, result: String) -> Unit)? = null,
    onSave: ((sowTag: String, date: Long, isPregnant: Boolean) -> Unit)? = null
) {
    val sows = availablePigs.filter { !it.isBatch }
    // Ưu tiên nái đang mang thai
    val pregnantSows = sows.filter { it.status == SowReproductiveStatus.MANG_THAI }.ifEmpty { sows }

    val sowOptions = remember(pregnantSows) {
        pregnantSows.map { sow ->
            PickerOption(
                id = sow.tagCode,
                title = "Nái ${sow.tagCode} • Lứa ${sow.parity}",
                subtitle = "Chuồng: ${sow.location} • Giống: ${sow.breed}",
                badge = sow.status.label,
                badgeColor = Color(0xFFE8F5E9),
                badgeTextColor = FarmGreenPrimary
            )
        }
    }

    var selectedSowTag by remember { mutableStateOf(sowOptions.firstOrNull()?.id ?: "N001") }
    var checkDateStr by remember {
        mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()))
    }
    // Kết quả: "PREGNANT" (+) | "NOT_PREGNANT" (-) | "DOUBTFUL" (?)
    var selectedResult by remember { mutableStateOf("PREGNANT") }

    var showSowPicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("dialog_pregnancy_check"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "2. Sự kiện: Kiểm Tra Thai",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = FarmGreenPrimary
                        )
                        Text(
                            text = "Khám siêu âm nái mang thai (D+28..35)",
                            fontSize = 11.sp,
                            color = FarmTextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng", tint = FarmTextSecondary)
                    }
                }

                // 1. Chọn nái mang thai
                QuickPickerField(
                    label = "Mã nái (Đang mang thai)",
                    value = selectedSowTag,
                    icon = Icons.Default.Pets,
                    onClick = { showSowPicker = true },
                    testTag = "preg_sow_picker"
                )

                // 2. Ngày kiểm tra
                OutlinedTextField(
                    value = checkDateStr,
                    onValueChange = { checkDateStr = it },
                    label = { Text("Ngày kiểm tra (dd/MM/yyyy)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // 3. Kết quả chọn nhanh 3 trạng thái
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Kết quả siêu âm:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FarmTextPrimary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val results = listOf(
                            Triple("PREGNANT", "Đậu thai (+)", FarmGreenPrimary),
                            Triple("NOT_PREGNANT", "Không đậu (-)", FarmRed),
                            Triple("DOUBTFUL", "Nghi ngờ (?)", FarmAmber)
                        )
                        results.forEach { (code, label, color) ->
                            val isSelected = selectedResult == code
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) color.copy(alpha = 0.15f) else Color(0xFFF5F5F5),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.5.dp,
                                    if (isSelected) color else FarmOutline
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedResult = code }
                                    .testTag("preg_result_$code")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) color else FarmTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Card logic tự động thích ứng với kết quả
                Surface(
                    color = when (selectedResult) {
                        "PREGNANT" -> FarmGreenContainer
                        "NOT_PREGNANT" -> FarmRedContainer
                        else -> Color(0xFFFFF3E0)
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = "✓ TỰ ĐỘNG HÓA HỆ THỐNG:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (selectedResult) {
                                "PREGNANT" -> FarmGreenPrimary
                                "NOT_PREGNANT" -> FarmRed
                                else -> FarmAmber
                            }
                        )
                        when (selectedResult) {
                            "PREGNANT" -> {
                                Text("• Giữ nguyên trạng thái: MANG THAI (MANG_THAI)", fontSize = 11.sp, color = FarmGreenPrimary)
                                Text("• Tự động kích hoạt mốc: Chuẩn bị chuồng đẻ (D+107)", fontSize = 11.sp, color = FarmGreenPrimary)
                            }
                            "NOT_PREGNANT" -> {
                                Text("• Chuyển trạng thái nái về: CHỜ PHỐI LẠI (CAI_SUA_CHO_PHOI)", fontSize = 11.sp, color = FarmRed)
                                Text("• Cảnh báo lên bảng điều khiển trang trại", fontSize = 11.sp, color = FarmRed)
                                Text("• Tự động hẹn lịch theo dõi lên giống lại sau 3 ngày", fontSize = 11.sp, color = FarmRed)
                            }
                            else -> {
                                Text("• Giữ trạng thái theo dõi nghi ngờ", fontSize = 11.sp, color = FarmAmber)
                                Text("• Tự động xếp lịch siêu âm kiểm tra lại sau 7 ngày (D+7)", fontSize = 11.sp, color = FarmAmber)
                            }
                        }
                    }
                }

                // Buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) { Text("Hủy") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsedDate = try {
                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(checkDateStr)?.time ?: System.currentTimeMillis()
                            } catch (e: Exception) {
                                System.currentTimeMillis()
                            }
                            if (onSaveResult != null) {
                                onSaveResult(selectedSowTag, parsedDate, selectedResult)
                            } else if (onSave != null) {
                                onSave(selectedSowTag, parsedDate, selectedResult == "PREGNANT")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (selectedResult) {
                                "PREGNANT" -> FarmGreenPrimary
                                "NOT_PREGNANT" -> FarmRed
                                else -> FarmAmber
                            }
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("preg_btn_confirm")
                    ) {
                        Text("Lưu kết quả")
                    }
                }
            }
        }
    }

    if (showSowPicker) {
        QuickPickerBottomSheet(
            title = "Chọn Heo Nái Khám Thai",
            searchPlaceholder = "Tìm nái đang mang thai...",
            options = sowOptions,
            selectedId = selectedSowTag,
            onSelect = { selectedSowTag = it.id },
            onDismiss = { showSowPicker = false }
        )
    }
}

// =============================================================================
// 3. SỰ KIỆN CAI SỮA HEO CON (Weaning)
// =============================================================================
@Composable
fun WeaningEventDialog(
    availableBatches: List<PigEntity>,
    onDismiss: () -> Unit,
    onSaveWeaning: ((sowTag: String, targetPen: String, weanedQty: Int, totalWeight: Float, date: Long) -> Unit)? = null,
    onSave: ((pigletTag: String, date: Long, weanedQty: Int, avgWeight: Float, sowTag: String?) -> Unit)? = null
) {
    // Lấy danh sách nái đang nuôi con
    val sows = availableBatches.filter { !it.isBatch }
    val nursingSows = sows.filter { it.status == SowReproductiveStatus.NUOI_CON }.ifEmpty { sows }

    val sowOptions = remember(nursingSows) {
        nursingSows.map { sow ->
            PickerOption(
                id = sow.tagCode,
                title = "Nái mẹ ${sow.tagCode} • Lứa ${sow.parity}",
                subtitle = "Chuồng đẻ: ${sow.location} • Giống: ${sow.breed}",
                badge = "Nuôi con",
                badgeColor = Color(0xFFE8F5E9),
                badgeTextColor = FarmGreenPrimary
            )
        }
    }

    var selectedSowTag by remember { mutableStateOf(sowOptions.firstOrNull()?.id ?: "N001") }
    var selectedGrowerPen by remember { mutableStateOf(FarmCatalogs.GROWER_PENS.first().title) }
    var weanedQtyStr by remember { mutableStateOf("10") }
    var totalWeightStr by remember { mutableStateOf("75.0") }
    var weaningDateStr by remember {
        mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()))
    }

    // Auto-calculated fields
    val weanedQty = weanedQtyStr.toIntOrNull() ?: 10
    val totalWeight = totalWeightStr.toFloatOrNull() ?: 75f
    val avgWeight = if (weanedQty > 0) totalWeight / weanedQty else 0f

    // Sinh mã đàn con tự động theo công thức: "Heo con " + Mã Nái mẹ + " " + dd/MM/yy
    val autoBatchCode = remember(selectedSowTag, weaningDateStr) {
        val shortDate = try {
            val d = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(weaningDateStr) ?: Date()
            SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(d)
        } catch (e: Exception) {
            "26/10/27"
        }
        "Heo con $selectedSowTag $shortDate"
    }

    var showSowPicker by remember { mutableStateOf(false) }
    var showPenPicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("dialog_weaning_event"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "3. Sự kiện: Cai Sữa Heo Con",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = FarmGreenPrimary
                        )
                        Text(
                            text = "Đóng chu kỳ nuôi con & tự động tạo đàn heo con cai sữa",
                            fontSize = 11.sp,
                            color = FarmTextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng", tint = FarmTextSecondary)
                    }
                }

                // 1. Mã nái mẹ
                QuickPickerField(
                    label = "Mã nái mẹ (Đang nuôi con)",
                    value = selectedSowTag,
                    icon = Icons.Default.Pets,
                    onClick = { showSowPicker = true },
                    testTag = "weaning_sow_picker"
                )

                // 2. Chuồng nuôi heo thịt / cai sữa
                QuickPickerField(
                    label = "Chuồng nhận heo cai sữa",
                    value = selectedGrowerPen,
                    icon = Icons.Default.SwapHoriz,
                    onClick = { showPenPicker = true },
                    testTag = "weaning_pen_picker"
                )

                // 3. Số lượng & Khối lượng tổng
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = weanedQtyStr,
                        onValueChange = { weanedQtyStr = it },
                        label = { Text("Số con cai sữa") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("weaning_qty_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = totalWeightStr,
                        onValueChange = { totalWeightStr = it },
                        label = { Text("Tổng khối lượng (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("weaning_total_weight_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Hiển thị tính toán khối lượng TB tự động
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = FarmGreenContainer)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Khối lượng trung bình:", fontSize = 12.sp, color = FarmGreenPrimary, fontWeight = FontWeight.Medium)
                        Text(
                            text = "${String.format(Locale.US, "%.1f", avgWeight)} kg/con",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = FarmGreenPrimary
                        )
                    }
                }

                // Hộp hiển thị mã đàn con tự động sinh
                Surface(
                    color = Color(0xFFF1F8E9),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FarmGreenPrimary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("MÃ ĐÀN CON TỰ ĐỘNG SINH:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FarmGreenPrimary)
                        Text(autoBatchCode, fontSize = 14.sp, fontWeight = FontWeight.Black, color = FarmGreenPrimary)
                        Text("• Nái $selectedSowTag chuyển sang: CAI SỮA CHỜ PHỐI", fontSize = 11.sp, color = FarmTextSecondary)
                        Text("• Kích hoạt theo dõi động dục sau cai sữa (D+5)", fontSize = 11.sp, color = FarmTextSecondary)
                        Text("• Tạo đàn mới tại: $selectedGrowerPen ($weanedQty con)", fontSize = 11.sp, color = FarmTextSecondary)
                    }
                }

                // Buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) { Text("Hủy") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsedDate = try {
                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(weaningDateStr)?.time ?: System.currentTimeMillis()
                            } catch (e: Exception) {
                                System.currentTimeMillis()
                            }
                            if (onSaveWeaning != null) {
                                onSaveWeaning(selectedSowTag, selectedGrowerPen, weanedQty, totalWeight, parsedDate)
                            } else if (onSave != null) {
                                onSave(selectedSowTag, parsedDate, weanedQty, avgWeight, selectedSowTag)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FarmGreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("weaning_btn_confirm")
                    ) {
                        Text("Lưu cai sữa")
                    }
                }
            }
        }
    }

    if (showSowPicker) {
        QuickPickerBottomSheet(
            title = "Chọn Heo Nái Đang Nuôi Con",
            searchPlaceholder = "Tìm nái đang nuôi con...",
            options = sowOptions,
            selectedId = selectedSowTag,
            onSelect = { selectedSowTag = it.id },
            onDismiss = { showSowPicker = false }
        )
    }

    if (showPenPicker) {
        QuickPickerBottomSheet(
            title = "Chọn Chuồng Heo Thịt / Cai Sữa",
            searchPlaceholder = "Tìm chuồng cai sữa, chuồng thịt...",
            options = FarmCatalogs.GROWER_PENS,
            selectedId = selectedGrowerPen,
            onSelect = { selectedGrowerPen = it.title },
            onDismiss = { showPenPicker = false }
        )
    }
}

// =============================================================================
// 4. SỰ KIỆN HEO CON THEO MẸ (Tiêm sắt, Cắt đuôi, Vaccine)
// =============================================================================
@Composable
fun PigletCareEventDialog(
    availableBatches: List<PigEntity>,
    onDismiss: () -> Unit,
    onSave: (pigletTag: String, date: Long, procedureType: String, vaccineName: String, tech: String, dosage: String, notes: String) -> Unit
) {
    // Đàn heo con
    val pigletBatches = availableBatches.filter {
        it.isBatch && (it.tagCode.startsWith("DC") || it.tagCode.contains("heo con", ignoreCase = true) || it.status == SowReproductiveStatus.NUOI_CON)
    }.ifEmpty { availableBatches.filter { it.isBatch } }

    val batchOptions = remember(pigletBatches) {
        pigletBatches.map { b ->
            PickerOption(
                id = b.tagCode,
                title = b.tagCode,
                subtitle = "Số lượng: ${b.quantity} con • Vị trí: ${b.location}",
                badge = "Theo mẹ",
                badgeColor = Color(0xFFE8F5E9),
                badgeTextColor = FarmGreenPrimary
            )
        }
    }

    var selectedBatchTag by remember { mutableStateOf(batchOptions.firstOrNull()?.id ?: "DC20270626-001") }
    var selectedProcedure by remember { mutableStateOf("Tiêm sắt (Iron Dextran)") }
    var selectedTech by remember { mutableStateOf(FarmCatalogs.TECHNICIANS.first().title) }
    var eventDateStr by remember {
        mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()))
    }
    var dosageStr by remember { mutableStateOf("1.0 ml/con") }
    var notes by remember { mutableStateOf("Tiêm bắp sau tai, tiệt trùng kim tiêm") }

    var showBatchPicker by remember { mutableStateOf(false) }
    var showProcedurePicker by remember { mutableStateOf(false) }
    var showTechPicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("dialog_piglet_care"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "4. Sự kiện: Can Thiệp Heo Con",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = FarmGreenPrimary
                        )
                        Text(
                            text = "Tiêm sắt D+3, Cắt đuôi D+7 hoặc Vaccine theo mẹ",
                            fontSize = 11.sp,
                            color = FarmTextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng", tint = FarmTextSecondary)
                    }
                }

                // 1. Mã đàn con
                QuickPickerField(
                    label = "Mã đàn heo con theo mẹ",
                    value = selectedBatchTag,
                    icon = Icons.Default.Pets,
                    onClick = { showBatchPicker = true },
                    testTag = "piglet_care_batch_picker"
                )

                // 2. Loại thủ thuật / can thiệp
                QuickPickerField(
                    label = "Loại thủ thuật / Can thiệp / Vaccine",
                    value = selectedProcedure,
                    icon = Icons.Default.MedicalServices,
                    onClick = { showProcedurePicker = true },
                    testTag = "piglet_care_procedure_picker"
                )

                // 3. Người thực hiện
                QuickPickerField(
                    label = "Kỹ thuật viên thực hiện",
                    value = selectedTech,
                    icon = Icons.Default.Check,
                    onClick = { showTechPicker = true },
                    testTag = "piglet_care_tech_picker"
                )

                // 4. Ngày & Liều lượng
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = eventDateStr,
                        onValueChange = { eventDateStr = it },
                        label = { Text("Ngày thực hiện") },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = dosageStr,
                        onValueChange = { dosageStr = it },
                        label = { Text("Liều lượng") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Ghi chú can thiệp") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Surface(
                    color = FarmGreenContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("✓ Ghi nhận ngay vào sổ tay thú y đàn $selectedBatchTag", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FarmGreenPrimary)
                        Text("✓ Cập nhật lịch phòng bệnh định kỳ toàn trại", fontSize = 11.sp, color = FarmGreenPrimary)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) { Text("Hủy") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsedDate = try {
                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(eventDateStr)?.time ?: System.currentTimeMillis()
                            } catch (e: Exception) {
                                System.currentTimeMillis()
                            }
                            val pType = if (selectedProcedure.contains("sắt", ignoreCase = true)) {
                                "Tiêm sắt"
                            } else if (selectedProcedure.contains("đuôi", ignoreCase = true)) {
                                "Cắt đuôi"
                            } else {
                                "Vaccine"
                            }
                            onSave(selectedBatchTag, parsedDate, pType, selectedProcedure, selectedTech, dosageStr, notes)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FarmGreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("piglet_care_btn_confirm")
                    ) {
                        Text("Lưu can thiệp")
                    }
                }
            }
        }
    }

    if (showBatchPicker) {
        QuickPickerBottomSheet(
            title = "Chọn Đàn Heo Con Theo Mẹ",
            searchPlaceholder = "Tìm theo mã đàn DC...",
            options = batchOptions,
            selectedId = selectedBatchTag,
            onSelect = { selectedBatchTag = it.id },
            onDismiss = { showBatchPicker = false }
        )
    }

    if (showProcedurePicker) {
        QuickPickerBottomSheet(
            title = "Chọn Thủ Thuật / Vaccine",
            searchPlaceholder = "Tìm tiêm sắt, cắt đuôi, Circo, Suyễn...",
            options = FarmCatalogs.PROCEDURES_AND_VACCINES,
            selectedId = selectedProcedure,
            onSelect = {
                selectedProcedure = it.title
                if (it.title.contains("sắt", ignoreCase = true)) {
                    dosageStr = "1.0 ml/con"
                } else if (it.title.contains("Vaccine", ignoreCase = true)) {
                    dosageStr = "2.0 ml/con"
                }
            },
            onDismiss = { showProcedurePicker = false }
        )
    }

    if (showTechPicker) {
        QuickPickerBottomSheet(
            title = "Chọn Kỹ Thuật Viên",
            searchPlaceholder = "Tìm nhân viên thú y phụ trách...",
            options = FarmCatalogs.TECHNICIANS,
            selectedId = selectedTech,
            onSelect = { selectedTech = it.title },
            onDismiss = { showTechPicker = false }
        )
    }
}

// =============================================================================
// 5. SỰ KIỆN NHẬP LÔ HEO THỊT (Grower Intake)
// =============================================================================
@Composable
fun GrowerIntakeDialog(
    availableBatches: List<PigEntity>,
    onDismiss: () -> Unit,
    onSave: (batchCode: String, intakeDate: Long, quantity: Int, avgWeight: Float, receivingPen: String) -> Unit
) {
    // Đàn con có sẵn có thể nhập lô
    val pigletBatches = availableBatches.filter {
        it.isBatch && it.quantity > 0 && !it.tagCode.startsWith("LT")
    }.ifEmpty { availableBatches.filter { it.isBatch } }

    val batchOptions = remember(pigletBatches) {
        pigletBatches.map { b ->
            val remaining = b.quantity
            PickerOption(
                id = b.tagCode,
                title = b.tagCode,
                subtitle = "Tồn đàn: $remaining con • Vị trí: ${b.location}",
                badge = "$remaining con",
                badgeColor = Color(0xFFE8F5E9),
                badgeTextColor = FarmGreenPrimary,
                extraData = b
            )
        }
    }

    var selectedBatchTag by remember { mutableStateOf(batchOptions.firstOrNull()?.id ?: "DC20270626-001") }
    var selectedPen by remember { mutableStateOf(FarmCatalogs.GROWER_PENS.first().title) }
    var intakeQtyStr by remember { mutableStateOf("10") }
    var avgWeightStr by remember { mutableStateOf("22.0") }
    var intakeDateStr by remember {
        mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()))
    }

    val selectedBatchEntity = remember(selectedBatchTag, pigletBatches) {
        pigletBatches.find { it.tagCode == selectedBatchTag }
    }

    // Tự động tính toán: Số lượng còn lại & Độ tuổi (ngày / tuần)
    val remainingQty = selectedBatchEntity?.quantity ?: 10
    val ageDays = remember(selectedBatchEntity) {
        if (selectedBatchEntity != null) {
            ((System.currentTimeMillis() - selectedBatchEntity.birthDate) / 86_400_000L).coerceAtLeast(0)
        } else 35L
    }
    val ageWeeks = remember(ageDays) {
        String.format(Locale.US, "%.1f", ageDays / 7.0)
    }

    var showBatchPicker by remember { mutableStateOf(false) }
    var showPenPicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("dialog_grower_intake"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "5. Sự kiện: Nhập Lô Heo Thịt",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = FarmGreenPrimary
                        )
                        Text(
                            text = "Tự động tính ngày tuổi, tuần tuổi và số lượng tồn đàn con",
                            fontSize = 11.sp,
                            color = FarmTextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng", tint = FarmTextSecondary)
                    }
                }

                // 1. Mã đàn con Picker
                QuickPickerField(
                    label = "Mã Đàn con nguồn",
                    value = selectedBatchTag,
                    icon = Icons.Default.Pets,
                    onClick = { showBatchPicker = true },
                    testTag = "intake_source_batch_picker"
                )

                // Hộp hiển thị tự động: Số lượng tồn & Độ tuổi (read-only)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = FarmGreenContainer)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Số con khả dụng:", fontSize = 11.sp, color = FarmGreenPrimary)
                            Text("$remainingQty con", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FarmGreenPrimary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Độ tuổi đàn:", fontSize = 11.sp, color = FarmGreenPrimary)
                            Text("$ageDays ngày ($ageWeeks tuần)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FarmGreenPrimary)
                        }
                    }
                }

                // 2. Chuồng nhận
                QuickPickerField(
                    label = "Chuồng nhận nuôi thịt",
                    value = selectedPen,
                    icon = Icons.Default.SwapHoriz,
                    onClick = { showPenPicker = true },
                    testTag = "intake_pen_picker"
                )

                // 3. Số lượng nhập & Khối lượng TB
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = intakeQtyStr,
                        onValueChange = { intakeQtyStr = it },
                        label = { Text("Số lượng nhập") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("intake_qty_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = avgWeightStr,
                        onValueChange = { avgWeightStr = it },
                        label = { Text("KL trung bình (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("intake_weight_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = intakeDateStr,
                    onValueChange = { intakeDateStr = it },
                    label = { Text("Ngày nhập (dd/MM/yyyy)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) { Text("Hủy") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsedDate = try {
                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(intakeDateStr)?.time ?: System.currentTimeMillis()
                            } catch (e: Exception) {
                                System.currentTimeMillis()
                            }
                            val qty = intakeQtyStr.toIntOrNull() ?: remainingQty
                            val weight = avgWeightStr.toFloatOrNull() ?: 22f
                            onSave(selectedBatchTag, parsedDate, qty, weight, selectedPen)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FarmGreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("intake_btn_confirm")
                    ) {
                        Text("Lưu nhập lô")
                    }
                }
            }
        }
    }

    if (showBatchPicker) {
        QuickPickerBottomSheet(
            title = "Chọn Đàn Con Nhập Lô Thịt",
            searchPlaceholder = "Tìm đàn con có sẵn...",
            options = batchOptions,
            selectedId = selectedBatchTag,
            onSelect = { selectedBatchTag = it.id },
            onDismiss = { showBatchPicker = false }
        )
    }

    if (showPenPicker) {
        QuickPickerBottomSheet(
            title = "Chọn Chuồng Nhận Thịt",
            searchPlaceholder = "Tìm chuồng nuôi thịt...",
            options = FarmCatalogs.GROWER_PENS,
            selectedId = selectedPen,
            onSelect = { selectedPen = it.title },
            onDismiss = { showPenPicker = false }
        )
    }
}

// =============================================================================
// 6. FORM CHUYỂN CHUỒNG HEO THỊT (Grower Relocation)
// =============================================================================
@Composable
fun GrowerRelocationDialog(
    availableBatches: List<PigEntity>,
    onDismiss: () -> Unit,
    onSave: (fromPen: String, toPen: String, moveDate: Long, quantity: Int, totalWeightKg: Float, avgAgeDays: Int, batchCode: String, notes: String) -> Unit
) {
    val growerBatches = availableBatches.filter {
        it.isBatch && (it.tagCode.startsWith("LT") || it.tagCode.startsWith("CS") || it.tagCode.contains("thịt", ignoreCase = true) || it.status == SowReproductiveStatus.DANG_NUOI)
    }.ifEmpty { availableBatches.filter { it.isBatch } }

    val batchOptions = remember(growerBatches) {
        growerBatches.map { b ->
            PickerOption(
                id = b.tagCode,
                title = "${b.tagCode} • Chuồng: ${b.location}",
                subtitle = "Hiện diện: ${b.quantity} con • KL TB: ${b.currentWeightKg} kg",
                badge = "${b.quantity} con",
                badgeColor = Color(0xFFE8F5E9),
                badgeTextColor = FarmGreenPrimary,
                extraData = b
            )
        }
    }

    var selectedBatchCode by remember { mutableStateOf(batchOptions.firstOrNull()?.id ?: "LT001") }
    val currentBatch = remember(selectedBatchCode, growerBatches) {
        growerBatches.find { it.tagCode == selectedBatchCode }
    }
    val sourcePen = currentBatch?.location ?: "Khu A-01"
    val maxAvailableQty = currentBatch?.quantity ?: 100

    var destPen by remember { mutableStateOf(FarmCatalogs.GROWER_PENS.last().title) }
    var moveDateStr by remember {
        mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()))
    }
    var qtyStr by remember { mutableStateOf(maxAvailableQty.toString()) }
    var totalWeightStr by remember {
        val totalW = (currentBatch?.currentWeightKg ?: 45f) * (qtyStr.toIntOrNull() ?: maxAvailableQty)
        mutableStateOf(String.format(Locale.US, "%.1f", totalW))
    }
    var notes by remember { mutableStateOf("Tách ghép đàn, phân loại thể trạng đồng đều") }

    val qty = qtyStr.toIntOrNull() ?: 0
    val isOverLimit = qty > maxAvailableQty || qty <= 0

    // Tính tuổi trung bình tự động từ ngày đẻ/nhập của lô nguồn
    val avgAgeDays = remember(currentBatch) {
        if (currentBatch != null) {
            ((System.currentTimeMillis() - currentBatch.birthDate) / 86_400_000L).coerceAtLeast(0).toInt()
        } else 75
    }

    var showBatchPicker by remember { mutableStateOf(false) }
    var showDestPenPicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("dialog_grower_relocation"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "6. Chuyển Chuồng Heo Thịt",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = FarmGreenPrimary
                        )
                        Text(
                            text = "Ràng buộc số lượng chuyển <= số tồn chuồng chuyển đi",
                            fontSize = 11.sp,
                            color = FarmTextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng", tint = FarmTextSecondary)
                    }
                }

                // 1. Chuồng chuyển đi / Lô nguồn
                QuickPickerField(
                    label = "Chuồng chuyển đi (Lô heo nguồn)",
                    value = "$selectedBatchCode ($sourcePen)",
                    icon = Icons.Default.SwapHoriz,
                    onClick = { showBatchPicker = true },
                    testTag = "grower_relocate_src_picker"
                )

                // 2. Chuồng nhận
                QuickPickerField(
                    label = "Chuồng nhận heo thịt",
                    value = destPen,
                    icon = Icons.Default.Check,
                    onClick = { showDestPenPicker = true },
                    testTag = "grower_relocate_dest_picker"
                )

                // 3. Số lượng con (validation ràng buộc)
                OutlinedTextField(
                    value = qtyStr,
                    onValueChange = { qtyStr = it },
                    label = { Text("Số con chuyển (Tối đa: $maxAvailableQty con)") },
                    isError = isOverLimit,
                    supportingText = {
                        if (isOverLimit) {
                            Text("⚠️ Không thể chuyển quá số tồn ($maxAvailableQty con)", color = FarmRed, fontSize = 11.sp)
                        } else {
                            Text("Tuổi trung bình: $avgAgeDays ngày (${String.format(Locale.US, "%.1f", avgAgeDays / 7.0)} tuần)", fontSize = 11.sp)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("grower_relocate_qty_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // 4. Tổng khối lượng & Ngày chuyển
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = totalWeightStr,
                        onValueChange = { totalWeightStr = it },
                        label = { Text("Tổng KL (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = moveDateStr,
                        onValueChange = { moveDateStr = it },
                        label = { Text("Ngày chuyển") },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Ghi chú") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) { Text("Hủy") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsedDate = try {
                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(moveDateStr)?.time ?: System.currentTimeMillis()
                            } catch (e: Exception) {
                                System.currentTimeMillis()
                            }
                            val w = totalWeightStr.toFloatOrNull() ?: 0f
                            onSave(sourcePen, destPen, parsedDate, qty, w, avgAgeDays, selectedBatchCode, notes)
                        },
                        enabled = !isOverLimit && qty > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = FarmGreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("grower_relocate_btn_confirm")
                    ) {
                        Text("Xác nhận chuyển")
                    }
                }
            }
        }
    }

    if (showBatchPicker) {
        QuickPickerBottomSheet(
            title = "Chọn Lô Heo Nguồn",
            searchPlaceholder = "Tìm lô heo chuyển đi...",
            options = batchOptions,
            selectedId = selectedBatchCode,
            onSelect = {
                selectedBatchCode = it.id
                val b = it.extraData as? PigEntity
                qtyStr = (b?.quantity ?: 100).toString()
            },
            onDismiss = { showBatchPicker = false }
        )
    }

    if (showDestPenPicker) {
        QuickPickerBottomSheet(
            title = "Chọn Chuồng Nhận",
            searchPlaceholder = "Tìm chuồng nhận heo thịt...",
            options = FarmCatalogs.GROWER_PENS,
            selectedId = destPen,
            onSelect = { destPen = it.title },
            onDismiss = { showDestPenPicker = false }
        )
    }
}

// =============================================================================
// 7. FORM CHUYỂN CHUỒNG HEO NÁI (Sow Relocation)
// =============================================================================
@Composable
fun SowRelocationDialog(
    availablePigs: List<PigEntity>,
    onDismiss: () -> Unit,
    onSave: (sowTag: String, moveDate: Long, fromLocation: String, toLocation: String, notes: String) -> Unit
) {
    val sows = availablePigs.filter { !it.isBatch }

    val sowOptions = remember(sows) {
        sows.map { s ->
            PickerOption(
                id = s.tagCode,
                title = "Nái ${s.tagCode} • Lứa ${s.parity}",
                subtitle = "Hiện tại: ${s.location} • Giống: ${s.breed}",
                badge = s.status.label,
                badgeColor = Color(0xFFE8F5E9),
                badgeTextColor = FarmGreenPrimary,
                extraData = s
            )
        }
    }

    var selectedSowTag by remember { mutableStateOf(sowOptions.firstOrNull()?.id ?: "N001") }
    val currentSow = remember(selectedSowTag, sows) {
        sows.find { it.tagCode == selectedSowTag }
    }
    val fromLocation = currentSow?.location ?: "Chuồng Bầu 01"
    val sowStatusLabel = currentSow?.status?.label ?: "Mang thai"

    var toLocation by remember { mutableStateOf(FarmCatalogs.SOW_PENS.find { it.title != fromLocation }?.title ?: "Chuồng Đẻ 01") }
    var moveDateStr by remember {
        mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()))
    }
    var notes by remember { mutableStateOf("Chuyển nái chuẩn bị đẻ D+107") }

    var showSowPicker by remember { mutableStateOf(false) }
    var showDestPenPicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("dialog_sow_relocation"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "7. Chuyển Chuồng Heo Nái",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = FarmGreenPrimary
                        )
                        Text(
                            text = "Tự động điền chuồng hiện tại & trạng thái sinh sản",
                            fontSize = 11.sp,
                            color = FarmTextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng", tint = FarmTextSecondary)
                    }
                }

                // 1. Mã nái Picker
                QuickPickerField(
                    label = "Mã heo nái",
                    value = selectedSowTag,
                    icon = Icons.Default.Pets,
                    onClick = { showSowPicker = true },
                    testTag = "sow_relocate_sow_picker"
                )

                // Hộp thông tin hiện tại (read-only auto filled)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = FarmGreenContainer)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Chuồng chuyển đi:", fontSize = 11.sp, color = FarmGreenPrimary)
                            Text(fromLocation, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FarmGreenPrimary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Trạng thái hiện tại:", fontSize = 11.sp, color = FarmGreenPrimary)
                            Text(sowStatusLabel, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FarmGreenPrimary)
                        }
                    }
                }

                // 2. Chuồng nhận Picker
                QuickPickerField(
                    label = "Chuồng nhận (Vị trí mới)",
                    value = toLocation,
                    icon = Icons.Default.SwapHoriz,
                    onClick = { showDestPenPicker = true },
                    testTag = "sow_relocate_dest_picker"
                )

                // 3. Ngày chuyển
                OutlinedTextField(
                    value = moveDateStr,
                    onValueChange = { moveDateStr = it },
                    label = { Text("Ngày chuyển (dd/MM/yyyy)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Ghi chú") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) { Text("Hủy") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsedDate = try {
                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(moveDateStr)?.time ?: System.currentTimeMillis()
                            } catch (e: Exception) {
                                System.currentTimeMillis()
                            }
                            onSave(selectedSowTag, parsedDate, fromLocation, toLocation, notes)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FarmGreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("sow_relocate_btn_confirm")
                    ) {
                        Text("Xác nhận chuyển")
                    }
                }
            }
        }
    }

    if (showSowPicker) {
        QuickPickerBottomSheet(
            title = "Chọn Heo Nái Chuyển Chuồng",
            searchPlaceholder = "Tìm theo mã nái, chuồng...",
            options = sowOptions,
            selectedId = selectedSowTag,
            onSelect = { selectedSowTag = it.id },
            onDismiss = { showSowPicker = false }
        )
    }

    if (showDestPenPicker) {
        QuickPickerBottomSheet(
            title = "Chọn Chuồng Nái Mới",
            searchPlaceholder = "Tìm chuồng nái bầu, lồng đẻ...",
            options = FarmCatalogs.SOW_PENS,
            selectedId = toLocation,
            onSelect = { toLocation = it.title },
            onDismiss = { showDestPenPicker = false }
        )
    }
}

// =============================================================================
// 8. FORM SỰ KIỆN XUẤT BÁN HEO THỊT (Grower Sale)
// =============================================================================
@Composable
fun GrowerSaleDialog(
    availableBatches: List<PigEntity>,
    onDismiss: () -> Unit,
    onSaveSale: (batchCode: String, saleDate: Long, quantity: Int, totalWeightKg: Float, salePriceVnd: String, buyerName: String) -> Unit
) {
    val growerBatches = availableBatches.filter {
        it.isBatch && (it.tagCode.startsWith("LT") || it.tagCode.contains("thịt", ignoreCase = true) || it.status == SowReproductiveStatus.DANG_NUOI)
    }.ifEmpty { availableBatches.filter { it.isBatch } }

    val batchOptions = remember(growerBatches) {
        growerBatches.map { b ->
            PickerOption(
                id = b.tagCode,
                title = "${b.tagCode} • Chuồng: ${b.location}",
                subtitle = "Số lượng tồn: ${b.quantity} con • KL TB: ${b.currentWeightKg} kg",
                badge = "${b.quantity} con",
                badgeColor = Color(0xFFE8F5E9),
                badgeTextColor = FarmGreenPrimary,
                extraData = b
            )
        }
    }

    var selectedBatchCode by remember { mutableStateOf(batchOptions.firstOrNull()?.id ?: "LT001") }
    val currentBatch = remember(selectedBatchCode, growerBatches) {
        growerBatches.find { it.tagCode == selectedBatchCode }
    }
    val maxAvailableQty = currentBatch?.quantity ?: 98

    var saleQtyStr by remember { mutableStateOf(maxAvailableQty.toString()) }
    var totalWeightStr by remember {
        val total = (currentBatch?.currentWeightKg ?: 110f) * (saleQtyStr.toIntOrNull() ?: maxAvailableQty)
        mutableStateOf(String.format(Locale.US, "%.1f", total))
    }
    var saleDateStr by remember {
        mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()))
    }

    // Giá bán VNĐ & Chế độ nhập (Đơn giá/kg vs Tổng tiền)
    var isPricePerKg by remember { mutableStateOf(true) }
    var rawPriceInput by remember { mutableStateOf("65000") }
    var selectedTrader by remember { mutableStateOf(FarmCatalogs.TRADERS.first().title) }

    val formattedPrice = remember(rawPriceInput) {
        formatNumberWithSeparator(rawPriceInput)
    }

    // Tự động tính toán tổng giá trị xuất bán
    val totalWeight = totalWeightStr.toFloatOrNull() ?: 0f
    val numericPrice = rawPriceInput.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val calculatedTotalRevenue = remember(isPricePerKg, totalWeight, numericPrice) {
        if (isPricePerKg) {
            (totalWeight * numericPrice).toLong()
        } else {
            numericPrice
        }
    }

    var showBatchPicker by remember { mutableStateOf(false) }
    var showTraderPicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("dialog_grower_sale"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "8. Xuất Bán Heo Thịt",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = FarmGreenPrimary
                        )
                        Text(
                            text = "Đơn giá kg / Tổng tiền • Tự động format dấu phân cách VNĐ",
                            fontSize = 11.sp,
                            color = FarmTextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng", tint = FarmTextSecondary)
                    }
                }

                // 1. Lô xuất
                QuickPickerField(
                    label = "Lô heo xuất bán",
                    value = selectedBatchCode,
                    icon = Icons.Default.MonetizationOn,
                    onClick = { showBatchPicker = true },
                    testTag = "sale_batch_picker"
                )

                // 2. Số lượng & Tổng khối lượng
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = saleQtyStr,
                        onValueChange = { saleQtyStr = it },
                        label = { Text("Số lượng (Max $maxAvailableQty)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("sale_qty_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = totalWeightStr,
                        onValueChange = { totalWeightStr = it },
                        label = { Text("Tổng KL (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("sale_weight_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // 3. Chế độ giá: Đơn giá / kg VS Tổng tiền lô
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Chế độ nhập giá:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FarmTextPrimary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isPricePerKg) FarmGreenPrimary else Color(0xFFF1F5F1),
                                modifier = Modifier.clickable { isPricePerKg = true }
                            ) {
                                Text(
                                    "Đơn giá/kg",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPricePerKg) Color.White else FarmTextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (!isPricePerKg) FarmGreenPrimary else Color(0xFFF1F5F1),
                                modifier = Modifier.clickable { isPricePerKg = false }
                            ) {
                                Text(
                                    "Tổng tiền lô",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isPricePerKg) Color.White else FarmTextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = formattedPrice,
                        onValueChange = { rawPriceInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text(if (isPricePerKg) "Giá bán (VNĐ / kg)" else "Tổng tiền lô (VNĐ)") },
                        trailingIcon = { Text("VNĐ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FarmGreenPrimary, modifier = Modifier.padding(end = 12.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("sale_price_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Card tự động tính doanh thu dự kiến
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = FarmGreenContainer)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("TỔNG GIÁ TRỊ LÔ XUẤT:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FarmGreenPrimary)
                        Text(
                            text = "${formatNumberWithSeparator(calculatedTotalRevenue.toString())} VNĐ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = FarmGreenPrimary
                        )
                        val avgW = if ((saleQtyStr.toIntOrNull() ?: 1) > 0) totalWeight / (saleQtyStr.toIntOrNull() ?: 1) else 0f
                        Text("Khối lượng trung bình: ${String.format(Locale.US, "%.1f", avgW)} kg/con", fontSize = 11.sp, color = FarmGreenPrimary)
                    }
                }

                // 4. Khách hàng / Thương lái Picker
                QuickPickerField(
                    label = "Khách hàng / Thương lái thu mua",
                    value = selectedTrader,
                    icon = Icons.Default.ShoppingCart,
                    onClick = { showTraderPicker = true },
                    testTag = "sale_trader_picker"
                )

                // 5. Ngày xuất
                OutlinedTextField(
                    value = saleDateStr,
                    onValueChange = { saleDateStr = it },
                    label = { Text("Ngày xuất bán (dd/MM/yyyy)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) { Text("Hủy") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsedDate = try {
                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(saleDateStr)?.time ?: System.currentTimeMillis()
                            } catch (e: Exception) {
                                System.currentTimeMillis()
                            }
                            val qty = saleQtyStr.toIntOrNull() ?: maxAvailableQty
                            val priceStrFormatted = "${formatNumberWithSeparator(calculatedTotalRevenue.toString())} VNĐ"
                            onSaveSale(selectedBatchCode, parsedDate, qty, totalWeight, priceStrFormatted, selectedTrader)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FarmGreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("sale_btn_confirm")
                    ) {
                        Text("Lưu xuất bán")
                    }
                }
            }
        }
    }

    if (showBatchPicker) {
        QuickPickerBottomSheet(
            title = "Chọn Lô Heo Xuất Bán",
            searchPlaceholder = "Tìm lô heo đến tuổi xuất...",
            options = batchOptions,
            selectedId = selectedBatchCode,
            onSelect = { selectedBatchCode = it.id },
            onDismiss = { showBatchPicker = false }
        )
    }

    if (showTraderPicker) {
        QuickPickerBottomSheet(
            title = "Chọn Khách Hàng / Thương Lái",
            searchPlaceholder = "Tìm thương lái, đối tác lò mổ...",
            options = FarmCatalogs.TRADERS,
            selectedId = selectedTrader,
            onSelect = { selectedTrader = it.title },
            onDismiss = { showTraderPicker = false }
        )
    }
}

// =============================================================================
// 9. SỰ KIỆN "+ NHẬP ĐÀN" HEO NÁI (New Sow Intake)
// =============================================================================
@Composable
fun NewSowIntakeDialog(
    availablePigs: List<PigEntity>,
    onDismiss: () -> Unit,
    onSave: (tagCode: String, breed: String, birthDate: Long, parity: Int, location: String, notes: String) -> Unit
) {
    var tagCodeInput by remember { mutableStateOf("") }
    var selectedBreed by remember { mutableStateOf(FarmCatalogs.BREEDS.first().title) }
    var birthDateStr by remember {
        mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(System.currentTimeMillis() - 240L * 86_400_000L)))
    }
    var parityStr by remember { mutableStateOf("0") }
    var selectedPen by remember { mutableStateOf(FarmCatalogs.SOW_PENS.last().title) }
    var notes by remember { mutableStateOf("Nhập giống chuẩn F1 năng suất cao") }

    // Kiểm tra trùng lặp mã tai thời gian thực (Real-time duplicate check)
    val trimmedTag = tagCodeInput.trim()
    val isDuplicateTag = remember(trimmedTag, availablePigs) {
        trimmedTag.isNotEmpty() && availablePigs.any { it.tagCode.equals(trimmedTag, ignoreCase = true) }
    }

    val parity = parityStr.toIntOrNull() ?: 0
    val autoStatusDesc = if (parity == 0) {
        "Lứa 0 -> Trạng thái ban đầu: HẬU BỊ (HAU_BI)"
    } else {
        "Lứa $parity -> Trạng thái ban đầu: CAI SỮA CHỜ PHỐI"
    }

    var showBreedPicker by remember { mutableStateOf(false) }
    var showPenPicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("dialog_new_sow_intake"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "9. + Nhập Đàn Heo Nái",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = FarmGreenPrimary
                        )
                        Text(
                            text = "Kiểm tra trùng mã tai thời gian thực với cơ sở dữ liệu",
                            fontSize = 11.sp,
                            color = FarmTextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng", tint = FarmTextSecondary)
                    }
                }

                // 1. Mã tai (với Real-time Duplicate Check)
                OutlinedTextField(
                    value = tagCodeInput,
                    onValueChange = { tagCodeInput = it },
                    label = { Text("Mã số tai nái (ví dụ: N089)") },
                    isError = isDuplicateTag,
                    supportingText = {
                        if (isDuplicateTag) {
                            Text(
                                text = "⚠️ Mã tai '$trimmedTag' đã tồn tại trong trại! Vui lòng nhập mã khác.",
                                color = FarmRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else if (trimmedTag.isNotEmpty()) {
                            Text("✓ Mã tai hợp lệ, chưa trùng lặp", color = FarmGreenPrimary, fontSize = 11.sp)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("new_sow_tag_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // 2. Giống heo Picker
                QuickPickerField(
                    label = "Giống heo nái",
                    value = selectedBreed,
                    icon = Icons.Default.Pets,
                    onClick = { showBreedPicker = true },
                    testTag = "new_sow_breed_picker"
                )

                // 3. Ngày sinh & Lứa đẻ
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = birthDateStr,
                        onValueChange = { birthDateStr = it },
                        label = { Text("Ngày sinh") },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = parityStr,
                        onValueChange = { parityStr = it },
                        label = { Text("Lứa đẻ (0=Hậu bị)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("new_sow_parity_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Card giải thích trạng thái khởi tạo
                Surface(
                    color = FarmGreenContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✓ $autoStatusDesc",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = FarmGreenPrimary,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                // 4. Chuồng nái Picker
                QuickPickerField(
                    label = "Vị trí chuồng nuôi ban đầu",
                    value = selectedPen,
                    icon = Icons.Default.SwapHoriz,
                    onClick = { showPenPicker = true },
                    testTag = "new_sow_pen_picker"
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Ghi chú nguồn gốc / thể trạng") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) { Text("Hủy") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsedDate = try {
                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(birthDateStr)?.time ?: (System.currentTimeMillis() - 240L * 86_400_000L)
                            } catch (e: Exception) {
                                System.currentTimeMillis() - 240L * 86_400_000L
                            }
                            onSave(trimmedTag, selectedBreed, parsedDate, parity, selectedPen, notes)
                        },
                        enabled = trimmedTag.isNotBlank() && !isDuplicateTag,
                        colors = ButtonDefaults.buttonColors(containerColor = FarmGreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("new_sow_btn_confirm")
                    ) {
                        Text("Nhập đàn nái")
                    }
                }
            }
        }
    }

    if (showBreedPicker) {
        QuickPickerBottomSheet(
            title = "Chọn Giống Heo Nái",
            searchPlaceholder = "Tìm Landrace, Yorkshire, Duroc...",
            options = FarmCatalogs.BREEDS,
            selectedId = selectedBreed,
            onSelect = { selectedBreed = it.title },
            onDismiss = { showBreedPicker = false }
        )
    }

    if (showPenPicker) {
        QuickPickerBottomSheet(
            title = "Chọn Chuồng Nái Ban Đầu",
            searchPlaceholder = "Tìm chuồng nái...",
            options = FarmCatalogs.SOW_PENS,
            selectedId = selectedPen,
            onSelect = { selectedPen = it.title },
            onDismiss = { showPenPicker = false }
        )
    }
}

// =============================================================================
// CÁC DIALOG LOẠI THẢI & KHÁC (Đẻ, Tiêm sắt đơn, Loại thải nái, Loại thải heo con, Loại thải heo thịt)
// =============================================================================

@Composable
fun FarrowingEventDialog(
    availablePigs: List<PigEntity>,
    onDismiss: () -> Unit,
    onSave: (
        sowId: String,
        farrowingDate: Long,
        totalBorn: Int,
        bornAlive: Int,
        stillborn: Int,
        mummies: Int,
        litterWeightKg: Float,
        notes: String
    ) -> Unit
) {
    val sows = availablePigs.filter { !it.isBatch }
    var selectedSow by remember { mutableStateOf(sows.firstOrNull()) }
    var sowExpanded by remember { mutableStateOf(false) }

    val todayStr = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }
    var farrowingDateStr by remember { mutableStateOf(todayStr) }
    var totalBornStr by remember { mutableStateOf("12") }
    var bornAliveStr by remember { mutableStateOf("11") }
    var stillbornStr by remember { mutableStateOf("1") }
    var mummiesStr by remember { mutableStateOf("0") }
    var litterWeightStr by remember { mutableStateOf("15.6") }
    var notes by remember { mutableStateOf("Đẻ khó, hỗ trợ kéo thai") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("dialog_farrowing_event"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Sự kiện: Đẻ (Heo con theo mẹ)",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = FarmGreenPrimary
                        )
                        Text(
                            text = "Tự động tạo đàn con & kích hoạt 5 việc nuôi",
                            fontSize = 11.sp,
                            color = FarmTextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng", tint = FarmTextSecondary)
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAF9)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FarmOutline)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("THÔNG TIN MẸ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FarmGreenPrimary)
                        Box {
                            OutlinedTextField(
                                value = selectedSow?.let { "${it.tagCode} (Chuồng: ${it.location}, Lứa: ${it.parity})" } ?: "Chọn Nái mẹ...",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { sowExpanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { sowExpanded = true }
                                    .testTag("farrowing_sow_dropdown"),
                                shape = RoundedCornerShape(10.dp)
                            )
                            DropdownMenu(expanded = sowExpanded, onDismissRequest = { sowExpanded = false }) {
                                sows.forEach { sow ->
                                    DropdownMenuItem(
                                        text = { Text("${sow.tagCode} • ${sow.location} • Lứa ${sow.parity}") },
                                        onClick = {
                                            selectedSow = sow
                                            sowExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAF9)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FarmOutline)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("THÔNG TIN SINH SẢN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FarmGreenPrimary)

                        OutlinedTextField(
                            value = farrowingDateStr,
                            onValueChange = { farrowingDateStr = it },
                            label = { Text("Ngày đẻ (dd/MM/yyyy)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = totalBornStr,
                                onValueChange = { totalBornStr = it },
                                label = { Text("Tổng sinh") },
                                modifier = Modifier.weight(1f).testTag("farrowing_total_born"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = bornAliveStr,
                                onValueChange = { bornAliveStr = it },
                                label = { Text("Con sống") },
                                modifier = Modifier.weight(1f).testTag("farrowing_born_alive"),
                                singleLine = true
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = stillbornStr,
                                onValueChange = { stillbornStr = it },
                                label = { Text("Con chết") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = litterWeightStr,
                                onValueChange = { litterWeightStr = it },
                                label = { Text("TL ổ (kg)") },
                                modifier = Modifier.weight(1f).testTag("farrowing_litter_weight"),
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Ghi chú") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = FarmGreenContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "✓ Sau khi lưu: Hệ thống tự tạo đàn con DC... ($bornAliveStr con, Đang theo mẹ)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FarmGreenPrimary
                        )
                        Text(
                            text = "✓ Kích hoạt 5 việc: Tiêm sắt D+3, Cắt đuôi D+7, Vaccine D+14, Tập ăn D+21, Cai sữa D+28.",
                            fontSize = 11.sp,
                            color = FarmGreenPrimary
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) { Text("Hủy") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val sow = selectedSow ?: sows.firstOrNull()
                            if (sow != null) {
                                onSave(
                                    sow.id,
                                    System.currentTimeMillis(),
                                    totalBornStr.toIntOrNull() ?: 12,
                                    bornAliveStr.toIntOrNull() ?: 11,
                                    stillbornStr.toIntOrNull() ?: 1,
                                    mummiesStr.toIntOrNull() ?: 0,
                                    litterWeightStr.toFloatOrNull() ?: 15.6f,
                                    notes
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FarmGreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("farrowing_btn_confirm")
                    ) {
                        Text("Lưu sự kiện")
                    }
                }
            }
        }
    }
}

@Composable
fun IronInjectionDialog(
    availableBatches: List<PigEntity>,
    onDismiss: () -> Unit,
    onSave: (pigletTag: String, date: Long, qty: Int, drug: String, dosage: Float) -> Unit
) {
    val pigletBatches = availableBatches.filter { it.isBatch && (it.tagCode.startsWith("DC") || it.tagCode.contains("heo con", ignoreCase = true)) }
    var selectedTag by remember { mutableStateOf(pigletBatches.firstOrNull()?.tagCode ?: "DC20270626-001") }
    var injectionDateStr by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    var qtyStr by remember { mutableStateOf("11") }
    var drugName by remember { mutableStateOf("Iron Dextran") }
    var dosageStr by remember { mutableStateOf("1.0") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp).testTag("dialog_iron_injection"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Sự kiện: Tiêm Sắt Heo Con", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = FarmGreenPrimary)

                OutlinedTextField(value = selectedTag, onValueChange = { selectedTag = it }, label = { Text("Mã đàn con") }, modifier = Modifier.fillMaxWidth().testTag("iron_tag_input"), singleLine = true)
                OutlinedTextField(value = injectionDateStr, onValueChange = { injectionDateStr = it }, label = { Text("Ngày tiêm (dd/MM/yyyy)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = qtyStr, onValueChange = { qtyStr = it }, label = { Text("Số con tiêm") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = drugName, onValueChange = { drugName = it }, label = { Text("Loại thuốc") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = dosageStr, onValueChange = { dosageStr = it }, label = { Text("Liều lượng (ml/con)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) { Text("Hủy") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(selectedTag.trim(), System.currentTimeMillis(), qtyStr.toIntOrNull() ?: 11, drugName.trim(), dosageStr.toFloatOrNull() ?: 1.0f)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FarmGreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("iron_btn_confirm")
                    ) {
                        Text("Lưu bản ghi")
                    }
                }
            }
        }
    }
}

@Composable
fun SowCullingDialog(
    availablePigs: List<PigEntity>,
    onDismiss: () -> Unit,
    onSave: (
        sowTag: String,
        cullDate: Long,
        reason: String,
        weightKg: Float,
        salePrice: String,
        buyer: String
    ) -> Unit
) {
    val sows = availablePigs.filter { !it.isBatch }
    var selectedSow by remember { mutableStateOf(sows.firstOrNull()) }
    var sowExpanded by remember { mutableStateOf(false) }

    var cullDateStr by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }

    val reasons = listOf(
        "Già",
        "Không đậu thai",
        "Đẻ ít con",
        "Viêm tử cung",
        "Chân móng yếu",
        "Bệnh mãn tính",
        "Khác"
    )
    var selectedReason by remember { mutableStateOf(reasons[0]) }
    var reasonExpanded by remember { mutableStateOf(false) }

    var weightStr by remember { mutableStateOf("185") }
    var priceStr by remember { mutableStateOf("12.500.000") }
    var buyerStr by remember { mutableStateOf("Thương lái Ba Tri") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp).testTag("dialog_sow_culling"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Form Loại Thải Heo Nái", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = FarmAmber)
                        Text("Cắt giảm đàn nái kém năng suất, chuyển sang Loại thải", fontSize = 11.sp, color = FarmTextSecondary)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng", tint = FarmTextSecondary)
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9EE)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FarmAmber.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("THÔNG TIN NÁI", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FarmAmber)
                        Box {
                            OutlinedTextField(
                                value = selectedSow?.let { "Mã: ${it.tagCode} • Giống: ${it.breed} • Lứa: ${it.parity}" } ?: "Chọn Nái mẹ...",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { sowExpanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().clickable { sowExpanded = true }.testTag("cull_sow_dropdown"),
                                shape = RoundedCornerShape(10.dp)
                            )
                            DropdownMenu(expanded = sowExpanded, onDismissRequest = { sowExpanded = false }) {
                                sows.forEach { sow ->
                                    DropdownMenuItem(
                                        text = { Text("${sow.tagCode} • Giống: ${sow.breed} • Lứa đẻ: ${sow.parity}") },
                                        onClick = {
                                            selectedSow = sow
                                            sowExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAF9)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FarmOutline)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("THÔNG TIN LOẠI THẢI", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FarmGreenPrimary)

                        OutlinedTextField(
                            value = cullDateStr,
                            onValueChange = { cullDateStr = it },
                            label = { Text("Ngày loại (dd/MM/yyyy)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Box {
                            OutlinedTextField(
                                value = selectedReason,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Lý do loại thải") },
                                trailingIcon = {
                                    IconButton(onClick = { reasonExpanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().clickable { reasonExpanded = true }.testTag("cull_reason_dropdown"),
                                shape = RoundedCornerShape(10.dp)
                            )
                            DropdownMenu(expanded = reasonExpanded, onDismissRequest = { reasonExpanded = false }) {
                                reasons.forEach { r ->
                                    DropdownMenuItem(
                                        text = { Text(r) },
                                        onClick = {
                                            selectedReason = r
                                            reasonExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAF9)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FarmOutline)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("THÔNG TIN XUẤT CHUỒNG", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FarmGreenPrimary)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = weightStr, onValueChange = { weightStr = it }, label = { Text("Khối lượng (kg)") }, modifier = Modifier.weight(1f), singleLine = true)
                            OutlinedTextField(value = priceStr, onValueChange = { priceStr = it }, label = { Text("Giá bán (VNĐ)") }, modifier = Modifier.weight(1f), singleLine = true)
                        }
                        OutlinedTextField(value = buyerStr, onValueChange = { buyerStr = it }, label = { Text("Người mua / Thương lái") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) { Text("Hủy") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val sow = selectedSow ?: sows.firstOrNull()
                            if (sow != null) {
                                onSave(sow.tagCode, System.currentTimeMillis(), selectedReason, weightStr.toFloatOrNull() ?: 185f, priceStr, buyerStr)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FarmAmber),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("cull_btn_confirm")
                    ) {
                        Text("Xác nhận loại thải")
                    }
                }
            }
        }
    }
}

@Composable
fun PigletCullingDialog(
    availableBatches: List<PigEntity>,
    onDismiss: () -> Unit,
    onSave: (pigletTag: String, date: Long, qty: Int, reason: String, weightKg: Float, disposition: String) -> Unit
) {
    val pigletBatches = availableBatches.filter { it.isBatch && (it.tagCode.startsWith("DC") || it.tagCode.startsWith("CS") || it.tagCode.contains("heo con", ignoreCase = true)) }
    var selectedTag by remember { mutableStateOf(pigletBatches.firstOrNull()?.tagCode ?: "DC20270626-001") }
    var tagExpanded by remember { mutableStateOf(false) }

    var cullDateStr by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    var qtyStr by remember { mutableStateOf("1") }
    var weightStr by remember { mutableStateOf("1.8") }

    val reasons = listOf(
        "Dị tật bẩm sinh",
        "Quá còi cọc",
        "Tiêu chảy mãn tính",
        "Chấn thương dập đè",
        "Viêm khớp mủ",
        "Khác"
    )
    var selectedReason by remember { mutableStateOf(reasons[0]) }
    var reasonExpanded by remember { mutableStateOf(false) }
    var disposition by remember { mutableStateOf("Tiêu hủy an toàn") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp).testTag("dialog_piglet_culling"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Loại Thải Heo Con", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = FarmAmber)
                        Text("Loại bỏ heo còi cọc, dị tật, giảm số lượng đàn", fontSize = 11.sp, color = FarmTextSecondary)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng", tint = FarmTextSecondary)
                    }
                }

                Box {
                    OutlinedTextField(
                        value = selectedTag,
                        onValueChange = { selectedTag = it },
                        label = { Text("Mã đàn con") },
                        trailingIcon = {
                            if (pigletBatches.isNotEmpty()) {
                                IconButton(onClick = { tagExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("piglet_cull_tag_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                    if (pigletBatches.isNotEmpty()) {
                        DropdownMenu(expanded = tagExpanded, onDismissRequest = { tagExpanded = false }) {
                            pigletBatches.forEach { batch ->
                                DropdownMenuItem(
                                    text = { Text("${batch.tagCode} (${batch.quantity} con)") },
                                    onClick = {
                                        selectedTag = batch.tagCode
                                        tagExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = cullDateStr, onValueChange = { cullDateStr = it }, label = { Text("Ngày loại") }, modifier = Modifier.weight(1.2f), singleLine = true)
                    OutlinedTextField(value = qtyStr, onValueChange = { qtyStr = it }, label = { Text("Số con loại") }, modifier = Modifier.weight(1f).testTag("piglet_cull_qty_input"), singleLine = true)
                }

                Box {
                    OutlinedTextField(
                        value = selectedReason,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Lý do loại thải") },
                        trailingIcon = {
                            IconButton(onClick = { reasonExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { reasonExpanded = true }.testTag("piglet_cull_reason_dropdown"),
                        shape = RoundedCornerShape(10.dp)
                    )
                    DropdownMenu(expanded = reasonExpanded, onDismissRequest = { reasonExpanded = false }) {
                        reasons.forEach { r ->
                            DropdownMenuItem(text = { Text(r) }, onClick = { selectedReason = r; reasonExpanded = false })
                        }
                    }
                }

                OutlinedTextField(value = weightStr, onValueChange = { weightStr = it }, label = { Text("Khối lượng TB (kg/con)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { disposition = "Tiêu hủy an toàn" }) {
                        RadioButton(selected = disposition == "Tiêu hủy an toàn", onClick = { disposition = "Tiêu hủy an toàn" }, colors = RadioButtonDefaults.colors(selectedColor = FarmAmber))
                        Text("Tiêu hủy", fontSize = 13.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { disposition = "Bán hạ giá" }) {
                        RadioButton(selected = disposition == "Bán hạ giá", onClick = { disposition = "Bán hạ giá" }, colors = RadioButtonDefaults.colors(selectedColor = FarmAmber))
                        Text("Bán hạ giá", fontSize = 13.sp)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) { Text("Hủy") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(selectedTag.trim(), System.currentTimeMillis(), qtyStr.toIntOrNull() ?: 1, selectedReason, weightStr.toFloatOrNull() ?: 1.5f, disposition)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FarmAmber),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("piglet_cull_btn_confirm")
                    ) {
                        Text("Xác nhận loại")
                    }
                }
            }
        }
    }
}

@Composable
fun GrowerCullingDialog(
    availableBatches: List<PigEntity>,
    onDismiss: () -> Unit,
    onSave: (batchCode: String, date: Long, qty: Int, reason: String, weightKg: Float, price: String, buyer: String) -> Unit
) {
    val growerBatches = availableBatches.filter { it.isBatch && (it.tagCode.startsWith("LT") || it.tagCode.contains("thịt", ignoreCase = true) || it.status == SowReproductiveStatus.DANG_NUOI) }
    var selectedBatch by remember { mutableStateOf(growerBatches.firstOrNull()?.tagCode ?: "LT001") }
    var batchExpanded by remember { mutableStateOf(false) }

    var cullDateStr by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    var qtyStr by remember { mutableStateOf("2") }
    var weightStr by remember { mutableStateOf("45.0") }
    var priceStr by remember { mutableStateOf("1.800.000") }
    var buyerStr by remember { mutableStateOf("Lò mổ địa phương") }

    val reasons = listOf(
        "Chấn thương bại liệt",
        "Còi cọc không lớn",
        "Ho suyễn mãn tính",
        "Thoát vị rốn",
        "Cắn đuôi hoại tử",
        "Khác"
    )
    var selectedReason by remember { mutableStateOf(reasons[0]) }
    var reasonExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp).testTag("dialog_grower_culling"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Loại Thải Heo Thịt", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = FarmAmber)
                        Text("Thanh lý heo chấn thương, còi cọc không đạt chuẩn", fontSize = 11.sp, color = FarmTextSecondary)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng", tint = FarmTextSecondary)
                    }
                }

                Box {
                    OutlinedTextField(
                        value = selectedBatch,
                        onValueChange = { selectedBatch = it },
                        label = { Text("Mã lô heo thịt") },
                        trailingIcon = {
                            if (growerBatches.isNotEmpty()) {
                                IconButton(onClick = { batchExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("grower_cull_batch_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                    if (growerBatches.isNotEmpty()) {
                        DropdownMenu(expanded = batchExpanded, onDismissRequest = { batchExpanded = false }) {
                            growerBatches.forEach { b ->
                                DropdownMenuItem(
                                    text = { Text("${b.tagCode} (${b.quantity} con • ${b.location})") },
                                    onClick = { selectedBatch = b.tagCode; batchExpanded = false }
                                )
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = cullDateStr, onValueChange = { cullDateStr = it }, label = { Text("Ngày loại") }, modifier = Modifier.weight(1.2f), singleLine = true)
                    OutlinedTextField(value = qtyStr, onValueChange = { qtyStr = it }, label = { Text("Số con loại") }, modifier = Modifier.weight(1f).testTag("grower_cull_qty_input"), singleLine = true)
                }

                Box {
                    OutlinedTextField(
                        value = selectedReason,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Lý do loại thải") },
                        trailingIcon = {
                            IconButton(onClick = { reasonExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { reasonExpanded = true }.testTag("grower_cull_reason_dropdown"),
                        shape = RoundedCornerShape(10.dp)
                    )
                    DropdownMenu(expanded = reasonExpanded, onDismissRequest = { reasonExpanded = false }) {
                        reasons.forEach { r ->
                            DropdownMenuItem(text = { Text(r) }, onClick = { selectedReason = r; reasonExpanded = false })
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = weightStr, onValueChange = { weightStr = it }, label = { Text("Khối lượng TB (kg)") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = priceStr, onValueChange = { priceStr = it }, label = { Text("Giá thanh lý (VNĐ)") }, modifier = Modifier.weight(1f), singleLine = true)
                }

                OutlinedTextField(value = buyerStr, onValueChange = { buyerStr = it }, label = { Text("Người mua / Lò mổ") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) { Text("Hủy") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(selectedBatch.trim(), System.currentTimeMillis(), qtyStr.toIntOrNull() ?: 2, selectedReason, weightStr.toFloatOrNull() ?: 45f, priceStr, buyerStr)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FarmAmber),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("grower_cull_btn_confirm")
                    ) {
                        Text("Xác nhận loại")
                    }
                }
            }
        }
    }
}

@Composable
fun GrowerEventDialog(
    initialSubEvent: FastEventType = FastEventType.GROWER_INTAKE,
    availableBatches: List<PigEntity>,
    onDismiss: () -> Unit,
    onIntake: (code: String, date: Long, qty: Int, weight: Float, loc: String) -> Unit,
    onWeigh: (code: String, date: Long, count: Int, weight: Float) -> Unit,
    onTreatment: (code: String, date: Long, sick: Int, disease: String, drug: String) -> Unit,
    onSale: (code: String, date: Long, qty: Int, weight: Float, buyer: String) -> Unit
) {
    var currentSubEvent by remember { mutableStateOf(initialSubEvent) }

    var intakeCode by remember { mutableStateOf("LT001") }
    var intakeQty by remember { mutableStateOf("100") }
    var intakeWeight by remember { mutableStateOf("22.0") }
    var intakeLocation by remember { mutableStateOf("Khu A-01") }

    var weighBatch by remember { mutableStateOf("LT001") }
    var weighCount by remember { mutableStateOf("100") }
    var weighAvgWeight by remember { mutableStateOf("30.5") }

    var treatBatch by remember { mutableStateOf("LT001") }
    var treatSickCount by remember { mutableStateOf("5") }
    var treatDisease by remember { mutableStateOf("Tiêu chảy") }
    var treatDrug by remember { mutableStateOf("ABC (Amoxicillin)") }

    var saleBatch by remember { mutableStateOf("LT001") }
    var saleQty by remember { mutableStateOf("98") }
    var saleWeight by remember { mutableStateOf("110.0") }
    var saleCustomer by remember { mutableStateOf("Công ty XYZ") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp).testTag("dialog_grower_event"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Sự Kiện Heo Thịt (Quản lý theo Lô)", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = FarmGreenPrimary)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val subEvents = listOf(
                        FastEventType.GROWER_INTAKE to "Nhập lô",
                        FastEventType.GROWER_WEIGH to "Cân",
                        FastEventType.GROWER_TREATMENT to "Điều trị",
                        FastEventType.GROWER_SALE to "Xuất bán"
                    )
                    subEvents.forEach { (type, label) ->
                        val isSelected = currentSubEvent == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) FarmGreenPrimary else Color(0xFFF1F5F1))
                                .clickable { currentSubEvent = type }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else FarmTextPrimary
                            )
                        }
                    }
                }

                when (currentSubEvent) {
                    FastEventType.GROWER_INTAKE -> {
                        OutlinedTextField(value = intakeCode, onValueChange = { intakeCode = it }, label = { Text("Mã lô") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = intakeQty, onValueChange = { intakeQty = it }, label = { Text("Số lượng con") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = intakeWeight, onValueChange = { intakeWeight = it }, label = { Text("Khối lượng TB (kg)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = intakeLocation, onValueChange = { intakeLocation = it }, label = { Text("Chuồng") }, modifier = Modifier.fillMaxWidth())
                    }
                    FastEventType.GROWER_WEIGH -> {
                        OutlinedTextField(value = weighBatch, onValueChange = { weighBatch = it }, label = { Text("Lô heo") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = weighCount, onValueChange = { weighCount = it }, label = { Text("Số con cân") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = weighAvgWeight, onValueChange = { weighAvgWeight = it }, label = { Text("Khối lượng TB (kg)") }, modifier = Modifier.fillMaxWidth())

                        val enteredWeight = weighAvgWeight.toFloatOrNull() ?: 30.5f
                        val simulatedOld = 22.0f
                        val gain = (enteredWeight - simulatedOld).coerceAtLeast(0f)
                        val adg = gain / 14f
                        val days = ((110f - enteredWeight) / adg).toInt().coerceAtLeast(0)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = FarmGreenContainer),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("HỆ THỐNG TÍNH TOÁN TỰ ĐỘNG:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FarmGreenPrimary)
                                Text("• Tăng trọng bình quân: +${String.format(Locale.US, "%.1f", gain)} kg", fontSize = 12.sp, color = FarmGreenPrimary)
                                Text("• ADG: ${String.format(Locale.US, "%.2f", adg)} kg/con/ngày", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = FarmGreenPrimary)
                                Text("• Dự báo xuất bán (đạt 110kg): sau $days ngày", fontSize = 12.sp, color = FarmGreenPrimary)
                            }
                        }
                    }
                    FastEventType.GROWER_TREATMENT -> {
                        OutlinedTextField(value = treatBatch, onValueChange = { treatBatch = it }, label = { Text("Lô heo") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = treatSickCount, onValueChange = { treatSickCount = it }, label = { Text("Số con bệnh") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = treatDisease, onValueChange = { treatDisease = it }, label = { Text("Tên bệnh") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = treatDrug, onValueChange = { treatDrug = it }, label = { Text("Thuốc điều trị") }, modifier = Modifier.fillMaxWidth())
                    }
                    FastEventType.GROWER_SALE -> {
                        OutlinedTextField(value = saleBatch, onValueChange = { saleBatch = it }, label = { Text("Mã lô xuất") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = saleQty, onValueChange = { saleQty = it }, label = { Text("Số lượng") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = saleWeight, onValueChange = { saleWeight = it }, label = { Text("Khối lượng TB (kg)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = saleCustomer, onValueChange = { saleCustomer = it }, label = { Text("Khách hàng") }, modifier = Modifier.fillMaxWidth())
                    }
                    else -> {}
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) { Text("Hủy") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            when (currentSubEvent) {
                                FastEventType.GROWER_INTAKE -> onIntake(intakeCode, System.currentTimeMillis(), intakeQty.toIntOrNull() ?: 100, intakeWeight.toFloatOrNull() ?: 22f, intakeLocation)
                                FastEventType.GROWER_WEIGH -> onWeigh(weighBatch, System.currentTimeMillis(), weighCount.toIntOrNull() ?: 100, weighAvgWeight.toFloatOrNull() ?: 30.5f)
                                FastEventType.GROWER_TREATMENT -> onTreatment(treatBatch, System.currentTimeMillis(), treatSickCount.toIntOrNull() ?: 5, treatDisease, treatDrug)
                                FastEventType.GROWER_SALE -> onSale(saleBatch, System.currentTimeMillis(), saleQty.toIntOrNull() ?: 98, saleWeight.toFloatOrNull() ?: 110f, saleCustomer)
                                else -> {}
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FarmGreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("grower_btn_confirm")
                    ) {
                        Text("Lưu sự kiện")
                    }
                }
            }
        }
    }
}
