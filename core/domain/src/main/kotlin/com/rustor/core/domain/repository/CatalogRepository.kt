package com.rustor.core.domain.repository

import com.rustor.core.model.App
import com.rustor.core.model.Category
import com.rustor.core.model.CategoryId
import kotlinx.coroutines.flow.Flow

interface CatalogRepository {
    fun observeFeed(): Flow<List<App>>

    fun observeFeedByCategory(categoryId: CategoryId): Flow<List<App>>

    fun observeCategories(): Flow<List<Category>>

    suspend fun refresh(reshuffle: Boolean)
}
