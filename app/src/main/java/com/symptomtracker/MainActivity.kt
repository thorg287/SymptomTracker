package com.symptomtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.symptomtracker.data.getDatabase
import com.symptomtracker.data.SymptomRepository
import com.symptomtracker.ui.SymptomTrackerApp
import com.symptomtracker.ui.SymptomViewModelFactory
import com.symptomtracker.ui.theme.SymptomTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = getDatabase(applicationContext)

        val repository = SymptomRepository(database.symptomEntryDao())
        val viewModelFactory = SymptomViewModelFactory(repository)

        setContent {
            SymptomTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SymptomTrackerApp(viewModelFactory = viewModelFactory)
                }
            }
        }
    }
}
