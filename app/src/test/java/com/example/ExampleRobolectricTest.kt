package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Expense
import com.example.domain.BudgetHealthCalculator
import com.example.domain.CurrencyFormatter
import com.example.domain.MathEvaluator
import com.example.domain.PaceStatus
import com.example.util.CsvExporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Budget Calculator", appName)
  }

  @Test
  fun `evaluate math expressions correctly including percentages and ans`() {
    // Basic operations
    val result1 = MathEvaluator.evaluate("200 + 50 * 2")
    assertTrue(result1.isSuccess)
    assertEquals(300.0, result1.getOrThrow(), 0.001)

    // Parentheses
    val result2 = MathEvaluator.evaluate("(1200 - 200) / 5")
    assertTrue(result2.isSuccess)
    assertEquals(200.0, result2.getOrThrow(), 0.001)

    // Percentage: 15% * 2000 = 300
    val result3 = MathEvaluator.evaluate("15% * 2000")
    assertTrue(result3.isSuccess)
    assertEquals(300.0, result3.getOrThrow(), 0.001)

    // Ans memory chaining
    val result4 = MathEvaluator.evaluate("ans + 50", ansValue = 300.0)
    assertTrue(result4.isSuccess)
    assertEquals(350.0, result4.getOrThrow(), 0.001)

    val formatted = MathEvaluator.formatResult(300.0)
    assertEquals("300", formatted)
  }

  @Test
  fun `verify currency formatting with minor units`() {
    val formatted = CurrencyFormatter.formatCents(300000L, "$")
    assertEquals("$3,000.00", formatted)

    val decimalStr = CurrencyFormatter.centsToDecimalString(14550L)
    assertEquals("145.50", decimalStr)

    val parsed = CurrencyFormatter.parseToCents("1,250.75")
    assertEquals(125075L, parsed)
  }

  @Test
  fun `verify spending distribution and csv export`() {
    val expenses = listOf(
      Expense(
        id = 1L,
        monthYear = "2026-03",
        description = "Rent",
        amountCents = 120000L,
        category = "Housing"
      ),
      Expense(
        id = 2L,
        monthYear = "2026-03",
        description = "Groceries",
        amountCents = 40000L,
        category = "Food"
      )
    )
    val totalCents = expenses.sumOf { it.amountCents }
    assertEquals(160000L, totalCents)

    val housingPct = (120000.0 / totalCents.toDouble()) * 100.0
    assertEquals(75.0, housingPct, 0.001)

    val csv = CsvExporter.generateCsvContent(expenses, "$")
    assertTrue(csv.contains("Rent"))
    assertTrue(csv.contains("1200.00"))
    assertTrue(csv.contains("Housing"))
  }

  @Test
  fun `verify budget health pacing calculation`() {
    val health = BudgetHealthCalculator.calculate(
      monthYear = "2026-03",
      monthlyIncomeCents = 300000L,
      totalExpensesCents = 150000L
    )
    assertEquals(50f, health.budgetSpentPercent, 0.1f)
    assertTrue(health.daysInMonth in 28..31)
  }

  @Test
  fun `verify pie chart sweep angles sum to 360 degrees and percentages sum to 100`() {
    val expenses = listOf(
      Expense(id = 1, monthYear = "2026-03", description = "Rent", amountCents = 100000L, category = "Housing"),
      Expense(id = 2, monthYear = "2026-03", description = "Groceries", amountCents = 50000L, category = "Food"),
      Expense(id = 3, monthYear = "2026-03", description = "Metro", amountCents = 30000L, category = "Transport"),
      Expense(id = 4, monthYear = "2026-03", description = "Electricity", amountCents = 20000L, category = "Utilities")
    )
    val totalCents = expenses.sumOf { it.amountCents }
    assertEquals(200000L, totalCents)

    val grouped = expenses.groupBy { it.category }
    var totalSweep = 0f
    var totalPct = 0f

    grouped.forEach { (_, catExpenses) ->
      val catTotal = catExpenses.sumOf { it.amountCents }
      val pct = (catTotal.toFloat() / totalCents.toFloat()) * 100f
      val sweep = (catTotal.toFloat() / totalCents.toFloat()) * 360f
      totalSweep += sweep
      totalPct += pct
    }

    assertEquals(360f, totalSweep, 0.01f)
    assertEquals(100f, totalPct, 0.01f)
  }
}
