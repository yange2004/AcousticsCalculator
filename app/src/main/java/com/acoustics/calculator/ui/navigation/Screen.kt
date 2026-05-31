package com.acoustics.calculator.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * All navigation destinations in the app.
 */
sealed class Screen(val route: String) {
    // Bottom navigation tabs
    data object Dashboard : Screen("dashboard")
    data object Materials : Screen("materials")
    data object Projects : Screen("projects")
    data object Converter : Screen("converter")

    // Feature screens
    data object RoomAcoustics : Screen("room_acoustics/{projectId}") {
        fun createRoute(projectId: Long = -1L) = "room_acoustics/$projectId"
    }
    data object Insulation : Screen("insulation/{projectId}") {
        fun createRoute(projectId: Long = -1L) = "insulation/$projectId"
    }
    data object Noise : Screen("noise/{projectId}") {
        fun createRoute(projectId: Long = -1L) = "noise/$projectId"
    }
    data object MaterialDetail : Screen("material_detail/{materialId}") {
        fun createRoute(materialId: Long) = "material_detail/$materialId"
    }
    data object Standards : Screen("standards")
    data object ProjectDetail : Screen("project_detail/{projectId}") {
        fun createRoute(projectId: Long) = "project_detail/$projectId"
    }
    data object PdfExport : Screen("pdf_export/{projectId}") {
        fun createRoute(projectId: Long) = "pdf_export/$projectId"
    }

    // Silencer module
    data object Silencer : Screen("silencer")

    // Settings
    data object Settings : Screen("settings")
}

/**
 * Bottom navigation tab items.
 */
data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val screen: Screen
)

val bottomNavItems = listOf(
    BottomNavItem("首页", Icons.Default.Dashboard, Screen.Dashboard),
    BottomNavItem("材料库", Icons.Default.List, Screen.Materials),
    BottomNavItem("项目", Icons.Default.Folder, Screen.Projects),
    BottomNavItem("设置", Icons.Default.Settings, Screen.Settings)
)
