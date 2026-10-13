package com.rustor.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "apps",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
        ),
    ],
    indices = [Index("category_id")],
)
data class AppEntity(
    @PrimaryKey val id: String,
    val name: String,
    val developer: String,
    @ColumnInfo(name = "category_id") val categoryId: String,
    @ColumnInfo(name = "age_rating") val ageRating: Int,
    @ColumnInfo(name = "icon_url") val iconUrl: String?,
    val summary: String,
    val description: String,
    val screenshots: List<String>,
    @ColumnInfo(name = "apk_url") val apkUrl: String?,
    @ColumnInfo(name = "feed_position") val feedPosition: Int,
)
