package com.example.cineverse_mvi_clean_architecture.presentation.favorites

import androidx.paging.PagingData
import com.example.cineverse_mvi_clean_architecture.domain.model.Movie
import com.example.cineverse_mvi_clean_architecture.domain.model.MovieDetails
import com.example.cineverse_mvi_clean_architecture.domain.repository.MovieRepository
import com.example.cineverse_mvi_clean_architecture.domain.usecase.GetFavoriteMoviesUseCase
import com.example.cineverse_mvi_clean_architecture.domain.usecase.RemoveFavoriteUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads success state when favorites exist`() = runTest {
        val movie = movie(3)
        val viewModel = viewModel(FakeFavoritesRepository(favorites = listOf(movie)))

        advanceUntilIdle()

        assertEquals(FavoritesStatus.Success, viewModel.uiState.value.status)
        assertEquals(listOf(movie), viewModel.uiState.value.movies)
    }

    @Test
    fun `loads empty state when no favorites exist`() = runTest {
        val viewModel = viewModel(FakeFavoritesRepository())

        advanceUntilIdle()

        assertEquals(FavoritesStatus.Empty, viewModel.uiState.value.status)
    }

    @Test
    fun `loads error state when favorites flow fails`() = runTest {
        val viewModel = viewModel(FakeFavoritesRepository(error = IOException("Database unavailable")))

        advanceUntilIdle()

        assertEquals(FavoritesStatus.Error, viewModel.uiState.value.status)
        assertEquals("Database unavailable", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `movie click emits selected effect`() = runTest {
        val viewModel = viewModel(FakeFavoritesRepository())
        val effects = mutableListOf<FavoritesEffect>()
        val job = launch { viewModel.effect.collect { effects += it } }

        viewModel.onIntent(FavoritesIntent.MovieClicked(8))
        advanceUntilIdle()

        assertEquals(FavoritesEffect.MovieSelected(8), effects.single())
        job.cancel()
    }

    @Test
    fun `remove favorite delegates and emits message`() = runTest {
        val repository = FakeFavoritesRepository()
        val viewModel = viewModel(repository)
        val effects = mutableListOf<FavoritesEffect>()
        val job = launch { viewModel.effect.collect { effects += it } }

        viewModel.onIntent(FavoritesIntent.RemoveFavorite(8))
        advanceUntilIdle()

        assertEquals(8, repository.removedId)
        assertEquals(FavoritesEffect.ShowMessage("Removed from favorites"), effects.single())
        job.cancel()
    }

    private fun viewModel(repository: FakeFavoritesRepository): FavoritesViewModel =
        FavoritesViewModel(
            getFavoriteMoviesUseCase = GetFavoriteMoviesUseCase(repository),
            removeFavoriteUseCase = RemoveFavoriteUseCase(repository),
        )
}

private class FakeFavoritesRepository(
    favorites: List<Movie> = emptyList(),
    private val error: Throwable? = null,
) : MovieRepository {
    private val favoritesFlow = MutableStateFlow(favorites)
    var removedId: Int? = null

    override suspend fun getPopularMovies(): Result<List<Movie>> = Result.success(emptyList())
    override suspend fun getTrendingMovies(): Result<List<Movie>> = Result.success(emptyList())
    override suspend fun getNowPlayingMovies(): Result<List<Movie>> = Result.success(emptyList())
    override suspend fun getTopRatedMovies(): Result<List<Movie>> = Result.success(emptyList())
    override fun searchMovies(query: String): Flow<PagingData<Movie>> = flowOf(PagingData.empty())
    override suspend fun getMovieDetails(movieId: Int): Result<MovieDetails> = Result.failure(UnsupportedOperationException())
    override fun isMovieFavorite(movieId: Int): Flow<Boolean> = flowOf(false)
    override suspend fun addFavorite(movie: MovieDetails): Result<Unit> = Result.success(Unit)

    override fun getFavoriteMovies(): Flow<List<Movie>> =
        if (error == null) {
            favoritesFlow
        } else {
            flow { throw error }
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
