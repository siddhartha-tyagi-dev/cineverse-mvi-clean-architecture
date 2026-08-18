package com.example.cineverse_mvi_clean_architecture.data.repository

import com.example.cineverse_mvi_clean_architecture.data.remote.CreditsDto
import com.example.cineverse_mvi_clean_architecture.data.remote.GenreDto
import com.example.cineverse_mvi_clean_architecture.data.remote.MovieDetailsDto
import com.example.cineverse_mvi_clean_architecture.data.remote.MovieResponseDto
import com.example.cineverse_mvi_clean_architecture.data.remote.TmdbApi
import com.example.cineverse_mvi_clean_architecture.data.local.MovieEntity
import com.example.cineverse_mvi_clean_architecture.data.local.MovieLocalDataSource
import com.example.cineverse_mvi_clean_architecture.data.local.MovieDao
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class MovieRepositoryImplTest {
    @Test
    fun `get movie details returns mapped details`() = runTest {
        val repository = MovieRepositoryImpl(
            api = FakeDetailsApi(
                detailsResponse = Response.success(
                    MovieDetailsDto(
                        id = 1,
                        title = "Heat",
                        posterPath = null,
                        backdropPath = null,
                        overview = "Crime in LA.",
                        voteAverage = 8.0,
                        releaseDate = "1995-12-15",
                        runtime = 170,
                        genres = listOf(GenreDto(id = 80, name = "Crime")),
                        credits = CreditsDto(cast = emptyList()),
                        similar = MovieResponseDto(page = 1, results = emptyList()),
                        recommendations = MovieResponseDto(page = 1, results = emptyList()),
                    ),
                ),
            ),
            localDataSource = MovieLocalDataSource(FakeMovieDao()),
        )

        val result = repository.getMovieDetails(1)

        assertTrue(result.isSuccess)
        assertEquals("Heat", result.getOrNull()?.title)
        assertEquals(listOf("Crime"), result.getOrNull()?.genres)
    }

    @Test
    fun `get movie details returns failure for http error`() = runTest {
        val repository = MovieRepositoryImpl(
            api = FakeDetailsApi(
                detailsResponse = Response.error(404, "Not found".toResponseBody()),
            ),
            localDataSource = MovieLocalDataSource(FakeMovieDao()),
        )

        val result = repository.getMovieDetails(1)

        assertTrue(result.isFailure)
    }

    @Test
    fun `favorite movie is stored and observed`() = runTest {
        val dao = StatefulMovieDao()
        val repository = MovieRepositoryImpl(
            api = FakeDetailsApi(detailsResponse = Response.success(minimalDetails(1))),
            localDataSource = MovieLocalDataSource(dao),
        )

        val result = repository.addFavorite(minimalDomainDetails(1))

        assertTrue(result.isSuccess)
        assertTrue(repository.isMovieFavorite(1).first())
        assertEquals(listOf("Movie 1"), repository.getFavoriteMovies().first().map { it.title })
    }

    @Test
    fun `remove favorite clears stored movie`() = runTest {
        val dao = StatefulMovieDao()
        val repository = MovieRepositoryImpl(
            api = FakeDetailsApi(detailsResponse = Response.success(minimalDetails(1))),
            localDataSource = MovieLocalDataSource(dao),
        )

        repository.addFavorite(minimalDomainDetails(1))
        repository.removeFavorite(1)

        assertTrue(repository.getFavoriteMovies().first().isEmpty())
        assertTrue(!repository.isMovieFavorite(1).first())
    }
}

private class FakeMovieDao : MovieDao {
    override fun observeFavoriteMovies(): Flow<List<MovieEntity>> = flowOf(emptyList())
    override fun observeIsFavorite(movieId: Int): Flow<Boolean> = flowOf(false)
    override suspend fun getMovie(movieId: Int): MovieEntity? = null
    override suspend fun upsertMovie(movie: MovieEntity) = Unit
    override suspend fun clearFavorite(movieId: Int) = Unit
    override suspend fun deleteUnsavedMovie(movieId: Int) = Unit
}

private class StatefulMovieDao : MovieDao {
    private val movies = MutableStateFlow<List<MovieEntity>>(emptyList())

    override fun observeFavoriteMovies(): Flow<List<MovieEntity>> = movies

    override fun observeIsFavorite(movieId: Int): Flow<Boolean> =
        kotlinx.coroutines.flow.flow {
            emit(movies.value.any { it.id == movieId && it.isFavorite })
        }

    override suspend fun getMovie(movieId: Int): MovieEntity? =
        movies.value.firstOrNull { it.id == movieId }

    override suspend fun upsertMovie(movie: MovieEntity) {
        movies.value = movies.value.filterNot { it.id == movie.id } + movie
    }

    override suspend fun clearFavorite(movieId: Int) {
        movies.value = movies.value.map {
            if (it.id == movieId) it.copy(isFavorite = false) else it
        }
    }

    override suspend fun deleteUnsavedMovie(movieId: Int) {
        movies.value = movies.value.filterNot {
            it.id == movieId && !it.isFavorite && !it.isWatchlisted
        }
    }
}

private fun minimalDetails(id: Int): MovieDetailsDto =
    MovieDetailsDto(
        id = id,
        title = "Movie $id",
        posterPath = null,
        backdropPath = null,
        overview = "Overview",
        voteAverage = 7.0,
        releaseDate = "2020-01-01",
        runtime = 100,
        genres = emptyList(),
        credits = CreditsDto(cast = emptyList()),
        similar = MovieResponseDto(page = 1, results = emptyList()),
        recommendations = MovieResponseDto(page = 1, results = emptyList()),
    )

private fun minimalDomainDetails(id: Int): com.example.cineverse_mvi_clean_architecture.domain.model.MovieDetails =
    com.example.cineverse_mvi_clean_architecture.domain.model.MovieDetails(
        id = id,
        title = "Movie $id",
        posterPath = null,
        backdropPath = null,
        overview = "Overview",
        rating = 7.0,
        releaseDate = "2020-01-01",
        runtimeMinutes = 100,
        genres = emptyList(),
        cast = emptyList(),
        similarMovies = emptyList(),
        recommendations = emptyList(),
    )

private class FakeDetailsApi(
    private val detailsResponse: Response<MovieDetailsDto>,
) : TmdbApi {
    override suspend fun getPopularMovies(): Response<MovieResponseDto> =
        Response.success(MovieResponseDto(page = 1, results = emptyList()))

    override suspend fun getTrendingMovies(): Response<MovieResponseDto> =
        Response.success(MovieResponseDto(page = 1, results = emptyList()))

    override suspend fun getNowPlayingMovies(): Response<MovieResponseDto> =
        Response.success(MovieResponseDto(page = 1, results = emptyList()))

    override suspend fun getTopRatedMovies(): Response<MovieResponseDto> =
        Response.success(MovieResponseDto(page = 1, results = emptyList()))

    override suspend fun searchMovies(
        query: String,
        page: Int,
    ): Response<MovieResponseDto> =
        Response.success(MovieResponseDto(page = page, results = emptyList()))

    override suspend fun getMovieDetails(
        movieId: Int,
        appendToResponse: String,
    ): Response<MovieDetailsDto> = detailsResponse
}
