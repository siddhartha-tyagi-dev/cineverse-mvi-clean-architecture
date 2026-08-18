package com.example.cineverse_mvi_clean_architecture.presentation.about

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AboutViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state exposes app information`() {
        val viewModel = AboutViewModel(versionName = "1.2.3")

        val state = viewModel.uiState.value

        assertEquals("CineVerse", state.appName)
        assertEquals("1.2.3", state.versionName)
        assertEquals("MVI + Clean Architecture", state.architecture)
    }

    @Test
    fun `back click emits navigation effect`() = runTest {
        val viewModel = AboutViewModel(versionName = "1.2.3")
        val effects = mutableListOf<AboutEffect>()
        val job = launch { viewModel.effect.collect { effects += it } }

        viewModel.onIntent(AboutIntent.BackClicked)
        advanceUntilIdle()

        assertEquals(AboutEffect.NavigateBack, effects.single())
        job.cancel()
    }
}
