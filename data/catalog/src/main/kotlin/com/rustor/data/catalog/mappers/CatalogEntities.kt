package com.rustor.data.catalog.mappers

import com.rustor.core.database.entity.AppEntity
import com.rustor.core.database.entity.CategoryEntity
import com.rustor.core.database.entity.PopularAppEntity

data class CatalogEntities(
    val categories: List<CategoryEntity>,
    val apps: List<AppEntity>,
    val popularApps: List<PopularAppEntity> = emptyList(),
)
