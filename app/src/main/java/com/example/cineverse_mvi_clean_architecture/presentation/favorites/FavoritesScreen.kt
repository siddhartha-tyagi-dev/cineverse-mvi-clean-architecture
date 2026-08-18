package com.example.cineverse_mvi_clean_architecture.presentation.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cineverse_mvi_clean_architecture.core.network.TmdbImageUrlProvider
import com.example.cineverse_mvi_clean_architecture.domain.model.Movie
import com.example.cineverse_mvi_clean_architecture.presentation.components.MoviePosterCard

@Composable
fun FavoritesRoute(
    onBack: () -> Unit,
    onMovieSelected: (Int) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                FavoritesEffect.NavigateBack -> onBack()
                is FavoritesEffect.MovieSelected -> onMovieSelected(effect.movieId)
                is FavoritesEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    FavoritesScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
    )
}

@Composable
fun FavoritesScreen(
    uiState: FavoritesUiState,
    snackbarHostState: SnackbarHostState,
    onIntent: (FavoritesIntent) -> Unit,
    imageUrlProvider: TmdbImageUrlProvider = remember { TmdbImageUrlProvider() },
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.background,
                        ),
                    ),
                )
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            FavoritesHeader(onBack = { onIntent(FavoritesIntent.BackClicked) })

            when (uiState.status) {
                FavoritesStatus.Loading -> FavoritesLoadingState()
                FavoritesStatus.Success -> FavoritesContent(
                    movies = uiState.movies,
                    imageUrlProvider = imageUrlProvider,
                    onMovieClick = { onIntent(FavoritesIntent.MovieClicked(it)) },
                    onRemoveFavorite = { onIntent(FavoritesIntent.RemoveFavorite(it)) },
                )
                FavoritesStatus.Empty -> FavoritesMessageState(
                    title = "No favorites yet",
                    message = "Add movies from the details screen to keep them here offline.",
                )
                FavoritesStatus.Error -> FavoritesMessageState(
                    title = "Could not load favorites",
                    message = uiState.errorMessage.orEmpty().ifBlank {
                        "Try opening favorites again."
                    },
                    actionText = "Retry",
                    onAction = { onIntent(FavoritesIntent.LoadFavorites) },
                )
            }
        }
    }
}

@Composable
private fun FavoritesHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onBack) {
            Text(text = "Back")
        }
        Text(
            text = "Favorites",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.width(64.dp))
    }
}

@Composable
private fun FavoritesContent(
    movies: List<Movie>,
    imageUrlProvider: TmdbImageUrlProvider,
    onMovieClick: (Int) -> Unit,
    onRemoveFavorite: (Int) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        items(movies, key = { it.id }) { movie ->
            Column {
                MoviePosterCard(
                    movie = movie,
                    imageUrl = imageUrlProvider.posterUrl(movie.posterPath),
                    onClick = { onMovieClick(movie.id) },
                    modifier = Modifier.width(170.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { onRemoveFavorite(movie.id) }) {
                    Text(text = "Remove")
                }
            }
        }
    }
}

@Composable
private fun FavoritesLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading favorites",
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun FavoritesMessageState(
    title: String,
    message: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (actionText != null && onAction != null) {
                Spacer(modifier = Modifier.height(18.dp))
                Button(onClick = onAction) {
                    Text(text = actionText)
                }
            }
        }
    }
}
