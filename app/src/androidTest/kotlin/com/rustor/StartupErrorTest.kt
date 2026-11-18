package com.rustor

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rustor.core.ui.R as CoreUiR
import com.rustor.core.ui.theme.RusTorTheme
import com.rustor.ui.AppUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StartupErrorTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun error_retryClick_callsOnRetryOnce() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val title = context.getString(R.string.startup_error_title)
        val description = context.getString(R.string.startup_error_description)
        val retryLabel = context.getString(CoreUiR.string.rustor_action_retry)
        var retryCalls = 0

        composeRule.setContent {
            RusTorTheme {
                AppContent(
                    uiState = AppUiState.Error,
                    onRetry = { retryCalls++ },
                )
            }
        }

        composeRule.onNodeWithText(title)
            .performScrollTo()
            .assertIsDisplayed()

        composeRule.onNodeWithText(description)
            .performScrollTo()
            .assertIsDisplayed()

        val retryButton = composeRule.onNodeWithText(retryLabel)
        retryButton
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsEnabled()

        composeRule.runOnIdle {
            assertEquals(0, retryCalls)
        }

        retryButton.performClick()

        composeRule.runOnIdle {
            assertEquals(1, retryCalls)
        }
    }
}