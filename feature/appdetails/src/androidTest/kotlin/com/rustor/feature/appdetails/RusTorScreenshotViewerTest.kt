package com.rustor.feature.appdetails

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rustor.feature.appdetails.R
import com.rustor.core.ui.theme.RusTorTheme
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.swipeDown
import com.rustor.feature.appdetails.details.RusTorScreenshotViewer
import com.rustor.feature.appdetails.details.SCREENSHOT_REFRESH_AREA_TAG

@RunWith(AndroidJUnit4::class)
class RusTorScreenshotViewerTest {

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @get:Rule
    val composeRule = createComposeRule()

    @get:Rule
    val imageFolder = TemporaryFolder(context.cacheDir)

    @Test
    fun swipeForwardAndBack_changesImageAndPosition() {
        val first = imageFolder.newFile("first.png")
        val second = imageFolder.newFile("second.png")
        writePng(first, Color.RED)
        writePng(second, Color.BLUE)

        showViewer(files = listOf(first, second), initialIndex = 0)

        val firstPosition = positionText(position = 1, total = 2)
        val secondPosition = positionText(position = 2, total = 2)

        assertPageDisplayed(firstPosition, Color.RED)

        composeRule.onNodeWithContentDescription(firstPosition)
            .performTouchInput { swipeLeft() }

        assertPageDisplayed(secondPosition, Color.BLUE)

        composeRule.onNodeWithContentDescription(secondPosition)
            .performTouchInput { swipeRight() }

        assertPageDisplayed(firstPosition, Color.RED)
    }

    @Test
    fun imageLoadError_retryClick_loadsCurrentImage() {
        val first = imageFolder.newFile("first.png")
        writePng(first, Color.RED)

        val second = File(imageFolder.root, "second.png")
        showViewer(files = listOf(first, second), initialIndex = 1)

        val currentPosition = positionText(position = 2, total = 2)
        val errorMessage = context.getString(R.string.rustor_screenshot_error)
        val retryDescription = context.getString(R.string.rustor_reload_screenshot)

        waitForNode(hasText(errorMessage))
        composeRule.onNodeWithText(currentPosition).assertIsDisplayed()
        composeRule.onNodeWithText(errorMessage).assertIsDisplayed()

        writePng(second, Color.BLUE)
        composeRule.onNodeWithText(errorMessage).assertIsDisplayed()

        composeRule.onNodeWithContentDescription(retryDescription)
            .assertIsDisplayed()
            .performClick()

        assertPageDisplayed(currentPosition, Color.BLUE)
        composeRule.onNodeWithText(errorMessage).assertDoesNotExist()
    }

    @Test
    fun imageLoadError_swipeDown_reloadsCurrentScreenshot() {
        val first = imageFolder.newFile("first.png")
        writePng(first, Color.RED)

        val second = File(imageFolder.root, "second.png")
        showViewer(files = listOf(first, second), initialIndex = 1)

        val position = positionText(position = 2, total = 2)
        val errorMessage = context.getString(R.string.rustor_screenshot_error)

        waitForNode(hasText(errorMessage))
        composeRule.onNodeWithText(position).assertIsDisplayed()

        writePng(second, Color.BLUE)
        composeRule.onNodeWithTag(SCREENSHOT_REFRESH_AREA_TAG)
            .performTouchInput { swipeDown() }

        assertPageDisplayed(position, Color.BLUE)
        composeRule.onNodeWithText(errorMessage).assertDoesNotExist()
    }

    private fun showViewer(files: List<File>, initialIndex: Int) {
        val urls = files.map { file -> Uri.fromFile(file).toString() }

        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                RusTorTheme {
                    RusTorScreenshotViewer(
                        screenshotUrls = urls,
                        initialIndex = initialIndex,
                        onBackClick = {},
                    )
                }
            }
        }
    }

    private fun assertPageDisplayed(position: String, expectedColor: Int) {
        waitForNode(hasText(position))
        composeRule.onNodeWithText(position).assertIsDisplayed()

        waitForNode(hasContentDescription(position))
        val image = composeRule.onNodeWithContentDescription(position)
        image.assertIsDisplayed()

        val pixels = image.captureToImage().toPixelMap()
        val centerColor = pixels[pixels.width / 2, pixels.height / 2].toArgb()
        assertEquals("Неверное изображение для позиции «$position»", expectedColor, centerColor)
    }

    private fun positionText(position: Int, total: Int): String =
        context.getString(R.string.rustor_screenshot_position, position, total)

    private fun waitForNode(matcher: SemanticsMatcher) {
        composeRule.waitUntil(timeoutMillis = 10_000L) {
            composeRule.onAllNodes(matcher).fetchSemanticsNodes().size == 1
        }
    }

    private fun writePng(file: File, color: Int) {
        val bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
        try {
            bitmap.eraseColor(color)
            file.outputStream().use { output ->
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                    "Не удалось записать тестовое изображение ${file.name}"
                }
            }
        } finally {
            bitmap.recycle()
        }
    }
}