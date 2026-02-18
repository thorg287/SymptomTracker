package com.symptomtracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [SymptomEntry::class, BloodPressureEntry::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(MedicationConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun symptomEntryDao(): SymptomEntryDao
    abstract fun bloodPressureEntryDao(): BloodPressureEntryDao
}

fun getDatabase(context: Context): AppDatabase = Room.databaseBuilder(
    context.applicationContext,
    AppDatabase::class.java,
    "symptom_tracker_db"
)
    .fallbackToDestructiveMigration()
    .build()
