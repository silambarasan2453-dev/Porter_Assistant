package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DetectionRecord
import com.example.model.RangeClassification
import com.example.ui.AppViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CyanDark
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: AppViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToSimulator: () -> Unit,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val latestDetection by viewModel.latestDetection.collectAsState()
    val isAccessibilityBound by viewModel.isAccessibilityBound.collectAsState()
    val diagnosticState by viewModel.diagnosticState.collectAsState()

    var showDiagnostics by remember { mutableStateOf(false) }

    val canDrawOverlays = viewModel.canDrawOverlays()

    // Determine actual monitoring status
    // Must NEVER show RUNNING unless the accessibility service is genuinely bound and active!
    val monitoringStatus = when {
        !settings.isMonitoringActive -> "STOPPED"
        !isAccessibilityBound -> "PERMISSION REQUIRED"
        else -> "RUNNING"
    }

    val statusColor = when (monitoringStatus) {
        "RUNNING" -> StatusGreen
        "PERMISSION REQUIRED" -> StatusAmber
        else -> Slate400
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Title & Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Porter Location Assistant",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Driver Range Companion",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )
            }

            IconButton(
                onClick = { showDiagnostics = !showDiagnostics },
                modifier = Modifier.testTag("toggle_diagnostics_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Diagnostics",
                    tint = if (showDiagnostics) CyanPrimary else Slate400
                )
            }
        }

        // Monitoring Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("monitoring_status_card"),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MONITORING STATUS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        letterSpacing = 1.sp
                    )

                    // Status Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = monitoringStatus,
                            color = statusColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (monitoringStatus == "PERMISSION REQUIRED") {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate800),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = StatusAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Accessibility Service is not enabled",
                                    color = StatusAmber,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Android requires explicit permission for Porter Location Assistant in Accessibility settings to read visible booking screens.",
                                color = Slate400,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("enable_accessibility_button")
                            ) {
                                Text("Open Accessibility Settings", fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Overlay Permission warning if missing
                if (!canDrawOverlays) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate800),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Floating Overlay Permission Needed",
                                    color = StatusAmber,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Required to display the quick range badge over the Porter app.",
                                    color = Slate400,
                                    fontSize = 11.sp
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        val intent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                                        context.startActivity(intent)
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("grant_overlay_button")
                            ) {
                                Text("Grant", fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Monitoring Toggle Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.toggleMonitoring(true) },
                        enabled = !settings.isMonitoringActive,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("start_monitoring_button")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start Monitoring")
                    }

                    OutlinedButton(
                        onClick = { viewModel.toggleMonitoring(false) },
                        enabled = settings.isMonitoringActive,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stop_monitoring_button")
                    ) {
                        Icon(imageVector = Icons.Default.Stop, contentDescription = null, tint = StatusRed)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Stop", color = StatusRed)
                    }
                }
            }
        }

        // Diagnostics Dropdown Card
        AnimatedVisibility(visible = showDiagnostics) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("diagnostics_card"),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "SERVICE DIAGNOSTICS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary
                    )
                    Text("• Accessibility Service Bound: ${if (isAccessibilityBound) "YES (Active)" else "NO (Disabled in OS)"}", fontSize = 12.sp, color = Slate400)
                    Text("• Floating Overlay Permitted: ${if (canDrawOverlays) "YES" else "NO"}", fontSize = 12.sp, color = Slate400)
                    Text("• Offers Detected: ${diagnosticState.offersDetectedCount}", fontSize = 12.sp, color = Slate400)
                    Text("• Last Inspected Package: ${diagnosticState.lastEventPackage ?: "None"}", fontSize = 12.sp, color = Slate400)
                    Text("• Status: ${diagnosticState.statusMessage}", fontSize = 12.sp, color = Slate400)
                }
            }
        }

        // Home Location & Radius Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("home_location_summary_card"),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "FIXED HOME BASE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate400,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = settings.homeLabel.ifBlank { "Home Base" },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("edit_home_button")
                    ) {
                        Icon(imageVector = Icons.Default.EditLocation, contentDescription = "Edit Home", tint = CyanPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (settings.homeCoordinates != null) {
                    Text(
                        text = "Coordinates: %.4f, %.4f".format(
                            settings.homeCoordinates!!.latitude,
                            settings.homeCoordinates!!.longitude
                        ),
                        fontSize = 13.sp,
                        color = Slate400
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = null, tint = StatusAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Home location not configured. Distances cannot be calculated.",
                            color = StatusAmber,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Radius & Buffer Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate800),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("RADIUS", fontSize = 10.sp, color = Slate400, fontWeight = FontWeight.Bold)
                            Text("%.1f km".format(settings.radiusKm), fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate800),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("BUFFER", fontSize = 10.sp, color = Slate400, fontWeight = FontWeight.Bold)
                            Text("± %.1f km".format(settings.bufferKm), fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate800),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("SAFE ZONE", fontSize = 10.sp, color = StatusGreen, fontWeight = FontWeight.Bold)
                            Text("< %.1f km".format(settings.radiusKm - settings.bufferKm), fontSize = 15.sp, color = StatusGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Latest Detected Booking Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("latest_detection_card"),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LATEST DETECTED BOOKING",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        letterSpacing = 1.sp
                    )

                    latestDetection?.let {
                        Text(
                            text = formatTimestamp(it.timestamp),
                            fontSize = 11.sp,
                            color = Slate400
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val record = latestDetection
                if (record != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusBadge(classification = record.classification, isLarge = true)

                        if (record.distanceKm != null) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "%.1f km".format(record.distanceKm),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Straight-line",
                                    fontSize = 10.sp,
                                    color = Slate400
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Destination:", fontSize = 11.sp, color = Slate400, fontWeight = FontWeight.Bold)
                    Text(record.destinationText, fontSize = 14.sp, color = Color.White)

                    if (record.pickupText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Pickup:", fontSize = 11.sp, color = Slate400, fontWeight = FontWeight.Bold)
                        Text(record.pickupText, fontSize = 13.sp, color = Slate400)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Resolved in ${record.processingDurationMs}ms (${record.geocodingStatus})",
                            fontSize = 11.sp,
                            color = CyanPrimary
                        )

                        Text(
                            text = "Conf: ${(record.extractionConfidence * 100).toInt()}%",
                            fontSize = 11.sp,
                            color = Slate400
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No bookings detected yet",
                                color = Slate400,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Use Booking Simulator or launch Porter while monitoring",
                                color = Slate600,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Quick Navigation Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onNavigateToSimulator,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("nav_simulator_button")
            ) {
                Text("Test Simulator")
            }

            OutlinedButton(
                onClick = onNavigateToHistory,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("nav_history_button")
            ) {
                Text("View History")
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("h:mm:ss a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
