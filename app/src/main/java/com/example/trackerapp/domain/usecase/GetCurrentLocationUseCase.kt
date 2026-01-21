package com.example.trackerapp.domain.usecase

import com.example.trackerapp.domain.model.LocationResult
import com.example.trackerapp.domain.repository.LocationRepository

/**
 * Use case for getting the current location
 *
 * This encapsulates the business logic for fetching a single location update.
 * Use cases provide a clean API for the presentation layer and can combine
 * multiple repository operations if needed.
 *
 * Single Responsibility: Gets current location with proper validation
 *
 * @property repository The location repository implementation
 */
class GetCurrentLocationUseCase(
    private val repository: LocationRepository
) {
    /**
     * Executes the use case to get current location
     *
     * Business logic:
     * 1. Check if permissions are granted
     * 2. Check if location services are enabled
     * 3. Attempt to get current location
     * 4. Validate location data
     *
     * @return LocationResult indicating success or specific error
     */
    suspend operator fun invoke(): LocationResult {
        // Check permissions first
        if (!repository.hasLocationPermission()) {
            return LocationResult.Error(
                com.example.trackerapp.domain.model.LocationError.PermissionDenied()
            )
        }

        // Check if location services are enabled
        if (!repository.isLocationEnabled()) {
            return LocationResult.Error(
                com.example.trackerapp.domain.model.LocationError.ServicesDisabled()
            )
        }

        // Get current location
        return when (val result = repository.getCurrentLocation()) {
            is LocationResult.Success -> {
                // Validate location
                if (result.location.isValid()) {
                    result
                } else {
                    LocationResult.Error(
                        com.example.trackerapp.domain.model.LocationError.Unknown(
                            message = "Invalid location coordinates received"
                        )
                    )
                }
            }
            is LocationResult.Error -> result
        }
    }

    /**
     * Attempts to get last known location as a faster alternative
     *
     * @return LocationResult with cached location or error
     */
    suspend fun getLastKnown(): LocationResult {
        if (!repository.hasLocationPermission()) {
            return LocationResult.Error(
                com.example.trackerapp.domain.model.LocationError.PermissionDenied()
            )
        }

        return repository.getLastKnownLocation()
    }
}
