package com.example.trackerapp.data.source

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.example.trackerapp.core.constants.AppConstants
import com.example.trackerapp.core.utils.toDomainModel
import com.example.trackerapp.domain.model.Location
import com.example.trackerapp.domain.model.LocationError
import com.example.trackerapp.domain.model.LocationResult
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult as GmsLocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Location data source implementation using Google Play Services
 *
 * This class handles all Android-specific location operations:
 * - FusedLocationProviderClient integration
 * - Permission checking
 * - Location service availability
 * - Continuous location updates via LocationCallback
 *
 * Features:
 * - Proper timeout handling
 * - Coroutine-based async operations
 * - Flow-based continuous updates
 * - Comprehensive error handling
 * - Lifecycle-aware callbacks
 *
 * @property context Application context
 * @property fusedLocationClient Google Play Services location client
 */
class LocationDataSource(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) {
    private var locationCallback: LocationCallback? = null
    private var cancellationTokenSource: CancellationTokenSource? = null

    /**
     * Gets current location with timeout handling
     *
     * This method:
     * 1. Checks for permissions (throws if missing)
     * 2. Creates a cancellation token with timeout
     * 3. Requests high-accuracy location
     * 4. Returns result or error
     *
     * @return LocationResult with location data or error
     * @throws SecurityException if permissions are not granted
     */
    suspend fun getCurrentLocation(): LocationResult {
        if (!hasLocationPermission()) {
            return LocationResult.Error(LocationError.PermissionDenied())
        }

        if (!isLocationEnabled()) {
            return LocationResult.Error(LocationError.ServicesDisabled())
        }

        return try {
            // Create cancellation token with timeout
            cancellationTokenSource = CancellationTokenSource()
            val token = cancellationTokenSource!!.token

            // Use withTimeoutOrNull to implement timeout
            val location = withTimeoutOrNull(AppConstants.Location.LOCATION_REQUEST_TIMEOUT_MS) {
                try {
                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        token
                    ).await()
                } catch (e: SecurityException) {
                    null
                } catch (e: Exception) {
                    null
                }
            }

            if (location != null) {
                LocationResult.Success(location.toDomainModel())
            } else {
                LocationResult.Error(LocationError.Timeout())
            }
        } catch (e: Exception) {
            LocationResult.Error(
                LocationError.Unknown(
                    message = e.message ?: "Unknown error occurred",
                    exception = e
                )
            )
        }
    }

    /**
     * Gets last known location from cache (fast but potentially stale)
     *
     * @return LocationResult with cached location or error
     */
    suspend fun getLastKnownLocation(): LocationResult {
        if (!hasLocationPermission()) {
            return LocationResult.Error(LocationError.PermissionDenied())
        }

        return try {
            val location = fusedLocationClient.lastLocation.await()
            if (location != null) {
                LocationResult.Success(location.toDomainModel())
            } else {
                LocationResult.Error(
                    LocationError.ProviderUnavailable("No cached location available")
                )
            }
        } catch (e: SecurityException) {
            LocationResult.Error(LocationError.PermissionDenied())
        } catch (e: Exception) {
            LocationResult.Error(
                LocationError.Unknown(
                    message = e.message ?: "Failed to get last known location",
                    exception = e
                )
            )
        }
    }

    /**
     * Starts continuous location updates as a Flow
     *
     * This creates a cold Flow that:
     * 1. Configures LocationRequest with appropriate intervals
     * 2. Registers a LocationCallback
     * 3. Emits updates through the Flow
     * 4. Automatically cleans up on cancellation
     *
     * @return Flow of LocationResult emitting updates
     */
    fun getLocationUpdates(): Flow<LocationResult> = callbackFlow {
        if (!hasLocationPermission()) {
            trySend(LocationResult.Error(LocationError.PermissionDenied()))
            close()
            return@callbackFlow
        }

        if (!isLocationEnabled()) {
            trySend(LocationResult.Error(LocationError.ServicesDisabled()))
            close()
            return@callbackFlow
        }

        // Configure location request
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            AppConstants.Location.UPDATE_INTERVAL_MS
        ).apply {
            setMinUpdateIntervalMillis(AppConstants.Location.FASTEST_UPDATE_INTERVAL_MS)
            setMaxUpdateDelayMillis(AppConstants.Location.MAX_WAIT_TIME_MS)
            setMinUpdateDistanceMeters(AppConstants.Location.MIN_DISPLACEMENT)
        }.build()

        // Create callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: GmsLocationResult) {
                result.lastLocation?.let { location ->
                    trySend(LocationResult.Success(location.toDomainModel()))
                }
            }
        }

        // Start location updates
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            ).await()
        } catch (e: SecurityException) {
            trySend(LocationResult.Error(LocationError.PermissionDenied()))
            close()
            return@callbackFlow
        } catch (e: Exception) {
            trySend(LocationResult.Error(
                LocationError.Unknown(
                    message = "Failed to start location updates",
                    exception = e
                )
            ))
            close()
            return@callbackFlow
        }

        // Clean up when Flow is cancelled
        awaitClose {
            stopLocationUpdates()
        }
    }

    /**
     * Stops continuous location updates
     */
    fun stopLocationUpdates() {
        locationCallback?.let { callback ->
            fusedLocationClient.removeLocationUpdates(callback)
            locationCallback = null
        }
        cancellationTokenSource?.cancel()
        cancellationTokenSource = null
    }

    /**
     * Checks if location permissions are granted
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Checks if location services are enabled on the device
     */
    fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }
}
