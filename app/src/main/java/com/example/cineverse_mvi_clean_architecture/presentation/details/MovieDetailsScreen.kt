package com.example.cineverse_mvi_clean_architecture.presentation.details

import androidx.compose.foundation.background
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.cineverse_mvi_clean_architecture.core.network.TmdbImageUrlProvider
import com.example.cineverse_mvi_clean_architecture.domain.model.CastMember
import com.example.cineverse_mvi_clean_architecture.domain.model.Movie
import com.example.cineverse_mvi_clean_architecture.domain.model.MovieDetails
import com.example.cineverse_mvi_clean_architecture.presentation.components.MoviePosterCard

@Composable
fun MovieDetailsRoute(
    movieId: Int,
    onBack: () -> Unit,
    onMovieSelected: (Int) -> Unit,
    viewModel: MovieDetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(movieId) {
        viewModel.onIntent(MovieDetailsIntent.LoadDetails(movieId))
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                MovieDetailsEffect.NavigateBack -> onBack()
                is MovieDetailsEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
                is MovieDetailsEffect.MovieSelected -> onMovieSelected(effect.movieId)
            }
        }
    }

    MovieDetailsScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
    )
}

@Composable
fun MovieDetailsScreen(
    uiState: MovieDetailsUiState,
    snackbarHostState: SnackbarHostState,
    onIntent: (MovieDetailsIntent) -> Unit,
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
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.background,
                        ),
                    ),
                ),
        ) {
            when (uiState.status) {
                MovieDetailsStatus.Loading -> DetailsLoadingState(
                    onBack = { onIntent(MovieDetailsIntent.BackClicked) },
                )
                MovieDetailsStatus.Success -> uiState.movieDetails?.let { details ->
                    DetailsContent(
                        details = details,
                        imageUrlProvider = imageUrlProvider,
                        onBack = { onIntent(MovieDetailsIntent.BackClicked) },
                        isFavorite = uiState.isFavorite,
                        isUpdatingFavorite = uiState.isUpdatingFavorite,
                        onFavoriteClick = { onIntent(MovieDetailsIntent.FavoriteClicked) },
                        onMovieClick = { onIntent(MovieDetailsIntent.MovieClicked(it)) },
                    )
                }
                MovieDetailsStatus.Error -> DetailsMessageState(
                    title = "Could not load details",
                    message = uiState.errorMessage.orEmpty().ifBlank {
                        "Check your connection and try again."
                    },
                    actionText = "Retry",
                    onAction = { onIntent(MovieDetailsIntent.Retry) },
                    onBack = { onIntent(MovieDetailsIntent.BackClicked) },
                )
            }
        }
    }
}

@Composable
private fun DetailsContent(
    details: MovieDetails,
    imageUrlProvider: TmdbImageUrlProvider,
    onBack: () -> Unit,
    isFavorite: Boolean,
    isUpdatingFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onMovieClick: (Int) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item {
            DetailsHero(
                details = details,
                backdropUrl = imageUrlProvider.backdropUrl(details.backdropPath),
                posterUrl = imageUrlProvider.posterUrl(details.posterPath),
                onBack = onBack,
                isFavorite = isFavorite,
                isUpdatingFavorite = isUpdatingFavorite,
                onFavoriteClick = onFavoriteClick,
            )
        }

        item {
            DetailsOverview(details = details)
        }

        if (details.cast.isNotEmpty()) {
            item {
                CastSection(
                    cast = details.cast,
                    imageUrlProvider = imageUrlProvider,
                )
            }
        }

        if (details.similarMovies.isNotEmpty()) {
            item {
                MovieRowSection(
                    title = "Similar Movies",
                    movies = details.similarMovies,
                    imageUrlProvider = imageUrlProvider,
                    onMovieClick = onMovieClick,
                )
            }
        }

        if (details.recommendations.isNotEmpty()) {
            item {
                MovieRowSection(
                    title = "Recommendations",
                    movies = details.recommendations,
                    imageUrlProvider = imageUrlProvider,
                    onMovieClick = onMovieClick,
                )
            }
        }
    }
}

@Composable
private fun DetailsHero(
    details: MovieDetails,
    backdropUrl: String?,
    posterUrl: String?,
    onBack: () -> Unit,
    isFavorite: Boolean,
    isUpdatingFavorite: Boolean,
    onFavoriteClick: () -> Unit,
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
        ) {
            if (backdropUrl == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
            } else {
                AsyncImage(
                    model = backdropUrl,
                    contentDescription = details.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.18f),
                                MaterialTheme.colorScheme.background,
                            ),
                            startY = 120f,
                        ),
                    ),
            )
            TextButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
            ) {
                Text(text = "Back")
            }
            Button(
                onClick = onFavoriteClick,
                enabled = !isUpdatingFavorite,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
            ) {
                Text(
                    text = when {
                        isUpdatingFavorite -> "Saving"
                        isFavorite -> "Remove Favorite"
                        else -> "Add Favorite"
                    },
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PosterImage(
                title = details.title,
                posterUrl = posterUrl,
                modifier = Modifier
                    .width(124.dp)
                    .aspectRatio(2f / 3f),
            )

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = details.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = listOf(
                        details.rating.oneDecimal(),
                        details.releaseYear(),
                        details.runtimeLabel(),
                    ).joinToString(" | "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (details.genres.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = details.genres.joinToString(", "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun PosterImage(
    title: String,
    posterUrl: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (posterUrl == null) {
            Text(
                text = "No poster",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            AsyncImage(
                model = posterUrl,
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
private fun DetailsOverview(details: MovieDetails) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
    ) {
        SectionTitle("Overview")
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = details.overview.ifBlank { "No overview available." },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun CastSection(
    cast: List<CastMember>,
    imageUrlProvider: TmdbImageUrlProvider,
) {
    Column {
        SectionTitle(
            text = "Cast",
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(cast, key = { it.id }) { member ->
                CastCard(
                    member = member,
                    imageUrl = imageUrlProvider.profileUrl(member.profilePath),
                )
            }
        }
    }
}

@Composable
private fun CastCard(
    member: CastMember,
    imageUrl: String?,
) {
    Column(
        modifier = Modifier.width(104.dp),
    ) {
        Box(
            modifier = Modifier
                .size(width = 104.dp, height = 140.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            if (imageUrl == null) {
                Text(
                    text = "No photo",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            } else {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = member.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = member.name,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = member.character.ifBlank { "Cast" },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MovieRowSection(
    title: String,
    movies: List<Movie>,
    imageUrlProvider: TmdbImageUrlProvider,
    onMovieClick: (Int) -> Unit,
) {
    Column {
        SectionTitle(
            text = title,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(movies, key = { it.id }) { movie ->
                MoviePosterCard(
                    movie = movie,
                    imageUrl = imageUrlProvider.posterUrl(movie.posterPath),
                    onClick = { onMovieClick(movie.id) },
                    modifier = Modifier.width(150.dp),
                )
            }
        }
    }
}

@Composable
private fun DetailsLoadingState(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        TextButton(
            onClick = onBack,
            modifier = Modifier.padding(12.dp),
        ) {
            Text(text = "Back")
        }
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading details",
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun DetailsMessageState(
    title: String,
    message: String,
    actionText: String,
    onAction: () -> Unit,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
    ) {
        TextButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart),
        ) {
            Text(text = "Back")
        }
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
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

@Composable
private fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

private fun MovieDetails.releaseYear(): String = releaseDate
    ?.takeIf { it.length >= 4 }
    ?.take(4)
    ?: "TBA"

private fun MovieDetails.runtimeLabel(): String {
    val runtime = runtimeMinutes ?: return "Runtime TBA"
    val hours = runtime / 60
    val minutes = runtime % 60
    return if (hours > 0) {
        "${hours}h ${minutes}m"
    } else {
        "${minutes}m"
    }
}

private fun Double.oneDecimal(): String = String.format("%.1f", this)
