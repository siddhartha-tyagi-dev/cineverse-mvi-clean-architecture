package com.example.cineverse_mvi_clean_architecture.data.remote

import com.example.cineverse_mvi_clean_architecture.domain.model.Movie

fun MovieDto.toDomain(): Movie? {
    val safeId = id ?: return null
    val safeTitle = title?.takeIf { it.isNotBlank() } ?: return null

    return Movie(
        id = safeId,
        title = safeTitle,
        posterPath = posterPath,
        backdropPath = backdropPath,
        overview = overview.orEmpty(),
        rating = voteAverage ?: 0.0,
        releaseDate = releaseDate?.takeIf { it.isNotBlank() },
    )
}
