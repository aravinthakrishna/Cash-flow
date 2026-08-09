package com.example.util

import com.example.data.model.BudgetSetting
import com.example.data.model.Expense
import com.example.data.model.LoggedChecklistEvent
import com.example.data.model.RecurringItem
import com.example.data.model.UserPreferenceEntity
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvReportUtil {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun generateCsv(expenses: List<Expense>): String {
        val sb = StringBuilder()
        sb.append("ID,Date,Name,Category,Amount(INR),Note\n")
        for (e in expenses) {
            val dateStr = dateFormat.format(Date(e.date))
            val cleanName = escapeCsv(e.name)
            val cleanCat = escapeCsv(e.category)
            val cleanNote = escapeCsv(e.note ?: "")
            sb.append("${e.id},\"$dateStr\",\"$cleanName\",\"$cleanCat\",${e.amount},\"$cleanNote\"\n")
        }
        return sb.toString()
    }

    fun generateMonthlyReportText(
        monthName: String,
        expenses: List<Expense>,
        monthlyBudget: Double
    ): String {
        val totalSpent = expenses.sumOf { it.amount }
        val remaining = monthlyBudget - totalSpent
        val count = expenses.size

        val categoryMap = expenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }

        val sb = StringBuilder()
        sb.append("=========================================\n")
        sb.append("   PERSONAL MONEY MANAGER - MONTHLY REPORT\n")
        sb.append("   Period: $monthName\n")
        sb.append("=========================================\n\n")

        sb.append("SUMMARY:\n")
        sb.append("- Total Expenses Logged: $count\n")
        sb.append("- Total Amount Spent: ₹${String.format(Locale.US, "%.2f", totalSpent)}\n")
        sb.append("- Monthly Budget: ₹${String.format(Locale.US, "%.2f", monthlyBudget)}\n")
        sb.append("- Budget Remaining: ₹${String.format(Locale.US, "%.2f", remaining)}\n\n")

        sb.append("SPENDING BY CATEGORY:\n")
        if (categoryMap.isEmpty()) {
            sb.append("  No expenses logged for this period.\n")
        } else {
            for ((cat, amount) in categoryMap) {
                val pct = if (totalSpent > 0) (amount / totalSpent) * 100 else 0.0
                sb.append("  - $cat: ₹${String.format(Locale.US, "%.2f", amount)} (${String.format(Locale.US, "%.1f", pct)}%)\n")
            }
        }

        sb.append("\nEXPENSE LOG:\n")
        for (e in expenses) {
            val dateStr = dateFormat.format(Date(e.date))
            val noteStr = if (!e.note.isNull_or_empty()) " (${e.note})" else ""
            sb.append("  [$dateStr] ${e.name} - ₹${String.format(Locale.US, "%.2f", e.amount)} [$e.category]$noteStr\n")
        }

        sb.append("\n=========================================\n")
        sb.append("Report generated on ${dateFormat.format(Date())} via Personal Money Manager\n")
        return sb.toString()
    }

    fun generateBackupJson(
        expenses: List<Expense>,
        recurringItems: List<RecurringItem>,
        loggedEvents: List<LoggedChecklistEvent>,
        budget: BudgetSetting?,
        prefs: UserPreferenceEntity?
    ): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("timestamp", System.currentTimeMillis())

        // Expenses array
        val expArray = JSONArray()
        for (e in expenses) {
            val item = JSONObject()
            item.put("id", e.id)
            item.put("name", e.name)
            item.put("amount", e.amount)
            item.put("category", e.category)
            item.put("date", e.date)
            item.put("note", e.note ?: "")
            expArray.put(item)
        }
        root.put("expenses", expArray)

        // Recurring items array
        val recArray = JSONArray()
        for (r in recurringItems) {
            val item = JSONObject()
            item.put("id", r.id)
            item.put("name", r.name)
            item.put("amount", r.amount)
            item.put("category", r.category)
            item.put("iconKey", r.iconKey)
            recArray.put(item)
        }
        root.put("recurringItems", recArray)

        // Logged events array
        val evtArray = JSONArray()
        for (ev in loggedEvents) {
            val item = JSONObject()
            item.put("id", ev.id)
            item.put("checklistId", ev.checklistId)
            item.put("dateString", ev.dateString)
            item.put("expenseId", ev.expenseId)
            evtArray.put(item)
        }
        root.put("loggedEvents", evtArray)

        // Budget
        val budgetObj = JSONObject()
        budgetObj.put("monthlyBudget", budget?.monthlyBudget ?: 15000.0)
        root.put("budget", budgetObj)

        // Preferences
        val prefObj = JSONObject()
        prefObj.put("darkModeEnabled", prefs?.darkModeEnabled ?: false)
        prefObj.put("trackingStreakCount", prefs?.trackingStreakCount ?: 1)
        prefObj.put("lastActiveDate", prefs?.lastActiveDate ?: "")
        root.put("preferences", prefObj)

        return root.toString(2)
    }

    fun parseBackupJson(jsonString: String): BackupData? {
        return try {
            val root = JSONObject(jsonString)

            val expenses = mutableListOf<Expense>()
            if (root.has("expenses")) {
                val expArray = root.getJSONArray("expenses")
                for (i in 0 until expArray.length()) {
                    val obj = expArray.getJSONObject(i)
                    expenses.add(
                        Expense(
                            id = obj.optLong("id", 0),
                            name = obj.getString("name"),
                            amount = obj.getDouble("amount"),
                            category = obj.getString("category"),
                            date = obj.getLong("date"),
                            note = obj.optString("note").ifEmpty { null }
                        )
                    )
                }
            }

            val recurringItems = mutableListOf<RecurringItem>()
            if (root.has("recurringItems")) {
                val recArray = root.getJSONArray("recurringItems")
                for (i in 0 until recArray.length()) {
                    val obj = recArray.getJSONObject(i)
                    recurringItems.add(
                        RecurringItem(
                            id = obj.optLong("id", 0),
                            name = obj.getString("name"),
                            amount = obj.getDouble("amount"),
                            category = obj.getString("category"),
                            iconKey = obj.optString("iconKey", "default")
                        )
                    )
                }
            }

            val loggedEvents = mutableListOf<LoggedChecklistEvent>()
            if (root.has("loggedEvents")) {
                val evtArray = root.getJSONArray("loggedEvents")
                for (i in 0 until evtArray.length()) {
                    val obj = evtArray.getJSONObject(i)
                    loggedEvents.add(
                        LoggedChecklistEvent(
                            id = obj.optLong("id", 0),
                            checklistId = obj.getLong("checklistId"),
                            dateString = obj.getString("dateString"),
                            expenseId = obj.getLong("expenseId")
                        )
                    )
                }
            }

            var budget: BudgetSetting? = null
            if (root.has("budget")) {
                val bObj = root.getJSONObject("budget")
                budget = BudgetSetting(id = 1, monthlyBudget = bObj.getDouble("monthlyBudget"))
            }

            var prefs: UserPreferenceEntity? = null
            if (root.has("preferences")) {
                val pObj = root.getJSONObject("preferences")
                prefs = UserPreferenceEntity(
                    id = 1,
                    darkModeEnabled = pObj.optBoolean("darkModeEnabled", false),
                    trackingStreakCount = pObj.optInt("trackingStreakCount", 1),
                    lastActiveDate = pObj.optString("lastActiveDate", "")
                )
            }

            BackupData(expenses, recurringItems, loggedEvents, budget, prefs)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun CharSequence?.isNull_or_empty(): Boolean {
        return this == null || this.isEmpty()
    }

    private fun escapeCsv(value: String): String {
        return value.replace("\"", "\"\"")
    }

    data class BackupData(
        val expenses: List<Expense>,
        val recurringItems: List<RecurringItem>,
        val loggedEvents: List<LoggedChecklistEvent>,
        val budget: BudgetSetting?,
        val preferences: UserPreferenceEntity?
    )
}
