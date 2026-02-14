package com.symptomtracker.ui

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.symptomtracker.data.SymptomEntry
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar

private val PAIN_TYPES = listOf("Stechend", "Dumpf", "Pochend", "Brennend")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CreateEntryScreen(
    existingBodyParts: StateFlow<List<String>>,
    existingMedications: StateFlow<List<String>>,
    getDosages: (String) -> StateFlow<List<String>>,
    onSave: (SymptomEntry) -> Unit,
    onDeleteBodyPart: (String) -> Unit,
    onDeleteMedication: (String) -> Unit,
    onBack: () -> Unit
) {
    var severity by remember { mutableIntStateOf(5) }
    var selectedPainType by remember { mutableStateOf(PAIN_TYPES[0]) }
    var painTypeOther by remember { mutableStateOf("") }
    var dateTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var trigger by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var heartRate by remember { mutableStateOf("") }
    var bloodPressureValue by remember { mutableStateOf(TextFieldValue("")) }
    var selectedBodyPart by remember { mutableStateOf<String?>(null) }
    var selectedMedication by remember { mutableStateOf<String?>(null) }
    var selectedDosage by remember { mutableStateOf<String?>(null) }
    
    val savedBodyParts by existingBodyParts.collectAsState()
    val savedMedications by existingMedications.collectAsState()
    
    val dosagesForMed by remember(selectedMedication) {
        derivedStateOf { 
            selectedMedication?.let { getDosages(it) } 
        }
    }
    
    val emptyListState = remember { mutableStateOf(emptyList<String>()) }
    val savedDosages by (dosagesForMed?.collectAsState() ?: emptyListState)

    val sessionBodyParts = remember { mutableStateListOf<String>() }
    val sessionMedications = remember { mutableStateListOf<String>() }
    val sessionDosages = remember { mutableStateListOf<String>() }
    
    val context = LocalContext.current
    
    val allBodyParts by remember { derivedStateOf { (savedBodyParts + sessionBodyParts).distinct() } }
    val allMedications by remember { derivedStateOf { (savedMedications + sessionMedications).distinct() } }
    val allDosages by remember { derivedStateOf { (savedDosages + sessionDosages).distinct() } }

    var showAddBodyPartDialog by remember { mutableStateOf(false) }
    var showAddMedicationDialog by remember { mutableStateOf(false) }
    var showAddDosageDialog by remember { mutableStateOf(false) }

    var itemToDelete by remember { mutableStateOf<Pair<String, String>?>(null) } // Type to Name
    
    var newBodyPartName by remember { mutableStateOf("") }
    var newMedicationName by remember { mutableStateOf("") }
    var newDosageName by remember { mutableStateOf("") }

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
                }) { Text("Weiter") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Abbrechen") } }
        ) { DatePicker(state = datePickerState) }
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
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Abbrechen") } },
            text = { TimePicker(state = timePickerState) }
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
                        if (!allBodyParts.contains(newBodyPartName)) sessionBodyParts.add(newBodyPartName)
                        selectedBodyPart = newBodyPartName
                        newBodyPartName = ""
                        showAddBodyPartDialog = false
                    }
                }) { Text("Hinzufügen") }
            },
            dismissButton = { TextButton(onClick = { showAddBodyPartDialog = false }) { Text("Abbrechen") } }
        )
    }

    if (showAddMedicationDialog) {
        AlertDialog(
            onDismissRequest = { showAddMedicationDialog = false },
            title = { Text("Medikation hinzufügen") },
            text = {
                OutlinedTextField(
                    value = newMedicationName,
                    onValueChange = { newMedicationName = it },
                    label = { Text("Name der Medikation") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newMedicationName.isNotBlank()) {
                        if (!allMedications.contains(newMedicationName)) sessionMedications.add(newMedicationName)
                        selectedMedication = newMedicationName
                        selectedDosage = null
                        newMedicationName = ""
                        showAddMedicationDialog = false
                    }
                }) { Text("Hinzufügen") }
            },
            dismissButton = { TextButton(onClick = { showAddMedicationDialog = false }) { Text("Abbrechen") } }
        )
    }

    if (showAddDosageDialog) {
        AlertDialog(
            onDismissRequest = { showAddDosageDialog = false },
            title = { Text("Dosierung hinzufügen") },
            text = {
                OutlinedTextField(
                    value = newDosageName,
                    onValueChange = { newDosageName = it },
                    label = { Text("Dosierung (z.B. 400mg)") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newDosageName.isNotBlank()) {
                        if (!allDosages.contains(newDosageName)) sessionDosages.add(newDosageName)
                        selectedDosage = newDosageName
                        newDosageName = ""
                        showAddDosageDialog = false
                    }
                }) { Text("Hinzufügen") }
            },
            dismissButton = { TextButton(onClick = { showAddDosageDialog = false }) { Text("Abbrechen") } }
        )
    }

    itemToDelete?.let { (type, name) ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Löschen bestätigen") },
            text = { Text("Möchten Sie '$name' und alle damit verbundenen Einträge wirklich löschen?") },
            confirmButton = {
                TextButton(onClick = {
                    if (type == "BODY_PART") {
                        onDeleteBodyPart(name)
                        sessionBodyParts.remove(name)
                        if (selectedBodyPart == name) selectedBodyPart = null
                    } else if (type == "MEDICATION") {
                        onDeleteMedication(name)
                        sessionMedications.remove(name)
                        if (selectedMedication == name) {
                            selectedMedication = null
                            selectedDosage = null
                        }
                    }
                    itemToDelete = null
                }) { Text("Löschen", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { itemToDelete = null }) { Text("Abbrechen") } }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Neuer Eintrag", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
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
                    SelectableChip(
                        selected = selectedBodyPart == part,
                        label = part,
                        onClick = { selectedBodyPart = if (selectedBodyPart == part) null else part },
                        onLongClick = { itemToDelete = "BODY_PART" to part }
                    )
                }
                item {
                    IconButton(onClick = { showAddBodyPartDialog = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "Hinzufügen", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Text("Schweregrad", style = MaterialTheme.typography.titleMedium)
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
                Text(text = "$severity", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(start = 16.dp))
            }

            Text("Art des Schmerzes", style = MaterialTheme.typography.titleMedium)
            val painButtonsDisabled = painTypeOther.isNotBlank()
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    PAIN_TYPES.take(2).forEach { type ->
                        val isSelected = !painButtonsDisabled && selectedPainType == type
                        Button(
                            onClick = { selectedPainType = type },
                            enabled = !painButtonsDisabled,
                            modifier = Modifier.weight(1f),
                            colors = if (isSelected) ButtonDefaults.buttonColors() else ButtonDefaults.filledTonalButtonColors()
                        ) { Text(text = type, style = MaterialTheme.typography.labelMedium, maxLines = 1) }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    PAIN_TYPES.drop(2).forEach { type ->
                        val isSelected = !painButtonsDisabled && selectedPainType == type
                        Button(
                            onClick = { selectedPainType = type },
                            enabled = !painButtonsDisabled,
                            modifier = Modifier.weight(1f),
                            colors = if (isSelected) ButtonDefaults.buttonColors() else ButtonDefaults.filledTonalButtonColors()
                        ) { Text(text = type, style = MaterialTheme.typography.labelMedium, maxLines = 1) }
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = heartRate,
                    onValueChange = { heartRate = it },
                    label = { Text("Puls") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = bloodPressureValue,
                    onValueChange = { newValue ->
                        val input = newValue.text
                        val filtered = input.filter { it.isDigit() || it == '/' }
                        
                        var updatedText = filtered
                        var updatedSelection = newValue.selection
                        
                        if (filtered.length > bloodPressureValue.text.length) {
                            if (filtered.length == 3 && !filtered.contains('/')) {
                                updatedText = filtered + "/"
                                updatedSelection = TextRange(updatedText.length)
                            }
                        }
                        
                        bloodPressureValue = TextFieldValue(updatedText, updatedSelection)
                    },
                    label = { Text("Blutdruck") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text("z.B. 120/80") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedTextField(
                    value = formatDateTime(dateTimeMillis),
                    onValueChange = { },
                    label = { Text("Datum & Uhrzeit") },
                    modifier = Modifier.weight(1f),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Wählen")
                        }
                    }
                )
                Button(onClick = { dateTimeMillis = System.currentTimeMillis() }, modifier = Modifier.padding(start = 8.dp)) { Text("Jetzt") }
            }

            // Medication Selection
            Text("Medikation", style = MaterialTheme.typography.titleMedium)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                items(allMedications) { med ->
                    SelectableChip(
                        selected = selectedMedication == med,
                        label = med,
                        onClick = { 
                            if (selectedMedication == med) {
                                selectedMedication = null
                                selectedDosage = null
                            } else {
                                selectedMedication = med
                                selectedDosage = null
                            }
                        },
                        onLongClick = { itemToDelete = "MEDICATION" to med }
                    )
                }
                item {
                    IconButton(onClick = { showAddMedicationDialog = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "Hinzufügen", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Dosage Selection
            if (selectedMedication != null) {
                Text("Dosierung für $selectedMedication", style = MaterialTheme.typography.titleMedium)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(allDosages) { dos ->
                        SelectableChip(
                            selected = selectedDosage == dos,
                            label = dos,
                            onClick = { selectedDosage = if (selectedDosage == dos) null else dos },
                            onLongClick = {}
                        )
                    }
                    item {
                        IconButton(onClick = { showAddDosageDialog = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Hinzufügen", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

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
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                                medication = selectedMedication ?: "",
                                dosage = selectedDosage,
                                trigger = trigger,
                                note = note,
                                heartRate = heartRate.toIntOrNull(),
                                bloodPressure = bloodPressureValue.text.takeIf { it.isNotBlank() },
                                bodyPart = selectedBodyPart
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp)
            ) { 
                Text(
                    "Eintrag speichern", 
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.Bold 
                ) 
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SelectableChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun ButtonDefaults.filledTonalButtonColors() = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors()
@Composable
private fun ButtonDefaults.buttonColors() = androidx.compose.material3.ButtonDefaults.buttonColors()

private fun formatDateTime(millis: Long): String {
    val sdf = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.GERMAN)
    return sdf.format(java.util.Date(millis))
}
