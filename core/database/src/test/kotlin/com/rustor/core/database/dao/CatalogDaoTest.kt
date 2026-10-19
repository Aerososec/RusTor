package com.rustor.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.rustor.core.database.RusTorDatabase
import com.rustor.core.database.entity.AppEntity
import com.rustor.core.database.entity.AppWithCategory
import com.rustor.core.database.entity.CategoryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CatalogDaoTest {

    private lateinit var db: RusTorDatabase
    private lateinit var dao: CatalogDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, RusTorDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.catalogDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `replace catalog keeps only the latest catalog`() = runTest {
        dao.replaceCatalog(
            categories = listOf(category("tools"), category("games")),
            apps = listOf(app("a.one", categoryId = "tools"), app("a.two", categoryId = "games")),
        )

        dao.replaceCatalog(
            categories = listOf(category("finance")),
            apps = listOf(app("b.one", categoryId = "finance")),
        )

        assertEquals(listOf("b.one"), dao.observeFeed().first().map { it.app.id })
        assertEquals(listOf("finance"), dao.observeCategories().first().map { it.id })
    }

    @Test
    fun `feed is ordered by feed position`() = runTest {
        dao.replaceCatalog(
            categories = listOf(category("tools")),
            apps = listOf(
                app("c.third", feedPosition = 2),
                app("a.first", feedPosition = 0),
                app("b.second", feedPosition = 1),
            ),
        )

        val feed = dao.observeFeed().first()

        assertEquals(listOf("a.first", "b.second", "c.third"), feed.map { it.app.id })
    }

    @Test
    fun `feed item contains its category`() = runTest {
        dao.replaceCatalog(
            categories = listOf(
                category("tools", title = "Инструменты"),
                category("games", title = "Игры"),
            ),
            apps = listOf(app("a.game", categoryId = "games")),
        )

        val item = dao.observeFeed().first().single()

        assertEquals(category("games", title = "Игры"), item.category)
    }

    @Test
    fun `categories are ordered by title`() = runTest {
        dao.replaceCatalog(
            categories = listOf(
                category("finance", title = "Финансы"),
                category("games", title = "Игры"),
                category("tools", title = "Инструменты"),
            ),
            apps = emptyList(),
        )

        val titles = dao.observeCategories().first().map { it.title }

        assertEquals(listOf("Игры", "Инструменты", "Финансы"), titles)
    }

    @Test
    fun `feed emits new catalog after replace`() = runTest {
        dao.observeFeed().test {
            assertEquals(emptyList<AppWithCategory>(), awaitItem())

            dao.replaceCatalog(
                categories = listOf(category("tools")),
                apps = listOf(app("a.one")),
            )

            assertEquals(listOf("a.one"), awaitItem().map { it.app.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `feed by category contains only that category in feed order`() = runTest {
        dao.replaceCatalog(
            categories = listOf(category("games"), category("tools")),
            apps = listOf(
                app("g.second", categoryId = "games", feedPosition = 3),
                app("t.first", categoryId = "tools", feedPosition = 1),
                app("g.first", categoryId = "games", feedPosition = 0),
                app("t.second", categoryId = "tools", feedPosition = 2),
            ),
        )

        val feed = dao.observeFeedByCategory("games").first()

        assertEquals(listOf("g.first", "g.second"), feed.map { it.app.id })
    }

    @Test
    fun `feed positions returns position of every app`() = runTest {
        dao.replaceCatalog(
            categories = listOf(category("tools")),
            apps = listOf(
                app("a.one", feedPosition = 5),
                app("b.two", feedPosition = 0),
                app("c.three", feedPosition = 2),
            ),
        )

        val positions = dao.getFeedPositions()

        assertEquals(mapOf("a.one" to 5, "b.two" to 0, "c.three" to 2), positions)
    }

    @Test
    fun `feed positions is empty for empty database`() = runTest {
        val positions = dao.getFeedPositions()

        assertEquals(emptyMap<String, Int>(), positions)
    }

    private fun category(id: String, title: String = id) = CategoryEntity(id = id, title = title)

    private fun app(id: String, categoryId: String = "tools", feedPosition: Int = 0) = AppEntity(
        id = id,
        name = "Example",
        developer = "Example Dev",
        categoryId = categoryId,
        ageRating = 0,
        iconUrl = null,
        summary = "Short summary",
        description = "Full description",
        screenshots = emptyList(),
        apkUrl = null,
        feedPosition = feedPosition,
    )
}
