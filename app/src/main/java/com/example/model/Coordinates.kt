package com.example.model

data class Coordinates(
    val latitude: Double,
    val longitude: Double
) {
    init {
        require(isValid(latitude, longitude)) {
            "Invalid coordinates: latitude must be between -90 and 90, longitude between -180 and 180"
        }
    }

    companion object {
        fun isValid(lat: Double, lng: Double): Boolean {
            return !lat.isNaN() && !lng.isNaN() &&
                    lat in -90.0..90.0 &&
                    lng in -180.0..180.0
        }

        fun fromStringOrNull(latStr: String, lngStr: String): Coordinates? {
            val lat = latStr.trim().toDoubleOrNull() ?: return null
            val lng = lngStr.trim().toDoubleOrNull() ?: return null
            return if (isValid(lat, lng)) Coordinates(lat, lng) else null
        }
    }
}
