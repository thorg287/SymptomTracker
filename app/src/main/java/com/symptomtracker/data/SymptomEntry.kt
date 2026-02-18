package com.symptomtracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class MedicationEntry(
    val name: String,
    val dosage: String? = null
)

class MedicationConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromMedicationList(value: List<MedicationEntry>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toMedicationList(value: String): List<MedicationEntry>? {
        val listType = object : TypeToken<List<MedicationEntry>>() {}.type
        return gson.fromJson(value, listType)
    }
}

@Entity(tableName = "symptom_entries")
data class SymptomEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val severity: Int,
    val painType: String,
    val painTypeOther: String?,
    val dateTimeMillis: Long,
    val medications: List<MedicationEntry> = emptyList(),
    val trigger: String,
    val note: String,
    val heartRate: Int? = null,
    val bloodPressure: String? = null,
    val bodyPart: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    // Legacy compatibility for older code if needed, though we'll update it
    val medication: String
        get() = medications.firstOrNull()?.name ?: ""
    val dosage: String?
        get() = medications.firstOrNull()?.dosage
}
