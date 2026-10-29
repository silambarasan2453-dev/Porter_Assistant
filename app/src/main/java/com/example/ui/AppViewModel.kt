package com.example.ui

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.PorterAssistantApp
import com.example.calc.HaversineCalculator
import com.example.calc.RangeClassifier
import com.example.data.local.DetectionRecord
import com.example.data.preferences.UserSettings
import com.example.model.Coordinates
import com.example.model.RangeClassification
import com.example.parser.BookingCardParser
import com.example.parser.ParsedBooking
import com.example.service.DetectionCoordinator
import com.example.service.DiagnosticState
import com.example.service.PorterAccessibilityService
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SimulatorPreset(
    val title: String,
    val subtitle: String,
    val rawText: String,
    val knownCoords: Coordinates? = null
)

val CHENNAI_PRESETS = listOf(
    SimulatorPreset(
        title = "Chennai Kolathur Offer (Screenshot)",
        subtitle = "From Ponniyammanmedu to Kolathur, Chennai (~₹137 offer)",
        rawText = """
            ₹137
            PICKUP
            ponniyammanmedu (0.5 km)
            7, 5th Main Rd, Muthamizh Nagar, ponniyammanmedu, Chennai, Tamil Nadu 600118, India
            DROP
            Kolathur (10.7 km)
            45PG+JQC, Sri Nagar, Kolathur, Chennai, Tamil Nadu 600053, India
            Accept in 7s
            - ₹67.56
            Ledger Balance
            Skip X
            Recharge
        """.trimIndent(),
        knownCoords = Coordinates(13.1242, 80.2081)
    ),
    SimulatorPreset(
        title = "Anna Nagar East Offer",
        subtitle = "Nearby drop location (~6 km from North Chennai)",
        rawText = """
            ₹195
            PICKUP
            Perambur (1.2 km)
            Madhavaram High Road, Perambur, Chennai 600011
            DROP
            Anna Nagar (6.2 km)
            2nd Avenue, Anna Nagar East, Chennai, Tamil Nadu 600102
            Accept in 12s
        """.trimIndent(),
        knownCoords = Coordinates(13.0850, 80.2101)
    ),
    SimulatorPreset(
        title = "Tambaram Sanatorium (Far / Long Distance)",
        subtitle = "Far South Chennai drop (~28 km from Kolathur base)",
        rawText = """
            ₹480
            PICKUP
            Koyambedu (2.0 km)
            Omni Bus Stand, Koyambedu, Chennai 600107
            DROP
            Tambaram (28.4 km)
            GST Road, Tambaram Sanatorium, Chennai, Tamil Nadu 600047
            Accept in 10s
        """.trimIndent(),
        knownCoords = Coordinates(12.9249, 80.1000)
    ),
    SimulatorPreset(
        title = "Ambiguous Landmark (Missing Pin / Area only)",
        subtitle = "Test handling for unverified or truncated addresses",
        rawText = """
            ₹150
            PICKUP
            Purasawalkam (0.8 km)
            Purasawalkam High Rd
            DROP
            Near Temple Complex
            Accept in 5s
        """.trimIndent(),
        knownCoords = null
    )
)

data class SimulationUiState(
    val rawInputText: String = CHENNAI_PRESETS[0].rawText,
    val selectedPresetIndex: Int = 0,
    val parsedBooking: ParsedBooking? = null,
    val lastResultRecord: DetectionRecord? = null,
    val isSimulating: Boolean = false,
    val manualDestLat: String = "",
    val manualDestLng: String = ""
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as PorterAssistantApp
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    val settings: StateFlow<UserSettings> = app.settingsRepository.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings(
                homeCoordinates = null,
                homeLabel = "Home Base",
                radiusKm = 20.0,
                bufferKm = 1.0,
                overlayEnabled = true,
                isMonitoringActive = false
            )
        )

    val allDetections: StateFlow<List<DetectionRecord>> = app.database.detectionDao()
        .getAllDetections()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val latestDetection: StateFlow<DetectionRecord?> = app.database.detectionDao()
        .getLatestDetection()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val isAccessibilityBound: StateFlow<Boolean> = PorterAccessibilityService.isServiceBound
    val diagnosticState: StateFlow<DiagnosticState> = PorterAccessibilityService.diagnosticState

    private val _historyFilter = MutableStateFlow<RangeClassification?>(null)
    val historyFilter: StateFlow<RangeClassification?> = _historyFilter.asStateFlow()

    private val _simulationState = MutableStateFlow(
        SimulationUiState(
            parsedBooking = BookingCardParser.parseFromRawText(CHENNAI_PRESETS[0].rawText)
        )
    )
    val simulationState: StateFlow<SimulationUiState> = _simulationState.asStateFlow()

    fun setHistoryFilter(classification: RangeClassification?) {
        _historyFilter.value = classification
    }

    fun toggleMonitoring(active: Boolean) {
        viewModelScope.launch {
            app.settingsRepository.setMonitoringActive(active)
        }
    }

    fun saveHomeLocation(lat: Double, lng: Double, label: String) {
        viewModelScope.launch {
            app.settingsRepository.saveHomeCoordinates(lat, lng, label)
        }
    }

    fun clearHomeLocation() {
        viewModelScope.launch {
            app.settingsRepository.clearHomeLocation()
        }
    }

    fun saveRadiusAndBuffer(radius: Double, buffer: Double) {
        viewModelScope.launch {
            app.settingsRepository.saveRadiusAndBuffer(radius, buffer)
        }
    }

    fun toggleOverlay(enabled: Boolean) {
        viewModelScope.launch {
            app.settingsRepository.setOverlayEnabled(enabled)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            app.database.detectionDao().clearAll()
        }
    }

    fun deleteHistoryRecord(id: Long) {
        viewModelScope.launch {
            app.database.detectionDao().deleteById(id)
        }
    }

    fun updateSimulationInput(text: String) {
        val parsed = BookingCardParser.parseFromRawText(text)
        _simulationState.value = _simulationState.value.copy(
            rawInputText = text,
            parsedBooking = parsed
        )
    }

    fun selectPreset(index: Int) {
        if (index in CHENNAI_PRESETS.indices) {
            val preset = CHENNAI_PRESETS[index]
            val parsed = BookingCardParser.parseFromRawText(preset.rawText)
            _simulationState.value = _simulationState.value.copy(
                selectedPresetIndex = index,
                rawInputText = preset.rawText,
                parsedBooking = parsed,
                manualDestLat = preset.knownCoords?.latitude?.toString() ?: "",
                manualDestLng = preset.knownCoords?.longitude?.toString() ?: ""
            )
        }
    }

    fun setManualCoordinates(lat: String, lng: String) {
        _simulationState.value = _simulationState.value.copy(
            manualDestLat = lat,
            manualDestLng = lng
        )
    }

    fun runSimulationTest() {
        val current = _simulationState.value
        val parsed = current.parsedBooking ?: BookingCardParser.parseFromRawText(current.rawInputText)

        if (parsed == null) return

        _simulationState.value = current.copy(isSimulating = true)

        viewModelScope.launch {
            val override = Coordinates.fromStringOrNull(current.manualDestLat, current.manualDestLng)
            val result = DetectionCoordinator.processParsedBooking(
                context = app.applicationContext,
                booking = parsed,
                overrideCoordinates = override
            )
            _simulationState.value = _simulationState.value.copy(
                isSimulating = false,
                lastResultRecord = result
            )
        }
    }

    fun testDismissOverlay() {
        DetectionCoordinator.getOverlayManager(app.applicationContext).dismiss()
    }

    @SuppressLint("MissingPermission")
    fun fetchCurrentGpsLocation(
        onSuccess: (Coordinates) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        onSuccess(Coordinates(loc.latitude, loc.longitude))
                    } else {
                        // Fallback to last location
                        fusedLocationClient.lastLocation.addOnSuccessListener { last ->
                            if (last != null) {
                                onSuccess(Coordinates(last.latitude, last.longitude))
                            } else {
                                onError("Could not obtain GPS lock. Please ensure Location services are active.")
                            }
                        }.addOnFailureListener {
                            onError("Failed to fetch location: ${it.message}")
                        }
                    }
                }
                .addOnFailureListener {
                    onError("Failed to obtain device location: ${it.message}")
                }
        } catch (e: Exception) {
            onError("GPS permission or service exception: ${e.message}")
        }
    }

    fun canDrawOverlays(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(app.applicationContext)
        } else {
            true
        }
    }
}
