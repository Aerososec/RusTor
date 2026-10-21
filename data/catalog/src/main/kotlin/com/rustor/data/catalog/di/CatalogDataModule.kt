package com.rustor.data.catalog.di

import com.rustor.core.domain.repository.CatalogRepository
import com.rustor.data.catalog.dataSourse.CatalogRemoteDataSource
import com.rustor.data.catalog.dataSourse.FakeCatalogConfig
import com.rustor.data.catalog.dataSourse.FakeCatalogRemoteDataSource
import com.rustor.data.catalog.repository.CatalogRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton
import kotlin.random.Random

@Module
@InstallIn(SingletonComponent::class)
internal abstract class CatalogDataModule {

    @Binds
    abstract fun bindCatalogRepository(impl: CatalogRepositoryImpl): CatalogRepository

    @Binds
    abstract fun bindRemoteDataSource(impl: FakeCatalogRemoteDataSource): CatalogRemoteDataSource

    companion object {

        @Provides
        fun provideRandom(): Random = Random.Default

        @Provides
        @Singleton
        fun provideJson(): Json = Json { ignoreUnknownKeys = true }

        @Provides
        fun provideFakeConfig(): FakeCatalogConfig = FakeCatalogConfig(
            delayMs = 1500,
            shouldFail = false,
        )
    }
}
