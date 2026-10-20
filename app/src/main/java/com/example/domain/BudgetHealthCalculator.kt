package com.example.domain

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class BudgetHealth(
    val daysInMonth: Int,
    val dayOfMonth: Int,
    val daysRemaining: Int,
    val monthProgressPercent: Float,
    val budgetSpentPercent: Float,
    val dailyBudgetRemainingCents: Long,
    val paceStatus: PaceStatus,
    val headlineMessage: String,
    val detailMessage: String
)

enum class PaceStatus {
    UNDER_PACING,
    ON_TRACK,
    WARNING_PACE,
    DANGER_PACE
}

object BudgetHealthCalculator {

    fun calculate(
        monthYear: String, // "YYYY-MM"
        monthlyIncomeCents: Long,
        totalExpensesCents: Long
    ): BudgetHealth {
        val calendar = Calendar.getInstance()

        // Check if viewing current month or a past/future month
        val currentMonthYear = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
        val isCurrentMonth = monthYear == currentMonthYear

        val targetCal = Calendar.getInstance()
        try {
            val parts = monthYear.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt() - 1
            targetCal.set(Calendar.YEAR, year)
            targetCal.set(Calendar.MONTH, month)
            targetCal.set(Calendar.DAY_OF_MONTH, 1)
        } catch (e: Exception) {
            // fallback to current
        }

        val daysInMonth = targetCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val dayOfMonth = if (isCurrentMonth) {
            calendar.get(Calendar.DAY_OF_MONTH).coerceIn(1, daysInMonth)
        } else {
            daysInMonth
        }

        val daysRemaining = (daysInMonth - dayOfMonth + 1).coerceAtLeast(1)
        val monthProgressPercent = (dayOfMonth.toFloat() / daysInMonth.toFloat()) * 100f

        val budgetSpentPercent = if (monthlyIncomeCents > 0) {
            (totalExpensesCents.toFloat() / monthlyIncomeCents.toFloat()) * 100f
        } else {
            0f
        }

        val remainingCents = (monthlyIncomeCents - totalExpensesCents).coerceAtLeast(0L)
        val dailyBudgetRemainingCents = remainingCents / daysRemaining

        val diff = budgetSpentPercent - monthProgressPercent

        val paceStatus = when {
            budgetSpentPercent >= 100f -> PaceStatus.DANGER_PACE
            diff > 20f -> PaceStatus.DANGER_PACE
            diff > 8f -> PaceStatus.WARNING_PACE
            diff < -15f -> PaceStatus.UNDER_PACING
            else -> PaceStatus.ON_TRACK
        }

        val headlineMessage = when (paceStatus) {
            PaceStatus.DANGER_PACE -> if (budgetSpentPercent >= 100f) "Budget Exceeded!" else "Pacing Far Over Budget"
            PaceStatus.WARNING_PACE -> "Pacing Slightly High"
            PaceStatus.UNDER_PACING -> "Excellent Budget Discipline"
            PaceStatus.ON_TRACK -> "On Track & Balanced"
        }

        val detailMessage = if (isCurrentMonth) {
            "${String.format(Locale.US, "%.0f", budgetSpentPercent)}% spent with $daysRemaining days left (day $dayOfMonth of $daysInMonth)."
        } else {
            "${String.format(Locale.US, "%.0f", budgetSpentPercent)}% of budget utilized during this period."
        }

        return BudgetHealth(
            daysInMonth = daysInMonth,
            dayOfMonth = dayOfMonth,
            daysRemaining = daysRemaining,
            monthProgressPercent = monthProgressPercent,
            budgetSpentPercent = budgetSpentPercent,
            dailyBudgetRemainingCents = dailyBudgetRemainingCents,
            paceStatus = paceStatus,
            headlineMessage = headlineMessage,
            detailMessage = detailMessage
        )
    }
}
