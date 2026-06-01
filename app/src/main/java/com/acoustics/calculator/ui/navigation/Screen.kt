package com.acoustics.calculator.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * V2.0 — All navigation destinations with new Knowledge & Example modules
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

    // ========= V2.0 NEW SCREENS =========

    // Knowledge Hub
    data object Knowledge : Screen("knowledge")
    data object KnowledgeDetail : Screen("knowledge_detail/{articleId}") {
        fun createRoute(articleId: Long) = "knowledge_detail/$articleId"
    }

    // Design Examples
    data object Examples : Screen("examples")
    data object ExampleDetail : Screen("example_detail/{exampleId}") {
        fun createRoute(exampleId: Long) = "example_detail/$exampleId"
    }

    // Room Mode Calculator
    data object RoomMode : Screen("room_mode")

    // Sound Barrier Calculator
    data object Barrier : Screen("barrier")

    // HVAC Noise Calculator
    data object Hvac : Screen("hvac")
}

/**
 * Bottom navigation tab items
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
