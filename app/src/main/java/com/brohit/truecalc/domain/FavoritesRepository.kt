package com.brohit.truecalc.domain

import com.brohit.truecalc.data.data_source.local.room.dao.FavoriteToolDao
import com.brohit.truecalc.data.data_source.local.room.entity.FavoriteTool
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FavoritesRepository @Inject constructor(
    private val favoriteToolDao: FavoriteToolDao
) {
    fun getAllFavorites(): Flow<List<FavoriteTool>> = favoriteToolDao.getAllFavorites()

    fun isFavorite(route: String): Flow<Boolean> = favoriteToolDao.isFavorite(route)

    suspend fun addFavorite(favoriteTool: FavoriteTool) {
        favoriteToolDao.insertFavorite(favoriteTool)
    }

    suspend fun removeFavorite(route: String) {
        favoriteToolDao.deleteByRoute(route)
    }

    suspend fun clearAllFavorites() {
        favoriteToolDao.clearAllFavorites()
    }
}
