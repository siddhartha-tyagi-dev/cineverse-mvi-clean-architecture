package com.example.cineverse_mvi_clean_architecture.domain.usecase

import androidx.paging.PagingData
import com.example.cineverse_mvi_clean_architecture.domain.model.Movie
import com.example.cineverse_mvi_clean_architecture.domain.model.MovieDetails
import com.example.cineverse_mvi_clean_architecture.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetMovieDetailsUseCaseTest {
    @Test
    fun `returns details for valid movie id`() = runTest {
        val details = movieDetails(12)
        val useCase = GetMovieDetailsUseCase(FakeDetailsRepository(Result.success(details)))

        val result = useCase(12)

        assertEquals(details, result.getOrNull())
    }

    @Test
    fun `returns failure for invalid movie id`() = runTest {
        val useCase = GetMovieDetailsUseCase(FakeDetailsRepository(Result.success(movieDetails(12))))

        val result = useCase(0)

        assertTrue(result.isFailure)
    }
}

private class FakeDetailsRepository(
    private val detailsResult: Result<MovieDetails>,
) : MovieRepository {
    override suspend fun getPopularMovies(): Result<List<Movie>> = Result.success(emptyList())
    override suspend fun getTrendingMovies(): Result<List<Movie>> = Result.success(emptyList())
    override suspend fun getNowPlayingMovies(): Result<List<Movie>> = Result.success(emptyList())
    override suspend fun getTopRatedMovies(): Result<List<Movie>> = Result.success(emptyList())
    override fun searchMovies(query: String): Flow<PagingData<Movie>> = flowOf(PagingData.empty())
    override suspend fun getMovieDetails(movieId: Int): Result<MovieDetails> = detailsResult
    override fun getFavoriteMovies(): Flow<List<Movie>> = flowOf(emptyList())
    override fun isMovieFavorite(movieId: Int): Flow<Boolean> = flowOf(false)
    override suspend fun addFavorite(movie: MovieDetails): Result<Unit> = Result.success(Unit)
    override suspend fun removeFavorite(movieId: Int): Result<Unit> = Result.success(Unit)
}

private fun movieDetails(id: Int): MovieDetails =
    MovieDetails(
        id = id,
        title = "Movie $id",
        posterPath = null,
        backdropPath = null,
        overview = "Overview",
        rating = 8.0,
        releaseDate = "2020-01-01",
        runtimeMinutes = 120,
        genres = emptyList(),
        cast = emptyList(),
        similarMovies = emptyList(),
        recommendations = emptyList(),
    )
