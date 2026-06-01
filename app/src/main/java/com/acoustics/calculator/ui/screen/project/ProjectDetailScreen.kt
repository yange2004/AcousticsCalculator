package com.acoustics.calculator.ui.screen.project

import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.acoustics.calculator.core.utils.ExportUtils
import com.acoustics.calculator.ui.components.ParticleBackground
import com.acoustics.calculator.ui.navigation.Screen
import com.acoustics.calculator.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    navController: NavController,
    projectId: Long,
    viewModel: ProjectDetailViewModel = hiltViewModel()
) {
    val project by viewModel.project.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var exportResult by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(projectId) { viewModel.loadProject(projectId) }

    val infiniteTransition = rememberInfiniteTransition(label = "projectDetail")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "detailGlow"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(project?.name ?: "项目详情",
                    fontWeight = FontWeight.Bold, color = Color.White) },
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

            project?.let { proj ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Project info card
                    Card(
                        modifier = Modifier.fillMaxWidth()
                            .border(
                                1.dp, NeonCyan.copy(alpha = glowAlpha * 0.5f),
                                RoundedCornerShape(16.dp)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF00F5FF).copy(alpha = 0.15f),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Folder, null,
                                            tint = NeonCyan, modifier = Modifier.size(26.dp))
                                    }
                                }
                                Spacer(Modifier.width(14.dp))
                                Column {
                                    Text(proj.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White)
                                    if (proj.description.isNotBlank()) {
                                        Text(proj.description,
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 13.sp)
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider(color = GlassWhite)
                            Spacer(Modifier.height(8.dp))

                            DetailRow("项目类型", proj.projectType, NeonCyan)
                            DetailRow("创建时间", formatDate(proj.createdAt), NeonGreen)
                            DetailRow("更新时间", formatDate(proj.updatedAt), NeonYellow)
                        }
                    }

                    // Quick actions
                    Text("快捷操作",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White)

                    // Calculator actions
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionChip(
                            icon = Icons.Default.Home,
                            label = "声学计算",
                            color = NeonCyan,
                            onClick = { navController.navigate(Screen.RoomAcoustics.createRoute(proj.id)) }
                        )
                        ActionChip(
                            icon = Icons.Default.Shield,
                            label = "隔声计算",
                            color = NeonPink,
                            onClick = { navController.navigate(Screen.Insulation.createRoute(proj.id)) }
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionChip(
                            icon = Icons.Default.VolumeUp,
                            label = "噪声预测",
                            color = NeonOrange,
                            onClick = { navController.navigate(Screen.Noise.createRoute(proj.id)) }
                        )
                        ActionChip(
                            icon = Icons.Default.Tune,
                            label = "消声器",
                            color = NeonPurple,
                            onClick = { navController.navigate(Screen.Silencer.route) }
                        )
                    }

                    // Export actions
                    Text("导出 / 分享",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionChip(
                            icon = Icons.Default.Description,
                            label = "导出为CSV",
                            color = NeonGreen,
                            onClick = {
                                scope.launch {
                                    val path = ExportUtils.exportProjectToCsv(context, proj)
                                    exportResult = if (path != null) "✅ 已导出到「下载」文件夹: ${proj.name}.csv"
                                    else "❌ 导出失败"
                                }
                            }
                        )
                        ActionChip(
                            icon = Icons.Default.Share,
                            label = "分享",
                            color = NeonCyan,
                            onClick = {
                                val text = "建筑声学计算器 - 项目: ${proj.name}\n类型: ${proj.projectType}\n创建: ${formatDate(proj.createdAt)}"
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                                context.startActivity(Intent.createChooser(intent, "分享项目"))
                            }
                        )
                    }

                    // Delete button
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonRed),
                        border = BorderStroke(1.dp, NeonRed.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("删除项目", fontWeight = FontWeight.Medium)
                    }

                    // Export result
                    exportResult?.let {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (it.contains("失败")) NeonRed.copy(alpha = 0.1f) else NeonGreen.copy(alpha = 0.1f)
                        ) {
                            Text(it,
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                color = if (it.contains("失败")) NeonRed else NeonGreen,
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            } ?: run {
                Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NeonCyan)
                }
            }
        }
    }

    // Delete confirm dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = SurfaceDark,
            title = { Text("确认删除", fontWeight = FontWeight.Bold, color = Color.White) },
            text = { Text("确定要删除此项目吗？此操作不可恢复。", color = Color.White.copy(alpha = 0.7f)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProject()
                        showDeleteConfirm = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消", color = Color.White.copy(alpha = 0.7f))
                }
            }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String, valueColor: Color = Color.White) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 13.sp)
        Text(value,
            fontWeight = FontWeight.Medium,
            color = valueColor,
            fontSize = 13.sp,
            textAlign = TextAlign.End)
    }
}

@Composable
fun ActionChip(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
