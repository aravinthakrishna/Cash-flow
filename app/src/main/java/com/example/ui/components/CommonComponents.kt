package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.getCategoryColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatRupee(amount: Double): String {
    return "₹" + String.format(Locale.US, "%,.2f", amount)
}

fun formatShortRupee(amount: Double): String {
    return if (amount >= 1000) {
        "₹" + String.format(Locale.US, "%.1fk", amount / 1000)
    } else {
        "₹" + String.format(Locale.US, "%.0f", amount)
    }
}

fun formatDate(millis: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(millis))
}

fun formatDateHeader(millis: Long): String {
    val todaySdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayStr = todaySdf.format(Date())

    val dateStr = todaySdf.format(Date(millis))
    return when {
        dateStr == todayStr -> "Today, " + SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(millis))
        else -> SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault()).format(Date(millis))
    }
}

@Composable
fun CategoryIcon(
    category: String,
    iconKey: String? = null,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val color = getCategoryColor(category)
    val vectorIcon = getIconForCategoryOrKey(category, iconKey)

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = vectorIcon,
            contentDescription = category,
            tint = color,
            modifier = Modifier.size(size * 0.55f)
        )
    }
}

fun getIconForCategoryOrKey(category: String, iconKey: String?): ImageVector {
    if (!iconKey.isNullOrEmpty()) {
        when (iconKey.lowercase()) {
            "breakfast" -> return Icons.Default.Fastfood
            "tea", "coffee" -> return Icons.Default.Coffee
            "bus", "transport" -> return Icons.Default.DirectionsCar
            "lunch" -> return Icons.Default.Restaurant
            "snacks" -> return Icons.Default.LocalDining
            "dinner" -> return Icons.Default.LocalDining
        }
    }

    return when (category.lowercase()) {
        "food" -> Icons.Default.Fastfood
        "transport", "travel" -> Icons.Default.DirectionsCar
        "shopping" -> Icons.Default.ShoppingBag
        "bills", "utilities" -> Icons.Default.Receipt
        "entertainment" -> Icons.Default.Movie
        "health", "medical" -> Icons.Default.FitnessCenter
        else -> Icons.Default.Category
    }
}

@Composable
fun CategoryBadge(
    category: String,
    modifier: Modifier = Modifier
) {
    val color = getCategoryColor(category)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = category,
            color = color,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}
