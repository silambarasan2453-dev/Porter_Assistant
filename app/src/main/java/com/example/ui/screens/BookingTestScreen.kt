package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calc.RangeClassifier
import com.example.model.RangeClassification
import com.example.ui.AppViewModel
import com.example.ui.CHENNAI_PRESETS
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CyanDark
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingTestScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val simState by viewModel.simulationState.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var showPresetMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Booking Test & Simulator", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
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
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Preset Selection Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "SAMPLE BOOKING PRESETS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary,
                        letterSpacing = 1.sp
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Slate800)
                                .clickable { showPresetMenu = true }
                                .padding(14.dp)
                                .testTag("preset_dropdown_selector"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = CHENNAI_PRESETS[simState.selectedPresetIndex].title,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = CHENNAI_PRESETS[simState.selectedPresetIndex].subtitle,
                                    color = Slate400,
                                    fontSize = 11.sp
                                )
                            }
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = CyanPrimary)
                        }

                        DropdownMenu(
                            expanded = showPresetMenu,
                            onDismissRequest = { showPresetMenu = false },
                            modifier = Modifier.background(Slate800)
                        ) {
                            CHENNAI_PRESETS.forEachIndexed { index, preset ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(preset.title, color = Color.White, fontWeight = FontWeight.SemiBold)
                                            Text(preset.subtitle, color = Slate400, fontSize = 11.sp)
                                        }
                                    },
                                    onClick = {
                                        viewModel.selectPreset(index)
                                        showPresetMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Raw Porter Offer Text Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "RAW PORTER OFFER CARD TEXT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary,
                        letterSpacing = 1.sp
                    )

                    OutlinedTextField(
                        value = simState.rawInputText,
                        onValueChange = { viewModel.updateSimulationInput(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("raw_text_input"),
                        placeholder = { Text("Paste raw text from screen inspection or OCR") },
                        maxLines = 8
                    )

                    // Optional Manual Destination Coordinates for controlled unit testing
                    Text(
                        text = "Controlled Coordinates Override (Optional)",
                        fontSize = 12.sp,
                        color = Slate400,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = simState.manualDestLat,
                            onValueChange = { viewModel.setManualCoordinates(it, simState.manualDestLng) },
                            label = { Text("Dest Lat") },
                            placeholder = { Text("13.1242") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manual_dest_lat_input")
                        )

                        OutlinedTextField(
                            value = simState.manualDestLng,
                            onValueChange = { viewModel.setManualCoordinates(simState.manualDestLat, it) },
                            label = { Text("Dest Lng") },
                            placeholder = { Text("80.2081") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manual_dest_lng_input")
                        )
                    }

                    // Run Simulation Button
                    Button(
                        onClick = { viewModel.runSimulationTest() },
                        enabled = !simState.isSimulating,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("run_simulation_button")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (simState.isSimulating) "Resolving & Calculating..." else "Run Extraction & Classification")
                    }
                }
            }

            // Parsed Extraction Breakdown
            simState.parsedBooking?.let { parsed ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "EXTRACTED FIELDS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Confidence: ${(parsed.confidence * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (parsed.confidence > 0.7f) Color.White else StatusAmber
                            )
                        }

                        Text("Pickup Area: ${parsed.pickupArea}", fontSize = 13.sp, color = Color.White)
                        if (parsed.pickupFullAddress.isNotBlank()) {
                            Text("Pickup Address: ${parsed.pickupFullAddress}", fontSize = 12.sp, color = Slate400)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Destination Area: ${parsed.destinationArea}", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        if (parsed.destinationFullAddress.isNotBlank()) {
                            Text("Destination Address: ${parsed.destinationFullAddress}", fontSize = 12.sp, color = Slate400)
                        }

                        if (parsed.porterTripDistanceKm != null) {
                            Text("Porter Displayed Route: ${parsed.porterTripDistanceKm} km (Excluded from Home calculation)", fontSize = 11.sp, color = Slate400)
                        }
                    }
                }
            }

            // Simulation Result Card
            simState.lastResultRecord?.let { record ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, CyanPrimary)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SIMULATION TEST RESULT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary,
                                letterSpacing = 1.sp
                            )
                            StatusBadge(classification = record.classification)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (record.distanceKm != null) "%.2f km".format(record.distanceKm) else "N/A",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text("Straight-line distance from fixed home", fontSize = 11.sp, color = Slate400)
                            }
                        }

                        // Formula Rationale Explanation
                        val explanation = RangeClassifier.explain(record.distanceKm, settings.radiusKm, settings.bufferKm)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Slate800)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "Rule Explanation: $explanation",
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }

                        Text(
                            text = "Geocoding: ${record.geocodingStatus} • Latency: ${record.processingDurationMs}ms",
                            fontSize = 11.sp,
                            color = CyanPrimary
                        )

                        if (record.failureReason != null) {
                            Text(
                                text = "Notice: ${record.failureReason}",
                                fontSize = 11.sp,
                                color = StatusAmber
                            )
                        }
                    }
                }
            }
        }
    }
}
