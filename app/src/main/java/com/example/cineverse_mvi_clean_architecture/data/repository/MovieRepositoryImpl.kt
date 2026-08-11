package com.example.cineverse_mvi_clean_architecture.data.repository

import com.example.cineverse_mvi_clean_architecture.data.remote.MovieResponseDto
import com.example.cineverse_mvi_clean_architecture.data.remote.TmdbApi
import com.example.cineverse_mvi_clean_architecture.data.remote.toDomain
import com.example.cineverse_mvi_clean_architecture.domain.model.Movie
import com.example.cineverse_mvi_clean_architecture.domain.repository.MovieRepository
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

class MovieRepositoryImpl @Inject constructor(
    private val api: TmdbApi,
) : MovieRepository {
    override suspend fun getPopularMovies(): Result<List<Movie>> =
        requestMovies { api.getPopularMovies() }

    override suspend fun getTrendingMovies(): Result<List<Movie>> =
        requestMovies { api.getTrendingMovies() }

    override suspend fun getNowPlayingMovies(): Result<List<Movie>> =
        requestMovies { api.getNowPlayingMovies() }

    override suspend fun getTopRatedMovies(): Result<List<Movie>> =
        requestMovies { api.getTopRatedMovies() }

    private suspend fun requestMovies(
        block: suspend () -> Response<MovieResponseDto>,
    ): Result<List<Movie>> = runCatching {
        val response = block()
        if (!response.isSuccessful) {
            throw IOException("TMDB request failed with HTTP ${response.code()}")
        }

        response.body()
            ?.results
            ?.mapNotNull { it.toDomain() }
            ?: throw IOException("TMDB returned an empty response")
    }
}
