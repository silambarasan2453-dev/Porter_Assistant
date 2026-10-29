package com.example

import com.example.calc.RangeClassifier
import com.example.model.RangeClassification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RangeClassifierTest {

    private val defaultRadius = 20.0
    private val defaultBuffer = 1.0

    @Test
    fun testWithinRangeBoundary() {
        // Lower bound = 20 - 1 = 19.0 km
        // Strictly less than 19.0 is WITHIN RANGE
        assertEquals(
            RangeClassification.WITHIN_RANGE,
            RangeClassifier.classify(18.0, defaultRadius, defaultBuffer)
        )
        assertEquals(
            RangeClassification.WITHIN_RANGE,
            RangeClassifier.classify(18.99, defaultRadius, defaultBuffer)
        )
    }

    @Test
    fun testReviewBufferBoundaries() {
        // [19.0, 21.0] is REVIEW
        assertEquals(
            RangeClassification.REVIEW,
            RangeClassifier.classify(19.0, defaultRadius, defaultBuffer)
        )
        assertEquals(
            RangeClassification.REVIEW,
            RangeClassifier.classify(20.0, defaultRadius, defaultBuffer)
        )
        assertEquals(
            RangeClassification.REVIEW,
            RangeClassifier.classify(21.0, defaultRadius, defaultBuffer)
        )
    }

    @Test
    fun testOutsideRangeBoundary() {
        // Strictly greater than 21.0 is OUTSIDE RANGE
        // Resolves the specification conflict: 21.5 km and 22 km must be OUTSIDE RANGE
        assertEquals(
            RangeClassification.OUTSIDE_RANGE,
            RangeClassifier.classify(21.001, defaultRadius, defaultBuffer)
        )
        assertEquals(
            RangeClassification.OUTSIDE_RANGE,
            RangeClassifier.classify(21.5, defaultRadius, defaultBuffer)
        )
        assertEquals(
            RangeClassification.OUTSIDE_RANGE,
            RangeClassifier.classify(22.0, defaultRadius, defaultBuffer)
        )
        assertEquals(
            RangeClassification.OUTSIDE_RANGE,
            RangeClassifier.classify(35.0, defaultRadius, defaultBuffer)
        )
    }

    @Test
    fun testUnknownConditions() {
        assertEquals(
            RangeClassification.UNKNOWN,
            RangeClassifier.classify(null, defaultRadius, defaultBuffer)
        )
        assertEquals(
            RangeClassification.UNKNOWN,
            RangeClassifier.classify(Double.NaN, defaultRadius, defaultBuffer)
        )
        assertEquals(
            RangeClassification.UNKNOWN,
            RangeClassifier.classify(-5.0, defaultRadius, defaultBuffer)
        )
    }

    @Test
    fun testExplanationContainsNumbers() {
        val explainWithin = RangeClassifier.explain(15.0, defaultRadius, defaultBuffer)
        assertTrue(explainWithin.contains("15.0"))
        assertTrue(explainWithin.contains("19.0"))

        val explainOutside = RangeClassifier.explain(25.0, defaultRadius, defaultBuffer)
        assertTrue(explainOutside.contains("25.0"))
        assertTrue(explainOutside.contains("21.0"))
    }
}
