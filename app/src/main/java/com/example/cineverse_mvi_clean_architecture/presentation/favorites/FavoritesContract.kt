package com.example.cineverse_mvi_clean_architecture.presentation.favorites

import com.example.cineverse_mvi_clean_architecture.domain.model.Movie

sealed interface FavoritesIntent {
    data object LoadFavorites : FavoritesIntent
    data object BackClicked : FavoritesIntent
    data class MovieClicked(val movieId: Int) : FavoritesIntent
    data class RemoveFavorite(val movieId: Int) : FavoritesIntent
}

data class FavoritesUiState(
    val status: FavoritesStatus = FavoritesStatus.Loading,
    val movies: List<Movie> = emptyList(),
    val errorMessage: String? = null,
)

enum class FavoritesStatus {
    Loading,
    Success,
    Empty,
    Error,
}

sealed interface FavoritesEffect {
    data object NavigateBack : FavoritesEffect
    data class MovieSelected(val movieId: Int) : FavoritesEffect
    data class ShowMessage(val message: String) : FavoritesEffect
}
