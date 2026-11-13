package com.rustor.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "popular_apps",
    foreignKeys = [
        ForeignKey(
            entity = AppEntity::class,
            parentColumns = ["id"],
            childColumns = ["app_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class PopularAppEntity(
    @PrimaryKey
    @ColumnInfo(name = "app_id")
    val appId: String,
    val position: Int,
)
