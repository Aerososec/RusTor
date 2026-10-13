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
    ageRating = ageRating.toAgeRating(),
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
    ageRating = AgeRating.Companion.fromMinAge(app.ageRating),
    iconUrl = app.iconUrl,
    summary = app.summary,
    description = app.description,
    screenshots = app.screenshots,
    apkUrl = app.apkUrl,
)

private fun String?.toAgeRating(): Int = when (this?.trim()) {
    "0+" -> AgeRating.ZERO_PLUS.value
    "6+" -> AgeRating.SIX_PLUS.value
    "8+" -> AgeRating.EIGHT_PLUS.value
    "12+" -> AgeRating.TWELVE_PLUS.value
    "16+" -> AgeRating.SIXTEEN_PLUS.value
    else -> AgeRating.EIGHTEEN_PLUS.value
}
