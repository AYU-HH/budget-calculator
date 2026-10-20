package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.data.model.Expense

/**
 * SpendingDonutChart delegates to the comprehensive Recharts/D3-inspired CategoryPieChart.
 */
@Composable
fun SpendingDonutChart(
    expenses: List<Expense>,
    currencySymbol: String = "$",
    categoryLimits: Map<String, Long> = emptyMap(),
    modifier: Modifier = Modifier
) {
    CategoryPieChart(
        expenses = expenses,
        currencySymbol = currencySymbol,
        categoryLimits = categoryLimits,
        modifier = modifier
    )
}
