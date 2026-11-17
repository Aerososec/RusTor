package com.rustor.testing

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.rustor.core.domain.repository.CatalogRepository
import com.rustor.core.domain.repository.UserPreferencesRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface CatalogTestEntryPoint {
    fun catalogRepository(): CatalogRepository

    fun userPreferencesRepository(): UserPreferencesRepository

    fun preferencesDataStore(): DataStore<Preferences>
}