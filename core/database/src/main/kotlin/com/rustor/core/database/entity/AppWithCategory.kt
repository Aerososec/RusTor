package com.rustor.core.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class AppWithCategory(
    @Embedded val app: AppEntity,
    @Relation(parentColumn = "category_id", entityColumn = "id")
    val category: CategoryEntity,
)
