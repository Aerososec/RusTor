package com.rustor.feature.showcase.uiState

import com.rustor.core.model.Category
import com.rustor.core.model.CategoryId

sealed interface ShowcaseHeader {
    data class Main(val categories: List<Category>, val selectedCategory: CategoryId?) : ShowcaseHeader

    data class CategoryPage(val title: String?) : ShowcaseHeader
}
