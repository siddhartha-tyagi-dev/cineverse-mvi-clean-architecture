package com.example.cineverse_mvi_clean_architecture.di

import android.content.Context
import androidx.room3.Room
import com.example.cineverse_mvi_clean_architecture.data.local.CineVerseDatabase
import com.example.cineverse_mvi_clean_architecture.data.local.MovieDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): CineVerseDatabase =
        Room.databaseBuilder(
            context = context,
            klass = CineVerseDatabase::class.java,
            name = "cineverse.db",
        ).build()

    @Provides
    fun provideMovieDao(database: CineVerseDatabase): MovieDao =
        database.movieDao()
}
