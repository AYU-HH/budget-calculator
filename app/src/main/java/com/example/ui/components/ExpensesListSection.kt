package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ExpensesListSection(
    expenses: List<ExpenseItem>,
    onAddExpenseClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Expenses",
                style = MaterialTheme.typography.headlineSmall
            )
            Button(
                onClick = onAddExpenseClick,
                modifier = Modifier.padding(end = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BudgetPrimary)
            ) {
                Text("Add", color = androidx.compose.ui.graphics.Color.White)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(expenses) { expense ->
                ExpenseItemCard(expense = expense)
            }
        }
    }
}

@Composable
fun ExpenseItemCard(expense: ExpenseItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = expense.date,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = expense.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = expense.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Text(
                text = expense.amount,
                style = MaterialTheme.typography.bodyMedium,
                color = if (expense.isIncome) BudgetGreen else androidx.compose.ui.graphics.Color.Red
            )
        }
    }
}

data class ExpenseItem(
    val date: String,
    val description: String,
    val category: String,
    val amount: String,
    val isIncome: Boolean
)