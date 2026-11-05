package com.rustor.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun RusTorBottomBar(modifier: Modifier, curTab: RusTorTab, onTabClick: (RusTorTab) -> Unit) {
    RusTorNavigationBar(
        modifier = modifier,
    ) {
        RusTorTab.entries.forEach { tab ->
            RusTorNavigationBarItem(
                selected = tab == curTab,
                onClick = { onTabClick(tab) },
                label = tab.label,
                icon = tab.icon,
                selectedIcon = tab.selectedIcon,
                modifier = modifier,
            )
        }
    }
}
