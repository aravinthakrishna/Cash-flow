package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Splash : Screen("splash", "Splash", Icons.Default.Home)
    object Home : Screen("home", "Home", Icons.Default.Home)
    object History : Screen("history", "History", Icons.Default.Receipt)
    object Analytics : Screen("analytics", "Analytics", Icons.Default.PieChart)
    object Budget : Screen("budget", "Budget", Icons.Default.AccountBalanceWallet)
    object More : Screen("more", "More", Icons.Default.MoreHoriz)
    object ManageChecklist : Screen("manage_checklist", "Manage Checklist", Icons.AutoMirrored.Filled.ListAlt)
}

val bottomNavScreens = listOf(
    Screen.Home,
    Screen.History,
    Screen.Analytics,
    Screen.Budget,
    Screen.More
)
