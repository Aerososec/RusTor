package com.rustor.navigation

import android.content.Context
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.AnnotatedString
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rustor.MainActivity
import com.rustor.core.model.App
import com.rustor.navigation.RusTorTab
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
import com.rustor.core.ui.R as CoreUiR
import com.rustor.feature.appdetails.R as AppDetailsR
import com.rustor.feature.search.R as SearchR

@RunWith(AndroidJUnit4::class)
class SearchNavigationTest {

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

    private val searchFieldMatcher = hasSetTextAction()

    private val searchTabMatcher: SemanticsMatcher
        get() = hasText(RusTorTab.SEARCH.label) and isSelectable()

    private val resultCardMatcher: SemanticsMatcher
        get() = hasText(expectedApp.name) and hasClickAction()

    private val detailsTitle: String
        get() = context.getString(AppDetailsR.string.app_details_title)

    private val popularTitle: String
        get() = context.getString(SearchR.string.rustor_search_popular)

    private val noResultsTitle: String
        get() = context.getString(SearchR.string.rustor_search_no_results)

    @Before
    fun setUp() {
        runBlocking {
            entryPoint.preferencesDataStore().edit { preferences ->
                preferences.clear()
            }

            entryPoint.userPreferencesRepository().completeOnboarding()

            val repository = entryPoint.catalogRepository()
            repository.refresh(reshuffle = false)

            val catalog = repository.observeFeed().first()

            expectedApp = catalog.single { app ->
                app.name.contains(SEARCH_QUERY, ignoreCase = true)
            }

            val popularApps = repository.observePopularApps().first()

            assertTrue(
                "Приложение для этого сценария должно отсутствовать в популярных",
                popularApps.none { it.id == expectedApp.id },
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
    fun search_openAppDetails_andBack_preservesQueryAndResults() {
        ActivityScenario.launch(MainActivity::class.java).use {
            waitForNode(searchTabMatcher)

            composeRule
                .onNode(searchTabMatcher)
                .assertIsDisplayed()
                .performClick()

            waitForNode(searchFieldMatcher)
            assertSearchQuery("")

            composeRule
                .onNode(searchFieldMatcher)
                .performTextInput(SEARCH_QUERY)

            assertSearchQuery(SEARCH_QUERY)

            waitForNode(resultCardMatcher)

            composeRule
                .onNode(searchFieldMatcher)
                .performImeAction()

            assertSearchResults()

            composeRule
                .onNode(resultCardMatcher)
                .performClick()

            waitForNode(hasText(detailsTitle))

            composeRule
                .onNodeWithText(detailsTitle)
                .assertIsDisplayed()

            val appHeadingMatcher =
                hasText(expectedApp.name) and isHeading()

            waitForNode(appHeadingMatcher)

            composeRule
                .onNode(appHeadingMatcher)
                .assertIsDisplayed()

            composeRule
                .onNodeWithText(expectedApp.developer)
                .assertIsDisplayed()

            val backDescription = context.getString(
                CoreUiR.string.rustor_action_back,
            )

            composeRule
                .onNode(
                    hasContentDescription(backDescription) and
                            hasClickAction(),
                )
                .assertIsDisplayed()
                .performClick()

            waitForNode(searchFieldMatcher)

            assertSearchQuery(SEARCH_QUERY)

            composeRule
                .onNode(searchFieldMatcher)
                .performImeAction()

            assertSearchResults()

            composeRule
                .onNode(searchTabMatcher)
                .assertIsSelected()

            composeRule
                .onNodeWithText(detailsTitle)
                .assertDoesNotExist()
        }
    }

    private fun assertSearchQuery(expectedQuery: String) {
        composeRule
            .onNode(searchFieldMatcher)
            .assertIsDisplayed()
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.EditableText,
                    AnnotatedString(expectedQuery),
                ),
            )
    }

    private fun assertSearchResults() {
        waitForNode(resultCardMatcher)

        assertSearchQuery(SEARCH_QUERY)

        composeRule
            .onNode(resultCardMatcher)
            .assertIsDisplayed()

        composeRule
            .onNodeWithText(popularTitle)
            .assertDoesNotExist()

        composeRule
            .onNodeWithText(noResultsTitle)
            .assertDoesNotExist()
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
        const val SEARCH_QUERY = "копил"
    }
}