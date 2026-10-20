package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BudgetPrimary

@Composable
fun SettingsCard(
    themeMode: String,
    onThemeChange: (String) -> Unit,
    onSave: () -> Unit
) {
    var notificationFrequency by remember { mutableStateOf("Weekly") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.Start)
        )

        SettingItem(
            title = "Theme",
            value = themeMode,
            options = listOf("Light", "Dark", "System"),
            selectedOption = themeMode,
            onOptionSelected = { onThemeChange(it) }
        )

        SettingItem(
            title = "Notification Frequency",
            value = notificationFrequency,
            options = listOf("Daily", "Weekly", "Monthly", "Never"),
            selectedOption = notificationFrequency,
            onOptionSelected = { notificationFrequency = it }
        )

        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BudgetPrimary)
        ) {
            Text(
                text = "Save Settings",
                color = androidx.compose.ui.graphics.Color.White
            )
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    value: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        DropdownMenu(
            expanded = false,
            onDismissRequest = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option) },
                    onClick = { onOptionSelected(option) },
                    selected = (option == selectedOption),
                    content = {}
                )
            }
        }
    }
}