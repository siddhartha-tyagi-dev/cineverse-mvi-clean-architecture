package com.example.cineverse_mvi_clean_architecture.data.remote

import androidx.paging.PagingSource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class SearchMoviesPagingSourceTest {
    @Test
    fun `loads first page with next key`() = runTest {
        val source = SearchMoviesPagingSource(
            api = FakeTmdbApi(
                responses = mapOf(
                    1 to MovieResponseDto(
                        page = 1,
                        results = listOf(movieDto(1)),
                        totalPages = 3,
                        totalResults = 3,
                    ),
                ),
            ),
            query = "batman",
        )

        val result = source.load(refreshParams())

        val page = result as PagingSource.LoadResult.Page
        assertNull(page.prevKey)
        assertEquals(2, page.nextKey)
        assertEquals(1, page.data.single().id)
    }

    @Test
    fun `loads middle page with previous and next keys`() = runTest {
        val source = SearchMoviesPagingSource(
            api = FakeTmdbApi(
                responses = mapOf(
                    2 to MovieResponseDto(
                        page = 2,
                        results = listOf(movieDto(2)),
                        totalPages = 3,
                        totalResults = 3,
                    ),
                ),
            ),
            query = "batman",
        )

        val result = source.load(appendParams(page = 2))

        val page = result as PagingSource.LoadResult.Page
        assertEquals(1, page.prevKey)
        assertEquals(3, page.nextKey)
        assertEquals(2, page.data.single().id)
    }

    @Test
    fun `loads last page without next key`() = runTest {
        val source = SearchMoviesPagingSource(
            api = FakeTmdbApi(
                responses = mapOf(
                    3 to MovieResponseDto(
                        page = 3,
                        results = listOf(movieDto(3)),
                        totalPages = 3,
                        totalResults = 3,
                    ),
                ),
            ),
            query = "batman",
        )

        val result = source.load(appendParams(page = 3))

        val page = result as PagingSource.LoadResult.Page
        assertEquals(2, page.prevKey)
        assertNull(page.nextKey)
    }

    @Test
    fun `blank query returns empty page without calling api`() = runTest {
        val api = FakeTmdbApi()
        val source = SearchMoviesPagingSource(api = api, query = " ")

        val result = source.load(refreshParams())

        val page = result as PagingSource.LoadResult.Page
        assertTrue(page.data.isEmpty())
        assertNull(page.prevKey)
        assertNull(page.nextKey)
        assertEquals(0, api.searchCalls)
    }

    @Test
    fun `api failure returns load error`() = runTest {
        val source = SearchMoviesPagingSource(
            api = FakeTmdbApi(error = IOException("No internet")),
            query = "batman",
        )

        val result = source.load(refreshParams())

        assertTrue(result is PagingSource.LoadResult.Error)
    }

    @Test
    fun `empty result stops pagination`() = runTest {
        val source = SearchMoviesPagingSource(
            api = FakeTmdbApi(
                responses = mapOf(
                    1 to MovieResponseDto(
                        page = 1,
                        results = emptyList(),
                        totalPages = 5,
                        totalResults = 0,
                    ),
                ),
            ),
            query = "unlikely title",
        )

        val result = source.load(refreshParams())

        val page = result as PagingSource.LoadResult.Page
        assertTrue(page.data.isEmpty())
        assertNull(page.nextKey)
    }

    private fun refreshParams(): PagingSource.LoadParams.Refresh<Int> =
        PagingSource.LoadParams.Refresh(
            key = null,
            loadSize = SearchMoviesPagingSource.PAGE_SIZE,
            placeholdersEnabled = false,
        )

    private fun appendParams(page: Int): PagingSource.LoadParams.Append<Int> =
        PagingSource.LoadParams.Append(
            key = page,
            loadSize = SearchMoviesPagingSource.PAGE_SIZE,
            placeholdersEnabled = false,
        )

    private fun movieDto(id: Int): MovieDto =
        MovieDto(
            id = id,
            title = "Movie $id",
            posterPath = "/poster$id.jpg",
            backdropPath = null,
            overview = "Overview",
            voteAverage = 7.0,
            releaseDate = "2020-01-01",
        )
}

private class FakeTmdbApi(
    private val responses: Map<Int, MovieResponseDto> = emptyMap(),
    private val error: Throwable? = null,
) : TmdbApi {
    var searchCalls = 0
        private set

    override suspend fun getPopularMovies(): Response<MovieResponseDto> =
        Response.success(MovieResponseDto(page = 1, results = emptyList(), totalPages = 1, totalResults = 0))

    override suspend fun getTrendingMovies(): Response<MovieResponseDto> =
        Response.success(MovieResponseDto(page = 1, results = emptyList(), totalPages = 1, totalResults = 0))

    override suspend fun getNowPlayingMovies(): Response<MovieResponseDto> =
        Response.success(MovieResponseDto(page = 1, results = emptyList(), totalPages = 1, totalResults = 0))

    override suspend fun getTopRatedMovies(): Response<MovieResponseDto> =
        Response.success(MovieResponseDto(page = 1, results = emptyList(), totalPages = 1, totalResults = 0))

    override suspend fun searchMovies(query: String, page: Int): Response<MovieResponseDto> {
        searchCalls += 1
        error?.let { throw it }
        return Response.success(
            responses[page] ?: MovieResponseDto(
                page = page,
                results = emptyList(),
                totalPages = page,
                totalResults = 0,
            ),
        )
    }

    override suspend fun getMovieDetails(
        movieId: Int,
        appendToResponse: String,
    ): Response<MovieDetailsDto> = Response.success(
        MovieDetailsDto(
            id = movieId,
            title = "Movie $movieId",
            posterPath = null,
            backdropPath = null,
            overview = null,
            voteAverage = null,
            releaseDate = null,
            runtime = null,
            genres = null,
            credits = null,
            similar = null,
            recommendations = null,
        ),
    )
}
