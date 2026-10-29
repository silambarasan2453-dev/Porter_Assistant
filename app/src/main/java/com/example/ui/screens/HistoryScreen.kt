package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.data.local.DetectionRecord
import com.example.model.RangeClassification
import com.example.ui.AppViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allDetections by viewModel.allDetections.collectAsState()
    val activeFilter by viewModel.historyFilter.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedRecordForDialog by remember { mutableStateOf<DetectionRecord?>(null) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val filteredList = allDetections.filter { record ->
        val matchesFilter = activeFilter == null || record.classification == activeFilter
        val matchesSearch = searchQuery.isBlank() ||
                record.destinationText.contains(searchQuery, ignoreCase = true) ||
                record.pickupText.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesSearch
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Detection History", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                if (allDetections.isNotEmpty()) {
                    IconButton(
                        onClick = { showClearConfirmDialog = true },
                        modifier = Modifier.testTag("clear_all_history_button")
                    ) {
                        Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear History", tint = StatusRed)
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Slate900,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by drop area or street...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Slate400) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("history_search_input"),
                singleLine = true
            )

            // Filter Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = activeFilter == null,
                        onClick = { viewModel.setHistoryFilter(null) },
                        label = { Text("All (${allDetections.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                RangeClassification.values().forEach { classification ->
                    val count = allDetections.count { it.classification == classification }
                    item {
                        FilterChip(
                            selected = activeFilter == classification,
                            onClick = {
                                viewModel.setHistoryFilter(
                                    if (activeFilter == classification) null else classification
                                )
                            },
                            label = { Text("${classification.label} ($count)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // List
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No detection records found", color = Slate400, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Incoming Porter cards will be recorded here", color = Slate700, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.id }) { record ->
                    HistoryItemCard(
                        record = record,
                        onClick = { selectedRecordForDialog = record },
                        onDelete = { viewModel.deleteHistoryRecord(record.id) }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }

    // Detail Dialog
    selectedRecordForDialog?.let { record ->
        AlertDialog(
            onDismissRequest = { selectedRecordForDialog = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Detection Record", fontWeight = FontWeight.Bold)
                    StatusBadge(classification = record.classification)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Timestamp: ${formatFullDate(record.timestamp)}", fontSize = 12.sp, color = Slate400)
                    Text("Destination: ${record.destinationText}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    if (record.destinationLat != null && record.destinationLng != null) {
                        Text("Coordinates: %.5f, %.5f".format(record.destinationLat, record.destinationLng), fontSize = 12.sp, color = CyanPrimary)
                    }
                    if (record.distanceKm != null) {
                        Text("Distance from Home: %.2f km (straight-line)".format(record.distanceKm), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    if (record.pickupText.isNotBlank()) {
                        Text("Pickup: ${record.pickupText}", fontSize = 12.sp, color = Slate400)
                    }
                    Text("Resolution Source: ${record.geocodingStatus}", fontSize = 12.sp, color = Slate400)
                    Text("Processing Latency: ${record.processingDurationMs}ms", fontSize = 12.sp, color = Slate400)
                    Text("Parse Confidence: ${(record.extractionConfidence * 100).toInt()}%", fontSize = 12.sp, color = Slate400)
                    if (record.failureReason != null) {
                        Text("Notice: ${record.failureReason}", fontSize = 12.sp, color = StatusRed)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedRecordForDialog = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Clear All Confirmation Dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear All Detection History?") },
            text = { Text("This will permanently remove all stored local detection records from device storage.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearHistory()
                    showClearConfirmDialog = false
                }) {
                    Text("Clear All", color = StatusRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun HistoryItemCard(
    record: DetectionRecord,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("history_item_${record.id}"),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(classification = record.classification)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatTime(record.timestamp),
                        fontSize = 11.sp,
                        color = Slate400
                    )
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp).padding(start = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Slate700, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.destinationText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )
                    if (record.pickupText.isNotBlank()) {
                        Text(
                            text = "From: ${record.pickupText}",
                            fontSize = 11.sp,
                            color = Slate400,
                            maxLines = 1
                        )
                    }
                }

                if (record.distanceKm != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "%.1f km".format(record.distanceKm),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "${record.processingDurationMs}ms",
                            fontSize = 10.sp,
                            color = CyanPrimary
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatFullDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy h:mm:ss a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
