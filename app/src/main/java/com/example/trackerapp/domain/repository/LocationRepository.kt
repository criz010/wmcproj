package com.example.trackerapp.domain.repository

import com.example.trackerapp.domain.model.Location
import com.example.trackerapp.domain.model.LocationResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for location operations
 *
 * This interface defines the contract for location data access,
 * following the Repository Pattern and Dependency Inversion Principle.
 *
 * The actual implementation is in the data layer, keeping the domain
 * layer independent of Android framework dependencies.
 */
interface LocationRepository {

    /**
     * Gets the current location as a one-time request
     *
     * @return LocationResult with either Success containing Location or Error
     */
    suspend fun getCurrentLocation(): LocationResult

    /**
     * Starts continuous location updates
     *
     * @return Flow of LocationResult that emits updates as location changes
     */
    fun getLocationUpdates(): Flow<LocationResult>

    /**
     * Stops continuous location updates
     */
    fun stopLocationUpdates()

    /**
     * Checks if location permissions are granted
     *
     * @return true if permissions are granted, false otherwise
     */
    suspend fun hasLocationPermission(): Boolean

    /**
     * Checks if location services are enabled on the device
     *
     * @return true if location services are enabled, false otherwise
     */
    suspend fun isLocationEnabled(): Boolean

    /**
     * Gets the last known location from cache (if available)
     *
     * This is faster than getCurrentLocation() but may return stale data
     *
     * @return LocationResult with cached location or Error if unavailable
     */
    suspend fun getLastKnownLocation(): LocationResult
}
