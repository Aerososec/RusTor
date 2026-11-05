package com.rustor.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.rustor.feature.showcase.ShowcaseRoute
import com.rustor.feature.showcase.uiState.ShowcaseMode

@Composable
fun RusTorNavDisplay(modifier: Modifier = Modifier) {
    var currentTab by rememberSaveable {
        mutableStateOf(RusTorTab.SHOWCASE)
    }

    val showcaseBackStack = rememberNavBackStack(ShowcaseKey)
    val categoriesBackStack = rememberNavBackStack(CategoriesKey)
    val searchBackStack = rememberNavBackStack(SearchKey)

    val provider = entryProvider<NavKey> {
        entry<ShowcaseKey> {
            ShowcaseRoute(
                mode = ShowcaseMode.Main,
                onAppClick = { _ ->
                    // Переход на карточку приложения появится позже.
                },
                onSearchClick = {
                    currentTab = RusTorTab.SEARCH
                },
                onAllCategoriesClick = {
                    currentTab = RusTorTab.CATEGORIES
                },
                onBackClick = {},
            )
        }

        entry<CategoriesKey> {
            CategoriesPlaceholderScreen()
        }

        entry<SearchKey> {
            SearchPlaceholderScreen()
        }
    }

    val showcaseEntries = rememberTabEntries(showcaseBackStack, provider)
    val categoriesEntries = rememberTabEntries(categoriesBackStack, provider)
    val searchEntries = rememberTabEntries(searchBackStack, provider)

    val activeBackStack = when (currentTab) {
        RusTorTab.SHOWCASE -> showcaseBackStack
        RusTorTab.CATEGORIES -> categoriesBackStack
        RusTorTab.SEARCH -> searchBackStack
    }

    val activeEntries = when (currentTab) {
        RusTorTab.SHOWCASE -> showcaseEntries
        RusTorTab.CATEGORIES -> categoriesEntries
        RusTorTab.SEARCH -> searchEntries
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            RusTorBottomBar(
                modifier = Modifier,
                curTab = currentTab,
                onTabClick = { tab -> currentTab = tab },
            )
        },
    ) { innerPadding ->
        NavDisplay(
            entries = activeEntries,
            onBack = {
                if (activeBackStack.size > 1) {
                    activeBackStack.removeLastOrNull()
                }
            },
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        )
    }
}

@Composable
private fun rememberTabEntries(
    backStack: List<NavKey>,
    provider: (NavKey) -> NavEntry<NavKey>,
): List<NavEntry<NavKey>> = rememberDecoratedNavEntries(
    backStack = backStack,
    entryDecorators = listOf(
        rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
        rememberViewModelStoreNavEntryDecorator<NavKey>(),
    ),
    entryProvider = provider,
)

@Composable
fun CategoriesPlaceholderScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "Экран категорий")
    }
}

@Composable
fun SearchPlaceholderScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "Экран поиска")
    }
}
