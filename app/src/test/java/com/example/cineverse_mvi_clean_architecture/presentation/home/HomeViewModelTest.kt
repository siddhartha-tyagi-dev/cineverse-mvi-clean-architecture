package com.example.cineverse_mvi_clean_architecture.presentation.home

import com.example.cineverse_mvi_clean_architecture.domain.model.Movie
import com.example.cineverse_mvi_clean_architecture.domain.repository.MovieRepository
import com.example.cineverse_mvi_clean_architecture.domain.usecase.GetHomeMoviesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
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
    fun `loads success state when repository returns movies`() = runTest {
        val movie = Movie(
            id = 1,
            title = "Interstellar",
            posterPath = "/poster.jpg",
            backdropPath = "/backdrop.jpg",
            overview = "Space and time.",
            rating = 8.4,
            releaseDate = "2014-11-07",
        )
        val viewModel = HomeViewModel(GetHomeMoviesUseCase(FakeMovieRepository(Result.success(listOf(movie)))))

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(HomeStatus.Success, state.status)
        assertEquals(4, state.sections.size)
        assertTrue(state.sections.all { it.movies == listOf(movie) })
    }

    @Test
    fun `loads empty state when all sections are empty`() = runTest {
        val viewModel = HomeViewModel(GetHomeMoviesUseCase(FakeMovieRepository(Result.success(emptyList()))))

        advanceUntilIdle()

        assertEquals(HomeStatus.Empty, viewModel.uiState.value.status)
    }

    @Test
    fun `loads error state when repository fails`() = runTest {
        val viewModel = HomeViewModel(
            GetHomeMoviesUseCase(
                FakeMovieRepository(Result.failure(IOException("No internet"))),
            ),
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(HomeStatus.Error, state.status)
        assertEquals("No internet", state.errorMessage)
    }
}

private class FakeMovieRepository(
    private val result: Result<List<Movie>>,
) : MovieRepository {
    override suspend fun getPopularMovies(): Result<List<Movie>> = result
    override suspend fun getTrendingMovies(): Result<List<Movie>> = result
    override suspend fun getNowPlayingMovies(): Result<List<Movie>> = result
    override suspend fun getTopRatedMovies(): Result<List<Movie>> = result
}
