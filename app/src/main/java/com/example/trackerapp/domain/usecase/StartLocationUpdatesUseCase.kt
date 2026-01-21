package com.example.trackerapp.domain.usecase

import com.example.trackerapp.domain.model.LocationResult
import com.example.trackerapp.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Use case for starting continuous location updates
 *
 * This use case manages the lifecycle of continuous location tracking,
 * providing a reactive stream of location updates via Flow.
 *
 * Single Responsibility: Manage continuous location updates
 *
 * @property repository The location repository implementation
 */
class StartLocationUpdatesUseCase(
    private val repository: LocationRepository
) {
    /**
     * Starts continuous location updates
     *
     * Business logic:
     * 1. Verify permissions and services are enabled
     * 2. Start location updates stream
     * 3. Emit updates as they arrive
     *
     * @return Flow of LocationResult that emits updates
     */
    operator fun invoke(): Flow<LocationResult> = flow {
        // Check prerequisites
        if (!repository.hasLocationPermission()) {
            emit(LocationResult.Error(
                com.example.trackerapp.domain.model.LocationError.PermissionDenied()
            ))
            return@flow
        }

        if (!repository.isLocationEnabled()) {
            emit(LocationResult.Error(
                com.example.trackerapp.domain.model.LocationError.ServicesDisabled()
            ))
            return@flow
        }

        // Start collecting location updates
        repository.getLocationUpdates().collect { result ->
            emit(result)
        }
    }

    /**
     * Stops continuous location updates
     */
    fun stop() {
        repository.stopLocationUpdates()
    }
}
