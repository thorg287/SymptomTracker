package com.symptomtracker.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SymptomRepository(
    private val symptomDao: SymptomEntryDao,
    private val bloodPressureDao: BloodPressureEntryDao
) {

    // Symptom Entries
    val allSymptomEntries: Flow<List<SymptomEntry>> = symptomDao.getAllEntries()
    
    val uniqueBodyParts: Flow<List<String>> = symptomDao.getUniqueBodyParts()

    val uniqueMedications: Flow<List<String>> = allSymptomEntries.map { entries ->
        entries.flatMap { it.medications }
            .map { it.name }
            .distinct()
            .sorted()
    }

    fun getDosagesForMedication(medication: String): Flow<List<String>> = allSymptomEntries.map { entries ->
        entries.flatMap { it.medications }
            .filter { it.name == medication }
            .mapNotNull { it.dosage }
            .distinct()
            .sorted()
    }

    suspend fun insertSymptomEntry(entry: SymptomEntry) {
        val symptomId = symptomDao.insert(entry)
        
        // Automatically create or update a BloodPressureEntry if data is available
        val systolicDiastolic = entry.bloodPressure?.split("/")
        val systolic = systolicDiastolic?.getOrNull(0)?.trim()?.toIntOrNull()
        val diastolic = systolicDiastolic?.getOrNull(1)?.trim()?.toIntOrNull()
        val pulse = entry.heartRate

        if (systolic != null || diastolic != null || pulse != null) {
            // Check if we already have an entry for this symptom to update it
            val existingBPs = bloodPressureDao.getAllEntries().first()
            val existingBP = existingBPs.find { it.symptomId == symptomId }
            
            val bpEntry = BloodPressureEntry(
                id = existingBP?.id ?: 0,
                systolic = systolic,
                diastolic = diastolic,
                pulse = pulse,
                dateTimeMillis = entry.dateTimeMillis,
                note = "Verbunden mit Symptom: ${entry.bodyPart ?: ""}",
                symptomId = symptomId,
                createdAt = existingBP?.createdAt ?: System.currentTimeMillis()
            )
            bloodPressureDao.insert(bpEntry)
        }
    }
    
    suspend fun insertAllSymptomEntries(entries: List<SymptomEntry>) {
        symptomDao.insertAll(entries)
    }

    suspend fun deleteSymptomEntry(entry: SymptomEntry) {
        // Also delete associated BP entry if it exists
        val existingBPs = bloodPressureDao.getAllEntries().first()
        val associatedBP = existingBPs.find { it.symptomId == entry.id }
        associatedBP?.let { bloodPressureDao.delete(it) }

        symptomDao.delete(entry)
    }

    // Blood Pressure Entries
    val allBloodPressureEntries: Flow<List<BloodPressureEntry>> = bloodPressureDao.getAllEntries()

    suspend fun insertBloodPressureEntry(entry: BloodPressureEntry) {
        bloodPressureDao.insert(entry)
    }

    suspend fun insertAllBloodPressureEntries(entries: List<BloodPressureEntry>) {
        bloodPressureDao.insertAll(entries)
    }

    suspend fun deleteBloodPressureEntry(entry: BloodPressureEntry) {
        bloodPressureDao.delete(entry)
    }

    // Other existing methods updated to use symptomDao
    suspend fun deleteEntriesByBodyPart(bodyPart: String) {
        symptomDao.deleteEntriesByBodyPart(bodyPart)
    }

    suspend fun deleteEntriesByMedication(medication: String) {
        try {
            val entries = allSymptomEntries.first()
            val entriesToDelete = entries.filter { entry ->
                entry.medications.any { it.name == medication }
            }
            entriesToDelete.forEach { symptomDao.delete(it) }
        } catch (e: Exception) {
            // Handle or log
        }
    }

    suspend fun deleteDosage(medication: String, dosage: String) {
        try {
            val entries = allSymptomEntries.first()
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
            symptomDao.insertAll(updatedEntries)
        } catch (e: Exception) {
            // Handle or log
        }
    }
}
