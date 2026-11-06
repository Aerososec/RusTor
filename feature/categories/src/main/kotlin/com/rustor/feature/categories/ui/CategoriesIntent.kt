package com.rustor.feature.categories.ui

sealed interface CategoriesIntent {
    data object Refresh : CategoriesIntent
}
