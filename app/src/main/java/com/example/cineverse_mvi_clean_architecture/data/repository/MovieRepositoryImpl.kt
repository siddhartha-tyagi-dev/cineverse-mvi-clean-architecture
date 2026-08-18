package com.example.cineverse_mvi_clean_architecture.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.cineverse_mvi_clean_architecture.data.local.MovieLocalDataSource
import com.example.cineverse_mvi_clean_architecture.data.local.toDomain
import com.example.cineverse_mvi_clean_architecture.data.local.toFavoriteEntity
import com.example.cineverse_mvi_clean_architecture.data.remote.MovieResponseDto
import com.example.cineverse_mvi_clean_architecture.data.remote.SearchMoviesPagingSource
import com.example.cineverse_mvi_clean_architecture.data.remote.TmdbApi
import com.example.cineverse_mvi_clean_architecture.data.remote.toDomain as detailsToDomain
import com.example.cineverse_mvi_clean_architecture.data.remote.toDomain as remoteToDomain
import com.example.cineverse_mvi_clean_architecture.domain.model.Movie
import com.example.cineverse_mvi_clean_architecture.domain.model.MovieDetails
import com.example.cineverse_mvi_clean_architecture.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

class MovieRepositoryImpl @Inject constructor(
    private val api: TmdbApi,
    private val localDataSource: MovieLocalDataSource,
) : MovieRepository {
    override suspend fun getPopularMovies(): Result<List<Movie>> =
        requestMovies { api.getPopularMovies() }

    override suspend fun getTrendingMovies(): Result<List<Movie>> =
        requestMovies { api.getTrendingMovies() }

    override suspend fun getNowPlayingMovies(): Result<List<Movie>> =
        requestMovies { api.getNowPlayingMovies() }

    override suspend fun getTopRatedMovies(): Result<List<Movie>> =
        requestMovies { api.getTopRatedMovies() }

    override fun searchMovies(query: String): Flow<PagingData<Movie>> =
        Pager(
            config = PagingConfig(
                pageSize = SearchMoviesPagingSource.PAGE_SIZE,
                initialLoadSize = SearchMoviesPagingSource.PAGE_SIZE,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                SearchMoviesPagingSource(
                    api = api,
                    query = query,
                )
            },
        ).flow

    override suspend fun getMovieDetails(movieId: Int): Result<MovieDetails> = runCatching {
        val response = api.getMovieDetails(movieId = movieId)
        if (!response.isSuccessful) {
            throw IOException("TMDB request failed with HTTP ${response.code()}")
        }

        response.body()
            ?.detailsToDomain()
            ?: throw IOException("TMDB returned an empty response")
    }

    override fun getFavoriteMovies(): Flow<List<Movie>> =
        localDataSource.observeFavoriteMovies()
            .map { movies -> movies.map { it.toDomain() } }

    override fun isMovieFavorite(movieId: Int): Flow<Boolean> =
        localDataSource.observeIsFavorite(movieId)

    override suspend fun addFavorite(movie: MovieDetails): Result<Unit> = runCatching {
        val existing = localDataSource.getMovie(movie.id)
        localDataSource.addFavorite(
            movie.toFavoriteEntity(
                savedAtMillis = System.currentTimeMillis(),
                existing = existing,
            ),
        )
    }

    override suspend fun removeFavorite(movieId: Int): Result<Unit> = runCatching {
        localDataSource.removeFavorite(movieId)
    }

    private suspend fun requestMovies(
        block: suspend () -> Response<MovieResponseDto>,
    ): Result<List<Movie>> = runCatching {
        val response = block()
        if (!response.isSuccessful) {
            throw IOException("TMDB request failed with HTTP ${response.code()}")
        }

        response.body()
            ?.results
            ?.mapNotNull { it.remoteToDomain() }
            ?: throw IOException("TMDB returned an empty response")
    }
}
