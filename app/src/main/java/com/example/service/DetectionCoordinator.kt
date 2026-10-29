package com.example.service

import android.content.Context
import com.example.PorterAssistantApp
import com.example.calc.HaversineCalculator
import com.example.calc.RangeClassifier
import com.example.data.local.DetectionRecord
import com.example.model.Coordinates
import com.example.model.RangeClassification
import com.example.parser.ParsedBooking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object DetectionCoordinator {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var overlayManager: OverlayManager? = null

    fun getOverlayManager(context: Context): OverlayManager {
        if (overlayManager == null) {
            overlayManager = OverlayManager(context.applicationContext)
        }
        return overlayManager!!
    }

    suspend fun processParsedBooking(
        context: Context,
        booking: ParsedBooking,
        overrideCoordinates: Coordinates? = null
    ): DetectionRecord = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val app = PorterAssistantApp.instance
        val settings = app.settingsRepository.settingsFlow.first()

        val destinationQuery = booking.bestSearchableDestination
        val pickupQuery = booking.bestSearchablePickup

        // Resolve coordinates
        val (destCoords, geocodingStatus, failureReason) = if (overrideCoordinates != null) {
            Triple(overrideCoordinates, "MANUAL_TEST_OVERRIDE", null)
        } else if (destinationQuery.isNotBlank()) {
            val res = app.geocodingEngine.resolveDestination(destinationQuery)
            Triple(res.coordinates, res.source, res.error)
        } else {
            Triple(null, "NO_DESTINATION_TEXT", "No valid destination found in card")
        }

        // Calculate distance if both Home and Destination coordinates are present
        val distanceKm = if (settings.homeCoordinates != null && destCoords != null) {
            HaversineCalculator.calculateDistanceKm(settings.homeCoordinates, destCoords)
        } else {
            null
        }

        // Classify
        val classification = if (settings.homeCoordinates == null) {
            RangeClassification.UNKNOWN
        } else {
            RangeClassifier.classify(distanceKm, settings.radiusKm, settings.bufferKm)
        }

        val totalDurationMs = System.currentTimeMillis() - startTime
        val effectiveFailure = if (settings.homeCoordinates == null) {
            "Home coordinates not configured"
        } else {
            failureReason
        }

        val record = DetectionRecord(
            timestamp = System.currentTimeMillis(),
            pickupText = if (pickupQuery.isNotBlank()) pickupQuery else booking.pickupArea,
            destinationText = if (destinationQuery.isNotBlank()) destinationQuery else booking.destinationArea,
            destinationLat = destCoords?.latitude,
            destinationLng = destCoords?.longitude,
            distanceKm = distanceKm,
            classification = classification,
            extractionConfidence = booking.confidence,
            geocodingStatus = geocodingStatus,
            processingDurationMs = totalDurationMs,
            failureReason = effectiveFailure
        )

        // Store into Room
        app.database.detectionDao().insert(record)

        // Show floating overlay if enabled and allowed
        if (settings.overlayEnabled) {
            val explanation = RangeClassifier.explain(distanceKm, settings.radiusKm, settings.bufferKm)
            val displayData = OverlayDisplayData(
                destination = record.destinationText,
                distanceKm = record.distanceKm,
                classification = record.classification,
                explanation = explanation,
                processingDurationMs = totalDurationMs
            )
            getOverlayManager(context).showOverlay(displayData) {
                scope.launch {
                    app.settingsRepository.setMonitoringActive(false)
                }
            }
        }

        return@withContext record
    }
}
