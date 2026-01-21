package com.example.trackerapp.presentation.map

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.trackerapp.presentation.location.LocationUiState
import com.example.trackerapp.presentation.location.LocationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.runBlocking
import ovh.plrapps.mapcompose.api.*
import ovh.plrapps.mapcompose.core.TileStreamProvider
import ovh.plrapps.mapcompose.ui.MapUI
import ovh.plrapps.mapcompose.ui.state.MapState
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.*

/**
 * Map-Konstanten für OpenStreetMap
 *
 * WICHTIG: fullWidth/fullHeight in MapState beziehen sich auf LEVEL 0!
 * - Level 0: 256×256 (1 Tile)
 * - Level 1: 512×512 (4 Tiles = 2^2)
 * - Level N: 256 * 2^N × 256 * 2^N
 */
object MapConstants {
    const val TILE_SIZE = 256        // OpenStreetMap Standard Tile-Größe
    const val MAX_ZOOM = 18          // OSM max zoom level (0-18)
    const val ZOOM_LEVELS = MAX_ZOOM + 1  // 19 levels total (0-18)

    // fullWidth/fullHeight für MapState = Größe bei Level 0 = 1 Tile!
    const val MAP_SIZE_LEVEL_0 = TILE_SIZE  // 256 pixels bei Level 0

    const val DEFAULT_SCALE = 0.125  // ~Zoom Level 10 (gute Übersicht)
    const val MIN_SCALE = 0.001      // Minimaler Zoom (weit raus)
    const val MAX_SCALE = 1.0        // Maximaler Zoom (nah ran)
    const val ZOOM_FACTOR = 1.5      // Zoom Schritt-Faktor
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    viewModel: LocationViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // MapState mit KORREKTER Konfiguration
    // fullWidth/fullHeight beziehen sich auf Level 0 = 256×256 (ein Tile!)
    val mapState = remember {
        MapState(
            levelCount = MapConstants.ZOOM_LEVELS,  // 19 levels (0-18)
            fullWidth = MapConstants.MAP_SIZE_LEVEL_0,  // 256 = Größe bei Level 0!
            fullHeight = MapConstants.MAP_SIZE_LEVEL_0, // 256 = Größe bei Level 0!
            workerCount = 16  // Mehr parallele Tile-Downloads
        ).apply {
            // Layer mit TileStreamProvider hinzufügen
            addLayer(
                tileStreamProvider = createTileStreamProvider()
            )

        }
    }

    // Auto-center auf Location wenn verfügbar
    LaunchedEffect(uiState) {
        if (uiState is LocationUiState.Success) {
            val location = (uiState as LocationUiState.Success).location
            val mercator = latLonToMercator(
                lat = location.latitude,
                lon = location.longitude
            )
            mapState.scrollTo(mercator.x, mercator.y)
            mapState.scale = 0.3  // Guter Zoom für Location-Ansicht
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kartenansicht") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Zurück"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Die Karte
            MapUI(
                modifier = Modifier.fillMaxSize(),
                state = mapState
            )

            // Location Info Card (nur bei Success State)
            if (uiState is LocationUiState.Success) {
                val location = (uiState as LocationUiState.Success).location

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopCenter),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Aktuelle Position",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = String.format(
                                    "%.6f, %.6f",
                                    location.latitude,
                                    location.longitude
                                ),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    val mercator = latLonToMercator(
                                        location.latitude,
                                        location.longitude
                                    )
                                    mapState.scrollTo(mercator.x, mercator.y)
                                    mapState.scale = 0.3
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Zu meiner Position",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Zoom Controls (rechts unten)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Zoom In
                FloatingActionButton(
                    onClick = {
                        val newScale = (mapState.scale * MapConstants.ZOOM_FACTOR)
                            .coerceAtMost(MapConstants.MAX_SCALE)
                        mapState.scale = newScale
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Vergrößern"
                    )
                }

                // Zoom Out
                FloatingActionButton(
                    onClick = {
                        val newScale = (mapState.scale / MapConstants.ZOOM_FACTOR)
                            .coerceAtLeast(MapConstants.MIN_SCALE)
                        mapState.scale = newScale
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Verkleinern"
                    )
                }
            }

            // Zoom Level Display (links unten)
            Card(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                Text(
                    text = "Zoom: ${(mapState.scale * 100).toInt()}%",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

/**
 * TileStreamProvider mit Debug-Logging und mehreren Tile-Servern
 *
 * WICHTIG: Der zoomLvl Parameter entspricht direkt dem OSM Zoom Level (0-18)!
 * MapCompose übergibt den Level-Index, der bei korrekter Konfiguration = OSM Zoom ist.
 */
private fun createTileStreamProvider(): TileStreamProvider {
    return TileStreamProvider { row, col, zoomLvl ->
        try {
            // Debug: Zeige was angefordert wird
            println("🗺️ [TILE REQUEST] OSM Zoom=$zoomLvl, col=$col, row=$row")

            runBlocking(Dispatchers.IO) {
                loadTile(row, col, zoomLvl)
            }
        } catch (e: Exception) {
            println("❌ [TILE ERROR] zoom=$zoomLvl, col=$col, row=$row: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}

/**
 * Lädt ein einzelnes Tile - probiert mehrere Server
 */
private suspend fun loadTile(row: Int, col: Int, zoomLvl: Int): InputStream? {
    return withContext(Dispatchers.IO) {
        // Liste von Tile-Servern (probiere nacheinander)
        val tileUrls = listOf(
            // 1. CartoDB - sehr stabil im Emulator
            "https://a.basemaps.cartocdn.com/rastertiles/voyager/$zoomLvl/$col/$row.png",

            // 2. OpenStreetMap - Standard
            "https://tile.openstreetmap.org/$zoomLvl/$col/$row.png",

            // 3. OpenStreetMap DE - Backup
            "https://a.tile.openstreetmap.de/$zoomLvl/$col/$row.png"
        )

        // Probiere jeden Server bis einer funktioniert
        for (urlString in tileUrls) {
            try {
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection

                // Wichtige Header
                connection.setRequestProperty("User-Agent", "TrackerApp/1.0")
                connection.connectTimeout = 15000  // Länger für Emulator
                connection.readTimeout = 15000
                connection.doInput = true

                // Verbinden
                connection.connect()

                // Prüfe Response-Code
                if (connection.responseCode == 200) {
                    println("✅ Tile loaded from ${urlString.substringAfter("https://")}: zoom=$zoomLvl, col=$col, row=$row")
                    return@withContext connection.inputStream
                } else {
                    println("⚠️ HTTP ${connection.responseCode} from ${urlString.substringAfter("https://")}")
                }
            } catch (e: Exception) {
                println("⚠️ Failed ${urlString.substringAfter("https://")}: ${e.message}")
                // Probiere nächsten Server
                continue
            }
        }

        // Alle Server fehlgeschlagen
        println("❌ All tile servers failed for zoom=$zoomLvl, col=$col, row=$row")
        null
    }
}

/**
 * Mercator-Punkt Datenklasse
 */
private data class MercatorPoint(val x: Double, val y: Double)

/**
 * Konvertiert GPS-Koordinaten zu Mercator-Pixel-Koordinaten
 *
 * MapCompose verwendet Koordinaten bezogen auf den MAXIMALEN Zoom-Level (Level 18).
 * Bei Level 18 ist die Karte 256 * 2^18 = 67,108,864 Pixel groß.
 *
 * @param lat Breitengrad (-90 bis +90)
 * @param lon Längengrad (-180 bis +180)
 * @return Mercator-Koordinaten (x, y) für Level 18
 */
private fun latLonToMercator(lat: Double, lon: Double): MercatorPoint {
    // Kartengröße bei maximaler Auflösung (Level 18)
    val mapSizeAtMaxLevel = MapConstants.MAP_SIZE_LEVEL_0 * (1 shl MapConstants.MAX_ZOOM)

    // Längengrad zu X: Lineare Transformation
    val x = (lon + 180.0) / 360.0 * mapSizeAtMaxLevel

    // Breitengrad zu Y: Mercator-Projektion
    val latRad = Math.toRadians(lat)
    val mercatorN = ln(tan(PI / 4.0 + latRad / 2.0))
    val y = (1.0 - mercatorN / PI) / 2.0 * mapSizeAtMaxLevel

    return MercatorPoint(x, y)
}