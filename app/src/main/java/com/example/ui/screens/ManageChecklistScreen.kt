package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RecurringItem
import com.example.ui.components.CategoryBadge
import com.example.ui.components.CategoryIcon
import com.example.ui.components.formatRupee
import com.example.ui.theme.GreenPrimary
import com.example.ui.viewmodel.MoneyViewModel

val iconKeysList = listOf("breakfast", "tea", "bus", "lunch", "snacks", "dinner", "default")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageChecklistScreen(
    viewModel: MoneyViewModel,
    onBack: () -> Unit
) {
    val recurringItems by viewModel.allRecurringItems.collectAsState()

    var editingItem by remember { mutableStateOf<RecurringItem?>(null) }
    var isAddingNew by remember { mutableStateOf(false) }
    var deletingItem by remember { mutableStateOf<RecurringItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Daily Checklist", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddingNew = true },
                containerColor = GreenPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_recurring_item_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Recurring Item")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Clarification Banner Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = GreenPrimary.copy(alpha = 0.08f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = GreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "These items reset every day — check them off as you spend.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Single + Add Item Button
            Button(
                onClick = { isAddingNew = true },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("+ Add Item", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (recurringItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ListAlt,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No recurring items",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap '+ Add Item' above to get started!",
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
                    items(recurringItems, key = { it.id }) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CategoryIcon(category = item.category, iconKey = item.iconKey, size = 44.dp)

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    CategoryBadge(category = item.category)
                                }

                                Text(
                                    text = formatRupee(item.amount),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = GreenPrimary
                                )

                                IconButton(onClick = { editingItem = item }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                IconButton(onClick = { deletingItem = item }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        // Add / Edit Dialog
        if (isAddingNew || editingItem != null) {
            RecurringItemDialog(
                initialItem = editingItem,
                onDismiss = {
                    isAddingNew = false
                    editingItem = null
                },
                onSave = { name, amount, category, iconKey ->
                    if (editingItem != null) {
                        viewModel.updateRecurringItem(editingItem!!.copy(name = name, amount = amount, category = category, iconKey = iconKey))
                    } else {
                        viewModel.addRecurringItem(name, amount, category, iconKey)
                    }
                    isAddingNew = false
                    editingItem = null
                }
            )
        }

        // Delete Dialog
        if (deletingItem != null) {
            AlertDialog(
                onDismissRequest = { deletingItem = null },
                title = { Text("Delete Recurring Item?") },
                text = { Text("Are you sure you want to remove '${deletingItem!!.name}' from your daily checklist?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteRecurringItem(deletingItem!!)
                            deletingItem = null
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deletingItem = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun QuickBulkAddDialog(
    onDismiss: () -> Unit,
    onAddBulk: (List<RecurringItem>) -> Unit
) {
    var bulkText by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quick Bulk Add Items") },
        text = {
            Column {
                Text(
                    text = "Enter multiple items (one per line) in format:\nname, amount, category\n\nExample:\nBreakfast, 50, Food\nBus Pass, 40, Transport\nTea, 20, Food",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = bulkText,
                    onValueChange = {
                        bulkText = it
                        errorText = null
                    },
                    placeholder = { Text("Breakfast, 50, Food\nBus Pass, 40, Transport") },
                    minLines = 5,
                    maxLines = 8,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorText != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(errorText!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lines = bulkText.lines().map { it.trim() }.filter { it.isNotEmpty() }
                    val itemsToAdd = mutableListOf<RecurringItem>()

                    for (line in lines) {
                        val tokens = line.split(",").map { it.trim() }
                        if (tokens.isNotEmpty()) {
                            val name = tokens[0]
                            val amount = tokens.getOrNull(1)?.toDoubleOrNull() ?: 0.0
                            val category = tokens.getOrNull(2)?.ifBlank { "Food" } ?: "Food"

                            if (name.isNotBlank() && amount > 0) {
                                val lower = name.lowercase()
                                val iconKey = when {
                                    lower.contains("breakfast") -> "breakfast"
                                    lower.contains("tea") || lower.contains("coffee") -> "tea"
                                    lower.contains("bus") || lower.contains("auto") -> "bus"
                                    lower.contains("lunch") -> "lunch"
                                    lower.contains("snack") -> "snacks"
                                    lower.contains("dinner") -> "dinner"
                                    else -> "default"
                                }
                                itemsToAdd.add(
                                    RecurringItem(
                                        name = name,
                                        amount = amount,
                                        category = category,
                                        iconKey = iconKey
                                    )
                                )
                            }
                        }
                    }

                    if (itemsToAdd.isEmpty()) {
                        errorText = "No valid lines found. Please check format: name, amount, category"
                    } else {
                        onAddBulk(itemsToAdd)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("Add All Items", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun StarterTemplatesDialog(
    onDismiss: () -> Unit,
    onSelectTemplate: (List<RecurringItem>) -> Unit
) {
    val templates = listOf(
        TemplateOption(
            title = "Student Starter",
            description = "Breakfast (₹40), Bus/Auto (₹30), Lunch (₹80), Snacks (₹20), Printouts (₹30)",
            items = listOf(
                RecurringItem(name = "Breakfast", amount = 40.0, category = "Food", iconKey = "breakfast"),
                RecurringItem(name = "Bus / Auto", amount = 30.0, category = "Transport", iconKey = "bus"),
                RecurringItem(name = "College Lunch", amount = 80.0, category = "Food", iconKey = "lunch"),
                RecurringItem(name = "Evening Tea & Snacks", amount = 20.0, category = "Food", iconKey = "snacks"),
                RecurringItem(name = "Printouts / Stationery", amount = 30.0, category = "Other", iconKey = "default")
            )
        ),
        TemplateOption(
            title = "Office Commuter",
            description = "Morning Coffee (₹30), Metro/Cab (₹60), Office Lunch (₹120), Evening Tea (₹20), Toll/Parking (₹40)",
            items = listOf(
                RecurringItem(name = "Morning Coffee", amount = 30.0, category = "Food", iconKey = "tea"),
                RecurringItem(name = "Metro / Cab", amount = 60.0, category = "Transport", iconKey = "bus"),
                RecurringItem(name = "Office Lunch", amount = 120.0, category = "Food", iconKey = "lunch"),
                RecurringItem(name = "Evening Tea", amount = 20.0, category = "Food", iconKey = "tea"),
                RecurringItem(name = "Parking / Toll", amount = 40.0, category = "Transport", iconKey = "default")
            )
        ),
        TemplateOption(
            title = "Family Daily Essentials",
            description = "Milk & Bread (₹60), Vegetables (₹150), Groceries (₹300), Utilities (₹50), Evening Snacks (₹80)",
            items = listOf(
                RecurringItem(name = "Milk & Bread", amount = 60.0, category = "Food", iconKey = "breakfast"),
                RecurringItem(name = "Daily Vegetables", amount = 150.0, category = "Food", iconKey = "default"),
                RecurringItem(name = "Groceries", amount = 300.0, category = "Shopping", iconKey = "default"),
                RecurringItem(name = "Utilities / Recharges", amount = 50.0, category = "Bills", iconKey = "default"),
                RecurringItem(name = "Family Evening Snacks", amount = 80.0, category = "Food", iconKey = "snacks")
            )
        )
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Starter Template") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Tap a template to add pre-configured daily recurring items in one tap:",
                    style = MaterialTheme.typography.bodySmall
                )

                templates.forEach { t ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectTemplate(t.items) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = t.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = GreenPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = t.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private data class TemplateOption(
    val title: String,
    val description: String,
    val items: List<RecurringItem>
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecurringItemDialog(
    initialItem: RecurringItem?,
    onDismiss: () -> Unit,
    onSave: (name: String, amount: Double, category: String, iconKey: String) -> Unit
) {
    var name by remember { mutableStateOf(initialItem?.name ?: "") }
    var amountText by remember { mutableStateOf(initialItem?.amount?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() } ?: "") }
    var selectedCategory by remember { mutableStateOf(initialItem?.category ?: "Food") }
    var selectedIconKey by remember { mutableStateOf(initialItem?.iconKey ?: "default") }
    var errorText by remember { mutableStateOf<String?>(null) }

    val categoriesList = listOf("Food", "Transport", "Shopping", "Bills", "Entertainment", "Health", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialItem == null) "Add Recurring Item" else "Edit Recurring Item") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorText = null },
                    label = { Text("Item Name") },
                    placeholder = { Text("e.g. Breakfast, Bus Pass, Tea") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            amountText = it
                            errorText = null
                        }
                    },
                    label = { Text("Default Amount (₹)") },
                    leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Category", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    categoriesList.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Icon Preset", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    iconKeysList.forEach { key ->
                        FilterChip(
                            selected = selectedIconKey == key,
                            onClick = { selectedIconKey = key },
                            label = { Text(key.capitalize(), fontSize = 11.sp) }
                        )
                    }
                }

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(errorText!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = amountText.toDoubleOrNull()
                    if (name.isBlank()) {
                        errorText = "Please enter item name."
                    } else if (parsed == null || parsed <= 0) {
                        errorText = "Please enter valid amount."
                    } else {
                        onSave(name.trim(), parsed, selectedCategory, selectedIconKey)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
