package com.example.cineverse_mvi_clean_architecture.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MovieMapperTest {
    @Test
    fun `maps valid dto to domain movie`() {
        val dto = MovieDto(
            id = 11,
            title = "Arrival",
            posterPath = "/arrival.jpg",
            backdropPath = "/arrival-backdrop.jpg",
            overview = "A linguist works with mysterious visitors.",
            voteAverage = 7.6,
            releaseDate = "2016-11-11",
        )

        val movie = dto.toDomain()

        assertEquals(11, movie?.id)
        assertEquals("Arrival", movie?.title)
        assertEquals("/arrival.jpg", movie?.posterPath)
        assertEquals(7.6, movie?.rating ?: 0.0, 0.0)
        assertEquals("2016-11-11", movie?.releaseDate)
    }

    @Test
    fun `drops dto with missing id`() {
        val dto = MovieDto(
            id = null,
            title = "Missing Id",
            posterPath = null,
            backdropPath = null,
            overview = null,
            voteAverage = null,
            releaseDate = null,
        )

        assertNull(dto.toDomain())
    }

    @Test
    fun `drops dto with blank title`() {
        val dto = MovieDto(
            id = 7,
            title = " ",
            posterPath = null,
            backdropPath = null,
            overview = null,
            voteAverage = null,
            releaseDate = null,
        )

        assertNull(dto.toDomain())
    }
}
