package com.example.cineverse_mvi_clean_architecture.data.local

import com.example.cineverse_mvi_clean_architecture.domain.model.MovieDetails
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MovieEntityMapperTest {
    @Test
    fun `maps entity to domain movie`() {
        val movie = movieEntity().toDomain()

        assertEquals(7, movie.id)
        assertEquals("Seven", movie.title)
        assertEquals("/poster.jpg", movie.posterPath)
        assertEquals(8.6, movie.rating, 0.0)
    }

    @Test
    fun `maps details to favorite entity preserving existing watchlist flag`() {
        val entity = movieDetails().toFavoriteEntity(
            savedAtMillis = 100L,
            existing = movieEntity(isWatchlisted = true),
        )

        assertEquals(7, entity.id)
        assertTrue(entity.isFavorite)
        assertTrue(entity.isWatchlisted)
        assertEquals(100L, entity.savedAtMillis)
    }

    @Test
    fun `maps details to favorite entity with watchlist disabled by default`() {
        val entity = movieDetails().toFavoriteEntity(savedAtMillis = 100L)

        assertTrue(entity.isFavorite)
        assertFalse(entity.isWatchlisted)
    }

    private fun movieEntity(isWatchlisted: Boolean = false): MovieEntity =
        MovieEntity(
            id = 7,
            title = "Seven",
            posterPath = "/poster.jpg",
            backdropPath = "/backdrop.jpg",
            overview = "A thriller.",
            rating = 8.6,
            releaseDate = "1995-09-22",
            isFavorite = true,
            isWatchlisted = isWatchlisted,
            savedAtMillis = 10L,
        )

    private fun movieDetails(): MovieDetails =
        MovieDetails(
            id = 7,
            title = "Seven",
            posterPath = "/poster.jpg",
            backdropPath = "/backdrop.jpg",
            overview = "A thriller.",
            rating = 8.6,
            releaseDate = "1995-09-22",
            runtimeMinutes = 127,
            genres = emptyList(),
            cast = emptyList(),
            similarMovies = emptyList(),
            recommendations = emptyList(),
        )
}
