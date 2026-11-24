package com.rustor.data.catalog.repository

import com.rustor.core.common.error.AppException
import com.rustor.core.database.dao.CatalogDao
import com.rustor.core.domain.repository.CatalogRepository
import com.rustor.core.model.App
import com.rustor.core.model.AppId
import com.rustor.core.model.Category
import com.rustor.core.model.CategoryId
import com.rustor.core.model.CategorySummary
import com.rustor.data.catalog.dataSourse.CatalogRemoteDataSource
import com.rustor.data.catalog.mappers.assignPositions
import com.rustor.data.catalog.mappers.mapErrors
import com.rustor.data.catalog.mappers.toDomain
import com.rustor.data.catalog.mappers.toEntities
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException
import kotlin.random.Random

@Singleton
internal class CatalogRepositoryImpl @Inject constructor(
    private val catalogDao: CatalogDao,
    private val remote: CatalogRemoteDataSource,
    private val random: Random,
) : CatalogRepository {

    private val mutex = Mutex()

    override fun observeFeed(): Flow<List<App>> = observeCatalogRead {
        catalogDao.observeFeed().map { rows ->
            rows.map { it.toDomain() }
        }
    }

    override fun observeFeedByCategory(categoryId: CategoryId): Flow<List<App>> = observeCatalogRead {
        catalogDao.observeFeedByCategory(categoryId = categoryId.value).map { rows ->
            rows.map { it.toDomain() }
        }
    }

    override fun observeCategories(): Flow<List<Category>> = observeCatalogRead {
        catalogDao.observeCategories().map { rows ->
            rows.map { it.toDomain() }
        }
    }

    override fun observeCategorySummaries(): Flow<List<CategorySummary>> = observeCatalogRead {
        catalogDao.observeCategorySummaries().map { rows ->
            rows.map { it.toDomain() }
        }
    }

    override suspend fun refresh(reshuffle: Boolean) = mutex.withLock {
        mapErrors {
            val dto = remote.fetchCatalog()

            val entities = dto.toEntities()
            val existing = if (reshuffle) emptyMap() else catalogDao.getFeedPositions()
            val ordered = assignPositions(entities.apps, existing, random)

            catalogDao.replaceCatalog(
                categories = entities.categories,
                apps = ordered,
                popularApps = entities.popularApps,
            )
        }
    }

    override fun observeApp(appId: AppId): Flow<App?> = observeCatalogRead {
        catalogDao.observeApp(appId.value).map { app ->
            app?.toDomain()
        }
    }

    override fun observePopularApps(): Flow<List<App>> = observeCatalogRead {
        catalogDao.observePopularApps().map { apps ->
            apps.map { it.toDomain() }
        }
    }

    private fun <T> observeCatalogRead(source: () -> Flow<T>): Flow<T> = flow {
        emitAll(source())
    }.catch { cause ->
        when (cause) {
            is CancellationException -> throw cause
            is AppException -> throw cause
            is Exception -> throw AppException.Unknown(cause)
            else -> throw cause
        }
    }
}
