package com.example.cineverse_mvi_clean_architecture.presentation.home

import com.example.cineverse_mvi_clean_architecture.domain.model.MovieCategory

sealed interface HomeIntent {
    data object LoadMovies : HomeIntent
    data object Refresh : HomeIntent
    data object Retry : HomeIntent
    data class MovieClicked(val movieId: Int) : HomeIntent
}

data class HomeUiState(
    val status: HomeStatus = HomeStatus.Loading,
    val sections: List<MovieCategory> = emptyList(),
    val errorMessage: String? = null,
    val isRefreshing: Boolean = false,
)

enum class HomeStatus {
    Loading,
    Success,
    Empty,
    Error,
}

sealed interface HomeEffect {
    data class ShowMessage(val message: String) : HomeEffect
    data class MovieSelected(val movieId: Int) : HomeEffect
}
