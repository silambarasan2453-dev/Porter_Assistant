package com.example.calc

import com.example.model.Coordinates
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object HaversineCalculator {

    private const val EARTH_RADIUS_KM = 6371.0

    /**
     * Calculates the straight-line great-circle distance in kilometers between two sets of WGS-84 coordinates.
     * Note: This calculates straight-line distance, NOT road driving distance.
     */
    fun calculateDistanceKm(from: Coordinates, to: Coordinates): Double {
        val lat1Rad = Math.toRadians(from.latitude)
        val lat2Rad = Math.toRadians(to.latitude)
        val dLatRad = Math.toRadians(to.latitude - from.latitude)
        val dLonRad = Math.toRadians(to.longitude - from.longitude)

        val a = sin(dLatRad / 2) * sin(dLatRad / 2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(dLonRad / 2) * sin(dLonRad / 2)

        val clampedA = a.coerceIn(0.0, 1.0)
        val c = 2 * atan2(sqrt(clampedA), sqrt(1.0 - clampedA))

        return EARTH_RADIUS_KM * c
    }

    /**
     * Calculates distance safely with validation, returning null if any coordinate is invalid.
     */
    fun calculateDistanceOrNull(fromLat: Double?, fromLng: Double?, toLat: Double?, toLng: Double?): Double? {
        if (fromLat == null || fromLng == null || toLat == null || toLng == null) return null
        if (!Coordinates.isValid(fromLat, fromLng) || !Coordinates.isValid(toLat, toLng)) return null
        return calculateDistanceKm(Coordinates(fromLat, fromLng), Coordinates(toLat, toLng))
    }
}
