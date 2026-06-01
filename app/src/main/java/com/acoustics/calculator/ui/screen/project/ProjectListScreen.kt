package com.acoustics.calculator.ui.screen.project

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.acoustics.calculator.ui.components.ParticleBackground
import com.acoustics.calculator.ui.navigation.Screen
import com.acoustics.calculator.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectListScreen(
    navController: NavController,
    viewModel: ProjectListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var newProjectName by remember { mutableStateOf("") }
    var newProjectDesc by remember { mutableStateOf("") }
    var selectedProjectType by remember { mutableStateOf("ROOM_ACOUSTICS") }
    var searchQuery by remember { mutableStateOf("") }

    val projectTypes = listOf(
        "ROOM_ACOUSTICS" to "室内声学",
        "INSULATION" to "隔声计算",
        "NOISE" to "噪声预测",
        "SILENCER" to "消声器设计"
    )

    val filteredProjects = remember(uiState.projects, searchQuery) {
        if (searchQuery.isBlank()) uiState.projects
        else uiState.projects.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true) ||
            it.projectType.contains(searchQuery, ignoreCase = true)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "projectList")
    val listGlow by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "projectListGlow"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("项目管理",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = NeonCyan,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, "新建项目")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(BgGradientStart, BgGradientMid, BgGradientEnd)
                    )
                )
        ) {
            ParticleBackground()

            if (uiState.isLoading) {
                Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NeonCyan)
                }
            } else if (uiState.projects.isEmpty()) {
                Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Folder, null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.White.copy(alpha = 0.3f))
                        Spacer(Modifier.height(12.dp))
                        Text("暂无项目",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.6f))
                        Text("点击右下角 + 创建新项目",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.4f))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Search bar
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("搜索项目...", color = Color.White.copy(alpha = 0.4f)) },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.5f)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonCyan.copy(alpha = listGlow),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                cursorColor = NeonCyan,
                                focusedLabelColor = NeonCyan,
                                unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
                            )
                        )
                    }

                    // Project count
                    item {
                        Text("共 ${filteredProjects.size} 个项目",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f))
                    }

                    // Project list
                    items(filteredProjects) { project ->
                        val typeLabel = projectTypes.find { it.first == project.projectType }?.second ?: project.projectType
                        val typeColor = when (project.projectType) {
                            "ROOM_ACOUSTICS" -> NeonCyan
                            "INSULATION" -> NeonPink
                            "NOISE" -> NeonOrange
                            "SILENCER" -> NeonPurple
                            else -> Color.White.copy(alpha = 0.5f)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    when (project.projectType) {
                                        "ROOM_ACOUSTICS" -> navController.navigate(Screen.RoomAcoustics.createRoute(project.id))
                                        "INSULATION" -> navController.navigate(Screen.Insulation.createRoute(project.id))
                                        "NOISE" -> navController.navigate(Screen.Noise.createRoute(project.id))
                                        else -> navController.navigate(Screen.ProjectDetail.createRoute(project.id))
                                    }
                                }
                                .border(
                                    1.dp, typeColor.copy(alpha = 0.3f),
                                    RoundedCornerShape(14.dp)
                                ),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Type icon
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = typeColor.copy(alpha = 0.15f),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Icon(
                                            when (project.projectType) {
                                                "ROOM_ACOUSTICS" -> Icons.Default.Home
                                                "INSULATION" -> Icons.Default.Shield
                                                "NOISE" -> Icons.Default.VolumeUp
                                                else -> Icons.Default.Folder
                                            },
                                            null,
                                            tint = typeColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(project.name,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis)
                                        Spacer(Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = typeColor.copy(alpha = 0.2f)
                                        ) {
                                            Text(" ${typeLabel} ",
                                                fontSize = 9.sp,
                                                color = typeColor,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                        }
                                    }
                                    if (project.description.isNotBlank()) {
                                        Text(project.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.5f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis)
                                    }
                                    Text(formatDate(project.updatedAt),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.3f))
                                }

                                IconButton(onClick = { viewModel.deleteProject(project.id) }) {
                                    Icon(Icons.Default.Delete, "删除",
                                        tint = NeonRed.copy(alpha = 0.7f),
                                        modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Create project dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = SurfaceDark,
            title = {
                Text("新建项目",
                    fontWeight = FontWeight.Bold,
                    color = Color.White)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newProjectName,
                        onValueChange = { newProjectName = it },
                        label = { Text("项目名称") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            cursorColor = NeonCyan,
                            focusedLabelColor = NeonCyan,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
                        )
                    )

                    // Project type selection
                    Text("项目类型", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        projectTypes.forEach { (type, label) ->
                            FilterChip(
                                selected = selectedProjectType == type,
                                onClick = { selectedProjectType = type },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                                    selectedLabelColor = NeonCyan,
                                    containerColor = GlassWhite,
                                    labelColor = Color.White.copy(alpha = 0.7f)
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = newProjectDesc,
                        onValueChange = { newProjectDesc = it },
                        label = { Text("描述 (可选)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            cursorColor = NeonCyan,
                            focusedLabelColor = NeonCyan,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newProjectName.isNotBlank()) {
                            viewModel.createProject(newProjectName, newProjectDesc, selectedProjectType)
                            newProjectName = ""
                            newProjectDesc = ""
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("创建") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("取消", color = Color.White.copy(alpha = 0.7f))
                }
            }
        )
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
