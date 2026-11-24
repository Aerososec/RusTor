package com.rustor.data.catalog.repository

import android.content.Context
import android.database.sqlite.SQLiteException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.rustor.core.common.error.AppException
import com.rustor.core.database.RusTorDatabase
import com.rustor.core.database.dao.CatalogDao
import com.rustor.core.model.AppId
import com.rustor.data.catalog.dataSourse.CatalogRemoteDataSource
import com.rustor.data.catalog.dto.CatalogDto
import com.rustor.data.catalog.mappers.appDto
import com.rustor.data.catalog.mappers.catalog
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
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
    fun `source io error becomes no internet and keeps old catalog and popular apps`() = runTest {
        val popularIds = listOf("c", "a")
        remote.catalog = catalogOf("a", "b", "c").copy(popularAppIds = popularIds)
        repository.refresh(reshuffle = true)

        val feedBefore = repository.observeFeed().first()
        val popularBefore = repository.observePopularApps().first()
        assertEquals(popularIds, popularBefore.map { it.id.value })

        remote.error = IOException("offline")

        assertFailsWith<AppException.NoInternet> {
            repository.refresh(reshuffle = false)
        }

        assertEquals(feedBefore, repository.observeFeed().first())
        assertEquals(popularBefore, repository.observePopularApps().first())
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

    @Test
    fun `refresh stores popular apps from dto in their original order`() = runTest {
        val popularIds = listOf("c", "a")
        remote.catalog = catalogOf("a", "b", "c").copy(popularAppIds = popularIds)

        repository.refresh(reshuffle = true)

        assertEquals(popularIds, dao.observePopularApps().first().map { it.app.id })
        assertEquals(popularIds, repository.observePopularApps().first().map { it.id.value })
    }

    @Test
    fun `observeApp wraps failure when DAO creates the flow`() = runTest {
        val cause = SQLiteException("Cannot open catalog")
        val failingDao = mockk<CatalogDao>()
        every { failingDao.observeApp("a") } throws cause
        val subject = CatalogRepositoryImpl(failingDao, remote, Random(42))

        val error = assertFailsWith<AppException.Unknown> {
            subject.observeApp(AppId("a")).first()
        }

        assertSame(cause, error.cause)
    }

    @Test
    fun `observePopularApps wraps failure during collection`() = runTest {
        val cause = IllegalStateException("Cannot read popular apps")
        val failingDao = mockk<CatalogDao>()
        every { failingDao.observePopularApps() } returns flow { throw cause }
        val subject = CatalogRepositoryImpl(failingDao, remote, Random(42))

        val error = assertFailsWith<AppException.Unknown> {
            subject.observePopularApps().first()
        }

        assertSame(cause, error.cause)
    }

    @Test
    fun `observePopularApps propagates cancellation`() = runTest {
        val failingDao = mockk<CatalogDao>()
        every { failingDao.observePopularApps() } returns flow {
            throw CancellationException("Collection cancelled")
        }
        val subject = CatalogRepositoryImpl(failingDao, remote, Random(42))

        assertFailsWith<CancellationException> {
            subject.observePopularApps().first()
        }
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
