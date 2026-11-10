package com.rustor.feature.appdetails.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rustor.core.common.error.AppException
import com.rustor.core.common.error.UiError
import com.rustor.core.common.error.toUiError
import com.rustor.core.domain.repository.CatalogRepository
import com.rustor.core.model.App
import com.rustor.core.model.AppId
import com.rustor.feature.appdetails.ui.AppDetailsIntent
import com.rustor.feature.appdetails.ui.AppDetailsSideEffect
import com.rustor.feature.appdetails.ui.AppDetailsUiState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel(
    assistedFactory = AppDetailsViewModel.AppDetailsFactory::class,
)
class AppDetailsViewModel @AssistedInject constructor(
    private val repository: CatalogRepository,
    @Assisted appIdValue: String,
) : ViewModel() {

    private val appId = AppId(appIdValue)

    private var loadJob: Job? = null
    private var isRequestInProgress = false

    private val _uiState =
        MutableStateFlow<AppDetailsUiState>(AppDetailsUiState.Loading)
    val uiState: StateFlow<AppDetailsUiState> = _uiState.asStateFlow()

    private val _effects =
        Channel<AppDetailsSideEffect>(Channel.BUFFERED)
    val effects: Flow<AppDetailsSideEffect> = _effects.receiveAsFlow()

    init {
        loadApp(forceRefresh = false)
    }

    fun onIntent(intent: AppDetailsIntent) {
        when (intent) {
            AppDetailsIntent.Refresh -> loadApp(forceRefresh = true)
        }
    }

    private fun loadApp(forceRefresh: Boolean) {
        if (isRequestInProgress) return

        isRequestInProgress = true
        loadJob?.cancel()

        val previousContent = _uiState.value as? AppDetailsUiState.Content

        _uiState.value = previousContent?.copy(isRefreshing = true)
            ?: AppDetailsUiState.Loading

        loadJob = viewModelScope.launch {
            try {
                loadAndObserve(forceRefresh)
            } catch (error: AppException) {
                handleReadFailure(error, previousContent)
            }
        }
    }

    private suspend fun loadAndObserve(forceRefresh: Boolean) {
        val needsRefresh = if (forceRefresh) {
            true
        } else {
            repository.observeApp(appId).first() == null
        }

        val refreshError = if (needsRefresh) {
            refreshCatalog()
        } else {
            null
        }

        observeApp(refreshError)
    }

    private suspend fun refreshCatalog(): UiError? = try {
        repository.refresh(reshuffle = false)
        null
    } catch (error: AppException) {
        error.toUiError()
    }

    private suspend fun observeApp(refreshError: UiError?) {
        var errorForMissingApp = refreshError
        var isFirstEmission = true

        repository.observeApp(appId).collect { app ->
            val snackbarError = if (isFirstEmission && app != null) {
                refreshError
            } else {
                null
            }
            isFirstEmission = false

            if (app != null) {
                errorForMissingApp = null
            }

            _uiState.value = computeState(app, errorForMissingApp)
            isRequestInProgress = false

            if (snackbarError != null) {
                _effects.send(
                    AppDetailsSideEffect.ShowSnackbarError(error = snackbarError),
                )
            }
        }
    }

    private fun computeState(app: App?, refreshError: UiError?): AppDetailsUiState = when {
        app != null -> AppDetailsUiState.Content(
            app = app,
            isRefreshing = false,
        )

        refreshError != null -> AppDetailsUiState.Error(
            error = refreshError,
        )

        else -> AppDetailsUiState.NotFound
    }

    private suspend fun handleReadFailure(error: AppException, previousContent: AppDetailsUiState.Content?) {
        val uiError = error.toUiError()

        val availableContent =
            _uiState.value as? AppDetailsUiState.Content ?: previousContent

        _uiState.value = availableContent?.copy(isRefreshing = false)
            ?: AppDetailsUiState.Error(uiError)

        isRequestInProgress = false

        if (availableContent != null) {
            _effects.send(
                AppDetailsSideEffect.ShowSnackbarError(uiError),
            )
        }
    }

    @AssistedFactory
    interface AppDetailsFactory {
        fun create(appIdValue: String): AppDetailsViewModel
    }
}
