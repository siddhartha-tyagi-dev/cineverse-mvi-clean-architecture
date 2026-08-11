package com.example.cineverse_mvi_clean_architecture

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.cineverse_mvi_clean_architecture.presentation.home.HomeRoute
import com.example.cineverse_mvi_clean_architecture.presentation.theme.CineVerseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CineVerseTheme {
                HomeRoute()
            }
        }
    }
}
