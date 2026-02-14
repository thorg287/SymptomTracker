package com.symptomtracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SymptomEntry::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun symptomEntryDao(): SymptomEntryDao
}

fun getDatabase(context: Context): AppDatabase = Room.databaseBuilder(
    context.applicationContext,
    AppDatabase::class.java,
    "symptom_tracker_db"
)
    .fallbackToDestructiveMigration()
    .build()
