package com.rustor.feature.showcase.uiState

import com.rustor.core.model.CategoryId

sealed interface ShowcaseIntent {
    data object Refresh : ShowcaseIntent
    data class SelectCategory(val id: CategoryId?) : ShowcaseIntent
    data object Back : ShowcaseIntent
}
