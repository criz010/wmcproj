package com.example.trackerapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.trackerapp.core.design.AirbusBlue
import com.example.trackerapp.core.design.CornerRadius
import com.example.trackerapp.core.design.GradientEnd
import com.example.trackerapp.core.design.GradientStart
import com.example.trackerapp.core.design.SafetyOrange
import com.example.trackerapp.core.design.Spacing

/**
 * Primary action button with gradient background (Airbus-inspired)
 *
 * Features:
 * - Gradient background (Airbus Blue to Sky Blue)
 * - Rounded corners
 * - Professional elevation
 * - Consistent sizing
 *
 * @param text Button text
 * @param onClick Click handler
 * @param modifier Modifier for customization
 * @param enabled Whether button is enabled
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(GradientStart, GradientEnd)
                ),
                shape = RoundedCornerShape(CornerRadius.sm)
            ),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White,
            disabledContainerColor = Color.Gray,
            disabledContentColor = Color.LightGray
        ),
        shape = RoundedCornerShape(CornerRadius.sm),
        contentPadding = PaddingValues(Spacing.md),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = Spacing.elevationMedium,
            pressedElevation = Spacing.elevationLow
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * Secondary button with outline style
 *
 * @param text Button text
 * @param onClick Click handler
 * @param modifier Modifier for customization
 * @param enabled Whether button is enabled
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(CornerRadius.sm),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = AirbusBlue
        ),
        contentPadding = PaddingValues(Spacing.md)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * Accent button for attention-grabbing actions (Safety Orange)
 *
 * @param text Button text
 * @param onClick Click handler
 * @param modifier Modifier for customization
 * @param enabled Whether button is enabled
 */
@Composable
fun AccentButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = SafetyOrange,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(CornerRadius.sm),
        contentPadding = PaddingValues(Spacing.md),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = Spacing.elevationMedium
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
