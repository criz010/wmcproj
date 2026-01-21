package com.example.trackerapp.presentation.map

import com.example.trackerapp.domain.model.Location

/**
 * UI state for MapScreen
 *
 * Represents the state of the map screen including location, zoom level, and map settings.
 *
 * @property location The current location to display on the map, null if unavailable
 * @property zoomScale Current zoom scale (0.01 to 2.0)
 * @property isLoading Whether the map is loading
 * @property error Error message if any
 * @property showAttribution Whether to show map attribution
 */
data class MapUiState(
    val location: Location? = null,
    val zoomScale: Double = 0.05, // 5% default zoom
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAttribution: Boolean = true
) {
    /**
     * Returns formatted zoom percentage
     */
    fun getZoomPercentage(): Int {
        return (zoomScale * 100).toInt()
    }

    /**
     * Checks if location is available
     */
    fun hasLocation(): Boolean {
        return location != null && location.isValid()
    }

    /**
     * Creates a copy with new zoom scale
     */
    fun withZoom(newScale: Double): MapUiState {
        return copy(zoomScale = newScale.coerceIn(MIN_SCALE, MAX_SCALE))
    }

    /**
     * Creates a copy with new location
     */
    fun withLocation(newLocation: Location?): MapUiState {
        return copy(location = newLocation)
    }

    companion object {
        const val MIN_SCALE = 0.01 // 1%
        const val MAX_SCALE = 2.0  // 200%
        const val DEFAULT_SCALE = 0.05 // 5%
        const val LOCATION_ZOOM_SCALE = 0.5 // 50% when focusing on location
    }
}
