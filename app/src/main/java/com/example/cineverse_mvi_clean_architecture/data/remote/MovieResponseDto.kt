package com.example.cineverse_mvi_clean_architecture.data.remote

data class MovieResponseDto(
    val page: Int?,
    val results: List<MovieDto>?,
)
