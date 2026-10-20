package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var showDialog by remember { mutableStateOf(true) }

    if (!showDialog) return

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Add Expense") },
        text = {
            ExpenseEntryForm(
                onSubmit = {
                    // Handle submission logic
                    onDismiss()
                    onConfirm()
                },
                onCancel = { onDismiss() }
            )
        },
        confirmButton = {
            Button(onClick = {
                // Handle submission logic
                onDismiss()
                onConfirm()
            }) {
                Text(text = "Confirm")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text(text = "Cancel")
            }
        }
    )
}