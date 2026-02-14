package com.symptomtracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "symptom_entries")
data class SymptomEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val severity: Int,
    val painType: String,
    val painTypeOther: String?,
    val dateTimeMillis: Long,
    val medication: String,
    val dosage: String? = null,
    val trigger: String,
    val note: String,
    val heartRate: Int? = null,
    val bloodPressure: String? = null,
    val bodyPart: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
