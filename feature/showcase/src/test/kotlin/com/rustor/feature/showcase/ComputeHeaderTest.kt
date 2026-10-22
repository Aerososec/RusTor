package com.rustor.feature.showcase

import com.rustor.core.model.Category
import com.rustor.core.model.CategoryId
import com.rustor.feature.showcase.uiState.ShowcaseHeader
import com.rustor.feature.showcase.uiState.ShowcaseMode
import com.rustor.feature.showcase.uiState.computeHeader
import org.junit.Test
import kotlin.test.assertEquals

class ComputeHeaderTest {

    private val categories = listOf(
        Category(id = CategoryId("games"), title = "Игры"),
        Category(id = CategoryId("tools"), title = "Инструменты"),
    )

    @Test
    fun `category page gives null title when category is not in list`() {
        val result = computeHeader(
            mode = ShowcaseMode.Category(CategoryId("finance")),
            categories = categories,
            selectedCategory = null,
        )

        assertEquals(ShowcaseHeader.CategoryPage(title = null), result)
    }

    @Test
    fun `category page gives title of that category`() {
        val result = computeHeader(
            mode = ShowcaseMode.Category(CategoryId("tools")),
            categories = categories,
            selectedCategory = null,
        )

        assertEquals(ShowcaseHeader.CategoryPage(title = "Инструменты"), result)
    }
}
