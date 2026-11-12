package com.example.ui.task

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.data.local.entity.TaskEntity
import com.example.ui.components.TaskPriorityBadge
import com.example.ui.components.TaskStatusBadge
import com.example.ui.theme.FarmBackground
import com.example.ui.theme.FarmGreenPrimary
import com.example.ui.theme.FarmOutline
import com.example.ui.theme.FarmTextPrimary
import com.example.ui.theme.FarmTextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskScreen(
    viewModel: TaskViewModel,
    onAddNewTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FarmBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CÔNG VIỆC",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = FarmTextPrimary,
                        letterSpacing = 0.5.sp
                    )

                    IconButton(
                        onClick = { /* filter icon */ },
                        modifier = Modifier
                            .testTag("task_filter_icon")
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, FarmOutline, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Bộ lọc",
                            tint = FarmGreenPrimary
                        )
                    }
                }
            }

            // Search Bar (PDF Page 10)
            item {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = {
                        Text(
                            text = "Tìm kiếm công việc...",
                            fontSize = 15.sp,
                            color = FarmTextSecondary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = FarmTextSecondary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_search_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = FarmGreenPrimary,
                        unfocusedBorderColor = FarmOutline
                    ),
                    singleLine = true
                )
            }

            // Chips Filter (PDF Page 10: Tất cả, Hôm nay, Tuần này, Chưa làm, Đang làm, Đã xong)
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TaskFilterOption.values().forEach { filter ->
                        val isSelected = state.activeFilter == filter
                        Box(
                            modifier = Modifier
                                .testTag("task_chip_${filter.name.lowercase()}")
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) FarmGreenPrimary else Color.White)
                                .border(
                                    1.dp,
                                    if (isSelected) FarmGreenPrimary else FarmOutline,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { viewModel.onFilterSelected(filter) }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = filter.label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else FarmTextPrimary
                            )
                        }
                    }
                }
            }

            // Task Items List (PDF Page 10)
            items(state.tasks, key = { it.id }) { task ->
                TaskDetailedCardItem(
                    task = task,
                    onStatusClick = { viewModel.toggleTaskStatus(task) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // Green Floating Action Button (+) (PDF Page 10)
        FloatingActionButton(
            onClick = onAddNewTask,
            containerColor = FarmGreenPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("task_fab_add")
                .size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Thêm công việc",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun TaskDetailedCardItem(
    task: TaskEntity,
    onStatusClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_item_${task.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = FarmTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${task.timeLabel} - ${task.referenceId} (${task.locationInfo})",
                        fontSize = 14.sp,
                        color = FarmTextSecondary
                    )
                    if (task.note.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = task.note,
                            fontSize = 12.sp,
                            color = Color(0xFF6B7A6B)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TaskPriorityBadge(priority = task.priority)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onStatusClick() }
                    ) {
                        TaskStatusBadge(status = task.status)
                    }
                }
            }
        }
    }
}
