package com.rustor.feature.showcase.fake

import com.rustor.core.common.error.AppException
import com.rustor.core.domain.repository.CatalogRepository
import com.rustor.core.model.App
import com.rustor.core.model.Category
import com.rustor.core.model.CategoryId
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.cancellation.CancellationException

internal class FakeCatalogRepository : CatalogRepository {

    val feed = MutableStateFlow<List<App>>(emptyList())
    val categories = MutableStateFlow<List<Category>>(emptyList())

    /** Если задан, refresh бросит эту ошибку. */
    var refreshError: AppException? = null

    /** Если задан, refresh «висит», пока тест не вызовет gate.complete(Unit). */
    var gate: CompletableDeferred<Unit>? = null

    /** Значение reshuffle для каждого вызова refresh, по порядку. */
    val refreshCalls = mutableListOf<Boolean>()

    var cancelledCalls = 0
        private set

    override fun observeFeed(): Flow<List<App>> = feed

    override fun observeFeedByCategory(categoryId: CategoryId): Flow<List<App>> =
        feed.map { apps -> apps.filter { it.category.id == categoryId } }

    override fun observeCategories(): Flow<List<Category>> = categories

    override suspend fun refresh(reshuffle: Boolean) {
        refreshCalls += reshuffle
        try {
            gate?.await()
        } catch (e: CancellationException) {
            cancelledCalls++
            throw e
        }
        refreshError?.let { throw it }
    }
}