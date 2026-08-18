package com.example.cineverse_mvi_clean_architecture.data.local

import androidx.room3.Database
import androidx.room3.RoomDatabase

@Database(
    entities = [MovieEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class CineVerseDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
}
