package com.rustor.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.rustor.core.database.entity.AppEntity
import com.rustor.core.database.entity.AppWithCategory
import com.rustor.core.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CatalogDao {
    @Transaction
    @Query("SELECT * FROM apps ORDER BY feed_position")
    abstract fun observeFeed(): Flow<List<AppWithCategory>>

    @Transaction
    @Query("SELECT * FROM apps WHERE category_id = :categoryId ORDER BY feed_position")
    abstract fun observeFeedByCategory(categoryId: String): Flow<List<AppWithCategory>>

    @Query("SELECT * FROM categories ORDER BY title")
    abstract fun observeCategories(): Flow<List<CategoryEntity>>

    @Transaction
    open suspend fun replaceCatalog(categories: List<CategoryEntity>, apps: List<AppEntity>) {
        deleteAllApps()
        deleteAllCategories()
        insertCategories(categories)
        insertApps(apps)
    }

    @Query("DELETE FROM apps")
    protected abstract suspend fun deleteAllApps()

    @Query("DELETE FROM categories")
    protected abstract suspend fun deleteAllCategories()

    @Insert
    protected abstract suspend fun insertCategories(categories: List<CategoryEntity>)

    @Insert
    protected abstract suspend fun insertApps(apps: List<AppEntity>)
}
