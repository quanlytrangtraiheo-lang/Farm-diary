package com.example.ui.task

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.TaskPriority
import com.example.data.model.TaskType
import com.example.ui.components.OutdoorInputField
import com.example.ui.theme.FarmGreenPrimary
import com.example.ui.theme.LocalOutdoorMode
import com.example.ui.theme.OutdoorBorder
import com.example.ui.theme.OutdoorCardBorder
import com.example.ui.theme.OutdoorFocusBorder
import com.example.ui.theme.OutdoorInputFill
import com.example.ui.theme.OutdoorLabelText
import com.example.ui.theme.OutdoorModeManager
import com.example.ui.theme.OutdoorSelectedRadio

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        taskType: TaskType,
        timeLabel: String,
        referenceId: String,
        locationInfo: String,
        priority: TaskPriority,
        note: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var timeLabel by remember { mutableStateOf("09:00") }
    var referenceId by remember { mutableStateOf("Lô LH-204") }
    var locationInfo by remember { mutableStateOf("Chuồng Cấp dưỡng 3") }
    var isHighPriority by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }

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
                    text = "Tạo công việc mới",
                    fontSize = OutdoorModeManager.scaledSp(18f, isOutdoor),
                    fontWeight = FontWeight.Bold,
                    color = OutdoorLabelText
                )

                OutdoorInputField(
                    label = "Tên công việc",
                    value = title,
                    onValueChange = { title = it },
                    placeholder = "VD: Tiêm vắc-xin, Sát trùng...",
                    isRequired = true,
                    testTag = "add_task_input_title"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutdoorInputField(
                        label = "Giờ thực hiện",
                        value = timeLabel,
                        onValueChange = { timeLabel = it },
                        placeholder = "09:00",
                        modifier = Modifier.weight(1f)
                    )
                    OutdoorInputField(
                        label = "Đối tượng",
                        value = referenceId,
                        onValueChange = { referenceId = it },
                        placeholder = "Lô LH-204",
                        modifier = Modifier.weight(1f)
                    )
                }

                OutdoorInputField(
                    label = "Vị trí chuồng / ô cụ thể",
                    value = locationInfo,
                    onValueChange = { locationInfo = it },
                    placeholder = "Chuồng Cấp dưỡng 3",
                    testTag = "add_task_input_location"
                )

                OutdoorInputField(
                    label = "Ghi chú kỹ thuật",
                    value = note,
                    onValueChange = { note = it },
                    placeholder = "Liều lượng, lưu ý an toàn sinh học...",
                    testTag = "add_task_input_note"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Đánh dấu Ưu tiên cao",
                        fontSize = OutdoorModeManager.scaledSp(15f, isOutdoor),
                        fontWeight = FontWeight.SemiBold,
                        color = OutdoorLabelText
                    )
                    Switch(
                        checked = isHighPriority,
                        onCheckedChange = { isHighPriority = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = OutdoorInputFill,
                            checkedTrackColor = OutdoorFocusBorder
                        )
                    )
                }

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
                            if (title.isNotBlank()) {
                                onConfirm(
                                    title.trim(),
                                    TaskType.GENERAL,
                                    timeLabel.trim(),
                                    referenceId.trim(),
                                    locationInfo.trim(),
                                    if (isHighPriority) TaskPriority.HIGH else TaskPriority.NORMAL,
                                    note.trim()
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OutdoorFocusBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("add_task_btn_confirm")
                    ) {
                        Text(
                            text = "Lưu công việc",
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
