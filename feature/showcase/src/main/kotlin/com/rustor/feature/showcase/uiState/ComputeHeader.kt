package com.rustor.feature.showcase.uiState

import com.rustor.core.model.Category
import com.rustor.core.model.CategoryId

internal fun computeHeader(
    mode: ShowcaseMode,
    categories: List<Category>,
    selectedCategory: CategoryId?,
): ShowcaseHeader = when (mode) {
    ShowcaseMode.Main -> ShowcaseHeader.Main(
        categories = categories,
        selectedCategory = selectedCategory,
    )

    is ShowcaseMode.Category -> ShowcaseHeader.CategoryPage(
        title = categories.firstOrNull { it.id == mode.categoryId }?.title,
    )
}
