package com.rustor.feature.appdetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.rustor.core.model.AgeRating
import com.rustor.core.model.App
import com.rustor.core.model.AppId
import com.rustor.core.ui.components.bar.RusTorCategoryTopBar
import com.rustor.core.ui.components.button.RusTorButton
import com.rustor.core.ui.components.details.RusTorAgeRating
import com.rustor.core.ui.components.details.RusTorAppDescription
import com.rustor.core.ui.components.details.RusTorAppDetailsHeader
import com.rustor.core.ui.components.details.RusTorScreenshotStrip
import com.rustor.core.ui.components.state.RusTorEmptyState
import com.rustor.core.ui.components.state.RusTorErrorState
import com.rustor.core.ui.components.state.RusTorLoadingState
import com.rustor.core.ui.theme.RusTorTheme
import com.rustor.feature.appdetails.ui.AppDetailsIntent
import com.rustor.feature.appdetails.ui.AppDetailsSideEffect
import com.rustor.feature.appdetails.ui.AppDetailsUiState
import com.rustor.feature.appdetails.viewModel.AppDetailsViewModel

@Composable
fun AppDetailsRoute(
    appId: AppId,
    onBackClick: () -> Unit,
    onScreenshotClick: (List<String>, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel =
        hiltViewModel<AppDetailsViewModel, AppDetailsViewModel.AppDetailsFactory>(
            creationCallback = { factory ->
                factory.create(appId.value)
            },
        )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(viewModel, lifecycleOwner, snackbarHostState) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is AppDetailsSideEffect.ShowSnackbarError -> {
                        snackbarHostState.showSnackbar(
                            message = "Не удалось обновить приложение. " +
                                effect.error.userMessage(),
                        )
                    }
                }
            }
        }
    }

    AppDetailsScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onBackClick = onBackClick,
        onScreenshotClick = onScreenshotClick,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailsScreen(
    uiState: AppDetailsUiState,
    onIntent: (AppDetailsIntent) -> Unit,
    onBackClick: () -> Unit,
    onScreenshotClick: (List<String>, Int) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val onRefresh: () -> Unit = {
        onIntent(AppDetailsIntent.Refresh)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            RusTorCategoryTopBar(
                title = stringResource(R.string.app_details_title),
                onBackClick = onBackClick,
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState is AppDetailsUiState.Content &&
                uiState.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            AppDetailsBody(
                uiState = uiState,
                onRefresh = onRefresh,
                onScreenshotClick = onScreenshotClick,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun AppDetailsBody(
    uiState: AppDetailsUiState,
    onRefresh: () -> Unit,
    onScreenshotClick: (List<String>, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        AppDetailsUiState.Loading -> {
            RusTorLoadingState(
                text = stringResource(R.string.app_details_loading),
                modifier = modifier.verticalScroll(rememberScrollState()),
            )
        }

        AppDetailsUiState.NotFound -> {
            RusTorEmptyState(
                title = stringResource(R.string.app_details_not_found_title),
                description = stringResource(
                    R.string.app_details_not_found_description,
                ),
                actionText = stringResource(R.string.app_details_refresh),
                onAction = onRefresh,
                modifier = modifier,
            )
        }

        is AppDetailsUiState.Error -> {
            RusTorErrorState(
                title = stringResource(R.string.app_details_error_title),
                description = uiState.error.userMessage(),
                actionText = stringResource(R.string.app_details_retry),
                onRetry = onRefresh,
                modifier = modifier,
            )
        }

        is AppDetailsUiState.Content -> {
            AppDetailsContent(
                app = uiState.app,
                onScreenshotClick = onScreenshotClick,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun AppDetailsContent(
    app: App,
    onScreenshotClick: (List<String>, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            vertical = RusTorTheme.spacing.lg,
        ),
        verticalArrangement = Arrangement.spacedBy(
            RusTorTheme.spacing.xl,
        ),
    ) {
        item(key = "header") {
            RusTorAppDetailsHeader(
                title = app.name,
                developer = app.developer,
                category = app.category.title,
                ageRating = app.ageRating.toUiAgeRating(),
                iconUrl = app.iconUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = RusTorTheme.spacing.lg),
            )
        }

        item(key = "install") {
            RusTorButton(
                text = stringResource(com.rustor.core.ui.R.string.rustor_install),
                onClick = {},
                enabled = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = RusTorTheme.spacing.lg),
            )
        }

        item(key = "screenshots") {
            RusTorScreenshotStrip(
                screenshotUrls = app.screenshots,
                onScreenshotClick = { index ->
                    onScreenshotClick(
                        app.screenshots.toList(),
                        index,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        item(key = "description") {
            RusTorAppDescription(
                description = app.description,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = RusTorTheme.spacing.lg),
            )
        }
    }
}

private fun AgeRating.toUiAgeRating(): RusTorAgeRating = when (this) {
    AgeRating.ZERO_PLUS -> RusTorAgeRating.AGE_0
    AgeRating.SIX_PLUS -> RusTorAgeRating.AGE_6
    AgeRating.EIGHT_PLUS -> RusTorAgeRating.AGE_8
    AgeRating.TWELVE_PLUS -> RusTorAgeRating.AGE_12
    AgeRating.SIXTEEN_PLUS -> RusTorAgeRating.AGE_16
    AgeRating.EIGHTEEN_PLUS -> RusTorAgeRating.AGE_18
}

private fun UiError.userMessage(): String = when (this) {
    UiError.NoConnection -> "Проверь подключение к интернету."
    UiError.ServiceUnavailable -> "Сервис временно недоступен."
    UiError.Unknown -> "Попробуй ещё раз."
}
