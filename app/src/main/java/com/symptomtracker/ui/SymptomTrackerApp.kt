package com.symptomtracker.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object CreateEntry : Screen("create_entry?entryId={entryId}") {
        fun createRoute(entryId: Long? = null) = if (entryId != null) "create_entry?entryId=$entryId" else "create_entry"
    }
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
                onAddClick = { navController.navigate(Screen.CreateEntry.createRoute()) },
                onEntryClick = { entry -> navController.navigate(Screen.CreateEntry.createRoute(entry.id)) },
                onDeleteClick = { entry -> viewModel.deleteEntry(entry) },
                onImportEntries = { entries -> viewModel.insertEntries(entries) }
            )
        }
        composable(
            route = Screen.CreateEntry.route,
            arguments = listOf(
                navArgument("entryId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong("entryId") ?: -1L
            CreateEntryScreen(
                entryId = if (entryId != -1L) entryId else null,
                existingBodyParts = viewModel.bodyParts,
                existingMedications = viewModel.medications,
                getDosages = { medication -> viewModel.getDosagesForMedication(medication) },
                getEntry = { id -> viewModel.getEntryById(id) },
                onDeleteBodyPart = { bodyPart -> viewModel.deleteBodyPart(bodyPart) },
                onDeleteMedication = { medication -> viewModel.deleteMedication(medication) },
                onDeleteDosage = { medication, dosage -> viewModel.deleteDosage(medication, dosage) },
                onSave = { entry ->
                    viewModel.insertEntry(entry)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
