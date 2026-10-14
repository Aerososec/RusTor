package com.rustor.data.catalog.mappers

import com.rustor.core.database.entity.AppEntity
import com.rustor.core.model.AgeRating
import com.rustor.data.catalog.dto.AppDto
import com.rustor.data.catalog.dto.CatalogDto
import com.rustor.data.catalog.dto.CategoryDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CatalogMapperTest {
    @Test
    fun `valid app maps all fields to entity`() {
        val app = appDto(packageName = "com.example.app")
        val result = catalog(app).toEntities()

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
        assertEquals(expected, result.apps.single())
    }

    @Test
    fun `valid apps are kept in order`() {
        val dto = catalog(
            appDto(packageName = "a.valid"),
            appDto(packageName = "b.valid"),
        )

        val result = dto.toEntities()

        assertEquals(listOf("a.valid", "b.valid"), result.apps.map { it.id })
    }

    @Test
    fun `app with blank name is rejected`() {
        val dto = catalog(
            appDto(packageName = "a.valid"),
            appDto(packageName = "b.blank", name = "   "),
        )

        val result = dto.toEntities()

        assertEquals(listOf("a.valid"), result.apps.map { it.id })
    }

    @Test
    fun `app with blank package name is rejected`() {
        val dto = catalog(
            appDto(packageName = " "),
            appDto(packageName = "b.valid"),
        )

        val result = dto.toEntities()

        assertEquals(listOf("b.valid"), result.apps.map { it.id })
    }

    @Test
    fun `app with unknown category is rejected`() {
        val dto = catalog(
            appDto(packageName = "a.valid"),
            appDto(packageName = "b.not.valid", categoryId = "qwerty"),
        )

        val result = dto.toEntities()
        assertEquals(listOf("a.valid"), result.apps.map { it.id })
    }

    @Test
    fun `duplicate app keeps first occurrence`() {
        val dto = catalog(
            appDto(packageName = "a.valid", name = "a.one"),
            appDto(packageName = "b.valid", name = "b.one"),
            appDto(packageName = "b.valid", name = "b.two"),
            appDto(packageName = "a.valid", name = "a.two"),
        )

        val result = dto.toEntities()

        assertEquals(listOf("a.valid", "b.valid"), result.apps.map { it.id })
        assertEquals(listOf("a.one", "b.one"), result.apps.map { it.name })
    }

    @Test
    fun `age rating is stored as min age`() {
        val dto = catalog(
            appDto(packageName = "a.zero", ageRating = "0+"),
            appDto(packageName = "a.six", ageRating = "6+"),
            appDto(packageName = "a.eight", ageRating = "8+"),
            appDto(packageName = "a.twelve", ageRating = "12+"),
            appDto(packageName = "a.sixteen", ageRating = "16+"),
            appDto(packageName = "a.eighteen", ageRating = "18+"),
            appDto(packageName = "a.null", ageRating = null),
            appDto(packageName = "a.else", ageRating = "else"),
        )

        val result = dto.toEntities()

        assertEquals(listOf(0, 6, 8, 12, 16, 18, 18, 18), result.apps.map { it.ageRating })
    }

    @Test
    fun `feed positions are consecutive after filtering`() {
        val dto = catalog(
            appDto(packageName = "a.valid"),
            appDto(packageName = "b.blank", name = "   "),
            appDto(packageName = "c.valid"),
        )

        val result = dto.toEntities()

        assertEquals(listOf("a.valid", "c.valid"), result.apps.map { it.id })
        assertEquals(listOf(0, 1), result.apps.map { it.feedPosition })
    }

    @Test
    fun `age rating with spaces is trimmed`() {
        assertEquals(AgeRating.TWELVE_PLUS, " 12+ ".toAgeRating())
    }

    @Test
    fun `blank urls become null and blank screenshots are removed`() {
        val result = catalog(
            appDto(packageName = "example.com", iconUrl = " ", apkUrl = "", screenshots = listOf("a", " ", "b")),
        ).toEntities()

        val app = result.apps.single()
        assertNull(app.iconUrl)
        assertNull(app.apkUrl)
        assertEquals(listOf("a", "b"), app.screenshots)
    }

    private fun catalog(vararg apps: AppDto) = CatalogDto(
        categories = listOf(CategoryDto(id = "tools", title = "Инструменты")),
        apps = apps.toList(),
    )

    private fun appDto(
        packageName: String,
        name: String = "Example",
        developer: String = "Example Dev",
        categoryId: String = "tools",
        ageRating: String? = "0+",
        iconUrl: String? = "https://example.com/icon.png",
        summary: String = "Short summary",
        description: String = "Full description",
        screenshots: List<String> = listOf("https://example.com/s1.png"),
        apkUrl: String? = "https://example.com/app.apk",
    ) = AppDto(
        packageName = packageName,
        name = name,
        developer = developer,
        categoryId = categoryId,
        ageRating = ageRating,
        iconUrl = iconUrl,
        summary = summary,
        description = description,
        screenshots = screenshots,
        apkUrl = apkUrl,
    )
}
