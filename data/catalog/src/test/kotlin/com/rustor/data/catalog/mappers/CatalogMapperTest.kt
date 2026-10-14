package com.rustor.data.catalog.mappers

import org.junit.Assert.assertEquals
import org.junit.Test

class CatalogMapperTest {
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
}
