package com.example.data.repository

import com.example.data.AppDatabase
import com.example.data.model.BudgetSetting
import com.example.data.model.Expense
import com.example.data.model.LoggedChecklistEvent
import com.example.data.model.RecurringItem
import com.example.data.model.UserPreferenceEntity
import com.example.util.CsvReportUtil
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MoneyRepository(private val db: AppDatabase) {

    private val expenseDao = db.expenseDao()
    private val recurringItemDao = db.recurringItemDao()
    private val budgetDao = db.budgetDao()
    private val preferencesDao = db.preferencesDao()

    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()
    val allRecurringItems: Flow<List<RecurringItem>> = recurringItemDao.getAllRecurringItems()
    val budgetSetting: Flow<BudgetSetting?> = budgetDao.getBudgetSetting()
    val userPreferences: Flow<UserPreferenceEntity?> = preferencesDao.getUserPreferences()
    val expenseCount: Flow<Int> = expenseDao.getExpenseCount()

    fun getLoggedEventsForDate(dateString: String): Flow<List<LoggedChecklistEvent>> {
        return recurringItemDao.getLoggedEventsForDate(dateString)
    }

    suspend fun insertExpensesBulk(expenses: List<Expense>) {
        expenseDao.insertAll(expenses)
        updateStreak()
    }

    suspend fun insertRecurringItemsBulk(items: List<RecurringItem>) {
        recurringItemDao.insertAllItems(items)
    }

    suspend fun insertExpense(expense: Expense): Long {
        val id = expenseDao.insertExpense(expense)
        updateStreak()
        return id
    }

    suspend fun updateExpense(expense: Expense) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun toggleRecurringItemForToday(item: RecurringItem, todayDateString: String, todayMillis: Long) {
        val existingEvent = recurringItemDao.getLoggedEvent(item.id, todayDateString)
        if (existingEvent != null) {
            // Already logged today: delete expense and event
            expenseDao.deleteExpenseById(existingEvent.expenseId)
            recurringItemDao.deleteLoggedEvent(item.id, todayDateString)
        } else {
            // Log new expense for today
            val newExpense = Expense(
                name = item.name,
                amount = item.amount,
                category = item.category,
                date = todayMillis,
                note = "Daily checklist",
                recurringItemId = item.id
            )
            val expenseId = expenseDao.insertExpense(newExpense)
            recurringItemDao.logChecklistEvent(
                LoggedChecklistEvent(
                    checklistId = item.id,
                    dateString = todayDateString,
                    expenseId = expenseId
                )
            )
            updateStreak()
        }
    }

    suspend fun insertRecurringItem(item: RecurringItem): Long {
        return recurringItemDao.insertRecurringItem(item)
    }

    suspend fun updateRecurringItem(item: RecurringItem) {
        recurringItemDao.updateRecurringItem(item)
    }

    suspend fun deleteRecurringItem(item: RecurringItem) {
        recurringItemDao.deleteRecurringItem(item)
    }

    suspend fun setMonthlyBudget(amount: Double) {
        budgetDao.setBudgetSetting(BudgetSetting(id = 1, monthlyBudget = amount))
    }

    suspend fun setDarkModeEnabled(enabled: Boolean) {
        val current = preferencesDao.getUserPreferencesDirect()
            ?: UserPreferenceEntity(id = 1, darkModeEnabled = enabled)
        preferencesDao.setUserPreferences(current.copy(darkModeEnabled = enabled))
    }

    private suspend fun updateStreak() {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val current = preferencesDao.getUserPreferencesDirect()
            ?: UserPreferenceEntity(id = 1, trackingStreakCount = 1, lastActiveDate = todayStr)

        if (current.lastActiveDate.isEmpty()) {
            preferencesDao.setUserPreferences(current.copy(trackingStreakCount = 1, lastActiveDate = todayStr))
        } else if (current.lastActiveDate != todayStr) {
            // Calculate days difference
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val lastDate = sdf.parse(current.lastActiveDate)
                val todayDate = sdf.parse(todayStr)
                if (lastDate != null && todayDate != null) {
                    val diff = (todayDate.time - lastDate.time) / (1000 * 60 * 60 * 24)
                    val newStreak = if (diff == 1L) current.trackingStreakCount + 1 else 1
                    preferencesDao.setUserPreferences(
                        current.copy(trackingStreakCount = newStreak, lastActiveDate = todayStr)
                    )
                }
            } catch (e: Exception) {
                preferencesDao.setUserPreferences(
                    current.copy(trackingStreakCount = 1, lastActiveDate = todayStr)
                )
            }
        }
    }

    suspend fun restoreData(backup: CsvReportUtil.BackupData) {
        expenseDao.deleteAllExpenses()
        recurringItemDao.deleteAllRecurringItems()
        recurringItemDao.deleteAllLoggedEvents()

        if (backup.expenses.isNotEmpty()) {
            expenseDao.insertAll(backup.expenses)
        }
        if (backup.recurringItems.isNotEmpty()) {
            recurringItemDao.insertAllItems(backup.recurringItems)
        }
        if (backup.loggedEvents.isNotEmpty()) {
            recurringItemDao.insertAllEvents(backup.loggedEvents)
        }
        backup.budget?.let { budgetDao.setBudgetSetting(it) }
        backup.preferences?.let { preferencesDao.setUserPreferences(it) }
    }
}
