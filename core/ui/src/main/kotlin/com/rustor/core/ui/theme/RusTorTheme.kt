package com.rustor.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import com.rustor.core.ui.theme.color.RusTorDarkColorScheme
import com.rustor.core.ui.theme.color.RusTorLightColorScheme
import com.rustor.core.ui.theme.dimens.LocalRusTorRadius
import com.rustor.core.ui.theme.dimens.LocalRusTorSpacing
import com.rustor.core.ui.theme.dimens.RusTorRadius
import com.rustor.core.ui.theme.dimens.RusTorShapes
import com.rustor.core.ui.theme.dimens.RusTorSpacing
import com.rustor.core.ui.theme.typography.RusTorTypography

@Composable
fun RusTorTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) RusTorDarkColorScheme else RusTorLightColorScheme

    CompositionLocalProvider(
        LocalRusTorSpacing provides RusTorSpacing(),
        LocalRusTorRadius provides RusTorRadius(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = RusTorTypography,
            shapes = RusTorShapes,
            content = content,
        )
    }
}

object RusTorTheme {

    val colors: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme

    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography

    val shapes: Shapes
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.shapes

    val spacing: RusTorSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalRusTorSpacing.current

    val radius: RusTorRadius
        @Composable
        @ReadOnlyComposable
        get() = LocalRusTorRadius.current
}
