package com.rustor.navigation

import android.content.Context
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rustor.MainActivity
import com.rustor.core.model.App
import com.rustor.core.model.Category
import com.rustor.feature.categories.CATEGORIES_LIST_TAG
import com.rustor.feature.showcase.SHOWCASE_FEED_TAG
import com.rustor.navigation.RusTorTab
import com.rustor.testing.CatalogTestEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.rustor.feature.appdetails.R as AppDetailsR

@RunWith(AndroidJUnit4::class)
class CategoryNavigationTest {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    private val entryPoint: CatalogTestEntryPoint
        get() = EntryPointAccessors.fromApplication(
            context,
            CatalogTestEntryPoint::class.java,
        )

    private lateinit var category: Category
    private lateinit var expectedApps: List<App>

    private val categoriesTabMatcher: SemanticsMatcher
        get() = hasText(RusTorTab.CATEGORIES.label) and isSelectable()

    private val categoriesListMatcher =
        hasTestTag(CATEGORIES_LIST_TAG ) and hasScrollToIndexAction()

    private val feedMatcher =
        hasTestTag(SHOWCASE_FEED_TAG) and hasScrollToIndexAction()

    @Before
    fun setUp() {
        runBlocking {
            entryPoint.preferencesDataStore().edit { preferences ->
                preferences.clear()
            }

            entryPoint.userPreferencesRepository().completeOnboarding()

            val repository = entryPoint.catalogRepository()
            repository.refresh(reshuffle = false)

            category = repository.observeCategories()
                .first()
                .single { it.id.value == CATEGORY_ID }

            val catalog = repository.observeFeed().first()

            expectedApps = catalog.filter { app ->
                app.category.id == category.id
            }

            assertTrue(
                "Для сценария нужны минимум два приложения выбранной категории",
                expectedApps.size >= 2,
            )

            assertTrue(
                "Для проверки фильтрации нужны приложения других категорий",
                catalog.any { it.category.id != category.id },
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
    fun categorySelection_filtersFeed_andBackRestoresFeedThenCategories() {
        ActivityScenario.launch(MainActivity::class.java).use {
            waitForNode(feedMatcher)

            composeRule
                .onNode(categoriesTabMatcher)
                .assertIsDisplayed()
                .performClick()

            waitForNode(categoriesListMatcher)

            composeRule
                .onNode(categoriesListMatcher)
                .performScrollToNode(
                    hasText(category.title) and hasClickAction(),
                )

            composeRule
                .onNode(
                    hasText(category.title) and
                            hasClickAction() and
                            hasAnyAncestor(hasTestTag(CATEGORIES_LIST_TAG)),
                )
                .assertIsDisplayed()
                .performClick()

            waitForNode(feedMatcher)
            assertFeedPageCount()

            expectedApps.forEachIndexed { index, app ->
                composeRule
                    .onNode(feedMatcher)
                    .performScrollToIndex(index)

                appCard(app)
                    .assertIsDisplayed()
                    .assert(hasText(category.title))
            }

            val selectedApp = expectedApps.last()

            appCard(selectedApp).performClick()

            val detailsTitle = context.getString(
                AppDetailsR.string.app_details_title,
            )

            waitForNode(hasText(detailsTitle))

            composeRule
                .onNodeWithText(detailsTitle)
                .assertIsDisplayed()

            waitForNode(hasText(selectedApp.name) and isHeading())

            composeRule
                .onNode(hasText(selectedApp.name) and isHeading())
                .assertIsDisplayed()

            composeRule
                .onNodeWithText(selectedApp.developer)
                .assertIsDisplayed()

            composeRule
                .onNodeWithText(category.title)
                .assertIsDisplayed()

            pressBack()

            waitForNode(feedMatcher)
            assertFeedPageCount()

            appCard(selectedApp)
                .assertIsDisplayed()
                .assert(hasText(category.title))

            composeRule
                .onNodeWithText(detailsTitle)
                .assertDoesNotExist()

            pressBack()

            waitForNode(categoriesListMatcher)

            composeRule
                .onNode(categoriesListMatcher)
                .assertIsDisplayed()

            composeRule
                .onNode(categoriesTabMatcher)
                .assertIsSelected()

            composeRule
                .onNode(feedMatcher)
                .assertDoesNotExist()
        }
    }

    private fun appCard(app: App): SemanticsNodeInteraction =
        composeRule.onNode(
            hasText(app.name) and
                    hasClickAction() and
                    hasAnyAncestor(hasTestTag(SHOWCASE_FEED_TAG)),
        )

    private fun assertFeedPageCount() {
        val collectionInfo = composeRule
            .onNode(feedMatcher)
            .fetchSemanticsNode()
            .config[SemanticsProperties.CollectionInfo]

        assertEquals(
            "В витрине должны быть только приложения выбранной категории",
            expectedApps.size,
            collectionInfo.rowCount,
        )
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
        const val CATEGORY_ID = "finance"
    }
}