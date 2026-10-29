package com.example

import com.example.calc.HaversineCalculator
import com.example.model.Coordinates
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HaversineCalculatorTest {

    @Test
    fun testSamePointDistanceIsZero() {
        val home = Coordinates(13.1242, 80.2081)
        val distance = HaversineCalculator.calculateDistanceKm(home, home)
        assertEquals(0.0, distance, 0.001)
    }

    @Test
    fun testKnownDistancePonniyammanmeduToKolathur() {
        // Ponniyammanmedu (13.1367, 80.2183) to Kolathur (13.1242, 80.2081) ~1.8 km
        val pickup = Coordinates(13.1367, 80.2183)
        val dest = Coordinates(13.1242, 80.2081)
        val distance = HaversineCalculator.calculateDistanceKm(pickup, dest)
        assertTrue("Distance should be approx 1.8km, was $distance", distance in 1.5..2.1)
    }

    @Test
    fun testKnownDistanceChennaiToTambaram() {
        // Kolathur (13.1242, 80.2081) to Tambaram (12.9249, 80.1000) ~25 km
        val home = Coordinates(13.1242, 80.2081)
        val farDrop = Coordinates(12.9249, 80.1000)
        val distance = HaversineCalculator.calculateDistanceKm(home, farDrop)
        assertTrue("Distance should be approx 25km, was $distance", distance in 24.0..26.5)
    }

    @Test
    fun testCoordinateValidation() {
        assertTrue(Coordinates.isValid(0.0, 0.0))
        assertTrue(Coordinates.isValid(90.0, 180.0))
        assertTrue(Coordinates.isValid(-90.0, -180.0))

        assertFalse(Coordinates.isValid(90.1, 0.0))
        assertFalse(Coordinates.isValid(-90.1, 0.0))
        assertFalse(Coordinates.isValid(0.0, 180.1))
        assertFalse(Coordinates.isValid(0.0, -180.1))
        assertFalse(Coordinates.isValid(Double.NaN, 0.0))
    }

    @Test
    fun testCalculateDistanceOrNullHandlesInvalidInputs() {
        assertNull(HaversineCalculator.calculateDistanceOrNull(null, 80.0, 13.0, 80.0))
        assertNull(HaversineCalculator.calculateDistanceOrNull(13.0, 80.0, null, 80.0))
        assertNull(HaversineCalculator.calculateDistanceOrNull(95.0, 80.0, 13.0, 80.0))
        assertNotNull(HaversineCalculator.calculateDistanceOrNull(13.0, 80.0, 13.1, 80.1))
    }
}
