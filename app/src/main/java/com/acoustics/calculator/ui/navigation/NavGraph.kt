package com.acoustics.calculator.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.acoustics.calculator.domain.model.KnowledgeBaseData
import com.acoustics.calculator.ui.screen.barrier.BarrierScreen
import com.acoustics.calculator.ui.screen.dashboard.DashboardScreen
import com.acoustics.calculator.ui.screen.examples.ExampleDetailScreen
import com.acoustics.calculator.ui.screen.examples.ExampleListScreen
import com.acoustics.calculator.ui.screen.hvac.HvacScreen
import com.acoustics.calculator.ui.screen.insulations.InsulationScreen
import com.acoustics.calculator.ui.screen.knowledge.KnowledgeDetailScreen
import com.acoustics.calculator.ui.screen.knowledge.KnowledgeScreen
import com.acoustics.calculator.ui.screen.materials.MaterialDetailScreen
import com.acoustics.calculator.ui.screen.materials.MaterialListScreen
import com.acoustics.calculator.ui.screen.noise.NoiseScreen
import com.acoustics.calculator.ui.screen.project.ProjectDetailScreen
import com.acoustics.calculator.ui.screen.project.ProjectListScreen
import com.acoustics.calculator.ui.screen.roomacoustics.RoomAcousticsScreen
import com.acoustics.calculator.ui.screen.roommode.RoomModeScreen
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
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ) {
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
            composable(Screen.Dashboard.route) { DashboardScreen(navController = navController) }
            composable(Screen.Materials.route) { MaterialListScreen(navController = navController) }
            composable(Screen.Projects.route) { ProjectListScreen(navController = navController) }

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
            composable(Screen.Standards.route) { StandardsListScreen(navController = navController) }

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
            composable(Screen.Silencer.route) { SilencerScreen(navController = navController) }

            // ========= V2.0 NEW ROUTES =========

            // Knowledge Hub
            composable(Screen.Knowledge.route) { KnowledgeScreen(navController = navController) }

            // Knowledge Detail — Look up by ID from static data
            composable(
                route = Screen.KnowledgeDetail.route,
                arguments = listOf(navArgument("articleId") { type = NavType.LongType })
            ) { entry ->
                val articleId = entry.arguments?.getLong("articleId") ?: -1L
                val article = KnowledgeBaseData.articles.find { it.id == articleId }
                KnowledgeDetailScreen(
                    navController = navController,
                    article = article
                )
            }

            // Design Examples
            composable(Screen.Examples.route) { ExampleListScreen(navController = navController) }

            // Example Detail — Look up by ID from static data
            composable(
                route = Screen.ExampleDetail.route,
                arguments = listOf(navArgument("exampleId") { type = NavType.LongType })
            ) { entry ->
                val exampleId = entry.arguments?.getLong("exampleId") ?: -1L
                val example = KnowledgeBaseData.designExamples.find { it.id == exampleId }
                ExampleDetailScreen(
                    navController = navController,
                    example = example
                )
            }

            // Room Mode Calculator
            composable(Screen.RoomMode.route) { RoomModeScreen(navController = navController) }

            // Sound Barrier Calculator
            composable(Screen.Barrier.route) { BarrierScreen(navController = navController) }

            // HVAC Noise Calculator
            composable(Screen.Hvac.route) { HvacScreen(navController = navController) }

            // Settings
            composable(Screen.Settings.route) { SettingsScreen(navController = navController) }
        }
    }
}
