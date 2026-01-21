package com.example.trackerapp.core.utils

import kotlin.math.abs

/**
 * Coordinate formatting utilities for different display formats
 */
object CoordinateFormatter {

    /**
     * Coordinate format types
     */
    enum class CoordinateFormat {
        DECIMAL_DEGREES,    // 52.520008, 13.404954
        DEGREES_MINUTES,    // 52° 31.200' N, 13° 24.297' E
        DEGREES_MINUTES_SECONDS // 52° 31' 12.0" N, 13° 24' 17.8" E
    }

    /**
     * Formats coordinates in Decimal Degrees (DD) format
     * Example: 52.520008° N, 13.404954° E
     */
    fun formatDecimalDegrees(lat: Double, lon: Double, precision: Int = 6): String {
        val latDir = if (lat >= 0) "N" else "S"
        val lonDir = if (lon >= 0) "E" else "W"
        val latStr = "%.${precision}f".format(abs(lat))
        val lonStr = "%.${precision}f".format(abs(lon))
        return "$latStr° $latDir, $lonStr° $lonDir"
    }

    /**
     * Formats coordinates in Degrees Decimal Minutes (DDM) format
     * Example: 52° 31.200' N, 13° 24.297' E
     */
    fun formatDegreesMinutes(lat: Double, lon: Double): String {
        val latDDM = toDDM(lat)
        val lonDDM = toDDM(lon)
        val latDir = if (lat >= 0) "N" else "S"
        val lonDir = if (lon >= 0) "E" else "W"
        return "${latDDM.degrees}° ${latDDM.minutes}' $latDir, ${lonDDM.degrees}° ${lonDDM.minutes}' $lonDir"
    }

    /**
     * Formats coordinates in Degrees Minutes Seconds (DMS) format
     * Example: 52° 31' 12.0" N, 13° 24' 17.8" E
     */
    fun formatDegreesMinutesSeconds(lat: Double, lon: Double): String {
        val latDMS = toDMS(lat)
        val lonDMS = toDMS(lon)
        val latDir = if (lat >= 0) "N" else "S"
        val lonDir = if (lon >= 0) "E" else "W"
        return "${latDMS.degrees}° ${latDMS.minutes}' ${latDMS.seconds}\" $latDir, ${lonDMS.degrees}° ${lonDMS.minutes}' ${lonDMS.seconds}\" $lonDir"
    }

    /**
     * Formats a single coordinate value based on the specified format
     */
    fun format(lat: Double, lon: Double, format: CoordinateFormat): String {
        return when (format) {
            CoordinateFormat.DECIMAL_DEGREES -> formatDecimalDegrees(lat, lon)
            CoordinateFormat.DEGREES_MINUTES -> formatDegreesMinutes(lat, lon)
            CoordinateFormat.DEGREES_MINUTES_SECONDS -> formatDegreesMinutesSeconds(lat, lon)
        }
    }

    /**
     * Formats latitude only
     */
    fun formatLatitude(lat: Double, format: CoordinateFormat = CoordinateFormat.DECIMAL_DEGREES): String {
        val dir = if (lat >= 0) "N" else "S"
        return when (format) {
            CoordinateFormat.DECIMAL_DEGREES -> "${"%.6f".format(abs(lat))}° $dir"
            CoordinateFormat.DEGREES_MINUTES -> {
                val ddm = toDDM(lat)
                "${ddm.degrees}° ${ddm.minutes}' $dir"
            }
            CoordinateFormat.DEGREES_MINUTES_SECONDS -> {
                val dms = toDMS(lat)
                "${dms.degrees}° ${dms.minutes}' ${dms.seconds}\" $dir"
            }
        }
    }

    /**
     * Formats longitude only
     */
    fun formatLongitude(lon: Double, format: CoordinateFormat = CoordinateFormat.DECIMAL_DEGREES): String {
        val dir = if (lon >= 0) "E" else "W"
        return when (format) {
            CoordinateFormat.DECIMAL_DEGREES -> "${"%.6f".format(abs(lon))}° $dir"
            CoordinateFormat.DEGREES_MINUTES -> {
                val ddm = toDDM(lon)
                "${ddm.degrees}° ${ddm.minutes}' $dir"
            }
            CoordinateFormat.DEGREES_MINUTES_SECONDS -> {
                val dms = toDMS(lon)
                "${dms.degrees}° ${dms.minutes}' ${dms.seconds}\" $dir"
            }
        }
    }

    // Helper data classes
    private data class DDM(val degrees: Int, val minutes: String)
    private data class DMS(val degrees: Int, val minutes: Int, val seconds: String)

    // Conversion helpers
    private fun toDDM(decimal: Double): DDM {
        val absDecimal = abs(decimal)
        val degrees = absDecimal.toInt()
        val minutes = (absDecimal - degrees) * 60
        return DDM(degrees, "%.3f".format(minutes))
    }

    private fun toDMS(decimal: Double): DMS {
        val absDecimal = abs(decimal)
        val degrees = absDecimal.toInt()
        val minutesDecimal = (absDecimal - degrees) * 60
        val minutes = minutesDecimal.toInt()
        val seconds = (minutesDecimal - minutes) * 60
        return DMS(degrees, minutes, "%.1f".format(seconds))
    }
}

/**
 * Extension functions for easy coordinate formatting
 */
fun Pair<Double, Double>.formatCoordinates(
    format: CoordinateFormatter.CoordinateFormat = CoordinateFormatter.CoordinateFormat.DECIMAL_DEGREES
): String {
    return CoordinateFormatter.format(first, second, format)
}
