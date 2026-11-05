package com.rustor.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector

enum class RusTorTab(val label: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    SHOWCASE(
        label = "Витрина",
        icon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home,
    ),
    CATEGORIES(
        label = "Категории",
        icon = Icons.AutoMirrored.Outlined.List,
        selectedIcon = Icons.AutoMirrored.Filled.List,
    ),
    SEARCH(
        label = "Поиск",
        icon = Icons.Outlined.Search,
        selectedIcon = Icons.Filled.Search,
    ),
}
