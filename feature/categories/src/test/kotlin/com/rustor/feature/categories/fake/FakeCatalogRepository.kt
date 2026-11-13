package com.rustor.feature.categories.fake

import com.rustor.core.common.error.AppException
import com.rustor.core.domain.repository.CatalogRepository
import com.rustor.core.model.App
import com.rustor.core.model.AppId
import com.rustor.core.model.Category
import com.rustor.core.model.CategoryId
import com.rustor.core.model.CategorySummary
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeCatalogRepository : CatalogRepository {

    val categorySummaries = MutableStateFlow<List<CategorySummary>>(emptyList())
    val refreshCalls = mutableListOf<Boolean>()

    var gate: CompletableDeferred<Unit>? = null
    var refreshError: AppException? = null

    var cancelledCalls = 0
        private set

    override fun observeCategorySummaries(): Flow<List<CategorySummary>> = categorySummaries

    override suspend fun refresh(reshuffle: Boolean) {
        refreshCalls += reshuffle

        try {
            gate?.await()
        } catch (error: CancellationException) {
            cancelledCalls++
            throw error
        }

        refreshError?.let { throw it }
    }

    override fun observeFeed(): Flow<List<App>> = error("CategoriesViewModel does not observe the feed")

    override fun observeFeedByCategory(categoryId: CategoryId): Flow<List<App>> =
        error("CategoriesViewModel does not observe a filtered feed")

    override fun observeCategories(): Flow<List<Category>> = error("CategoriesViewModel observes category summaries")

    override fun observeApp(appId: AppId): Flow<App?> = error("CategoriesViewModel does not observe an individual app")

    override fun observePopularApps(): Flow<List<App>> =
        error("Popular apps observation is not supported by this test fake")
}
