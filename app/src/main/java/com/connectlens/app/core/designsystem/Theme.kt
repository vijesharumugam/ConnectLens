package com.connectlens.app.core.designsystem

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Light colour scheme ───────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary            = Indigo40,
    onPrimary          = White,
    primaryContainer   = Indigo90,
    onPrimaryContainer = Indigo10,
    secondary          = Violet40,
    onSecondary        = White,
    secondaryContainer = Violet90,
    onSecondaryContainer = VioletDark20,
    tertiary           = Teal40,
    onTertiary         = White,
    tertiaryContainer  = Teal90,
    onTertiaryContainer = Color(0xFF00201A),
    error              = ErrorRed,
    errorContainer     = ErrorRedContainer,
    onError            = White,
    onErrorContainer   = Color(0xFF410002),
    background         = Indigo99,
    onBackground       = NeutralDark,
    surface            = Indigo99,
    onSurface          = NeutralDark,
    surfaceVariant     = Color(0xFFE3E1EC),
    onSurfaceVariant   = Color(0xFF46444F),
    outline            = Color(0xFF777680),
    outlineVariant     = Color(0xFFC7C5D0),
)

// ── Dark colour scheme ────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary            = Indigo80,
    onPrimary          = Indigo20,
    primaryContainer   = Indigo30,
    onPrimaryContainer = Indigo90,
    secondary          = Violet80,
    onSecondary        = Color(0xFF492532),
    secondaryContainer = Color(0xFF633B48),
    onSecondaryContainer = Violet90,
    tertiary           = Teal80,
    onTertiary         = Color(0xFF003731),
    tertiaryContainer  = Color(0xFF005143),
    onTertiaryContainer = Teal90,
    error              = ErrorRedDark,
    errorContainer     = ErrorContainerDark,
    onError            = Color(0xFF690005),
    onErrorContainer   = Color(0xFFFFDAD6),
    background         = NeutralDark,
    onBackground       = NeutralLight,
    surface            = NeutralDark,
    onSurface          = NeutralLight,
    surfaceVariant     = Color(0xFF49454F),
    onSurfaceVariant   = Color(0xFFCAC4D0),
    outline            = Color(0xFF938F99),
    outlineVariant     = Color(0xFF49454F),
)

/**
 * ConnectLens Material 3 theme.
 *
 * Uses dynamic colour on Android 12+ (API 31+).
 * Falls back to Indigo / Violet / Teal custom scheme on older devices.
 */
@Composable
fun ConnectLensTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = ConnectLensTypography,
        shapes      = ConnectLensShapes,
        content     = content
    )
}
