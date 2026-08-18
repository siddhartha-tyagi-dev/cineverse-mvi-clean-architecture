package com.example.cineverse_mvi_clean_architecture.presentation.details

import com.example.cineverse_mvi_clean_architecture.domain.model.MovieDetails

sealed interface MovieDetailsIntent {
    data class LoadDetails(val movieId: Int) : MovieDetailsIntent
    data object Retry : MovieDetailsIntent
    data object BackClicked : MovieDetailsIntent
    data object FavoriteClicked : MovieDetailsIntent
    data class MovieClicked(val movieId: Int) : MovieDetailsIntent
}

data class MovieDetailsUiState(
    val status: MovieDetailsStatus = MovieDetailsStatus.Loading,
    val movieDetails: MovieDetails? = null,
    val isFavorite: Boolean = false,
    val isUpdatingFavorite: Boolean = false,
    val errorMessage: String? = null,
)

enum class MovieDetailsStatus {
    Loading,
    Success,
    Error,
}

sealed interface MovieDetailsEffect {
    data object NavigateBack : MovieDetailsEffect
    data class ShowMessage(val message: String) : MovieDetailsEffect
    data class MovieSelected(val movieId: Int) : MovieDetailsEffect
}
