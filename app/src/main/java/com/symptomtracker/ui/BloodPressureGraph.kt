package com.symptomtracker.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.symptomtracker.data.BloodPressureEntry
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodPressureGraph(
    entries: List<BloodPressureEntry>,
    modifier: Modifier = Modifier
) {
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    calendar.timeInMillis = selectedDateMillis
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val startOfDay = calendar.timeInMillis
    
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    val endOfDay = calendar.timeInMillis

    val filteredEntries = entries.filter { it.dateTimeMillis in startOfDay..endOfDay }
        .sortedBy { it.dateTimeMillis }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDateMillis = datePickerState.selectedDateMillis ?: selectedDateMillis
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Abbrechen") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Date Selection Header
        Card(
            modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Ausgewählter Tag", style = MaterialTheme.typography.labelMedium)
                    Text(
                        SimpleDateFormat("EEEE, dd. MMMM yyyy", Locale.GERMAN).format(Date(selectedDateMillis)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(Icons.Default.DateRange, contentDescription = "Datum wählen")
            }
        }

        if (filteredEntries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Keine Messungen für diesen Tag vorhanden",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val avgSys = filteredEntries.mapNotNull { it.systolic }.let { if (it.isEmpty()) 0 else it.average().toInt() }
                    val avgDia = filteredEntries.mapNotNull { it.diastolic }.let { if (it.isEmpty()) 0 else it.average().toInt() }
                    val avgPulse = filteredEntries.mapNotNull { it.pulse }.let { if (it.isEmpty()) 0 else it.average().toInt() }

                    Text(
                        "Durchschnitt (${filteredEntries.size} Messungen)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatBox("Systole Ø", "$avgSys", "mmHg", Color(0xFFE57373))
                        StatBox("Diastole Ø", "$avgDia", "mmHg", Color(0xFF64B5F6))
                        StatBox("Puls Ø", "$avgPulse", "bpm", Color(0xFF81C784))
                    }
                }
            }

            // Blood Pressure Chart
            ChartCard(
                title = "Blutdruck (Tag)",
                entries = filteredEntries,
                minY = 40f,
                maxY = 200f,
                lines = listOf(
                    ChartLineData("Sys", Color(0xFFE57373)) { it.systolic?.toFloat() ?: 0f },
                    ChartLineData("Dia", Color(0xFF64B5F6)) { it.diastolic?.toFloat() ?: 0f }
                )
            )

            // Pulse Chart
            ChartCard(
                title = "Puls (Tag)",
                entries = filteredEntries,
                minY = 40f,
                maxY = 140f,
                lines = listOf(
                    ChartLineData("Puls", Color(0xFF81C784)) { it.pulse?.toFloat() ?: 0f }
                )
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun StatBox(label: String, value: String, unit: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.Black)
        Text(unit, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

data class ChartLineData(
    val name: String,
    val color: Color,
    val valueProvider: (BloodPressureEntry) -> Float
)

@Composable
private fun ChartCard(
    title: String,
    entries: List<BloodPressureEntry>,
    minY: Float,
    maxY: Float,
    lines: List<ChartLineData>
) {
    Card(
        modifier = Modifier.fillMaxWidth().height(280.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Row {
                    lines.forEach { line ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 12.dp)) {
                            Box(Modifier.size(8.dp).background(line.color, RoundedCornerShape(4.dp)))
                            Spacer(Modifier.width(4.dp))
                            Text(line.name, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(modifier = Modifier.fillMaxSize()) {
                val yRange = maxY - minY
                
                // Y-Axis Labels
                Column(
                    modifier = Modifier.fillMaxHeight().padding(bottom = 24.dp, top = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${maxY.toInt()}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${(minY + yRange / 2).toInt()}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${minY.toInt()}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 32.dp, bottom = 24.dp, top = 8.dp, end = 8.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val xStep = if (entries.size > 1) width / (entries.size - 1) else width / 2

                    drawChartGrid(width, height)

                    lines.forEach { line ->
                        val path = Path()
                        entries.forEachIndexed { index, entry ->
                            val value = line.valueProvider(entry).coerceIn(minY, maxY)
                            val x = if (entries.size > 1) index * xStep else width / 2
                            val y = height - ((value - minY) / yRange * height)

                            if (index == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                            
                            drawCircle(line.color, radius = 3.dp.toPx(), center = Offset(x, y))
                        }

                        drawPath(
                            path = path,
                            color = line.color,
                            style = Stroke(width = 2.5.dp.toPx())
                        )
                    }
                }
                
                // X-Axis Labels (Time)
                XAxisLabels(entries)
            }
        }
    }
}

@Composable
private fun BoxScope.XAxisLabels(entries: List<BloodPressureEntry>) {
    Row(
        modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart).padding(start = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.GERMAN)
        if (entries.isNotEmpty()) {
            Text(timeFormat.format(Date(entries.first().dateTimeMillis)), fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (entries.size > 2) {
                Text(timeFormat.format(Date(entries[entries.size/2].dateTimeMillis)), fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(timeFormat.format(Date(entries.last().dateTimeMillis)), fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun DrawScope.drawChartGrid(width: Float, height: Float) {
    val gridColor = Color.LightGray.copy(alpha = 0.2f)
    drawLine(gridColor, Offset(0f, 0f), Offset(width, 0f), strokeWidth = 1.dp.toPx())
    drawLine(gridColor, Offset(0f, height / 2), Offset(width, height / 2), strokeWidth = 1.dp.toPx())
    drawLine(gridColor, Offset(0f, height), Offset(width, height), strokeWidth = 1.dp.toPx())
}
