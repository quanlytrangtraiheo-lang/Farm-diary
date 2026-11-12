package com.example.ui.herd

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PigEntity
import com.example.ui.components.ReproductiveStatusBadge
import com.example.ui.components.SyncStatusBadge
import com.example.ui.theme.FarmBackground
import com.example.ui.theme.FarmGreenContainer
import com.example.ui.theme.FarmGreenPrimary
import com.example.ui.theme.FarmOutline
import com.example.ui.theme.FarmRed
import com.example.ui.theme.FarmTextPrimary
import com.example.ui.theme.FarmTextSecondary

@Composable
fun HerdListScreen(
    viewModel: HerdViewModel,
    onPigSelected: (String) -> Unit,
    onAddNewPig: () -> Unit,
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
                        text = "ĐÀN HEO",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = FarmTextPrimary,
                        letterSpacing = 0.5.sp
                    )

                    IconButton(
                        onClick = { /* quick filter */ },
                        modifier = Modifier
                            .testTag("herd_filter_icon")
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

            // Search Bar (PDF Page 8)
            item {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = {
                        Text(
                            text = "Tìm theo mã, lô, chuồng...",
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
                        .testTag("herd_search_input"),
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

            // Filter Chips (PDF Page 8)
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(HerdFilterType.values()) { filter ->
                        val isSelected = state.activeFilter == filter
                        Box(
                            modifier = Modifier
                                .testTag("herd_chip_${filter.name.lowercase()}")
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) FarmGreenPrimary else Color.White)
                                .border(
                                    1.dp,
                                    if (isSelected) FarmGreenPrimary else FarmOutline,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { viewModel.onFilterSelected(filter) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
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

            // Pig Cards List (PDF Page 8)
            items(state.pigs, key = { it.id }) { pig ->
                PigListItemCard(
                    pig = pig,
                    onClick = {
                        viewModel.selectPig(pig.id)
                        onPigSelected(pig.id)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // Green Floating Action Button (+) (PDF Page 8)
        FloatingActionButton(
            onClick = onAddNewPig,
            containerColor = FarmGreenPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("herd_fab_add")
                .size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Thêm Đàn/Heo mới",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun PigListItemCard(
    pig: PigEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pig_item_${pig.tagCode}")
            .clickable { onClick() },
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pig.tagCode,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = FarmTextPrimary
                )
                SyncStatusBadge(status = pig.syncStatus)
            }

            // Info details
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (pig.isBatch) {
                    Text(
                        text = "Số lượng: ${pig.quantity} con",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FarmTextSecondary
                    )
                }
                Text(
                    text = "Giống: ${pig.breed}",
                    fontSize = 14.sp,
                    color = FarmTextSecondary
                )
                Text(
                    text = "Chuồng: ${pig.location}",
                    fontSize = 14.sp,
                    color = FarmTextSecondary
                )
            }

            // Status Badge & Warnings Row (PDF Page 8)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Calculate days in status if pregnant
                val daysInGestation = if (pig.lastMatingDate != null) {
                    ((System.currentTimeMillis() - pig.lastMatingDate) / 86_400_000L).toInt()
                } else null

                ReproductiveStatusBadge(
                    status = pig.status,
                    daysInStatus = daysInGestation
                )

                if (pig.hasOverdueVaccine) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = FarmRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "quá hạn tiêm",
                            color = FarmRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (pig.hasUltrasoundWarning) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFF57C00),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "quá hạn siêu âm",
                            color = Color(0xFFF57C00),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
