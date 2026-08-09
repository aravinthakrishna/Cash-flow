package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Expense
import com.example.ui.components.CategoryIcon
import com.example.ui.components.formatRupee
import com.example.ui.components.formatShortRupee
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.getCategoryColor
import com.example.ui.viewmodel.MoneyViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AnalyticsScreen(viewModel: MoneyViewModel) {
    val expenses by viewModel.allExpenses.collectAsState()

    // Calculate Current Month Data
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val startOfCurrentMonth = cal.timeInMillis

    val currentMonthExpenses = remember(expenses, startOfCurrentMonth) {
        expenses.filter { it.date >= startOfCurrentMonth }
    }

    val currentMonthTotal = remember(currentMonthExpenses) {
        currentMonthExpenses.sumOf { it.amount }
    }

    // Days passed in current month
    val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    val avgDailySpend = if (today > 0) currentMonthTotal / today else 0.0

    // Highest Category this month
    val categoryTotals = remember(currentMonthExpenses) {
        currentMonthExpenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
    }

    val highestCategory = categoryTotals.firstOrNull()

    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                Text(
                    text = "Spending Analytics",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Summary Tiles
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Highest Category Tile
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Highest Category",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            if (highestCategory != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CategoryIcon(category = highestCategory.first, size = 36.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = highestCategory.first,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = formatShortRupee(highestCategory.second),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = GreenPrimary
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "No data yet",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Avg Daily Spend Tile
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Avg. Daily Spend",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = formatRupee(avgDailySpend),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = GreenPrimary
                            )
                            Text(
                                text = "this month",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 1. Daily Spending Bar Chart (Last 7 Days)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Daily Spending (Last 7 Days)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap any bar to view exact total",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        val dailyData = remember(expenses) { calculateDailySpendingLast7Days(expenses) }
                        DailyBarChart(dailyData = dailyData)
                    }
                }
            }

            // 2. Weekly Spending Line Chart (Last 6 Weeks)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Weekly Spending (Last 6 Weeks)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        val weeklyData = remember(expenses) { calculateWeeklySpendingLast6Weeks(expenses) }
                        TrendLineChart(data = weeklyData)
                    }
                }
            }

            // 3. Monthly Spending Line Chart (Last 6 Months)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Monthly Spending (Last 6 Months)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        val monthlyData = remember(expenses) { calculateMonthlySpendingLast6Months(expenses) }
                        TrendLineChart(data = monthlyData)
                    }
                }
            }

            // 4. Category Donut Chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Spending by Category (This Month)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        if (categoryTotals.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.PieChart,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No expenses logged this month yet",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            CategoryDonutChart(categoryTotals = categoryTotals, totalSpent = currentMonthTotal)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

data class ChartPoint(val label: String, val amount: Double)

private fun calculateDailySpendingLast7Days(expenses: List<Expense>): List<ChartPoint> {
    val result = mutableListOf<ChartPoint>()
    val cal = Calendar.getInstance()
    val daySdf = SimpleDateFormat("EEE", Locale.getDefault())
    val dateSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    for (i in 6 downTo 0) {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_YEAR, -i)
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)

        val dayStart = c.timeInMillis
        val dayEnd = dayStart + (24 * 60 * 60 * 1000) - 1

        val sum = expenses.filter { it.date in dayStart..dayEnd }.sumOf { it.amount }
        result.add(ChartPoint(daySdf.format(c.time), sum))
    }
    return result
}

private fun calculateWeeklySpendingLast6Weeks(expenses: List<Expense>): List<ChartPoint> {
    val result = mutableListOf<ChartPoint>()
    for (i in 5 downTo 0) {
        val calEnd = Calendar.getInstance()
        calEnd.add(Calendar.WEEK_OF_YEAR, -i)
        val endMillis = calEnd.timeInMillis

        val calStart = Calendar.getInstance()
        calStart.timeInMillis = endMillis
        calStart.add(Calendar.DAY_OF_YEAR, -7)
        val startMillis = calStart.timeInMillis

        val sum = expenses.filter { it.date in startMillis..endMillis }.sumOf { it.amount }
        val label = if (i == 0) "This Wk" else "Wk -${i}"
        result.add(ChartPoint(label, sum))
    }
    return result
}

private fun calculateMonthlySpendingLast6Months(expenses: List<Expense>): List<ChartPoint> {
    val result = mutableListOf<ChartPoint>()
    val monthSdf = SimpleDateFormat("MMM", Locale.getDefault())

    for (i in 5 downTo 0) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -i)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startMillis = cal.timeInMillis

        val calEnd = Calendar.getInstance()
        calEnd.timeInMillis = startMillis
        calEnd.add(Calendar.MONTH, 1)
        val endMillis = calEnd.timeInMillis - 1

        val sum = expenses.filter { it.date in startMillis..endMillis }.sumOf { it.amount }
        result.add(ChartPoint(monthSdf.format(cal.time), sum))
    }
    return result
}

@Composable
fun DailyBarChart(dailyData: List<ChartPoint>) {
    var selectedIndex by remember { mutableIntStateOf(-1) }
    val textMeasurer = rememberTextMeasurer()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = GreenPrimary

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val barWidthSpace = size.width / dailyData.size
                    val clickedIndex = (offset.x / barWidthSpace).toInt()
                    if (clickedIndex in dailyData.indices) {
                        selectedIndex = if (selectedIndex == clickedIndex) -1 else clickedIndex
                    }
                }
            }
    ) {
        val maxAmount = (dailyData.maxOfOrNull { it.amount } ?: 1.0).coerceAtLeast(100.0)
        val barWidthSpace = size.width / dailyData.size
        val barWidth = barWidthSpace * 0.5f

        dailyData.forEachIndexed { index, point ->
            val isSelected = index == selectedIndex
            val barHeight = ((point.amount / maxAmount) * (size.height - 40.dp.toPx())).toFloat().coerceAtLeast(6.dp.toPx())
            val left = index * barWidthSpace + (barWidthSpace - barWidth) / 2
            val top = size.height - 24.dp.toPx() - barHeight

            // Draw Bar
            drawRoundRect(
                color = if (isSelected) Color(0xFFE65100) else primaryColor,
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )

            // Draw Day Label
            val textLayout = textMeasurer.measure(
                text = point.label,
                style = TextStyle(color = onSurfaceColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            )
            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(left + (barWidth - textLayout.size.width) / 2, size.height - 20.dp.toPx())
            )

            // Tooltip if selected
            if (isSelected) {
                val tooltipText = formatRupee(point.amount)
                val tooltipLayout = textMeasurer.measure(
                    text = tooltipText,
                    style = TextStyle(color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                )

                val tooltipWidth = tooltipLayout.size.width + 16.dp.toPx()
                val tooltipHeight = tooltipLayout.size.height + 8.dp.toPx()
                val tooltipLeft = (left + barWidth / 2 - tooltipWidth / 2).coerceIn(0f, size.width - tooltipWidth)
                val tooltipTop = (top - tooltipHeight - 6.dp.toPx()).coerceAtLeast(0f)

                drawRoundRect(
                    color = Color(0xFF2E2E2E),
                    topLeft = Offset(tooltipLeft, tooltipTop),
                    size = Size(tooltipWidth, tooltipHeight),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )

                drawText(
                    textLayoutResult = tooltipLayout,
                    topLeft = Offset(tooltipLeft + 8.dp.toPx(), tooltipTop + 4.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun TrendLineChart(data: List<ChartPoint>) {
    var selectedIndex by remember { mutableIntStateOf(-1) }
    val textMeasurer = rememberTextMeasurer()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = GreenPrimary

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    if (data.isEmpty()) return@detectTapGestures
                    val stepX = size.width / (data.size - 1).coerceAtLeast(1)
                    val clickedIndex = ((offset.x + stepX / 2) / stepX).toInt().coerceIn(0, data.size - 1)
                    selectedIndex = if (selectedIndex == clickedIndex) -1 else clickedIndex
                }
            }
    ) {
        if (data.isEmpty()) return@Canvas

        val maxAmount = (data.maxOfOrNull { it.amount } ?: 1.0).coerceAtLeast(100.0)
        val stepX = size.width / (data.size - 1).coerceAtLeast(1)
        val chartHeight = size.height - 30.dp.toPx()

        val points = data.mapIndexed { index, point ->
            val x = index * stepX
            val y = chartHeight - ((point.amount / maxAmount) * (chartHeight - 20.dp.toPx())).toFloat()
            Offset(x, y)
        }

        // Draw Line Path
        val path = Path()
        points.forEachIndexed { i, pt ->
            if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
        }

        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 3.dp.toPx())
        )

        // Draw Points and Tooltips
        points.forEachIndexed { index, pt ->
            val isSelected = index == selectedIndex

            drawCircle(
                color = if (isSelected) Color(0xFFE65100) else primaryColor,
                radius = if (isSelected) 7.dp.toPx() else 4.dp.toPx(),
                center = pt
            )

            // Draw Label
            val textLayout = textMeasurer.measure(
                text = data[index].label,
                style = TextStyle(color = onSurfaceColor, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            )
            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(pt.x - textLayout.size.width / 2, size.height - 18.dp.toPx())
            )

            if (isSelected) {
                val tooltipText = formatRupee(data[index].amount)
                val tooltipLayout = textMeasurer.measure(
                    text = tooltipText,
                    style = TextStyle(color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                )

                val tooltipWidth = tooltipLayout.size.width + 16.dp.toPx()
                val tooltipHeight = tooltipLayout.size.height + 8.dp.toPx()
                val tooltipLeft = (pt.x - tooltipWidth / 2).coerceIn(0f, size.width - tooltipWidth)
                val tooltipTop = (pt.y - tooltipHeight - 8.dp.toPx()).coerceAtLeast(0f)

                drawRoundRect(
                    color = Color(0xFF2E2E2E),
                    topLeft = Offset(tooltipLeft, tooltipTop),
                    size = Size(tooltipWidth, tooltipHeight),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )

                drawText(
                    textLayoutResult = tooltipLayout,
                    topLeft = Offset(tooltipLeft + 8.dp.toPx(), tooltipTop + 4.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun CategoryDonutChart(categoryTotals: List<Pair<String, Double>>, totalSpent: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Donut Canvas
        Canvas(
            modifier = Modifier
                .size(140.dp)
                .padding(8.dp)
        ) {
            var startAngle = -90f
            categoryTotals.forEach { (category, amount) ->
                val sweepAngle = if (totalSpent > 0) ((amount / totalSpent) * 360f).toFloat() else 0f
                val color = getCategoryColor(category)

                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 24.dp.toPx())
                )
                startAngle += sweepAngle
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Legend
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            categoryTotals.forEach { (category, amount) ->
                val pct = if (totalSpent > 0) (amount / totalSpent) * 100 else 0.0
                val color = getCategoryColor(category)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .padding(top = 1.dp)
                            .background(color = color, shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = category,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.1f", pct)}%",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
