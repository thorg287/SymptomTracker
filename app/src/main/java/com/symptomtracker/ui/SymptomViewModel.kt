package com.symptomtracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.symptomtracker.data.SymptomEntry
import com.symptomtracker.data.SymptomRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SymptomViewModel(private val repository: SymptomRepository) : ViewModel() {

    val entries: StateFlow<List<SymptomEntry>> = repository.allEntries
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val bodyParts: StateFlow<List<String>> = repository.uniqueBodyParts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val medications: StateFlow<List<String>> = repository.uniqueMedications
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getDosagesForMedication(medication: String): StateFlow<List<String>> = 
        repository.getDosagesForMedication(medication)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun getEntryById(id: Long): StateFlow<SymptomEntry?> = 
        entries.map { list -> list.find { it.id == id } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    fun insertEntry(entry: SymptomEntry) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.insertEntry(entry)
            }
        }
    }

    fun insertEntries(entries: List<SymptomEntry>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.insertAll(entries)
            }
        }
    }

    fun deleteEntry(entry: SymptomEntry) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.deleteEntry(entry)
            }
        }
    }

    fun deleteBodyPart(bodyPart: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.deleteEntriesByBodyPart(bodyPart)
            }
        }
    }

    fun deleteMedication(medication: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.deleteEntriesByMedication(medication)
            }
        }
    }

    fun deleteDosage(medication: String, dosage: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.deleteDosage(medication, dosage)
            }
        }
    }
}

class SymptomViewModelFactory(private val repository: SymptomRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SymptomViewModel::class.java)) {
            return SymptomViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
