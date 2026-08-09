package com.example.ui.theme

import androidx.compose.ui.graphics.Color

val GreenPrimary = Color(0xFF0B5A39)
val GreenPrimaryDark = Color(0xFF187A50)
val GreenSecondary = Color(0xFF2E7D5B)
val MintBackground = Color(0xFFF5F7F6)
val MintSurface = Color(0xFFFFFFFF)

val DarkBackground = Color(0xFF101915)
val DarkSurface = Color(0xFF1A2620)
val DarkSurfaceVariant = Color(0xFF24332B)

// Category Colors
val CategoryFood = Color(0xFFE65100)
val CategoryTransport = Color(0xFF0277BD)
val CategoryShopping = Color(0xFFAD1457)
val CategoryBills = Color(0xFFC62828)
val CategoryEntertainment = Color(0xFF6A1B9A)
val CategoryHealth = Color(0xFF00695C)
val CategoryOther = Color(0xFF455A64)

fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "food" -> CategoryFood
        "transport", "travel" -> CategoryTransport
        "shopping" -> CategoryShopping
        "bills", "utilities" -> CategoryBills
        "entertainment" -> CategoryEntertainment
        "health", "medical" -> CategoryHealth
        else -> CategoryOther
    }
}
