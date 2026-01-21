package com.example.trackerapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.trackerapp.core.constants.AppConstants
import com.example.trackerapp.presentation.location.LocationScreen
import com.example.trackerapp.presentation.location.LocationViewModel
import com.example.trackerapp.presentation.map.MapScreen
import com.example.trackerapp.presentation.map.MapViewModel
import com.example.trackerapp.presentation.tracker.TrackerScreen
import com.example.trackerapp.presentation.list.ListScreen
import com.example.trackerapp.presentation.path.PathScreen

/**
 * Navigation sealed class defining all app routes
 *
 * Type-safe navigation routes using sealed class pattern
 */
sealed class Screen(val route: String) {
    data object Location : Screen(AppConstants.Routes.LOCATION)
    data object Map : Screen(AppConstants.Routes.MAP)
    data object Tracker : Screen(AppConstants.Routes.TRACKER)
    data object List : Screen(AppConstants.Routes.LIST)
    data object Path : Screen(AppConstants.Routes.PATH)
}

/**
 * Main navigation graph for TrackerApp
 *
 * Sets up the navigation between LocationScreen and MapScreen
 * with proper ViewModel management
 *
 * @param navController Navigation controller
 * @param locationViewModel Shared LocationViewModel
 * @param mapViewModel MapViewModel instance
 */
@Composable
fun TrackerNavigation(
    navController: NavHostController,
    locationViewModel: LocationViewModel,
    mapViewModel: MapViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Location.route
    ) {
        composable(route = Screen.Location.route) {
            LocationScreen(
                navController = navController,
                viewModel = locationViewModel
            )
        }

        composable(route = Screen.Map.route) {
            MapScreen(
                navController = navController,
                viewModel = locationViewModel
            )
        }

        composable(route = Screen.Tracker.route) {
            TrackerScreen(
                navController = navController
            )
        }

        composable(route = Screen.List.route) {
            ListScreen(
                navController = navController
            )
        }

        composable(route = Screen.Path.route) {
            PathScreen(
                navController = navController
            )
        }
    }
}
