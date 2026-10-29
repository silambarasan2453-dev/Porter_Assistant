package com.example

import com.example.parser.BookingCardParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BookingCardParserTest {

    @Test
    fun testParseScreenshotBookingCard() {
        val rawScreenshotText = """
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
        """.trimIndent()

        val parsed = BookingCardParser.parseFromRawText(rawScreenshotText)
        assertNotNull(parsed)
        parsed!!

        // Check pickup extraction
        assertEquals("ponniyammanmedu", parsed.pickupArea)
        assertTrue(parsed.pickupFullAddress.contains("Muthamizh Nagar"))

        // Check destination extraction
        assertEquals("Kolathur", parsed.destinationArea)
        assertTrue(parsed.destinationFullAddress.contains("Sri Nagar, Kolathur"))

        // Verify fare was captured but excluded from location addresses
        assertEquals("₹137", parsed.porterFare)
        assertFalse(parsed.destinationArea.contains("₹"))
        assertFalse(parsed.destinationFullAddress.contains("Accept in"))

        // Porter displayed route distance (10.7 km) should NOT contaminate destination area
        assertFalse(parsed.destinationArea.contains("10.7"))

        // Searchable destination query should be clean
        assertTrue(parsed.bestSearchableDestination.contains("Kolathur"))
        assertTrue(parsed.confidence >= 0.7f)
    }

    @Test
    fun testAmbiguousOrMissingDestination() {
        val emptyText = """
            ₹50
            Accept in 10s
            Skip X
        """.trimIndent()
        val parsed = BookingCardParser.parseFromRawText(emptyText)
        assertNull(parsed)
    }

    @Test
    fun testIdenticalPickupAndDropFlaggedAsAmbiguous() {
        val duplicateText = """
            PICKUP
            Kolathur
            DROP
            Kolathur
        """.trimIndent()
        val parsed = BookingCardParser.parseFromRawText(duplicateText)
        assertNotNull(parsed)
        assertTrue("Identical pickup and drop should be marked ambiguous", parsed!!.isAmbiguous)
    }

    @Test
    fun testCleanAddressHelper() {
        val raw = "Kolathur (10.7 km)..."
        val cleaned = com.example.parser.ParsedBooking.cleanAddressForGeocoding(raw)
        assertEquals("Kolathur", cleaned)
    }
}
