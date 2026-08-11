package com.example.cineverse_mvi_clean_architecture.data.remote

import com.google.gson.annotations.SerializedName

data class MovieDto(
    val id: Int?,
    val title: String?,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("backdrop_path")
    val backdropPath: String?,
    val overview: String?,
    @SerializedName("vote_average")
    val voteAverage: Double?,
    @SerializedName("release_date")
    val releaseDate: String?,
)
