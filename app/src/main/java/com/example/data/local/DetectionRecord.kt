package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.RangeClassification

@Entity(tableName = "detection_records")
data class DetectionRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val pickupText: String,
    val destinationText: String,
    val destinationLat: Double?,
    val destinationLng: Double?,
    val distanceKm: Double?,
    val classification: RangeClassification,
    val extractionConfidence: Float,
    val geocodingStatus: String,
    val processingDurationMs: Long,
    val failureReason: String? = null
)
