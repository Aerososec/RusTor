package com.rustor.core.ui.components.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.rustor.core.ui.R
import com.rustor.core.ui.components.chip.RusTorCategoryTag
import com.rustor.core.ui.components.icon.RusTorAppIcon
import com.rustor.core.ui.components.icon.RusTorAppIconSize
import com.rustor.core.ui.theme.RusTorTheme

@Composable
fun RusTorAppDetailsHeader(
    title: String,
    developer: String,
    category: String,
    ageRating: RusTorAgeRating,
    iconUrl: String?,
    modifier: Modifier = Modifier,
) {
    val ageRatingDescription = stringResource(
        R.string.rustor_age_rating,
        ageRating.label,
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(RusTorTheme.spacing.lg),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(RusTorTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RusTorAppIcon(
                imageUrl = iconUrl,
                appName = title,
                size = RusTorAppIconSize.Large,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(RusTorTheme.spacing.xs),
            ) {
                Text(
                    text = title,
                    style = RusTorTheme.typography.headlineMedium,
                    color = RusTorTheme.colors.onSurface,
                    modifier = Modifier.semantics { heading() },
                )

                Text(
                    text = developer,
                    style = RusTorTheme.typography.bodyLarge,
                    color = RusTorTheme.colors.onSurfaceVariant,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(RusTorTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RusTorCategoryTag(text = category)

            Text(
                text = ageRating.label,
                style = RusTorTheme.typography.bodyMedium,
                color = RusTorTheme.colors.onSurfaceVariant,
                modifier = Modifier.clearAndSetSemantics {
                    contentDescription = ageRatingDescription
                },
            )
        }
    }
}

@Composable
fun RusTorAppDescription(description: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(
            RusTorTheme.spacing.md,
        ),
    ) {
        Text(
            text = stringResource(R.string.rustor_about_app),
            style = RusTorTheme.typography.titleLarge,
            color = RusTorTheme.colors.onSurface,
            modifier = Modifier.semantics { heading() },
        )

        SelectionContainer {
            Text(
                text = description,
                style = RusTorTheme.typography.bodyLarge,
                color = RusTorTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun RusTorScreenshotStrip(
    screenshotUrls: List<String>,
    onScreenshotClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(
            RusTorTheme.spacing.md,
        ),
    ) {
        Text(
            text = stringResource(R.string.rustor_screenshots),
            style = RusTorTheme.typography.titleLarge,
            modifier = Modifier
                .padding(horizontal = RusTorTheme.spacing.lg)
                .semantics { heading() },
        )

        if (screenshotUrls.isEmpty()) {
            Text(
                text = stringResource(R.string.rustor_no_screenshots),
                style = RusTorTheme.typography.bodyMedium,
                color = RusTorTheme.colors.onSurfaceVariant,
                modifier = Modifier.padding(
                    horizontal = RusTorTheme.spacing.lg,
                ),
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(
                    horizontal = RusTorTheme.spacing.lg,
                ),
                horizontalArrangement = Arrangement.spacedBy(
                    RusTorTheme.spacing.md,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                itemsIndexed(screenshotUrls) { index, url ->
                    RusTorScreenshotThumbnail(
                        url = url,
                        contentDescription = stringResource(
                            R.string.rustor_screenshot_position,
                            index + 1,
                            screenshotUrls.size,
                        ),
                        onClick = { onScreenshotClick(index) },
                    )
                }
            }
        }
    }
}

// Общий компонент для миниатюр и полноэкранной галереи.
@Composable
internal fun RusTorScreenshotImage(
    url: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    retryKey: Int = 0,
) {
    key(url, retryKey) {
        SubcomposeAsyncImage(
            model = url,
            contentDescription = contentDescription,
            modifier = modifier,
            // Показывает скриншот целиком, включая горизонтальные изображения.
            contentScale = ContentScale.Fit,
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = RusTorTheme.colors.primary,
                    )
                }
            },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(RusTorTheme.spacing.md),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(
                            R.string.rustor_screenshot_error,
                        ),
                        style = RusTorTheme.typography.bodySmall,
                        color = RusTorTheme.colors.onSurfaceVariant,
                    )
                }
            },
        )
    }
}

enum class RusTorAgeRating(val label: String) {
    AGE_0("0+"),
    AGE_6("6+"),
    AGE_8("8+"),
    AGE_12("12+"),
    AGE_16("16+"),
    AGE_18("18+"),
}
