package com.example.cineverse_mvi_clean_architecture.di

import com.example.cineverse_mvi_clean_architecture.data.repository.MovieRepositoryImpl
import com.example.cineverse_mvi_clean_architecture.domain.repository.MovieRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindMovieRepository(
        implementation: MovieRepositoryImpl,
    ): MovieRepository
}
