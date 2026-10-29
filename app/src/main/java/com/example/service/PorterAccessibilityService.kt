package com.example.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.example.PorterAssistantApp
import com.example.parser.BookingCardParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class DiagnosticState(
    val lastEventPackage: String? = null,
    val lastEventTimestamp: Long = 0,
    val nodesInspectedCount: Int = 0,
    val offersDetectedCount: Int = 0,
    val lastExtractedDrop: String? = null,
    val statusMessage: String = "Accessibility service ready"
)

class PorterAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var lastProcessedDestination: String? = null
    private var lastProcessedTimestamp: Long = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        _isServiceBound.value = true
        _diagnosticState.value = _diagnosticState.value.copy(
            statusMessage = "Connected and active"
        )
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val pkgName = event.packageName?.toString() ?: ""
        // Check if monitoring is active
        serviceScope.launch {
            try {
                val settings = PorterAssistantApp.instance.settingsRepository.settingsFlow.first()
                if (!settings.isMonitoringActive) {
                    return@launch
                }

                // If package is not Porter or test runner, skip
                val isPorterApp = pkgName.contains("porter", ignoreCase = true) ||
                        pkgName.contains("partner", ignoreCase = true) ||
                        pkgName == applicationContext.packageName

                if (!isPorterApp) return@launch

                val rootNode = rootInActiveWindow ?: return@launch
                val parsed = try {
                    BookingCardParser.parseFromAccessibilityNode(rootNode)
                } finally {
                    rootNode.recycle()
                }

                if (parsed != null && parsed.destinationArea.isNotBlank()) {
                    val now = System.currentTimeMillis()
                    // Deduplicate identical bookings within 4 seconds
                    if (parsed.destinationArea == lastProcessedDestination && (now - lastProcessedTimestamp) < 4000L) {
                        return@launch
                    }

                    lastProcessedDestination = parsed.destinationArea
                    lastProcessedTimestamp = now

                    _diagnosticState.value = _diagnosticState.value.copy(
                        lastEventPackage = pkgName,
                        lastEventTimestamp = now,
                        offersDetectedCount = _diagnosticState.value.offersDetectedCount + 1,
                        lastExtractedDrop = parsed.bestSearchableDestination,
                        statusMessage = "Booking detected: ${parsed.destinationArea}"
                    )

                    DetectionCoordinator.processParsedBooking(applicationContext, parsed)
                }
            } catch (e: Exception) {
                _diagnosticState.value = _diagnosticState.value.copy(
                    statusMessage = "Event error: ${e.message}"
                )
            }
        }
    }

    override fun onInterrupt() {
        _diagnosticState.value = _diagnosticState.value.copy(
            statusMessage = "Service interrupted"
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _isServiceBound.value = false
        serviceScope.cancel()
    }

    companion object {
        private val _isServiceBound = MutableStateFlow(false)
        val isServiceBound = _isServiceBound.asStateFlow()

        private val _diagnosticState = MutableStateFlow(DiagnosticState())
        val diagnosticState = _diagnosticState.asStateFlow()
    }
}
