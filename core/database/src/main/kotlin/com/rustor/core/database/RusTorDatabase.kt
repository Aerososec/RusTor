package com.rustor.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rustor.core.database.converter.ScreenshotsConverter
import com.rustor.core.database.dao.CatalogDao
import com.rustor.core.database.entity.AppEntity
import com.rustor.core.database.entity.CategoryEntity

@Database(
    entities = [AppEntity::class, CategoryEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(ScreenshotsConverter::class)
abstract class RusTorDatabase : RoomDatabase() {
    abstract fun catalogDao(): CatalogDao
}
