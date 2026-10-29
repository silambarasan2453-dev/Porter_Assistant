package com.example.model

enum class RangeClassification(
    val label: String,
    val description: String
) {
    WITHIN_RANGE(
        label = "WITHIN RANGE",
        description = "Destination is safely within your preferred driving radius."
    ),
    REVIEW(
        label = "REVIEW",
        description = "Destination is near your radius threshold. Exercise driver discretion."
    ),
    OUTSIDE_RANGE(
        label = "OUTSIDE RANGE",
        description = "Destination exceeds your configured travel radius plus buffer."
    ),
    UNKNOWN(
        label = "UNKNOWN",
        description = "Coordinates or address could not be reliably verified."
    )
}
