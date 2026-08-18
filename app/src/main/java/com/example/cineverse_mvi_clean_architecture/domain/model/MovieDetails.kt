package com.example.cineverse_mvi_clean_architecture.domain.model

data class MovieDetails(
    val id: Int,
    val title: String,
    val posterPath: String?,
    val backdropPath: String?,
    val overview: String,
    val rating: Double,
    val releaseDate: String?,
    val runtimeMinutes: Int?,
    val genres: List<String>,
    val cast: List<CastMember>,
    val similarMovies: List<Movie>,
    val recommendations: List<Movie>,
)
