package com.rustor.feature.search.fake

import com.rustor.core.common.error.AppException
import com.rustor.core.domain.repository.CatalogRepository
import com.rustor.core.model.App
import com.rustor.core.model.AppId
import com.rustor.core.model.Category
import com.rustor.core.model.CategoryId
import com.rustor.core.model.CategorySummary
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

internal class TestSource<T>(initialValue: T) {

    private val state = MutableStateFlow(Result.success(initialValue))

    var subscriptionGate: CompletableDeferred<Unit>? = null

    var subscriptionCount: Int = 0
        private set

    fun observe(): Flow<T> = flow {
        subscriptionCount++
        subscriptionGate?.await()

        emitAll(
            state.map { result ->
                result.getOrThrow()
            },
        )
    }

    fun emit(value: T) {
        state.value = Result.success(value)
    }

    fun fail(error: AppException) {
        state.value = Result.failure(error)
    }
}

internal class FakeSearchRepository : CatalogRepository {

    val feed = TestSource<List<App>>(emptyList())
    val popular = TestSource<List<App>>(emptyList())
    val categories = TestSource<List<Category>>(emptyList())

    val refreshCalls = mutableListOf<Boolean>()

    var refreshGate: CompletableDeferred<Unit>? = null
    var refreshError: AppException? = null
    var onRefreshSuccess: () -> Unit = {}

    override fun observeFeed(): Flow<List<App>> = feed.observe()

    override fun observePopularApps(): Flow<List<App>> = popular.observe()

    override fun observeCategories(): Flow<List<Category>> = categories.observe()

    override suspend fun refresh(reshuffle: Boolean) {
        refreshCalls += reshuffle
        refreshGate?.await()
        refreshError?.let { throw it }
        onRefreshSuccess()
    }

    override fun observeFeedByCategory(categoryId: CategoryId): Flow<List<App>> = feed.observe().map { apps ->
        apps.filter { it.category.id == categoryId }
    }

    override fun observeApp(appId: AppId): Flow<App?> = feed.observe().map { apps ->
        apps.find { it.id == appId }
    }

    override fun observeCategorySummaries(): Flow<List<CategorySummary>> =
        error("Category summaries are not used by SearchViewModel")
}
