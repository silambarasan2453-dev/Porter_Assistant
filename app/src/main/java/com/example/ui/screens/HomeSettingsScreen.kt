package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.core.content.ContextCompat
import com.example.calc.RangeClassifier
import com.example.model.Coordinates
import com.example.ui.AppViewModel
import com.example.ui.theme.CyanDark
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSettingsScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()

    var labelInput by remember { mutableStateOf(settings.homeLabel) }
    var latInput by remember { mutableStateOf(settings.homeCoordinates?.latitude?.toString() ?: "") }
    var lngInput by remember { mutableStateOf(settings.homeCoordinates?.longitude?.toString() ?: "") }

    var radiusValue by remember { mutableFloatStateOf(settings.radiusKm.toFloat()) }
    var bufferValue by remember { mutableFloatStateOf(settings.bufferKm.toFloat()) }
    var overlayToggle by remember { mutableStateOf(settings.overlayEnabled) }

    LaunchedEffect(settings) {
        labelInput = settings.homeLabel
        if (settings.homeCoordinates != null && latInput.isEmpty() && lngInput.isEmpty()) {
            latInput = settings.homeCoordinates!!.latitude.toString()
            lngInput = settings.homeCoordinates!!.longitude.toString()
        }
        radiusValue = settings.radiusKm.toFloat()
        bufferValue = settings.bufferKm.toFloat()
        overlayToggle = settings.overlayEnabled
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            viewModel.fetchCurrentGpsLocation(
                onSuccess = { coords ->
                    latInput = coords.latitude.toString()
                    lngInput = coords.longitude.toString()
                    Toast.makeText(context, "Acquired current GPS coordinates", Toast.LENGTH_SHORT).show()
                },
                onError = { err ->
                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                }
            )
        } else {
            Toast.makeText(context, "Location permission is required to detect GPS", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Home & Range Settings", fontWeight = FontWeight.Bold) },
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
            // Fixed Home Section
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "FIXED HOME BASE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Enter your permanent home coordinates. This is strictly separate from your live GPS location and serves as the distance baseline.",
                        fontSize = 12.sp,
                        color = Slate400
                    )

                    OutlinedTextField(
                        value = labelInput,
                        onValueChange = { labelInput = it },
                        label = { Text("Home Label") },
                        placeholder = { Text("e.g., Home - Kolathur, Chennai") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_label_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = latInput,
                            onValueChange = { latInput = it },
                            label = { Text("Latitude") },
                            placeholder = { Text("13.1242") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_latitude_input")
                        )

                        OutlinedTextField(
                            value = lngInput,
                            onValueChange = { lngInput = it },
                            label = { Text("Longitude") },
                            placeholder = { Text("80.2081") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_longitude_input")
                        )
                    }

                    // Set from GPS Button
                    OutlinedButton(
                        onClick = {
                            val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                            if (fine == PackageManager.PERMISSION_GRANTED) {
                                viewModel.fetchCurrentGpsLocation(
                                    onSuccess = { coords ->
                                        latInput = coords.latitude.toString()
                                        lngInput = coords.longitude.toString()
                                        Toast.makeText(context, "Acquired GPS location", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { err ->
                                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                    }
                                )
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("use_gps_location_button")
                    ) {
                        Icon(imageVector = Icons.Default.MyLocation, contentDescription = null, tint = CyanPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Fill from Current GPS Location")
                    }
                }
            }

            // Radius & Buffer Slider Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "RANGE & REVIEW BUFFER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary,
                        letterSpacing = 1.sp
                    )

                    // Radius Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Maximum Target Radius", fontSize = 13.sp, color = Color.White)
                        Text("%.1f km".format(radiusValue), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                    }
                    Slider(
                        value = radiusValue,
                        onValueChange = { radiusValue = it },
                        valueRange = 5f..50f,
                        steps = 45,
                        colors = SliderDefaults.colors(thumbColor = CyanPrimary, activeTrackColor = CyanDark),
                        modifier = Modifier.testTag("radius_slider")
                    )

                    // Buffer Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Review Buffer", fontSize = 13.sp, color = Color.White)
                        Text("± %.1f km".format(bufferValue), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StatusAmber)
                    }
                    Slider(
                        value = bufferValue,
                        onValueChange = { bufferValue = it },
                        valueRange = 0f..5f,
                        steps = 10,
                        colors = SliderDefaults.colors(thumbColor = StatusAmber, activeTrackColor = StatusAmber.copy(alpha = 0.6f)),
                        modifier = Modifier.testTag("buffer_slider")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Real-Time Threshold Preview Box
                    val thresholds = RangeClassifier.getThresholds(radiusValue.toDouble(), bufferValue.toDouble())
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Slate800)
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "CALCULATED FORMULA THRESHOLDS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate400
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(StatusGreen))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("WITHIN RANGE: < %.1f km".format(thresholds.withinRangeMax), fontSize = 12.sp, color = StatusGreen, fontWeight = FontWeight.SemiBold)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(StatusAmber))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("REVIEW BUFFER: %.1f km to %.1f km".format(thresholds.reviewMin, thresholds.reviewMax), fontSize = 12.sp, color = StatusAmber, fontWeight = FontWeight.SemiBold)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(StatusRed))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("OUTSIDE RANGE: > %.1f km".format(thresholds.outsideRangeMin), fontSize = 12.sp, color = StatusRed, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Overlay Toggle Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Floating HUD Overlay", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            "Display an instant range pill over Porter when an offer card is detected.",
                            fontSize = 12.sp,
                            color = Slate400
                        )
                    }
                    Switch(
                        checked = overlayToggle,
                        onCheckedChange = {
                            overlayToggle = it
                            viewModel.toggleOverlay(it)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyanPrimary),
                        modifier = Modifier.testTag("overlay_toggle_switch")
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.clearHomeLocation()
                        latInput = ""
                        lngInput = ""
                        labelInput = "Home Base"
                        radiusValue = 20f
                        bufferValue = 1f
                        Toast.makeText(context, "Settings reset to defaults", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("reset_settings_button")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset")
                }

                Button(
                    onClick = {
                        val lat = latInput.trim().toDoubleOrNull()
                        val lng = lngInput.trim().toDoubleOrNull()
                        if (lat != null && lng != null) {
                            if (!Coordinates.isValid(lat, lng)) {
                                Toast.makeText(context, "Invalid coordinates. Lat [-90,90], Lng [-180,180]", Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            viewModel.saveHomeLocation(lat, lng, labelInput.trim())
                        }
                        viewModel.saveRadiusAndBuffer(radiusValue.toDouble(), bufferValue.toDouble())
                        Toast.makeText(context, "Home location & range saved successfully", Toast.LENGTH_SHORT).show()
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_settings_button")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save")
                }
            }
        }
    }
}
