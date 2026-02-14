package com.symptomtracker.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.symptomtracker.data.SymptomEntry
import com.symptomtracker.ui.theme.SymptomTrackerTheme
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    entries: StateFlow<List<SymptomEntry>>,
    onAddClick: () -> Unit,
    onDeleteClick: (SymptomEntry) -> Unit
) {
    val entryList by entries.collectAsState()

    HomeScreenContent(
        entryList = entryList,
        onAddClick = onAddClick,
        onDeleteClick = onDeleteClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    entryList: List<SymptomEntry>,
    onAddClick: () -> Unit,
    onDeleteClick: (SymptomEntry) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
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

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "SymptomTracker",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Neuer Eintrag")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (entryList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Noch keine Einträge.\nTippen Sie auf + um einen neuen Symptom-Eintrag zu erstellen.",
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
                item {
                    Text(
                        "Letzte Einträge",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                    )
                }
                items(entryList) { entry ->
                    EntryCard(
                        entry = entry,
                        onDeleteClick = { entryToDelete = entry }
                    )
                }
            }
        }
    }
}

@Composable
private fun EntryCard(
    entry: SymptomEntry,
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header: Body Part and Score
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
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = labelColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (entry.painTypeOther?.isNotBlank() == true) entry.painTypeOther else entry.painType,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = contentColor.copy(alpha = 0.7f)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${entry.severity}",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        ),
                        color = contentColor
                    )
                    Text(
                        text = "/ 10",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = labelColor
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                thickness = 1.dp,
                color = contentColor.copy(alpha = 0.1f)
            )

            // Grid Section
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    AttributeItem(
                        icon = Icons.Default.Schedule,
                        label = "ZEITPUNKT",
                        value = "$displayDate | $displayTime",
                        contentColor = contentColor,
                        labelColor = labelColor
                    )
                    if (entry.trigger.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        AttributeItem(
                            icon = Icons.Default.Warning,
                            label = "AUSLÖSER",
                            value = entry.trigger,
                            contentColor = contentColor,
                            labelColor = labelColor
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    if (entry.heartRate != null || !entry.bloodPressure.isNullOrBlank()) {
                        val vitals = buildString {
                            if (entry.heartRate != null) append("${entry.heartRate} bpm")
                            if (entry.heartRate != null && !entry.bloodPressure.isNullOrBlank()) append("\n")
                            if (!entry.bloodPressure.isNullOrBlank()) append(entry.bloodPressure)
                        }
                        AttributeItem(
                            icon = Icons.Default.Favorite,
                            label = "VITALS",
                            value = vitals,
                            contentColor = contentColor,
                            labelColor = labelColor
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    if (entry.medication.isNotBlank()) {
                        val medValue = if (entry.dosage?.isNotBlank() == true) "${entry.medication}\n${entry.dosage}" else entry.medication
                        AttributeItem(
                            icon = Icons.Default.MedicalServices,
                            label = "MEDIKATION",
                            value = medValue,
                            contentColor = contentColor,
                            labelColor = labelColor
                        )
                    }
                }
            }

            // Notes Section
            if (entry.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notes,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = labelColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "NOTIZ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = labelColor
                        )
                    }
                    Text(
                        text = entry.note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Footer: Delete Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eintrag löschen",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
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
                .size(16.dp)
                .padding(top = 2.dp),
            tint = labelColor
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = labelColor
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@Composable
private fun getIntensityColor(severity: Int): Color {
    val startColor = Color(0xFFC8E6C9) // Light Green
    val midColor = Color(0xFFFFF9C4)   // Light Yellow
    val endColor = Color(0xFFFFCDD2)   // Light Red

    return if (severity <= 5) {
        val fraction = (severity - 1) / 4f
        lerp(startColor, midColor, fraction.coerceIn(0f, 1f))
    } else {
        val fraction = (severity - 5) / 5f
        lerp(midColor, endColor, fraction.coerceIn(0f, 1f))
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val sampleEntries = listOf(
        SymptomEntry(
            id = 1,
            severity = 7,
            painType = "Kopfschmerz",
            painTypeOther = null,
            dateTimeMillis = System.currentTimeMillis(),
            medication = "Ibuprofen",
            dosage = "400mg",
            trigger = "Stress",
            note = "Starker Druck im Schläfenbereich.",
            bodyPart = "Kopf",
            heartRate = 85,
            bloodPressure = "120/80"
        ),
        SymptomEntry(
            id = 2,
            severity = 4,
            painType = "Sonstiges",
            painTypeOther = "Rückenschmerzen",
            dateTimeMillis = System.currentTimeMillis() - 86400000,
            medication = "",
            trigger = "Langes Sitzen",
            note = "Leichtes Ziehen im unteren Rücken.",
            bodyPart = "Rücken"
        )
    )
    SymptomTrackerTheme {
        HomeScreenContent(
            entryList = sampleEntries,
            onAddClick = {},
            onDeleteClick = {}
        )
    }
}
