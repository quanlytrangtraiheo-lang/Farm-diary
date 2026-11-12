package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FarmRed
import com.example.ui.theme.LocalOutdoorMode
import com.example.ui.theme.OutdoorBorder
import com.example.ui.theme.OutdoorFocusBorder
import com.example.ui.theme.OutdoorFocusHalo
import com.example.ui.theme.OutdoorIcon
import com.example.ui.theme.OutdoorInputFill
import com.example.ui.theme.OutdoorInputValue
import com.example.ui.theme.OutdoorLabelText
import com.example.ui.theme.OutdoorModeManager
import com.example.ui.theme.OutdoorPlaceholder
import com.example.ui.theme.OutdoorRequiredStar

/**
 * Trường nhập liệu tối ưu hóa cho môi trường chói nắng ngoài trời (Outdoor High-Contrast WCAG AAA).
 * Đặc điểm kiến trúc:
 * 1. Persistent Top Label: Nhãn luôn nằm cố định phía trên ô nhập với cỡ chữ >= 15sp Semi-Bold #0F172A.
 * 2. Dấu sao đỏ bắt buộc (*) #B91C1C nét dày.
 * 3. Vùng bấm (Touch Box): Chiều cao 56dp (hoặc tối thiểu 56dp).
 * 4. Bo góc 8dp: Tối đa hóa diện tích trắng phẳng đọc rõ số và chữ.
 * 5. Đường viền: 1.5dp Xanh đen đậm #1E293B; khi Focus là 2.5dp Xanh đậm #0F52BA kèm halo chống lóa.
 * 6. Giá trị hiển thị: Đen tuyền #000000 Bold 15sp-16sp.
 * 7. Placeholder: Xám đậm #475569.
 */
@Composable
fun OutdoorInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isRequired: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    testTag: String = "outdoor_input_field"
) {
    val isOutdoor = LocalOutdoorMode.current
    var isFocused by remember { mutableStateOf(false) }

    val labelFontSize = OutdoorModeManager.scaledSp(15f, isOutdoor)
    val inputFontSize = OutdoorModeManager.scaledSp(15f, isOutdoor)
    val subFontSize = OutdoorModeManager.scaledSp(12f, isOutdoor)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 1. Persistent Top Label (>= 15sp Semi-Bold, phân tách rõ ràng)
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

        // 2. Container Box với Focus Halo chống lóa
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isFocused && !isError) {
                        Modifier.border(
                            width = 4.dp,
                            color = OutdoorFocusHalo,
                            shape = RoundedCornerShape(10.dp)
                        )
                    } else Modifier
                )
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    if (placeholder.isNotBlank()) {
                        Text(
                            text = placeholder,
                            fontSize = inputFontSize,
                            color = OutdoorPlaceholder,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                singleLine = singleLine,
                readOnly = readOnly,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                isError = isError,
                textStyle = TextStyle(
                    fontSize = inputFontSize,
                    fontWeight = FontWeight.Bold,
                    color = OutdoorInputValue
                ),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isError) FarmRed else OutdoorFocusBorder,
                    unfocusedBorderColor = if (isError) FarmRed else if (isOutdoor) Color(0xFF0F172A) else OutdoorBorder,
                    errorBorderColor = FarmRed,
                    focusedContainerColor = OutdoorInputFill,
                    unfocusedContainerColor = OutdoorInputFill,
                    cursorColor = OutdoorFocusBorder,
                    focusedTextColor = OutdoorInputValue,
                    unfocusedTextColor = OutdoorInputValue
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .onFocusChanged { isFocused = it.isFocused }
                    .testTag(testTag)
            )
        }

        // Thông báo lỗi nếu có
        if (isError && !errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                fontSize = subFontSize,
                fontWeight = FontWeight.Bold,
                color = OutdoorRequiredStar,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}
