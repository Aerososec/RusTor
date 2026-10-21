package com.rustor.data.catalog.repository

import com.rustor.core.database.dao.CatalogDao
import com.rustor.core.domain.repository.CatalogRepository
import com.rustor.core.model.App
import com.rustor.core.model.Category
import com.rustor.core.model.CategoryId
import com.rustor.data.catalog.dataSourse.CatalogRemoteDataSource
import com.rustor.data.catalog.mappers.assignPositions
import com.rustor.data.catalog.mappers.mapErrors
import com.rustor.data.catalog.mappers.toDomain
import com.rustor.data.catalog.mappers.toEntities
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
internal class CatalogRepositoryImpl @Inject constructor(
    private val catalogDao: CatalogDao,
    private val remote: CatalogRemoteDataSource,
    private val random: Random,
) : CatalogRepository {

    private val mutex = Mutex()

    override fun observeFeed(): Flow<List<App>> = catalogDao.observeFeed().map { list -> list.map { it.toDomain() } }

    override fun observeFeedByCategory(categoryId: CategoryId): Flow<List<App>> = catalogDao.observeFeedByCategory(
        categoryId = categoryId.value,
    ).map { list -> list.map { it.toDomain() } }

    override fun observeCategories(): Flow<List<Category>> = catalogDao.observeCategories().map { list ->
        list.map { it.toDomain() }
    }

    override suspend fun refresh(reshuffle: Boolean) = mutex.withLock {
        mapErrors {
            val dto = remote.fetchCatalog()

            val entities = dto.toEntities()
            val existing = if (reshuffle) emptyMap() else catalogDao.getFeedPositions()
            val ordered = assignPositions(entities.apps, existing, random)

            catalogDao.replaceCatalog(entities.categories, ordered)
        }
    }
}
