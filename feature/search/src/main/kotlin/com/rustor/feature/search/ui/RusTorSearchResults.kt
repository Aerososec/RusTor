package com.rustor.feature.search.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.rustor.core.model.App
import com.rustor.core.model.AppId
import com.rustor.core.ui.components.card.RusTorSearchAppCard
import com.rustor.core.ui.theme.RusTorTheme

@Composable
fun RusTorSearchResults(
    apps: List<App>,
    showingPopular: Boolean,
    noResults: Boolean,
    onAppClick: (AppId) -> Unit,
    modifier: Modifier = Modifier,
    gridState: LazyGridState = rememberLazyGridState(),
) {
    val fontScale = LocalDensity.current.fontScale

    LazyVerticalGrid(
        columns = GridCells.Adaptive(
            minSize = if (fontScale >= 1.3f) 240.dp else 156.dp,
        ),
        state = gridState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = RusTorTheme.spacing.lg,
            vertical = RusTorTheme.spacing.md,
        ),
        horizontalArrangement = Arrangement.spacedBy(
            RusTorTheme.spacing.md,
        ),
        verticalArrangement = Arrangement.spacedBy(
            RusTorTheme.spacing.md,
        ),
    ) {
        if (noResults) {
            item(
                key = "no-results",
                span = { GridItemSpan(maxLineSpan) },
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(
                        RusTorTheme.spacing.xs,
                    ),
                ) {
                    Text(
                        text = stringResource(
                            com.rustor.feature.search.R.string.rustor_search_no_results,
                        ),
                        style = RusTorTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(
                            com.rustor.feature.search.R.string.rustor_search_change_query,
                        ),
                        style = RusTorTheme.typography.bodyMedium,
                        color = RusTorTheme.colors.onSurfaceVariant,
                    )
                }
            }
        }

        if (showingPopular && apps.isNotEmpty()) {
            item(
                key = "popular",
                span = { GridItemSpan(maxLineSpan) },
            ) {
                Text(
                    text = stringResource(
                        com.rustor.feature.search.R.string.rustor_search_popular,
                    ),
                    style = RusTorTheme.typography.titleLarge,
                    modifier = Modifier.semantics { heading() },
                )
            }
        }

        if (apps.isEmpty() && !noResults) {
            item(
                key = "empty",
                span = { GridItemSpan(maxLineSpan) },
            ) {
                Text(
                    text = stringResource(
                        com.rustor.feature.search.R.string.rustor_search_empty,
                    ),
                    style = RusTorTheme.typography.bodyMedium,
                    color = RusTorTheme.colors.onSurfaceVariant,
                )
            }
        }

        items(
            items = apps,
            key = { "app:${it.id}" },
        ) { app ->
            RusTorSearchAppCard(
                title = app.name,
                category = app.category.title,
                iconUrl = app.iconUrl,
                coverUrl = app.iconUrl,
                onClick = { onAppClick(app.id) },
            )
        }
    }
}
