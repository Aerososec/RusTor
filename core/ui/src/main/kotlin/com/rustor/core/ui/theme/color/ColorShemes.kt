package com.rustor.core.ui.theme.color

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import com.rustor.core.ui.theme.color.RusTorColors

internal val RusTorLightColorScheme = lightColorScheme(
    // Primary: brand blue
    primary = RusTorColors.Primary40,
    onPrimary = RusTorColors.White,
    primaryContainer = RusTorColors.Primary90,
    onPrimaryContainer = RusTorColors.Primary10,

    // Secondary: muted steel blue
    secondary = RusTorColors.Secondary40,
    onSecondary = RusTorColors.White,
    secondaryContainer = RusTorColors.Secondary90,
    onSecondaryContainer = RusTorColors.Secondary10,

    // Tertiary: cool teal
    tertiary = RusTorColors.Tertiary40,
    onTertiary = RusTorColors.White,
    tertiaryContainer = RusTorColors.Tertiary90,
    onTertiaryContainer = RusTorColors.Tertiary10,

    // Background and surfaces
    background = RusTorColors.SurfaceLight,
    onBackground = RusTorColors.OnSurfaceLight,
    surface = RusTorColors.SurfaceLight,
    onSurface = RusTorColors.OnSurfaceLight,
    surfaceVariant = RusTorColors.SurfaceContainerHighestLight,
    onSurfaceVariant = RusTorColors.OnSurfaceVariantLight,
    surfaceDim = RusTorColors.SurfaceDimLight,
    surfaceBright = RusTorColors.SurfaceLight,

    // Surface containers
    surfaceContainerLowest = RusTorColors.SurfaceLight,
    surfaceContainerLow = RusTorColors.SurfaceContainerLowLight,
    surfaceContainer = RusTorColors.SurfaceContainerLight,
    surfaceContainerHigh = RusTorColors.SurfaceContainerHighLight,
    surfaceContainerHighest = RusTorColors.SurfaceContainerHighestLight,

    // Inverse: snackbar
    inverseSurface = RusTorColors.InverseSurfaceLight,
    inverseOnSurface = RusTorColors.InverseOnSurfaceLight,
    inversePrimary = RusTorColors.Primary80,

    // Outlines
    outline = RusTorColors.OutlineLight,
    outlineVariant = RusTorColors.OutlineVariantLight,

    // Error
    error = RusTorColors.ErrorLight,
    onError = RusTorColors.OnErrorLight,
    errorContainer = RusTorColors.ErrorContainerLight,
    onErrorContainer = RusTorColors.OnErrorContainerLight,
)

internal val RusTorDarkColorScheme = darkColorScheme(
    // Primary: brand blue
    primary = RusTorColors.Primary80,
    onPrimary = RusTorColors.Primary20,
    primaryContainer = RusTorColors.Primary30,
    onPrimaryContainer = RusTorColors.Primary90,

    // Secondary: muted steel blue
    secondary = RusTorColors.Secondary80,
    onSecondary = RusTorColors.Secondary20,
    secondaryContainer = RusTorColors.Secondary30,
    onSecondaryContainer = RusTorColors.Secondary90,

    // Tertiary: cool teal
    tertiary = RusTorColors.Tertiary80,
    onTertiary = RusTorColors.Tertiary20,
    tertiaryContainer = RusTorColors.Tertiary30,
    onTertiaryContainer = RusTorColors.Tertiary90,

    // Background and surfaces
    background = RusTorColors.SurfaceDark,
    onBackground = RusTorColors.OnSurfaceDark,
    surface = RusTorColors.SurfaceDark,
    onSurface = RusTorColors.OnSurfaceDark,
    surfaceVariant = RusTorColors.SurfaceContainerHighestDark,
    onSurfaceVariant = RusTorColors.OnSurfaceVariantDark,
    surfaceContainerLowest = RusTorColors.SurfaceContainerLowestDark,
    surfaceDim = RusTorColors.SurfaceDark,
    surfaceBright = RusTorColors.SurfaceBrightDark,

    // Surface containers
    surfaceContainerLow = RusTorColors.SurfaceContainerLowDark,
    surfaceContainer = RusTorColors.SurfaceContainerDark,
    surfaceContainerHigh = RusTorColors.SurfaceContainerHighDark,
    surfaceContainerHighest = RusTorColors.SurfaceContainerHighestDark,

    // Inverse: snackbar
    inverseSurface = RusTorColors.OnSurfaceDark,
    inverseOnSurface = RusTorColors.InverseSurfaceLight,
    inversePrimary = RusTorColors.Primary30,

    // Outlines
    outline = RusTorColors.OutlineDark,
    outlineVariant = RusTorColors.OutlineVariantDark,

    // Error
    error = RusTorColors.ErrorDark,
    onError = RusTorColors.OnErrorDark,
    errorContainer = RusTorColors.ErrorContainerDark,
    onErrorContainer = RusTorColors.OnErrorContainerDark,
)
