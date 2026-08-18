package com.example.cineverse_mvi_clean_architecture.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cineverse_mvi_clean_architecture.domain.model.MovieDetails
import com.example.cineverse_mvi_clean_architecture.domain.usecase.FavoriteMovieUseCase
import com.example.cineverse_mvi_clean_architecture.domain.usecase.GetMovieDetailsUseCase
import com.example.cineverse_mvi_clean_architecture.domain.usecase.IsMovieFavoriteUseCase
import com.example.cineverse_mvi_clean_architecture.domain.usecase.RemoveFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    private val getMovieDetailsUseCase: GetMovieDetailsUseCase,
    private val favoriteMovieUseCase: FavoriteMovieUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase,
    private val isMovieFavoriteUseCase: IsMovieFavoriteUseCase,
) : ViewModel() {
    private var currentMovieId: Int = 0
    private var favoriteJob: Job? = null

    private val _uiState = MutableStateFlow(MovieDetailsUiState())
    val uiState: StateFlow<MovieDetailsUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<MovieDetailsEffect>()
    val effect: SharedFlow<MovieDetailsEffect> = _effect.asSharedFlow()

    fun onIntent(intent: MovieDetailsIntent) {
        when (intent) {
            is MovieDetailsIntent.LoadDetails -> loadDetails(intent.movieId)
            MovieDetailsIntent.Retry -> loadDetails(currentMovieId)
            MovieDetailsIntent.BackClicked -> emitBack()
            MovieDetailsIntent.FavoriteClicked -> toggleFavorite()
            is MovieDetailsIntent.MovieClicked -> emitMovieSelected(intent.movieId)
        }
    }

    private fun loadDetails(movieId: Int) {
        currentMovieId = movieId
        observeFavorite(movieId)
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    status = MovieDetailsStatus.Loading,
                    errorMessage = null,
                )
            }

            getMovieDetailsUseCase(currentMovieId)
                .onSuccess { details ->
                    _uiState.update { current ->
                        current.copy(
                            status = MovieDetailsStatus.Success,
                            movieDetails = details,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            status = MovieDetailsStatus.Error,
                            movieDetails = null,
                            errorMessage = throwable.message ?: "Unable to load movie details right now.",
                        )
                    }
                }
        }
    }

    private fun observeFavorite(movieId: Int) {
        favoriteJob?.cancel()
        favoriteJob = viewModelScope.launch {
            isMovieFavoriteUseCase(movieId).collect { isFavorite ->
                _uiState.update { it.copy(isFavorite = isFavorite) }
            }
        }
    }

    private fun toggleFavorite() {
        val details = uiState.value.movieDetails ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingFavorite = true) }
            val wasFavorite = uiState.value.isFavorite
            val result = if (wasFavorite) {
                removeFavoriteUseCase(details.id)
            } else {
                favoriteMovieUseCase(details)
            }

            _uiState.update { it.copy(isUpdatingFavorite = false) }
            result
                .onSuccess {
                    _effect.emit(
                        MovieDetailsEffect.ShowMessage(
                            if (wasFavorite) "Removed from favorites" else "Added to favorites",
                        ),
                    )
                }
                .onFailure { throwable ->
                    _effect.emit(
                        MovieDetailsEffect.ShowMessage(
                            throwable.message ?: "Could not update favorites.",
                        ),
                    )
                }
        }
    }

    private fun emitBack() {
        viewModelScope.launch {
            _effect.emit(MovieDetailsEffect.NavigateBack)
        }
    }

    private fun emitMovieSelected(movieId: Int) {
        viewModelScope.launch {
            _effect.emit(MovieDetailsEffect.MovieSelected(movieId))
        }
    }
}
