package com.example.cineverse_mvi_clean_architecture.presentation.search

import androidx.paging.PagingData
import com.example.cineverse_mvi_clean_architecture.domain.model.Movie
import com.example.cineverse_mvi_clean_architecture.domain.model.MovieDetails
import com.example.cineverse_mvi_clean_architecture.domain.repository.MovieRepository
import com.example.cineverse_mvi_clean_architecture.domain.usecase.SearchMoviesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
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
    fun `query changed updates state`() = runTest {
        val viewModel = SearchViewModel(SearchMoviesUseCase(FakeSearchRepository()))

        viewModel.onIntent(SearchIntent.QueryChanged("Batman"))

        assertEquals("Batman", viewModel.uiState.value.query)
    }

    @Test
    fun `empty query does not call repository`() = runTest {
        val repository = FakeSearchRepository()
        val viewModel = SearchViewModel(SearchMoviesUseCase(repository))
        val job = launch { viewModel.movies.collect {} }

        advanceTimeBy(SearchViewModel.SEARCH_DEBOUNCE_MS + 1)
        advanceUntilIdle()

        assertEquals(0, repository.searchCalls)
        job.cancel()
    }

    @Test
    fun `debounced query calls repository once for latest text`() = runTest {
        val repository = FakeSearchRepository()
        val viewModel = SearchViewModel(SearchMoviesUseCase(repository))
        val job = launch { viewModel.movies.collect {} }

        viewModel.onIntent(SearchIntent.QueryChanged("B"))
        advanceTimeBy(100)
        viewModel.onIntent(SearchIntent.QueryChanged("Ba"))
        advanceTimeBy(100)
        viewModel.onIntent(SearchIntent.QueryChanged("Batman"))
        advanceTimeBy(SearchViewModel.SEARCH_DEBOUNCE_MS + 1)
        advanceUntilIdle()

        assertEquals(listOf("Batman"), repository.queries)
        job.cancel()
    }

    @Test
    fun `movie clicked emits selected movie id`() = runTest {
        val viewModel = SearchViewModel(SearchMoviesUseCase(FakeSearchRepository()))
        val effects = mutableListOf<SearchEffect>()
        val job = launch { viewModel.effect.collect { effects += it } }

        viewModel.onIntent(SearchIntent.MovieClicked(42))
        advanceUntilIdle()

        assertEquals(SearchEffect.MovieSelected(42), effects.single())
        job.cancel()
    }

    @Test
    fun `retry emits retry paging effect`() = runTest {
        val viewModel = SearchViewModel(SearchMoviesUseCase(FakeSearchRepository()))
        val effects = mutableListOf<SearchEffect>()
        val job = launch { viewModel.effect.collect { effects += it } }

        viewModel.onIntent(SearchIntent.Retry)
        advanceUntilIdle()

        assertEquals(SearchEffect.RetryPaging, effects.single())
        job.cancel()
    }
}

private class FakeSearchRepository : MovieRepository {
    val queries = mutableListOf<String>()
    val searchCalls: Int get() = queries.size

    override suspend fun getPopularMovies(): Result<List<Movie>> = Result.success(emptyList())
    override suspend fun getTrendingMovies(): Result<List<Movie>> = Result.success(emptyList())
    override suspend fun getNowPlayingMovies(): Result<List<Movie>> = Result.success(emptyList())
    override suspend fun getTopRatedMovies(): Result<List<Movie>> = Result.success(emptyList())
    override suspend fun getMovieDetails(movieId: Int): Result<MovieDetails> =
        Result.failure(UnsupportedOperationException())
    override fun getFavoriteMovies(): Flow<List<Movie>> = flowOf(emptyList())
    override fun isMovieFavorite(movieId: Int): Flow<Boolean> = flowOf(false)
    override suspend fun addFavorite(movie: MovieDetails): Result<Unit> =
        Result.failure(UnsupportedOperationException())
    override suspend fun removeFavorite(movieId: Int): Result<Unit> =
        Result.failure(UnsupportedOperationException())

    override fun searchMovies(query: String): Flow<PagingData<Movie>> {
        queries += query
        return flowOf(PagingData.empty())
    }
}
