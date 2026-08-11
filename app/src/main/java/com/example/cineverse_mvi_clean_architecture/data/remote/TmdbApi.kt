package com.example.cineverse_mvi_clean_architecture.data.remote

import retrofit2.Response
import retrofit2.http.GET

interface TmdbApi {
    @GET("movie/popular")
    suspend fun getPopularMovies(): Response<MovieResponseDto>

    @GET("trending/movie/day")
    suspend fun getTrendingMovies(): Response<MovieResponseDto>

    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(): Response<MovieResponseDto>

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(): Response<MovieResponseDto>
}
