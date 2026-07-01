package com.example.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepFormScreen(
    viewModel: SleepViewModel,
    logIdToEdit: Long?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val formState by viewModel.formState.collectAsState()

    // Load edit data if logIdToEdit is provided
    LaunchedEffect(logIdToEdit) {
        if (logIdToEdit != null && logIdToEdit > 0) {
            viewModel.loadLogForEdit(logIdToEdit)
        } else {
            viewModel.resetForm()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (logIdToEdit != null) "Edit Catatan Tidur" else "Catat Pola Tidur",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Form Error Alert
            if (formState.error != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formState.error ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Sleep & Wake Timing Cards
            Text(
                text = "Waktu Tidur & Bangun",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            DateTimePickerCard(
                label = "Waktu Tidur",
                timestamp = formState.sleepTime,
                icon = Icons.Default.Bedtime,
                iconColor = MaterialTheme.colorScheme.primary,
                onTimestampSelected = { selectedTime ->
                    viewModel.updateSleepTime(selectedTime)
                }
            )

            DateTimePickerCard(
                label = "Waktu Bangun",
                timestamp = formState.wakeTime,
                icon = Icons.Default.WbSunny,
                iconColor = Color(0xFFFBC02D),
                onTimestampSelected = { selectedTime ->
                    viewModel.updateWakeTime(selectedTime)
                }
            )

            // Calculated sleep duration overview
            if (formState.wakeTime > formState.sleepTime) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Schedule, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Durasi tidur: ${DateTimeHelper.formatDuration(formState.sleepTime, formState.wakeTime)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Sleep Quality Rating Row
            Text(
                text = "Kualitas Tidur",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Seberapa nyenyak tidur Anda semalam?",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                for (rating in 1..5) {
                    val isActive = rating <= formState.sleepQuality
                    Icon(
                        imageVector = if (isActive) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Kualitas $rating dari 5",
                        tint = if (isActive) Color(0xFFFBC02D) else MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { viewModel.updateSleepQuality(rating) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Mood Rating Row
            Text(
                text = "Mood Setelah Bangun",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Bagaimana perasaan atau kondisi mental Anda saat bangun?",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                for (rating in 1..5) {
                    val isActive = rating == formState.mood
                    val emoji = getMoodEmoji(rating)
                    val label = when (rating) {
                        1 -> "Buruk"
                        2 -> "Kurang"
                        3 -> "Biasa"
                        4 -> "Baik"
                        5 -> "Sangat Baik"
                        else -> ""
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.updateMood(rating) }
                            .background(
                                if (isActive) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                            )
                            .padding(8.dp)
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Notes field
            Text(
                text = "Catatan Tambahan (Opsional)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = formState.note,
                onValueChange = { viewModel.updateNote(it) },
                placeholder = { Text("Tuliskan catatan singkat mengenai kondisi tidur Anda (misal: kafein malam, mimpi buruk, dsb.)") },
                maxLines = 5,
                shape = RoundedCornerShape(16.dp),
                supportingText = {
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("${formState.note.length} / 280")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Save submit button
            Button(
                onClick = {
                    viewModel.saveLog {
                        onNavigateBack()
                    }
                },
                enabled = !formState.isSaving,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_log_button")
            ) {
                if (formState.isSaving) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (logIdToEdit != null) "Perbarui Catatan" else "Simpan Catatan",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
fun DateTimePickerCard(
    label: String,
    timestamp: Long,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    onTimestampSelected: (Long) -> Unit
) {
    val context = LocalContext.current
    val calendar = remember(timestamp) {
        Calendar.getInstance().apply { timeInMillis = timestamp }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // Show DatePickerDialog first
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        // After selecting Date, show TimePickerDialog
                        TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                val combinedTimestamp = DateTimeHelper.combineDateAndTime(
                                    year, month, dayOfMonth, hourOfDay, minute
                                )
                                onTimestampSelected(combinedTimestamp)
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true // 24 hours format
                        ).show()
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = DateTimeHelper.formatDateTime(timestamp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = "Pilih Tanggal & Waktu",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
