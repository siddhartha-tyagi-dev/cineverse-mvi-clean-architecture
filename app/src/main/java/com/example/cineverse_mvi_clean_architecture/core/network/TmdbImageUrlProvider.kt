package com.example.cineverse_mvi_clean_architecture.core.network

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TmdbImageUrlProvider @Inject constructor() {
    fun posterUrl(path: String?): String? = path
        ?.takeIf { it.isNotBlank() }
        ?.let { "$IMAGE_BASE_URL$POSTER_SIZE$it" }

    companion object {
        private const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"
        private const val POSTER_SIZE = "w342"
    }
}
