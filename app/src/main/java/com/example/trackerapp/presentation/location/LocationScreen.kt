package com.example.trackerapp.presentation.location

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.trackerapp.R
import com.example.trackerapp.core.constants.AppConstants
import com.example.trackerapp.core.design.CoordinateTypography
import com.example.trackerapp.core.design.Spacing
import com.example.trackerapp.core.utils.CoordinateFormatter
import com.example.trackerapp.presentation.components.AccentButton
import com.example.trackerapp.presentation.components.DataRow
import com.example.trackerapp.presentation.components.ElevatedCard
import com.example.trackerapp.presentation.components.ErrorIndicator
import com.example.trackerapp.presentation.components.InfoCard
import com.example.trackerapp.presentation.components.LoadingIndicator
import com.example.trackerapp.presentation.components.PrimaryButton
import com.example.trackerapp.presentation.components.PulsingGpsIndicator
import com.example.trackerapp.presentation.components.SecondaryButton
import com.example.trackerapp.presentation.components.SignalQualityIndicator
import com.example.trackerapp.presentation.components.SuccessIndicator
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Location Screen - Modern implementation with Clean Architecture
 *
 * Features:
 * - Airbus-inspired professional design
 * - Proper permission handling with rationale
 * - StateFlow-based reactive UI
 * - Comprehensive error handling
 * - Smooth animations
 * - Accessibility support
 *
 * @param navController Navigation controller
 * @param viewModel LocationViewModel instance
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationScreen(
    navController: NavController,
    viewModel: LocationViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Permission state
    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Request location when permissions are granted
    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            viewModel.onPermissionGranted()
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Spacing.md)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            // Hero Section
            HeroSection()

            Spacer(modifier = Modifier.height(Spacing.xs))

            // Permission Handling
            when {
                !locationPermissionsState.allPermissionsGranted -> {
                    PermissionRequestSection(
                        showRationale = locationPermissionsState.shouldShowRationale,
                        onRequestPermission = {
                            locationPermissionsState.launchMultiplePermissionRequest()
                        },
                        onOpenSettings = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
                else -> {
                    // Main Content
                    when (val state = uiState) {
                        is LocationUiState.Idle -> {
                            IdleState(
                                onGetLocation = { viewModel.getCurrentLocation() }
                            )
                        }
                        is LocationUiState.Loading -> {
                            LoadingState()
                        }
                        is LocationUiState.Success -> {
                            SuccessState(
                                state = state,
                                onRefresh = { viewModel.getCurrentLocation() },
                                onShowMap = {
                                    navController.navigate(AppConstants.Routes.MAP)
                                },
                                navController = navController
                            )
                        }
                        is LocationUiState.Error -> {
                            ErrorState(
                                state = state,
                                onRetry = { viewModel.retry() }
                            )
                        }
                        is LocationUiState.PermissionDenied -> {
                            PermissionDeniedState(
                                isPermanent = state.isPermanentlyDenied,
                                onOpenSettings = {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", context.packageName, null)
                                    }
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Hero section with app title and subtitle
 */
@Composable
private fun HeroSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        Text(
            text = stringResource(R.string.location_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.location_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Permission request section
 */
@Composable
private fun PermissionRequestSection(
    showRationale: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    ElevatedCard {
        Text(
            text = stringResource(R.string.permission_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = if (showRationale) {
                stringResource(R.string.permission_rationale)
            } else {
                stringResource(R.string.permission_message)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        if (showRationale) {
            AccentButton(
                text = stringResource(R.string.btn_request_permission),
                onClick = onRequestPermission
            )
        } else {
            PrimaryButton(
                text = stringResource(R.string.btn_request_permission),
                onClick = onRequestPermission
            )
        }
    }
}

/**
 * Idle state - Initial state before location request
 */
@Composable
private fun IdleState(
    onGetLocation: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        ElevatedCard {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Text(
                    text = stringResource(R.string.empty_no_location),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.empty_request_location),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        PrimaryButton(
            text = stringResource(R.string.btn_get_location),
            onClick = onGetLocation
        )
    }
}

/**
 * Loading state
 */
@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            PulsingGpsIndicator()
            LoadingIndicator(text = stringResource(R.string.loading_location))
        }
    }
}

/**
 * Success state - Location acquired
 */
@Composable
private fun SuccessState(
    state: LocationUiState.Success,
    onRefresh: () -> Unit,
    onShowMap: () -> Unit,
    navController: NavController
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // Success message
            SuccessIndicator(
                message = stringResource(R.string.success_location_found)
            )

            // Signal Quality
            ElevatedCard {
                SignalQualityIndicator(
                    signalQuality = state.signalQuality,
                    accuracy = state.location.accuracy,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Coordinates Card
            InfoCard(title = stringResource(R.string.map_position)) {
                // Latitude
                Text(
                    text = CoordinateFormatter.formatLatitude(state.location.latitude),
                    style = CoordinateTypography,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )
                // Longitude
                Text(
                    text = CoordinateFormatter.formatLongitude(state.location.longitude),
                    style = CoordinateTypography,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Additional Info
            InfoCard(title = "Details") {
                state.location.accuracy?.let {
                    DataRow(
                        label = stringResource(R.string.location_label_accuracy),
                        value = "±${it.toInt()} ${stringResource(R.string.unit_meters)}"
                    )
                }
                state.location.altitude?.let {
                    DataRow(
                        label = stringResource(R.string.location_label_altitude),
                        value = "${it.toInt()} ${stringResource(R.string.unit_meters)}"
                    )
                }
                state.location.speed?.let {
                    DataRow(
                        label = stringResource(R.string.location_label_speed),
                        value = "${"%.1f".format(it)} ${stringResource(R.string.unit_mps)}"
                    )
                }
                state.location.bearing?.let {
                    DataRow(
                        label = stringResource(R.string.location_label_bearing),
                        value = "${it.toInt()}${stringResource(R.string.unit_degrees)}"
                    )
                }
                DataRow(
                    label = stringResource(R.string.location_label_timestamp),
                    value = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                        .format(Date(state.location.timestamp))
                )
            }

            // Action Buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                PrimaryButton(
                    text = stringResource(R.string.btn_show_map),
                    onClick = onShowMap
                )
                SecondaryButton(
                    text = stringResource(R.string.btn_refresh),
                    onClick = onRefresh
                )
                // Tracker Button
                SecondaryButton(
                    text = "Location Tracker",
                    onClick = {
                        navController.navigate(AppConstants.Routes.TRACKER)
                    }
                )
                // List Button
                SecondaryButton(
                    text = "Gespeicherte Locations",
                    onClick = {
                        navController.navigate(AppConstants.Routes.LIST)
                    }
                )
                // Path Button (TODO)
                SecondaryButton(
                    text = "Path anzeigen",
                    onClick = {
                        navController.navigate(AppConstants.Routes.PATH)
                    }
                )
            }
        }
    }
}

/**
 * Error state
 */
@Composable
private fun ErrorState(
    state: LocationUiState.Error,
    onRetry: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        ErrorIndicator(message = state.error.message)

        if (state.canRetry) {
            AccentButton(
                text = stringResource(R.string.btn_retry),
                onClick = onRetry
            )
        }
    }
}

/**
 * Permission denied state
 */
@Composable
private fun PermissionDeniedState(
    isPermanent: Boolean,
    onOpenSettings: () -> Unit
) {
    ElevatedCard {
        Text(
            text = if (isPermanent) {
                stringResource(R.string.permission_permanently_denied)
            } else {
                stringResource(R.string.permission_denied)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
        if (isPermanent) {
            Spacer(modifier = Modifier.height(Spacing.sm))
            SecondaryButton(
                text = stringResource(R.string.btn_settings),
                onClick = onOpenSettings
            )
        }
    }
}
