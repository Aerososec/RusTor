package com.rustor.feature.categories

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rustor.core.common.error.UiError
import com.rustor.core.ui.theme.RusTorTheme
import com.rustor.feature.categories.ui.CategoriesIntent
import com.rustor.feature.categories.ui.CategoriesUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import com.rustor.core.model.CategoryId
import com.rustor.core.model.CategorySummary
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import com.rustor.core.ui.R as CoreUiR

@RunWith(AndroidJUnit4::class)
class CategoriesPullToRefreshTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    private val receivedIntents = mutableListOf<CategoriesIntent>()

    @Test
    fun error_retryClick_sendsRefreshIntent() {
        showScreen(CategoriesUiState.Error(UiError.NoConnection))

        val errorTitle = context.getString(R.string.categories_error_title)
        val retryLabel = context.getString(R.string.categories_retry)

        composeRule.onNodeWithText(errorTitle)
            .assertIsDisplayed()

        val errorDescription = context.getString(R.string.categories_error_no_connection)
        composeRule.onNodeWithText(errorDescription)
            .assertIsDisplayed()

        composeRule.runOnIdle {
            assertTrue(receivedIntents.isEmpty())
        }

        composeRule.onNodeWithText(retryLabel)
            .assertIsDisplayed()
            .performClick()

        composeRule.runOnIdle {
            assertEquals(listOf(CategoriesIntent.Refresh), receivedIntents.toList())
        }
    }

    @Test
    fun content_swipeDown_sendsRefreshIntent() {
        val category = CategorySummary(
            id = CategoryId("tools"),
            title = "Инструменты",
            appCount = 3,
        )
        showScreen(
            CategoriesUiState.Content(
                content = listOf(category),
                isRefreshing = false,
            ),
        )

        composeRule.onNodeWithText(category.title)
            .assertIsDisplayed()

        composeRule.runOnIdle {
            assertTrue(receivedIntents.isEmpty())
        }

        composeRule.onNodeWithTag(CATEGORIES_LIST_TAG)
            .assertIsDisplayed()
            .performTouchInput {
                swipeDown(
                    startY = height * 0.25f,
                    endY = height * 0.90f,
                    durationMillis = 1_000L,
                )
            }

        composeRule.runOnIdle {
            assertEquals(listOf(CategoriesIntent.Refresh), receivedIntents.toList())
        }
    }

    @Test
    fun empty_swipeDown_sendsRefreshIntent() {
        showScreen(CategoriesUiState.NoCategoriesUi)

        val emptyTitle = context.getString(R.string.categories_empty_title)

        composeRule.onNodeWithText(emptyTitle)
            .assertIsDisplayed()

        composeRule.runOnIdle {
            assertTrue(receivedIntents.isEmpty())
        }

        composeRule.onNode(hasScrollAction() and hasAnyDescendant(hasText(emptyTitle)))
            .assertIsDisplayed()
            .performTouchInput {
                swipeDown(
                    startY = height * 0.25f,
                    endY = height * 0.90f,
                    durationMillis = 1_000L,
                )
            }

        composeRule.runOnIdle {
            assertEquals(listOf(CategoriesIntent.Refresh), receivedIntents.toList())
        }
    }

    @Test
    fun error_swipeDown_sendsRefreshIntent() {
        showScreen(CategoriesUiState.Error(UiError.NoConnection))

        val errorTitle = context.getString(R.string.categories_error_title)

        composeRule.onNodeWithText(errorTitle)
            .assertIsDisplayed()

        composeRule.runOnIdle {
            assertTrue(receivedIntents.isEmpty())
        }

        composeRule.onNode(hasScrollAction() and hasAnyDescendant(hasText(errorTitle)))
            .assertIsDisplayed()
            .performTouchInput {
                swipeDown(
                    startY = height * 0.25f,
                    endY = height * 0.90f,
                    durationMillis = 1_000L,
                )
            }

        composeRule.runOnIdle {
            assertEquals(listOf(CategoriesIntent.Refresh), receivedIntents.toList())
        }
    }

    @Test
    fun empty_refreshClick_sendsRefreshIntent() {
        showScreen(CategoriesUiState.NoCategoriesUi)

        val emptyTitle = context.getString(R.string.categories_empty_title)
        val refreshLabel = context.getString(R.string.categories_refresh)

        composeRule.onNodeWithText(emptyTitle)
            .assertIsDisplayed()

        composeRule.runOnIdle {
            assertTrue(receivedIntents.isEmpty())
        }

        composeRule.onNodeWithText(refreshLabel)
            .assertIsDisplayed()
            .performClick()

        composeRule.runOnIdle {
            assertEquals(listOf(CategoriesIntent.Refresh), receivedIntents.toList())
        }
    }

    @Test
    fun content_refreshing_showsIndicatorAndKeepsCategories() {
        val categories = listOf(
            CategorySummary(id = CategoryId("tools"), title = "Инструменты", appCount = 3),
            CategorySummary(id = CategoryId("games"), title = "Игры", appCount = 5),
        )
        val content = CategoriesUiState.Content(
            content = categories,
            isRefreshing = false,
        )
        val screenState = showScreen(content)

        val refreshIndicator = composeRule.onNode(
            SemanticsMatcher.expectValue(
                SemanticsProperties.ProgressBarRangeInfo,
                ProgressBarRangeInfo.Indeterminate,
            ),
        )

        fun assertCategoriesDisplayed() {
            composeRule.onNodeWithTag(CATEGORIES_LIST_TAG).assertIsDisplayed()

            categories.forEach { category ->
                val expectedCount = context.resources.getQuantityString(
                    CoreUiR.plurals.rustor_category_app_count,
                    category.appCount,
                    category.appCount,
                )

                composeRule
                    .onNode(hasText(category.title) and hasText(expectedCount))
                    .assertIsDisplayed()
            }
        }

        refreshIndicator.assertDoesNotExist()
        assertCategoriesDisplayed()

        composeRule.runOnIdle {
            screenState.value = content.copy(isRefreshing = true)
        }

        refreshIndicator.assertIsDisplayed()
        assertCategoriesDisplayed()

        composeRule.runOnIdle {
            screenState.value = content
        }

        refreshIndicator.assertDoesNotExist()
        assertCategoriesDisplayed()
    }

    private fun showScreen(uiState: CategoriesUiState): MutableState<CategoriesUiState> {
        val screenState = mutableStateOf(uiState)

        composeRule.setContent {
            RusTorTheme {
                CategoriesScreen(
                    uiState = screenState.value,
                    onIntent = { intent -> receivedIntents += intent },
                    onCategoryClick = {},
                    onBackClick = {},
                    onSearchClick = {},
                    snackbarHostState = remember { SnackbarHostState() },
                )
            }
        }

        return screenState
    }
}