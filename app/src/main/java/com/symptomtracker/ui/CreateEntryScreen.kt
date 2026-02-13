package com.symptomtracker.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.symptomtracker.data.SymptomEntry
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar

private val PAIN_TYPES = listOf("Stechend", "Dumpf", "Pochend", "Brennend")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEntryScreen(
    existingBodyParts: StateFlow<List<String>>,
    onSave: (SymptomEntry) -> Unit,
    onBack: () -> Unit
) {
    var severity by remember { mutableIntStateOf(5) }
    var selectedPainType by remember { mutableStateOf(PAIN_TYPES[0]) }
    var painTypeOther by remember { mutableStateOf("") }
    var dateTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var medication by remember { mutableStateOf("") }
    var trigger by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var heartRate by remember { mutableStateOf("") }
    var bloodPressure by remember { mutableStateOf("") }
    var selectedBodyPart by remember { mutableStateOf<String?>(null) }
    
    val savedBodyParts by existingBodyParts.collectAsState()
    val sessionBodyParts = remember { mutableStateListOf<String>() }
    val context = LocalContext.current
    
    val allBodyParts by remember {
        derivedStateOf {
            (savedBodyParts + sessionBodyParts).distinct()
        }
    }

    var showAddBodyPartDialog by remember { mutableStateOf(false) }
    var newBodyPartName by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val calendar = remember(dateTimeMillis) {
        Calendar.getInstance().apply { timeInMillis = dateTimeMillis }
    }
    
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateTimeMillis)
    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE),
        is24Hour = true
    )

    val scrollState = rememberScrollState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dateMillis ->
                        val dateCal = Calendar.getInstance().apply { timeInMillis = dateMillis }
                        calendar.set(Calendar.YEAR, dateCal.get(Calendar.YEAR))
                        calendar.set(Calendar.MONTH, dateCal.get(Calendar.MONTH))
                        calendar.set(Calendar.DAY_OF_MONTH, dateCal.get(Calendar.DAY_OF_MONTH))
                        dateTimeMillis = calendar.timeInMillis
                    }
                    showDatePicker = false
                    showTimePicker = true
                }) {
                    Text("Weiter")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Abbrechen")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    calendar.set(Calendar.MINUTE, timePickerState.minute)
                    dateTimeMillis = calendar.timeInMillis
                    showTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Abbrechen")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }

    if (showAddBodyPartDialog) {
        AlertDialog(
            onDismissRequest = { showAddBodyPartDialog = false },
            title = { Text("Körperstelle hinzufügen") },
            text = {
                OutlinedTextField(
                    value = newBodyPartName,
                    onValueChange = { newBodyPartName = it },
                    label = { Text("Name der Körperstelle") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newBodyPartName.isNotBlank()) {
                        if (!allBodyParts.contains(newBodyPartName)) {
                            sessionBodyParts.add(newBodyPartName)
                        }
                        selectedBodyPart = newBodyPartName
                        newBodyPartName = ""
                        showAddBodyPartDialog = false
                    }
                }) {
                    Text("Hinzufügen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBodyPartDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Neuer Eintrag",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Zurück"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Body Part Selection
            Text(
                "Körperstelle *",
                style = MaterialTheme.typography.titleMedium,
                color = if (selectedBodyPart == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                items(allBodyParts) { part ->
                    FilterChip(
                        selected = selectedBodyPart == part,
                        onClick = { selectedBodyPart = if (selectedBodyPart == part) null else part },
                        label = { Text(part) }
                    )
                }
                item {
                    IconButton(
                        onClick = { showAddBodyPartDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Körperstelle hinzufügen",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Text(
                "Schweregrad",
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Slider(
                    value = severity.toFloat(),
                    onValueChange = { severity = it.toInt() },
                    valueRange = 1f..10f,
                    steps = 8,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$severity",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Text(
                "Art des Schmerzes",
                style = MaterialTheme.typography.titleMedium
            )
            val painButtonsDisabled = painTypeOther.isNotBlank()
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PAIN_TYPES.take(2).forEach { type ->
                    val isSelected = !painButtonsDisabled && selectedPainType == type
                    Button(
                        onClick = { selectedPainType = type },
                        enabled = !painButtonsDisabled,
                        modifier = Modifier.weight(1f),
                        colors = if (isSelected) {
                            androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
                            )
                        }
                    ) {
                        Text(
                            text = type,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PAIN_TYPES.drop(2).forEach { type ->
                    val isSelected = !painButtonsDisabled && selectedPainType == type
                    Button(
                        onClick = { selectedPainType = type },
                        enabled = !painButtonsDisabled,
                        modifier = Modifier.weight(1f),
                        colors = if (isSelected) {
                            androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
                            )
                        }
                    ) {
                        Text(
                            text = type,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1
                        )
                    }
                }
            }

            OutlinedTextField(
                value = painTypeOther,
                onValueChange = { painTypeOther = it },
                label = { Text("Sonstige (falls zutreffend)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Vitals
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = heartRate,
                    onValueChange = { heartRate = it },
                    label = { Text("Puls") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = bloodPressure,
                    onValueChange = { bloodPressure = it },
                    label = { Text("Blutdruck") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text("z.B. 120/80") }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedTextField(
                    value = formatDateTime(dateTimeMillis),
                    onValueChange = { },
                    label = { Text("Datum & Uhrzeit") },
                    modifier = Modifier.weight(1f),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Datum & Uhrzeit wählen")
                        }
                    }
                )
                Button(
                    onClick = { dateTimeMillis = System.currentTimeMillis() },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("Jetzt")
                }
            }

            OutlinedTextField(
                value = medication,
                onValueChange = { medication = it },
                label = { Text("Eingenommene Medikation") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = trigger,
                onValueChange = { trigger = it },
                label = { Text("Auslöser") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Notiz") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (selectedBodyPart == null) {
                        Toast.makeText(context, "Bitte wählen Sie eine Körperstelle aus", Toast.LENGTH_SHORT).show()
                    } else {
                        val painType = if (painTypeOther.isNotBlank()) "Sonstige" else selectedPainType
                        onSave(
                            SymptomEntry(
                                severity = severity,
                                painType = painType,
                                painTypeOther = painTypeOther.takeIf { it.isNotBlank() },
                                dateTimeMillis = dateTimeMillis,
                                medication = medication,
                                trigger = trigger,
                                note = note,
                                heartRate = heartRate.toIntOrNull(),
                                bloodPressure = bloodPressure.takeIf { it.isNotBlank() },
                                bodyPart = selectedBodyPart
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Text("Eintrag speichern")
            }
        }
    }
}

private fun formatDateTime(millis: Long): String {
    val sdf = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.GERMAN)
    return sdf.format(java.util.Date(millis))
}
