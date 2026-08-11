package com.example.cineverse_mvi_clean_architecture.domain.usecase

import com.example.cineverse_mvi_clean_architecture.domain.model.Movie
import com.example.cineverse_mvi_clean_architecture.domain.repository.MovieRepository
import javax.inject.Inject

class GetPopularMoviesUseCase @Inject constructor(
    private val repository: MovieRepository,
) {
    suspend operator fun invoke(): Result<List<Movie>> = repository.getPopularMovies()
}
