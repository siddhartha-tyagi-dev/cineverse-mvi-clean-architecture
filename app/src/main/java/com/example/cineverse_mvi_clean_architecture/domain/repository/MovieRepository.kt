package com.example.cineverse_mvi_clean_architecture.domain.repository

import androidx.paging.PagingData
import com.example.cineverse_mvi_clean_architecture.domain.model.Movie
import com.example.cineverse_mvi_clean_architecture.domain.model.MovieDetails
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    suspend fun getPopularMovies(): Result<List<Movie>>
    suspend fun getTrendingMovies(): Result<List<Movie>>
    suspend fun getNowPlayingMovies(): Result<List<Movie>>
    suspend fun getTopRatedMovies(): Result<List<Movie>>
    fun searchMovies(query: String): Flow<PagingData<Movie>>
    suspend fun getMovieDetails(movieId: Int): Result<MovieDetails>
    fun getFavoriteMovies(): Flow<List<Movie>>
    fun isMovieFavorite(movieId: Int): Flow<Boolean>
    suspend fun addFavorite(movie: MovieDetails): Result<Unit>
    suspend fun removeFavorite(movieId: Int): Result<Unit>
}
