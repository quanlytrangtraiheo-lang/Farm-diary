package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Trình quản lý Chế độ Ngoài trời (Outdoor High-Contrast Mode - WCAG AAA).
 * Khi kích hoạt:
 * 1. Chuyển sang bảng màu WCAG AAA độ tương phản cao, nền trắng #FFFFFF trên xám #EDF0F4.
 * 2. Tăng 1.2x kích cỡ chữ trên các biểu mẫu nhập liệu, nhãn và danh sách chọn.
 * 3. Tắt bóng mờ nhạt (Box Shadow -> 0dp) và thay thế bằng viền phẳng sắc nét (Solid Stroke 1.5dp - 2dp).
 */
object OutdoorModeManager {
    private val _isOutdoorMode = MutableStateFlow(false)
    val isOutdoorMode: StateFlow<Boolean> = _isOutdoorMode.asStateFlow()

    fun toggleOutdoorMode() {
        _isOutdoorMode.value = !_isOutdoorMode.value
    }

    fun setOutdoorMode(enabled: Boolean) {
        _isOutdoorMode.value = enabled
    }

    fun getFontScale(isOutdoor: Boolean): Float = if (isOutdoor) 1.2f else 1.0f

    fun scaledSp(baseSp: Float, isOutdoor: Boolean): TextUnit {
        return (baseSp * getFontScale(isOutdoor)).sp
    }

    fun getBorderStroke(isOutdoor: Boolean): BorderStroke {
        return if (isOutdoor) {
            BorderStroke(2.dp, OutdoorBorder)
        } else {
            BorderStroke(1.dp, FarmOutline)
        }
    }

    fun getCardElevation(isOutdoor: Boolean, defaultElevation: Dp = 2.dp): Dp {
        return if (isOutdoor) 0.dp else defaultElevation
    }
}

val LocalOutdoorMode = compositionLocalOf { false }

/**
 * Nút chuyển đổi nhanh Chế độ ngoài trời (Quick Toggle) đặt trên Header bar.
 * testTag = "outdoor_mode_toggle"
 */
@Composable
fun OutdoorModeToggleButton(
    modifier: Modifier = Modifier,
    iconOnly: Boolean = false
) {
    val isOutdoor by OutdoorModeManager.isOutdoorMode.collectAsState()

    Surface(
        onClick = { OutdoorModeManager.toggleOutdoorMode() },
        shape = if (iconOnly) CircleShape else RoundedCornerShape(20.dp),
        color = if (isOutdoor) Color(0xFF0F172A) else Color.White,
        border = BorderStroke(
            width = if (isOutdoor) 2.dp else 1.dp,
            color = if (isOutdoor) Color(0xFF0F52BA) else FarmOutline
        ),
        shadowElevation = if (isOutdoor) 0.dp else 2.dp,
        modifier = modifier.testTag("outdoor_mode_toggle")
    ) {
        if (iconOnly) {
            Box(
                modifier = Modifier.size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isOutdoor) Icons.Default.WbSunny else Icons.Default.LightMode,
                    contentDescription = "Chuyển chế độ ngoài trời",
                    tint = if (isOutdoor) Color(0xFFFFD54F) else FarmAmber,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (isOutdoor) Icons.Default.WbSunny else Icons.Default.LightMode,
                    contentDescription = "Chuyển chế độ ngoài trời",
                    tint = if (isOutdoor) Color(0xFFFFD54F) else FarmAmber,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (isOutdoor) "Ngoài trời: BẬT" else "Ngoài trời",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isOutdoor) Color.White else FarmTextPrimary
                )
            }
        }
    }
}
