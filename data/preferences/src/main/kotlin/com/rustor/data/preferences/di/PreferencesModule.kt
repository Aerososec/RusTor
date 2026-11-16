package com.rustor.data.preferences.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.rustor.core.domain.repository.UserPreferencesRepository
import com.rustor.data.preferences.repository.UserPreferencesRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface PreferencesModule {

    @Binds
    @Singleton
    fun bindUserPreferencesRepository(implementation: UserPreferencesRepositoryImpl): UserPreferencesRepository

    companion object {

        private const val PREFERENCES_FILE_NAME = "user_preferences"

        @Provides
        @Singleton
        fun providePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
            PreferenceDataStoreFactory.create(
                produceFile = {
                    context.preferencesDataStoreFile(PREFERENCES_FILE_NAME)
                },
            )
    }
}
