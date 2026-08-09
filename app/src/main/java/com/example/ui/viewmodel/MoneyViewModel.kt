package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.Expense
import com.example.data.model.LoggedChecklistEvent
import com.example.data.model.RecurringItem
import com.example.data.model.UserPreferenceEntity
import com.example.data.repository.MoneyRepository
import com.example.util.CsvReportUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class HistoryFilter {
    TODAY, YESTERDAY, LAST_7_DAYS, THIS_MONTH, CUSTOM
}

class MoneyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MoneyRepository = MoneyRepository(AppDatabase.getDatabase(application))

    val allExpenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecurringItems: StateFlow<List<RecurringItem>> = repository.allRecurringItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyBudget: StateFlow<Double> = repository.budgetSetting
        .combine(MutableStateFlow(15000.0)) { setting, default ->
            setting?.monthlyBudget ?: default
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 15000.0)

    val userPreferences: StateFlow<UserPreferenceEntity?> = repository.userPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Today's date string formatted YYYY-MM-DD
    private val todayDateString: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val loggedEventsToday: StateFlow<List<LoggedChecklistEvent>> = repository.getLoggedEventsForDate(todayDateString)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // History Filters State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(HistoryFilter.THIS_MONTH)
    val selectedFilter: StateFlow<HistoryFilter> = _selectedFilter.asStateFlow()

    private val _customDateRange = MutableStateFlow<Pair<Long, Long>?>(null)
    val customDateRange: StateFlow<Pair<Long, Long>?> = _customDateRange.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: HistoryFilter) {
        _selectedFilter.value = filter
    }

    fun setCustomDateRange(startMillis: Long, endMillis: Long) {
        _customDateRange.value = Pair(startMillis, endMillis)
        _selectedFilter.value = HistoryFilter.CUSTOM
    }

    // Filtered Expenses Flow for History Screen
    val filteredExpenses: StateFlow<List<Expense>> = combine(
        allExpenses,
        _searchQuery,
        _selectedFilter,
        _customDateRange
    ) { expenses, query, filter, customRange ->
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val startOfToday = cal.timeInMillis

        cal.add(Calendar.DAY_OF_YEAR, -1)
        val startOfYesterday = cal.timeInMillis

        cal.timeInMillis = startOfToday
        cal.add(Calendar.DAY_OF_YEAR, -6) // 7 days inclusive
        val startOf7DaysAgo = cal.timeInMillis

        cal.timeInMillis = System.currentTimeMillis()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfThisMonth = cal.timeInMillis

        val dateFiltered = when (filter) {
            HistoryFilter.TODAY -> expenses.filter { it.date >= startOfToday }
            HistoryFilter.YESTERDAY -> expenses.filter { it.date >= startOfYesterday && it.date < startOfToday }
            HistoryFilter.LAST_7_DAYS -> expenses.filter { it.date >= startOf7DaysAgo }
            HistoryFilter.THIS_MONTH -> expenses.filter { it.date >= startOfThisMonth }
            HistoryFilter.CUSTOM -> {
                if (customRange != null) {
                    expenses.filter { it.date >= customRange.first && it.date <= customRange.second }
                } else {
                    expenses
                }
            }
        }

        if (query.trim().isEmpty()) {
            dateFiltered
        } else {
            val q = query.lowercase().trim()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFiltered.filter { exp ->
                exp.name.lowercase().contains(q) ||
                exp.category.lowercase().contains(q) ||
                dateFormat.format(Date(exp.date)).contains(q) ||
                (exp.note?.lowercase()?.contains(q) == true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Actions
    fun addExpense(name: String, amount: Double, category: String, dateMillis: Long, note: String?) {
        viewModelScope.launch {
            repository.insertExpense(
                Expense(
                    name = name,
                    amount = amount,
                    category = category,
                    date = dateMillis,
                    note = note
                )
            )
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            repository.updateExpense(expense)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun toggleChecklistForToday(item: RecurringItem) {
        viewModelScope.launch {
            repository.toggleRecurringItemForToday(item, todayDateString, System.currentTimeMillis())
        }
    }

    fun setBudget(amount: Double) {
        viewModelScope.launch {
            repository.setMonthlyBudget(amount)
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDarkModeEnabled(enabled)
        }
    }

    fun addRecurringItem(name: String, amount: Double, category: String, iconKey: String) {
        viewModelScope.launch {
            repository.insertRecurringItem(
                RecurringItem(
                    name = name,
                    amount = amount,
                    category = category,
                    iconKey = iconKey
                )
            )
        }
    }

    fun updateRecurringItem(item: RecurringItem) {
        viewModelScope.launch {
            repository.updateRecurringItem(item)
        }
    }

    fun deleteRecurringItem(item: RecurringItem) {
        viewModelScope.launch {
            repository.deleteRecurringItem(item)
        }
    }

    fun importExpenses(items: List<com.example.util.ParsedImportItem>, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val expenseEntities = items.map { item ->
                Expense(
                    name = item.name,
                    amount = item.amount,
                    category = item.category,
                    date = item.dateMillis,
                    note = "Imported"
                )
            }
            repository.insertExpensesBulk(expenseEntities)
            onComplete(expenseEntities.size)
        }
    }

    fun addRecurringItemsBulk(items: List<RecurringItem>) {
        viewModelScope.launch {
            repository.insertRecurringItemsBulk(items)
        }
    }

    fun getExportCsv(): String {
        return CsvReportUtil.generateCsv(allExpenses.value)
    }

    fun getMonthlyReportText(): String {
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val monthName = monthFormat.format(Date())
        return CsvReportUtil.generateMonthlyReportText(
            monthName = monthName,
            expenses = allExpenses.value,
            monthlyBudget = monthlyBudget.value
        )
    }
}
