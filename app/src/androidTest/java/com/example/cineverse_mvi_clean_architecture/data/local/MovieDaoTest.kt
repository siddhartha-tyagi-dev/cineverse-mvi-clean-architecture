package com.example.cineverse_mvi_clean_architecture.data.local

import android.content.Context
import androidx.room3.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MovieDaoTest {
    private lateinit var database: CineVerseDatabase
    private lateinit var dao: MovieDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context = context,
            klass = CineVerseDatabase::class.java,
        ).build()
        dao = database.movieDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun favorite_movies_are_observed_in_saved_order() = runTest {
        dao.upsertMovie(movieEntity(id = 1, savedAtMillis = 10L))
        dao.upsertMovie(movieEntity(id = 2, savedAtMillis = 20L))

        val favorites = dao.observeFavoriteMovies().first()

        assertEquals(listOf(2, 1), favorites.map { it.id })
    }

    @Test
    fun favorite_status_updates_after_remove() = runTest {
        dao.upsertMovie(movieEntity(id = 1))

        assertTrue(dao.observeIsFavorite(1).first())

        dao.clearFavorite(1)
        dao.deleteUnsavedMovie(1)

        assertFalse(dao.observeIsFavorite(1).first())
    }

    private fun movieEntity(
        id: Int,
        savedAtMillis: Long = 10L,
    ): MovieEntity =
        MovieEntity(
            id = id,
            title = "Movie $id",
            posterPath = null,
            backdropPath = null,
            overview = "Overview",
            rating = 7.0,
            releaseDate = "2020-01-01",
            isFavorite = true,
            isWatchlisted = false,
            savedAtMillis = savedAtMillis,
        )
}
