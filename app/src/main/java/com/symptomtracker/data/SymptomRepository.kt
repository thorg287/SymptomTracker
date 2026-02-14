package com.symptomtracker.data

import kotlinx.coroutines.flow.Flow

class SymptomRepository(private val dao: SymptomEntryDao) {

    val allEntries: Flow<List<SymptomEntry>> = dao.getAllEntries()
    
    val uniqueBodyParts: Flow<List<String>> = dao.getUniqueBodyParts()

    val uniqueMedications: Flow<List<String>> = dao.getUniqueMedications()

    fun getDosagesForMedication(medication: String): Flow<List<String>> = dao.getDosagesForMedication(medication)

    suspend fun insertEntry(entry: SymptomEntry) {
        dao.insert(entry)
    }

    suspend fun deleteEntry(entry: SymptomEntry) {
        dao.delete(entry)
    }

    suspend fun deleteEntriesByBodyPart(bodyPart: String) {
        dao.deleteEntriesByBodyPart(bodyPart)
    }

    suspend fun deleteEntriesByMedication(medication: String) {
        dao.deleteEntriesByMedication(medication)
    }
}
