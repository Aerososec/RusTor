package com.rustor.core.ui.components.bar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rustor.core.ui.components.annotation.ThemePreviews
import com.rustor.core.ui.theme.RusTorTheme

@Composable
fun RusTorNavigationBar(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    NavigationBar(
        modifier = modifier,
        containerColor = RusTorTheme.colors.surfaceContainerLow,
        contentColor = RusTorTheme.colors.onSurfaceVariant,
        tonalElevation = 0.dp,
        content = content,
    )
}

@Composable
fun RowScope.RusTorNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    icon: ImageVector,
    selectedIcon: ImageVector,
    modifier: Modifier = Modifier,
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        icon = {
            Icon(
                imageVector = if (selected) selectedIcon else icon,
                contentDescription = null,
            )
        },
        label = {
            Text(
                text = label,
                style = RusTorTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        alwaysShowLabel = true,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = RusTorTheme.colors.primary,
            selectedTextColor = RusTorTheme.colors.primary,
            unselectedIconColor = RusTorTheme.colors.onSurfaceVariant,
            unselectedTextColor = RusTorTheme.colors.onSurfaceVariant,
            indicatorColor = Color.Transparent,
        ),
    )
}

@ThemePreviews
@Composable
private fun RusTorNavigationBarPreview() {
    var selectedIndex by remember { mutableIntStateOf(0) }

    RusTorTheme {
        RusTorNavigationBar {
            RusTorNavigationBarItem(
                selected = selectedIndex == 0,
                onClick = { selectedIndex = 0 },
                label = "Витрина",
                icon = Icons.Outlined.Home,
                selectedIcon = Icons.Filled.Home,
            )
            RusTorNavigationBarItem(
                selected = selectedIndex == 1,
                onClick = { selectedIndex = 1 },
                label = "Поиск",
                icon = Icons.Outlined.Search,
                selectedIcon = Icons.Filled.Search,
            )
            RusTorNavigationBarItem(
                selected = selectedIndex == 2,
                onClick = { selectedIndex = 2 },
                label = "Категории",
                icon = Icons.AutoMirrored.Outlined.List,
                selectedIcon = Icons.AutoMirrored.Filled.List,
            )
        }
    }
}
