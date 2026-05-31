package com.acoustics.calculator.ui.screen.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import kotlinx.coroutines.launch
import com.acoustics.calculator.core.utils.UpdateResult
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.acoustics.calculator.core.utils.UpdateChecker
import com.acoustics.calculator.ui.screen.auth.LoginScreen
import com.acoustics.calculator.ui.screen.auth.UserProfileBar
import com.acoustics.calculator.ui.theme.*

data class CalculatorModule(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String,
    val gradient: Pair<Color, Color> = CardGradientStart to CardGradientEnd
)

val calculatorModules = listOf(
    CalculatorModule("室内声学计算", "混响时间 · 清晰度 · STIPA", Icons.Default.Home, "room_acoustics/-1",
        ChartBlue to Color(0xFF42A5F5)),
    CalculatorModule("隔声计算", "墙体隔声 · STC · 组合隔声", Icons.Default.Shield, "insulation/-1",
        Teal500 to Color(0xFF66BB6A)),
    CalculatorModule("消声器设计", "阻性/抗性/复合 · 智能选型", Icons.Default.Tune, "silencer",
        AccentPurple to Color(0xFFAB47BC)),
    CalculatorModule("噪声预测", "室内外噪声 · 屏障衰减", Icons.Default.VolumeUp, "noise/-1",
        AccentOrange to Color(0xFFFFCA28)),
    CalculatorModule("标准查询", "GB 50118 · GB/T 50121 · 达标判定", Icons.Default.MenuBook, "standards",
        AccentIndigo to Color(0xFF7986CB))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, viewModel: DashboardViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    // 🎬 Infinite marquee animation
    val infiniteTransition = rememberInfiniteTransition(label = "marquee")
    val marqueeOffset by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "scroll"
    )

    // Shimmer animation
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "shimmer"
    )

    // Pulse animation for stats
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text("建筑声学计算器", fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge.copy(
                            shadow = Shadow(Color.Black.copy(alpha = 0.3f), Offset(1f, 1f), blurRadius = 2f),
                            color = Color.White, fontWeight = FontWeight.Bold
                        ))
                },
                actions = {
                    // Settings button
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, "设置")
                    }

                    // ===== 版本更新：网络检测+下载+安装 =====
                    var showUpdateDialog by remember { mutableStateOf(false) }
                    var isDownloading by remember { mutableStateOf(false) }
                    var downloadProgress by remember { mutableStateOf(0f) }
                    var downloadComplete by remember { mutableStateOf(false) }
                    var downloadError by remember { mutableStateOf(false) }
                    var showNoUpdateToast by remember { mutableStateOf(false) }
                    var updateResult by remember { mutableStateOf<UpdateResult?>(null) }
                    var isCheckingUpdate by remember { mutableStateOf(true) }
                    val ctx = androidx.compose.ui.platform.LocalContext.current

                    // 网络检查更新
                    LaunchedEffect(Unit) {
                        updateResult = UpdateChecker.checkForUpdate()
                        isCheckingUpdate = false
                    }

                    // "无新版本"提示2秒自动消失
                    LaunchedEffect(showNoUpdateToast) {
                        if (showNoUpdateToast) {
                            snackbarHostState.showSnackbar(
                                message = "暂无新版本可更新",
                                duration = SnackbarDuration.Short
                            )
                            kotlinx.coroutines.delay(2200)
                            showNoUpdateToast = false
                        }
                    }

                    val result = updateResult
                    IconButton(onClick = {
                        if (result != null && result.hasUpdate) {
                            showUpdateDialog = true
                            if (UpdateChecker.isDownloaded(ctx) && !isDownloading) {
                                downloadComplete = true
                            }
                        } else {
                            showNoUpdateToast = true
                        }
                    }) {
                        if (result != null && result.hasUpdate) {
                            BadgedBox(badge = {
                                Badge(containerColor = NonCompliantRed) { Text("1") }
                            }) {
                                Icon(Icons.Default.Info, "有新版本")
                            }
                        } else {
                            Icon(Icons.Default.CheckCircle, "版本信息",
                                tint = CompliantGreen)
                        }
                    }

                    // Update Dialog
                    if (showUpdateDialog && result != null) {
                        AlertDialog(
                            onDismissRequest = {
                                // Always allow closing, download continues in background
                                showUpdateDialog = false
                            },
                            icon = {
                                Icon(
                                    when {
                                        downloadComplete -> Icons.Default.CheckCircle
                                        isDownloading -> Icons.Default.Sync
                                        else -> Icons.Default.Update
                                    }, null,
                                    tint = when {
                                        downloadComplete -> CompliantGreen
                                        isDownloading -> MaterialTheme.colorScheme.primary
                                        else -> NonCompliantRed
                                    }
                                )
                            },
                            title = {
                                Text(
                                    when {
                                        downloadComplete -> "✅ 下载完成，点击安装！"
                                        isDownloading -> "⏳ 正在下载 ${result.latestVersionName} (${(downloadProgress * 100).toInt()}%)"
                                        else -> "🎉 发现新版本 ${result.latestVersionName}！"
                                    }
                                )
                            },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row {
                                        Text("${result.currentVersionName}  →  ", fontWeight = FontWeight.SemiBold)
                                        Text(result.latestVersionName, fontWeight = FontWeight.Bold,
                                            color = NonCompliantRed)
                                    }
                                    Text("大小: ${UpdateChecker.getApkSize(ctx)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)

                                    // Progress bar during download
                                    if (isDownloading) {
                                        Spacer(Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = downloadProgress,
                                            modifier = Modifier.fillMaxWidth().height(8.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        Text("正在下载... ${(downloadProgress * 100).toInt()}%",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary)
                                    }

                                    // Download complete message
                                    if (downloadComplete) {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = CompliantGreenBg),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.CheckCircle, null,
                                                    tint = CompliantGreen, modifier = Modifier.size(20.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text("更新包已就绪，点击下方「安装」按钮进行安装",
                                                    color = CompliantGreen, fontSize = 13.sp)
                                            }
                                        }
                                    }

                                    // Error
                                    if (downloadError) {
                                        Card(colors = CardDefaults.cardColors(containerColor = NonCompliantRedBg)) {
                                            Text("下载失败，请重试",
                                                modifier = Modifier.padding(12.dp),
                                                color = NonCompliantRed, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }

                                    // Release notes when idle
                                    if (!isDownloading && !downloadComplete) {
                                        HorizontalDivider()
                                        Text("更新内容", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text(result.releaseNotes,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    // Hint about closing
                                    if (isDownloading) {
                                        Text("关闭对话框后下载将继续进行，可稍后查看",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp)
                                    }
                                }
                            },
                            confirmButton = {
                                when {
                                    // Show "安装" when download complete
                                    downloadComplete -> {
                                        Button(
                                            onClick = {
                                                val ok = UpdateChecker.installApk(ctx)
                                                if (ok) {
                                                    showUpdateDialog = false
                                                } else {
                                                    downloadError = true
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = CompliantGreen)
                                        ) {
                                            Icon(Icons.Default.Update, null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("安装 ${result.latestVersionName}")
                                        }
                                    }
                                    // Show "下载更新" when idle
                                    !isDownloading -> {
                                        val scope = rememberCoroutineScope()
                                        Button(
                                            onClick = {
                                                isDownloading = true
                                                downloadError = false
                                                downloadProgress = 0f
                                                scope.launch {
                                                    val dlResult = UpdateChecker.downloadUpdate(ctx, result.downloadUrl) { progress ->
                                                        downloadProgress = progress
                                                    }
                                                    if (dlResult != null) {
                                                        downloadProgress = 1f
                                                        downloadComplete = true
                                                    } else {
                                                        downloadError = true
                                                    }
                                                    isDownloading = false
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = NonCompliantRed)
                                        ) {
                                            Icon(Icons.Default.Download, null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("下载更新")
                                        }
                                    }
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    showUpdateDialog = false
                                }) {
                                    Text(if (isDownloading) "后台下载" else "关闭")
                                }
                            }
                        )
                    }

                    IconButton(onClick = {
                        if (uiState.currentUser == null) viewModel.showLogin() else viewModel.logout()
                    }) {
                        Icon(
                            if (uiState.currentUser != null) Icons.Default.Person else Icons.Default.PersonOutline,
                            if (uiState.currentUser != null) "用户" else "登录"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // 🌟 Gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(GradientStart, GradientMid, GradientEnd)
                        )
                    )
            )

            // ✨ Decorative circles
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                // Large decorative circles
                drawCircle(
                    color = Color.White.copy(alpha = 0.03f),
                    radius = w * 0.8f, center = Offset(w * 0.1f, -h * 0.2f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.04f),
                    radius = w * 0.6f, center = Offset(w * 0.8f, h * 0.3f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.02f),
                    radius = w * 0.5f, center = Offset(w * 0.3f, h * 0.7f)
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // User profile
                uiState.currentUser?.let { user ->
                    item {
                        UserProfileBar(user = user, onLogout = viewModel::logout)
                    }
                }

                // ====== 🏆 HERO HEADER with Marquee ======
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Box {
                            // Card gradient background
                            Box(
                                Modifier
                                    .matchParentSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                ChartBlue, AccentPurple, AccentPink
                                            )
                                        )
                                    )
                            )

                            Column(
                                modifier = Modifier.padding(22.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                // Animated title with glow
                                Text(
                                    "🔊 建筑声学计算器",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        shadow = Shadow(Color.Black.copy(alpha = 0.5f), Offset(2f, 2f), 4f),
                                        color = Color.White, fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(Modifier.height(4.dp))

                                // Marquee text scrolling
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.15f))
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    val marqueeText = "🚀 专业声学设计工具  |  📚 内置${uiState.materialCount}种材料  |  📋 GB标准查询  |  🎯 消声器智能选型  |  📊 PDF报告导出  |  🔄 持续更新中...  |  ⚡ 声学工程师必备  |  🏗️ 建筑声学一站式解决方案 ⚡"
                                    Text(
                                        marqueeText,
                                        color = Color.White.copy(alpha = shimmerAlpha),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Visible,
                                        modifier = Modifier.offset(
                                            x = (marqueeOffset * 500).dp
                                        )
                                    )
                                }

                                Spacer(Modifier.height(12.dp))

                                // Stats row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    StatMini("🏗️", "${uiState.recentProjects.size}", "项目")
                                    StatMini("🧱", "${uiState.materialCount}", "材料")
                                    StatMini("📋", "86条", "标准")
                                }
                            }
                        }
                    }
                }

                // ====== 🎯 核心计算模块 ======
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚡ 核心计算模块",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White)
                        Spacer(Modifier.weight(1f))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text("5个模块", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                color = Color.White, fontSize = 11.sp)
                        }
                    }
                }

                // Module cards with gradients
                items(calculatorModules) { module ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate(module.route) }
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(module.gradient.first, module.gradient.second)
                                ),
                                RoundedCornerShape(16.dp)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.elevatedCardElevation(4.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = Color.Transparent
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Module icon with glow
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White.copy(alpha = 0.25f),
                                modifier = Modifier.size(54.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(module.icon, null, tint = Color.White, modifier = Modifier.size(28.dp))
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(module.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = Color.White)
                                Text(module.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f))
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }

                // ====== 📊 快速统计 ======
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard("📚 材料库", "${uiState.materialCount}种", ChartBlue, Modifier.weight(1f))
                        StatCard("📂 最近项目", "${uiState.recentProjects.size}个", ChartTeal, Modifier.weight(1f))
                    }
                }

                // ====== 📁 最近项目 ======
                if (uiState.recentProjects.isNotEmpty()) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📂 最近项目",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White)
                            Spacer(Modifier.weight(1f))
                            Text("查看全部 →",
                                color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        }
                    }
                    items(uiState.recentProjects.take(3)) { project ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate(
                                        com.acoustics.calculator.ui.navigation.Screen.ProjectDetail.createRoute(project.id)
                                    )
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.15f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Folder, null,
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(project.name, fontWeight = FontWeight.Medium, color = Color.White)
                                    Text(project.projectType,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.6f))
                                }
                                Icon(Icons.Default.ChevronRight, null,
                                    tint = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Bottom padding
                item { Spacer(Modifier.height(20.dp)) }
            }
        }
    }

    // Login dialog
    if (uiState.showLoginSheet) {
        AlertDialog(
            onDismissRequest = viewModel::hideLogin,
            title = {},
            text = {
                LoginScreen(onLoginSuccess = viewModel::onLoginSuccess)
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = viewModel::hideLogin) { Text("关闭") }
            }
        )
    }
}

@Composable
fun StatMini(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$emoji $value",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.White)
        Text(label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f))
    }
}

@Composable
fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(value,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color.White)
            Spacer(Modifier.width(8.dp))
            Text(label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f))
        }
    }
}

// Note: Use `val ctx = androidx.compose.ui.platform.LocalContext.current` inside @Composable functions
