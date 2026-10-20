package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object TransactionDateFormatter {

    private val timeFormat = SimpleDateFormat("h:mm a", Locale.US)
    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
    private val shortDateFormat = SimpleDateFormat("MMM d", Locale.US)
    private val dayHeaderFormat = SimpleDateFormat("EEEE, MMM d", Locale.US)

    /**
     * Formats timestamp into a clean, human-friendly string containing both the date and exact timing:
     * e.g., "Today, 11:45 AM", "Yesterday, 4:20 PM", "Oct 14 • 11:45 AM", or "Oct 14, 2025 • 11:45 AM"
     */
    fun formatDateTime(timestampMillis: Long): String {
        if (timestampMillis <= 0L) return ""
        val transactionCal = Calendar.getInstance().apply { timeInMillis = timestampMillis }
        val nowCal = Calendar.getInstance()

        val timeStr = timeFormat.format(Date(timestampMillis))

        val isToday = isSameDay(transactionCal, nowCal)
        if (isToday) {
            return "Today, $timeStr"
        }

        val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val isYesterday = isSameDay(transactionCal, yesterdayCal)
        if (isYesterday) {
            return "Yesterday, $timeStr"
        }

        val isSameYear = transactionCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)
        val dateStr = if (isSameYear) {
            shortDateFormat.format(Date(timestampMillis))
        } else {
            dateFormat.format(Date(timestampMillis))
        }

        return "$dateStr • $timeStr"
    }

    /**
     * Formats into a full verbose date and time, e.g. "Oct 14, 2026 at 11:45 AM"
     */
    fun formatFullDateTime(timestampMillis: Long): String {
        if (timestampMillis <= 0L) return ""
        val date = Date(timestampMillis)
        return "${dateFormat.format(date)} at ${timeFormat.format(date)}"
    }

    /**
     * Formats the date portion only, e.g. "Oct 14, 2026"
     */
    fun formatDateOnly(timestampMillis: Long): String {
        if (timestampMillis <= 0L) return ""
        return dateFormat.format(Date(timestampMillis))
    }

    /**
     * Formats the time portion only, e.g. "11:45 AM"
     */
    fun formatTimeOnly(timestampMillis: Long): String {
        if (timestampMillis <= 0L) return ""
        return timeFormat.format(Date(timestampMillis))
    }

    /**
     * Header string for day grouping, e.g., "Today", "Yesterday", or "Wednesday, Oct 14"
     */
    fun formatDayHeader(timestampMillis: Long): String {
        if (timestampMillis <= 0L) return ""
        val transactionCal = Calendar.getInstance().apply { timeInMillis = timestampMillis }
        val nowCal = Calendar.getInstance()

        if (isSameDay(transactionCal, nowCal)) {
            return "Today"
        }
        val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        if (isSameDay(transactionCal, yesterdayCal)) {
            return "Yesterday"
        }
        return dayHeaderFormat.format(Date(timestampMillis))
    }

    fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
