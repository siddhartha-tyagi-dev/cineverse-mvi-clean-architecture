package com.example.cineverse_mvi_clean_architecture.presentation.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cineverse_mvi_clean_architecture.BuildConfig
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AboutViewModel(
    versionName: String = BuildConfig.VERSION_NAME,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        AboutUiState(versionName = versionName),
    )
    val uiState: StateFlow<AboutUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<AboutEffect>()
    val effect: SharedFlow<AboutEffect> = _effect.asSharedFlow()

    fun onIntent(intent: AboutIntent) {
        when (intent) {
            AboutIntent.BackClicked -> emitBack()
        }
    }

    private fun emitBack() {
        viewModelScope.launch {
            _effect.emit(AboutEffect.NavigateBack)
        }
    }
}
