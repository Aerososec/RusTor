package com.rustor.feature.appdetails.fake

import androidx.lifecycle.viewModelScope
import com.rustor.core.model.AgeRating
import com.rustor.core.model.App
import com.rustor.core.model.AppId
import com.rustor.core.model.Category
import com.rustor.core.model.CategoryId
import com.rustor.feature.appdetails.ui.AppDetailsIntent
import com.rustor.feature.appdetails.ui.AppDetailsSideEffect
import com.rustor.feature.appdetails.ui.AppDetailsUiState
import com.rustor.feature.appdetails.viewModel.AppDetailsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule : TestWatcher() {

    private val dispatcher = StandardTestDispatcher()

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

internal val testApp = App(
    id = AppId("example.calculator"),
    name = "Калькулятор",
    iconUrl = null,
    category = Category(CategoryId("tools"), "Инструменты"),
    summary = "Калькулятор для быстрых вычислений",
    description = "Полное описание приложения",
    developer = "Example",
    ageRating = AgeRating.ZERO_PLUS,
    screenshots = emptyList(),
    apkUrl = null,
)

internal class AppDetailsTestSession(
    val repository: FakeAppDetailsRepository,
    private val viewModel: AppDetailsViewModel,
    val effects: List<AppDetailsSideEffect>,
) {
    val state: AppDetailsUiState
        get() = viewModel.uiState.value

    fun refresh() {
        viewModel.onIntent(AppDetailsIntent.Refresh)
    }

    fun assertContent(app: App = testApp, isRefreshing: Boolean = false) {
        assertEquals(
            AppDetailsUiState.Content(app, isRefreshing),
            state,
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
internal suspend fun TestScope.withAppDetails(
    cachedApp: App? = null,
    configure: FakeAppDetailsRepository.() -> Unit = {},
    verify: suspend AppDetailsTestSession.() -> Unit,
) {
    val repository = FakeAppDetailsRepository(cachedApp).apply(configure)
    val viewModel = AppDetailsViewModel(repository, testApp.id.value)
    val effects = mutableListOf<AppDetailsSideEffect>()

    val effectCollection = backgroundScope.launch {
        viewModel.effects.toList(effects)
    }

    try {
        runCurrent()
        val session = AppDetailsTestSession(repository, viewModel, effects)
        session.verify()
    } finally {
        effectCollection.cancel()
        viewModel.viewModelScope.cancel()
        runCurrent()
    }
}
