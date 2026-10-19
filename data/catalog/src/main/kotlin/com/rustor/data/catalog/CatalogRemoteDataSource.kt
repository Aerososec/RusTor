package com.rustor.data.catalog

import com.rustor.data.catalog.dto.CatalogDto

internal interface CatalogRemoteDataSource {
    suspend fun fetchCatalog(): CatalogDto
}
