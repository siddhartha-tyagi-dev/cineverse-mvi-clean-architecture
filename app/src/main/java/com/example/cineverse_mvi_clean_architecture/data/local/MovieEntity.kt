package com.example.cineverse_mvi_clean_architecture.data.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "saved_movies")
data class MovieEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val posterPath: String?,
    val backdropPath: String?,
    val overview: String,
    val rating: Double,
    val releaseDate: String?,
    val isFavorite: Boolean,
    val isWatchlisted: Boolean,
    val savedAtMillis: Long,
)
