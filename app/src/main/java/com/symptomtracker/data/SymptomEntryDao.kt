package com.symptomtracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SymptomEntryDao {

    @Query("SELECT * FROM symptom_entries ORDER BY dateTimeMillis DESC")
    fun getAllEntries(): Flow<List<SymptomEntry>>

    @Query("SELECT DISTINCT bodyPart FROM symptom_entries WHERE bodyPart IS NOT NULL AND bodyPart != ''")
    fun getUniqueBodyParts(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: SymptomEntry): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<SymptomEntry>)

    @Delete
    suspend fun delete(entry: SymptomEntry)

    @Query("DELETE FROM symptom_entries WHERE bodyPart = :bodyPart")
    suspend fun deleteEntriesByBodyPart(bodyPart: String)

    // Note: Medication related queries moved to ViewModel/Repository processing 
    // due to medications being stored in a JSON list.
}
