package com.symptomtracker.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object CreateEntry : Screen("create_entry")
}

@Composable
fun SymptomTrackerApp(
    viewModelFactory: SymptomViewModelFactory
) {
    val navController = rememberNavController()
    val viewModel: SymptomViewModel = viewModel(factory = viewModelFactory)

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                entries = viewModel.entries,
                onAddClick = { navController.navigate(Screen.CreateEntry.route) },
                onDeleteClick = { entry -> viewModel.deleteEntry(entry) }
            )
        }
        composable(Screen.CreateEntry.route) {
            CreateEntryScreen(
                existingBodyParts = viewModel.bodyParts,
                existingMedications = viewModel.medications,
                getDosages = { medication -> viewModel.getDosagesForMedication(medication) },
                onDeleteBodyPart = { bodyPart -> viewModel.deleteBodyPart(bodyPart) },
                onDeleteMedication = { medication -> viewModel.deleteMedication(medication) },
                onSave = { entry ->
                    viewModel.insertEntry(entry)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
