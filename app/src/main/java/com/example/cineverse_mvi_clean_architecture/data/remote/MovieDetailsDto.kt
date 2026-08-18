package com.example.cineverse_mvi_clean_architecture.data.remote

import com.google.gson.annotations.SerializedName

data class MovieDetailsDto(
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
    val runtime: Int?,
    val genres: List<GenreDto>?,
    val credits: CreditsDto?,
    val similar: MovieResponseDto?,
    val recommendations: MovieResponseDto?,
)

data class GenreDto(
    val id: Int?,
    val name: String?,
)

data class CreditsDto(
    val cast: List<CastMemberDto>?,
)

data class CastMemberDto(
    val id: Int?,
    val name: String?,
    val character: String?,
    @SerializedName("profile_path")
    val profilePath: String?,
)
