package com.example.cineverse_mvi_clean_architecture.domain.usecase

import androidx.paging.PagingData
import com.example.cineverse_mvi_clean_architecture.domain.model.Movie
import com.example.cineverse_mvi_clean_architecture.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class SearchMoviesUseCase @Inject constructor(
    private val repository: MovieRepository,
) {
    operator fun invoke(query: String): Flow<PagingData<Movie>> {
        val normalizedQuery = query.trim()
        return if (normalizedQuery.isBlank()) {
            flowOf(PagingData.empty())
        } else {
            repository.searchMovies(normalizedQuery)
        }
    }
}
