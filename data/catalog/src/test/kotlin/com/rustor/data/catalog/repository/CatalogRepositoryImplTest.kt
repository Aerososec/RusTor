package com.rustor.data.catalog.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.rustor.core.common.error.AppException
import com.rustor.core.database.RusTorDatabase
import com.rustor.core.database.dao.CatalogDao
import com.rustor.data.catalog.dataSourse.CatalogRemoteDataSource
import com.rustor.data.catalog.dto.CatalogDto
import com.rustor.data.catalog.mappers.appDto
import com.rustor.data.catalog.mappers.catalog
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import kotlin.random.Random
import kotlin.test.assertFailsWith

@RunWith(RobolectricTestRunner::class)
class CatalogRepositoryImplTest {

    private lateinit var db: RusTorDatabase
    private lateinit var dao: CatalogDao
    private lateinit var remote: TestRemoteDataSource
    private lateinit var repository: CatalogRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, RusTorDatabase::class.java).build()
        dao = db.catalogDao()
        remote = TestRemoteDataSource()
        repository = CatalogRepositoryImpl(dao, remote, Random(42))
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `refresh stores catalog and feed follows positions`() = runTest {
        remote.catalog = catalogOf("a", "b", "c")

        repository.refresh(reshuffle = true)

        val feedIds = repository.observeFeed().first().map { it.id.value }
        val idsByPosition = dao.getFeedPositions().entries.sortedBy { it.value }.map { it.key }
        assertEquals(setOf("a", "b", "c"), feedIds.toSet())
        assertEquals(idsByPosition, feedIds)
    }

    @Test
    fun `background refresh keeps old positions and appends new apps`() = runTest {
        remote.catalog = catalogOf("a", "b", "c")
        repository.refresh(reshuffle = true)
        val before = dao.getFeedPositions()

        remote.catalog = catalogOf("a", "b", "c", "d")
        repository.refresh(reshuffle = false)
        val after = dao.getFeedPositions()

        assertEquals(before, after - "d")
        assertEquals(before.values.max() + 1, after.getValue("d"))
    }

    @Test
    fun `reshuffle refresh changes feed order`() = runTest {
        remote.catalog = catalogOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j")
        repository.refresh(reshuffle = true)
        val firstOrder = repository.observeFeed().first().map { it.id.value }

        repository.refresh(reshuffle = true)
        val secondOrder = repository.observeFeed().first().map { it.id.value }

        assertNotEquals(firstOrder, secondOrder)
    }

    @Test
    fun `source io error becomes no internet and keeps old catalog`() = runTest {
        remote.catalog = catalogOf("a", "b")
        repository.refresh(reshuffle = true)
        val before = repository.observeFeed().first()

        remote.error = IOException("offline")

        assertFailsWith<AppException.NoInternet> {
            repository.refresh(reshuffle = false)
        }
        assertEquals(before, repository.observeFeed().first())
    }

    @Test
    fun `concurrent refresh calls do not hit source at the same time`() = runTest {
        val source = GatedRemoteDataSource(catalogOf("a", "b"))
        val repository = CatalogRepositoryImpl(dao, source, Random(42))

        val first = launch { repository.refresh(reshuffle = true) }
        val second = launch { repository.refresh(reshuffle = false) }
        runCurrent()

        assertEquals(1, source.calls)

        source.gate.complete(Unit)
        joinAll(first, second)

        assertEquals(2, source.calls)
        assertEquals(1, source.maxActive)
    }

    private fun catalogOf(vararg ids: String): CatalogDto =
        catalog(*ids.map { appDto(packageName = it) }.toTypedArray())

    private class TestRemoteDataSource : CatalogRemoteDataSource {
        var catalog: CatalogDto = catalog()
        var error: Throwable? = null

        override suspend fun fetchCatalog(): CatalogDto {
            error?.let { throw it }
            return catalog
        }
    }

    private class GatedRemoteDataSource(private val result: CatalogDto) : CatalogRemoteDataSource {
        val gate = CompletableDeferred<Unit>()
        var calls = 0
            private set
        var maxActive = 0
            private set
        private var active = 0

        override suspend fun fetchCatalog(): CatalogDto {
            calls++
            active++
            maxActive = maxOf(maxActive, active)
            try {
                gate.await()
                return result
            } finally {
                active--
            }
        }
    }
}
