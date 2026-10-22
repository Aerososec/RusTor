package com.rustor.feature.showcase

import com.rustor.core.common.error.AppException
import com.rustor.core.model.AgeRating
import com.rustor.core.model.App
import com.rustor.core.model.AppId
import com.rustor.core.model.Category
import com.rustor.core.model.CategoryId
import com.rustor.feature.showcase.error.ShowcaseError
import com.rustor.feature.showcase.uiState.FeedState
import com.rustor.feature.showcase.uiState.ShowcaseMode
import com.rustor.feature.showcase.uiState.SyncStatus
import com.rustor.feature.showcase.uiState.SyncTrigger
import com.rustor.feature.showcase.uiState.computeFeedState
import org.junit.Test
import kotlin.test.assertEquals

class ComputeFeedStateTest {

    private val category = Category(id = CategoryId("games"), title = "games")

    private val allStatuses = listOf(
        SyncStatus.Idle,
        SyncStatus.Syncing(SyncTrigger.User),
        SyncStatus.Syncing(SyncTrigger.Background),
        SyncStatus.Failed(AppException.NoInternet()),
    )

    @Test
    fun `null apps give loading for any sync status`() {
        allStatuses.forEach { status ->
            val result = computeFeedState(
                apps = null,
                syncStatus = status,
                mode = ShowcaseMode.Main,
                selectedCategory = null,
            )

            assertEquals(FeedState.Loading, result)
        }
    }

    @Test
    fun `apps with user sync give refreshing content`() {
        val listApps = apps("a", "b", "c")
        val result = computeFeedState(
            apps = listApps,
            syncStatus = SyncStatus.Syncing(SyncTrigger.User),
            mode = ShowcaseMode.Main,
            selectedCategory = null,
        )

        assertEquals(FeedState.Content(listApps, true), result)
    }

    @Test
    fun `apps without user sync give content without refreshing`() {
        val listApps = apps("a", "b", "c")
        allStatuses.forEach { status ->
            if (status != SyncStatus.Syncing(SyncTrigger.User)) {
                val result = computeFeedState(
                    apps = listApps,
                    syncStatus = status,
                    mode = ShowcaseMode.Main,
                    selectedCategory = null,
                )
                assertEquals(FeedState.Content(listApps, false), result)
            }
        }
    }

    @Test
    fun `empty filtered feed gives empty category for any sync status`() {
        allStatuses.forEach { status ->
            val withChip = computeFeedState(
                apps = emptyList(),
                syncStatus = status,
                mode = ShowcaseMode.Main,
                selectedCategory = CategoryId("games"),
            )
            val onCategoryPage = computeFeedState(
                apps = emptyList(),
                syncStatus = status,
                mode = ShowcaseMode.Category(CategoryId("games")),
                selectedCategory = null,
            )

            assertEquals(FeedState.EmptyCategory, withChip)
            assertEquals(FeedState.EmptyCategory, onCategoryPage)
        }
    }

    @Test
    fun `empty unfiltered feed while syncing gives loading`() {
        val listApps = emptyList<App>()
        val resultOne = computeFeedState(
            apps = listApps,
            syncStatus = SyncStatus.Syncing(SyncTrigger.User),
            mode = ShowcaseMode.Main,
            selectedCategory = null,
        )

        val resultTwo = computeFeedState(
            apps = listApps,
            syncStatus = SyncStatus.Syncing(SyncTrigger.Background),
            mode = ShowcaseMode.Main,
            selectedCategory = null,
        )

        assertEquals(FeedState.Loading, resultOne)
        assertEquals(FeedState.Loading, resultTwo)
    }

    @Test
    fun `empty unfiltered feed after failure gives error`() {
        val result = computeFeedState(
            apps = emptyList(),
            syncStatus = SyncStatus.Failed(AppException.Unknown()),
            mode = ShowcaseMode.Main,
            selectedCategory = null,
        )

        assertEquals(FeedState.Error(ShowcaseError.Unknown), result)
    }

    @Test
    fun `empty unfiltered feed when idle gives empty catalog`() {
        val result = computeFeedState(
            apps = emptyList(),
            syncStatus = SyncStatus.Idle,
            mode = ShowcaseMode.Main,
            selectedCategory = null,
        )

        assertEquals(FeedState.EmptyCatalog, result)
    }

    private fun apps(vararg ids: String): List<App> = ids.map { id -> app(id) }

    private fun app(id: String) = App(
        id = AppId(id),
        name = "example",
        iconUrl = null,
        category = category,
        summary = "",
        description = "",
        developer = "",
        ageRating = AgeRating.SIXTEEN_PLUS,
        screenshots = emptyList(),
        apkUrl = null,
    )
}
