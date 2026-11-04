package com.rustor.feature.showcase

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.rustor.core.model.AppId
import com.rustor.core.model.CategoryId
import com.rustor.core.ui.components.bar.RusTorCategoryTopBar
import com.rustor.core.ui.components.bar.RusTorMainTopBar
import com.rustor.core.ui.components.card.RusTorAppCard
import com.rustor.core.ui.components.chip.RusTorAssistChip
import com.rustor.core.ui.components.chip.RusTorFilterChip
import com.rustor.core.ui.components.state.RusTorEmptyState
import com.rustor.core.ui.components.state.RusTorErrorState
import com.rustor.core.ui.theme.RusTorTheme
import com.rustor.feature.showcase.error.ShowcaseError
import com.rustor.feature.showcase.ui.showcaseFeed.ShowcaseFeedSkeleton
import com.rustor.feature.showcase.uiState.FeedState
import com.rustor.feature.showcase.uiState.ShowcaseHeader
import com.rustor.feature.showcase.uiState.ShowcaseIntent
import com.rustor.feature.showcase.uiState.ShowcaseMode
import com.rustor.feature.showcase.uiState.ShowcaseSideEffect
import com.rustor.feature.showcase.uiState.ShowcaseUiState
import com.rustor.feature.showcase.viewModel.ShowcaseViewModel

@Composable
fun ShowcaseRoute(
    mode: ShowcaseMode,
    onAppClick: (AppId) -> Unit,
    onSearchClick: () -> Unit,
    onAllCategoriesClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = hiltViewModel<ShowcaseViewModel, ShowcaseViewModel.ShowcaseFactory>(
        creationCallback = { factory -> factory.create(mode) },
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val screenState = uiState
    val appliedCategory = (screenState.header as? ShowcaseHeader.Main)?.selectedCategory
    val pageCount = (screenState.feed as? FeedState.Content)?.apps?.size ?: 0

    val pagerState = key(mode, appliedCategory?.value) {
        rememberPagerState(initialPage = 0) { pageCount }
    }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(viewModel, lifecycleOwner, pagerState, snackbarHostState) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    ShowcaseSideEffect.ScrollToTop -> {
                        if (pagerState.pageCount > 0) {
                            pagerState.scrollToPage(0)
                        }
                    }

                    is ShowcaseSideEffect.ShowSnackbarError -> {
                        snackbarHostState.showSnackbar(
                            "Не удалось обновить. ${effect.error.userMessage()}",
                        )
                    }
                }
            }
        }
    }

    ShowcaseScreen(
        uiState = screenState,
        onIntent = viewModel::onIntent,
        onAppClick = onAppClick,
        onSearchClick = onSearchClick,
        onAllCategoriesClick = onAllCategoriesClick,
        onBackClick = onBackClick,
        snackbarHostState = snackbarHostState,
        pagerState = pagerState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowcaseScreen(
    uiState: ShowcaseUiState,
    onIntent: (ShowcaseIntent) -> Unit,
    onAppClick: (AppId) -> Unit,
    onSearchClick: () -> Unit,
    onAllCategoriesClick: () -> Unit,
    onBackClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    val header = uiState.header
    val feed = uiState.feed
    val refresh = { onIntent(ShowcaseIntent.Refresh) }
    BackHandler(enabled = header is ShowcaseHeader.Main && header.selectedCategory != null) {
        onIntent(ShowcaseIntent.Back)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            when (header) {
                is ShowcaseHeader.Main -> RusTorMainTopBar(
                    title = "Витрина",
                    subtitle = "Открой что-то новое",
                    logo = rememberVectorPainter(Icons.Filled.Star),
                    onSearchClick = onSearchClick,
                )

                is ShowcaseHeader.CategoryPage -> RusTorCategoryTopBar(
                    title = header.title ?: "Категория",
                    onBackClick = onBackClick,
                    onSearchClick = onSearchClick,
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = feed is FeedState.Content && feed.isRefreshing,
            onRefresh = refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (
                    header is ShowcaseHeader.Main &&
                    (feed is FeedState.Content || feed == FeedState.EmptyCategory)
                ) {
                    CategoryChips(
                        header = header,
                        onSelectCategory = { categoryId ->
                            onIntent(ShowcaseIntent.SelectCategory(categoryId))
                        },
                        onAllCategoriesClick = onAllCategoriesClick,
                    )
                }

                key(pagerState) {
                    ShowcaseFeed(
                        feed = feed,
                        pagerState = pagerState,
                        onAppClick = onAppClick,
                        onRefresh = refresh,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun ShowcaseFeed(
    feed: FeedState,
    pagerState: PagerState,
    onAppClick: (AppId) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (feed) {
        is FeedState.Content -> {
            val appIds = feed.apps.map { it.id.value }
            var previousAppIds by remember(pagerState) {
                mutableStateOf(appIds)
            }

            LaunchedEffect(pagerState, appIds) {
                if (appIds != previousAppIds) {
                    previousAppIds = appIds

                    if (appIds.isNotEmpty()) {
                        pagerState.scrollToPage(0)
                    }
                }
            }

            VerticalPager(
                state = pagerState,
                key = { page -> feed.apps[page].id.value },
                modifier = modifier,
            ) { page ->
                val app = feed.apps[page]

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(RusTorTheme.spacing.lg),
                ) {
                    val availableHeight = maxHeight

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .heightIn(min = availableHeight),
                        contentAlignment = BiasAlignment(
                            horizontalBias = 0f,
                            verticalBias = -0.5f,
                        ),
                    ) {
                        RusTorAppCard(
                            title = app.name,
                            category = app.category.title,
                            description = app.summary,
                            iconUrl = app.iconUrl,
                            coverUrl = app.screenshots.firstOrNull(),
                            onClick = { onAppClick(app.id) },
                            coverAspectRatio = 4f / 5f,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }

        else -> Box(
            modifier = modifier,
            contentAlignment = Alignment.Center,
        ) {
            when (feed) {
                FeedState.Loading -> ShowcaseFeedSkeleton()

                FeedState.EmptyCatalog -> RusTorEmptyState(
                    title = "Каталог пока пуст",
                    description = "Попробуй обновить витрину.",
                    actionText = "Обновить",
                    onAction = onRefresh,
                )

                FeedState.EmptyCategory -> RusTorEmptyState(
                    title = "В этой категории пока нет приложений",
                    description = "Обнови витрину или вернись к другим категориям.",
                )

                is FeedState.Error -> RusTorErrorState(
                    title = "Не удалось загрузить витрину",
                    description = feed.error.userMessage(),
                    onRetry = onRefresh,
                )
            }
        }
    }
}

@Composable
private fun CategoryChips(
    header: ShowcaseHeader.Main,
    onSelectCategory: (CategoryId?) -> Unit,
    onAllCategoriesClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = RusTorTheme.spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(RusTorTheme.spacing.sm),
    ) {
        RusTorFilterChip(
            text = "Все",
            selected = header.selectedCategory == null,
            onClick = { onSelectCategory(null) },
        )

        header.categories.forEach { category ->
            RusTorFilterChip(
                text = category.title,
                selected = header.selectedCategory == category.id,
                onClick = { onSelectCategory(category.id) },
            )
        }

        RusTorAssistChip(
            text = "Все категории",
            onClick = onAllCategoriesClick,
            trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
        )
    }
}

private fun ShowcaseError.userMessage(): String = when (this) {
    ShowcaseError.NoConnection -> "Проверь подключение к интернету."
    ShowcaseError.ServiceUnavailable -> "Сервис временно недоступен."
    ShowcaseError.Unknown -> "Попробуй ещё раз."
}
