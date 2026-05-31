package com.acoustics.calculator.ui.screen.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.acoustics.calculator.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    navController: NavController,
    projectId: Long,
    viewModel: ProjectDetailViewModel = hiltViewModel()
) {
    val project by viewModel.project.collectAsState()

    LaunchedEffect(projectId) { viewModel.loadProject(projectId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(project?.name ?: "项目详情") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        project?.let { proj ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(proj.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        if (proj.description.isNotBlank()) {
                            Text(proj.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("类型: ${proj.projectType}", style = MaterialTheme.typography.bodySmall)
                        Text("创建时间: ${formatDate(proj.createdAt)}", style = MaterialTheme.typography.labelSmall)
                    }
                }

                // Quick actions
                Text("快捷操作", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = { navController.navigate(Screen.RoomAcoustics.createRoute(proj.id)) },
                        label = { Text("声学计算") },
                        leadingIcon = { Icon(Icons.Default.Home, null, modifier = Modifier.size(16.dp)) }
                    )
                    AssistChip(
                        onClick = { /* PDF export */ },
                        label = { Text("导出PDF") },
                        leadingIcon = { Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(16.dp)) }
                    )
                    AssistChip(
                        onClick = { viewModel.deleteProject(); navController.popBackStack() },
                        label = { Text("删除") },
                        leadingIcon = { Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }
        } ?: run {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}
