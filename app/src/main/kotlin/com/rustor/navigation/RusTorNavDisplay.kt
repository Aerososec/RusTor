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
import com.rustor.core.model.AppId
import com.rustor.core.model.CategoryId
import com.rustor.core.ui.components.details.RusTorScreenshotViewer
import com.rustor.feature.appdetails.AppDetailsRoute
import com.rustor.feature.categories.CategoriesRoute
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

    val onTabSelected: (RusTorTab) -> Unit = { tab ->
        currentTab = tab
    }

    val showcaseEntries = rememberTabEntries(
        backStack = showcaseBackStack,
        onTabSelected = onTabSelected,
    )
    val categoriesEntries = rememberTabEntries(
        backStack = categoriesBackStack,
        onTabSelected = onTabSelected,
    )
    val searchEntries = rememberTabEntries(
        backStack = searchBackStack,
        onTabSelected = onTabSelected,
    )

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
            if (activeBackStack.lastOrNull() !is ScreenshotsKey) {
                RusTorBottomBar(
                    curTab = currentTab,
                    onTabClick = onTabSelected,
                    modifier = modifier,
                )
            }
        },
    ) { innerPadding ->
        NavDisplay(
            entries = activeEntries,
            onBack = {
                activeBackStack.popBackStack()
            },
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        )
    }
}

private fun createEntryProvider(
    backStack: MutableList<NavKey>,
    onTabSelected: (RusTorTab) -> Unit,
): (NavKey) -> NavEntry<NavKey> = entryProvider<NavKey> {
    entry<ShowcaseKey> {
        ShowcaseRoute(
            mode = ShowcaseMode.Main,
            onAppClick = { appId ->
                backStack.add(AppDetailsKey(appId.value))
            },
            onSearchClick = {
                onTabSelected(RusTorTab.SEARCH)
            },
            onAllCategoriesClick = {
                onTabSelected(RusTorTab.CATEGORIES)
            },
            onBackClick = {},
        )
    }

    entry<CategoriesKey> {
        CategoriesRoute(
            onCategoryClick = { categoryId ->
                backStack.add(CategoryShowcaseKey(categoryId.value))
            },
            onBackClick = {
                backStack.popBackStack()
            },
            onSearchClick = {
                onTabSelected(RusTorTab.SEARCH)
            },
        )
    }

    entry<CategoryShowcaseKey> { key ->
        ShowcaseRoute(
            mode = ShowcaseMode.Category(CategoryId(key.categoryId)),
            onAppClick = { appId ->
                backStack.add(AppDetailsKey(appId.value))
            },
            onSearchClick = {
                onTabSelected(RusTorTab.SEARCH)
            },
            onAllCategoriesClick = {
                onTabSelected(RusTorTab.CATEGORIES)
            },
            onBackClick = {
                backStack.popBackStack()
            },
        )
    }

    entry<SearchKey> {
        SearchPlaceholderScreen()
    }

    entry<AppDetailsKey> { key ->
        AppDetailsRoute(
            appId = AppId(key.appId),
            onBackClick = {
                backStack.popBackStack()
            },
            onScreenshotClick = { urls, index ->
                backStack.add(
                    ScreenshotsKey(
                        screenshotUrls = urls.toList(),
                        initialIndex = index,
                    ),
                )
            },
        )
    }

    entry<ScreenshotsKey> { key ->
        RusTorScreenshotViewer(
            screenshotUrls = key.screenshotUrls,
            initialIndex = key.initialIndex,
            onBackClick = {
                backStack.popBackStack()
            },
        )
    }
}

@Composable
private fun rememberTabEntries(
    backStack: MutableList<NavKey>,
    onTabSelected: (RusTorTab) -> Unit,
): List<NavEntry<NavKey>> {
    val provider = createEntryProvider(
        backStack = backStack,
        onTabSelected = onTabSelected,
    )

    return rememberDecoratedNavEntries(
        backStack = backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
            rememberViewModelStoreNavEntryDecorator<NavKey>(),
        ),
        entryProvider = provider,
    )
}

private fun MutableList<NavKey>.popBackStack() {
    if (size > 1) {
        removeLastOrNull()
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
