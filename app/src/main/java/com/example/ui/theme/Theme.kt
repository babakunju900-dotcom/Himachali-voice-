package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val StarKingColorScheme = darkColorScheme(
    primary = StarGoldPrimary,
    onPrimary = StarKingBgDark,
    primaryContainer = StarKingSurfaceVariantDark,
    onPrimaryContainer = StarGoldLight,
    secondary = RoyalPurple,
    onSecondary = TextWhite,
    secondaryContainer = StarKingCardDark,
    onSecondaryContainer = StarGoldLight,
    tertiary = NeonCyan,
    onTertiary = StarKingBgDark,
    background = StarKingBgDark,
    onBackground = TextWhite,
    surface = StarKingSurfaceDark,
    onSurface = TextWhite,
    surfaceVariant = StarKingSurfaceVariantDark,
    onSurfaceVariant = TextMuted,
    outline = StarKingCardBorder,
    error = DangerRed,
    onError = TextWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent luxury dark branding
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = StarKingBgDark.toArgb()
                window.navigationBarColor = StarKingBgDark.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = StarKingColorScheme,
        typography = Typography,
        content = content
    )
}
