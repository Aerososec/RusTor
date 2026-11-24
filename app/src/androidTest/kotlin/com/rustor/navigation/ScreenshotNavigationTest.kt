package com.rustor.navigation

import android.content.Context
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rustor.MainActivity
import com.rustor.core.model.App
import com.rustor.feature.appdetails.details.SCREENSHOT_STRIP_TAG
import com.rustor.feature.appdetails.APP_DETAILS_CONTENT_TAG
import com.rustor.testing.CatalogTestEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.rustor.feature.appdetails.R as AppDetailsR

@RunWith(AndroidJUnit4::class)
class ScreenshotNavigationTest {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    private val entryPoint: CatalogTestEntryPoint
        get() = EntryPointAccessors.fromApplication(
            context,
            CatalogTestEntryPoint::class.java,
        )

    private lateinit var expectedApp: App

    private val searchTabMatcher: SemanticsMatcher
        get() = hasText(RusTorTab.SEARCH.label) and isSelectable()

    private val searchFieldMatcher = hasSetTextAction()

    private val detailsContentMatcher =
        hasTestTag(APP_DETAILS_CONTENT_TAG)

    private val screenshotStripMatcher =
        hasTestTag(SCREENSHOT_STRIP_TAG)

    private val appHeadingMatcher: SemanticsMatcher
        get() = hasText(expectedApp.name) and isHeading()

    private val detailsTitle: String
        get() = context.getString(AppDetailsR.string.app_details_title)

    private val selectedPositionText: String
        get() = context.getString(
            AppDetailsR.string.rustor_screenshot_position,
            SELECTED_SCREENSHOT_INDEX + 1,
            expectedApp.screenshots.size,
        )

    @Before
    fun setUp() {
        runBlocking {
            entryPoint.preferencesDataStore().edit { preferences ->
                preferences.clear()
            }

            entryPoint.userPreferencesRepository().completeOnboarding()

            val repository = entryPoint.catalogRepository()
            repository.refresh(reshuffle = false)

            expectedApp = repository.observeFeed()
                .first()
                .single { it.id.value == APP_ID }

            assertTrue(
                "Для сценария нужны минимум три скриншота",
                expectedApp.screenshots.size >= 3,
            )
        }
    }

    @After
    fun tearDown() {
        runBlocking {
            entryPoint.preferencesDataStore().edit { preferences ->
                preferences.clear()
            }
        }
    }

    @Test
    fun screenshotClick_opensSelectedPosition_andBackReturnsToAppDetails() {
        ActivityScenario.launch(MainActivity::class.java).use {
            waitForNode(searchTabMatcher)

            composeRule
                .onNode(searchTabMatcher)
                .assertIsDisplayed()
                .performClick()

            waitForNode(searchFieldMatcher)

            composeRule
                .onNode(searchFieldMatcher)
                .performTextInput(expectedApp.name)

            composeRule
                .onNode(searchFieldMatcher)
                .performImeAction()

            val resultCardMatcher =
                hasText(expectedApp.name) and
                        hasClickAction() and
                        hasSetTextAction().not()

            waitForNode(resultCardMatcher)

            composeRule
                .onNode(resultCardMatcher)
                .assertIsDisplayed()
                .performClick()

            waitForNode(appHeadingMatcher)

            composeRule
                .onNode(appHeadingMatcher)
                .assertIsDisplayed()

            composeRule
                .onNodeWithText(detailsTitle)
                .assertIsDisplayed()

            composeRule
                .onNode(detailsContentMatcher)
                .performScrollToNode(screenshotStripMatcher)

            composeRule
                .onNode(screenshotStripMatcher)
                .assertIsDisplayed()
                .performScrollToIndex(SELECTED_SCREENSHOT_INDEX)

            val thumbnailMatcher =
                hasContentDescription(selectedPositionText) and
                        hasClickAction() and
                        hasAnyAncestor(screenshotStripMatcher)

            waitForNode(thumbnailMatcher)

            composeRule
                .onNode(thumbnailMatcher)
                .assertIsDisplayed()
                .performClick()

            waitForNode(hasText(selectedPositionText))

            composeRule
                .onNodeWithText(selectedPositionText)
                .assertIsDisplayed()

            composeRule
                .onNode(detailsContentMatcher)
                .assertDoesNotExist()

            composeRule
                .onNodeWithText(detailsTitle)
                .assertDoesNotExist()

            pressBack()

            waitForNode(detailsContentMatcher)

            composeRule
                .onNodeWithText(detailsTitle)
                .assertIsDisplayed()

            composeRule
                .onNodeWithText(selectedPositionText)
                .assertDoesNotExist()

            composeRule
                .onNode(detailsContentMatcher)
                .performScrollToIndex(0)

            waitForNode(appHeadingMatcher)

            composeRule
                .onNode(appHeadingMatcher)
                .assertIsDisplayed()

            composeRule
                .onNodeWithText(expectedApp.developer)
                .assertIsDisplayed()
        }
    }

    private fun waitForNode(matcher: SemanticsMatcher) {
        composeRule.waitUntil(timeoutMillis = UI_TIMEOUT_MILLIS) {
            composeRule
                .onAllNodes(matcher)
                .fetchSemanticsNodes()
                .size == 1
        }
    }

    private companion object {
        const val UI_TIMEOUT_MILLIS = 10_000L
        const val APP_ID = "com.shatteredpixel.shatteredpixeldungeon"
        const val SELECTED_SCREENSHOT_INDEX = 1
    }
}