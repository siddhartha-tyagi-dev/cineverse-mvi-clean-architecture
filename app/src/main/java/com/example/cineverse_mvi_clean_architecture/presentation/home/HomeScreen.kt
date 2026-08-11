package com.example.cineverse_mvi_clean_architecture.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.cineverse_mvi_clean_architecture.core.network.TmdbImageUrlProvider
import com.example.cineverse_mvi_clean_architecture.domain.model.Movie
import com.example.cineverse_mvi_clean_architecture.domain.model.MovieCategory

@Composable
fun HomeRoute(
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            if (effect is HomeEffect.ShowMessage) {
                snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    HomeScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    snackbarHostState: SnackbarHostState,
    onIntent: (HomeIntent) -> Unit,
    imageUrlProvider: TmdbImageUrlProvider = remember { TmdbImageUrlProvider() },
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF191A1F),
                            MaterialTheme.colorScheme.background,
                        ),
                    ),
                ),
        ) {
            when (uiState.status) {
                HomeStatus.Loading -> LoadingState()
                HomeStatus.Success -> HomeContent(
                    sections = uiState.sections,
                    isRefreshing = uiState.isRefreshing,
                    imageUrlProvider = imageUrlProvider,
                    onIntent = onIntent,
                )
                HomeStatus.Empty -> EmptyState(onRetry = { onIntent(HomeIntent.Retry) })
                HomeStatus.Error -> ErrorState(
                    message = uiState.errorMessage.orEmpty(),
                    onRetry = { onIntent(HomeIntent.Retry) },
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    sections: List<MovieCategory>,
    isRefreshing: Boolean,
    imageUrlProvider: TmdbImageUrlProvider,
    onIntent: (HomeIntent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item {
            HomeHeader(
                isRefreshing = isRefreshing,
                onRefresh = { onIntent(HomeIntent.Refresh) },
            )
        }

        items(sections, key = { it.title }) { section ->
            MovieSection(
                section = section,
                imageUrlProvider = imageUrlProvider,
                onMovieClick = { onIntent(HomeIntent.MovieClicked(it)) },
            )
        }
    }
}

@Composable
private fun HomeHeader(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "CineVerse",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Button(onClick = onRefresh, enabled = !isRefreshing) {
                Text(text = if (isRefreshing) "Refreshing" else "Refresh")
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(
                text = "Search movies",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        if (isRefreshing) {
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun MovieSection(
    section: MovieCategory,
    imageUrlProvider: TmdbImageUrlProvider,
    onMovieClick: (Int) -> Unit,
) {
    Column {
        Text(
            text = section.title,
            modifier = Modifier.padding(horizontal = 20.dp),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(section.movies, key = { it.id }) { movie ->
                MovieCard(
                    movie = movie,
                    imageUrl = imageUrlProvider.posterUrl(movie.posterPath),
                    onClick = { onMovieClick(movie.id) },
                )
            }
        }
    }
}

@Composable
private fun MovieCard(
    movie: Movie,
    imageUrl: String?,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            if (imageUrl == null) {
                Text(
                    text = "No poster",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = movie.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }

        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = movie.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${movie.rating.oneDecimal()} • ${movie.releaseYear()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading movies",
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun EmptyState(onRetry: () -> Unit) {
    MessageState(
        title = "No movies found",
        message = "TMDB returned no movies for today's discovery rows.",
        actionText = "Try again",
        onAction = onRetry,
    )
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
) {
    MessageState(
        title = "Could not load CineVerse",
        message = message.ifBlank { "Check your TMDB token or network connection." },
        actionText = "Retry",
        onAction = onRetry,
    )
}

@Composable
private fun MessageState(
    title: String,
    message: String,
    actionText: String,
    onAction: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
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
            Spacer(modifier = Modifier.height(18.dp))
            Button(onClick = onAction) {
                Text(text = actionText)
            }
        }
    }
}

private fun Movie.releaseYear(): String = releaseDate
    ?.takeIf { it.length >= 4 }
    ?.take(4)
    ?: "TBA"

private fun Double.oneDecimal(): String = String.format("%.1f", this)
