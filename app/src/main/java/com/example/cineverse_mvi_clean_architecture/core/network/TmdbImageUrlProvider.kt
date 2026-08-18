package com.example.cineverse_mvi_clean_architecture.core.network

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TmdbImageUrlProvider @Inject constructor() {
    fun posterUrl(path: String?): String? = path
        ?.takeIf { it.isNotBlank() }
        ?.let { "$IMAGE_BASE_URL$POSTER_SIZE$it" }

    fun backdropUrl(path: String?): String? = path
        ?.takeIf { it.isNotBlank() }
        ?.let { "$IMAGE_BASE_URL$BACKDROP_SIZE$it" }

    fun profileUrl(path: String?): String? = path
        ?.takeIf { it.isNotBlank() }
        ?.let { "$IMAGE_BASE_URL$PROFILE_SIZE$it" }

    companion object {
        private const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"
        private const val POSTER_SIZE = "w342"
        private const val BACKDROP_SIZE = "w780"
        private const val PROFILE_SIZE = "w185"
    }
}
