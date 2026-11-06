package com.rustor.feature.showcase.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rustor.core.common.error.AppException
import com.rustor.core.common.error.toUiError
import com.rustor.core.domain.repository.CatalogRepository
import com.rustor.core.model.App
import com.rustor.core.model.CategoryId
import com.rustor.feature.showcase.uiState.FeedState
import com.rustor.feature.showcase.uiState.ShowcaseIntent
import com.rustor.feature.showcase.uiState.ShowcaseMode
import com.rustor.feature.showcase.uiState.ShowcaseSideEffect
import com.rustor.feature.showcase.uiState.ShowcaseUiState
import com.rustor.feature.showcase.uiState.SyncStatus
import com.rustor.feature.showcase.uiState.SyncTrigger
import com.rustor.feature.showcase.uiState.computeFeedState
import com.rustor.feature.showcase.uiState.computeHeader
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = ShowcaseViewModel.ShowcaseFactory::class)
internal class ShowcaseViewModel @AssistedInject constructor(
    private val catalogRepository: CatalogRepository,
    private val savedStateHandle: SavedStateHandle,
    @Assisted private val mode: ShowcaseMode,
) : ViewModel() {

    private var syncJob: Job? = null

    private val syncStatus: MutableStateFlow<SyncStatus> = MutableStateFlow(
        when (mode) {
            ShowcaseMode.Main -> SyncStatus.Syncing(SyncTrigger.Background)
            is ShowcaseMode.Category -> SyncStatus.Idle
        },
    )

    private val categories = catalogRepository.observeCategories()

    private data class FeedResult(val categoryId: CategoryId?, val apps: List<App>)

    private val selectedCategory =
        savedStateHandle.getStateFlow<String?>(SELECTED_CATEGORY_KEY, null)

    private val feedResults: Flow<FeedResult> = when (mode) {
        ShowcaseMode.Main -> selectedCategory.flatMapLatest { value ->
            observeFeedResult(value?.let(::CategoryId))
        }

        is ShowcaseMode.Category -> observeFeedResult(mode.categoryId)
    }

    private fun observeFeedResult(categoryId: CategoryId?): Flow<FeedResult> = observeFeedFor(categoryId).map { apps ->
        FeedResult(categoryId = categoryId, apps = apps)
    }

    val uiState: StateFlow<ShowcaseUiState> = combine(
        syncStatus,
        categories,
        feedResults,
    ) { status, categories, result ->
        ShowcaseUiState(
            header = computeHeader(
                mode = mode,
                categories = categories,
                selectedCategory = result.categoryId,
            ),
            feed = computeFeedState(
                apps = result.apps,
                syncStatus = status,
                mode = mode,
                selectedCategory = result.categoryId,
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ShowcaseUiState(
            header = computeHeader(
                mode = mode,
                categories = emptyList(),
                selectedCategory = selectedCategory.value?.let(::CategoryId),
            ),
            feed = FeedState.Loading,
        ),
    )

    private val _effects = Channel<ShowcaseSideEffect>(Channel.BUFFERED)
    val effects: Flow<ShowcaseSideEffect> = _effects.receiveAsFlow()

    init {
        if (mode is ShowcaseMode.Main) {
            startSync(trigger = SyncTrigger.Background)
        }
    }

    fun onIntent(intent: ShowcaseIntent) = when (intent) {
        ShowcaseIntent.Refresh -> {
            refresh()
        }

        is ShowcaseIntent.SelectCategory -> {
            selectCategory(intent.id)
        }

        ShowcaseIntent.Back -> {
            selectCategory(null)
        }
    }

    private fun refresh() {
        if (syncStatus.value == SyncStatus.Syncing(SyncTrigger.User)) return
        syncJob?.cancel()

        startSync(SyncTrigger.User)
    }

    private fun selectCategory(id: CategoryId?) {
        savedStateHandle[SELECTED_CATEGORY_KEY] = id?.value
    }

    private fun observeFeedFor(id: CategoryId?): Flow<List<App>> = if (id == null) {
        catalogRepository.observeFeed()
    } else {
        catalogRepository.observeFeedByCategory(categoryId = id)
    }

    private fun startSync(trigger: SyncTrigger) {
        syncStatus.value = SyncStatus.Syncing(trigger)
        val reshuffle = when (trigger) {
            SyncTrigger.Background -> false
            SyncTrigger.User -> true
        }

        syncJob = viewModelScope.launch {
            try {
                catalogRepository.refresh(reshuffle)
                syncStatus.value = SyncStatus.Idle
                if (trigger == SyncTrigger.User) _effects.send(ShowcaseSideEffect.ScrollToTop)
            } catch (e: AppException) {
                syncStatus.value = SyncStatus.Failed(e)
                if (trigger == SyncTrigger.User && shouldShowErrorAsSnackbar()) {
                    _effects.send(ShowcaseSideEffect.ShowSnackbarError(e.toUiError()))
                }
            }
        }
    }

    private fun shouldShowErrorAsSnackbar(): Boolean =
        uiState.value.feed is FeedState.Content || uiState.value.feed is FeedState.EmptyCategory

    @AssistedFactory
    interface ShowcaseFactory {
        fun create(mode: ShowcaseMode): ShowcaseViewModel
    }

    private companion object {
        const val SELECTED_CATEGORY_KEY = "selected_category"
    }
}
