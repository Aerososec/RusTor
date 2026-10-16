package com.rustor.core.database.converter

import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenshotsConverterTest {

    private val converter = ScreenshotsConverter()

    @Test
    fun `list is joined with line breaks`() {
        val list = listOf(
            "urlOne",
            "urlTwo",
            "urlThree",
            "urlFour",
        )

        val result = converter.fromList(list)

        assertEquals("urlOne\nurlTwo\nurlThree\nurlFour", result)
    }

    @Test
    fun `string is split by line breaks`() {
        val string = "urlOne\nurlTwo\nurlThree\nurlFour"
        val list = listOf(
            "urlOne",
            "urlTwo",
            "urlThree",
            "urlFour",
        )

        val result = converter.toList(string)

        assertEquals(list, result)
    }

    @Test
    fun `list survives round trip`() {
        val original = listOf("urlOne", "urlTwo", "urlThree")

        val result = converter.toList(converter.fromList(original))

        assertEquals(original, result)
    }

    @Test
    fun `empty list survives round trip`() {
        val result = converter.toList(converter.fromList(emptyList()))

        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `single element survives round trip`() {
        val result = converter.toList(converter.fromList(listOf("urlOne")))

        assertEquals(listOf("urlOne"), result)
    }

    @Test
    fun `list with single blank url collapses to empty`() {
        val result = converter.toList(converter.fromList(listOf("")))

        assertEquals(emptyList<String>(), result)
    }
}
