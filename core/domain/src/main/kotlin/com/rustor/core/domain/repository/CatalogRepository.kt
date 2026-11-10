package com.rustor.core.domain.repository

import com.rustor.core.model.App
import com.rustor.core.model.AppId
import com.rustor.core.model.Category
import com.rustor.core.model.CategoryId
import com.rustor.core.model.CategorySummary
import kotlinx.coroutines.flow.Flow

interface CatalogRepository {
    fun observeFeed(): Flow<List<App>>

    fun observeFeedByCategory(categoryId: CategoryId): Flow<List<App>>

    fun observeCategories(): Flow<List<Category>>

    suspend fun refresh(reshuffle: Boolean)

    fun observeCategorySummaries(): Flow<List<CategorySummary>>

    fun observeApp(appId: AppId): Flow<App?>
}
