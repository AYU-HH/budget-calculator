package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budgets",
    indices = [Index(value = ["monthYear"], unique = true)]
)
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val monthYear: String, // Format: "YYYY-MM", e.g. "2026-03"
    val monthlyIncomeCents: Long = 300000L, // Minor units (e.g. $3,000.00 = 300000 cents)
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "expenses",
    indices = [Index(value = ["monthYear"]), Index(value = ["category"])]
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val monthYear: String, // Ties expense to budget period
    val description: String,
    val amountCents: Long, // Minor units: Long cents (never floating point)
    val category: String,
    val isCommon: Boolean = false,
    val note: String = "",
    val isRecurring: Boolean = false,
    val recurrence: String = "NONE", // NONE, WEEKLY, MONTHLY, YEARLY
    val nextDueDate: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "income_entries",
    indices = [Index(value = ["monthYear"])]
)
data class IncomeEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val monthYear: String,
    val description: String,
    val amountCents: Long,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "common_expenses")
data class CommonExpense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val defaultAmountCents: Long,
    val category: String
)

@Entity(tableName = "todos")
data class TodoItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "category_limits")
data class CategoryLimit(
    @PrimaryKey
    val category: String,
    val monthlyCapCents: Long
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val currencyCode: String = "USD",
    val currencySymbol: String = "$",
    val rolloverEnabled: Boolean = false,
    val appLockPin: String = "",
    val isLockEnabled: Boolean = false,
    val themeMode: String = "DARK", // DARK, LIGHT, SYSTEM
    val isOnboardingCompleted: Boolean = true
)

@Entity(
    tableName = "budget_categories",
    indices = [Index(value = ["name"], unique = true)]
)
data class BudgetCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val icon: String = "🏷️",
    val colorHex: String = "#3B82F6",
    val monthlyBudgetLimitCents: Long = 50000L,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
