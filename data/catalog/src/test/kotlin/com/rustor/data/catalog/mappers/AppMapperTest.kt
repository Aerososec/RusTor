package com.rustor.data.catalog.mappers

import com.rustor.core.database.entity.AppEntity
import com.rustor.core.model.AgeRating
import com.rustor.core.model.App
import com.rustor.core.model.AppId
import com.rustor.core.model.Category
import com.rustor.core.model.CategoryId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppMapperTest {
    @Test
    fun `valid app maps all fields to entity`() {
        val app = appDto(packageName = "com.example.app")
        val result = app.toEntity(0)

        val expected = AppEntity(
            id = "com.example.app",
            name = "Example",
            developer = "Example Dev",
            categoryId = "tools",
            ageRating = 0,
            iconUrl = "https://example.com/icon.png",
            summary = "Short summary",
            description = "Full description",
            screenshots = listOf("https://example.com/s1.png"),
            apkUrl = "https://example.com/app.apk",
            feedPosition = 0,
        )
        assertEquals(expected, result)
    }

    @Test
    fun `blank urls become null and blank screenshots are removed`() {
        val result = appDto(
            packageName = "com.example.app",
            iconUrl = " ",
            apkUrl = "",
            screenshots = listOf("a", " ", "b"),
        ).toEntity(feedPosition = 0)

        assertNull(result.iconUrl)
        assertNull(result.apkUrl)
        assertEquals(listOf("a", "b"), result.screenshots)
    }

    @Test
    fun `known age ratings are stored as min age`() {
        val cases = mapOf("0+" to 0, "6+" to 6, "8+" to 8, "12+" to 12, "16+" to 16, "18+" to 18)

        cases.forEach { (raw, expected) ->
            assertEquals("rating '$raw'", expected, storedAgeRating(raw))
        }
    }

    @Test
    fun `missing age rating is stored as 18`() {
        assertEquals(18, storedAgeRating(null))
    }

    @Test
    fun `unrecognized age rating is stored as 18`() {
        assertEquals(18, storedAgeRating("else"))
    }

    @Test
    fun `unknown numeric age rating is stored as 18`() {
        assertEquals(18, storedAgeRating("3+"))
    }

    @Test
    fun `age rating with spaces is trimmed`() {
        assertEquals(12, storedAgeRating(" 12+ "))
    }

    @Test
    fun `entity maps all fields to domain`() {
        val result = appWithCategory().toDomain()

        val expected = App(
            id = AppId("com.example.app"),
            name = "Example",
            developer = "Example Dev",
            category = Category(id = CategoryId("tools"), title = "Инструменты"),
            ageRating = AgeRating.ZERO_PLUS,
            iconUrl = "https://example.com/icon.png",
            summary = "Short summary",
            description = "Full description",
            screenshots = listOf("https://example.com/s1.png"),
            apkUrl = "https://example.com/app.apk",
        )
        assertEquals(expected, result)
    }

    @Test
    fun `unknown min age in database maps to 18+`() {
        val result = appWithCategory(app = appEntity(ageRating = 13)).toDomain()

        assertEquals(AgeRating.EIGHTEEN_PLUS, result.ageRating)
    }
}
