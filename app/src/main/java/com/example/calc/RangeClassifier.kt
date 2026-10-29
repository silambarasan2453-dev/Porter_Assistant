package com.example.calc

import com.example.model.RangeClassification

object RangeClassifier {

    data class Thresholds(
        val radiusKm: Double,
        val bufferKm: Double,
        val withinRangeMax: Double,
        val reviewMin: Double,
        val reviewMax: Double,
        val outsideRangeMin: Double
    ) {
        fun formatSummary(): String {
            return "< %.1f km = WITHIN RANGE | %.1f to %.1f km = REVIEW | > %.1f km = OUTSIDE RANGE"
                .format(withinRangeMax, reviewMin, reviewMax, outsideRangeMin)
        }
    }

    fun getThresholds(radiusKm: Double, bufferKm: Double): Thresholds {
        val safeRadius = radiusKm.coerceAtLeast(1.0)
        val safeBuffer = bufferKm.coerceAtLeast(0.0)
        val lower = safeRadius - safeBuffer
        val upper = safeRadius + safeBuffer
        return Thresholds(
            radiusKm = safeRadius,
            bufferKm = safeBuffer,
            withinRangeMax = lower,
            reviewMin = lower,
            reviewMax = upper,
            outsideRangeMin = upper
        )
    }

    /**
     * Classifies a straight-line distance according to configured radius and buffer:
     * - WITHIN RANGE: distance < radius - buffer
     * - REVIEW: distance in [radius - buffer, radius + buffer]
     * - OUTSIDE RANGE: distance > radius + buffer
     * - UNKNOWN: missing, negative, NaN or unresolved distance
     */
    fun classify(distanceKm: Double?, radiusKm: Double, bufferKm: Double): RangeClassification {
        if (distanceKm == null || distanceKm.isNaN() || distanceKm < 0.0) {
            return RangeClassification.UNKNOWN
        }

        val thresholds = getThresholds(radiusKm, bufferKm)

        return when {
            distanceKm < thresholds.reviewMin -> RangeClassification.WITHIN_RANGE
            distanceKm <= thresholds.reviewMax -> RangeClassification.REVIEW
            else -> RangeClassification.OUTSIDE_RANGE
        }
    }

    /**
     * Returns a human-readable explanation of why this classification occurred.
     */
    fun explain(distanceKm: Double?, radiusKm: Double, bufferKm: Double): String {
        if (distanceKm == null || distanceKm.isNaN() || distanceKm < 0.0) {
            return "Classification is UNKNOWN because valid destination coordinates were not resolved."
        }
        val thresholds = getThresholds(radiusKm, bufferKm)
        return when (classify(distanceKm, radiusKm, bufferKm)) {
            RangeClassification.WITHIN_RANGE ->
                "%.1f km is strictly less than %.1f km (radius %.1f km - buffer %.1f km)"
                    .format(distanceKm, thresholds.withinRangeMax, radiusKm, bufferKm)
            RangeClassification.REVIEW ->
                "%.1f km is within the review buffer range of %.1f km to %.1f km (radius %.1f km ± buffer %.1f km)"
                    .format(distanceKm, thresholds.reviewMin, thresholds.reviewMax, radiusKm, bufferKm)
            RangeClassification.OUTSIDE_RANGE ->
                "%.1f km exceeds the outer boundary of %.1f km (radius %.1f km + buffer %.1f km)"
                    .format(distanceKm, thresholds.outsideRangeMin, radiusKm, bufferKm)
            RangeClassification.UNKNOWN ->
                "Invalid or unresolved distance values."
        }
    }
}
