package com.example.trackerapp.domain.model

/**
 * Domain model representing a geographical location
 *
 * This is an immutable data class representing the core business entity
 * for location information in the app.
 *
 * @property latitude Latitude in decimal degrees (-90 to 90)
 * @property longitude Longitude in decimal degrees (-180 to 180)
 * @property accuracy Estimated horizontal accuracy in meters, null if unavailable
 * @property altitude Altitude in meters above WGS84 datum, null if unavailable
 * @property bearing Bearing in degrees (0-360), null if unavailable
 * @property speed Speed in meters/second, null if unavailable
 * @property timestamp Time this location was acquired (milliseconds since epoch)
 */
data class Location(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null,
    val altitude: Double? = null,
    val bearing: Float? = null,
    val speed: Float? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Validates if the coordinates are within valid ranges
     */
    fun isValid(): Boolean {
        return latitude in -90.0..90.0 && longitude in -180.0..180.0
    }

    /**
     * Returns signal quality level based on accuracy
     * 0 = No signal/poor, 1 = Fair, 2 = Good, 3 = Very Good, 4 = Excellent
     */
    fun getSignalQuality(): Int {
        return when {
            accuracy == null -> 0
            accuracy <= 5f -> 4 // Excellent
            accuracy <= 15f -> 3 // Very Good
            accuracy <= 30f -> 2 // Good
            accuracy <= 50f -> 1 // Fair
            else -> 0 // Poor
        }
    }

    /**
     * Checks if this location has acceptable accuracy (< 100m)
     */
    fun hasAcceptableAccuracy(): Boolean {
        return accuracy != null && accuracy < 100f
    }

    /**
     * Checks if this location has good accuracy (< 50m)
     */
    fun hasGoodAccuracy(): Boolean {
        return accuracy != null && accuracy < 50f
    }

    companion object {
        /**
         * Creates a Location with default/unknown values
         */
        fun unknown() = Location(
            latitude = 0.0,
            longitude = 0.0,
            accuracy = null,
            timestamp = 0L
        )
    }
}
