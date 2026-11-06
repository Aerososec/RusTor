package com.rustor.feature.categories.ui

import com.rustor.core.common.error.UiError
import com.rustor.core.model.CategorySummary

sealed interface CategoriesUiState {
    data object Loading : CategoriesUiState

    data object NoCategoriesUi : CategoriesUiState

    data class Error(val e: UiError) : CategoriesUiState

    data class Content(val content: List<CategorySummary>, val isRefreshing: Boolean) : CategoriesUiState
}
