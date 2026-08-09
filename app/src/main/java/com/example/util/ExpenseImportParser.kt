package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

data class ParsedImportItem(
    val dateMillis: Long,
    val dateDisplay: String,
    val name: String,
    val category: String,
    val amount: Double
)

data class ImportResult(
    val items: List<ParsedImportItem>,
    val skippedCount: Int,
    val totalAmount: Double
)

object ExpenseImportParser {

    private val dateFormats = listOf(
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()),
        SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    )

    private val categories = listOf("Food", "Transport", "Shopping", "Bills", "Entertainment", "Health", "Other")

    fun parseText(rawInput: String): ImportResult {
        val lines = rawInput.lines()
        val parsedList = mutableListOf<ParsedImportItem>()
        var skipped = 0

        val datePattern = Pattern.compile("(\\d{4}[-/]\\d{1,2}[-/]\\d{1,2}|\\d{1,2}[-/]\\d{1,2}[-/]\\d{4})")
        val numberPattern = Pattern.compile("₹?\\s*(\\d+(?:\\.\\d{1,2})?)")

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.lowercase().startsWith("date,") || trimmed.lowercase().startsWith("id,")) {
                continue
            }

            // Split by comma, tab, pipe, or multiple spaces
            val tokens = trimmed.split(Regex("[,\\t|]")).map { it.trim().removeSurrounding("\"") }.filter { it.isNotEmpty() }

            if (tokens.isEmpty()) {
                skipped++
                continue
            }

            var foundDateMillis = System.currentTimeMillis()
            var foundDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            var foundName = ""
            var foundCategory = "Other"
            var foundAmount: Double? = null

            val remainingTokens = mutableListOf<String>()

            for (token in tokens) {
                // Try parsing date
                val dateMatcher = datePattern.matcher(token)
                if (dateMatcher.find() && foundName.isEmpty()) {
                    val rawDateStr = dateMatcher.group(1)
                    val parsedDate = parseDateString(rawDateStr)
                    if (parsedDate != null) {
                        foundDateMillis = parsedDate.time
                        foundDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(parsedDate)
                        continue
                    }
                }

                // Check category match
                val matchedCategory = categories.firstOrNull { it.equals(token, ignoreCase = true) }
                if (matchedCategory != null && foundCategory == "Other") {
                    foundCategory = matchedCategory
                    continue
                }

                // Try parsing amount
                val cleanedNumStr = token.replace("₹", "").replace(",", "").trim()
                val parsedNum = cleanedNumStr.toDoubleOrNull()
                if (parsedNum != null && parsedNum > 0 && foundAmount == null) {
                    foundAmount = parsedNum
                    continue
                }

                remainingTokens.add(token)
            }

            // If name was not set, use remaining tokens
            if (remainingTokens.isNotEmpty()) {
                foundName = remainingTokens.joinToString(" ")
            }

            // If name or category was not found from token split, try regex on whole line
            if (foundAmount == null) {
                val numMatcher = numberPattern.matcher(trimmed)
                if (numMatcher.find()) {
                    foundAmount = numMatcher.group(1)?.toDoubleOrNull()
                }
            }

            if (foundName.isNotBlank() && foundAmount != null && foundAmount > 0) {
                // Auto infer category from name if category was default
                if (foundCategory == "Other") {
                    foundCategory = inferCategoryByName(foundName)
                }

                parsedList.add(
                    ParsedImportItem(
                        dateMillis = foundDateMillis,
                        dateDisplay = foundDateStr,
                        name = foundName.take(50),
                        category = foundCategory,
                        amount = foundAmount
                    )
                )
            } else {
                skipped++
            }
        }

        val totalAmt = parsedList.sumOf { it.amount }
        return ImportResult(parsedList, skipped, totalAmt)
    }

    private fun parseDateString(str: String): Date? {
        for (format in dateFormats) {
            try {
                return format.parse(str)
            } catch (_: Exception) {}
        }
        return null
    }

    private fun inferCategoryByName(name: String): String {
        val lower = name.lowercase()
        return when {
            lower.contains("breakfast") || lower.contains("lunch") || lower.contains("dinner") ||
            lower.contains("coffee") || lower.contains("tea") || lower.contains("food") ||
            lower.contains("restaurant") || lower.contains("snack") || lower.contains("pizza") || lower.contains("burger") -> "Food"

            lower.contains("bus") || lower.contains("auto") || lower.contains("cab") ||
            lower.contains("uber") || lower.contains("ola") || lower.contains("train") ||
            lower.contains("metro") || lower.contains("petrol") || lower.contains("fuel") -> "Transport"

            lower.contains("bill") || lower.contains("recharge") || lower.contains("rent") ||
            lower.contains("electricity") || lower.contains("wifi") || lower.contains("water") -> "Bills"

            lower.contains("movie") || lower.contains("game") || lower.contains("netflix") ||
            lower.contains("spotify") || lower.contains("cinema") -> "Entertainment"

            lower.contains("doctor") || lower.contains("medicine") || lower.contains("pharmacy") ||
            lower.contains("hospital") || lower.contains("clinic") -> "Health"

            lower.contains("dress") || lower.contains("shirt") || lower.contains("shoes") ||
            lower.contains("amazon") || lower.contains("flipkart") || lower.contains("mall") -> "Shopping"

            else -> "Other"
        }
    }
}
