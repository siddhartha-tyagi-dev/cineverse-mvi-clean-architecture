package com.example.cineverse_mvi_clean_architecture.data.local

import com.example.cineverse_mvi_clean_architecture.domain.model.Movie
import com.example.cineverse_mvi_clean_architecture.domain.model.MovieDetails

fun MovieEntity.toDomain(): Movie =
    Movie(
        id = id,
        title = title,
        posterPath = posterPath,
        backdropPath = backdropPath,
        overview = overview,
        rating = rating,
        releaseDate = releaseDate,
    )

fun Movie.toFavoriteEntity(
    savedAtMillis: Long,
    existing: MovieEntity? = null,
): MovieEntity =
    MovieEntity(
        id = id,
        title = title,
        posterPath = posterPath,
        backdropPath = backdropPath,
        overview = overview,
        rating = rating,
        releaseDate = releaseDate,
        isFavorite = true,
        isWatchlisted = existing?.isWatchlisted ?: false,
        savedAtMillis = savedAtMillis,
    )

fun MovieDetails.toFavoriteEntity(
    savedAtMillis: Long,
    existing: MovieEntity? = null,
): MovieEntity =
    MovieEntity(
        id = id,
        title = title,
        posterPath = posterPath,
        backdropPath = backdropPath,
        overview = overview,
        rating = rating,
        releaseDate = releaseDate,
        isFavorite = true,
        isWatchlisted = existing?.isWatchlisted ?: false,
        savedAtMillis = savedAtMillis,
    )
