package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.components.*
import com.example.ui.screens.*

@Composable
fun BudgetApp(viewModel: BudgetViewModel) {
    val navController = rememberNavController()
    val themeMode by viewModel.themeMode.collectAsState()

    MyApplicationTheme(themeMode = themeMode) {
        Scaffold(
            bottomBar = { BottomNavigationBar(navController) }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                startDestination = "budget"
            ) {
                composable("budget") {
                    BudgetScreen(viewModel = viewModel)
                }
                composable("expenses") {
                    ExpensesScreen(viewModel = viewModel)
                }
                composable("insights") {
                    InsightsScreen(viewModel = viewModel)
                }
                composable("settings") {
                    SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem("Budget", "budget"),
        BottomNavItem("Expenses", "expenses"),
        BottomNavItem("Insights", "insights"),
        BottomNavItem("Settings", "settings")
    )

    BottomAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                BottomNavigation(
                    selected = navController.currentDestination?.route == item.route,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    label = { Text(text = item.title) },
                    icon = {
                        when (item.route) {
                            "budget" -> Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Home,
                                contentDescription = "Budget"
                            )
                            "expenses" -> Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Receipt,
                                contentDescription = "Expenses"
                            )
                            "insights" -> Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Insights,
                                contentDescription = "Insights"
                            )
                            "settings" -> Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Settings,
                                contentDescription = "Settings"
                            )
                        }
                    }
                )
            }
        }
    }
}

data class BottomNavItem(
    val title: String,
    val route: String
)