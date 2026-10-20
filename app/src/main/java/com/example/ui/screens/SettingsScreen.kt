package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.components.SettingsCard

@Composable
fun SettingsScreen(viewModel: BudgetViewModel) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsCard(
            themeMode = state.themeMode,
            onThemeChange = { newThemeMode -> viewModel.updateThemeMode(newThemeMode) },
            onSave = { viewModel.saveSettings() }
        )
    }
}