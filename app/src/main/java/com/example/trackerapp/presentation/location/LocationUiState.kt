package com.example.trackerapp.presentation.location

import com.example.trackerapp.domain.model.Location
import com.example.trackerapp.domain.model.LocationError

/**
 * UI state for LocationScreen
 *
 * Sealed class hierarchy representing all possible states of the location screen.
 * This provides type-safe state management and makes it easy to handle each state
 * in the UI layer.
 *
 * Benefits:
 * - Type-safe: Compile-time checking of state handling
 * - Exhaustive: when() expressions must handle all cases
 * - Clear: Each state has exactly the data it needs
 * - Immutable: States cannot be modified once created
 */
sealed class LocationUiState {
    /**
     * Initial state - No location request made yet
     */
    data object Idle : LocationUiState()

    /**
     * Loading state - Location request in progress
     */
    data object Loading : LocationUiState()

    /**
     * Success state - Location successfully acquired
     *
     * @property location The acquired location data
     * @property signalQuality GPS signal quality (0-4)
     */
    data class Success(
        val location: Location,
        val signalQuality: Int = location.getSignalQuality()
    ) : LocationUiState()

    /**
     * Error state - Location request failed
     *
     * @property error The error that occurred
     * @property canRetry Whether the operation can be retried
     */
    data class Error(
        val error: LocationError,
        val canRetry: Boolean = true
    ) : LocationUiState()

    /**
     * Permission denied state
     *
     * @property isPermanentlyDenied Whether permission is permanently denied
     */
    data class PermissionDenied(
        val isPermanentlyDenied: Boolean = false
    ) : LocationUiState()
}

/**
 * Helper function to check if state has location data
 */
fun LocationUiState.hasLocation(): Boolean {
    return this is LocationUiState.Success
}

/**
 * Helper function to get location data if available
 */
fun LocationUiState.getLocationOrNull(): Location? {
    return (this as? LocationUiState.Success)?.location
}

/**
 * Helper function to check if loading
 */
fun LocationUiState.isLoading(): Boolean {
    return this is LocationUiState.Loading
}

/**
 * Helper function to check if error
 */
fun LocationUiState.isError(): Boolean {
    return this is LocationUiState.Error || this is LocationUiState.PermissionDenied
}