package com.example.cineverse_mvi_clean_architecture.domain.usecase

import com.example.cineverse_mvi_clean_architecture.domain.model.MovieDetails
import com.example.cineverse_mvi_clean_architecture.domain.repository.MovieRepository
import javax.inject.Inject

class FavoriteMovieUseCase @Inject constructor(
    private val repository: MovieRepository,
) {
    suspend operator fun invoke(movie: MovieDetails): Result<Unit> =
        repository.addFavorite(movie)
}
