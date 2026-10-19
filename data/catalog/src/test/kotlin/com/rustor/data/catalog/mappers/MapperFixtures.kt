package com.rustor.data.catalog.mappers

import com.rustor.core.database.entity.AppEntity
import com.rustor.core.database.entity.AppWithCategory
import com.rustor.core.database.entity.CategoryEntity
import com.rustor.data.catalog.dto.AppDto
import com.rustor.data.catalog.dto.CatalogDto
import com.rustor.data.catalog.dto.CategoryDto

internal fun catalog(
    vararg apps: AppDto,
    categories: List<CategoryDto> = listOf(
        CategoryDto(id = "tools", title = "Инструменты"),
    ),
) = CatalogDto(
    categories = categories,
    apps = apps.toList(),
)

internal fun appDto(
    packageName: String,
    name: String = "Example",
    developer: String = "Example Dev",
    categoryId: String = "tools",
    ageRating: String? = "0+",
    iconUrl: String? = "https://example.com/icon.png",
    summary: String = "Short summary",
    description: String = "Full description",
    screenshots: List<String> = listOf("https://example.com/s1.png"),
    apkUrl: String? = "https://example.com/app.apk",
) = AppDto(
    packageName = packageName,
    name = name,
    developer = developer,
    categoryId = categoryId,
    ageRating = ageRating,
    iconUrl = iconUrl,
    summary = summary,
    description = description,
    screenshots = screenshots,
    apkUrl = apkUrl,
)

internal fun storedAgeRating(raw: String?): Int =
    appDto(packageName = "com.example.app", ageRating = raw).toEntity(feedPosition = 0).ageRating

internal fun appEntity(id: String = "com.example.app", ageRating: Int = 0, feedPosition: Int = 0) = AppEntity(
    id = id,
    name = "Example",
    developer = "Example Dev",
    categoryId = "tools",
    ageRating = ageRating,
    iconUrl = "https://example.com/icon.png",
    summary = "Short summary",
    description = "Full description",
    screenshots = listOf("https://example.com/s1.png"),
    apkUrl = "https://example.com/app.apk",
    feedPosition = feedPosition,
)

internal fun appWithCategory(
    app: AppEntity = appEntity(),
    category: CategoryEntity = CategoryEntity(id = "tools", title = "Инструменты"),
) = AppWithCategory(app = app, category = category)
