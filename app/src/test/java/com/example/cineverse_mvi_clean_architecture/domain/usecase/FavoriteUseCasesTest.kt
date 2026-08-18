package com.example.cineverse_mvi_clean_architecture.domain.usecase

import androidx.paging.PagingData
import com.example.cineverse_mvi_clean_architecture.domain.model.Movie
import com.example.cineverse_mvi_clean_architecture.domain.model.MovieDetails
import com.example.cineverse_mvi_clean_architecture.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FavoriteUseCasesTest {
    @Test
    fun `favorite movie use case delegates to repository`() = runTest {
        val repository = FakeFavoritesRepository()

        FavoriteMovieUseCase(repository)(movieDetails(4))

        assertEquals(4, repository.favoriteAdded?.id)
    }

    @Test
    fun `remove favorite use case delegates valid id`() = runTest {
        val repository = FakeFavoritesRepository()

        val result = RemoveFavoriteUseCase(repository)(4)

        assertTrue(result.isSuccess)
        assertEquals(4, repository.removedId)
    }

    @Test
    fun `remove favorite use case rejects invalid id`() = runTest {
        val result = RemoveFavoriteUseCase(FakeFavoritesRepository())(0)

        assertTrue(result.isFailure)
    }

    @Test
    fun `get favorite movies use case returns flow`() = runTest {
        val repository = FakeFavoritesRepository(
            favorites = listOf(movie(4)),
        )

        val favorites = GetFavoriteMoviesUseCase(repository)().first()

        assertEquals(listOf(movie(4)), favorites)
    }

    @Test
    fun `is movie favorite use case returns false for invalid id`() = runTest {
        val isFavorite = IsMovieFavoriteUseCase(FakeFavoritesRepository())(0).first()

        assertFalse(isFavorite)
    }
}

private class FakeFavoritesRepository(
    favorites: List<Movie> = emptyList(),
) : MovieRepository {
    private val favoritesFlow = MutableStateFlow(favorites)
    var favoriteAdded: MovieDetails? = null
    var removedId: Int? = null

    override suspend fun getPopularMovies(): Result<List<Movie>> = Result.success(emptyList())
    override suspend fun getTrendingMovies(): Result<List<Movie>> = Result.success(emptyList())
    override suspend fun getNowPlayingMovies(): Result<List<Movie>> = Result.success(emptyList())
    override suspend fun getTopRatedMovies(): Result<List<Movie>> = Result.success(emptyList())
    override fun searchMovies(query: String): Flow<PagingData<Movie>> = flowOf(PagingData.empty())
    override suspend fun getMovieDetails(movieId: Int): Result<MovieDetails> = Result.success(movieDetails(movieId))
    override fun getFavoriteMovies(): Flow<List<Movie>> = favoritesFlow
    override fun isMovieFavorite(movieId: Int): Flow<Boolean> = flowOf(favoritesFlow.value.any { it.id == movieId })

    override suspend fun addFavorite(movie: MovieDetails): Result<Unit> {
        favoriteAdded = movie
        return Result.success(Unit)
    }

    override suspend fun removeFavorite(movieId: Int): Result<Unit> {
        removedId = movieId
        return Result.success(Unit)
    }
}

private fun movie(id: Int): Movie =
    Movie(
        id = id,
        title = "Movie $id",
        posterPath = null,
        backdropPath = null,
        overview = "Overview",
        rating = 7.0,
        releaseDate = "2020-01-01",
    )

private fun movieDetails(id: Int): MovieDetails =
    MovieDetails(
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
