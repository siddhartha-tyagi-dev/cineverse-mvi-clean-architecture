package com.example.cineverse_mvi_clean_architecture.domain.usecase

import com.example.cineverse_mvi_clean_architecture.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class IsMovieFavoriteUseCase @Inject constructor(
    private val repository: MovieRepository,
) {
    operator fun invoke(movieId: Int): Flow<Boolean> =
        if (movieId <= 0) {
            flowOf(false)
        } else {
            repository.isMovieFavorite(movieId)
        }
}
