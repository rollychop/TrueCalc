package com.brohit.truecalc.data.data_source.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.brohit.truecalc.data.data_source.local.room.entity.FavoriteTool
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteToolDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favoriteTool: FavoriteTool)

    @Delete
    suspend fun deleteFavorite(favoriteTool: FavoriteTool)

    @Query("DELETE FROM favorite_tools_table WHERE route = :route")
    suspend fun deleteByRoute(route: String)

    @Query("SELECT * FROM favorite_tools_table ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteTool>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_tools_table WHERE route = :route)")
    fun isFavorite(route: String): Flow<Boolean>

    @Query("DELETE FROM favorite_tools_table")
    suspend fun clearAllFavorites()
}
