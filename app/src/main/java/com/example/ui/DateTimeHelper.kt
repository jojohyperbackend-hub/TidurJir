package com.example.ui

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeHelper {
    private val idLocale = Locale.forLanguageTag("id-ID")
    private val dateTimeFormatter = SimpleDateFormat("EEEE, d MMM yyyy • HH:mm", idLocale)
    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", idLocale)
    private val timeFormatter = SimpleDateFormat("HH:mm", idLocale)

    fun formatDateTime(epochMillis: Long): String {
        return dateTimeFormatter.format(Date(epochMillis))
    }

    fun formatDate(epochMillis: Long): String {
        return dateFormatter.format(Date(epochMillis))
    }

    fun formatTime(epochMillis: Long): String {
        return timeFormatter.format(Date(epochMillis))
    }

    fun formatDuration(sleepMillis: Long, wakeMillis: Long): String {
        val diffMillis = wakeMillis - sleepMillis
        if (diffMillis <= 0) return "0 jam"
        
        val totalMinutes = diffMillis / (60 * 1000)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        
        return if (minutes > 0) {
            "${hours}j ${minutes}m"
        } else {
            "${hours}j"
        }
    }

    fun getDurationHours(sleepMillis: Long, wakeMillis: Long): Double {
        val diffMillis = wakeMillis - sleepMillis
        if (diffMillis <= 0) return 0.0
        return diffMillis.toDouble() / (1000 * 60 * 60)
    }

    // Combined builder for date & time dialog results
    fun combineDateAndTime(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, day)
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
