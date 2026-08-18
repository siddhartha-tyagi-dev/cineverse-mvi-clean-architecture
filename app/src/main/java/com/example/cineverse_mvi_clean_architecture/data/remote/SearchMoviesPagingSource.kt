package com.example.cineverse_mvi_clean_architecture.data.remote

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.cineverse_mvi_clean_architecture.domain.model.Movie
import java.io.IOException

class SearchMoviesPagingSource(
    private val api: TmdbApi,
    private val query: String,
) : PagingSource<Int, Movie>() {
    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val anchorPage = state.closestPageToPosition(anchorPosition) ?: return null
        return anchorPage.prevKey?.plus(1) ?: anchorPage.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        val page = params.key ?: FIRST_PAGE
        val trimmedQuery = query.trim()

        if (trimmedQuery.isBlank()) {
            return LoadResult.Page(
                data = emptyList(),
                prevKey = null,
                nextKey = null,
            )
        }

        return runCatching {
            val response = api.searchMovies(query = trimmedQuery, page = page)
            if (!response.isSuccessful) {
                throw IOException("Unable to search movies right now.")
            }

            val body = response.body() ?: throw IOException("TMDB returned an empty response.")
            val movies = body.results.orEmpty().mapNotNull { it.toDomain() }
            val totalPages = body.totalPages ?: body.page ?: page
            val nextKey = when {
                movies.isEmpty() -> null
                page >= totalPages -> null
                else -> page + 1
            }

            LoadResult.Page(
                data = movies,
                prevKey = if (page == FIRST_PAGE) null else page - 1,
                nextKey = nextKey,
            )
        }.getOrElse { throwable ->
            LoadResult.Error(throwable)
        }
    }

    companion object {
        const val FIRST_PAGE = 1
        const val PAGE_SIZE = 20
    }
}
