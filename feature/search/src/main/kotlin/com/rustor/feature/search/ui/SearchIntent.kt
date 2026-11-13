package com.rustor.feature.search.ui

import com.rustor.core.model.CategoryId

sealed interface SearchIntent {
    data class QueryChanged(val query: String) : SearchIntent

    data class CategorySelected(val categoryId: CategoryId?) : SearchIntent

    data object SearchResultRefresh : SearchIntent

    data object Retry : SearchIntent
}
