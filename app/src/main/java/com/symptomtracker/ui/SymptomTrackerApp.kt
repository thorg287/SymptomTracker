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
    data object CreateBloodPressure : Screen("create_bp?entryId={entryId}") {
        fun createRoute(entryId: Long? = null) = if (entryId != null) "create_bp?entryId=$entryId" else "create_bp"
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
                symptomEntries = viewModel.entries,
                bpEntries = viewModel.bloodPressureEntries,
                onAddSymptomClick = { navController.navigate(Screen.CreateEntry.createRoute()) },
                onSymptomClick = { entry -> navController.navigate(Screen.CreateEntry.createRoute(entry.id)) },
                onDeleteSymptomClick = { entry -> viewModel.deleteEntry(entry) },
                onAddBPClick = { navController.navigate(Screen.CreateBloodPressure.createRoute()) },
                onBPClick = { entry -> navController.navigate(Screen.CreateBloodPressure.createRoute(entry.id)) },
                onDeleteBPClick = { entry -> viewModel.deleteBloodPressureEntry(entry) },
                onImportSymptomEntries = { entries -> viewModel.insertEntries(entries) },
                onImportBPEntries = { entries -> viewModel.insertBloodPressureEntries(entries) }
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
        composable(
            route = Screen.CreateBloodPressure.route,
            arguments = listOf(
                navArgument("entryId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong("entryId") ?: -1L
            CreateBloodPressureScreen(
                entryId = if (entryId != -1L) entryId else null,
                getEntry = { id -> viewModel.getBloodPressureEntryById(id) },
                onSave = { entry ->
                    viewModel.insertBloodPressureEntry(entry)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
