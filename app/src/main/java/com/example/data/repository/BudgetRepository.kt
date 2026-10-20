package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.model.AppSettingsEntity
import com.example.data.model.Budget
import com.example.data.model.BudgetCategory
import com.example.data.model.CategoryLimit
import com.example.data.model.CommonExpense
import com.example.data.model.Expense
import com.example.data.model.IncomeEntry
import com.example.data.model.TodoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BudgetRepository(context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val budgetDao = db.budgetDao()
    private val expenseDao = db.expenseDao()
    private val incomeDao = db.incomeDao()
    private val commonExpenseDao = db.commonExpenseDao()
    private val todoDao = db.todoDao()
    private val categoryLimitDao = db.categoryLimitDao()
    private val settingsDao = db.settingsDao()
    private val budgetCategoryDao = db.budgetCategoryDao()

    // ----------------------------------------------------
    // Budget & Multi-Month Flow
    // ----------------------------------------------------
    fun getBudgetFlow(monthYear: String): Flow<Budget?> =
        budgetDao.getBudgetForMonthFlow(monthYear).flowOn(Dispatchers.IO)

    fun getAllBudgetsFlow(): Flow<List<Budget>> =
        budgetDao.getAllBudgetsFlow().flowOn(Dispatchers.IO)

    suspend fun getOrCreateBudget(monthYear: String, defaultIncomeCents: Long = 300000L): Budget = withContext(Dispatchers.IO) {
        val existing = budgetDao.getBudgetForMonth(monthYear)
        if (existing != null) return@withContext existing

        // Check if rollover is enabled and fetch previous month's surplus
        val settings = settingsDao.getSettings() ?: AppSettingsEntity()
        var startingIncome = defaultIncomeCents

        if (settings.rolloverEnabled) {
            val prevMonth = getPreviousMonthString(monthYear)
            val prevBudget = budgetDao.getBudgetForMonth(prevMonth)
            if (prevBudget != null) {
                val prevExpenses = expenseDao.getExpensesForMonth(prevMonth)
                val prevTotal = prevExpenses.sumOf { it.amountCents }
                val surplus = (prevBudget.monthlyIncomeCents - prevTotal).coerceAtLeast(0L)
                startingIncome += surplus
            }
        }

        val newBudget = Budget(monthYear = monthYear, monthlyIncomeCents = startingIncome)
        budgetDao.insertOrUpdate(newBudget)
        newBudget
    }

    suspend fun updateMonthlyIncome(monthYear: String, incomeCents: Long) = withContext(Dispatchers.IO) {
        val existing = budgetDao.getBudgetForMonth(monthYear)
        if (existing != null) {
            budgetDao.updateMonthlyIncome(monthYear, incomeCents)
        } else {
            budgetDao.insertOrUpdate(Budget(monthYear = monthYear, monthlyIncomeCents = incomeCents))
        }
    }

    // ----------------------------------------------------
    // Expenses CRUD & Auto-Materialize Recurring
    // ----------------------------------------------------
    fun getAllExpensesFlow(): Flow<List<Expense>> =
        expenseDao.getAllExpensesFlow().flowOn(Dispatchers.IO)

    fun getExpensesFlow(monthYear: String): Flow<List<Expense>> =
        expenseDao.getExpensesForMonthFlow(monthYear).flowOn(Dispatchers.IO)

    suspend fun getExpensesForMonth(monthYear: String): List<Expense> = withContext(Dispatchers.IO) {
        expenseDao.getExpensesForMonth(monthYear)
    }

    suspend fun addExpense(expense: Expense): Long = withContext(Dispatchers.IO) {
        expenseDao.insert(expense)
    }

    suspend fun updateExpense(expense: Expense) = withContext(Dispatchers.IO) {
        expenseDao.update(expense)
    }

    suspend fun deleteExpense(expenseId: Long) = withContext(Dispatchers.IO) {
        expenseDao.deleteById(expenseId)
    }

    suspend fun getExpenseById(expenseId: Long): Expense? = withContext(Dispatchers.IO) {
        expenseDao.getExpenseById(expenseId)
    }

    suspend fun findPredictedAmount(description: String): Long? = withContext(Dispatchers.IO) {
        if (description.isBlank()) return@withContext null
        val match = expenseDao.findLatestExpenseByDescription(description.trim())
        match?.amountCents
    }

    suspend fun materializeRecurringExpensesForMonth(monthYear: String) = withContext(Dispatchers.IO) {
        val recurring = expenseDao.getRecurringExpenses()
        val currentMonthExpenses = expenseDao.getExpensesForMonth(monthYear)
        val existingDescriptions = currentMonthExpenses.map { it.description.lowercase() }.toSet()

        for (rec in recurring) {
            if (!existingDescriptions.contains(rec.description.lowercase())) {
                val materialized = rec.copy(
                    id = 0,
                    monthYear = monthYear,
                    isRecurring = true,
                    createdAt = System.currentTimeMillis()
                )
                expenseDao.insert(materialized)
            }
        }
    }

    // ----------------------------------------------------
    // Income Entries CRUD
    // ----------------------------------------------------
    fun getIncomeEntriesFlow(monthYear: String): Flow<List<IncomeEntry>> =
        incomeDao.getIncomeForMonthFlow(monthYear).flowOn(Dispatchers.IO)

    suspend fun addIncomeEntry(entry: IncomeEntry): Long = withContext(Dispatchers.IO) {
        incomeDao.insert(entry)
    }

    suspend fun deleteIncomeEntry(id: Long) = withContext(Dispatchers.IO) {
        incomeDao.deleteById(id)
    }

    // ----------------------------------------------------
    // Common Presets CRUD
    // ----------------------------------------------------
    fun getCommonExpensesFlow(): Flow<List<CommonExpense>> =
        commonExpenseDao.getAllCommonExpensesFlow().flowOn(Dispatchers.IO)

    suspend fun addCommonExpense(preset: CommonExpense): Long = withContext(Dispatchers.IO) {
        commonExpenseDao.insert(preset)
    }

    suspend fun updateCommonExpense(preset: CommonExpense) = withContext(Dispatchers.IO) {
        commonExpenseDao.update(preset)
    }

    suspend fun deleteCommonExpense(id: Long) = withContext(Dispatchers.IO) {
        commonExpenseDao.deleteById(id)
    }

    // ----------------------------------------------------
    // Todos CRUD
    // ----------------------------------------------------
    fun getTodosFlow(): Flow<List<TodoItem>> =
        todoDao.getAllTodosFlow().flowOn(Dispatchers.IO)

    suspend fun addTodo(todo: TodoItem): Long = withContext(Dispatchers.IO) {
        todoDao.insert(todo)
    }

    suspend fun updateTodo(todo: TodoItem) = withContext(Dispatchers.IO) {
        todoDao.update(todo)
    }

    suspend fun deleteTodo(id: Long) = withContext(Dispatchers.IO) {
        todoDao.deleteById(id)
    }

    // ----------------------------------------------------
    // Budget Categories CRUD
    // ----------------------------------------------------
    fun getAllCategoriesFlow(): Flow<List<BudgetCategory>> =
        budgetCategoryDao.getAllCategoriesFlow().flowOn(Dispatchers.IO)

    suspend fun getAllCategories(): List<BudgetCategory> = withContext(Dispatchers.IO) {
        budgetCategoryDao.getAllCategories()
    }

    suspend fun getCategoryById(id: Long): BudgetCategory? = withContext(Dispatchers.IO) {
        budgetCategoryDao.getCategoryById(id)
    }

    suspend fun getCategoryByName(name: String): BudgetCategory? = withContext(Dispatchers.IO) {
        budgetCategoryDao.getCategoryByName(name)
    }

    suspend fun addCategory(category: BudgetCategory): Long = withContext(Dispatchers.IO) {
        budgetCategoryDao.insert(category)
    }

    suspend fun updateCategory(category: BudgetCategory) = withContext(Dispatchers.IO) {
        budgetCategoryDao.update(category)
    }

    suspend fun deleteCategory(id: Long) = withContext(Dispatchers.IO) {
        budgetCategoryDao.deleteById(id)
    }

    fun getExpensesByCategoryFlow(category: String): Flow<List<Expense>> =
        expenseDao.getExpensesByCategoryFlow(category).flowOn(Dispatchers.IO)

    fun getExpensesByCategoryAndMonthFlow(category: String, monthYear: String): Flow<List<Expense>> =
        expenseDao.getExpensesByCategoryAndMonthFlow(category, monthYear).flowOn(Dispatchers.IO)

    // ----------------------------------------------------
    // Category Limits
    // ----------------------------------------------------
    fun getCategoryLimitsFlow(): Flow<List<CategoryLimit>> =
        categoryLimitDao.getAllLimitsFlow().flowOn(Dispatchers.IO)

    suspend fun setCategoryLimit(category: String, monthlyCapCents: Long) = withContext(Dispatchers.IO) {
        categoryLimitDao.insertOrUpdate(CategoryLimit(category, monthlyCapCents))
    }

    // ----------------------------------------------------
    // Settings & Security
    // ----------------------------------------------------
    fun getSettingsFlow(): Flow<AppSettingsEntity?> =
        settingsDao.getSettingsFlow().flowOn(Dispatchers.IO)

    suspend fun updateSettings(settings: AppSettingsEntity) = withContext(Dispatchers.IO) {
        settingsDao.insertOrUpdate(settings)
    }

    // ----------------------------------------------------
    // 6-Month Historical Totals for Insights
    // ----------------------------------------------------
    suspend fun getLast6MonthsTotals(): List<Pair<String, Long>> = withContext(Dispatchers.IO) {
        val result = mutableListOf<Pair<String, Long>>()
        val cal = Calendar.getInstance()
        val format = SimpleDateFormat("yyyy-MM", Locale.US)
        val displayFormat = SimpleDateFormat("MMM", Locale.US)

        for (i in 5 downTo 0) {
            val c = cal.clone() as Calendar
            c.add(Calendar.MONTH, -i)
            val monthKey = format.format(c.time)
            val label = displayFormat.format(c.time)
            val expenses = expenseDao.getExpensesForMonth(monthKey)
            val sum = expenses.sumOf { it.amountCents }
            result.add(Pair(label, sum))
        }
        result
    }

    // ----------------------------------------------------
    // Full JSON Backup & Restore
    // ----------------------------------------------------
    suspend fun exportFullBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 2)
        root.put("exportedAt", System.currentTimeMillis())

        val budgetsJson = JSONArray()
        budgetDao.getAllBudgets().forEach { b ->
            val o = JSONObject()
            o.put("monthYear", b.monthYear)
            o.put("monthlyIncomeCents", b.monthlyIncomeCents)
            budgetsJson.put(o)
        }
        root.put("budgets", budgetsJson)

        val expensesJson = JSONArray()
        expenseDao.getAllExpenses().forEach { e ->
            val o = JSONObject()
            o.put("monthYear", e.monthYear)
            o.put("description", e.description)
            o.put("amountCents", e.amountCents)
            o.put("category", e.category)
            o.put("isCommon", e.isCommon)
            o.put("note", e.note)
            o.put("isRecurring", e.isRecurring)
            o.put("createdAt", e.createdAt)
            expensesJson.put(o)
        }
        root.put("expenses", expensesJson)

        val presetsJson = JSONArray()
        commonExpenseDao.getAllCommonExpenses().forEach { p ->
            val o = JSONObject()
            o.put("name", p.name)
            o.put("defaultAmountCents", p.defaultAmountCents)
            o.put("category", p.category)
            presetsJson.put(o)
        }
        root.put("presets", presetsJson)

        val todosJson = JSONArray()
        todoDao.getAllTodos().forEach { t ->
            val o = JSONObject()
            o.put("text", t.text)
            o.put("isDone", t.isDone)
            todosJson.put(o)
        }
        root.put("todos", todosJson)

        val categoriesJson = JSONArray()
        budgetCategoryDao.getAllCategories().forEach { c ->
            val o = JSONObject()
            o.put("name", c.name)
            o.put("icon", c.icon)
            o.put("colorHex", c.colorHex)
            o.put("monthlyBudgetLimitCents", c.monthlyBudgetLimitCents)
            o.put("isDefault", c.isDefault)
            categoriesJson.put(o)
        }
        root.put("categories", categoriesJson)

        root.toString(2)
    }

    suspend fun restoreFullBackupJson(jsonString: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            var count = 0

            // Restore budgets
            if (root.has("budgets")) {
                val bArray = root.getJSONArray("budgets")
                for (i in 0 until bArray.length()) {
                    val o = bArray.getJSONObject(i)
                    budgetDao.insertOrUpdate(
                        Budget(
                            monthYear = o.getString("monthYear"),
                            monthlyIncomeCents = o.getLong("monthlyIncomeCents")
                        )
                    )
                    count++
                }
            }

            // Restore expenses
            if (root.has("expenses")) {
                val eArray = root.getJSONArray("expenses")
                for (i in 0 until eArray.length()) {
                    val o = eArray.getJSONObject(i)
                    expenseDao.insert(
                        Expense(
                            monthYear = o.getString("monthYear"),
                            description = o.getString("description"),
                            amountCents = o.getLong("amountCents"),
                            category = o.getString("category"),
                            isCommon = o.optBoolean("isCommon", false),
                            note = o.optString("note", ""),
                            isRecurring = o.optBoolean("isRecurring", false),
                            createdAt = o.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                    count++
                }
            }

            // Restore presets
            if (root.has("presets")) {
                val pArray = root.getJSONArray("presets")
                for (i in 0 until pArray.length()) {
                    val o = pArray.getJSONObject(i)
                    commonExpenseDao.insert(
                        CommonExpense(
                            name = o.getString("name"),
                            defaultAmountCents = o.getLong("defaultAmountCents"),
                            category = o.getString("category")
                        )
                    )
                    count++
                }
            }

            // Restore todos
            if (root.has("todos")) {
                val tArray = root.getJSONArray("todos")
                for (i in 0 until tArray.length()) {
                    val o = tArray.getJSONObject(i)
                    todoDao.insert(
                        TodoItem(
                            text = o.getString("text"),
                            isDone = o.getBoolean("isDone")
                        )
                    )
                    count++
                }
            }

            // Restore budget categories
            if (root.has("categories")) {
                val cArray = root.getJSONArray("categories")
                for (i in 0 until cArray.length()) {
                    val o = cArray.getJSONObject(i)
                    budgetCategoryDao.insert(
                        BudgetCategory(
                            name = o.getString("name"),
                            icon = o.optString("icon", "🏷️"),
                            colorHex = o.optString("colorHex", "#3B82F6"),
                            monthlyBudgetLimitCents = o.optLong("monthlyBudgetLimitCents", 50000L),
                            isDefault = o.optBoolean("isDefault", false)
                        )
                    )
                    count++
                }
            }

            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getPreviousMonthString(monthYear: String): String {
        return try {
            val parts = monthYear.split("-")
            var year = parts[0].toInt()
            var month = parts[1].toInt() - 1 // 1-indexed to 0-indexed
            month -= 1
            if (month < 0) {
                month = 11
                year -= 1
            }
            String.format(Locale.US, "%04d-%02d", year, month + 1)
        } catch (e: Exception) {
            monthYear
        }
    }
}
