package com.rustor.data.catalog.mappers

import com.rustor.core.database.entity.AppEntity
import com.rustor.core.database.entity.AppWithCategory
import com.rustor.core.model.AgeRating
import com.rustor.core.model.App
import com.rustor.core.model.AppId
import com.rustor.data.catalog.dto.AppDto

internal fun AppDto.toEntity(feedPosition: Int) = AppEntity(
    id = packageName,
    name = name,
    developer = developer,
    categoryId = categoryId,
    ageRating = ageRating.toAgeRating().minAge,
    iconUrl = iconUrl?.takeIf { it.isNotBlank() },
    summary = summary,
    description = description,
    screenshots = screenshots.filter { it.isNotBlank() },
    apkUrl = apkUrl?.takeIf { it.isNotBlank() },
    feedPosition = feedPosition,
)

internal fun AppWithCategory.toDomain(): App = App(
    id = AppId(app.id),
    name = app.name,
    developer = app.developer,
    category = category.toDomain(),
    ageRating = AgeRating.fromMinAge(app.ageRating),
    iconUrl = app.iconUrl,
    summary = app.summary,
    description = app.description,
    screenshots = app.screenshots,
    apkUrl = app.apkUrl,
)

internal fun String?.toAgeRating(): AgeRating = when (this?.trim()) {
    "0+" -> AgeRating.ZERO_PLUS
    "6+" -> AgeRating.SIX_PLUS
    "8+" -> AgeRating.EIGHT_PLUS
    "12+" -> AgeRating.TWELVE_PLUS
    "16+" -> AgeRating.SIXTEEN_PLUS
    else -> AgeRating.EIGHTEEN_PLUS
}
