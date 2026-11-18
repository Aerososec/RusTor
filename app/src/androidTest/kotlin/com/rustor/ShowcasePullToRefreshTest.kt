package com.rustor

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
import com.rustor.feature.showcase.SHOWCASE_FEED_TAG
import com.rustor.feature.showcase.SHOWCASE_REFRESH_AREA_TAG
import com.rustor.feature.showcase.ShowcaseScreen
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

    private fun showScreen(feed: FeedState) {
        val pageCount = (feed as? FeedState.Content)?.apps?.size ?: 0

        composeRule.setContent {
            RusTorTheme {
                val pagerState = rememberPagerState(
                    initialPage = 0,
                    pageCount = { pageCount },
                )
                val snackbarHostState = remember {
                    SnackbarHostState()
                }

                ShowcaseScreen(
                    uiState = ShowcaseUiState(
                        header = ShowcaseHeader.Main(
                            categories = listOf(category),
                            selectedCategory = null,
                        ),
                        feed = feed,
                    ),
                    onIntent = { intent ->
                        receivedIntents += intent
                    },
                    onAppClick = {},
                    onSearchClick = {},
                    onAllCategoriesClick = {},
                    onBackClick = {},
                    snackbarHostState = snackbarHostState,
                    pagerState = pagerState,
                )
            }
        }
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