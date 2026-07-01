package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SleepLog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepListScreen(
    viewModel: SleepViewModel,
    onAddLogClick: () -> Unit,
    onEditLogClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val logs by viewModel.allLogs.collectAsState()
    var showDeleteConfirmDialog by remember { mutableStateOf<SleepLog?>(null) }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        "SleepSanity",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.resetForm()
                    onAddLogClick()
                },
                icon = { Icon(Icons.Default.Add, "Tambah Entry") },
                text = { Text("Catat Tidur") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("add_log_fab")
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + 80.dp // extra padding for FAB
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (logs.isNotEmpty()) {
                // Dashboard Section (Static Calculations & Static Rules Warnings)
                item {
                    InsightDashboard(logs = logs)
                }

                item {
                    Text(
                        text = "Riwayat Tidur & Mood",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                items(items = logs, key = { it.id }) { log ->
                    SleepLogCard(
                        log = log,
                        onEditClick = { onEditLogClick(log.id) },
                        onDeleteClick = { showDeleteConfirmDialog = log }
                    )
                }
            } else {
                item {
                    EmptyStateView(onAddClick = {
                        viewModel.resetForm()
                        onAddLogClick()
                    })
                }
            }
        }
    }

    // Delete Confirmation Dialog
    showDeleteConfirmDialog?.let { log ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Hapus Catatan Tidur?") },
            text = { Text("Apakah Anda yakin ingin menghapus catatan tidur ini? Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteLog(log)
                        showDeleteConfirmDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun InsightDashboard(logs: List<SleepLog>) {
    // Filter last 7 days of entries for short-term warnings
    val now = System.currentTimeMillis()
    val sevenDaysAgoMillis = now - 7 * 24 * 3600 * 1000
    val last7DaysLogs = logs.filter { it.createdAt >= sevenDaysAgoMillis }

    // Computations
    val totalRecords = logs.size
    
    val allDurations = logs.map { DateTimeHelper.getDurationHours(it.sleepTime, it.wakeTime) }
    val avgSleepDuration = if (allDurations.isNotEmpty()) allDurations.average() else 0.0

    val recentDurations = last7DaysLogs.map { DateTimeHelper.getDurationHours(it.sleepTime, it.wakeTime) }
    val avgRecentSleepDuration = if (recentDurations.isNotEmpty()) recentDurations.average() else 0.0

    val avgMood = if (logs.isNotEmpty()) logs.map { it.mood }.average() else 0.0
    val avgRecentMood = if (last7DaysLogs.isNotEmpty()) last7DaysLogs.map { it.mood }.average() else 0.0

    val avgQuality = if (logs.isNotEmpty()) logs.map { it.sleepQuality }.average() else 0.0

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("insight_chart_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Statistik Kesehatan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Berdasarkan analisis log tidur lokal Anda",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stat grid
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatCard(
                    title = "Rata-rata Tidur",
                    value = String.format("%.1f jam", avgSleepDuration),
                    subtext = "Semua riwayat",
                    icon = Icons.Default.Bedtime,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Rata-rata Mood",
                    value = String.format("%.1f/5", avgMood),
                    subtext = getMoodEmoji(avgMood.toInt().coerceIn(1, 5)),
                    icon = Icons.Default.SentimentSatisfiedAlt,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic rule-based warnings/badges (statis, non-AI)
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Warning 1: Sleep < 6h in last 7 days
                if (last7DaysLogs.isNotEmpty() && avgRecentSleepDuration < 6.0) {
                    WarningBadge(
                        text = "Durasi tidur rendah (${String.format("%.1f jam", avgRecentSleepDuration)} • 7 hari terakhir)",
                        description = "Tidur kurang dari 6 jam dapat memengaruhi konsentrasi dan imunitas tubuh Anda.",
                        isWarning = true
                    )
                }

                // Badge 1: Healthy sleep 7-9h
                if (last7DaysLogs.isNotEmpty() && avgRecentSleepDuration in 7.0..9.0) {
                    WarningBadge(
                        text = "Pola Tidur Sehat (${String.format("%.1f jam", avgRecentSleepDuration)})",
                        description = "Rata-rata durasi tidur Anda sangat ideal! Pertahankan konsistensi ini.",
                        isWarning = false
                    )
                }

                // Warning 2: Mood < 3.0 in last 7 days
                if (last7DaysLogs.isNotEmpty() && avgRecentMood < 3.0) {
                    WarningBadge(
                        text = "Kondisi Mood Rendah (${String.format("%.1f/5", avgRecentMood)})",
                        description = "Mood rata-rata Anda belakangan ini agak rendah. Prioritaskan relaksasi diri.",
                        isWarning = true
                    )
                }

                // Badge 2: Stable & Fit (Avg quality >= 4.0 and avg mood >= 4.0)
                if (avgQuality >= 4.0 && avgMood >= 4.0) {
                    WarningBadge(
                        text = "Kualitas Tidur & Mood Prima",
                        description = "Sinergi luar biasa antara tidur berkualitas dan kondisi emosional yang bugar.",
                        isWarning = false,
                        colorOverride = Color(0xFF6750A4) // elegant purple
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Correlation Chart (Last 7 logs)
            if (logs.size >= 2) {
                Text(
                    text = "Korelasi: Durasi Tidur vs Mood",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Menampilkan 7 catatan tidur terakhir",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                CorrelationChart(lastLogs = logs.take(7).reversed())
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Butuh minimal 2 catatan untuk menganalisis grafik korelasi tidur & mood.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WarningBadge(
    text: String,
    description: String,
    isWarning: Boolean,
    colorOverride: Color? = null
) {
    val containerColor = colorOverride?.copy(alpha = 0.15f)
        ?: if (isWarning) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
           else Color(0xFFE8F5E9) // soft teal/green
           
    val contentColor = colorOverride
        ?: if (isWarning) MaterialTheme.colorScheme.error
           else Color(0xFF2E7D32) // forest green

    val icon = if (isWarning) Icons.Default.Warning else Icons.Default.CheckCircle

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier
                    .size(20.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun CorrelationChart(lastLogs: List<SleepLog>) {
    val barColor = MaterialTheme.colorScheme.primary
    val linePointColor = MaterialTheme.colorScheme.secondary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        val width = size.width
        val height = size.height

        val maxDuration = 12.0 // scale up to 12 hours max
        val maxMood = 5.0

        val sizeCount = lastLogs.size
        val colWidth = width / sizeCount
        val barWidth = (colWidth * 0.4f).coerceAtMost(32.dp.toPx())

        // Grid lines helper
        for (i in 1..3) {
            val y = height * (i / 4f)
            drawLine(
                color = labelColor.copy(alpha = 0.15f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        val moodPoints = mutableListOf<Offset>()

        lastLogs.forEachIndexed { index, log ->
            val duration = DateTimeHelper.getDurationHours(log.sleepTime, log.wakeTime)
            val mood = log.mood.toDouble()

            // Calculate coordinates
            val colCenterX = (index * colWidth) + (colWidth / 2f)
            
            // 1. Draw sleep duration bar
            val barHeight = ((duration / maxDuration) * height).toFloat().coerceAtMost(height)
            val barTopY = height - barHeight
            
            drawRoundRect(
                color = barColor.copy(alpha = 0.75f),
                topLeft = Offset(colCenterX - (barWidth / 2f), barTopY),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            // 2. Plot mood score (connected line)
            val moodY = (height - ((mood / maxMood) * height).toFloat()).coerceAtMost(height)
            val moodPoint = Offset(colCenterX, moodY)
            moodPoints.add(moodPoint)

            // Draw individual mood point
            drawCircle(
                color = linePointColor,
                radius = 5.dp.toPx(),
                center = moodPoint
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = moodPoint
            )
        }

        // Connect mood points with an elegant line
        if (moodPoints.size > 1) {
            for (i in 0 until moodPoints.size - 1) {
                drawLine(
                    color = linePointColor,
                    start = moodPoints[i],
                    end = moodPoints[i + 1],
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
    }

    // Legend
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp, 8.dp)
                .background(barColor.copy(alpha = 0.75f), RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text("Durasi Tidur (Bar)", style = MaterialTheme.typography.labelSmall, color = labelColor)

        Spacer(modifier = Modifier.width(20.dp))

        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(linePointColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text("Mood Harian (Garis)", style = MaterialTheme.typography.labelSmall, color = labelColor)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepLogCard(
    log: SleepLog,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val duration = DateTimeHelper.formatDuration(log.sleepTime, log.wakeTime)
    val sleepDateStr = DateTimeHelper.formatDate(log.sleepTime)
    val sleepTimeStr = DateTimeHelper.formatTime(log.sleepTime)
    val wakeTimeStr = DateTimeHelper.formatTime(log.wakeTime)

    val moodColor = when (log.mood) {
        5 -> Color(0xFF2E7D32) // Forest Green
        4 -> Color(0xFF4CAF50) // Bright Green
        3 -> Color(0xFFFBC02D) // Soft Amber
        2 -> Color(0xFFF57C00) // Orange
        else -> Color(0xFFD32F2F) // Red
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Sleep Date
                Text(
                    text = sleepDateStr,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Mood circle badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = getMoodEmoji(log.mood),
                        fontSize = 18.sp
                    )
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(moodColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Timing Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Sleep Time Info
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Bedtime, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Tidur", 
                            style = MaterialTheme.typography.labelMedium, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = sleepTimeStr,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Sleep Duration representation
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1.2f)
                ) {
                    Text(
                        text = duration,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Kualitas: ${log.sleepQuality}/5",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Wake Time Info
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Bangun", 
                            style = MaterialTheme.typography.labelMedium, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.WbSunny, 
                            contentDescription = null, 
                            tint = Color(0xFFFBC02D),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = wakeTimeStr,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Note or Actions expandable area
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    if (!log.note.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                Icons.AutoMirrored.Filled.Notes,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = log.note,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    } else {
                        Text(
                            text = "Tidak ada catatan.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Action buttons
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier.testTag("edit_button_${log.id}")
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit Catatan",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.testTag("delete_button_${log.id}")
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Hapus Catatan",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(onAddClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.NightsStay,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Belum Ada Catatan Tidur",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Mulai pantau kesehatan Anda dengan mencatatkan durasi tidur dan kondisi mood Anda sehari-hari.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Buat Catatan Pertama")
            }
        }
    }
}

fun getMoodEmoji(mood: Int): String {
    return when (mood) {
        5 -> "😄" // Sangat Baik
        4 -> "🙂" // Baik
        3 -> "😐" // Biasa Saja
        2 -> "😕" // Buruk
        1 -> "😢" // Sangat Buruk
        else -> "😐"
    }
}
