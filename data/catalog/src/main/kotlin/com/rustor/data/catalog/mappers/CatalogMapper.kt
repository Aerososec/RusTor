package com.rustor.data.catalog.mappers

import com.rustor.core.database.entity.CatalogEntities
import com.rustor.core.database.entity.CategoryEntity
import com.rustor.data.catalog.dto.CatalogDto

fun CatalogDto.toEntities(): CatalogEntities {
    val categoryEntities = categories
        .filter { it.id.isNotBlank() }
        .distinctBy { it.id }
        .map { CategoryEntity(id = it.id, title = it.title) }

    val knownCategoryIds = categoryEntities.map { it.id }.toSet()

    val appEntities = apps
        .filter { it.packageName.isNotBlank() }
        .filter { it.categoryId in knownCategoryIds }
        .distinctBy { it.packageName }
        .mapIndexed { index, dto -> dto.toEntity(feedPosition = index) }

    return CatalogEntities(categoryEntities, appEntities)
}
