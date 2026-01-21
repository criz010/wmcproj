package com.example.trackerapp.core.constants

/**
 * Application-wide constants
 */
object AppConstants {

    /**
     * Location-related constants
     */
    object Location {
        // Location update intervals
        const val UPDATE_INTERVAL_MS = 5000L // 5 seconds
        const val FASTEST_UPDATE_INTERVAL_MS = 2000L // 2 seconds
        const val MAX_WAIT_TIME_MS = 10000L // 10 seconds

        // Location timeout
        const val LOCATION_REQUEST_TIMEOUT_MS = 10000L // 10 seconds

        // Accuracy thresholds (in meters)
        const val ACCURACY_EXCELLENT = 5f
        const val ACCURACY_VERY_GOOD = 15f
        const val ACCURACY_GOOD = 30f
        const val ACCURACY_FAIR = 50f

        // Minimum displacement for updates (in meters)
        const val MIN_DISPLACEMENT = 10f

        // Default location (Airbus Headquarters, Toulouse, France)
        const val DEFAULT_LATITUDE = 43.6047
        const val DEFAULT_LONGITUDE = 1.4442
    }

    /**
     * Map-related constants
     */
    object Map {
        // Map dimensions (Web Mercator)
        const val MAP_SIZE = 262144 // 2^18 pixels
        const val ZOOM_LEVELS = 18

        // Zoom limits
        const val MIN_SCALE = 0.01 // 1%
        const val MAX_SCALE = 2.0 // 200%
        const val DEFAULT_SCALE = 0.05 // 5%
        const val LOCATION_ZOOM_SCALE = 0.5 // 50%

        // Zoom step
        const val ZOOM_STEP = 1.5f

        // OpenStreetMap tile URL
        const val OSM_TILE_URL = "https://tile.openstreetmap.org"
        const val USER_AGENT = "TrackerApp/1.0"

        // Attribution
        const val OSM_ATTRIBUTION = "© OpenStreetMap contributors"
        const val OSM_COPYRIGHT_URL = "https://www.openstreetmap.org/copyright"
    }

    /**
     * Animation durations (in milliseconds)
     */
    object Animation {
        const val FAST = 150
        const val NORMAL = 300
        const val SLOW = 500
        const val VERY_SLOW = 1000
    }

    /**
     * UI constants
     */
    object UI {
        // Bottom sheet peek height
        const val BOTTOM_SHEET_PEEK_HEIGHT_DP = 120

        // Fab sizes
        const val FAB_SIZE_SMALL_DP = 40
        const val FAB_SIZE_MEDIUM_DP = 56
        const val FAB_SIZE_LARGE_DP = 96

        // Icon sizes
        const val ICON_SIZE_SMALL_DP = 16
        const val ICON_SIZE_MEDIUM_DP = 24
        const val ICON_SIZE_LARGE_DP = 32
        const val ICON_SIZE_XLARGE_DP = 48
    }

    /**
     * App metadata
     */
    object App {
        const val NAME = "TrackerApp"
        const val VERSION = "1.0.0"
    }

    /**
     * Navigation routes
     */
    object Routes {
        const val LOCATION = "location"
        const val MAP = "map"
        const val TRACKER = "tracker"
        const val LIST = "list"
        const val PATH = "path"
    }

    /**
     * Shared preferences keys
     */
    object Preferences {
        const val PREFS_NAME = "tracker_app_prefs"
        const val KEY_COORDINATE_FORMAT = "coordinate_format"
        const val KEY_DARK_MODE = "dark_mode"
        const val KEY_DYNAMIC_COLOR = "dynamic_color"
    }
}
