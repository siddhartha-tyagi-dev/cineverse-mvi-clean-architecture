package com.example.cineverse_mvi_clean_architecture.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cineverse_mvi_clean_architecture.domain.usecase.GetHomeMoviesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
class HomeViewModel @Inject constructor(
    private val getHomeMoviesUseCase: GetHomeMoviesUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<HomeEffect>()
    val effect: SharedFlow<HomeEffect> = _effect.asSharedFlow()

    init {
        onIntent(HomeIntent.LoadMovies)
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.LoadMovies -> loadMovies(showFullScreenLoading = true)
            HomeIntent.Refresh -> loadMovies(showFullScreenLoading = false)
            HomeIntent.Retry -> loadMovies(showFullScreenLoading = true)
            is HomeIntent.MovieClicked -> emitMovieClicked(intent.movieId)
        }
    }

    private fun loadMovies(showFullScreenLoading: Boolean) {
        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    status = if (showFullScreenLoading) HomeStatus.Loading else current.status,
                    errorMessage = null,
                    isRefreshing = !showFullScreenLoading,
                )
            }

            getHomeMoviesUseCase()
                .onSuccess { sections ->
                    val visibleSections = sections.filter { it.movies.isNotEmpty() }
                    _uiState.value = if (visibleSections.isEmpty()) {
                        HomeUiState(status = HomeStatus.Empty)
                    } else {
                        HomeUiState(
                            status = HomeStatus.Success,
                            sections = visibleSections,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.value = HomeUiState(
                        status = HomeStatus.Error,
                        errorMessage = throwable.message ?: "Unable to load movies right now.",
                    )
                }
        }
    }

    private fun emitMovieClicked(movieId: Int) {
        viewModelScope.launch {
            _effect.emit(HomeEffect.MovieSelected(movieId))
            _effect.emit(HomeEffect.ShowMessage("Movie details arrive on Day 3."))
        }
    }
}
