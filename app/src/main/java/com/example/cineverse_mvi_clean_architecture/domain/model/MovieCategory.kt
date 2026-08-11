package com.example.cineverse_mvi_clean_architecture.domain.model

data class MovieCategory(
    val title: String,
    val movies: List<Movie>,
)
