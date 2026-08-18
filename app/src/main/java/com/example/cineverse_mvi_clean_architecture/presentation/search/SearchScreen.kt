package com.example.cineverse_mvi_clean_architecture.presentation.search

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.cineverse_mvi_clean_architecture.core.network.TmdbImageUrlProvider
import com.example.cineverse_mvi_clean_architecture.domain.model.Movie
import com.example.cineverse_mvi_clean_architecture.presentation.components.MoviePosterCard

@Composable
fun SearchRoute(
    onBack: () -> Unit,
    onMovieSelected: (Int) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val movies = viewModel.movies.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel, movies) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SearchEffect.MovieSelected -> onMovieSelected(effect.movieId)
                SearchEffect.RetryPaging -> movies.retry()
            }
        }
    }

    SearchScreen(
        uiState = uiState,
        movies = movies,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onIntent = viewModel::onIntent,
    )
}

@Composable
fun SearchScreen(
    uiState: SearchUiState,
    movies: LazyPagingItems<Movie>,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onIntent: (SearchIntent) -> Unit,
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
            SearchHeader(
                query = uiState.query,
                onBack = onBack,
                onQueryChanged = { onIntent(SearchIntent.QueryChanged(it)) },
            )

            SearchResults(
                query = uiState.query,
                movies = movies,
                imageUrlProvider = imageUrlProvider,
                onMovieClick = { onIntent(SearchIntent.MovieClicked(it)) },
                onRetry = { onIntent(SearchIntent.Retry) },
            )
        }
    }
}

@Composable
private fun SearchHeader(
    query: String,
    onBack: () -> Unit,
    onQueryChanged: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onBack) {
            Text(text = "Back")
        }

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier.weight(1f),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            placeholder = { Text(text = "Search movies...") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        )
    }
}

@Composable
private fun SearchResults(
    query: String,
    movies: LazyPagingItems<Movie>,
    imageUrlProvider: TmdbImageUrlProvider,
    onMovieClick: (Int) -> Unit,
    onRetry: () -> Unit,
) {
    val refreshState = movies.loadState.refresh
    val appendState = movies.loadState.append

    when {
        query.isBlank() -> SearchMessageState(
            title = "Start searching for a movie",
            message = "Type a title to discover matching TMDB results.",
        )

        refreshState is LoadState.Loading -> SearchLoadingState()

        refreshState is LoadState.Error -> SearchMessageState(
            title = "Search failed",
            message = refreshState.error.userMessage(),
            actionText = "Retry",
            onAction = onRetry,
        )

        movies.itemCount == 0 -> SearchMessageState(
            title = "No movies found",
            message = "Try searching with a different title.",
        )

        else -> LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            items(
                count = movies.itemCount,
                key = { index -> movies.peek(index)?.id ?: "placeholder-$index" },
            ) { index ->
                val movie = movies[index]
                if (movie == null) {
                    MoviePlaceholder()
                } else {
                    MoviePosterCard(
                        movie = movie,
                        imageUrl = imageUrlProvider.posterUrl(movie.posterPath),
                        onClick = { onMovieClick(movie.id) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            appendLoadState(appendState = appendState, onRetry = onRetry)
        }
    }
}

private fun LazyGridScope.appendLoadState(
    appendState: LoadState,
    onRetry: () -> Unit,
) {
    when (appendState) {
        is LoadState.Loading -> item(span = { GridItemSpan(maxLineSpan) }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        is LoadState.Error -> item(span = { GridItemSpan(maxLineSpan) }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = appendState.error.userMessage(),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(onClick = onRetry) {
                    Text(text = "Retry")
                }
            }
        }

        is LoadState.NotLoading -> Unit
    }
}

@Composable
private fun SearchLoadingState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(24.dp))
        SearchMessageState(
            title = "Searching movies",
            message = "Finding matching titles on TMDB.",
        )
    }
}

@Composable
private fun MoviePlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.56f),
                shape = RoundedCornerShape(8.dp),
            ),
    )
}

@Composable
private fun SearchMessageState(
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

private fun Throwable.userMessage(): String =
    message?.takeIf { it.isNotBlank() } ?: "Check your connection and try again."
