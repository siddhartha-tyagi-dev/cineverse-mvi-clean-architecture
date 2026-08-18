package com.example.cineverse_mvi_clean_architecture.presentation.about

sealed interface AboutIntent {
    data object BackClicked : AboutIntent
}

data class AboutUiState(
    val appName: String = "CineVerse",
    val versionName: String = "",
    val architecture: String = "MVI + Clean Architecture",
)

sealed interface AboutEffect {
    data object NavigateBack : AboutEffect
}
