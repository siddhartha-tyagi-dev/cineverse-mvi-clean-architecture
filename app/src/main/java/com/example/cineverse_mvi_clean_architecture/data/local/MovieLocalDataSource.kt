package com.example.cineverse_mvi_clean_architecture.data.local

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MovieLocalDataSource @Inject constructor(
    private val movieDao: MovieDao,
) {
    fun observeFavoriteMovies(): Flow<List<MovieEntity>> =
        movieDao.observeFavoriteMovies()

    fun observeIsFavorite(movieId: Int): Flow<Boolean> =
        movieDao.observeIsFavorite(movieId)

    suspend fun addFavorite(movie: MovieEntity) {
        movieDao.upsertMovie(movie)
    }

    suspend fun removeFavorite(movieId: Int) {
        movieDao.clearFavorite(movieId)
        movieDao.deleteUnsavedMovie(movieId)
    }

    suspend fun getMovie(movieId: Int): MovieEntity? =
        movieDao.getMovie(movieId)
}
