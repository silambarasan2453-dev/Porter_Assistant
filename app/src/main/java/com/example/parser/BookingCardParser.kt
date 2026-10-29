package com.example.parser

import android.view.accessibility.AccessibilityNodeInfo

data class ParsedBooking(
    val pickupArea: String,
    val pickupFullAddress: String,
    val destinationArea: String,
    val destinationFullAddress: String,
    val porterTripDistanceKm: Double?,
    val porterFare: String?,
    val confidence: Float,
    val isAmbiguous: Boolean,
    val rawText: String
) {
    val bestSearchableDestination: String
        get() {
            val full = destinationFullAddress.trim()
            val area = destinationArea.trim()
            val base = when {
                full.isNotBlank() && full.length > 5 -> full
                area.isNotBlank() -> area
                else -> ""
            }
            return cleanAddressForGeocoding(base)
        }

    val bestSearchablePickup: String
        get() {
            val full = pickupFullAddress.trim()
            val area = pickupArea.trim()
            val base = when {
                full.isNotBlank() && full.length > 5 -> full
                area.isNotBlank() -> area
                else -> ""
            }
            return cleanAddressForGeocoding(base)
        }

    companion object {
        fun cleanAddressForGeocoding(address: String): String {
            // Remove trailing ellipsis and duplicate street chunks
            var cleaned = address.replace(Regex("[.,…\\s]+$"), "").trim()
            // Remove parenthetical distances like (10.7 km) or (0.5 km)
            cleaned = cleaned.replace(Regex("\\(\\s*\\d+(\\.\\d+)?\\s*km\\s*\\)", RegexOption.IGNORE_CASE), "").trim()
            return cleaned
        }
    }
}

object BookingCardParser {

    private val FARE_REGEX = Regex("₹\\s*[0-9]+(?:\\.[0-9]+)?")
    private val DISTANCE_PAREN_REGEX = Regex("\\(?\\s*([0-9]+(?:\\.[0-9]+)?)\\s*km\\s*\\)?", RegexOption.IGNORE_CASE)
    private val TIMER_REGEX = Regex("Accept\\s*(?:in)?\\s*(\\d+s?)?", RegexOption.IGNORE_CASE)
    private val SKIP_REGEX = Regex("Skip\\s*X?", RegexOption.IGNORE_CASE)
    private val RECHARGE_REGEX = Regex("Recharge|Ledger Balance|Noticeboard|GO OFFLINE", RegexOption.IGNORE_CASE)

    /**
     * Parses from an AccessibilityNodeInfo root window or card.
     */
    fun parseFromAccessibilityNode(rootNode: AccessibilityNodeInfo?): ParsedBooking? {
        if (rootNode == null) return null
        val texts = mutableListOf<String>()
        extractAllTexts(rootNode, texts)
        if (texts.isEmpty()) return null
        return parseFromTextLines(texts)
    }

    /**
     * Traverses the node tree collecting readable text values.
     */
    private fun extractAllTexts(node: AccessibilityNodeInfo, output: MutableList<String>) {
        node.text?.toString()?.trim()?.let {
            if (it.isNotBlank()) output.add(it)
        }
        node.contentDescription?.toString()?.trim()?.let {
            if (it.isNotBlank() && it !in output) output.add(it)
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            try {
                extractAllTexts(child, output)
            } finally {
                child.recycle()
            }
        }
    }

    /**
     * Parses raw text block (from accessibility node hierarchy or clipboard test paste).
     */
    fun parseFromRawText(raw: String): ParsedBooking? {
        val lines = raw.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
        return parseFromTextLines(lines)
    }

    /**
     * Core line-by-line parsing engine.
     */
    fun parseFromTextLines(lines: List<String>): ParsedBooking? {
        // Filter out non-address noise like fare balance or buttons
        val cleanedLines = lines.filterNot { line ->
            line.matches(Regex("^(Recharge|Ledger Balance|Noticeboard|GO OFFLINE|Please recharge.*)$", RegexOption.IGNORE_CASE))
        }

        var fare: String? = null
        for (line in cleanedLines) {
            val fareMatch = FARE_REGEX.find(line)
            if (fareMatch != null && !line.contains("-")) {
                fare = fareMatch.value
                break
            }
        }

        var pickupIndex = -1
        var dropIndex = -1

        for (i in cleanedLines.indices) {
            val line = cleanedLines[i]
            if (pickupIndex == -1 && (line.equals("PICKUP", ignoreCase = true) || line.contains("PICKUP", ignoreCase = true))) {
                pickupIndex = i
            } else if (dropIndex == -1 && (line.equals("DROP", ignoreCase = true) || line.contains("DROP", ignoreCase = true))) {
                dropIndex = i
            }
        }

        var pickupArea = ""
        var pickupAddress = ""
        var destinationArea = ""
        var destinationAddress = ""
        var dropKm: Double? = null

        if (pickupIndex != -1 && dropIndex != -1 && dropIndex > pickupIndex) {
            // Label-guided extraction
            val pickupLines = cleanedLines.subList(pickupIndex + 1, dropIndex)
                .filterNot { it.contains("PICKUP", ignoreCase = true) }
            val dropLines = cleanedLines.subList(dropIndex + 1, cleanedLines.size)
                .filterNot { isActionOrNoise(it) }

            if (pickupLines.isNotEmpty()) {
                pickupArea = sanitizeLocationLine(pickupLines[0])
                if (pickupLines.size > 1) {
                    pickupAddress = sanitizeLocationLine(pickupLines.subList(1, pickupLines.size).joinToString(", "))
                }
            }

            if (dropLines.isNotEmpty()) {
                destinationArea = sanitizeLocationLine(dropLines[0])
                // Extract distance if present in area header e.g. "Kolathur (10.7 km)"
                val distMatch = DISTANCE_PAREN_REGEX.find(dropLines[0])
                if (distMatch != null) {
                    dropKm = distMatch.groupValues[1].toDoubleOrNull()
                }

                if (dropLines.size > 1) {
                    destinationAddress = sanitizeLocationLine(dropLines.subList(1, dropLines.size).joinToString(", "))
                }
            }
        } else {
            // Heuristic extraction when explicit PICKUP/DROP labels are absent
            val candidateLines = cleanedLines.filterNot { isActionOrNoise(it) }
            if (candidateLines.size >= 2) {
                // Look for lines containing "(X km)"
                val distanceLines = candidateLines.filter { DISTANCE_PAREN_REGEX.containsMatchIn(it) }
                if (distanceLines.size >= 2) {
                    pickupArea = sanitizeLocationLine(distanceLines[0])
                    destinationArea = sanitizeLocationLine(distanceLines[1])
                    val distMatch = DISTANCE_PAREN_REGEX.find(distanceLines[1])
                    dropKm = distMatch?.groupValues?.get(1)?.toDoubleOrNull()
                } else {
                    pickupArea = sanitizeLocationLine(candidateLines[0])
                    destinationArea = sanitizeLocationLine(candidateLines[1])
                }
            }
        }

        // Clean parenthetical km from area titles
        val cleanDestArea = destinationArea.replace(DISTANCE_PAREN_REGEX, "").trim()
        val cleanPickupArea = pickupArea.replace(DISTANCE_PAREN_REGEX, "").trim()

        if (cleanDestArea.isBlank() && destinationAddress.isBlank()) {
            return null
        }

        // Evaluate extraction confidence
        var confidenceScore = 0.5f
        if (pickupIndex != -1 && dropIndex != -1) confidenceScore += 0.3f
        if (destinationAddress.length > 10) confidenceScore += 0.2f
        if (cleanDestArea.isNotBlank() && cleanDestArea.equals(cleanPickupArea, ignoreCase = true)) {
            // Pickup and drop are identical -> suspicious
            confidenceScore -= 0.4f
        }

        val isAmbiguous = destinationAddress.isBlank() ||
                destinationAddress.length < 5 ||
                cleanDestArea.equals(cleanPickupArea, ignoreCase = true)

        return ParsedBooking(
            pickupArea = cleanPickupArea,
            pickupFullAddress = pickupAddress,
            destinationArea = cleanDestArea,
            destinationFullAddress = destinationAddress,
            porterTripDistanceKm = dropKm,
            porterFare = fare,
            confidence = confidenceScore.coerceIn(0.1f, 1.0f),
            isAmbiguous = isAmbiguous,
            rawText = lines.joinToString("\n")
        )
    }

    private fun isActionOrNoise(line: String): Boolean {
        return TIMER_REGEX.containsMatchIn(line) ||
                SKIP_REGEX.containsMatchIn(line) ||
                RECHARGE_REGEX.containsMatchIn(line) ||
                line.startsWith("₹") ||
                line.startsWith("- ₹") ||
                line.contains("Ledger", ignoreCase = true)
    }

    private fun sanitizeLocationLine(line: String): String {
        return line.replace(Regex("^(PICKUP|DROP)\\s*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("[,\\s]+$"), "")
            .trim()
    }
}
