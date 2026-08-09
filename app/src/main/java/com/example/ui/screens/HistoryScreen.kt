package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Expense
import com.example.ui.components.AddExpenseBottomSheet
import com.example.ui.components.CategoryBadge
import com.example.ui.components.CategoryIcon
import com.example.ui.components.formatDateHeader
import com.example.ui.components.formatRupee
import com.example.ui.theme.GreenPrimary
import com.example.ui.viewmodel.HistoryFilter
import com.example.ui.viewmodel.MoneyViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(viewModel: MoneyViewModel) {
    val filteredExpenses by viewModel.filteredExpenses.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    var editingExpense by remember { mutableStateOf<Expense?>(null) }
    var deletingExpense by remember { mutableStateOf<Expense?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val context = LocalContext.current

    // Custom Date Range Dialog logic
    var showDateRangeDialog by remember { mutableStateOf(false) }

    // Group expenses by formatted date string
    val groupedExpenses = remember(filteredExpenses) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        filteredExpenses.groupBy { dateFormat.format(Date(it.date)) }
    }

    val totalAmount = remember(filteredExpenses) { filteredExpenses.sumOf { it.amount } }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Expense History",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search by name, category, or date...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                } else null,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input"),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == HistoryFilter.TODAY,
                        onClick = { viewModel.setFilter(HistoryFilter.TODAY) },
                        label = { Text("Today") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = GreenPrimary
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == HistoryFilter.YESTERDAY,
                        onClick = { viewModel.setFilter(HistoryFilter.YESTERDAY) },
                        label = { Text("Yesterday") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = GreenPrimary
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == HistoryFilter.LAST_7_DAYS,
                        onClick = { viewModel.setFilter(HistoryFilter.LAST_7_DAYS) },
                        label = { Text("Last 7 Days") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = GreenPrimary
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == HistoryFilter.THIS_MONTH,
                        onClick = { viewModel.setFilter(HistoryFilter.THIS_MONTH) },
                        label = { Text("This Month") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = GreenPrimary
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == HistoryFilter.CUSTOM,
                        onClick = { showDateRangeDialog = true },
                        label = { Text("Custom...") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = GreenPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Result Display Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredExpenses.size} expenses found",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Total: ${formatRupee(totalAmount)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = GreenPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredExpenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Nothing here yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No expenses found for this filter.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    groupedExpenses.forEach { (dateKey, itemsInGroup) ->
                        val dateMillis = itemsInGroup.firstOrNull()?.date ?: System.currentTimeMillis()

                        item(key = "header_$dateKey") {
                            Text(
                                text = formatDateHeader(dateMillis),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = GreenPrimary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }

                        items(itemsInGroup, key = { it.id }) { expense ->
                            var showItemMenu by remember { mutableStateOf(false) }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = { editingExpense = expense },
                                        onLongClick = { showItemMenu = true }
                                    ),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Box {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CategoryIcon(category = expense.category, size = 44.dp)

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = expense.name,
                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                CategoryBadge(category = expense.category)
                                                if (!expense.note.isNull_or_empty()) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "• ${expense.note}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = formatRupee(expense.amount),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showItemMenu,
                                        onDismissRequest = { showItemMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Edit") },
                                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                            onClick = {
                                                showItemMenu = false
                                                editingExpense = expense
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Delete") },
                                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                            onClick = {
                                                showItemMenu = false
                                                deletingExpense = expense
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        // Edit Expense Sheet
        if (editingExpense != null) {
            AddExpenseBottomSheet(
                sheetState = sheetState,
                initialExpense = editingExpense,
                onDismiss = { editingExpense = null },
                onSave = { name, amount, category, dateMillis, note ->
                    val updated = editingExpense!!.copy(
                        name = name,
                        amount = amount,
                        category = category,
                        date = dateMillis,
                        note = note
                    )
                    viewModel.updateExpense(updated)
                    editingExpense = null
                }
            )
        }

        // Delete Confirmation Dialog
        if (deletingExpense != null) {
            AlertDialog(
                onDismissRequest = { deletingExpense = null },
                title = { Text("Delete Expense?") },
                text = { Text("Are you sure you want to delete '${deletingExpense!!.name}' (${formatRupee(deletingExpense!!.amount)})?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteExpense(deletingExpense!!)
                            deletingExpense = null
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deletingExpense = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Custom Date Range Picker Dialog
        if (showDateRangeDialog) {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    cal.set(year, month, dayOfMonth, 0, 0, 0)
                    val startMillis = cal.timeInMillis
                    cal.set(year, month, dayOfMonth, 23, 59, 59)
                    val endMillis = cal.timeInMillis
                    viewModel.setCustomDateRange(startMillis, endMillis)
                    showDateRangeDialog = false
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }
}

private fun CharSequence?.isNull_or_empty(): Boolean {
    return this == null || this.isEmpty()
}
