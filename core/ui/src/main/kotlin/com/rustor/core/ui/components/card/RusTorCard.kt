package com.rustor.core.ui.components.card

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.rustor.core.ui.components.annotation.ThemePreviews
import com.rustor.core.ui.components.chip.RusTorCategoryTag
import com.rustor.core.ui.components.icon.RusTorAppIcon
import com.rustor.core.ui.components.icon.RusTorAppIconSize
import com.rustor.core.ui.components.skeleton.RusTorSkeleton
import com.rustor.core.ui.components.skeleton.SkeletonBlock
import com.rustor.core.ui.components.skeleton.SkeletonTextLine
import com.rustor.core.ui.theme.RusTorTheme

private const val DEFAULT_COVER_ASPECT_RATIO = 4f / 3f

private const val COVER_TITLE_WIDTH_FRACTION = 0.7f
private const val COVER_SCRIM_END_FRACTION = 0.6f

private const val COVER_TITLE_MAX_LINES = 3
private const val APP_CARD_TITLE_MAX_LINES = 1
private const val APP_CARD_DESCRIPTION_MAX_LINES = 2

private val COVER_TITLE_COLOR = Color.White
private val COVER_SCRIM_COLOR = Color.Black.copy(alpha = 0.55f)

private val APP_CARD_ELEVATION = 2.dp
private val APP_CARD_PREVIEW_WIDTH = 360.dp

@Composable
fun RusTorAppCard(
    title: String,
    category: String,
    description: String,
    iconUrl: String?,
    coverUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    coverTitle: String? = null,
    coverAspectRatio: Float = DEFAULT_COVER_ASPECT_RATIO,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(RusTorTheme.radius.xl),
        colors = CardDefaults.elevatedCardColors(
            containerColor = RusTorTheme.colors.surfaceContainerLow,
            contentColor = RusTorTheme.colors.onSurface,
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = APP_CARD_ELEVATION,
        ),
    ) {
        AppCardCover(
            coverUrl = coverUrl,
            coverTitle = coverTitle,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(coverAspectRatio),
        )
        Column(
            modifier = Modifier.padding(RusTorTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(RusTorTheme.spacing.md),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(RusTorTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RusTorAppIcon(
                    imageUrl = iconUrl,
                    appName = title,
                    size = RusTorAppIconSize.Medium,
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(RusTorTheme.spacing.xs),
                ) {
                    Text(
                        text = title,
                        style = RusTorTheme.typography.titleLarge,
                        maxLines = APP_CARD_TITLE_MAX_LINES,
                        overflow = TextOverflow.Ellipsis,
                    )
                    RusTorCategoryTag(text = category)
                }
            }
            Text(
                text = description,
                style = RusTorTheme.typography.bodyMedium,
                color = RusTorTheme.colors.onSurfaceVariant,
                maxLines = APP_CARD_DESCRIPTION_MAX_LINES,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AppCardCover(coverUrl: String?, coverTitle: String?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(RusTorTheme.colors.surfaceContainerHigh),
    ) {
        if (coverUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(coverUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (coverTitle != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to COVER_SCRIM_COLOR,
                            COVER_SCRIM_END_FRACTION to Color.Transparent,
                        ),
                    ),
            )
            Text(
                text = coverTitle,
                modifier = Modifier
                    .fillMaxWidth(COVER_TITLE_WIDTH_FRACTION)
                    .padding(RusTorTheme.spacing.lg),
                style = RusTorTheme.typography.headlineMedium,
                color = COVER_TITLE_COLOR,
                maxLines = COVER_TITLE_MAX_LINES,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@ThemePreviews
@Composable
private fun RusTorAppCardPreview() {
    RusTorTheme {
        Surface {
            Column(
                modifier = Modifier
                    .width(APP_CARD_PREVIEW_WIDTH)
                    .padding(RusTorTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(RusTorTheme.spacing.lg),
            ) {
                RusTorAppCard(
                    title = "Финансы рядом",
                    category = "Финансы",
                    description = "Удобный контроль расходов, планирование бюджета и аналитика в одном приложении.",
                    iconUrl = null,
                    coverUrl = null,
                    onClick = {},
                )
                RusTorAppCard(
                    title = "GameZone",
                    category = "Игры",
                    description = "Тысячи игр, эксклюзивные подборки и удобная система рекомендаций.",
                    iconUrl = null,
                    coverUrl = null,
                    coverTitle = "Больше, чем просто игры",
                    onClick = {},
                )
            }
        }
    }
}

@Composable
fun RusTorAppCardSkeleton(modifier: Modifier = Modifier, coverAspectRatio: Float = DEFAULT_COVER_ASPECT_RATIO) {
    val tagHeight = with(LocalDensity.current) {
        RusTorTheme.typography.labelMedium.lineHeight.toDp()
    } + RusTorTheme.spacing.xs * 2

    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(RusTorTheme.radius.xl),
        colors = CardDefaults.elevatedCardColors(
            containerColor = RusTorTheme.colors.surfaceContainerLow,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        SkeletonBlock(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(coverAspectRatio),
            shape = RectangleShape,
        )
        Column(
            modifier = Modifier.padding(RusTorTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(RusTorTheme.spacing.md),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(RusTorTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SkeletonBlock(
                    modifier = Modifier.size(RusTorAppIconSize.Medium.size),
                    shape = RoundedCornerShape(RusTorTheme.radius.lg),
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(RusTorTheme.spacing.xs),
                ) {
                    SkeletonTextLine(
                        style = RusTorTheme.typography.titleLarge,
                        modifier = Modifier.width(160.dp),
                    )
                    SkeletonBlock(
                        modifier = Modifier
                            .width(72.dp)
                            .height(tagHeight),
                        shape = RoundedCornerShape(RusTorTheme.radius.full),
                    )
                }
            }
            Column {
                SkeletonTextLine(
                    style = RusTorTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                )
                SkeletonTextLine(
                    style = RusTorTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(COVER_TITLE_WIDTH_FRACTION),
                )
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RusTorAppCardSkeletonPreview() {
    RusTorTheme {
        Surface {
            RusTorSkeleton(modifier = Modifier.width(360.dp)) {
                RusTorAppCardSkeleton(modifier = Modifier.padding(RusTorTheme.spacing.lg))
            }
        }
    }
}
