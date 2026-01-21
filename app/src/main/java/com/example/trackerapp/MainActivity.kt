package com.example.trackerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.trackerapp.core.design.TrackerAppTheme
import com.example.trackerapp.data.repository.LocationRepositoryImpl
import com.example.trackerapp.data.source.LocationDataSource
import com.example.trackerapp.domain.usecase.GetCurrentLocationUseCase
import com.example.trackerapp.presentation.location.LocationViewModel
import com.example.trackerapp.presentation.map.MapViewModel
import com.example.trackerapp.presentation.navigation.TrackerNavigation
import com.google.android.gms.location.LocationServices

/**
 * Main Activity - Entry point for TrackerApp
 *
 * Modern implementation with:
 * - Edge-to-edge display
 * - Material 3 theming
 * - Proper dependency setup
 * - Clean Architecture integration
 *
 * Note: In a production app, dependency injection (Hilt/Koin) would be used
 * to manage these dependencies. For this refactored version, manual DI is
 * used to maintain simplicity while demonstrating Clean Architecture.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()

        // Manual Dependency Injection
        // In production: Use Hilt or Koin
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationDataSource = LocationDataSource(
            context = applicationContext,
            fusedLocationClient = fusedLocationClient
        )
        val locationRepository = LocationRepositoryImpl(locationDataSource)
        val getCurrentLocationUseCase = GetCurrentLocationUseCase(locationRepository)

        setContent {
            TrackerAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // ViewModels with manual dependency injection
                    // Note: Factory pattern would be cleaner, but this demonstrates the architecture
                    val locationViewModel = LocationViewModel(
                        getCurrentLocationUseCase = getCurrentLocationUseCase
                    )

                    val mapViewModel = MapViewModel()

                    // Navigation Graph
                    TrackerNavigation(
                        navController = navController,
                        locationViewModel = locationViewModel,
                        mapViewModel = mapViewModel
                    )
                }
            }
        }
    }
}
