package com.rustor.core.database.di

import android.content.Context
import androidx.room.Room
import com.rustor.core.database.RusTorDatabase
import com.rustor.core.database.dao.CatalogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    @Provides
    @Singleton
    internal fun provideDataBase(@ApplicationContext context: Context): RusTorDatabase = Room.databaseBuilder(
        context = context,
        klass = RusTorDatabase::class.java,
        name = DATA_BASE_NAME,
    )
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()

    @Provides
    internal fun provideCatalogDao(db: RusTorDatabase): CatalogDao = db.catalogDao()

    private const val DATA_BASE_NAME = "rustor.db"
}
