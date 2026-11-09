package com.rustor.feature.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.rustor.core.common.error.UiError
import com.rustor.core.model.CategoryId
import com.rustor.core.model.CategorySummary
import com.rustor.core.ui.components.bar.RusTorCategoryTopBar
import com.rustor.core.ui.components.card.RusTorCategoryCard
import com.rustor.core.ui.components.state.RusTorEmptyState
import com.rustor.core.ui.components.state.RusTorErrorState
import com.rustor.core.ui.components.state.RusTorLoadingState
import com.rustor.core.ui.theme.RusTorTheme
import com.rustor.feature.categories.ui.CategoriesIntent
import com.rustor.feature.categories.ui.CategoriesUiState
import com.rustor.feature.categories.ui.CategorySideEffect
import com.rustor.feature.categories.viewModel.CategoriesViewModel

@Composable
fun CategoriesRoute(
    onCategoryClick: (CategoryId) -> Unit,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = hiltViewModel<CategoriesViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(viewModel, lifecycleOwner, snackbarHostState) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is CategorySideEffect.ShowSnackbarError -> {
                        snackbarHostState.showSnackbar(
                            message = "Не удалось обновить категории. " +
                                effect.error.userMessage(),
                        )
                    }
                }
            }
        }
    }

    CategoriesScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onCategoryClick = onCategoryClick,
        onBackClick = onBackClick,
        onSearchClick = onSearchClick,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    uiState: CategoriesUiState,
    onIntent: (CategoriesIntent) -> Unit,
    onCategoryClick: (CategoryId) -> Unit,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val onBack = onBackClick
    val onRefresh: () -> Unit = {
        onIntent(CategoriesIntent.Refresh)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            RusTorCategoryTopBar(
                title = stringResource(R.string.categories_title),
                onSearchClick = onSearchClick,
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState is CategoriesUiState.Content &&
                uiState.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            CategoriesContent(
                uiState = uiState,
                onRefresh = onRefresh,
                onCategoryClick = onCategoryClick,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun CategoriesContent(
    uiState: CategoriesUiState,
    onRefresh: () -> Unit,
    onCategoryClick: (CategoryId) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        CategoriesUiState.Loading -> {
            RusTorLoadingState(
                modifier = modifier.verticalScroll(rememberScrollState()),
                text = stringResource(R.string.categories_loading),
            )
        }

        CategoriesUiState.NoCategoriesUi -> {
            RusTorEmptyState(
                title = stringResource(R.string.categories_empty_title),
                description = stringResource(
                    R.string.categories_empty_description,
                ),
                actionText = stringResource(R.string.categories_refresh),
                onAction = onRefresh,
                modifier = modifier,
            )
        }

        is CategoriesUiState.Error -> {
            RusTorErrorState(
                title = stringResource(R.string.categories_error_title),
                description = uiState.e.userMessage(),
                actionText = stringResource(R.string.categories_retry),
                onRetry = onRefresh,
                modifier = modifier,
            )
        }

        is CategoriesUiState.Content -> {
            CategoriesList(
                categories = uiState.content,
                onCategoryClick = onCategoryClick,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun CategoriesList(
    categories: List<CategorySummary>,
    onCategoryClick: (CategoryId) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(RusTorTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(
            RusTorTheme.spacing.md,
        ),
    ) {
        items(
            items = categories,
            key = { it.id.value },
        ) { category ->
            RusTorCategoryCard(
                title = category.title,
                appCount = category.appCount,
                icon = Icons.Filled.Star,
                onClick = {
                    onCategoryClick(category.id)
                },
            )
        }
    }
}

private fun UiError.userMessage(): String = when (this) {
    UiError.NoConnection -> "Проверь подключение к интернету."
    UiError.ServiceUnavailable -> "Сервис временно недоступен."
    UiError.Unknown -> "Попробуй ещё раз."
}
