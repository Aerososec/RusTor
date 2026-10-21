package com.rustor.feature.showcase.uiState

import com.rustor.core.model.CategoryId

sealed interface ShowcaseMode {
    data object Main : ShowcaseMode
    data class Category(val categoryId: CategoryId) : ShowcaseMode
}
