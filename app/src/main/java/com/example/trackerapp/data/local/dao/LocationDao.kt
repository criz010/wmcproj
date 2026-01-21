package com.example.trackerapp.data.local.dao

import androidx.room.*
import com.example.trackerapp.data.local.entity.LocationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object für Location Tracking
 *
 * Verwendet Kotlin Coroutines & Flow für asynchrone Queries.
 * Best Practices:
 * - suspend für einzelne Operationen (Insert, Delete, Update)
 * - Flow für reaktive Queries (automatische Updates bei Datenänderung)
 */
@Dao
interface LocationDao {

    /**
     * Speichert eine neue Location
     *
     * @param location LocationEntity zum Speichern
     * @return ID der eingefügten Location
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: LocationEntity): Long

    /**
     * Speichert mehrere Locations gleichzeitig
     *
     * @param locations Liste von LocationEntities
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(locations: List<LocationEntity>)

    /**
     * Löscht eine Location
     *
     * @param location LocationEntity zum Löschen
     */
    @Delete
    suspend fun delete(location: LocationEntity)

    /**
     * Löscht eine Location anhand der ID
     *
     * @param id ID der zu löschenden Location
     */
    @Query("DELETE FROM locations WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * Löscht alle Locations eines Users
     *
     * @param userId User-ID
     */
    @Query("DELETE FROM locations WHERE userId = :userId")
    suspend fun deleteByUserId(userId: String)

    /**
     * Löscht alle Locations (z.B. für Entwicklung/Testing)
     */
    @Query("DELETE FROM locations")
    suspend fun deleteAll()

    /**
     * Holt alle Locations als Flow (reaktiv)
     *
     * Flow Updates automatisch bei Datenänderungen!
     *
     * @return Flow mit Liste aller Locations
     */
    @Query("SELECT * FROM locations ORDER BY timestamp DESC")
    fun getAllLocations(): Flow<List<LocationEntity>>

    /**
     * Holt alle Locations eines Users als Flow
     *
     * @param userId User-ID
     * @return Flow mit Liste der User-Locations
     */
    @Query("SELECT * FROM locations WHERE userId = :userId ORDER BY timestamp DESC")
    fun getLocationsByUser(userId: String): Flow<List<LocationEntity>>

    /**
     * Holt Locations in einem Zeitfenster
     *
     * @param startTime Start-Zeitstempel
     * @param endTime End-Zeitstempel
     * @return Flow mit gefilterten Locations
     */
    @Query("SELECT * FROM locations WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    fun getLocationsByTimeRange(startTime: Long, endTime: Long): Flow<List<LocationEntity>>

    /**
     * Holt die neuesten N Locations
     *
     * @param limit Anzahl der Locations
     * @return Flow mit den neuesten Locations
     */
    @Query("SELECT * FROM locations ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLocations(limit: Int = 100): Flow<List<LocationEntity>>

    /**
     * Zählt die Anzahl aller Locations
     *
     * @return Flow mit Anzahl
     */
    @Query("SELECT COUNT(*) FROM locations")
    fun getLocationCount(): Flow<Int>

    /**
     * Holt die letzte gespeicherte Location
     *
     * @return LocationEntity oder null
     */
    @Query("SELECT * FROM locations ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastLocation(): LocationEntity?
}
