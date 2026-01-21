package com.example.trackerapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity für Location Tracking
 *
 * Speichert GPS-Koordinaten mit Zeitstempel und User-ID
 * für spätere Analyse und Path-Darstellung.
 *
 * @property id Auto-incrementierte Primary Key
 * @property userId User-ID (für Multi-User Support später)
 * @property latitude Breitengrad (-90 bis +90)
 * @property longitude Längengrad (-180 bis +180)
 * @property accuracy GPS-Genauigkeit in Metern (optional)
 * @property altitude Höhe über Meeresspiegel in Metern (optional)
 * @property speed Geschwindigkeit in m/s (optional)
 * @property bearing Richtung in Grad (0-360, optional)
 * @property timestamp Unix-Zeitstempel in Millisekunden
 */
@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val userId: String = "default",  // Später für Multi-User

    val latitude: Double,
    val longitude: Double,

    // Optionale Felder
    val accuracy: Float? = null,
    val altitude: Double? = null,
    val speed: Float? = null,
    val bearing: Float? = null,

    val timestamp: Long = System.currentTimeMillis()
)
