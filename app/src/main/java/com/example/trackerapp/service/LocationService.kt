package com.example.trackerapp.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.trackerapp.MainActivity
import com.example.trackerapp.R
import com.example.trackerapp.data.local.AppDatabase
import com.example.trackerapp.data.local.entity.LocationEntity
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import android.util.Log

/**
 * Foreground Service für kontinuierliches Location Tracking
 *
 * Dieser Service läuft im Vordergrund und trackt die Position des Benutzers
 * auch wenn die App im Hintergrund ist. Zeigt eine permanente Notification.
 *
 * Features:
 * - Foreground Service mit Notification
 * - FusedLocationProvider für effizientes GPS-Tracking
 * - Location Updates alle 10 Sekunden
 * - Speicherung in Database (später mit Room)
 */
class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var database: AppDatabase
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    companion object {
        private const val TAG = "LocationService"
        private const val NOTIFICATION_CHANNEL_ID = "location_tracking_channel"
        private const val NOTIFICATION_CHANNEL_NAME = "Location Tracking"
        private const val NOTIFICATION_ID = 1

        const val ACTION_START = "ACTION_START_LOCATION_SERVICE"
        const val ACTION_STOP = "ACTION_STOP_LOCATION_SERVICE"

        // Location Update Interval
        private const val UPDATE_INTERVAL = 10000L  // 10 Sekunden
        private const val FASTEST_INTERVAL = 5000L   // 5 Sekunden minimum
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate()")

        // Database initialisieren
        database = AppDatabase.getDatabase(applicationContext)

        // FusedLocationClient initialisieren
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // LocationCallback für Updates
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    handleLocationUpdate(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = location.accuracy,
                        altitude = if (location.hasAltitude()) location.altitude else null,
                        speed = if (location.hasSpeed()) location.speed else null,
                        bearing = if (location.hasBearing()) location.bearing else null
                    )
                }
            }
        }

        // Notification Channel erstellen
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: action=${intent?.action}")

        when (intent?.action) {
            ACTION_START -> {
                startForegroundService()
                startLocationUpdates()
            }
            ACTION_STOP -> {
                stopForegroundService()
            }
        }

        // Service soll nach Neustart weiterlaufen
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.d(TAG, "Service onDestroy()")
        stopLocationUpdates()
        serviceScope.cancel()
        super.onDestroy()
    }

    /**
     * Startet den Service im Vordergrund mit Notification
     */
    private fun startForegroundService() {
        val notification = createNotification(
            title = "TrackerApp aktiv",
            content = "Location wird getrackt..."
        )

        startForeground(NOTIFICATION_ID, notification)
        Log.d(TAG, "Service started in foreground")
    }

    /**
     * Stoppt den Foreground Service
     */
    private fun stopForegroundService() {
        Log.d(TAG, "Stopping foreground service")
        stopLocationUpdates()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    /**
     * Startet kontinuierliche Location Updates
     */
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            UPDATE_INTERVAL
        ).apply {
            setMinUpdateIntervalMillis(FASTEST_INTERVAL)
            setWaitForAccurateLocation(true)
        }.build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            Log.d(TAG, "Location updates started")
        } catch (e: SecurityException) {
            Log.e(TAG, "Missing location permission", e)
        }
    }

    /**
     * Stoppt Location Updates
     */
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d(TAG, "Location updates stopped")
    }

    /**
     * Behandelt neue Location Updates
     *
     * Speichert Location in Room Database und updated Notification
     */
    private fun handleLocationUpdate(
        latitude: Double,
        longitude: Double,
        accuracy: Float? = null,
        altitude: Double? = null,
        speed: Float? = null,
        bearing: Float? = null
    ) {
        Log.d(TAG, "📍 Location Update: $latitude, $longitude (accuracy: ${accuracy}m)")

        // Speichere in Database (asynchron)
        serviceScope.launch {
            try {
                val locationEntity = LocationEntity(
                    userId = "default",
                    latitude = latitude,
                    longitude = longitude,
                    accuracy = accuracy,
                    altitude = altitude,
                    speed = speed,
                    bearing = bearing,
                    timestamp = System.currentTimeMillis()
                )

                val locationId = database.locationDao().insert(locationEntity)
                Log.d(TAG, "✅ Location saved to database with ID: $locationId")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to save location to database", e)
            }
        }

        // Update Notification
        val notification = createNotification(
            title = "TrackerApp aktiv",
            content = "Position: ${String.format("%.6f, %.6f", latitude, longitude)}"
        )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Erstellt Notification Channel (erforderlich ab Android O)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW  // LOW = keine Sounds/Vibration
            ).apply {
                description = "Zeigt Status des Location Trackings"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)

            Log.d(TAG, "Notification channel created")
        }
    }

    /**
     * Erstellt eine Notification
     */
    private fun createNotification(title: String, content: String) =
        NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)  // TODO: Custom Icon
            .setOngoing(true)  // Notification kann nicht weggewischt werden
            .setContentIntent(createPendingIntent())
            .build()

    /**
     * PendingIntent um App beim Tippen auf Notification zu öffnen
     */
    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
