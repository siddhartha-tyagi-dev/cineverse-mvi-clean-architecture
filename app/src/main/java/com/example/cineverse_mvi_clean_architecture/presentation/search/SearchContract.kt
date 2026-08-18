package com.example.cineverse_mvi_clean_architecture.presentation.search

sealed interface SearchIntent {
    data class QueryChanged(val query: String) : SearchIntent
    data object Retry : SearchIntent
    data class MovieClicked(val movieId: Int) : SearchIntent
}

data class SearchUiState(
    val query: String = "",
)

sealed interface SearchEffect {
    data class MovieSelected(val movieId: Int) : SearchEffect
    data object RetryPaging : SearchEffect
}
