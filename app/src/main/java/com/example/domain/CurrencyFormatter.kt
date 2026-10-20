package com.example.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyFormatter {

    /**
     * Formats Long cents into formatted currency string.
     * e.g., 300000L with "$" -> "$3,000.00"
     */
    fun formatCents(cents: Long, symbol: String = "$"): String {
        val isNegative = cents < 0
        val absCents = kotlin.math.abs(cents)
        val units = absCents / 100
        val remainder = absCents % 100

        val symbols = DecimalFormatSymbols(Locale.US)
        // Standard comma grouped format for crisp financial legibility
        val formatter = DecimalFormat("#,##0", symbols)
        val formattedUnits = formatter.format(units)
        val formattedRemainder = String.format(Locale.US, "%02d", remainder)

        val spacing = if (symbol.startsWith("Rs") || symbol.length > 2) " " else ""
        val prefix = if (isNegative) "-$symbol$spacing" else "$symbol$spacing"
        return "$prefix$formattedUnits.$formattedRemainder"
    }

    /**
     * Formats Long cents into pure decimal number string without currency symbol.
     * e.g., 14550L -> "145.50"
     */
    fun centsToDecimalString(cents: Long): String {
        val dollars = cents / 100
        val remainder = kotlin.math.abs(cents % 100)
        return "$dollars.${String.format(Locale.US, "%02d", remainder)}"
    }

    /**
     * Parses a string representation (e.g. "145.50" or "145" or "₹500") to minor units (Long cents).
     * Returns null if string is invalid.
     */
    fun parseToCents(input: String): Long? {
        val cleaned = input.trim()
            .replace(",", "")
            .replace("₹", "")
            .replace("Rs.", "")
            .replace("Rs", "")
            .replace("INR", "", ignoreCase = true)
            .replace("$", "")
            .replace("€", "")
            .replace("£", "")
            .replace("¥", "")
            .replace("C$", "")
            .replace("A$", "")
            .trim()
        if (cleaned.isEmpty()) return null
        return try {
            val bd = BigDecimal(cleaned).setScale(2, RoundingMode.HALF_UP)
            bd.multiply(BigDecimal(100)).toLong()
        } catch (e: Exception) {
            null
        }
    }
}
