package com.rustor.data.catalog.dataSourse

import android.content.Context
import com.rustor.core.common.dispatchers.IoDispatcher
import com.rustor.data.catalog.dto.CatalogDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

internal class FakeCatalogRemoteDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val config: FakeCatalogConfig,
) : CatalogRemoteDataSource {
    override suspend fun fetchCatalog(): CatalogDto = withContext(ioDispatcher) {
        delay(config.delayMs.milliseconds)
        if (config.shouldFail) throw IOException("Fake: no connection")

        val text = context.assets.open(CATALOG_FILE).bufferedReader().use { it.readText() }
        json.decodeFromString<CatalogDto>(text)
    }

    private companion object {
        const val CATALOG_FILE = "catalog.json"
    }
}

internal data class FakeCatalogConfig(val delayMs: Long, val shouldFail: Boolean)
