package com.rustor

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rustor.testing.PreferencesTestEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.rustor.feature.onboarding.R as OnboardingR
import com.rustor.feature.showcase.R as ShowcaseR

@RunWith(AndroidJUnit4::class)
class OnboardingFlowTest {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    private val onboardingTitle: String
        get() = context.getString(OnboardingR.string.onboarding_title)

    private val continueButton: String
        get() = context.getString(OnboardingR.string.onboarding_continue)

    private val showcaseSubtitle: String
        get() = context.getString(ShowcaseR.string.showcase_subtitle)

    @Before
    fun setUp() {
        clearPreferences()
    }

    @After
    fun tearDown() {
        clearPreferences()
    }

    @Test
    fun firstLaunch_showsOnboarding_andNextLaunchOpensShowcase() {
        ActivityScenario.launch(MainActivity::class.java).use {
            waitForText(onboardingTitle)

            composeRule
                .onNodeWithText(onboardingTitle)
                .assertIsDisplayed()

            composeRule
                .onNodeWithText(continueButton)
                .performScrollTo()
                .assertIsDisplayed()
                .performClick()

            assertShowcaseDisplayed()
        }

        ActivityScenario.launch(MainActivity::class.java).use {
            assertShowcaseDisplayed()
        }
    }

    private fun assertShowcaseDisplayed() {
        waitForText(showcaseSubtitle)

        composeRule
            .onNodeWithText(showcaseSubtitle)
            .assertIsDisplayed()

        composeRule
            .onNodeWithText(onboardingTitle)
            .assertDoesNotExist()

        composeRule
            .onNodeWithText(continueButton)
            .assertDoesNotExist()
    }

    private fun waitForText(text: String) {
        composeRule.waitUntil(timeoutMillis = UI_TIMEOUT_MILLIS) {
            composeRule
                .onAllNodesWithText(text)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun clearPreferences() {
        val entryPoint = EntryPointAccessors.fromApplication(
            context,
            PreferencesTestEntryPoint::class.java,
        )

        runBlocking {
            entryPoint.preferencesDataStore().edit { preferences ->
                preferences.clear()
            }
        }
    }

    private companion object {
        const val UI_TIMEOUT_MILLIS = 10_000L
    }
}