package com.example.cineverse_mvi_clean_architecture.data.remote

import com.example.cineverse_mvi_clean_architecture.domain.model.CastMember
import com.example.cineverse_mvi_clean_architecture.domain.model.MovieDetails

fun MovieDetailsDto.toDomain(): MovieDetails? {
    val safeId = id ?: return null
    val safeTitle = title?.takeIf { it.isNotBlank() } ?: return null

    return MovieDetails(
        id = safeId,
        title = safeTitle,
        posterPath = posterPath,
        backdropPath = backdropPath,
        overview = overview.orEmpty(),
        rating = voteAverage ?: 0.0,
        releaseDate = releaseDate?.takeIf { it.isNotBlank() },
        runtimeMinutes = runtime?.takeIf { it > 0 },
        genres = genres.orEmpty().mapNotNull { it.name?.takeIf(String::isNotBlank) },
        cast = credits
            ?.cast
            .orEmpty()
            .mapNotNull { it.toDomain() }
            .take(MAX_CAST_MEMBERS),
        similarMovies = similar
            ?.results
            .orEmpty()
            .mapNotNull { it.toDomain() },
        recommendations = recommendations
            ?.results
            .orEmpty()
            .mapNotNull { it.toDomain() },
    )
}

private fun CastMemberDto.toDomain(): CastMember? {
    val safeId = id ?: return null
    val safeName = name?.takeIf { it.isNotBlank() } ?: return null

    return CastMember(
        id = safeId,
        name = safeName,
        character = character.orEmpty(),
        profilePath = profilePath,
    )
}

private const val MAX_CAST_MEMBERS = 12
