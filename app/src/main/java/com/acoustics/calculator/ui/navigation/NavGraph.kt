package com.acoustics.calculator.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.acoustics.calculator.ui.screen.converter.UnitConverterScreen
import com.acoustics.calculator.ui.screen.dashboard.DashboardScreen
import com.acoustics.calculator.ui.screen.insulations.InsulationScreen
import com.acoustics.calculator.ui.screen.materials.MaterialDetailScreen
import com.acoustics.calculator.ui.screen.materials.MaterialListScreen
import com.acoustics.calculator.ui.screen.noise.NoiseScreen
import com.acoustics.calculator.ui.screen.project.ProjectDetailScreen
import com.acoustics.calculator.ui.screen.project.ProjectListScreen
import com.acoustics.calculator.ui.screen.roomacoustics.RoomAcousticsScreen
import com.acoustics.calculator.ui.screen.settings.SettingsScreen
import com.acoustics.calculator.ui.screen.silencer.SilencerScreen
import com.acoustics.calculator.ui.screen.standards.StandardsListScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcousticNavGraph() {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = bottomNavItems.any { item ->
        currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any {
                                it.route == item.screen.route
                            } == true,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Bottom tabs
            composable(Screen.Dashboard.route) {
                DashboardScreen(navController = navController)
            }
            composable(Screen.Materials.route) {
                MaterialListScreen(navController = navController)
            }
            composable(Screen.Projects.route) {
                ProjectListScreen(navController = navController)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(navController = navController)
            }

            // Room Acoustics
            composable(
                route = Screen.RoomAcoustics.route,
                arguments = listOf(navArgument("projectId") { type = NavType.LongType })
            ) { entry ->
                RoomAcousticsScreen(
                    navController = navController,
                    projectId = entry.arguments?.getLong("projectId") ?: -1L
                )
            }

            // Insulation
            composable(
                route = Screen.Insulation.route,
                arguments = listOf(navArgument("projectId") { type = NavType.LongType })
            ) { entry ->
                InsulationScreen(
                    navController = navController,
                    projectId = entry.arguments?.getLong("projectId") ?: -1L
                )
            }

            // Noise
            composable(
                route = Screen.Noise.route,
                arguments = listOf(navArgument("projectId") { type = NavType.LongType })
            ) { entry ->
                NoiseScreen(
                    navController = navController,
                    projectId = entry.arguments?.getLong("projectId") ?: -1L
                )
            }

            // Material Detail
            composable(
                route = Screen.MaterialDetail.route,
                arguments = listOf(navArgument("materialId") { type = NavType.LongType })
            ) { entry ->
                MaterialDetailScreen(
                    navController = navController,
                    materialId = entry.arguments?.getLong("materialId") ?: 0L
                )
            }

            // Standards
            composable(Screen.Standards.route) {
                StandardsListScreen(navController = navController)
            }

            // Project Detail
            composable(
                route = Screen.ProjectDetail.route,
                arguments = listOf(navArgument("projectId") { type = NavType.LongType })
            ) { entry ->
                ProjectDetailScreen(
                    navController = navController,
                    projectId = entry.arguments?.getLong("projectId") ?: 0L
                )
            }

            // Silencer module
            composable(Screen.Silencer.route) {
                SilencerScreen(navController = navController)
            }

            // Settings
            composable(Screen.Settings.route) {
                SettingsScreen(navController = navController)
            }

            // PDF Export
            composable(
                route = Screen.PdfExport.route,
                arguments = listOf(navArgument("projectId") { type = NavType.LongType })
            ) { entry ->
                // TODO: PdfExportScreen
            }
        }
    }
}
