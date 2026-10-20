package com.example

import com.example.util.TransactionDateFormatter
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun `verify transaction date and timing formatting`() {
    val now = System.currentTimeMillis()
    val formatted = TransactionDateFormatter.formatDateTime(now)
    assertTrue("Should contain Today for current timestamp: $formatted", formatted.startsWith("Today"))
    assertTrue("Should contain AM or PM: $formatted", formatted.contains("AM") || formatted.contains("PM"))

    val header = TransactionDateFormatter.formatDayHeader(now)
    assertEquals("Today", header)

    val timeOnly = TransactionDateFormatter.formatTimeOnly(now)
    assertTrue("Time should have AM or PM", timeOnly.contains("AM") || timeOnly.contains("PM"))

    val dateOnly = TransactionDateFormatter.formatDateOnly(now)
    assertTrue("Date should not be blank", dateOnly.isNotBlank())

    val full = TransactionDateFormatter.formatFullDateTime(now)
    assertTrue("Full format should contain 'at'", full.contains(" at "))

    // Test a fixed timestamp (e.g. 2026-06-15 14:30:00)
    val cal = Calendar.getInstance().apply {
      set(2026, Calendar.JUNE, 15, 14, 30, 0)
    }
    val fixedFormatted = TransactionDateFormatter.formatDateTime(cal.timeInMillis)
    assertTrue(fixedFormatted.contains("Jun 15"))
    assertTrue(fixedFormatted.contains("2:30 PM"))
  }
}
