package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AppSettingsEntity
import com.example.data.model.Budget
import com.example.data.model.BudgetCategory
import com.example.data.model.CategoryLimit
import com.example.data.model.CommonExpense
import com.example.data.model.Expense
import com.example.data.model.IncomeEntry
import com.example.data.model.TodoItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        Budget::class,
        Expense::class,
        IncomeEntry::class,
        CommonExpense::class,
        TodoItem::class,
        CategoryLimit::class,
        AppSettingsEntity::class,
        BudgetCategory::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun budgetDao(): BudgetDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun incomeDao(): IncomeDao
    abstract fun commonExpenseDao(): CommonExpenseDao
    abstract fun todoDao(): TodoDao
    abstract fun categoryLimitDao(): CategoryLimitDao
    abstract fun settingsDao(): SettingsDao
    abstract fun budgetCategoryDao(): BudgetCategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Safely add any new columns to existing tables without destroying user data
                try {
                    db.execSQL("ALTER TABLE expenses ADD COLUMN note TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE expenses ADD COLUMN isRecurring INTEGER NOT NULL DEFAULT 0")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE expenses ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
                } catch (_: Exception) {}
                db.execSQL("CREATE TABLE IF NOT EXISTS category_limits (category TEXT NOT NULL PRIMARY KEY, monthlyCapCents INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS app_settings (id INTEGER PRIMARY KEY NOT NULL, currencyCode TEXT NOT NULL, currencySymbol TEXT NOT NULL, rolloverEnabled INTEGER NOT NULL, appLockPin TEXT NOT NULL, isLockEnabled INTEGER NOT NULL, themeMode TEXT NOT NULL, isOnboardingCompleted INTEGER NOT NULL)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Safely add custom budget categories table without affecting old records
                db.execSQL("CREATE TABLE IF NOT EXISTS budget_categories (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, icon TEXT NOT NULL, colorHex TEXT NOT NULL, monthlyBudgetLimitCents INTEGER NOT NULL, isDefault INTEGER NOT NULL)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budget_calculator_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val database = getDatabase(context)
                val currentMonth = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())

                // 1. Initial Budget
                database.budgetDao().insertOrUpdate(
                    Budget(
                        monthYear = currentMonth,
                        monthlyIncomeCents = 300000L // $3,000.00
                    )
                )

                // 2. Initial Primary Income Entry
                database.incomeDao().insert(
                    IncomeEntry(
                        monthYear = currentMonth,
                        description = "Monthly Salary",
                        amountCents = 300000L
                    )
                )

                // 3. Initial Common Expense Presets
                val presets = listOf(
                    CommonExpense(name = "Rent", defaultAmountCents = 120000L, category = "Housing"),
                    CommonExpense(name = "Groceries", defaultAmountCents = 40000L, category = "Food"),
                    CommonExpense(name = "Utilities", defaultAmountCents = 15000L, category = "Utilities"),
                    CommonExpense(name = "Internet", defaultAmountCents = 6000L, category = "Utilities"),
                    CommonExpense(name = "Transport", defaultAmountCents = 20000L, category = "Transport"),
                    CommonExpense(name = "Entertainment", defaultAmountCents = 10000L, category = "Entertainment"),
                    CommonExpense(name = "Cold Drinks", defaultAmountCents = 500L, category = "Food")
                )
                presets.forEach { database.commonExpenseDao().insert(it) }

                // 4. Initial Demo Expenses
                val demoExpenses = listOf(
                    Expense(
                        monthYear = currentMonth,
                        description = "Supermarket Groceries",
                        amountCents = 14550L, // $145.50
                        category = "Food",
                        note = "Weekly groceries restock"
                    ),
                    Expense(
                        monthYear = currentMonth,
                        description = "Electric Utility Bill",
                        amountCents = 8520L, // $85.20
                        category = "Utilities",
                        note = "Summer AC usage"
                    ),
                    Expense(
                        monthYear = currentMonth,
                        description = "Metro Subway Pass",
                        amountCents = 6000L, // $60.00
                        category = "Transport"
                    ),
                    Expense(
                        monthYear = currentMonth,
                        description = "Cinema Tickets & Snacks",
                        amountCents = 3200L, // $32.00
                        category = "Entertainment"
                    )
                )
                demoExpenses.forEach { database.expenseDao().insert(it) }

                // 5. Initial Category Limits
                val limits = listOf(
                    CategoryLimit(category = "Housing", monthlyCapCents = 140000L),
                    CategoryLimit(category = "Food", monthlyCapCents = 60000L),
                    CategoryLimit(category = "Transport", monthlyCapCents = 25000L),
                    CategoryLimit(category = "Utilities", monthlyCapCents = 20000L),
                    CategoryLimit(category = "Entertainment", monthlyCapCents = 15000L),
                    CategoryLimit(category = "Other", monthlyCapCents = 15000L)
                )
                limits.forEach { database.categoryLimitDao().insertOrUpdate(it) }

                // 6. Initial Todos
                val todos = listOf(
                    TodoItem(text = "Review monthly electricity bill", isDone = true),
                    TodoItem(text = "Buy organic vegetables", isDone = false),
                    TodoItem(text = "Compare internet providers for next month", isDone = false)
                )
                todos.forEach { database.todoDao().insert(it) }

                // 7. Initial Budget Categories
                val initialCategories = listOf(
                    BudgetCategory(name = "Housing", icon = "🏠", colorHex = "#3B82F6", monthlyBudgetLimitCents = 140000L, isDefault = true),
                    BudgetCategory(name = "Food", icon = "🍔", colorHex = "#10B981", monthlyBudgetLimitCents = 60000L, isDefault = true),
                    BudgetCategory(name = "Transport", icon = "🚗", colorHex = "#8B5CF6", monthlyBudgetLimitCents = 25000L, isDefault = true),
                    BudgetCategory(name = "Utilities", icon = "💡", colorHex = "#F59E0B", monthlyBudgetLimitCents = 20000L, isDefault = true),
                    BudgetCategory(name = "Entertainment", icon = "🎬", colorHex = "#EC4899", monthlyBudgetLimitCents = 15000L, isDefault = true),
                    BudgetCategory(name = "Health", icon = "💊", colorHex = "#14B8A6", monthlyBudgetLimitCents = 10000L, isDefault = true),
                    BudgetCategory(name = "Shopping", icon = "🛍️", colorHex = "#F43F5E", monthlyBudgetLimitCents = 15000L, isDefault = true),
                    BudgetCategory(name = "Other", icon = "📦", colorHex = "#6366F1", monthlyBudgetLimitCents = 15000L, isDefault = true)
                )
                database.budgetCategoryDao().insertAll(initialCategories)

                // 8. Initial App Settings
                database.settingsDao().insertOrUpdate(
                    AppSettingsEntity(
                        id = 1,
                        currencyCode = "USD",
                        currencySymbol = "$",
                        rolloverEnabled = true,
                        appLockPin = "",
                        isLockEnabled = false,
                        themeMode = "DARK",
                        isOnboardingCompleted = true
                    )
                )
            }
        }
    }
}
