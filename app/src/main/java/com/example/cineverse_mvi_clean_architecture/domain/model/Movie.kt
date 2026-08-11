package com.example.cineverse_mvi_clean_architecture.domain.model

data class Movie(
    val id: Int,
    val title: String,
    val posterPath: String?,
    val backdropPath: String?,
    val overview: String,
    val rating: Double,
    val releaseDate: String?,
)
