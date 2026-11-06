package com.rustor.feature.categories.ui

import com.rustor.core.common.error.toUiError
import com.rustor.core.model.CategorySummary

fun computeCategoriesState(categories: List<CategorySummary>?, syncStatus: SyncStatus): CategoriesUiState {
    if (categories == null) return CategoriesUiState.Loading

    if (categories.isNotEmpty()) {
        return CategoriesUiState.Content(categories, isRefreshing = syncStatus == SyncStatus.Syncing)
    }

    return when (syncStatus) {
        is SyncStatus.Failed -> CategoriesUiState.Error(syncStatus.error.toUiError())
        SyncStatus.Idle -> CategoriesUiState.NoCategoriesUi
        SyncStatus.Syncing -> CategoriesUiState.Loading
    }
}
