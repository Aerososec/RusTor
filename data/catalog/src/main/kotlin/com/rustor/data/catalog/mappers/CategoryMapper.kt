package com.rustor.data.catalog.mappers

import com.rustor.core.database.entity.CategoryEntity
import com.rustor.core.model.Category
import com.rustor.core.model.CategoryId
import com.rustor.data.catalog.dto.CategoryDto

internal fun CategoryEntity.toDomain(): Category = Category(
    id = CategoryId(id),
    title = title,
)

internal fun CategoryDto.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    title = title,
)
