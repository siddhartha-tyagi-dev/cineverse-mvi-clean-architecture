package com.example.cineverse_mvi_clean_architecture.data.remote

import com.google.gson.annotations.SerializedName

data class MovieResponseDto(
    val page: Int?,
    val results: List<MovieDto>?,
    @SerializedName("total_pages")
    val totalPages: Int? = null,
    @SerializedName("total_results")
    val totalResults: Int? = null,
)
