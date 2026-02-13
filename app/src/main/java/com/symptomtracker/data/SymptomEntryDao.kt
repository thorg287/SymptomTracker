package com.symptomtracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SymptomEntryDao {

    @Query("SELECT * FROM symptom_entries ORDER BY dateTimeMillis DESC")
    fun getAllEntries(): Flow<List<SymptomEntry>>

    @Query("SELECT DISTINCT bodyPart FROM symptom_entries WHERE bodyPart IS NOT NULL AND bodyPart != ''")
    fun getUniqueBodyParts(): Flow<List<String>>

    @Insert
    suspend fun insert(entry: SymptomEntry): Long

    @Delete
    suspend fun delete(entry: SymptomEntry)
}
