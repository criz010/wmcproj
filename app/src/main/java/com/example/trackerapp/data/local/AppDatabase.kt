package com.example.trackerapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.trackerapp.data.local.dao.LocationDao
import com.example.trackerapp.data.local.entity.LocationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Room Database für TrackerApp
 *
 * Singleton Pattern: Nur eine Database-Instanz für die gesamte App.
 * Thread-safe Implementation mit synchronized.
 *
 * Features:
 * - Auto-migration support (für spätere Versionen)
 * - Pre-population mit Test-Daten (optional)
 * - Export Schema für Migrations-Testing
 *
 * Version History:
 * - Version 1: Initial schema mit LocationEntity
 */
@Database(
    entities = [LocationEntity::class],
    version = 1,
    exportSchema = true  // Für Testing & Migrations
)
abstract class AppDatabase : RoomDatabase() {

    /**
     * DAO Access
     */
    abstract fun locationDao(): LocationDao

    companion object {
        // Volatile = Änderungen sofort für alle Threads sichtbar
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Holt Database-Instanz (Singleton Pattern)
         *
         * Thread-safe mit Double-Check Locking
         *
         * @param context Application Context
         * @param prePopulate Soll Database mit Test-Daten gefüllt werden?
         * @return Database-Instanz
         */
        fun getDatabase(
            context: Context,
            prePopulate: Boolean = false
        ): AppDatabase {
            // Schneller Zugriff ohne Lock wenn bereits initialisiert
            return INSTANCE ?: synchronized(this) {
                // Double-check: War ein anderer Thread schneller?
                val instance = INSTANCE ?: buildDatabase(context, prePopulate)
                INSTANCE = instance
                instance
            }
        }

        /**
         * Baut die Database
         */
        private fun buildDatabase(
            context: Context,
            prePopulate: Boolean
        ): AppDatabase {
            val builder = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "tracker_database"
            )

            // Pre-populate mit Test-Daten (optional)
            if (prePopulate) {
                builder.addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            populateDatabase(INSTANCE?.locationDao())
                        }
                    }
                })
            }

            return builder.build()
        }

        /**
         * Füllt Database mit Test-Daten
         *
         * Beispiel-Route: Wien → Graz → Salzburg
         */
        private suspend fun populateDatabase(locationDao: LocationDao?) {
            locationDao ?: return

            val testLocations = listOf(
                // Wien (Stephansdom)
                LocationEntity(
                    userId = "test",
                    latitude = 48.2082,
                    longitude = 16.3738,
                    accuracy = 10f,
                    timestamp = System.currentTimeMillis() - 3600000 // vor 1h
                ),
                // Wiener Neustadt
                LocationEntity(
                    userId = "test",
                    latitude = 47.8117,
                    longitude = 16.2426,
                    accuracy = 15f,
                    timestamp = System.currentTimeMillis() - 2400000 // vor 40min
                ),
                // Graz (Schlossberg)
                LocationEntity(
                    userId = "test",
                    latitude = 47.0758,
                    longitude = 15.4384,
                    accuracy = 12f,
                    timestamp = System.currentTimeMillis() - 1200000 // vor 20min
                ),
                // Salzburg (Festung)
                LocationEntity(
                    userId = "test",
                    latitude = 47.7951,
                    longitude = 13.0470,
                    accuracy = 8f,
                    timestamp = System.currentTimeMillis() // jetzt
                )
            )

            locationDao.insertAll(testLocations)
        }

        /**
         * Für Testing: Zerstört Database-Instanz
         */
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
