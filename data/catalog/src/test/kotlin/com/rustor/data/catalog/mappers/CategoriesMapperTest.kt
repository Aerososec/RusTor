package com.rustor.data.catalog.mappers

import com.rustor.data.catalog.dto.CategoryDto
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoriesMapperTest {
    @Test
    fun `category with blank id is rejected`() {
        val categories = listOf(
            CategoryDto(id = "tools", title = "Инструменты"),
            CategoryDto(id = " ", title = "Пусто"),
        )
        val dto = catalog(
            appDto(packageName = "a.valid"),
            appDto(packageName = "b.not.valid", categoryId = " "),
            categories = categories,
        )

        val result = dto.toEntities()

        assertEquals(listOf("a.valid"), result.apps.map { it.id })
        assertEquals(listOf("tools"), result.categories.map { it.id })
        assertEquals(listOf("Инструменты"), result.categories.map { it.title })
    }

    @Test
    fun `duplicate category keeps first occurrence`() {
        val categories = listOf(
            CategoryDto(id = "tools", title = "Инструменты"),
            CategoryDto(id = "tools", title = "Не инструменты"),
            CategoryDto(id = "games", title = "Игры"),
            CategoryDto(id = "games", title = "Не игры"),
        )

        val app = appDto(packageName = "com.example.app")
        val result = catalog(app, categories = categories).toEntities()

        assertEquals(listOf("tools", "games"), result.categories.map { it.id })
        assertEquals(listOf("Инструменты", "Игры"), result.categories.map { it.title })
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
    fun `category without apps is kept`() {
        val result = catalog(
            appDto(packageName = "a.valid", categoryId = "tools"),
            categories = listOf(
                CategoryDto(id = "tools", title = "Инструменты"),
                CategoryDto(id = "games", title = "Игры"),
            ),
        ).toEntities()

        assertEquals(listOf("tools", "games"), result.categories.map { it.id })
        assertEquals(listOf("a.valid"), result.apps.map { it.id })
    }
}
