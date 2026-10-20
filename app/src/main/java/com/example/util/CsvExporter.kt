package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.Expense
import com.example.domain.CurrencyFormatter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    fun generateCsvContent(expenses: List<Expense>, currencySymbol: String = "$"): String {
        val sb = StringBuilder()
        sb.append("ID,Month,Date,Description,Category,Amount,Note,IsRecurring\n")

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        for (expense in expenses) {
            val dateStr = dateFormat.format(Date(expense.createdAt))
            val amountFormatted = CurrencyFormatter.centsToDecimalString(expense.amountCents)
            val escapedDesc = escapeCsvField(expense.description)
            val escapedNote = escapeCsvField(expense.note)

            sb.append("${expense.id},")
            sb.append("${expense.monthYear},")
            sb.append("\"$dateStr\",")
            sb.append("\"$escapedDesc\",")
            sb.append("\"${expense.category}\",")
            sb.append("$amountFormatted,")
            sb.append("\"$escapedNote\",")
            sb.append("${expense.isRecurring}\n")
        }
        return sb.toString()
    }

    private fun escapeCsvField(field: String): String {
        return field.replace("\"", "\"\"")
    }

    fun exportAndShareCsv(context: Context, expenses: List<Expense>, currencySymbol: String = "$") {
        try {
            val csvContent = generateCsvContent(expenses, currencySymbol)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "expenses_export_$timeStamp.csv"

            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val file = File(exportDir, fileName)
            FileOutputStream(file).use { out ->
                out.write(csvContent.toByteArray(Charsets.UTF_8))
            }

            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, "Budget Calculator - Expense Export")
                putExtra(Intent.EXTRA_TEXT, "Attached is the expense report export from Budget Calculator.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Export Expenses CSV"))
        } catch (e: Exception) {
            // Fallback plain text share if FileProvider fails
            val plainText = generateCsvContent(expenses, currencySymbol)
            val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, plainText)
                putExtra(Intent.EXTRA_SUBJECT, "Budget Calculator - Expenses Export")
            }
            context.startActivity(Intent.createChooser(fallbackIntent, "Export Expenses"))
        }
    }
}
