package com.rustor.data.catalog.mappers

import com.rustor.core.database.entity.CatalogEntities
import com.rustor.core.database.entity.PopularAppEntity
import com.rustor.data.catalog.dto.CatalogDto

fun CatalogDto.toEntities(): CatalogEntities {
    val categoryEntities = categories
        .filter { it.id.isNotBlank() }
        .distinctBy { it.id }
        .map { it.toEntity() }

    val knownCategoryIds = categoryEntities.map { it.id }.toSet()

    val appEntities = apps
        .filter { it.packageName.isNotBlank() }
        .filter { it.name.isNotBlank() }
        .filter { it.categoryId in knownCategoryIds }
        .distinctBy { it.packageName }
        .mapIndexed { index, dto ->
            dto.toEntity(feedPosition = index)
        }

    val knownAppIds = appEntities.map { it.id }.toSet()

    val popularAppEntities = popularAppIds
        .distinct()
        .filter { it in knownAppIds }
        .mapIndexed { index, appId ->
            PopularAppEntity(
                appId = appId,
                position = index,
            )
        }

    return CatalogEntities(
        categories = categoryEntities,
        apps = appEntities,
        popularApps = popularAppEntities,
    )
}
