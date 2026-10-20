package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.components.BudgetSummaryCard
import com.example.ui.components.SpendingChart
import com.example.ui.theme.BudgetPrimary
import com.example.ui.theme.BudgetSecondary
import com.example.ui.theme.BudgetTertiary

@Composable
fun BudgetScreen(viewModel: BudgetViewModel) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BudgetSummaryCard(
            totalBudget = "$${state.totalBudget}",
            spentAmount = "$${state.spentAmount}",
            remainingAmount = "$${state.remainingAmount}",
            savingsGoal = "$${state.savingsGoal}",
            onAddExpenseClick = { /* TODO: Navigate to expense entry */ }
        )

        SpendingChart(
            chartData = state.spendingChartData,
            modifier = Modifier.fillMaxWidth()
        )

        // Placeholder for recent transactions
        Text(
            text = "Recent Transactions",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 16.dp)
        )

        // TODO: Add recent transactions list
    }
}