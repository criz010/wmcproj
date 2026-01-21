package com.example.trackerapp.core.utils

import android.location.Location
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Extension functions for Location handling
 */

/**
 * Calculates distance between this location and another location in meters
 */
fun Location.distanceTo(other: Location): Float {
    val results = FloatArray(1)
    Location.distanceBetween(
        this.latitude,
        this.longitude,
        other.latitude,
        other.longitude,
        results
    )
    return results[0]
}

/**
 * Calculates bearing from this location to another location in degrees
 */
fun Location.bearingTo(other: Location): Float {
    val results = FloatArray(2)
    Location.distanceBetween(
        this.latitude,
        this.longitude,
        other.latitude,
        other.longitude,
        results
    )
    return results[1]
}

/**
 * Checks if location has good accuracy (< 50 meters)
 */
fun Location.hasGoodAccuracy(): Boolean = hasAccuracy() && accuracy < 50f

/**
 * Checks if location has acceptable accuracy (< 100 meters)
 */
fun Location.hasAcceptableAccuracy(): Boolean = hasAccuracy() && accuracy < 100f

/**
 * Gets GPS signal quality level (0-4)
 * 0 = No signal/poor
 * 1 = Fair
 * 2 = Good
 * 3 = Very Good
 * 4 = Excellent
 */
fun Location.getSignalQuality(): Int {
    if (!hasAccuracy()) return 0
    return when {
        accuracy <= 5f -> 4 // Excellent
        accuracy <= 15f -> 3 // Very Good
        accuracy <= 30f -> 2 // Good
        accuracy <= 50f -> 1 // Fair
        else -> 0 // Poor
    }
}

/**
 * Converts Location to domain model
 */
fun Location.toDomainModel(): com.example.trackerapp.domain.model.Location {
    return com.example.trackerapp.domain.model.Location(
        latitude = latitude,
        longitude = longitude,
        accuracy = if (hasAccuracy()) accuracy else null,
        altitude = if (hasAltitude()) altitude else null,
        bearing = if (hasBearing()) bearing else null,
        speed = if (hasSpeed()) speed else null,
        timestamp = time
    )
}

/**
 * Mercator projection data class
 */
data class MercatorPoint(val x: Double, val y: Double)

/**
 * Converts latitude/longitude to Web Mercator coordinates
 *
 * @param mapSize The size of the map in pixels (typically 2^zoom * 256)
 * @return MercatorPoint with x,y coordinates in map pixel space
 */
fun latLonToMercator(lat: Double, lon: Double, mapSize: Int): MercatorPoint {
    val x = (lon + 180.0) / 360.0 * mapSize
    val latRad = Math.toRadians(lat)
    val mercatorN = ln(tan(Math.PI / 4 + latRad / 2))
    val y = (1.0 - mercatorN / Math.PI) / 2.0 * mapSize
    return MercatorPoint(x, y)
}

/**
 * Calculates zoom level based on accuracy to fit the uncertainty circle
 */
fun calculateZoomForAccuracy(accuracy: Float, mapWidth: Int = 1000): Double {
    // Each zoom level doubles the resolution
    // We want the accuracy circle to be ~1/4 of the screen
    val metersPerPixel = accuracy * 4 / mapWidth
    // At zoom level 0, the entire earth (40075km) fits in 256 pixels
    val metersPerPixelAtZoom0 = 40075000.0 / 256.0
    val zoomLevel = ln(metersPerPixelAtZoom0 / metersPerPixel) / ln(2.0)
    return zoomLevel.coerceIn(1.0, 20.0)
}
