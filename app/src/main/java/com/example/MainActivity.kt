package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.example.ui.AppViewModel
import com.example.ui.screens.BookingTestScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeSettingsScreen
import com.example.ui.theme.CyanDark
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.PorterAssistantTheme
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

enum class AppScreen(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard, "tab_dashboard"),
    SETTINGS("Home Settings", Icons.Default.Home, "tab_settings"),
    SIMULATOR("Test Lab", Icons.Default.Science, "tab_simulator"),
    HISTORY("History", Icons.Default.History, "tab_history")
}

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PorterAssistantTheme {
                MainContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainContent(viewModel: AppViewModel) {
    var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }

    // Predictive / system back handling
    BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
        currentScreen = AppScreen.DASHBOARD
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                containerColor = Slate900,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_bottom_nav")
            ) {
                AppScreen.values().forEach { screen ->
                    NavigationBarItem(
                        selected = currentScreen == screen,
                        onClick = { currentScreen = screen },
                        icon = { Icon(imageVector = screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = CyanPrimary,
                            indicatorColor = CyanDark,
                            unselectedIconColor = Slate400,
                            unselectedTextColor = Slate400
                        ),
                        modifier = Modifier.testTag(screen.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentScreen) {
            AppScreen.DASHBOARD -> DashboardScreen(
                viewModel = viewModel,
                onNavigateToSettings = { currentScreen = AppScreen.SETTINGS },
                onNavigateToSimulator = { currentScreen = AppScreen.SIMULATOR },
                onNavigateToHistory = { currentScreen = AppScreen.HISTORY },
                modifier = Modifier.padding(innerPadding)
            )

            AppScreen.SETTINGS -> HomeSettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { currentScreen = AppScreen.DASHBOARD },
                modifier = Modifier.padding(innerPadding)
            )

            AppScreen.SIMULATOR -> BookingTestScreen(
                viewModel = viewModel,
                onNavigateBack = { currentScreen = AppScreen.DASHBOARD },
                modifier = Modifier.padding(innerPadding)
            )

            AppScreen.HISTORY -> HistoryScreen(
                viewModel = viewModel,
                onNavigateBack = { currentScreen = AppScreen.DASHBOARD },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
