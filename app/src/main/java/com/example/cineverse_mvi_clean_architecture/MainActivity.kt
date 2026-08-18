package com.example.cineverse_mvi_clean_architecture

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.cineverse_mvi_clean_architecture.presentation.about.AboutRoute
import com.example.cineverse_mvi_clean_architecture.presentation.details.MovieDetailsRoute
import com.example.cineverse_mvi_clean_architecture.presentation.favorites.FavoritesRoute
import com.example.cineverse_mvi_clean_architecture.presentation.home.HomeRoute
import com.example.cineverse_mvi_clean_architecture.presentation.search.SearchRoute
import com.example.cineverse_mvi_clean_architecture.presentation.theme.CineVerseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CineVerseTheme {
                CineVerseRoot()
            }
        }
    }
}

@Composable
private fun CineVerseRoot() {
    var screen: CineVerseScreen by remember { mutableStateOf(CineVerseScreen.Home) }

    when (screen) {
        CineVerseScreen.Home -> HomeRoute(
            onOpenSearch = { screen = CineVerseScreen.Search },
            onOpenFavorites = { screen = CineVerseScreen.Favorites },
            onOpenAbout = { screen = CineVerseScreen.About },
            onMovieSelected = { movieId ->
                screen = CineVerseScreen.Details(movieId, previous = CineVerseScreen.Home)
            },
        )

        CineVerseScreen.Search -> SearchRoute(
            onBack = { screen = CineVerseScreen.Home },
            onMovieSelected = { movieId ->
                screen = CineVerseScreen.Details(movieId, previous = CineVerseScreen.Search)
            },
        )

        CineVerseScreen.Favorites -> FavoritesRoute(
            onBack = { screen = CineVerseScreen.Home },
            onMovieSelected = { movieId ->
                screen = CineVerseScreen.Details(movieId, previous = CineVerseScreen.Favorites)
            },
        )

        CineVerseScreen.About -> AboutRoute(
            onBack = { screen = CineVerseScreen.Home },
        )

        is CineVerseScreen.Details -> MovieDetailsRoute(
            movieId = (screen as CineVerseScreen.Details).movieId,
            onBack = {
                screen = (screen as CineVerseScreen.Details).previous
            },
            onMovieSelected = { movieId ->
                screen = (screen as CineVerseScreen.Details).copy(movieId = movieId)
            },
        )
    }
}

private sealed interface CineVerseScreen {
    data object Home : CineVerseScreen
    data object Search : CineVerseScreen
    data object Favorites : CineVerseScreen
    data object About : CineVerseScreen
    data class Details(
        val movieId: Int,
        val previous: CineVerseScreen,
    ) : CineVerseScreen
}
