package com.example.cineverse_mvi_clean_architecture.data.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Query("SELECT * FROM saved_movies WHERE isFavorite = 1 ORDER BY savedAtMillis DESC")
    fun observeFavoriteMovies(): Flow<List<MovieEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_movies WHERE id = :movieId AND isFavorite = 1)")
    fun observeIsFavorite(movieId: Int): Flow<Boolean>

    @Query("SELECT * FROM saved_movies WHERE id = :movieId LIMIT 1")
    suspend fun getMovie(movieId: Int): MovieEntity?

    @Upsert
    suspend fun upsertMovie(movie: MovieEntity)

    @Query("UPDATE saved_movies SET isFavorite = 0 WHERE id = :movieId")
    suspend fun clearFavorite(movieId: Int)

    @Query("DELETE FROM saved_movies WHERE id = :movieId AND isFavorite = 0 AND isWatchlisted = 0")
    suspend fun deleteUnsavedMovie(movieId: Int)
}
