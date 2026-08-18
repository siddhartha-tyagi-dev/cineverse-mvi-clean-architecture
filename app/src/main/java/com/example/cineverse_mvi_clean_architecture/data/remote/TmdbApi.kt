package com.example.cineverse_mvi_clean_architecture.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApi {
    @GET("movie/popular")
    suspend fun getPopularMovies(): Response<MovieResponseDto>

    @GET("trending/movie/day")
    suspend fun getTrendingMovies(): Response<MovieResponseDto>

    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(): Response<MovieResponseDto>

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(): Response<MovieResponseDto>

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int,
    ): Response<MovieResponseDto>

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("append_to_response") appendToResponse: String = "credits,similar,recommendations",
    ): Response<MovieDetailsDto>
}
