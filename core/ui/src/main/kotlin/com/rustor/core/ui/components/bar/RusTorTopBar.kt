package com.rustor.core.ui.components.bar

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rustor.core.ui.R
import com.rustor.core.ui.components.annotation.ThemePreviews
import com.rustor.core.ui.theme.RusTorTheme

private val LogoSize = 40.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RusTorMainTopBar(
    title: String,
    logo: Painter,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onSearchClick: (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(RusTorTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = logo,
                    contentDescription = null,
                    modifier = Modifier
                        .size(LogoSize)
                        .clip(RoundedCornerShape(RusTorTheme.radius.md)),
                )
                Column {
                    Text(
                        text = title,
                        style = RusTorTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = RusTorTheme.typography.bodySmall,
                            color = RusTorTheme.colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        },
        actions = {
            if (onSearchClick != null) {
                SearchAction(onClick = onSearchClick)
            }
        },
        colors = rusTorTopBarColors(),
        scrollBehavior = scrollBehavior,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RusTorCategoryTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSearchClick: (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = RusTorTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.rustor_action_back),
                )
            }
        },
        actions = {
            if (onSearchClick != null) {
                SearchAction(onClick = onSearchClick)
            }
        },
        colors = rusTorTopBarColors(),
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun SearchAction(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = stringResource(R.string.rustor_action_search),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rusTorTopBarColors(): TopAppBarColors = TopAppBarDefaults.topAppBarColors(
    containerColor = RusTorTheme.colors.surface,
    scrolledContainerColor = RusTorTheme.colors.surfaceContainer,
    navigationIconContentColor = RusTorTheme.colors.onSurface,
    titleContentColor = RusTorTheme.colors.onSurface,
    actionIconContentColor = RusTorTheme.colors.primary,
)

@OptIn(ExperimentalMaterial3Api::class)
@ThemePreviews
@Composable
private fun RusTorMainTopBarPreview() {
    RusTorTheme {
        RusTorMainTopBar(
            title = "Витрина",
            subtitle = "Открой что-то новое",
            logo = rememberVectorPainter(Icons.Filled.Star),
            onSearchClick = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@ThemePreviews
@Composable
private fun RusTorCategoryTopBarPreview() {
    RusTorTheme {
        RusTorCategoryTopBar(
            title = "Финансы",
            onBackClick = {},
            onSearchClick = {},
        )
    }
}
