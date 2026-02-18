package com.symptomtracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BloodPressureEntryDao {

    @Query("SELECT * FROM blood_pressure_entries ORDER BY dateTimeMillis DESC")
    fun getAllEntries(): Flow<List<BloodPressureEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: BloodPressureEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<BloodPressureEntry>)

    @Delete
    suspend fun delete(entry: BloodPressureEntry)
}
