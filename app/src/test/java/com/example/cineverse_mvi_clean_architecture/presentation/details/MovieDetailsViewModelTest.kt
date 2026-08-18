package com.example.cineverse_mvi_clean_architecture.presentation.details

import androidx.paging.PagingData
import com.example.cineverse_mvi_clean_architecture.domain.model.Movie
import com.example.cineverse_mvi_clean_architecture.domain.model.MovieDetails
import com.example.cineverse_mvi_clean_architecture.domain.repository.MovieRepository
import com.example.cineverse_mvi_clean_architecture.domain.usecase.FavoriteMovieUseCase
import com.example.cineverse_mvi_clean_architecture.domain.usecase.GetMovieDetailsUseCase
import com.example.cineverse_mvi_clean_architecture.domain.usecase.IsMovieFavoriteUseCase
import com.example.cineverse_mvi_clean_architecture.domain.usecase.RemoveFavoriteUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
class MovieDetailsViewModelTest {
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
    fun `loads success state`() = runTest {
        val details = movieDetails(9)
        val viewModel = MovieDetailsViewModel(
            GetMovieDetailsUseCase(FakeDetailsRepository(Result.success(details))),
            FavoriteMovieUseCase(FakeDetailsRepository(Result.success(details))),
            RemoveFavoriteUseCase(FakeDetailsRepository(Result.success(details))),
            IsMovieFavoriteUseCase(FakeDetailsRepository(Result.success(details))),
        )

        viewModel.onIntent(MovieDetailsIntent.LoadDetails(9))
        advanceUntilIdle()

        assertEquals(MovieDetailsStatus.Success, viewModel.uiState.value.status)
        assertEquals(details, viewModel.uiState.value.movieDetails)
    }

    @Test
    fun `loads error state`() = runTest {
        val viewModel = MovieDetailsViewModel(
            GetMovieDetailsUseCase(FakeDetailsRepository(Result.failure(IOException("No internet")))),
            FavoriteMovieUseCase(FakeDetailsRepository(Result.failure(IOException("No internet")))),
            RemoveFavoriteUseCase(FakeDetailsRepository(Result.failure(IOException("No internet")))),
            IsMovieFavoriteUseCase(FakeDetailsRepository(Result.failure(IOException("No internet")))),
        )

        viewModel.onIntent(MovieDetailsIntent.LoadDetails(9))
        advanceUntilIdle()

        assertEquals(MovieDetailsStatus.Error, viewModel.uiState.value.status)
        assertEquals("No internet", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `retry reloads current movie id`() = runTest {
        val repository = FakeDetailsRepository(Result.success(movieDetails(9)))
        val viewModel = MovieDetailsViewModel(
            GetMovieDetailsUseCase(repository),
            FavoriteMovieUseCase(repository),
            RemoveFavoriteUseCase(repository),
            IsMovieFavoriteUseCase(repository),
        )

        viewModel.onIntent(MovieDetailsIntent.LoadDetails(9))
        advanceUntilIdle()
        viewModel.onIntent(MovieDetailsIntent.Retry)
        advanceUntilIdle()

        assertEquals(listOf(9, 9), repository.requestedIds)
    }

    @Test
    fun `back emits navigation effect`() = runTest {
        val viewModel = MovieDetailsViewModel(
            GetMovieDetailsUseCase(FakeDetailsRepository(Result.success(movieDetails(9)))),
            FavoriteMovieUseCase(FakeDetailsRepository(Result.success(movieDetails(9)))),
            RemoveFavoriteUseCase(FakeDetailsRepository(Result.success(movieDetails(9)))),
            IsMovieFavoriteUseCase(FakeDetailsRepository(Result.success(movieDetails(9)))),
        )
        val effects = mutableListOf<MovieDetailsEffect>()
        val job = launch { viewModel.effect.collect { effects += it } }

        viewModel.onIntent(MovieDetailsIntent.BackClicked)
        advanceUntilIdle()

        assertEquals(MovieDetailsEffect.NavigateBack, effects.single())
        job.cancel()
    }

    @Test
    fun `movie click emits selected movie id`() = runTest {
        val viewModel = MovieDetailsViewModel(
            GetMovieDetailsUseCase(FakeDetailsRepository(Result.success(movieDetails(9)))),
            FavoriteMovieUseCase(FakeDetailsRepository(Result.success(movieDetails(9)))),
            RemoveFavoriteUseCase(FakeDetailsRepository(Result.success(movieDetails(9)))),
            IsMovieFavoriteUseCase(FakeDetailsRepository(Result.success(movieDetails(9)))),
        )
        val effects = mutableListOf<MovieDetailsEffect>()
        val job = launch { viewModel.effect.collect { effects += it } }

        viewModel.onIntent(MovieDetailsIntent.MovieClicked(42))
        advanceUntilIdle()

        assertEquals(MovieDetailsEffect.MovieSelected(42), effects.single())
        job.cancel()
    }
}

private class FakeDetailsRepository(
    private val detailsResult: Result<MovieDetails>,
) : MovieRepository {
    val requestedIds = mutableListOf<Int>()

    override suspend fun getPopularMovies(): Result<List<Movie>> = Result.success(emptyList())
    override suspend fun getTrendingMovies(): Result<List<Movie>> = Result.success(emptyList())
    override suspend fun getNowPlayingMovies(): Result<List<Movie>> = Result.success(emptyList())
    override suspend fun getTopRatedMovies(): Result<List<Movie>> = Result.success(emptyList())
    override fun searchMovies(query: String): Flow<PagingData<Movie>> = flowOf(PagingData.empty())
    override fun getFavoriteMovies(): Flow<List<Movie>> = flowOf(emptyList())
    override fun isMovieFavorite(movieId: Int): Flow<Boolean> = flowOf(false)

    override suspend fun getMovieDetails(movieId: Int): Result<MovieDetails> {
        requestedIds += movieId
        return detailsResult
    }

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
