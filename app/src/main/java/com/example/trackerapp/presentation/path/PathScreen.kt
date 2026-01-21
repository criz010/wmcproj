package com.example.trackerapp.presentation.path

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.trackerapp.data.local.AppDatabase
import com.example.trackerapp.data.local.entity.LocationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import ovh.plrapps.mapcompose.api.*
import ovh.plrapps.mapcompose.core.TileStreamProvider
import ovh.plrapps.mapcompose.ui.MapUI
import ovh.plrapps.mapcompose.ui.state.MapState
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.*

/**
 * PathScreen - Zeigt gespeicherte Location-History auf Karte
 *
 * Features:
 * - Lädt Locations aus Room Database
 * - Reactive UI mit Flow
 * - Auto-Zoom auf Locations
 * - Distanzberechnung
 *
 * TODO: Path-Drawing mit MapCompose Paths API implementieren
 * Die aktuelle MapCompose Version hat eine andere API als erwartet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PathScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    // Locations aus Database laden
    val locations by database.locationDao().getAllLocations().collectAsState(initial = emptyList())

    // MapState
    val mapState = remember {
        MapState(
            levelCount = 19,  // 0-18
            fullWidth = 256,   // Level 0 size
            fullHeight = 256,  // Level 0 size
            workerCount = 16
        ).apply {
            addLayer(tileStreamProvider = createTileStreamProvider())
        }
    }

    // Center on first location when loaded
    LaunchedEffect(locations) {
        if (locations.isNotEmpty()) {
            centerOnPath(mapState, locations)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Path Ansicht") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Zurück"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // Center on Path FAB
            if (locations.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            centerOnPath(mapState, locations)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.CenterFocusStrong,
                        contentDescription = "Auf Path zentrieren"
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (locations.isEmpty()) {
                // Empty State
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Keine Path-Daten verfügbar",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Starte den Tracker um eine Route aufzuzeichnen",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Map mit Locations
                MapUI(
                    modifier = Modifier.fillMaxSize(),
                    state = mapState
                )

                // Info Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopCenter),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Path Information",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Punkte: ${locations.size}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (locations.size >= 2) {
                            val distance = calculateTotalDistance(locations)
                            Text(
                                text = "Distanz: ${String.format("%.2f", distance / 1000)} km",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // TODO Info
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Text(
                        text = "ℹ️ Path-Visualisierung noch in Entwicklung\n" +
                                "Die Locations werden geladen, aber die Linie wird noch nicht gezeichnet.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

/**
 * Zentriert Karte auf Path
 */
private suspend fun centerOnPath(mapState: MapState, locations: List<LocationEntity>) {
    if (locations.isEmpty()) return

    // Berechne Mittelpunkt
    val centerLat = locations.map { it.latitude }.average()
    val centerLon = locations.map { it.longitude }.average()

    val center = latLonToMercator(centerLat, centerLon)

    // Scroll to center
    mapState.scrollTo(center.x, center.y)

    // Set appropriate zoom (0.2 = ca. zoom level 12-13)
    mapState.scale = 0.2
}

/**
 * Berechnet Gesamtdistanz des Paths in Metern
 */
private fun calculateTotalDistance(locations: List<LocationEntity>): Double {
    if (locations.size < 2) return 0.0

    var totalDistance = 0.0

    for (i in 0 until locations.size - 1) {
        val loc1 = locations[i]
        val loc2 = locations[i + 1]

        totalDistance += haversineDistance(
            loc1.latitude, loc1.longitude,
            loc2.latitude, loc2.longitude
        )
    }

    return totalDistance
}

/**
 * Haversine-Formel für Distanzberechnung
 */
private fun haversineDistance(
    lat1: Double, lon1: Double,
    lat2: Double, lon2: Double
): Double {
    val R = 6371000.0  // Erdradius in Metern

    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return R * c
}

/**
 * Mercator-Punkt Datenklasse
 */
private data class MercatorPoint(val x: Double, val y: Double)

/**
 * Konvertiert GPS zu Mercator (Level 18)
 */
private fun latLonToMercator(lat: Double, lon: Double): MercatorPoint {
    val mapSizeAtMaxLevel = 256 * (1 shl 18)

    val x = (lon + 180.0) / 360.0 * mapSizeAtMaxLevel
    val latRad = Math.toRadians(lat)
    val mercatorN = ln(tan(PI / 4.0 + latRad / 2.0))
    val y = (1.0 - mercatorN / PI) / 2.0 * mapSizeAtMaxLevel

    return MercatorPoint(x, y)
}

/**
 * TileStreamProvider (gleich wie MapScreen)
 */
private fun createTileStreamProvider(): TileStreamProvider {
    return TileStreamProvider { row, col, zoomLvl ->
        try {
            runBlocking(Dispatchers.IO) {
                loadTile(row, col, zoomLvl)
            }
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Lädt Tile von Server
 */
private suspend fun loadTile(row: Int, col: Int, zoomLvl: Int): InputStream? {
    return withContext(Dispatchers.IO) {
        val tileUrls = listOf(
            "https://a.basemaps.cartocdn.com/rastertiles/voyager/$zoomLvl/$col/$row.png",
            "https://tile.openstreetmap.org/$zoomLvl/$col/$row.png"
        )

        for (urlString in tileUrls) {
            try {
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.setRequestProperty("User-Agent", "TrackerApp/1.0")
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.doInput = true
                connection.connect()

                if (connection.responseCode == 200) {
                    return@withContext connection.inputStream
                }
            } catch (e: Exception) {
                continue
            }
        }
        null
    }
}
