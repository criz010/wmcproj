package com.example.trackerapp.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsNotFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.trackerapp.R
import com.example.trackerapp.core.design.CornerRadius
import com.example.trackerapp.core.design.Error
import com.example.trackerapp.core.design.GpsSignalExcellent
import com.example.trackerapp.core.design.GpsSignalFair
import com.example.trackerapp.core.design.GpsSignalGood
import com.example.trackerapp.core.design.GpsSignalNone
import com.example.trackerapp.core.design.GpsSignalPoor
import com.example.trackerapp.core.design.Spacing
import com.example.trackerapp.core.design.Success

/**
 * GPS signal quality indicator
 *
 * Shows GPS signal strength with color-coded bars and icon
 *
 * @param signalQuality Signal quality level (0-4)
 * @param accuracy Accuracy in meters (optional)
 * @param modifier Modifier for customization
 */
@Composable
fun SignalQualityIndicator(
    signalQuality: Int,
    accuracy: Float? = null,
    modifier: Modifier = Modifier
) {
    val (color, label, icon) = when (signalQuality) {
        4 -> Triple(GpsSignalExcellent, stringResource(R.string.accuracy_excellent), Icons.Default.GpsFixed)
        3 -> Triple(GpsSignalGood, stringResource(R.string.accuracy_very_good), Icons.Default.GpsFixed)
        2 -> Triple(GpsSignalFair, stringResource(R.string.accuracy_good), Icons.Default.GpsNotFixed)
        1 -> Triple(GpsSignalPoor, stringResource(R.string.accuracy_fair), Icons.Default.GpsNotFixed)
        else -> Triple(GpsSignalNone, stringResource(R.string.accuracy_poor), Icons.Default.GpsOff)
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(R.string.cd_gps_signal),
            tint = color,
            modifier = Modifier.size(24.dp)
        )

        // Signal bars
        SignalBars(
            strength = signalQuality,
            color = color
        )

        Spacer(modifier = Modifier.width(Spacing.xs))

        // Label and accuracy
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = color
            )
            accuracy?.let {
                Text(
                    text = "±${it.toInt()} ${stringResource(R.string.unit_meters)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Signal strength bars visualization
 */
@Composable
private fun SignalBars(
    strength: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(5) { index ->
            val barColor = if (index < strength) color else Color.Gray.copy(alpha = 0.3f)
            Canvas(
                modifier = Modifier
                    .width(4.dp)
                    .size(height = (8 + index * 4).dp, width = 4.dp)
            ) {
                drawLine(
                    color = barColor,
                    start = Offset(0f, size.height),
                    end = Offset(0f, 0f),
                    strokeWidth = size.width,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

/**
 * Loading indicator with custom animation
 *
 * @param text Loading message
 * @param modifier Modifier for customization
 */
@Composable
fun LoadingIndicator(
    text: String = stringResource(R.string.loading_location),
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Pulsing GPS search indicator
 */
@Composable
fun PulsingGpsIndicator(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.GpsNotFixed,
            contentDescription = stringResource(R.string.location_status_searching),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
            modifier = Modifier.size(64.dp)
        )
    }
}

/**
 * Error indicator with icon and message
 *
 * @param message Error message
 * @param modifier Modifier for customization
 */
@Composable
fun ErrorIndicator(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Error
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Error.copy(alpha = 0.1f)
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(CornerRadius.sm)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Error,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Error
            )
        }
    }
}

/**
 * Success indicator with checkmark
 *
 * @param message Success message
 * @param modifier Modifier for customization
 */
@Composable
fun SuccessIndicator(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Success.copy(alpha = 0.1f)
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(CornerRadius.sm)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.GpsFixed,
                contentDescription = null,
                tint = Success,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Success
            )
        }
    }
}
