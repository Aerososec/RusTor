package com.rustor.feature.categories.test

import com.rustor.core.common.error.AppException
import com.rustor.core.common.error.UiError
import com.rustor.core.model.CategoryId
import com.rustor.core.model.CategorySummary
import com.rustor.feature.categories.ui.CategoriesUiState
import com.rustor.feature.categories.ui.SyncStatus
import com.rustor.feature.categories.ui.computeCategoriesState
import org.junit.Assert.assertEquals
import org.junit.Test

class ComputeCategoriesStateTest {

    private val categories = listOf(
        CategorySummary(
            id = CategoryId("tools"),
            title = "Инструменты",
            appCount = 0,
        ),
    )

    private val failed = SyncStatus.Failed(AppException.NoInternet())

    @Test
    fun `missing repository result shows loading for every sync status`() {
        val statuses = listOf(SyncStatus.Idle, SyncStatus.Syncing, failed)

        for (status in statuses) {
            assertEquals(
                "Status: $status",
                CategoriesUiState.Loading,
                computeCategoriesState(categories = null, syncStatus = status),
            )
        }
    }

    @Test
    fun `existing category remains content even when its app count is zero`() {
        assertEquals(
            CategoriesUiState.Content(categories, isRefreshing = false),
            computeCategoriesState(categories, SyncStatus.Idle),
        )
        assertEquals(
            CategoriesUiState.Content(categories, isRefreshing = true),
            computeCategoriesState(categories, SyncStatus.Syncing),
        )
        assertEquals(
            CategoriesUiState.Content(categories, isRefreshing = false),
            computeCategoriesState(categories, failed),
        )
    }

    @Test
    fun `empty category list uses sync status to determine the screen`() {
        assertEquals(
            CategoriesUiState.NoCategoriesUi,
            computeCategoriesState(emptyList(), SyncStatus.Idle),
        )
        assertEquals(
            CategoriesUiState.Loading,
            computeCategoriesState(emptyList(), SyncStatus.Syncing),
        )
        assertEquals(
            CategoriesUiState.Error(UiError.NoConnection),
            computeCategoriesState(emptyList(), failed),
        )
    }
}
