package com.example.cineverse_mvi_clean_architecture.domain.usecase

import com.example.cineverse_mvi_clean_architecture.domain.model.MovieDetails
import com.example.cineverse_mvi_clean_architecture.domain.repository.MovieRepository
import javax.inject.Inject

class GetMovieDetailsUseCase @Inject constructor(
    private val repository: MovieRepository,
) {
    suspend operator fun invoke(movieId: Int): Result<MovieDetails> =
        if (movieId <= 0) {
            Result.failure(IllegalArgumentException("Invalid movie selected."))
        } else {
            repository.getMovieDetails(movieId)
        }
}
