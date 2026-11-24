package com.rustor.feature.appdetails

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
import com.rustor.core.ui.theme.RusTorTheme
import com.rustor.feature.appdetails.ui.AppDetailsIntent
import com.rustor.feature.appdetails.ui.AppDetailsUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import com.rustor.core.model.AgeRating
import com.rustor.core.model.App
import com.rustor.core.model.AppId
import com.rustor.core.model.Category
import com.rustor.core.model.CategoryId
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
class AppDetailsPullToRefreshTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    private val receivedIntents = mutableListOf<AppDetailsIntent>()

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
    fun error_retryClick_sendsRefreshIntent() {
        showScreen(AppDetailsUiState.Error(UiError.NoConnection))

        val errorTitle = context.getString(R.string.app_details_error_title)
        val errorDescription = context.getString(CoreUiR.string.rustor_error_no_connection)
        val retryLabel = context.getString(R.string.app_details_retry)

        composeRule.onNodeWithText(errorTitle)
            .performScrollTo()
            .assertIsDisplayed()

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
            assertEquals(listOf(AppDetailsIntent.Refresh), receivedIntents.toList())
        }
    }

    @Test
    fun content_swipeDown_sendsRefreshIntent() {
        showScreen(
            AppDetailsUiState.Content(
                app = calculatorApp,
                isRefreshing = false,
            ),
        )

        composeRule.onNodeWithText(calculatorApp.name)
            .assertIsDisplayed()

        composeRule.runOnIdle {
            assertTrue(receivedIntents.isEmpty())
        }

        composeRule.onNodeWithTag(APP_DETAILS_CONTENT_TAG)
            .assertIsDisplayed()
            .performTouchInput {
                swipeDown(
                    startY = height * 0.25f,
                    endY = height * 0.90f,
                    durationMillis = 1_000L,
                )
            }

        composeRule.runOnIdle {
            assertEquals(listOf(AppDetailsIntent.Refresh), receivedIntents.toList())
        }
    }

    @Test
    fun error_swipeDown_sendsRefreshIntent() {
        showScreen(AppDetailsUiState.Error(UiError.NoConnection))

        val errorTitle = context.getString(R.string.app_details_error_title)
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
            assertEquals(listOf(AppDetailsIntent.Refresh), receivedIntents.toList())
        }
    }

    @Test
    fun notFound_swipeDown_sendsRefreshIntent() {
        showScreen(AppDetailsUiState.NotFound)

        val notFoundTitle = context.getString(R.string.app_details_not_found_title)
        composeRule.onNodeWithText(notFoundTitle).assertIsDisplayed()

        val notFoundContainer = composeRule.onNode(
            hasScrollAction() and hasAnyDescendant(hasText(notFoundTitle)),
        )

        composeRule.runOnIdle {
            assertTrue(receivedIntents.isEmpty())
        }

        notFoundContainer
            .assertIsDisplayed()
            .performTouchInput {
                swipeDown(
                    startY = height * 0.25f,
                    endY = height * 0.90f,
                    durationMillis = 1_000L,
                )
            }

        composeRule.runOnIdle {
            assertEquals(listOf(AppDetailsIntent.Refresh), receivedIntents.toList())
        }
    }

    @Test
    fun notFound_refreshClick_sendsRefreshIntent() {
        showScreen(AppDetailsUiState.NotFound)

        val title = context.getString(R.string.app_details_not_found_title)
        val description = context.getString(R.string.app_details_not_found_description)
        val refreshLabel = context.getString(R.string.app_details_refresh)

        composeRule.onNodeWithText(title)
            .performScrollTo()
            .assertIsDisplayed()

        composeRule.onNodeWithText(description)
            .performScrollTo()
            .assertIsDisplayed()

        val refreshButton = composeRule.onNodeWithText(refreshLabel)
        refreshButton.performScrollTo().assertIsDisplayed()

        composeRule.runOnIdle {
            assertTrue(receivedIntents.isEmpty())
        }

        refreshButton.performClick()

        composeRule.runOnIdle {
            assertEquals(listOf(AppDetailsIntent.Refresh), receivedIntents.toList())
        }
    }

    @Test
    fun content_refreshing_showsIndicatorAndKeepsAppDetails() {
        val content = AppDetailsUiState.Content(
            app = calculatorApp,
            isRefreshing = false,
        )
        val screenState = showScreen(content)

        val refreshIndicator = composeRule.onNode(
            SemanticsMatcher.expectValue(
                SemanticsProperties.ProgressBarRangeInfo,
                ProgressBarRangeInfo.Indeterminate,
            ),
        )

        fun assertAppDetailsDisplayed() {
            composeRule.onNodeWithText(calculatorApp.name).assertIsDisplayed()
            composeRule.onNodeWithText(calculatorApp.developer).assertIsDisplayed()
            composeRule.onNodeWithText(calculatorApp.category.title).assertIsDisplayed()
        }

        refreshIndicator.assertDoesNotExist()
        assertAppDetailsDisplayed()

        composeRule.runOnIdle {
            screenState.value = content.copy(isRefreshing = true)
        }

        refreshIndicator.assertIsDisplayed()
        assertAppDetailsDisplayed()

        composeRule.runOnIdle {
            screenState.value = content
        }

        refreshIndicator.assertDoesNotExist()
        assertAppDetailsDisplayed()
    }

    private fun showScreen(uiState: AppDetailsUiState): MutableState<AppDetailsUiState> {
        val screenState = mutableStateOf(uiState)

        composeRule.setContent {
            RusTorTheme {
                AppDetailsScreen(
                    uiState = screenState.value,
                    onIntent = { intent -> receivedIntents += intent },
                    onBackClick = {},
                    onScreenshotClick = { _, _ -> },
                    snackbarHostState = remember { SnackbarHostState() },
                )
            }
        }

        return screenState
    }
}