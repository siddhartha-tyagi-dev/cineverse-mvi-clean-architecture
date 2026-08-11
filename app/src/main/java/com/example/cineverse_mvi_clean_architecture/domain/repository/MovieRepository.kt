package com.example.cineverse_mvi_clean_architecture.domain.repository

import com.example.cineverse_mvi_clean_architecture.domain.model.Movie

interface MovieRepository {
    suspend fun getPopularMovies(): Result<List<Movie>>
    suspend fun getTrendingMovies(): Result<List<Movie>>
    suspend fun getNowPlayingMovies(): Result<List<Movie>>
    suspend fun getTopRatedMovies(): Result<List<Movie>>
}
