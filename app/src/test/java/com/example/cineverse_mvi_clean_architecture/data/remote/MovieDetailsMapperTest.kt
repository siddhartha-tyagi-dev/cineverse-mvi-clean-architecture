package com.example.cineverse_mvi_clean_architecture.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MovieDetailsMapperTest {
    @Test
    fun `maps details dto to domain`() {
        val dto = movieDetailsDto()

        val details = dto.toDomain()

        assertEquals(11, details?.id)
        assertEquals("Arrival", details?.title)
        assertEquals(116, details?.runtimeMinutes)
        assertEquals(listOf("Science Fiction", "Drama"), details?.genres)
        assertEquals("Amy Adams", details?.cast?.single()?.name)
        assertEquals(21, details?.similarMovies?.single()?.id)
        assertEquals(31, details?.recommendations?.single()?.id)
    }

    @Test
    fun `drops details dto with missing id`() {
        assertNull(movieDetailsDto(id = null).toDomain())
    }

    @Test
    fun `drops details dto with blank title`() {
        assertNull(movieDetailsDto(title = " ").toDomain())
    }

    private fun movieDetailsDto(
        id: Int? = 11,
        title: String? = "Arrival",
    ): MovieDetailsDto =
        MovieDetailsDto(
            id = id,
            title = title,
            posterPath = "/arrival.jpg",
            backdropPath = "/arrival-backdrop.jpg",
            overview = "A linguist works with mysterious visitors.",
            voteAverage = 7.6,
            releaseDate = "2016-11-11",
            runtime = 116,
            genres = listOf(
                GenreDto(id = 878, name = "Science Fiction"),
                GenreDto(id = 18, name = "Drama"),
            ),
            credits = CreditsDto(
                cast = listOf(
                    CastMemberDto(
                        id = 1,
                        name = "Amy Adams",
                        character = "Louise Banks",
                        profilePath = "/amy.jpg",
                    ),
                ),
            ),
            similar = MovieResponseDto(
                page = 1,
                results = listOf(movieDto(21, "Contact")),
            ),
            recommendations = MovieResponseDto(
                page = 1,
                results = listOf(movieDto(31, "Interstellar")),
            ),
        )

    private fun movieDto(id: Int, title: String): MovieDto =
        MovieDto(
            id = id,
            title = title,
            posterPath = null,
            backdropPath = null,
            overview = null,
            voteAverage = null,
            releaseDate = null,
        )
}
