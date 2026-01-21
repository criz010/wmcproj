package com.example.trackerapp.data.repository

import com.example.trackerapp.data.source.LocationDataSource
import com.example.trackerapp.domain.model.LocationResult
import com.example.trackerapp.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of LocationRepository interface
 *
 * This repository acts as a single source of truth for location data,
 * abstracting the data source implementation details from the domain layer.
 *
 * Responsibilities:
 * - Delegates location operations to LocationDataSource
 * - Could add caching logic if needed
 * - Could aggregate multiple data sources
 * - Transforms data source results to domain models (already done in data source)
 *
 * Benefits:
 * - Testable: Can be mocked in unit tests
 * - Flexible: Easy to swap data source implementations
 * - Clean: Domain layer doesn't know about Android APIs
 *
 * @property dataSource The location data source implementation
 */
class LocationRepositoryImpl(
    private val dataSource: LocationDataSource
) : LocationRepository {

    /**
     * Gets the current location as a one-time request
     *
     * Delegates to data source and could add caching logic here if needed
     *
     * @return LocationResult with either Success or Error
     */
    override suspend fun getCurrentLocation(): LocationResult {
        return dataSource.getCurrentLocation()
    }

    /**
     * Gets continuous location updates as a Flow
     *
     * The Flow is cold - it only starts emitting when collected
     *
     * @return Flow of LocationResult emitting updates
     */
    override fun getLocationUpdates(): Flow<LocationResult> {
        return dataSource.getLocationUpdates()
    }

    /**
     * Stops continuous location updates
     */
    override fun stopLocationUpdates() {
        dataSource.stopLocationUpdates()
    }

    /**
     * Checks if location permissions are granted
     *
     * @return true if permissions granted, false otherwise
     */
    override suspend fun hasLocationPermission(): Boolean {
        return dataSource.hasLocationPermission()
    }

    /**
     * Checks if location services are enabled
     *
     * @return true if enabled, false otherwise
     */
    override suspend fun isLocationEnabled(): Boolean {
        return dataSource.isLocationEnabled()
    }

    /**
     * Gets last known location from cache
     *
     * Faster than getCurrentLocation() but may be stale
     *
     * @return LocationResult with cached location or error
     */
    override suspend fun getLastKnownLocation(): LocationResult {
        return dataSource.getLastKnownLocation()
    }
}
