package com.rustor.feature.showcase

import android.content.Context
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rustor.core.common.error.UiError
import com.rustor.core.model.AgeRating
import com.rustor.core.model.App
import com.rustor.core.model.AppId
import com.rustor.core.model.Category
import com.rustor.core.model.CategoryId
import com.rustor.core.ui.theme.RusTorTheme
import com.rustor.feature.showcase.uiState.FeedState
import com.rustor.feature.showcase.uiState.ShowcaseHeader
import com.rustor.feature.showcase.uiState.ShowcaseIntent
import com.rustor.feature.showcase.uiState.ShowcaseUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.rustor.feature.showcase.R as ShowcaseR
import androidx.compose.ui.test.performClick
import com.rustor.core.ui.R as CoreUiR
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.runtime.State

@RunWith(AndroidJUnit4::class)
class ShowcasePullToRefreshTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    private val receivedIntents = mutableListOf<ShowcaseIntent>()

    private val category = Category(
        id = CategoryId("tools"),
        title = "Инструменты",
    )

    private val firstApp = App(
        id = AppId("com.rustor.test.first"),
        name = "Первое тестовое приложение",
        iconUrl = null,
        category = category,
        summary = "Приложение для проверки жеста обновления",
        description = "Описание тестового приложения",
        developer = "Test Developer",
        ageRating = AgeRating.ZERO_PLUS,
        screenshots = emptyList(),
        apkUrl = null,
    )

    private val secondApp = firstApp.copy(
        id = AppId("com.rustor.test.second"),
        name = "Второе тестовое приложение",
    )

    @Test
    fun content_swipeDown_sendsRefreshIntent() {
        showScreen(
            FeedState.Content(
                apps = listOf(firstApp, secondApp),
                isRefreshing = false,
            ),
        )

        composeRule
            .onNodeWithTag(SHOWCASE_FEED_TAG)
            .assertIsDisplayed()

        swipeDownAndAssertRefresh()
    }

    @Test
    fun emptyCatalog_swipeDown_sendsRefreshIntent() {
        showScreen(FeedState.EmptyCatalog)

        val emptyTitle = context.getString(
            ShowcaseR.string.showcase_empty_title,
        )

        composeRule
            .onNodeWithText(emptyTitle)
            .assertIsDisplayed()

        swipeDownAndAssertRefresh()
    }

    @Test
    fun error_swipeDown_sendsRefreshIntent() {
        showScreen(FeedState.Error(UiError.NoConnection))

        val errorTitle = context.getString(
            ShowcaseR.string.showcase_error_title,
        )

        composeRule
            .onNodeWithText(errorTitle)
            .assertIsDisplayed()

        swipeDownAndAssertRefresh()
    }

    @Test
    fun emptyCategory_swipeDown_sendsRefreshIntent() {
        showScreen(
            feed = FeedState.EmptyCategory,
            selectedCategory = category.id,
        )

        val emptyCategoryTitle = context.getString(
            ShowcaseR.string.showcase_empty_category_title,
        )

        composeRule
            .onNodeWithText(emptyCategoryTitle)
            .assertIsDisplayed()

        swipeDownAndAssertRefresh()
    }

    @Test
    fun error_retryClick_sendsRefreshIntent() {
        showScreen(FeedState.Error(UiError.NoConnection))

        val errorTitle = context.getString(ShowcaseR.string.showcase_error_title)
        val retryLabel = context.getString(CoreUiR.string.rustor_action_retry)

        composeRule
            .onNodeWithText(errorTitle)
            .assertIsDisplayed()

        composeRule.runOnIdle {
            assertTrue(receivedIntents.isEmpty())
        }

        composeRule
            .onNodeWithText(retryLabel)
            .assertIsDisplayed()
            .performClick()

        composeRule.runOnIdle {
            assertEquals(
                listOf(ShowcaseIntent.Refresh),
                receivedIntents.toList(),
            )
        }
    }

    @Test
    fun emptyCatalog_refreshClick_sendsRefreshIntent() {
        showScreen(FeedState.EmptyCatalog)

        val emptyTitle = context.getString(ShowcaseR.string.showcase_empty_title)
        val refreshLabel = context.getString(ShowcaseR.string.showcase_action_refresh)

        composeRule
            .onNodeWithText(emptyTitle)
            .assertIsDisplayed()

        composeRule.runOnIdle {
            assertTrue(receivedIntents.isEmpty())
        }

        composeRule
            .onNodeWithText(refreshLabel)
            .assertIsDisplayed()
            .performClick()

        composeRule.runOnIdle {
            assertEquals(listOf(ShowcaseIntent.Refresh), receivedIntents.toList())
        }
    }

    @Test
    fun content_refreshing_showsIndicatorAndKeepsCurrentCard() {
        val content = FeedState.Content(
            apps = listOf(firstApp, secondApp),
            isRefreshing = false,
        )
        val feedState = showScreen(content)

        composeRule
            .onNodeWithTag(SHOWCASE_FEED_TAG)
            .performScrollToIndex(1)

        val currentCard = composeRule.onNodeWithText(secondApp.name)
        val refreshIndicator = composeRule.onNode(
            SemanticsMatcher.expectValue(
                SemanticsProperties.ProgressBarRangeInfo,
                ProgressBarRangeInfo.Indeterminate,
            ) and hasAnyAncestor(hasTestTag(SHOWCASE_REFRESH_AREA_TAG)),
        )

        currentCard.assertIsDisplayed()
        refreshIndicator.assertDoesNotExist()

        composeRule.runOnIdle {
            feedState.value = content.copy(isRefreshing = true)
        }

        refreshIndicator.assertIsDisplayed()
        currentCard.assertIsDisplayed()

        composeRule.runOnIdle {
            feedState.value = content.copy(isRefreshing = false)
        }

        refreshIndicator.assertDoesNotExist()
        currentCard.assertIsDisplayed()
    }

    @Test
    fun content_appendedWithoutUserRefresh_keepsVisibleApp() {
        val thirdApp = firstApp.copy(
            id = AppId("com.rustor.test.third"),
            name = "Third app",
        )

        assertSecondAppRemainsVisible(listOf(firstApp, secondApp, thirdApp))

        composeRule.onNodeWithTag(SHOWCASE_FEED_TAG).performScrollToIndex(2)
        composeRule.onNodeWithText(thirdApp.name).assertIsDisplayed()
    }

    @Test
    fun content_replacedEarlierAppWithoutUserRefresh_keepsVisibleApp() {
        val replacement = firstApp.copy(
            id = AppId("com.rustor.test.replacement"),
            name = "Replacement",
        )

        assertSecondAppRemainsVisible(listOf(replacement, secondApp))
    }

    private fun assertSecondAppRemainsVisible(updatedApps: List<App>) {
        val content = FeedState.Content(
            apps = listOf(firstApp, secondApp),
            isRefreshing = false,
        )
        val feedState = showScreen(content)

        composeRule.onNodeWithTag(SHOWCASE_FEED_TAG).performScrollToIndex(1)
        composeRule.onNodeWithText(secondApp.name).assertIsDisplayed()

        composeRule.runOnIdle {
            feedState.value = content.copy(apps = updatedApps)
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithText(secondApp.name).assertIsDisplayed()
    }


    private fun showScreen(
        feed: FeedState,
        selectedCategory: CategoryId? = null,
        refreshRevision: State<Long> = mutableStateOf(0L),
    ): MutableState<FeedState> {
        val feedState = mutableStateOf(feed)

        composeRule.setContent {
            RusTorTheme {
                val currentFeed = feedState.value
                val revision = refreshRevision.value
                val pager = rememberShowcasePager(
                    feed = currentFeed,
                    refreshRevision = revision,
                )
                val snackbarHostState = remember { SnackbarHostState() }

                ShowcaseScreen(
                    uiState = ShowcaseUiState(
                        header = ShowcaseHeader.Main(
                            categories = listOf(category),
                            selectedCategory = selectedCategory,
                        ),
                        feed = currentFeed,
                        refreshRevision = revision,
                    ),
                    onIntent = { intent -> receivedIntents += intent },
                    onAppClick = {},
                    onSearchClick = {},
                    onAllCategoriesClick = {},
                    onBackClick = {},
                    snackbarHostState = snackbarHostState,
                    pager = pager,
                )
            }
        }

        return feedState
    }

    @Test
    fun manualRefreshWithReorderedFeedShowsNewFirstPage() {
        assertRefreshShowsFirstPage(
            updatedApps = listOf(
                secondApp.copy(name = "New first page"),
                firstApp.copy(name = "New second page"),
            ),
        )
    }

    @Test
    fun manualRefreshWithUnchangedOrderStillReturnsToFirstPage() {
        assertRefreshShowsFirstPage(updatedApps = listOf(firstApp, secondApp))
    }

    private fun assertRefreshShowsFirstPage(updatedApps: List<App>) {
        val revision = mutableStateOf(0L)
        val content = FeedState.Content(
            apps = listOf(firstApp, secondApp),
            isRefreshing = false,
        )
        val feed = showScreen(feed = content, refreshRevision = revision)

        composeRule.runOnIdle {
            feed.value = content.copy(isRefreshing = true)
        }
        composeRule.onNodeWithTag(SHOWCASE_FEED_TAG).performScrollToIndex(1)
        composeRule.onNodeWithText(secondApp.name).assertIsDisplayed()

        composeRule.runOnIdle {
            feed.value = FeedState.Content(updatedApps, isRefreshing = false)
            revision.value = 1L
        }

        composeRule.onNodeWithText(updatedApps.first().name).assertIsDisplayed()
    }

    private fun swipeDownAndAssertRefresh() {
        composeRule.runOnIdle {
            assertTrue(
                "До жеста экран не должен отправлять события",
                receivedIntents.isEmpty(),
            )
        }

        composeRule
            .onNodeWithTag(SHOWCASE_REFRESH_AREA_TAG)
            .assertIsDisplayed()
            .performTouchInput {
                swipeDown(
                    startY = height * 0.25f,
                    endY = height * 0.90f,
                    durationMillis = 1_000L,
                )
            }

        composeRule.runOnIdle {
            assertEquals(
                "Один свайп должен отправить ровно одно событие Refresh",
                listOf(ShowcaseIntent.Refresh),
                receivedIntents.toList(),
            )
        }
    }
}