package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BudgetPrimary
import com.example.ui.theme.BudgetSecondary
import com.example.ui.theme.BudgetTertiary

@Composable
fun SpendingChart(
    chartData: List<ChartDataPoint>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Monthly Spending",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.Start)
            )

            // Placeholder for chart visualization
            // In a real app, use a library like MPAndroidChart or Jetpack Compose Charts
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Chart Placeholder",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            // Chart legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ChartLegendItem(
                    color = BudgetPrimary,
                    label = "Food"
                )
                ChartLegendItem(
                    color = BudgetSecondary,
                    label = "Transport"
                )
                ChartLegendItem(
                    color = BudgetTertiary,
                    label = "Entertainment"
                )
            }
        }
    }
}

@Composable
fun ChartLegendItem(
    color: Color,
    label: String
) {
    Row(
        modifier = Modifier.size(40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

// Data class for chart data
data class ChartDataPoint(
    val category: String,
    val amount: Float,
    val color: Color
)