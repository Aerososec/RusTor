package com.rustor.core.ui.components.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.rustor.core.ui.components.chip.RusTorCategoryTag
import com.rustor.core.ui.components.icon.RusTorAppIcon
import com.rustor.core.ui.components.icon.RusTorAppIconSize
import com.rustor.core.ui.theme.RusTorTheme

private const val DEFAULT_ASPECT_RATIO = 4f / 3f

@Composable
fun RusTorSearchAppCard(
    title: String,
    category: String,
    iconUrl: String?,
    coverUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(RusTorTheme.radius.xl),
        colors = CardDefaults.elevatedCardColors(
            containerColor = RusTorTheme.colors.surfaceContainerLow,
            contentColor = RusTorTheme.colors.onSurface,
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 1.dp,
        ),
    ) {
        Surface(
            color = RusTorTheme.colors.surfaceContainerHigh,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(DEFAULT_ASPECT_RATIO),
        ) {
            if (coverUrl.isNullOrBlank()) {
                SearchCoverFallback(title = title)
            } else {
                SubcomposeAsyncImage(
                    model = coverUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                            )
                        }
                    },
                    error = {
                        SearchCoverFallback(title = title)
                    },
                )
            }
        }

        Column(
            modifier = Modifier.padding(RusTorTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(
                RusTorTheme.spacing.sm,
            ),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    RusTorTheme.spacing.sm,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RusTorAppIcon(
                    imageUrl = iconUrl,
                    appName = title,
                    size = RusTorAppIconSize.Small,
                )

                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    style = RusTorTheme.typography.titleSmall,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            RusTorCategoryTag(text = category)
        }
    }
}

@Composable
private fun SearchCoverFallback(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(RusTorTheme.spacing.md),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = RusTorTheme.typography.titleMedium,
            color = RusTorTheme.colors.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
