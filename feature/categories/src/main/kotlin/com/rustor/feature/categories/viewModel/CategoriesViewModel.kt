package com.rustor.feature.categories.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rustor.core.common.error.AppException
import com.rustor.core.common.error.toUiError
import com.rustor.core.domain.repository.CatalogRepository
import com.rustor.feature.categories.ui.CategoriesIntent
import com.rustor.feature.categories.ui.CategoriesUiState
import com.rustor.feature.categories.ui.CategorySideEffect
import com.rustor.feature.categories.ui.SyncStatus
import com.rustor.feature.categories.ui.computeCategoriesState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(private val repository: CatalogRepository) : ViewModel() {

    private var syncJob: Job? = null

    private val categoriesSummary = repository.observeCategorySummaries()

    private val syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)

    val uiState: StateFlow<CategoriesUiState> = combine(
        categoriesSummary,
        syncStatus,
    ) { summary, status ->
        computeCategoriesState(
            categories = summary,
            syncStatus = status,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CategoriesUiState.Loading,
    )

    private val _effects = Channel<CategorySideEffect>(Channel.BUFFERED)
    val effects: Flow<CategorySideEffect> = _effects.receiveAsFlow()

    fun onIntent(intent: CategoriesIntent) {
        when (intent) {
            CategoriesIntent.Refresh -> refresh()
        }
    }

    private fun refresh() {
        if (syncJob?.isActive == true) return

        syncStatus.value = SyncStatus.Syncing

        syncJob = viewModelScope.launch {
            try {
                repository.refresh(reshuffle = false)
                syncStatus.value = SyncStatus.Idle
            } catch (error: AppException) {
                syncStatus.value = SyncStatus.Failed(error)

                if (uiState.value is CategoriesUiState.Content) {
                    _effects.send(
                        CategorySideEffect.ShowSnackbarError(
                            error = error.toUiError(),
                        ),
                    )
                }
            }
        }
    }
}
