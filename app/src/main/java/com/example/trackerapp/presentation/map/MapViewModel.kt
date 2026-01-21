package com.example.trackerapp.presentation.map

import androidx.lifecycle.ViewModel
import com.example.trackerapp.domain.model.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for MapScreen
 *
 * Manages map state including zoom level, location, and map settings.
 * Uses StateFlow for reactive state management.
 *
 * Architecture:
 * - Single source of truth: uiState StateFlow
 * - Immutable state updates via copy()
 * - Type-safe state management
 *
 * @property initialLocation Optional initial location to display
 */
class MapViewModel(
    initialLocation: Location? = null
) : ViewModel() {

    // Backing property for mutable state
    private val _uiState = MutableStateFlow(
        MapUiState(location = initialLocation)
    )

    /**
     * Immutable UI state exposed to the UI layer
     */
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    /**
     * Updates the location to display on the map
     *
     * @param location The new location
     */
    fun updateLocation(location: Location?) {
        _uiState.update { it.withLocation(location) }
    }

    /**
     * Zooms in on the map
     *
     * @param step Zoom multiplier (default 1.5x)
     */
    fun zoomIn(step: Double = ZOOM_STEP) {
        _uiState.update {
            it.withZoom(it.zoomScale * step)
        }
    }

    /**
     * Zooms out on the map
     *
     * @param step Zoom divisor (default 1.5x)
     */
    fun zoomOut(step: Double = ZOOM_STEP) {
        _uiState.update {
            it.withZoom(it.zoomScale / step)
        }
    }

    /**
     * Sets zoom to a specific scale
     *
     * @param scale The zoom scale to set (0.01 to 2.0)
     */
    fun setZoom(scale: Double) {
        _uiState.update {
            it.withZoom(scale)
        }
    }

    /**
     * Centers the map on the current location with appropriate zoom
     */
    fun centerOnLocation() {
        _uiState.update {
            if (it.hasLocation()) {
                it.copy(zoomScale = MapUiState.LOCATION_ZOOM_SCALE)
            } else {
                it
            }
        }
    }

    /**
     * Resets zoom to default
     */
    fun resetZoom() {
        _uiState.update {
            it.withZoom(MapUiState.DEFAULT_SCALE)
        }
    }

    /**
     * Sets loading state
     *
     * @param isLoading Whether the map is loading
     */
    fun setLoading(isLoading: Boolean) {
        _uiState.update { it.copy(isLoading = isLoading) }
    }

    /**
     * Sets error state
     *
     * @param error Error message, null to clear error
     */
    fun setError(error: String?) {
        _uiState.update { it.copy(error = error) }
    }

    /**
     * Toggles attribution visibility
     */
    fun toggleAttribution() {
        _uiState.update { it.copy(showAttribution = !it.showAttribution) }
    }

    companion object {
        private const val ZOOM_STEP = 1.5
    }
}
