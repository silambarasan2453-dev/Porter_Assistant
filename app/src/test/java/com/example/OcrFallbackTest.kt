package com.example

import com.example.ocr.OcrFallbackModule
import com.example.parser.ParsedBooking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OcrFallbackTest {

    private val module = OcrFallbackModule()

    @Test
    fun testShouldTriggerFallbackWhenParsedIsNull() {
        assertTrue(module.shouldTriggerFallback(null))
    }

    @Test
    fun testShouldTriggerFallbackWhenDropIsBlank() {
        val blankDrop = ParsedBooking(
            pickupArea = "Chennai",
            pickupFullAddress = "",
            destinationArea = "",
            destinationFullAddress = "",
            porterTripDistanceKm = null,
            porterFare = null,
            confidence = 0.8f,
            isAmbiguous = true,
            rawText = ""
        )
        assertTrue(module.shouldTriggerFallback(blankDrop))
    }

    @Test
    fun testShouldNotTriggerFallbackWhenGoodAccessibilityExtraction() {
        val good = ParsedBooking(
            pickupArea = "Ponniyammanmedu",
            pickupFullAddress = "Muthamizh Nagar",
            destinationArea = "Kolathur",
            destinationFullAddress = "Sri Nagar, Kolathur, Chennai",
            porterTripDistanceKm = 10.7,
            porterFare = "₹137",
            confidence = 0.9f,
            isAmbiguous = false,
            rawText = "..."
        )
        assertFalse(module.shouldTriggerFallback(good))
    }

    @Test
    fun testParseOcrOutput() {
        val ocrSample = """
            ₹137
            PICKUP
            ponniyammanmedu (0.5 km)
            DROP
            Kolathur (10.7 km)
            Accept in 7s
        """.trimIndent()

        val parsed = module.parseOcrOutput(ocrSample)
        assertNotNull(parsed)
        assertTrue(parsed!!.destinationArea.contains("Kolathur"))
    }
}
