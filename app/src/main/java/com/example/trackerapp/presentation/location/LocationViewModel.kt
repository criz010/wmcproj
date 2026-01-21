package com.example.trackerapp.presentation.location

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.trackerapp.presentation.location.LocationUiState
import com.example.trackerapp.domain.usecase.GetCurrentLocationUseCase
import com.example.trackerapp.domain.model.LocationError
import com.example.trackerapp.domain.model.LocationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

/**
 * ViewModel für Location-Funktionalität
 *
 * Verwaltet den State für die aktuelle GPS-Position des Nutzers.
 */
class LocationViewModel(private val getCurrentLocationUseCase: GetCurrentLocationUseCase
) : ViewModel() {

    // Backing property for mutable state
    private val _uiState = MutableStateFlow<LocationUiState>(LocationUiState.Idle)

    /**
     * Immutable UI state exposed to the UI layer
     */
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

        /**
         * Requests current location
         *
         * This method:
         * 1. Sets loading state
         * 2. Executes use case
         * 3. Updates state based on result
         * 4. Handles errors appropriately
         */
        fun getCurrentLocation() {
            viewModelScope.launch {
                // Set loading state
                _uiState.value = LocationUiState.Loading

                // Execute use case
                when (val result = getCurrentLocationUseCase()) {
                    is LocationResult.Success -> {
                        _uiState.value = LocationUiState.Success(
                            location = result.location,
                            signalQuality = result.location.getSignalQuality()
                        )
                    }
                    is LocationResult.Error -> {
                        handleError(result.error)
                    }
                }
            }
        }

        /**
         * Attempts to get last known location (faster, may be stale)
         */
        fun getLastKnownLocation() {
            viewModelScope.launch {
                _uiState.value = LocationUiState.Loading

                when (val result = getCurrentLocationUseCase.getLastKnown()) {
                    is LocationResult.Success -> {
                        _uiState.value = LocationUiState.Success(
                            location = result.location,
                            signalQuality = result.location.getSignalQuality()
                        )
                    }
                    is LocationResult.Error -> {
                        // If last known fails, try current location
                        getCurrentLocation()
                    }
                }
            }
        }

        /**
         * Retries location request after error
         */
        fun retry() {
            getCurrentLocation()
        }

        /**
         * Resets state to idle
         */
        fun resetState() {
            _uiState.value = LocationUiState.Idle
        }

        /**
         * Handles permission denied state
         *
         * @param isPermanent Whether the denial is permanent
         */
        fun onPermissionDenied(isPermanent: Boolean = false) {
            _uiState.value = LocationUiState.PermissionDenied(isPermanent)
        }

        /**
         * Handles permission granted - proceeds with location request
         */
        fun onPermissionGranted() {
            getCurrentLocation()
        }

        /**
         * Handles different error types appropriately
         */
        private fun handleError(error: LocationError) {
            _uiState.value = when (error) {
                is LocationError.PermissionDenied -> {
                    LocationUiState.PermissionDenied(isPermanentlyDenied = false)
                }
                is LocationError.ServicesDisabled -> {
                    LocationUiState.Error(error, canRetry = false)
                }
                is LocationError.Timeout -> {
                    LocationUiState.Error(error, canRetry = true)
                }
                is LocationError.NetworkError -> {
                    LocationUiState.Error(error, canRetry = true)
                }
                is LocationError.ProviderUnavailable -> {
                    LocationUiState.Error(error, canRetry = true)
                }
                is LocationError.Unknown -> {
                    LocationUiState.Error(error, canRetry = true)
                }
            }
        }

    /**
     * Cleans up resources when ViewModel is cleared
     */
    override fun onCleared() {
        super.onCleared()
        // Any cleanup needed (e.g., stopping location updates if we add that feature)
    }
}