package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AppSettingsEntity
import com.example.data.model.Budget
import com.example.data.model.BudgetCategory
import com.example.data.model.CategoryLimit
import com.example.data.model.CommonExpense
import com.example.data.model.Expense
import com.example.data.model.IncomeEntry
import com.example.data.model.TodoItem
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE monthYear = :monthYear LIMIT 1")
    fun getBudgetForMonthFlow(monthYear: String): Flow<Budget?>

    @Query("SELECT * FROM budgets WHERE monthYear = :monthYear LIMIT 1")
    suspend fun getBudgetForMonth(monthYear: String): Budget?

    @Query("SELECT * FROM budgets ORDER BY monthYear DESC")
    fun getAllBudgetsFlow(): Flow<List<Budget>>

    @Query("SELECT * FROM budgets ORDER BY monthYear DESC")
    suspend fun getAllBudgets(): List<Budget>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(budget: Budget): Long

    @Query("UPDATE budgets SET monthlyIncomeCents = :incomeCents WHERE monthYear = :monthYear")
    suspend fun updateMonthlyIncome(monthYear: String, incomeCents: Long)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE monthYear = :monthYear ORDER BY createdAt DESC")
    fun getExpensesForMonthFlow(monthYear: String): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE monthYear = :monthYear ORDER BY createdAt DESC")
    suspend fun getExpensesForMonth(monthYear: String): List<Expense>

    @Query("SELECT * FROM expenses ORDER BY createdAt DESC")
    fun getAllExpensesFlow(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses ORDER BY createdAt DESC")
    suspend fun getAllExpenses(): List<Expense>

    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY createdAt DESC")
    fun getExpensesByCategoryFlow(category: String): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE category = :category AND monthYear = :monthYear ORDER BY createdAt DESC")
    fun getExpensesByCategoryAndMonthFlow(category: String, monthYear: String): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE isRecurring = 1")
    suspend fun getRecurringExpenses(): List<Expense>

    @Query("SELECT * FROM expenses WHERE description LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchExpensesFlow(query: String): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE description = :description ORDER BY createdAt DESC LIMIT 1")
    suspend fun findLatestExpenseByDescription(description: String): Expense?

    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    suspend fun getExpenseById(id: Long): Expense?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expenses: List<Expense>)

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM expenses")
    suspend fun deleteAll()
}

@Dao
interface IncomeDao {
    @Query("SELECT * FROM income_entries WHERE monthYear = :monthYear ORDER BY createdAt DESC")
    fun getIncomeForMonthFlow(monthYear: String): Flow<List<IncomeEntry>>

    @Query("SELECT * FROM income_entries WHERE monthYear = :monthYear ORDER BY createdAt DESC")
    suspend fun getIncomeForMonth(monthYear: String): List<IncomeEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(income: IncomeEntry): Long

    @Update
    suspend fun update(income: IncomeEntry)

    @Delete
    suspend fun delete(income: IncomeEntry)

    @Query("DELETE FROM income_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM income_entries WHERE id = :id LIMIT 1")
    suspend fun getIncomeById(id: Long): IncomeEntry?
}

@Dao
interface CommonExpenseDao {
    @Query("SELECT * FROM common_expenses ORDER BY name ASC")
    fun getAllCommonExpensesFlow(): Flow<List<CommonExpense>>

    @Query("SELECT * FROM common_expenses ORDER BY name ASC")
    suspend fun getAllCommonExpenses(): List<CommonExpense>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(commonExpense: CommonExpense): Long

    @Update
    suspend fun update(commonExpense: CommonExpense)

    @Query("DELETE FROM common_expenses WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM common_expenses WHERE id = :id LIMIT 1")
    suspend fun getCommonExpenseById(id: Long): CommonExpense?
}

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY createdAt DESC")
    fun getAllTodosFlow(): Flow<List<TodoItem>>

    @Query("SELECT * FROM todos ORDER BY createdAt DESC")
    suspend fun getAllTodos(): List<TodoItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todo: TodoItem): Long

    @Update
    suspend fun update(todo: TodoItem)

    @Delete
    suspend fun delete(todo: TodoItem)

    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM todos WHERE id = :id LIMIT 1")
    suspend fun getTodoById(id: Long): TodoItem?
}

@Dao
interface CategoryLimitDao {
    @Query("SELECT * FROM category_limits")
    fun getAllLimitsFlow(): Flow<List<CategoryLimit>>

    @Query("SELECT * FROM category_limits")
    suspend fun getAllLimits(): List<CategoryLimit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(limit: CategoryLimit)

    @Query("DELETE FROM category_limits WHERE category = :category")
    suspend fun deleteByCategory(category: String)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: AppSettingsEntity)
}

@Dao
interface BudgetCategoryDao {
    @Query("SELECT * FROM budget_categories ORDER BY isDefault DESC, name ASC")
    fun getAllCategoriesFlow(): Flow<List<BudgetCategory>>

    @Query("SELECT * FROM budget_categories ORDER BY isDefault DESC, name ASC")
    suspend fun getAllCategories(): List<BudgetCategory>

    @Query("SELECT * FROM budget_categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: Long): BudgetCategory?

    @Query("SELECT * FROM budget_categories WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): BudgetCategory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: BudgetCategory): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<BudgetCategory>)

    @Update
    suspend fun update(category: BudgetCategory)

    @Delete
    suspend fun delete(category: BudgetCategory)

    @Query("DELETE FROM budget_categories WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM budget_categories WHERE isDefault = 0")
    suspend fun deleteCustomCategories()
}
