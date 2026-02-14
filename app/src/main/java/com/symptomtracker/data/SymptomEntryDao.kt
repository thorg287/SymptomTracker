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

    @Query("SELECT DISTINCT medication FROM symptom_entries WHERE medication IS NOT NULL AND medication != ''")
    fun getUniqueMedications(): Flow<List<String>>

    @Query("SELECT DISTINCT dosage FROM symptom_entries WHERE medication = :medication AND dosage IS NOT NULL AND dosage != ''")
    fun getDosagesForMedication(medication: String): Flow<List<String>>

    @Insert
    suspend fun insert(entry: SymptomEntry): Long

    @Delete
    suspend fun delete(entry: SymptomEntry)

    @Query("DELETE FROM symptom_entries WHERE bodyPart = :bodyPart")
    suspend fun deleteEntriesByBodyPart(bodyPart: String)

    @Query("DELETE FROM symptom_entries WHERE medication = :medication")
    suspend fun deleteEntriesByMedication(medication: String)
}
