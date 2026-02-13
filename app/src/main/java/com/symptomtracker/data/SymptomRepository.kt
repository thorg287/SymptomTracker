package com.symptomtracker.data

import kotlinx.coroutines.flow.Flow

class SymptomRepository(private val dao: SymptomEntryDao) {

    val allEntries: Flow<List<SymptomEntry>> = dao.getAllEntries()
    
    val uniqueBodyParts: Flow<List<String>> = dao.getUniqueBodyParts()

    suspend fun insertEntry(entry: SymptomEntry) {
        dao.insert(entry)
    }

    suspend fun deleteEntry(entry: SymptomEntry) {
        dao.delete(entry)
    }
}
