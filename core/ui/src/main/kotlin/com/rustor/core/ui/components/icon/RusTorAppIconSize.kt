package com.rustor.core.ui.components.icon

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.rustor.core.ui.components.annotation.ThemePreviews
import com.rustor.core.ui.theme.RusTorTheme

enum class RusTorAppIconSize(val size: Dp) {
    /** Списки: поиск, категории. */
    Small(40.dp),

    /** Карточка на витрине. */
    Medium(56.dp),

    /** Экран приложения. */
    Large(96.dp),
}

@Composable
fun RusTorAppIcon(
    imageUrl: String?,
    appName: String,
    modifier: Modifier = Modifier,
    size: RusTorAppIconSize = RusTorAppIconSize.Medium,
    contentDescription: String? = null,
) {
    val shape = size.shape()
    var isLoaded by remember(imageUrl) { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(size.size)
            .clip(shape)
            .border(
                width = 1.dp,
                color = RusTorTheme.colors.outlineVariant.copy(alpha = 0.5f),
                shape = shape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (!isLoaded) {
            AppIconFallback(
                appName = appName,
                textStyle = size.fallbackTextStyle(),
            )
        }
        if (imageUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                onSuccess = { isLoaded = true },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun AppIconFallback(appName: String, textStyle: TextStyle) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = RusTorTheme.colors.surfaceContainerHigh,
        contentColor = RusTorTheme.colors.onSurfaceVariant,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = appName.firstOrNull()?.uppercase().orEmpty(),
                style = textStyle,
            )
        }
    }
}

@Composable
private fun RusTorAppIconSize.shape(): Shape = RoundedCornerShape(
    when (this) {
        RusTorAppIconSize.Small -> RusTorTheme.radius.md
        RusTorAppIconSize.Medium -> RusTorTheme.radius.lg
        RusTorAppIconSize.Large -> RusTorTheme.radius.xl
    },
)

@Composable
private fun RusTorAppIconSize.fallbackTextStyle(): TextStyle = when (this) {
    RusTorAppIconSize.Small -> RusTorTheme.typography.titleMedium
    RusTorAppIconSize.Medium -> RusTorTheme.typography.titleLarge
    RusTorAppIconSize.Large -> RusTorTheme.typography.headlineMedium
}

@ThemePreviews
@Composable
private fun RusTorAppIconPreview() {
    RusTorTheme {
        Surface {
            Row(
                modifier = Modifier.padding(RusTorTheme.spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(RusTorTheme.spacing.lg),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RusTorAppIcon(imageUrl = null, appName = "Финансы рядом", size = RusTorAppIconSize.Small)
                RusTorAppIcon(imageUrl = null, appName = "Планета рядом", size = RusTorAppIconSize.Medium)
                RusTorAppIcon(imageUrl = null, appName = "GameZone", size = RusTorAppIconSize.Large)
            }
        }
    }
}
