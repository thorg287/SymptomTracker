package com.symptomtracker.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SymptomRepository(private val dao: SymptomEntryDao) {

    val allEntries: Flow<List<SymptomEntry>> = dao.getAllEntries()
    
    val uniqueBodyParts: Flow<List<String>> = dao.getUniqueBodyParts()

    val uniqueMedications: Flow<List<String>> = allEntries.map { entries ->
        entries.flatMap { it.medications }
            .map { it.name }
            .distinct()
            .sorted()
    }

    fun getDosagesForMedication(medication: String): Flow<List<String>> = allEntries.map { entries ->
        entries.flatMap { it.medications }
            .filter { it.name == medication }
            .mapNotNull { it.dosage }
            .distinct()
            .sorted()
    }

    suspend fun insertEntry(entry: SymptomEntry) {
        dao.insert(entry)
    }
    
    suspend fun insertAll(entries: List<SymptomEntry>) {
        dao.insertAll(entries)
    }

    suspend fun deleteEntry(entry: SymptomEntry) {
        dao.delete(entry)
    }

    suspend fun deleteEntriesByBodyPart(bodyPart: String) {
        dao.deleteEntriesByBodyPart(bodyPart)
    }

    suspend fun deleteEntriesByMedication(medication: String) {
        try {
            val entries = allEntries.first()
            val entriesToDelete = entries.filter { entry ->
                entry.medications.any { it.name == medication }
            }
            entriesToDelete.forEach { dao.delete(it) }
        } catch (e: Exception) {
            // Handle or log
        }
    }

    suspend fun deleteDosage(medication: String, dosage: String) {
        try {
            val entries = allEntries.first()
            val updatedEntries = entries.map { entry ->
                val updatedMeds = entry.medications.map { med ->
                    if (med.name == medication && med.dosage == dosage) {
                        med.copy(dosage = null)
                    } else {
                        med
                    }
                }
                entry.copy(medications = updatedMeds)
            }
            dao.insertAll(updatedEntries)
        } catch (e: Exception) {
            // Handle or log
        }
    }
}
