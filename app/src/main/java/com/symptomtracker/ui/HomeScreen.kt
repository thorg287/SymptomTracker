package com.symptomtracker.ui

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.symptomtracker.data.BloodPressureEntry
import com.symptomtracker.data.SymptomEntry
import kotlinx.coroutines.flow.StateFlow
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ExportData(
    val symptomEntries: List<SymptomEntry>?,
    val bpEntries: List<BloodPressureEntry>?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    symptomEntries: StateFlow<List<SymptomEntry>>,
    bpEntries: StateFlow<List<BloodPressureEntry>>,
    onAddSymptomClick: () -> Unit,
    onSymptomClick: (SymptomEntry) -> Unit,
    onDeleteSymptomClick: (SymptomEntry) -> Unit,
    onAddBPClick: () -> Unit,
    onBPClick: (BloodPressureEntry) -> Unit,
    onDeleteBPClick: (BloodPressureEntry) -> Unit,
    onImportSymptomEntries: (List<SymptomEntry>) -> Unit,
    onImportBPEntries: (List<BloodPressureEntry>) -> Unit
) {
    val symptomList by symptomEntries.collectAsState()
    val bpList by bpEntries.collectAsState()

    // Using rememberSaveable ensures the tab index persists even after navigating away and back
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { exportAllData(context, it, symptomList, bpList) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { importAllData(context, it, onImportSymptomEntries, onImportBPEntries) }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (selectedTab == 0) "SymptomTracker" else "Blutdruck",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.ImportExport, contentDescription = "Daten")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Exportieren") },
                                onClick = {
                                    showMenu = false
                                    exportLauncher.launch("symptom_tracker_export_${System.currentTimeMillis()}.json")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Importieren") },
                                onClick = {
                                    showMenu = false
                                    importLauncher.launch(arrayOf("application/json"))
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.FlashOn, contentDescription = null) },
                    label = { Text("Symptome") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.MonitorHeart, contentDescription = null) },
                    label = { Text("Blutdruck") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = if (selectedTab == 0) onAddSymptomClick else onAddBPClick,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Neuer Eintrag")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (selectedTab == 0) {
            SymptomTabContent(
                padding = padding,
                entryList = symptomList,
                onEntryClick = onSymptomClick,
                onDeleteClick = onDeleteSymptomClick
            )
        } else {
            BloodPressureTabContent(
                padding = padding,
                entryList = bpList,
                onEntryClick = onBPClick,
                onDeleteClick = onDeleteBPClick
            )
        }
    }
}

@Composable
fun SymptomTabContent(
    padding: PaddingValues,
    entryList: List<SymptomEntry>,
    onEntryClick: (SymptomEntry) -> Unit,
    onDeleteClick: (SymptomEntry) -> Unit
) {
    var entryToDelete by remember { mutableStateOf<SymptomEntry?>(null) }
    val context = LocalContext.current

    if (entryToDelete != null) {
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Eintrag löschen") },
            text = { Text("Möchten Sie diesen Eintrag wirklich löschen?") },
            confirmButton = {
                TextButton(onClick = {
                    entryToDelete?.let {
                        onDeleteClick(it)
                        Toast.makeText(context, "Eintrag gelöscht", Toast.LENGTH_SHORT).show()
                    }
                    entryToDelete = null
                }) {
                    Text("Löschen", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    if (entryList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Noch keine Symptom-Einträge.\nTippen Sie auf + um einen neuen Eintrag zu erstellen.",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(32.dp)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(entryList) { entry ->
                SymptomEntryCard(
                    entry = entry,
                    onEditClick = { onEntryClick(entry) },
                    onDeleteClick = { entryToDelete = entry }
                )
            }
        }
    }
}

@Composable
fun BloodPressureTabContent(
    padding: PaddingValues,
    entryList: List<BloodPressureEntry>,
    onEntryClick: (BloodPressureEntry) -> Unit,
    onDeleteClick: (BloodPressureEntry) -> Unit
) {
    var subTabSelected by rememberSaveable { mutableIntStateOf(0) }
    var entryToDelete by remember { mutableStateOf<BloodPressureEntry?>(null) }
    val context = LocalContext.current

    if (entryToDelete != null) {
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Messung löschen") },
            text = { Text("Möchten Sie diese Messung wirklich löschen?") },
            confirmButton = {
                TextButton(onClick = {
                    entryToDelete?.let {
                        onDeleteClick(it)
                        Toast.makeText(context, "Messung gelöscht", Toast.LENGTH_SHORT).show()
                    }
                    entryToDelete = null
                }) {
                    Text("Löschen", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(padding)) {
        TabRow(
            selectedTabIndex = subTabSelected,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = subTabSelected == 0,
                onClick = { subTabSelected = 0 },
                text = { Text("Liste") }
            )
            Tab(
                selected = subTabSelected == 1,
                onClick = { subTabSelected = 1 },
                text = { Text("Diagramm") }
            )
        }

        if (subTabSelected == 0) {
            if (entryList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Noch keine Blutdruck-Einträge.\nTippen Sie auf + um eine neue Messung hinzuzufügen.",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(entryList) { entry ->
                        BloodPressureEntryCard(
                            entry = entry,
                            onEditClick = { onEntryClick(entry) },
                            onDeleteClick = { entryToDelete = entry }
                        )
                    }
                }
            }
        } else {
            BloodPressureGraph(
                entries = entryList,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SymptomEntryCard(
    entry: SymptomEntry,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)
    val timeFormat = SimpleDateFormat("HH:mm", Locale.GERMAN)
    val displayDate = dateFormat.format(Date(entry.dateTimeMillis))
    val displayTime = timeFormat.format(Date(entry.dateTimeMillis))

    val backgroundColor = getIntensityColor(entry.severity)
    val contentColor = Color.Black.copy(alpha = 0.8f)
    val labelColor = Color.Black.copy(alpha = 0.5f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onEditClick,
                onLongClick = onDeleteClick
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.bodyPart ?: "Unbekannt",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = contentColor
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FlashOn, null, Modifier.size(14.dp), labelColor)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (entry.painTypeOther?.isNotBlank() == true) entry.painTypeOther else entry.painType,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = contentColor.copy(alpha = 0.7f)
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("${entry.severity}", style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp, fontWeight = FontWeight.Black), color = contentColor)
                    Text("/ 10", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = labelColor)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 1.dp, color = contentColor.copy(alpha = 0.1f))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    AttributeItem(Icons.Default.Schedule, "ZEITPUNKT", "$displayDate | $displayTime", contentColor, labelColor)
                    if (entry.trigger.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        AttributeItem(Icons.Default.Warning, "AUSLÖSER", entry.trigger, contentColor, labelColor)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    if (entry.heartRate != null || !entry.bloodPressure.isNullOrBlank()) {
                        val vitals = buildString {
                            if (entry.heartRate != null) append("${entry.heartRate} bpm")
                            if (entry.heartRate != null && !entry.bloodPressure.isNullOrBlank()) append("\n")
                            if (!entry.bloodPressure.isNullOrBlank()) append(entry.bloodPressure)
                        }
                        AttributeItem(Icons.Default.Favorite, "VITALS", vitals, contentColor, labelColor)
                        Spacer(Modifier.height(12.dp))
                    }
                    if (entry.medications.isNotEmpty()) {
                        val medValue = entry.medications.joinToString("\n") { med ->
                            if (med.dosage?.isNotBlank() == true) "${med.name} (${med.dosage})" else med.name
                        }
                        AttributeItem(Icons.Default.MedicalServices, "MEDIKATION", medValue, contentColor, labelColor)
                    }
                }
            }
            if (entry.note.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                AttributeItem(Icons.AutoMirrored.Filled.Notes, "NOTIZ", entry.note, contentColor, labelColor)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BloodPressureEntryCard(
    entry: BloodPressureEntry,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)
    val timeFormat = SimpleDateFormat("HH:mm", Locale.GERMAN)
    val displayDate = dateFormat.format(Date(entry.dateTimeMillis))
    val displayTime = timeFormat.format(Date(entry.dateTimeMillis))

    val contentColor = MaterialTheme.colorScheme.onSurface
    val labelColor = contentColor.copy(alpha = 0.6f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onEditClick,
                onLongClick = onDeleteClick
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${entry.systolic}/${entry.diastolic}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = contentColor
                    )
                    Text(
                        text = "mmHg",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = labelColor
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Favorite, null, Modifier.size(16.dp), MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "${entry.pulse} bpm",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 1.dp, color = contentColor.copy(alpha = 0.1f))

            Row(modifier = Modifier.fillMaxWidth()) {
                AttributeItem(Icons.Default.Schedule, "ZEITPUNKT", "$displayDate | $displayTime", contentColor, labelColor)
            }
            
            if (entry.note.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                AttributeItem(Icons.AutoMirrored.Filled.Notes, "NOTIZ", entry.note, contentColor, labelColor)
            }
        }
    }
}

@Composable
private fun AttributeItem(
    icon: ImageVector,
    label: String,
    value: String,
    contentColor: Color,
    labelColor: Color
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(14.dp),
            tint = labelColor
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = labelColor,
                letterSpacing = 1.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}

private fun getIntensityColor(severity: Int): Color {
    val lowColor = Color(0xFFE8F5E9)   // Light Green
    val midColor = Color(0xFFFFF3E0)   // Light Orange
    val highColor = Color(0xFFFFEBEE)  // Light Red
    
    return when {
        severity <= 3 -> lerp(lowColor, midColor, (severity - 1) / 2f)
        severity <= 7 -> lerp(midColor, highColor, (severity - 4) / 3f)
        else -> highColor
    }
}

private fun exportAllData(context: Context, uri: Uri, symptoms: List<SymptomEntry>, bpEntries: List<BloodPressureEntry>) {
    try {
        val gson = Gson()
        val data = ExportData(symptoms, bpEntries)
        val json = gson.toJson(data)
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(json.toByteArray())
        }
        Toast.makeText(context, "Daten exportiert", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Export fehlgeschlagen: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

private fun importAllData(
    context: Context,
    uri: Uri,
    onImportSymptoms: (List<SymptomEntry>) -> Unit,
    onImportBP: (List<BloodPressureEntry>) -> Unit
) {
    try {
        val contentResolver = context.contentResolver
        contentResolver.openInputStream(uri)?.use { inputStream ->
            val reader = BufferedReader(InputStreamReader(inputStream))
            val json = reader.readText()
            val gson = Gson()
            
            // Try to import as ExportData first
            try {
                val data = gson.fromJson(json, ExportData::class.java)
                if (data.symptomEntries != null) onImportSymptoms(data.symptomEntries)
                if (data.bpEntries != null) onImportBP(data.bpEntries)
                Toast.makeText(context, "Daten importiert", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                // Fallback: Try to import as old SymptomEntry list
                try {
                    val type = object : TypeToken<List<SymptomEntry>>() {}.type
                    val entries: List<SymptomEntry> = gson.fromJson(json, type)
                    onImportSymptoms(entries)
                    Toast.makeText(context, "Alte Symptomdaten importiert", Toast.LENGTH_SHORT).show()
                } catch (e2: Exception) {
                    Toast.makeText(context, "Import fehlgeschlagen: Ungültiges Format", Toast.LENGTH_LONG).show()
                }
            }
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Import fehlgeschlagen: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
