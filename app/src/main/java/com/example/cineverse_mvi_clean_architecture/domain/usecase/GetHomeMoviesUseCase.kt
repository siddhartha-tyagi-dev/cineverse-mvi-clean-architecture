package com.example.cineverse_mvi_clean_architecture.domain.usecase

import com.example.cineverse_mvi_clean_architecture.domain.model.MovieCategory
import com.example.cineverse_mvi_clean_architecture.domain.repository.MovieRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class GetHomeMoviesUseCase @Inject constructor(
    private val repository: MovieRepository,
) {
    suspend operator fun invoke(): Result<List<MovieCategory>> = coroutineScope {
        val trending = async { repository.getTrendingMovies() }
        val popular = async { repository.getPopularMovies() }
        val nowPlaying = async { repository.getNowPlayingMovies() }
        val topRated = async { repository.getTopRatedMovies() }

        val sections = listOf(
            "Trending" to trending.await(),
            "Popular Movies" to popular.await(),
            "Now Playing" to nowPlaying.await(),
            "Top Rated" to topRated.await(),
        )

        val failure = sections.firstNotNullOfOrNull { (_, result) -> result.exceptionOrNull() }
        if (failure != null) {
            Result.failure(failure)
        } else {
            Result.success(
                sections.map { (title, result) ->
                    MovieCategory(title = title, movies = result.getOrDefault(emptyList()))
                },
            )
        }
    }
}
