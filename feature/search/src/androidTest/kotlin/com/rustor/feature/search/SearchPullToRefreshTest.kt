package com.rustor.feature.search

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rustor.core.common.error.UiError
import com.rustor.core.ui.R as CoreUiR
import com.rustor.core.ui.theme.RusTorTheme
import com.rustor.feature.search.ui.SearchIntent
import com.rustor.feature.search.ui.SearchResultsState
import com.rustor.feature.search.ui.SearchUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import com.rustor.core.model.AgeRating
import com.rustor.core.model.App
import com.rustor.core.model.AppId
import com.rustor.core.model.Category
import com.rustor.core.model.CategoryId
import com.rustor.feature.search.ui.SearchResultKind
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.text.AnnotatedString

@RunWith(AndroidJUnit4::class)
class SearchPullToRefreshTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    private val receivedIntents = mutableListOf<SearchIntent>()

    private val calculatorApp = App(
        id = AppId("com.rustor.test.calculator"),
        name = "Калькулятор",
        iconUrl = null,
        category = Category(
            id = CategoryId("tools"),
            title = "Инструменты",
        ),
        summary = "Калькулятор для повседневных расчётов",
        description = "Поддерживает основные арифметические операции.",
        developer = "Test Developer",
        ageRating = AgeRating.ZERO_PLUS,
        screenshots = emptyList(),
        apkUrl = null,
    )

    @Test
    fun error_retryClick_sendsRetryIntent() {
        showScreen(
            SearchUiState(
                results = SearchResultsState.Error(UiError.NoConnection),
            ),
        )

        val errorTitle = context.getString(CoreUiR.string.rustor_error_title)
        val retryLabel = context.getString(CoreUiR.string.rustor_action_retry)

        composeRule.onNodeWithText(errorTitle)
            .performScrollTo()
            .assertIsDisplayed()

        val errorDescription = context.getString(R.string.rustor_search_error_no_connection)

        composeRule.onNodeWithText(errorDescription)
            .performScrollTo()
            .assertIsDisplayed()

        val retryButton = composeRule.onNodeWithText(retryLabel)
        retryButton.performScrollTo().assertIsDisplayed()

        composeRule.runOnIdle {
            assertTrue(receivedIntents.isEmpty())
        }

        retryButton.performClick()

        composeRule.runOnIdle {
            assertEquals(listOf(SearchIntent.Retry), receivedIntents.toList())
        }
    }

    @Test
    fun matches_swipeDown_sendsSearchResultRefreshIntent() {
        showScreen(
            SearchUiState(
                query = "кальк",
                results = SearchResultsState.Content(
                    apps = listOf(calculatorApp),
                    kind = SearchResultKind.Matches,
                ),
            ),
        )

        composeRule.onNodeWithText(calculatorApp.name)
            .assertIsDisplayed()

        val resultsGrid = composeRule.onNode(
            hasScrollAction() and hasAnyDescendant(hasText(calculatorApp.name)),
        )

        composeRule.runOnIdle {
            assertTrue(receivedIntents.isEmpty())
        }

        resultsGrid
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
                listOf(SearchIntent.SearchResultRefresh),
                receivedIntents.toList(),
            )
        }
    }

    @Test
    fun popularWithEmptyQuery_swipeDown_sendsSearchResultRefreshIntent() {
        showScreen(
            SearchUiState(
                query = "",
                results = SearchResultsState.Content(
                    apps = listOf(calculatorApp),
                    kind = SearchResultKind.Popular,
                ),
            ),
        )

        val popularTitle = context.getString(R.string.rustor_search_popular)
        val noResultsTitle = context.getString(R.string.rustor_search_no_results)

        composeRule.onNodeWithText(popularTitle).assertIsDisplayed()
        composeRule.onNodeWithText(calculatorApp.name).assertIsDisplayed()
        composeRule.onNodeWithText(noResultsTitle).assertDoesNotExist()

        val resultsGrid = composeRule.onNode(
            hasScrollAction() and hasAnyDescendant(hasText(popularTitle)),
        )

        composeRule.runOnIdle {
            assertTrue(receivedIntents.isEmpty())
        }

        resultsGrid
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
                listOf(SearchIntent.SearchResultRefresh),
                receivedIntents.toList(),
            )
        }
    }

    @Test
    fun noMatchesWithPopularApps_swipeDown_sendsSearchResultRefreshIntent() {
        showScreen(
            SearchUiState(
                query = "погода",
                results = SearchResultsState.Content(
                    apps = listOf(calculatorApp),
                    kind = SearchResultKind.NoMatches,
                ),
            ),
        )

        val noResultsTitle = context.getString(R.string.rustor_search_no_results)
        val popularTitle = context.getString(R.string.rustor_search_popular)

        composeRule.onNodeWithText(noResultsTitle).assertIsDisplayed()

        val resultsGrid = composeRule.onNode(
            hasScrollAction() and hasAnyDescendant(hasText(noResultsTitle)),
        )

        composeRule.runOnIdle {
            assertTrue(receivedIntents.isEmpty())
        }

        resultsGrid
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
                listOf(SearchIntent.SearchResultRefresh),
                receivedIntents.toList(),
            )
        }

        resultsGrid.performScrollToNode(hasText(popularTitle))
        composeRule.onNodeWithText(popularTitle).assertIsDisplayed()

        val popularGrid = composeRule.onNode(
            hasScrollAction() and hasAnyDescendant(hasText(popularTitle)),
        )
        popularGrid.performScrollToNode(hasText(calculatorApp.name))
        composeRule.onNodeWithText(calculatorApp.name).assertIsDisplayed()
    }

    @Test
    fun emptyResults_swipeDown_sendsSearchResultRefreshIntent() {
        showScreen(
            SearchUiState(
                query = "",
                results = SearchResultsState.Content(
                    apps = emptyList(),
                    kind = SearchResultKind.Popular,
                ),
            ),
        )

        val emptyTitle = context.getString(R.string.rustor_search_empty)
        val popularTitle = context.getString(R.string.rustor_search_popular)
        val noResultsTitle = context.getString(R.string.rustor_search_no_results)

        composeRule.onNodeWithText(emptyTitle).assertIsDisplayed()
        composeRule.onNodeWithText(popularTitle).assertDoesNotExist()
        composeRule.onNodeWithText(noResultsTitle).assertDoesNotExist()

        val resultsGrid = composeRule.onNode(
            hasScrollAction() and hasAnyDescendant(hasText(emptyTitle)),
        )

        composeRule.runOnIdle {
            assertTrue(receivedIntents.isEmpty())
        }

        resultsGrid
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
                listOf(SearchIntent.SearchResultRefresh),
                receivedIntents.toList(),
            )
        }
    }

    @Test
    fun error_swipeDown_sendsSearchResultRefreshIntent() {
        showScreen(
            SearchUiState(
                results = SearchResultsState.Error(UiError.NoConnection),
            ),
        )

        val errorTitle = context.getString(CoreUiR.string.rustor_error_title)
        composeRule.onNodeWithText(errorTitle).assertIsDisplayed()

        val errorContainer = composeRule.onNode(
            hasScrollAction() and hasAnyDescendant(hasText(errorTitle)),
        )

        composeRule.runOnIdle {
            assertTrue(receivedIntents.isEmpty())
        }

        errorContainer
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
                listOf(SearchIntent.SearchResultRefresh),
                receivedIntents.toList(),
            )
        }
    }

    @Test
    fun matches_refreshing_showsIndicatorAndKeepsQueryAndResults() {
        val query = "кальк"
        val content = SearchUiState(
            query = query,
            results = SearchResultsState.Content(
                apps = listOf(calculatorApp),
                kind = SearchResultKind.Matches,
            ),
        )
        val screenState = showScreen(content)

        val searchField = composeRule.onNode(hasSetTextAction())
        val resultCard = composeRule.onNodeWithText(calculatorApp.name)
        val popularTitle = context.getString(R.string.rustor_search_popular)
        val refreshIndicator = composeRule.onNode(
            SemanticsMatcher.expectValue(
                SemanticsProperties.ProgressBarRangeInfo,
                ProgressBarRangeInfo.Indeterminate,
            ),
        )

        fun assertQueryAndResultsDisplayed() {
            searchField
                .assertIsDisplayed()
                .assert(
                    SemanticsMatcher.expectValue(
                        SemanticsProperties.EditableText,
                        AnnotatedString(query),
                    ),
                )
            resultCard.assertIsDisplayed()
            composeRule.onNodeWithText(popularTitle).assertDoesNotExist()
        }

        refreshIndicator.assertDoesNotExist()
        assertQueryAndResultsDisplayed()

        composeRule.runOnIdle {
            screenState.value = content.copy(isRefreshing = true)
        }

        refreshIndicator.assertIsDisplayed()
        assertQueryAndResultsDisplayed()

        composeRule.runOnIdle {
            screenState.value = content
        }

        refreshIndicator.assertDoesNotExist()
        assertQueryAndResultsDisplayed()
    }

    @Test
    fun popularWithEmptyQuery_refreshing_showsIndicatorAndKeepsPopularApps() {
        val content = SearchUiState(
            query = "",
            results = SearchResultsState.Content(
                apps = listOf(calculatorApp),
                kind = SearchResultKind.Popular,
            ),
        )
        val screenState = showScreen(content)

        val popularTitle = context.getString(R.string.rustor_search_popular)
        val noResultsTitle = context.getString(R.string.rustor_search_no_results)
        val refreshIndicator = composeRule.onNode(
            SemanticsMatcher.expectValue(
                SemanticsProperties.ProgressBarRangeInfo,
                ProgressBarRangeInfo.Indeterminate,
            ),
        )

        fun assertPopularAppsDisplayed() {
            composeRule.onNodeWithText(popularTitle).assertIsDisplayed()
            composeRule.onNodeWithText(calculatorApp.name).assertIsDisplayed()
            composeRule.onNodeWithText(noResultsTitle).assertDoesNotExist()
        }

        refreshIndicator.assertDoesNotExist()
        assertPopularAppsDisplayed()

        composeRule.runOnIdle {
            screenState.value = content.copy(isRefreshing = true)
        }

        refreshIndicator.assertIsDisplayed()
        assertPopularAppsDisplayed()

        composeRule.runOnIdle {
            screenState.value = content
        }

        refreshIndicator.assertDoesNotExist()
        assertPopularAppsDisplayed()
    }

    private fun showScreen(uiState: SearchUiState): MutableState<SearchUiState> {
        val screenState = mutableStateOf(uiState)

        composeRule.setContent {
            RusTorTheme {
                SearchScreen(
                    uiState = screenState.value,
                    onIntent = { intent -> receivedIntents += intent },
                    snackbarHostState = remember { SnackbarHostState() },
                    onAppClick = {},
                )
            }
        }

        return screenState
    }
}