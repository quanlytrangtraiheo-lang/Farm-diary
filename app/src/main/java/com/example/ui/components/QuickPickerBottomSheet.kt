package com.example.ui.components

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FarmBackground
import com.example.ui.theme.FarmGreenContainer
import com.example.ui.theme.FarmGreenPrimary
import com.example.ui.theme.FarmOutline
import com.example.ui.theme.FarmTextPrimary
import com.example.ui.theme.FarmTextSecondary
import com.example.ui.theme.LocalOutdoorMode
import com.example.ui.theme.OutdoorBorder
import com.example.ui.theme.OutdoorBorderAlt
import com.example.ui.theme.OutdoorCardBorder
import com.example.ui.theme.OutdoorDivider
import com.example.ui.theme.OutdoorFocusBorder
import com.example.ui.theme.OutdoorFocusHalo
import com.example.ui.theme.OutdoorIcon
import com.example.ui.theme.OutdoorInputFill
import com.example.ui.theme.OutdoorInputValue
import com.example.ui.theme.OutdoorLabelText
import com.example.ui.theme.OutdoorModeManager
import com.example.ui.theme.OutdoorOverallBackground
import com.example.ui.theme.OutdoorPlaceholder
import com.example.ui.theme.OutdoorRequiredStar
import com.example.ui.theme.OutdoorSelectedRadio

/**
 * Model biểu diễn một lựa chọn trong bộ chọn nhanh ngoài chuồng (QuickPicker).
 */
data class PickerOption(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val badge: String? = null,
    val badgeColor: Color = FarmGreenContainer,
    val badgeTextColor: Color = FarmGreenPrimary,
    val icon: ImageVector? = null,
    val extraData: Any? = null
)

/**
 * Giao diện Bottom Sheet chọn nhanh bằng một tay ngoài chuồng (UI Pattern).
 * Chuẩn hóa độ tương phản cao ngoài trời (Outdoor High-Contrast WCAG AAA):
 * 1. Thanh tiêu đề: ✕   [TÊN BỘ CHỌN] (15sp Bold #0F172A, nút ✕ >= 48dp).
 * 2. Thanh tìm kiếm: Chiều cao 56dp, nền trắng tinh #FFFFFF, viền #1E293B 1.5dp, kính lúp #0F172A 24dp.
 * 3. Hàng danh sách: Chiều cao >= 56dp, vạch phân cách #CBD5E1 1dp chống lóa hoa mắt.
 * 4. Trạng thái chọn: Radio viền 2dp, nền xanh #0F52BA với dấu tích trắng #FFFFFF, chữ 16sp Bold #0F172A.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickPickerBottomSheet(
    title: String,
    searchPlaceholder: String = "Tìm kiếm nhanh...",
    options: List<PickerOption>,
    selectedId: String? = null,
    onSelect: (PickerOption) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }
    val isOutdoor = LocalOutdoorMode.current

    val filteredOptions = remember(options, searchQuery) {
        if (searchQuery.isBlank()) options
        else {
            val q = searchQuery.trim().lowercase()
            options.filter {
                it.title.lowercase().contains(q) ||
                    (it.subtitle?.lowercase()?.contains(q) == true) ||
                    (it.badge?.lowercase()?.contains(q) == true)
            }
        }
    }

    val titleFontSize = OutdoorModeManager.scaledSp(15f, isOutdoor)
    val itemFontSize = OutdoorModeManager.scaledSp(16f, isOutdoor)
    val subFontSize = OutdoorModeManager.scaledSp(13f, isOutdoor)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = OutdoorInputFill,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 480.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("quick_picker_bottom_sheet")
        ) {
            // Header: Cấu trúc ✕   [TÊN BỘ CHỌN] (15sp Bold #0F172A)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nút đóng ✕ kích thước tối thiểu 48x48dp chống bấm trượt bằng ngón cái
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("picker_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng bộ chọn",
                        tint = OutdoorIcon,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title.uppercase(),
                        fontSize = titleFontSize,
                        fontWeight = FontWeight.Bold,
                        color = OutdoorLabelText,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${filteredOptions.size} mục phù hợp • Chạm 1 lần để chọn",
                        fontSize = subFontSize,
                        color = OutdoorPlaceholder,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Thanh tìm kiếm: Chiều cao 56dp, nền trắng, viền #1E293B 1.5dp, kính lúp #0F172A 24dp
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = searchPlaceholder,
                        fontSize = subFontSize,
                        color = OutdoorPlaceholder,
                        fontWeight = FontWeight.Medium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = OutdoorIcon,
                        modifier = Modifier.size(24.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Xóa tìm kiếm",
                                tint = OutdoorIcon,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = itemFontSize,
                    fontWeight = FontWeight.Bold,
                    color = OutdoorInputValue
                ),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OutdoorFocusBorder,
                    unfocusedBorderColor = OutdoorBorder,
                    focusedContainerColor = OutdoorInputFill,
                    unfocusedContainerColor = OutdoorInputFill,
                    cursorColor = OutdoorFocusBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("quick_picker_search_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Danh sách lựa chọn: Chiều cao mỗi dòng >= 56dp, vạch phân cách #CBD5E1 1dp
            if (filteredOptions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Không tìm thấy kết quả phù hợp",
                        color = OutdoorPlaceholder,
                        fontSize = itemFontSize,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                ) {
                    itemsIndexed(filteredOptions, key = { _, it -> it.id }) { index, option ->
                        val isSelected = option.id == selectedId
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 56.dp)
                                    .background(if (isSelected) Color(0xFFF0F7FF) else OutdoorInputFill)
                                    .clickable {
                                        onSelect(option)
                                        onDismiss()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 10.dp)
                                    .testTag("picker_item_${option.id}"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Nút tròn Radio 2dp stroke, khi chọn tô đặc #0F52BA với dấu tick trắng
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .border(
                                                width = 2.dp,
                                                color = if (isSelected) OutdoorSelectedRadio else OutdoorBorder,
                                                shape = CircleShape
                                            )
                                            .background(if (isSelected) OutdoorSelectedRadio else Color.Transparent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Đã chọn",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        // Tên lựa chọn: 16sp Bold #0F172A
                                        Text(
                                            text = option.title,
                                            fontSize = itemFontSize,
                                            fontWeight = FontWeight.Bold,
                                            color = OutdoorLabelText
                                        )
                                        if (!option.subtitle.isNullOrBlank()) {
                                            Text(
                                                text = option.subtitle,
                                                fontSize = subFontSize,
                                                color = OutdoorPlaceholder,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Badge có viền đậm rõ ràng để không hòa lẫn
                                    if (!option.badge.isNullOrBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = option.badgeColor,
                                            border = androidx.compose.foundation.BorderStroke(1.5.dp, OutdoorFocusBorder),
                                            modifier = Modifier.padding(end = 4.dp)
                                        ) {
                                            Text(
                                                text = option.badge,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = OutdoorLabelText,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Vạch kẻ phân cách hàng #CBD5E1 (1dp thickness) chống hoa mắt
                            if (index < filteredOptions.size - 1) {
                                HorizontalDivider(
                                    thickness = 1.dp,
                                    color = OutdoorDivider,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

/**
 * Trường nhập liệu chọn nhanh (QuickPickerField) hiển thị trên form.
 * Chuẩn công thái học và độ tương phản cao ngoài trời (Outdoor High-Contrast WCAG AAA):
 * - Cố định nhãn phía trên (Persistent Top Label >= 15sp Semi-Bold #0F172A).
 * - Dấu sao đỏ bắt buộc (*) #B91C1C nét dày.
 * - Vùng bấm tối thiểu 56dp (chiều cao cố định 56dp).
 * - Bo góc 8dp để tối đa diện tích phẳng trắng #FFFFFF.
 * - Viền xanh đen đậm #1E293B (1.5dp) hoặc xanh đậm #0F52BA khi đang tương tác.
 * - Giá trị hiển thị: #000000 Bold 15sp-16sp.
 * - Mũi tên Dropdown: #0F172A kích thước 24dp.
 */
@Composable
fun QuickPickerField(
    label: String,
    value: String,
    placeholder: String = "Chạm để chọn...",
    icon: ImageVector? = null,
    isRequired: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "quick_picker_field"
) {
    val isOutdoor = LocalOutdoorMode.current
    val labelFontSize = OutdoorModeManager.scaledSp(15f, isOutdoor)
    val valueFontSize = OutdoorModeManager.scaledSp(15f, isOutdoor)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Nhãn cố định phía trên (Persistent Top Label >= 15sp Semi-Bold)
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = labelFontSize,
                fontWeight = FontWeight.SemiBold,
                color = OutdoorLabelText
            )
            if (isRequired) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "*",
                    fontSize = labelFontSize,
                    fontWeight = FontWeight.Bold,
                    color = OutdoorRequiredStar
                )
            }
        }

        // Khung bấm 56dp, nền trắng, viền #1E293B 1.5dp, bo góc 8dp
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = OutdoorInputFill,
            border = androidx.compose.foundation.BorderStroke(
                width = if (isOutdoor) 2.dp else 1.5.dp,
                color = if (value.isNotBlank()) OutdoorFocusBorder else OutdoorBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable { onClick() }
                .testTag(testTag)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = OutdoorIcon,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Text(
                        text = value.ifBlank { placeholder },
                        fontSize = valueFontSize,
                        fontWeight = if (value.isNotBlank()) FontWeight.Bold else FontWeight.Medium,
                        color = if (value.isNotBlank()) OutdoorInputValue else OutdoorPlaceholder
                    )
                }

                // Mũi tên dropdown 24dp màu #0F172A
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Mở danh sách",
                    tint = OutdoorIcon,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Danh mục chuẩn hóa toàn trại cho bộ chọn nhanh.
 */
object FarmCatalogs {
    val BOARS_AND_SEMEN = listOf(
        PickerOption("D01", "Duroc D-01", "Đực giống nhảy trực tiếp • Tinh lực 92%", "Trực tiếp", FarmGreenContainer, FarmGreenPrimary),
        PickerOption("D02", "Duroc D-02", "Đực giống nhảy trực tiếp • Chuẩn ngoại hình", "Trực tiếp", FarmGreenContainer, FarmGreenPrimary),
        PickerOption("P01", "Pietrain P-01", "Đực giống nhảy trực tiếp • Siêu nạc 2 máu", "Trực tiếp", FarmGreenContainer, FarmGreenPrimary),
        PickerOption("LT301", "Liều tinh Duroc 301", "Kho liều tinh mát • Tồn 25 liều", "Tinh mát", Color(0xFFE3F2FD), Color(0xFF1976D2)),
        PickerOption("LT502", "Liều tinh Pietrain 502", "Kho liều tinh mát • Tồn 18 liều", "Tinh mát", Color(0xFFE3F2FD), Color(0xFF1976D2)),
        PickerOption("LT105", "Liều tinh Landrace L-05", "Kho liều tinh mát • Tồn 12 liều", "Tinh mát", Color(0xFFE3F2FD), Color(0xFF1976D2))
    )

    val TECHNICIANS = listOf(
        PickerOption("T01", "Nguyễn Văn Tuấn", "Kỹ thuật viên Trưởng • Ca trực sáng"),
        PickerOption("T02", "Trần Văn Bình", "Kỹ thuật viên Phối giống & Thụ tinh nhân tạo"),
        PickerOption("T03", "Lê Thị Mai", "Bác sĩ Thú y trại • Phụ trách vaccine & dịch tễ"),
        PickerOption("T04", "Phạm Hoàng Nam", "Kỹ thuật viên Chăm sóc & Vệ sinh chuồng")
    )

    val BREEDS = listOf(
        PickerOption("B01", "Landrace", "Giống Đan Mạch siêu dài đòn, mắn đẻ"),
        PickerOption("B02", "Yorkshire", "Đại Bạch, khả năng nuôi con & tiết sữa xuất sắc"),
        PickerOption("B03", "Duroc", "Heo đực cuối, cơ bắp cuồn cuộn, tăng trọng nhanh"),
        PickerOption("B04", "Pietrain", "Giống Bỉ siêu nạc, mỡ lưng mỏng"),
        PickerOption("B05", "F1 (Landrace x Yorkshire)", "Nái nền sinh sản phổ biến nhất trang trại"),
        PickerOption("B06", "F2 (Duroc x F1)", "Heo thương phẩm 3 máu nuôi thịt")
    )

    val SOW_PENS = listOf(
        PickerOption("P01", "Chuồng Bầu 01", "Dãy nái mang thai kỳ 1 • Còn 4 ô"),
        PickerOption("P02", "Chuồng Bầu 02", "Dãy nái mang thai kỳ 2 • Còn 2 ô"),
        PickerOption("P03", "Chuồng Bầu 12", "Dãy nái chuẩn bị đẻ D+107 • Còn 3 ô"),
        PickerOption("P04", "Chuồng Đẻ 01", "Khu lồng đẻ ấm áp, đèn úm • Còn 2 lồng"),
        PickerOption("P05", "Chuồng Đẻ 02", "Khu lồng đẻ ấm áp, đèn úm • Còn 1 lồng"),
        PickerOption("P06", "Chuồng Chờ Phối 01", "Dãy nái cai sữa & hậu bị • Còn 6 ô")
    )

    val GROWER_PENS = listOf(
        PickerOption("G01", "Chuồng Cai Sữa 01", "Sàn nhựa ấm, máng ăn tập ăn • Đang trống"),
        PickerOption("G02", "Chuồng Cai Sữa 02", "Sàn nhựa ấm, núm uống tự động • Đang trống"),
        PickerOption("G03", "Chuồng Thịt 01", "Khu Nuôi Thịt A • Sàn bê tông khe, quạt mát"),
        PickerOption("G04", "Chuồng Thịt 02", "Khu Nuôi Thịt B • Hệ thống làm mát Cooling Pad"),
        PickerOption("G05", "Khu Vỗ Béo A-01", "Giai đoạn chuẩn bị xuất bán (>85kg)")
    )

    val PROCEDURES_AND_VACCINES = listOf(
        PickerOption("PR01", "Tiêm sắt (Iron Dextran)", "D+3 tiêm 1ml/con • Phòng thiếu máu heo con", "Thủ thuật"),
        PickerOption("PR02", "Cắt đuôi & Bấm nanh", "D+7 cắt đuôi, bấm nanh, sát trùng Iodine", "Thủ thuật"),
        PickerOption("PR03", "Vaccine Mycoplasma (Suyễn heo)", "Phòng viêm phổi mạn tính • Liều 2ml/con", "Vaccine"),
        PickerOption("PR04", "Vaccine Circo (PCV2)", "Phòng hội chứng còi cọc sau cai sữa • Liều 1ml/con", "Vaccine"),
        PickerOption("PR05", "Vaccine E.coli", "Phòng tiêu chảy phân trắng heo con • Liều 2ml/con", "Vaccine"),
        PickerOption("PR06", "Vaccine PRRS (Tai xanh)", "Vaccine chủng độc lực cao định kỳ", "Vaccine"),
        PickerOption("PR07", "Vaccine Dịch tả lợn cổ điển (CSF)", "Vaccine nhược độc đông khô bắt buộc", "Vaccine")
    )

    val TRADERS = listOf(
        PickerOption("TR01", "Công ty CP Chăn nuôi Việt Nam", "Hợp đồng thu mua heo thịt số lượng lớn"),
        PickerOption("TR02", "Thương lái Ba Tri", "Thu mua heo thịt xuất lò mổ miền Tây"),
        PickerOption("TR03", "Lái Hùng Đồng Nai", "Thu mua heo thịt đều đặn hàng tuần"),
        PickerOption("TR04", "Lò mổ Vissan", "Tiêu chuẩn kiểm soát dịch bệnh nghiêm ngặt")
    )
}
