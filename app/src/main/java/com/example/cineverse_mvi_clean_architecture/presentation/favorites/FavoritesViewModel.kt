package com.example.cineverse_mvi_clean_architecture.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cineverse_mvi_clean_architecture.domain.usecase.GetFavoriteMoviesUseCase
import com.example.cineverse_mvi_clean_architecture.domain.usecase.RemoveFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val getFavoriteMoviesUseCase: GetFavoriteMoviesUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<FavoritesEffect>()
    val effect: SharedFlow<FavoritesEffect> = _effect.asSharedFlow()

    private var favoritesJob: Job? = null

    init {
        onIntent(FavoritesIntent.LoadFavorites)
    }

    fun onIntent(intent: FavoritesIntent) {
        when (intent) {
            FavoritesIntent.LoadFavorites -> observeFavorites()
            FavoritesIntent.BackClicked -> emitBack()
            is FavoritesIntent.MovieClicked -> emitMovieSelected(intent.movieId)
            is FavoritesIntent.RemoveFavorite -> removeFavorite(intent.movieId)
        }
    }

    private fun observeFavorites() {
        favoritesJob?.cancel()
        favoritesJob = viewModelScope.launch {
            _uiState.update { it.copy(status = FavoritesStatus.Loading, errorMessage = null) }
            getFavoriteMoviesUseCase()
                .catch { throwable ->
                    _uiState.value = FavoritesUiState(
                        status = FavoritesStatus.Error,
                        errorMessage = throwable.message ?: "Unable to load favorites right now.",
                    )
                }
                .collect { movies ->
                    _uiState.value = if (movies.isEmpty()) {
                        FavoritesUiState(status = FavoritesStatus.Empty)
                    } else {
                        FavoritesUiState(
                            status = FavoritesStatus.Success,
                            movies = movies,
                        )
                    }
                }
        }
    }

    private fun removeFavorite(movieId: Int) {
        viewModelScope.launch {
            removeFavoriteUseCase(movieId)
                .onSuccess {
                    _effect.emit(FavoritesEffect.ShowMessage("Removed from favorites"))
                }
                .onFailure { throwable ->
                    _effect.emit(
                        FavoritesEffect.ShowMessage(
                            throwable.message ?: "Could not remove favorite.",
                        ),
                    )
                }
        }
    }

    private fun emitBack() {
        viewModelScope.launch {
            _effect.emit(FavoritesEffect.NavigateBack)
        }
    }

    private fun emitMovieSelected(movieId: Int) {
        viewModelScope.launch {
            _effect.emit(FavoritesEffect.MovieSelected(movieId))
        }
    }
}
