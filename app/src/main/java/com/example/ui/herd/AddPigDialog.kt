package com.example.ui.herd

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SowReproductiveStatus
import com.example.ui.components.OutdoorInputField
import com.example.ui.theme.FarmGreenPrimary
import com.example.ui.theme.FarmOutline
import com.example.ui.theme.FarmTextPrimary
import com.example.ui.theme.LocalOutdoorMode
import com.example.ui.theme.OutdoorBorder
import com.example.ui.theme.OutdoorCardBorder
import com.example.ui.theme.OutdoorFocusBorder
import com.example.ui.theme.OutdoorInputFill
import com.example.ui.theme.OutdoorLabelText
import com.example.ui.theme.OutdoorModeManager
import com.example.ui.theme.OutdoorSelectedRadio

@Composable
fun AddPigDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        tagCode: String,
        isBatch: Boolean,
        quantity: Int,
        breed: String,
        gender: String,
        status: SowReproductiveStatus,
        location: String,
        currentWeightKg: Float,
        parity: Int
    ) -> Unit
) {
    var isBatch by remember { mutableStateOf(false) }
    var tagCode by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("1") }
    var breed by remember { mutableStateOf("York-Land") }
    var gender by remember { mutableStateOf("Cái") }
    var status by remember { mutableStateOf(SowReproductiveStatus.HAU_BI) }
    var location by remember { mutableStateOf("Chuồng Hậu bị 1") }
    var weightText by remember { mutableStateOf("120") }
    var parityText by remember { mutableStateOf("0") }

    val isOutdoor = LocalOutdoorMode.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = OutdoorInputFill),
            border = BorderStroke(
                width = if (isOutdoor) 2.dp else 1.5.dp,
                color = if (isOutdoor) OutdoorCardBorder else OutdoorBorder
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isOutdoor) 0.dp else 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Thêm Cá thể / Lô mới",
                    fontSize = OutdoorModeManager.scaledSp(18f, isOutdoor),
                    fontWeight = FontWeight.Bold,
                    color = OutdoorLabelText
                )

                // Type selector: Cá thể vs Lô
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            isBatch = false
                            quantityText = "1"
                            status = SowReproductiveStatus.HAU_BI
                        }
                    ) {
                        RadioButton(
                            selected = !isBatch,
                            onClick = {
                                isBatch = false
                                quantityText = "1"
                                status = SowReproductiveStatus.HAU_BI
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = OutdoorSelectedRadio,
                                unselectedColor = OutdoorBorder
                            )
                        )
                        Text(
                            text = "Heo Nái Cá Thể",
                            fontSize = OutdoorModeManager.scaledSp(15f, isOutdoor),
                            fontWeight = FontWeight.SemiBold,
                            color = OutdoorLabelText
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            isBatch = true
                            quantityText = "50"
                            status = SowReproductiveStatus.DANG_NUOI
                        }
                    ) {
                        RadioButton(
                            selected = isBatch,
                            onClick = {
                                isBatch = true
                                quantityText = "50"
                                status = SowReproductiveStatus.DANG_NUOI
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = OutdoorSelectedRadio,
                                unselectedColor = OutdoorBorder
                            )
                        )
                        Text(
                            text = "Theo Lô (Đàn)",
                            fontSize = OutdoorModeManager.scaledSp(15f, isOutdoor),
                            fontWeight = FontWeight.SemiBold,
                            color = OutdoorLabelText
                        )
                    }
                }

                OutdoorInputField(
                    label = if (isBatch) "Mã Lô" else "Số tai (Ear Tag)",
                    value = tagCode,
                    onValueChange = { tagCode = it },
                    placeholder = if (isBatch) "ví dụ: Lô LH-205" else "ví dụ: NY-090",
                    isRequired = true,
                    testTag = "add_pig_input_tag"
                )

                if (isBatch) {
                    OutdoorInputField(
                        label = "Số lượng con trong lô",
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = "ví dụ: 50",
                        isRequired = true,
                        testTag = "add_pig_input_quantity"
                    )
                } else {
                    OutdoorInputField(
                        label = "Lứa đẻ (Parity)",
                        value = parityText,
                        onValueChange = { parityText = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = "0: Hậu bị, 1, 2...",
                        testTag = "add_pig_input_parity"
                    )
                }

                OutdoorInputField(
                    label = "Giống heo",
                    value = breed,
                    onValueChange = { breed = it },
                    placeholder = "Landrace, Yorkshire, Duroc, F1...",
                    isRequired = true,
                    testTag = "add_pig_input_breed"
                )

                OutdoorInputField(
                    label = "Vị trí chuồng / ô",
                    value = location,
                    onValueChange = { location = it },
                    placeholder = "ví dụ: Chuồng Hậu bị 1, Ô 04",
                    isRequired = true,
                    testTag = "add_pig_input_location"
                )

                OutdoorInputField(
                    label = "Trọng lượng bình quân (kg)",
                    value = weightText,
                    onValueChange = { weightText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    placeholder = "ví dụ: 120",
                    testTag = "add_pig_input_weight"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.5.dp, OutdoorBorder),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(
                            text = "Hủy",
                            fontSize = OutdoorModeManager.scaledSp(15f, isOutdoor),
                            fontWeight = FontWeight.Bold,
                            color = OutdoorLabelText
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            if (tagCode.isNotBlank()) {
                                onConfirm(
                                    tagCode.trim(),
                                    isBatch,
                                    quantityText.toIntOrNull() ?: 1,
                                    breed.trim(),
                                    gender,
                                    status,
                                    location.trim(),
                                    weightText.toFloatOrNull() ?: 100f,
                                    parityText.toIntOrNull() ?: 0
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OutdoorFocusBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("add_pig_btn_confirm")
                    ) {
                        Text(
                            text = "Lưu bản ghi",
                            fontSize = OutdoorModeManager.scaledSp(15f, isOutdoor),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
