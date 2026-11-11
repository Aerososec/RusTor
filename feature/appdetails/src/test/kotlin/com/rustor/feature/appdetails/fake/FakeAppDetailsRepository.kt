package com.rustor.feature.appdetails.fake

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
import kotlinx.coroutines.flow.map

internal class FakeAppDetailsRepository(cachedApp: App?) : CatalogRepository {

    private val readResult = MutableStateFlow<Result<App?>>(Result.success(cachedApp))

    val observedAppIds = mutableListOf<AppId>()
    val refreshCalls = mutableListOf<Boolean>()

    var gate: CompletableDeferred<Unit>? = null
    var refreshError: AppException? = null

    var cancelledCalls = 0
        private set

    fun setApp(app: App?) {
        readResult.value = Result.success(app)
    }

    fun failRead(error: AppException) {
        readResult.value = Result.failure(error)
    }

    override fun observeApp(appId: AppId): Flow<App?> {
        observedAppIds += appId

        return readResult.map { result ->
            result.getOrThrow()?.takeIf { it.id == appId }
        }
    }

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

    override fun observeFeed(): Flow<List<App>> = error("AppDetailsViewModel does not observe the feed")

    override fun observeFeedByCategory(categoryId: CategoryId): Flow<List<App>> =
        error("AppDetailsViewModel does not observe a filtered feed")

    override fun observeCategories(): Flow<List<Category>> = error("AppDetailsViewModel does not observe categories")

    override fun observeCategorySummaries(): Flow<List<CategorySummary>> =
        error("AppDetailsViewModel does not observe category summaries")
}
