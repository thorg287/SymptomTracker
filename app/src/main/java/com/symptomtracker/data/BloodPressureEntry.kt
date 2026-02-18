package com.symptomtracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blood_pressure_entries")
data class BloodPressureEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val systolic: Int? = null,
    val diastolic: Int? = null,
    val pulse: Int? = null,
    val dateTimeMillis: Long,
    val note: String = "",
    val symptomId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
