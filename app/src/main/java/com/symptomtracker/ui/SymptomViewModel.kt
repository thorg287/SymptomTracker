package com.symptomtracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.symptomtracker.data.SymptomEntry
import com.symptomtracker.data.SymptomRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

    fun insertEntry(entry: SymptomEntry) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.insertEntry(entry)
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
