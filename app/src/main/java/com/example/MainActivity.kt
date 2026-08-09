package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.navigation.Screen
import com.example.ui.navigation.bottomNavScreens
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.BudgetScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ManageChecklistScreen
import com.example.ui.screens.MoreScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.MoneyManagerTheme
import com.example.ui.viewmodel.MoneyViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MoneyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        setContent {
            val userPrefs by viewModel.userPreferences.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val isDarkTheme = userPrefs?.darkModeEnabled ?: systemDark

            MoneyManagerTheme(darkTheme = isDarkTheme) {
                MainAppStructure(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppStructure(viewModel: MoneyViewModel) {
    var showSplash by rememberSaveable { mutableStateOf(true) }
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isBottomBarVisible = currentRoute in bottomNavScreens.map { it.route }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (isBottomBarVisible) {
                    NavigationBar(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .testTag("bottom_navigation_bar"),
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        bottomNavScreens.forEach { screen ->
                            val isSelected = currentRoute == screen.route

                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    if (currentRoute != screen.route) {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = screen.title,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = GreenPrimary,
                                    selectedTextColor = GreenPrimary,
                                    indicatorColor = GreenPrimary.copy(alpha = 0.15f),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag("nav_tab_${screen.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToManageChecklist = {
                            navController.navigate(Screen.ManageChecklist.route)
                        }
                    )
                }

                composable(Screen.History.route) {
                    HistoryScreen(viewModel = viewModel)
                }

                composable(Screen.Analytics.route) {
                    AnalyticsScreen(viewModel = viewModel)
                }

                composable(Screen.Budget.route) {
                    BudgetScreen(viewModel = viewModel)
                }

                composable(Screen.More.route) {
                    MoreScreen(
                        viewModel = viewModel,
                        onNavigateToManageChecklist = {
                            navController.navigate(Screen.ManageChecklist.route)
                        }
                    )
                }

                composable(Screen.ManageChecklist.route) {
                    ManageChecklistScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }

        if (showSplash) {
            SplashScreen(
                onSplashFinished = {
                    showSplash = false
                }
            )
        }
    }
}
