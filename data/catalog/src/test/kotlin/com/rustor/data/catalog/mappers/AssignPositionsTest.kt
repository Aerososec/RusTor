package com.rustor.data.catalog.mappers

import com.rustor.core.database.entity.AppEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class AssignPositionsTest {

    @Test
    fun `empty existing assigns positions from zero`() {
        val result = assignPositions(apps("a", "b", "c"), existing = emptyMap(), Random(42))

        assertEquals(setOf("a", "b", "c"), result.map { it.id }.toSet())
        assertEquals(listOf(0, 1, 2), result.map { it.feedPosition }.sorted())
    }

    @Test
    fun `existing apps keep their positions`() {
        val result = assignPositions(apps("a", "b"), mapOf("a" to 5, "b" to 2), Random(42))

        assertEquals(mapOf("a" to 5, "b" to 2), result.positions())
    }

    @Test
    fun `new apps go after max kept position`() {
        val result = assignPositions(apps("a", "b", "n"), mapOf("a" to 0, "b" to 3), Random(42))

        assertEquals(mapOf("a" to 0, "b" to 3, "n" to 4), result.positions())
    }

    @Test
    fun `several new apps get consecutive positions after max`() {
        val result = assignPositions(apps("a", "n1", "n2", "n3"), mapOf("a" to 2), Random(42))

        assertEquals(2, result.positions().getValue("a"))
        assertEquals(listOf(3, 4, 5), result.filter { it.id != "a" }.map { it.feedPosition }.sorted())
    }

    @Test
    fun `removed apps are not in result and do not shift others`() {
        val result = assignPositions(apps("a", "c"), mapOf("a" to 0, "b" to 1, "c" to 2), Random(42))

        assertEquals(mapOf("a" to 0, "c" to 2), result.positions())
    }

    @Test
    fun `new apps start from zero when no existing app is kept`() {
        val result = assignPositions(apps("n1", "n2"), mapOf("old" to 7), Random(42))

        assertEquals(listOf(0, 1), result.map { it.feedPosition }.sorted())
    }

    @Test
    fun `removed app position does not affect new apps`() {
        val result = assignPositions(apps("a", "n"), mapOf("a" to 0, "b" to 9), Random(42))

        assertEquals(mapOf("a" to 0, "n" to 1), result.positions())
    }

    @Test
    fun `existing positions do not depend on random`() {
        val input = apps("a", "b", "n1", "n2")
        val existing = mapOf("a" to 0, "b" to 1)

        val first = assignPositions(input, existing, Random(1)).positions()
        val second = assignPositions(input, existing, Random(2)).positions()

        assertEquals(0, first.getValue("a"))
        assertEquals(1, first.getValue("b"))
        assertEquals(first.getValue("a"), second.getValue("a"))
        assertEquals(first.getValue("b"), second.getValue("b"))
    }

    @Test
    fun `same seed gives same result`() {
        val input = apps("a", "b", "c", "d", "e")

        val first = assignPositions(input, emptyMap(), Random(42))
        val second = assignPositions(input, emptyMap(), Random(42))

        assertEquals(first, second)
    }

    @Test
    fun `reshuffle changes order`() {
        val ids = listOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j")

        val result = assignPositions(apps(*ids.toTypedArray()), emptyMap(), Random(42))

        assertNotEquals(ids, result.map { it.id })
    }

    @Test
    fun `input feed positions are ignored`() {
        val input = listOf(
            appEntity(id = "a", feedPosition = 99),
            appEntity(id = "b", feedPosition = 99),
            appEntity(id = "c", feedPosition = 99),
        )

        val result = assignPositions(input, emptyMap(), Random(42))

        assertEquals(listOf(0, 1, 2), result.map { it.feedPosition }.sorted())
    }

    @Test
    fun `only feed position is changed`() {
        val input = appEntity(id = "a", ageRating = 12, feedPosition = 99)

        val result = assignPositions(listOf(input), emptyMap(), Random(42)).single()

        assertEquals(input.copy(feedPosition = 0), result)
    }

    @Test
    fun `empty input gives empty result`() {
        val result = assignPositions(emptyList(), mapOf("a" to 0), Random(42))

        assertTrue(result.isEmpty())
    }

    @Test
    fun `result is sorted by position`() {
        val result = assignPositions(apps("c", "a", "b"), mapOf("a" to 0, "b" to 1, "c" to 2), Random(42))

        assertEquals(listOf("a", "b", "c"), result.map { it.id })
    }

    private fun apps(vararg ids: String): List<AppEntity> = ids.map { appEntity(id = it) }

    private fun List<AppEntity>.positions(): Map<String, Int> = associate { it.id to it.feedPosition }
}
