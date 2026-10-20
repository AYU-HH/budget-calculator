package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppSettingsEntity
import com.example.data.model.Budget
import com.example.data.model.BudgetCategory
import com.example.data.model.CategoryLimit
import com.example.data.model.CommonExpense
import com.example.data.model.Expense
import com.example.data.model.ExpenseCategory
import com.example.data.model.IncomeEntry
import com.example.data.model.TodoItem
import com.example.data.repository.BudgetRepository
import com.example.domain.BudgetHealth
import com.example.domain.BudgetHealthCalculator
import com.example.domain.CurrencyFormatter
import com.example.domain.MathEvaluator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class SortOption(val displayName: String) {
    DATE_DESC("Newest first"),
    DATE_ASC("Oldest first"),
    AMOUNT_DESC("Highest amount"),
    AMOUNT_ASC("Lowest amount")
}

data class DashboardUiState(
    val monthYear: String = "",
    val monthlyIncomeCents: Long = 300000L,
    val totalExpensesCents: Long = 0L,
    val remainingBudgetCents: Long = 300000L,
    val percentageUsed: Float = 0f,
    val expenses: List<Expense> = emptyList(),
    val rawMonthExpenses: List<Expense> = emptyList(),
    val commonExpenses: List<CommonExpense> = emptyList(),
    val todos: List<TodoItem> = emptyList(),
    val completedTodosCount: Int = 0,
    val totalTodosCount: Int = 0,
    val categoryLimits: Map<String, Long> = emptyMap(),
    val health: BudgetHealth? = null,
    val currencySymbol: String = "$",
    val incomeEntries: List<IncomeEntry> = emptyList(),
    val categories: List<BudgetCategory> = emptyList()
)

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BudgetRepository(application)

    // Current Month Selection (format: "YYYY-MM")
    private val _currentMonthYear = MutableStateFlow(
        SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
    )
    val currentMonthYear: StateFlow<String> = _currentMonthYear.asStateFlow()

    // Add Expense Form State
    private val _descriptionInput = MutableStateFlow("")
    val descriptionInput: StateFlow<String> = _descriptionInput.asStateFlow()

    private val _amountInput = MutableStateFlow("")
    val amountInput: StateFlow<String> = _amountInput.asStateFlow()

    private val _selectedCategory = MutableStateFlow(ExpenseCategory.FOOD)
    val selectedCategory: StateFlow<ExpenseCategory> = _selectedCategory.asStateFlow()

    private val _noteInput = MutableStateFlow("")
    val noteInput: StateFlow<String> = _noteInput.asStateFlow()

    private val _isRecurringInput = MutableStateFlow(false)
    val isRecurringInput: StateFlow<Boolean> = _isRecurringInput.asStateFlow()

    private val _predictedAmount = MutableStateFlow<Long?>(null)
    val predictedAmount: StateFlow<Long?> = _predictedAmount.asStateFlow()

    private val _transactionTimeInput = MutableStateFlow<Long?>(null)
    val transactionTimeInput: StateFlow<Long?> = _transactionTimeInput.asStateFlow()

    fun onTransactionTimeChange(timeMillis: Long?) {
        _transactionTimeInput.value = timeMillis
    }

    // Search, Filter, & Sort
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _categoryFilter = MutableStateFlow<String?>(null)
    val categoryFilter: StateFlow<String?> = _categoryFilter.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.DATE_DESC)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    // Edit Expense Dialog
    private val _editingExpense = MutableStateFlow<Expense?>(null)
    val editingExpense: StateFlow<Expense?> = _editingExpense.asStateFlow()

    // Dialog & UI Visibility
    private val _showEditIncomeDialog = MutableStateFlow(false)
    val showEditIncomeDialog: StateFlow<Boolean> = _showEditIncomeDialog.asStateFlow()

    private val _showAddCustomCommonDialog = MutableStateFlow(false)
    val showAddCustomCommonDialog: StateFlow<Boolean> = _showAddCustomCommonDialog.asStateFlow()

    private val _showIncomeListDialog = MutableStateFlow(false)
    val showIncomeListDialog: StateFlow<Boolean> = _showIncomeListDialog.asStateFlow()

    // Navigation & Theme
    private val _selectedTab = MutableStateFlow(0) // 0=Dashboard, 1=Insights, 2=Calculator, 3=Notes, 4=Settings
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _themeMode = MutableStateFlow("SYSTEM")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Undo caches
    private var lastDeletedExpense: Expense? = null
    private var lastDeletedTodo: TodoItem? = null

    // Calculator State
    private val _calculatorExpression = MutableStateFlow("")
    val calculatorExpression: StateFlow<String> = _calculatorExpression.asStateFlow()

    private val _calculatorDisplay = MutableStateFlow("0")
    val calculatorDisplay: StateFlow<String> = _calculatorDisplay.asStateFlow()

    // App Settings & Lock
    private val _settings = MutableStateFlow(AppSettingsEntity())
    val settings: StateFlow<AppSettingsEntity> = _settings.asStateFlow()

    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    // 6-Month historical totals computed reactively from all expenses in real-time
    val historyTotals: StateFlow<List<Pair<String, Long>>> = repository.getAllExpensesFlow().map { allExpenses ->
        val result = mutableListOf<Pair<String, Long>>()
        val cal = Calendar.getInstance()
        val format = SimpleDateFormat("yyyy-MM", Locale.US)
        val displayFormat = SimpleDateFormat("MMM", Locale.US)

        for (i in 5 downTo 0) {
            val c = cal.clone() as Calendar
            c.add(Calendar.MONTH, -i)
            val monthKey = format.format(c.time)
            val label = displayFormat.format(c.time)
            val sum = allExpenses.filter { it.monthYear == monthKey }.sumOf { it.amountCents }
            result.add(Pair(label, sum))
        }
        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private data class FilterParams(
        val month: String,
        val search: String,
        val catFilter: String?,
        val sort: SortOption,
        val settings: AppSettingsEntity
    )

    private data class RealtimeDataBundle(
        val allExpenses: List<Expense>,
        val allBudgets: List<Budget>,
        val categoryLimits: List<CategoryLimit>,
        val commonExpenses: List<CommonExpense>,
        val todos: List<TodoItem>
    )

    private val filterParamsFlow: Flow<FilterParams> = combine(
        _currentMonthYear,
        _searchQuery,
        _categoryFilter,
        _sortOption,
        _settings
    ) { month, search, cat, sort, sett ->
        FilterParams(month, search, cat, sort, sett)
    }

    private val dataBundleFlow: Flow<RealtimeDataBundle> = combine(
        repository.getAllExpensesFlow(),
        repository.getAllBudgetsFlow(),
        repository.getCategoryLimitsFlow(),
        repository.getCommonExpensesFlow(),
        repository.getTodosFlow()
    ) { expenses, budgets, limits, presets, todos ->
        RealtimeDataBundle(expenses, budgets, limits, presets, todos)
    }

    // Combined Real-Time Dashboard State
    val dashboardState: StateFlow<DashboardUiState> = combine(
        filterParamsFlow,
        dataBundleFlow
    ) { params, data ->
        val month = params.month
        val currentBudget = data.allBudgets.find { it.monthYear == month }
        val incomeCents = currentBudget?.monthlyIncomeCents ?: 300000L

        val monthExpenses = data.allExpenses.filter { it.monthYear == month }
        val totalExpensesCents = monthExpenses.sumOf { it.amountCents }
        val remainingCents = incomeCents - totalExpensesCents
        val pct = if (incomeCents > 0) {
            (totalExpensesCents.toFloat() / incomeCents.toFloat()) * 100f
        } else 0f

        // Filter & Sort for UI display in real-time
        var filtered = monthExpenses
        if (params.search.isNotBlank()) {
            val q = params.search.trim().lowercase()
            filtered = filtered.filter {
                it.description.lowercase().contains(q) || it.note.lowercase().contains(q)
            }
        }
        if (params.catFilter != null) {
            filtered = filtered.filter { it.category.equals(params.catFilter, ignoreCase = true) }
        }
        filtered = when (params.sort) {
            SortOption.DATE_DESC -> filtered.sortedByDescending { it.createdAt }
            SortOption.DATE_ASC -> filtered.sortedBy { it.createdAt }
            SortOption.AMOUNT_DESC -> filtered.sortedByDescending { it.amountCents }
            SortOption.AMOUNT_ASC -> filtered.sortedBy { it.amountCents }
        }

        val health = BudgetHealthCalculator.calculate(
            monthYear = month,
            monthlyIncomeCents = incomeCents,
            totalExpensesCents = totalExpensesCents
        )

        val limitsMap = data.categoryLimits.associate { it.category to it.monthlyCapCents }

        DashboardUiState(
            monthYear = month,
            monthlyIncomeCents = incomeCents,
            totalExpensesCents = totalExpensesCents,
            remainingBudgetCents = remainingCents,
            percentageUsed = pct,
            expenses = filtered,
            rawMonthExpenses = monthExpenses,
            commonExpenses = data.commonExpenses,
            todos = data.todos,
            completedTodosCount = data.todos.count { it.isDone },
            totalTodosCount = data.todos.size,
            categoryLimits = limitsMap,
            health = health,
            currencySymbol = params.settings.currencySymbol
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    init {
        // Observe current month and ensure budget record exists + materialize recurring expenses
        viewModelScope.launch {
            _currentMonthYear.collect { month ->
                repository.getOrCreateBudget(month)
                repository.materializeRecurringExpensesForMonth(month)
            }
        }

        // Load initial settings and observe them
        viewModelScope.launch {
            repository.getSettingsFlow().collect { s ->
                val activeSettings = s ?: AppSettingsEntity()
                _settings.value = activeSettings
                _themeMode.value = activeSettings.themeMode
                _isDarkTheme.value = activeSettings.themeMode != "LIGHT"
                if (activeSettings.isLockEnabled && activeSettings.appLockPin.isNotBlank()) {
                    _isAppLocked.value = true
                }
            }
        }
    }

    private fun refreshHistory() {
        // Kept for backward-compatibility; historyTotals updates automatically via Flow
    }

    // ----------------------------------------------------
    // Month Switching
    // ----------------------------------------------------
    fun nextMonth() {
        val cal = Calendar.getInstance()
        try {
            val parts = _currentMonthYear.value.split("-")
            cal.set(Calendar.YEAR, parts[0].toInt())
            cal.set(Calendar.MONTH, parts[1].toInt() - 1)
            cal.add(Calendar.MONTH, 1)
            _currentMonthYear.value = SimpleDateFormat("yyyy-MM", Locale.US).format(cal.time)
            refreshHistory()
        } catch (e: Exception) {
            // ignore
        }
    }

    fun prevMonth() {
        val cal = Calendar.getInstance()
        try {
            val parts = _currentMonthYear.value.split("-")
            cal.set(Calendar.YEAR, parts[0].toInt())
            cal.set(Calendar.MONTH, parts[1].toInt() - 1)
            cal.add(Calendar.MONTH, -1)
            _currentMonthYear.value = SimpleDateFormat("yyyy-MM", Locale.US).format(cal.time)
            refreshHistory()
        } catch (e: Exception) {
            // ignore
        }
    }

    // ----------------------------------------------------
    // Expense Form Actions
    // ----------------------------------------------------
    fun onDescriptionChange(text: String) {
        _descriptionInput.value = text
        // Smart Auto-categorization rule match
        val suggested = ExpenseCategory.suggestCategory(text)
        if (suggested != ExpenseCategory.OTHER) {
            _selectedCategory.value = suggested
        }

        // Predictive amount prefill
        viewModelScope.launch {
            _predictedAmount.value = repository.findPredictedAmount(text)
        }
    }

    fun onAmountChange(text: String) {
        _amountInput.value = text
    }

    fun onCategoryChange(category: ExpenseCategory) {
        _selectedCategory.value = category
    }

    fun onNoteChange(note: String) {
        _noteInput.value = note
    }

    fun onRecurringToggle(recurring: Boolean) {
        _isRecurringInput.value = recurring
    }

    fun applyPredictedAmount(cents: Long) {
        _amountInput.value = CurrencyFormatter.centsToDecimalString(cents)
        _predictedAmount.value = null
    }

    fun addExpense() {
        val desc = _descriptionInput.value.trim()
        val amountCents = CurrencyFormatter.parseToCents(_amountInput.value)

        if (desc.isEmpty()) {
            _snackbarMessage.value = "Please enter an expense description"
            return
        }
        if (amountCents == null || amountCents <= 0L) {
            _snackbarMessage.value = "Please enter a valid amount"
            return
        }

        viewModelScope.launch {
            val txTime = _transactionTimeInput.value ?: System.currentTimeMillis()
            val txMonthYear = SimpleDateFormat("yyyy-MM", Locale.US).format(Date(txTime))
            val expense = Expense(
                monthYear = txMonthYear,
                description = desc,
                amountCents = amountCents,
                category = _selectedCategory.value.displayName,
                note = _noteInput.value.trim(),
                isRecurring = _isRecurringInput.value,
                createdAt = txTime
            )
            repository.addExpense(expense)
            _snackbarMessage.value = "Added $desc (${CurrencyFormatter.formatCents(amountCents, _settings.value.currencySymbol)})"
            _descriptionInput.value = ""
            _amountInput.value = ""
            _noteInput.value = ""
            _isRecurringInput.value = false
            _predictedAmount.value = null
            _transactionTimeInput.value = null
            refreshHistory()
        }
    }

    fun quickAddCommonExpense(preset: CommonExpense) {
        viewModelScope.launch {
            val expense = Expense(
                monthYear = _currentMonthYear.value,
                description = preset.name,
                amountCents = preset.defaultAmountCents,
                category = preset.category,
                isCommon = true
            )
            repository.addExpense(expense)
            _snackbarMessage.value = "Quick added ${preset.name}"
            refreshHistory()
        }
    }

    fun deleteExpense(expenseId: Long) {
        viewModelScope.launch {
            val exp = repository.getExpenseById(expenseId)
            lastDeletedExpense = exp
            repository.deleteExpense(expenseId)
            _snackbarMessage.value = "Expense deleted"
            refreshHistory()
        }
    }

    fun undoDeleteExpense() {
        val toRestore = lastDeletedExpense ?: return
        viewModelScope.launch {
            repository.addExpense(toRestore.copy(id = 0))
            lastDeletedExpense = null
            _snackbarMessage.value = "Restored ${toRestore.description}"
            refreshHistory()
        }
    }

    // ----------------------------------------------------
    // Edit Expense Dialog
    // ----------------------------------------------------
    fun startEditingExpense(expense: Expense) {
        _editingExpense.value = expense
    }

    fun dismissEditingExpense() {
        _editingExpense.value = null
    }

    fun saveEditedExpense(updated: Expense) {
        viewModelScope.launch {
            repository.updateExpense(updated)
            _editingExpense.value = null
            _snackbarMessage.value = "Expense updated"
            refreshHistory()
        }
    }

    // ----------------------------------------------------
    // Income Management
    // ----------------------------------------------------
    fun updateMonthlyIncome(newIncomeCents: Long) {
        viewModelScope.launch {
            repository.updateMonthlyIncome(_currentMonthYear.value, newIncomeCents)
            _showEditIncomeDialog.value = false
            _snackbarMessage.value = "Income updated to ${CurrencyFormatter.formatCents(newIncomeCents, _settings.value.currencySymbol)}"
        }
    }

    // ----------------------------------------------------
    // Common Presets
    // ----------------------------------------------------
    fun addCustomPreset(name: String, amountCents: Long, category: String) {
        viewModelScope.launch {
            val preset = CommonExpense(
                name = name.trim(),
                defaultAmountCents = amountCents,
                category = category
            )
            repository.addCommonExpense(preset)
            _showAddCustomCommonDialog.value = false
            _snackbarMessage.value = "Saved preset: ${preset.name}"
        }
    }

    // ----------------------------------------------------
    // Todo CRUD
    // ----------------------------------------------------
    fun addTodo(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.addTodo(TodoItem(text = text.trim()))
        }
    }

    fun toggleTodo(item: TodoItem) {
        viewModelScope.launch {
            repository.updateTodo(item.copy(isDone = !item.isDone))
        }
    }

    fun deleteTodo(id: Long) {
        viewModelScope.launch {
            repository.deleteTodo(id)
        }
    }

    // ----------------------------------------------------
    // Category Limits & Budget Categories
    // ----------------------------------------------------
    val budgetCategories: StateFlow<List<BudgetCategory>> = repository.getAllCategoriesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateCategoryLimit(category: String, capCents: Long) {
        viewModelScope.launch {
            repository.setCategoryLimit(category, capCents)
            _snackbarMessage.value = "Limit for $category updated"
        }
    }

    fun addCategory(name: String, icon: String = "🏷️", limitCents: Long = 50000L, colorHex: String = "#3B82F6") {
        if (name.isBlank()) return
        viewModelScope.launch {
            val cat = BudgetCategory(
                name = name.trim(),
                icon = icon.ifBlank { "🏷️" },
                colorHex = colorHex,
                monthlyBudgetLimitCents = limitCents,
                isDefault = false
            )
            repository.addCategory(cat)
            repository.setCategoryLimit(name.trim(), limitCents)
            _snackbarMessage.value = "Created category '$name'"
        }
    }

    fun deleteCategory(id: Long, name: String) {
        viewModelScope.launch {
            repository.deleteCategory(id)
            _snackbarMessage.value = "Removed category '$name'"
        }
    }

    // ----------------------------------------------------
    // Search, Filter & Sort
    // ----------------------------------------------------
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String?) {
        _categoryFilter.value = category
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    // ----------------------------------------------------
    // Calculator Keypad Logic
    // ----------------------------------------------------
    fun onCalculatorKey(key: String) {
        when (key) {
            "C" -> {
                _calculatorExpression.value = ""
                _calculatorDisplay.value = "0"
            }
            "Back" -> {
                val current = _calculatorExpression.value
                if (current.isNotEmpty()) {
                    val updated = current.dropLast(1)
                    _calculatorExpression.value = updated
                    if (updated.isNotEmpty()) {
                        MathEvaluator.evaluate(updated).onSuccess {
                            _calculatorDisplay.value = MathEvaluator.formatResult(it)
                        }
                    } else {
                        _calculatorDisplay.value = "0"
                    }
                }
            }
            "=" -> {
                val expr = _calculatorExpression.value
                if (expr.isNotEmpty()) {
                    val result = MathEvaluator.evaluate(expr)
                    result.onSuccess { value ->
                        val formatted = MathEvaluator.formatResult(value)
                        _calculatorDisplay.value = formatted
                        _calculatorExpression.value = formatted
                    }.onFailure {
                        _calculatorDisplay.value = "Error"
                    }
                }
            }
            "Ans" -> {
                val ansStr = MathEvaluator.formatResult(MathEvaluator.lastAnswer)
                _calculatorExpression.value += ansStr
                _calculatorDisplay.value = ansStr
            }
            else -> {
                _calculatorExpression.value += key
                MathEvaluator.evaluate(_calculatorExpression.value).onSuccess {
                    _calculatorDisplay.value = MathEvaluator.formatResult(it)
                }
            }
        }
    }

    fun useCalculatorResultInAmount() {
        val value = _calculatorDisplay.value
        if (value.isNotEmpty() && value != "Error" && value != "0") {
            _amountInput.value = value
            _selectedTab.value = 0 // Return to Dashboard
            _snackbarMessage.value = "Amount set to $value"
        }
    }

    // ----------------------------------------------------
    // Settings & Security
    // ----------------------------------------------------
    fun updateCurrency(symbol: String, code: String) {
        viewModelScope.launch {
            val updated = _settings.value.copy(currencySymbol = symbol, currencyCode = code)
            repository.updateSettings(updated)
            _snackbarMessage.value = "Currency set to $symbol ($code)"
        }
    }

    fun toggleRollover(enabled: Boolean) {
        viewModelScope.launch {
            val updated = _settings.value.copy(rolloverEnabled = enabled)
            repository.updateSettings(updated)
            _snackbarMessage.value = if (enabled) "Rollover enabled for new months" else "Rollover disabled"
        }
    }

    fun setAppLock(enabled: Boolean, pin: String) {
        viewModelScope.launch {
            val updated = _settings.value.copy(isLockEnabled = enabled, appLockPin = pin)
            repository.updateSettings(updated)
            _isAppLocked.value = enabled && pin.isNotEmpty()
            _snackbarMessage.value = if (enabled) "App PIN lock enabled" else "App lock disabled"
        }
    }

    fun unlockApp(pin: String): Boolean {
        if (pin == _settings.value.appLockPin) {
            _isAppLocked.value = false
            return true
        }
        return false
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        val isDark = when (mode) {
            "LIGHT" -> false
            "DARK", "MIDNIGHT", "OLED" -> true
            else -> _isDarkTheme.value
        }
        _isDarkTheme.value = isDark
        viewModelScope.launch {
            repository.updateSettings(_settings.value.copy(themeMode = mode))
            val label = when (mode) {
                "LIGHT" -> "Apple Light"
                "DARK" -> "Apple Dark"
                "MIDNIGHT", "OLED" -> "Midnight OLED"
                else -> "System Default"
            }
            _snackbarMessage.value = "Theme switched to $label"
        }
    }

    fun toggleTheme() {
        val nextMode = when (_themeMode.value) {
            "LIGHT" -> "DARK"
            "DARK" -> "MIDNIGHT"
            "MIDNIGHT" -> "LIGHT"
            else -> if (_isDarkTheme.value) "LIGHT" else "DARK"
        }
        setThemeMode(nextMode)
    }

    fun setCurrencyToInr(useSymbol: Boolean = true) {
        val symbol = if (useSymbol) "₹" else "Rs."
        updateCurrency(symbol = symbol, code = "INR")
    }

    suspend fun exportBackupJson(): String = repository.exportFullBackupJson()

    suspend fun restoreBackupJson(json: String): Result<Int> {
        val res = repository.restoreFullBackupJson(json)
        refreshHistory()
        return res
    }

    // UI Dialog controls
    fun setShowEditIncomeDialog(show: Boolean) { _showEditIncomeDialog.value = show }
    fun setShowAddCustomCommonDialog(show: Boolean) { _showAddCustomCommonDialog.value = show }
    fun setSelectedTab(tab: Int) { _selectedTab.value = tab }
    fun clearSnackbar() { _snackbarMessage.value = null }
}
