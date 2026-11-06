package com.rustor.data.catalog.mappers

import com.rustor.core.database.entity.CategorySummaryRow
import com.rustor.core.model.CategoryId
import com.rustor.core.model.CategorySummary

internal fun CategorySummaryRow.toDomain(): CategorySummary = CategorySummary(
    id = CategoryId(id),
    title = title,
    appCount = appCount,
)
