package com.symptomtracker.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Letzte Einträge",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
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
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMAN)
    val displayDate = dateFormat.format(Date(entry.dateTimeMillis))

    val backgroundColor = getIntensityColor(entry.severity)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Schweregrad: ${entry.severity}/10",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black.copy(alpha = 0.8f)
                    )
                    Text(
                        text = displayDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black.copy(alpha = 0.6f)
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eintrag löschen",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            if (!entry.bodyPart.isNullOrBlank()) {
                Text(
                    text = "Körperstelle: ${entry.bodyPart}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black.copy(alpha = 0.7f)
                )
            }
            Text(
                text = "Art: ${if (entry.painTypeOther?.isNotBlank() == true) entry.painTypeOther else entry.painType}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 2.dp)
            )
            if (entry.heartRate != null || !entry.bloodPressure.isNullOrBlank()) {
                val vitals = buildString {
                    if (entry.heartRate != null) append("Puls: ${entry.heartRate} ")
                    if (!entry.bloodPressure.isNullOrBlank()) append("Blutdruck: ${entry.bloodPressure}")
                }
                Text(
                    text = vitals,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            if (entry.medication.isNotBlank()) {
                Text(
                    text = "Medikation: ${entry.medication}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black.copy(alpha = 0.6f)
                )
            }
            if (entry.note.isNotBlank()) {
                Text(
                    text = entry.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black.copy(alpha = 0.8f),
                    maxLines = 2,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
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
            medication = "Ibuprofen 400mg",
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
