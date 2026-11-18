package com.rustor.feature.onboarding

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rustor.core.ui.theme.RusTorTheme
import com.rustor.feature.onboarding.ui.OnboardingIntent
import com.rustor.feature.onboarding.ui.OnboardingUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    private val receivedIntents = mutableListOf<OnboardingIntent>()

    @Test
    fun saveFailed_continueClick_sendsContinueClickedIntent() {
        showScreen(OnboardingUiState.SaveFailed)

        val errorMessage = context.getString(R.string.onboarding_save_error)
        val continueLabel = context.getString(R.string.onboarding_continue)

        composeRule.onNodeWithText(errorMessage)
            .performScrollTo()
            .assertIsDisplayed()

        val continueButton = composeRule.onNodeWithText(continueLabel)
        continueButton
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsEnabled()

        composeRule.runOnIdle {
            assertTrue(receivedIntents.isEmpty())
        }

        continueButton.performClick()

        composeRule.runOnIdle {
            assertEquals(
                listOf(OnboardingIntent.ContinueClicked),
                receivedIntents.toList(),
            )
        }
    }

    @Test
    fun saving_showsIndicatorAndIgnoresRepeatedTap() {
        val screenState = showScreen(OnboardingUiState.Ready)

        val continueLabel = context.getString(R.string.onboarding_continue)
        val savingLabel = context.getString(R.string.onboarding_saving)
        val savingIndicator = composeRule.onNode(
            matcher = SemanticsMatcher.expectValue(
                SemanticsProperties.ProgressBarRangeInfo,
                ProgressBarRangeInfo.Indeterminate,
            ),
            useUnmergedTree = true,
        )

        savingIndicator.assertDoesNotExist()

        composeRule.onNodeWithText(continueLabel)
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertEquals(
                listOf(OnboardingIntent.ContinueClicked),
                receivedIntents.toList(),
            )
            screenState.value = OnboardingUiState.Saving
        }

        val savingButton = composeRule.onNodeWithText(savingLabel)
        savingButton
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsNotEnabled()

        savingIndicator.assertIsDisplayed()

        savingButton.performTouchInput { click() }

        composeRule.runOnIdle {
            assertEquals(
                listOf(OnboardingIntent.ContinueClicked),
                receivedIntents.toList(),
            )
        }
    }

    private fun showScreen(
        uiState: OnboardingUiState,
    ): MutableState<OnboardingUiState> {
        val screenState = mutableStateOf(uiState)

        composeRule.setContent {
            RusTorTheme {
                OnboardingScreen(
                    uiState = screenState.value,
                    onIntent = { intent -> receivedIntents += intent },
                )
            }
        }

        return screenState
    }
}