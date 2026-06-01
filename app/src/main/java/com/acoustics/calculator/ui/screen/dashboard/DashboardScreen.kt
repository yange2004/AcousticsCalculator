package com.acoustics.calculator.ui.screen.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.acoustics.calculator.core.constants.AppVersion
import com.acoustics.calculator.ui.components.*
import com.acoustics.calculator.ui.screen.auth.LoginScreen
import com.acoustics.calculator.ui.screen.auth.LoginViewModel
import com.acoustics.calculator.ui.theme.*

/**
 * V2.0 DASHBOARD — Full Neon / Cyberpunk redesign with all new modules
 */
data class CalculatorModule(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String,
    val gradient: Pair<Color, Color> = CardGradientStart to CardGradientEnd,
    val badge: String? = null,
    val isNew: Boolean = false,
    val versionTag: String = ""
)

val calculatorModules = listOf(
    // === Original core modules ===
    CalculatorModule("室内声学计算", "混响时间 · 清晰度 · STIPA", Icons.Default.Home, "room_acoustics/-1",
        NeonCyan to NeonBlue, null, false, "v1"),
    CalculatorModule("隔声计算", "墙体隔声 · STC · 组合隔声", Icons.Default.Shield, "insulation/-1",
        NeonPink to NeonPurple, null, false, "v1"),
    CalculatorModule("消声器设计", "阻性/抗性/复合 · 智能选型", Icons.Default.Tune, "silencer",
        NeonOrange to NeonYellow, null, false, "v1"),
    CalculatorModule("噪声预测", "室内外噪声 · 屏障衰减", Icons.Default.VolumeUp, "noise/-1",
        NeonRed to NeonOrange, null, false, "v1"),
    CalculatorModule("标准查询", "GB 50118 · GB/T 50121 · 达标判定", Icons.Default.MenuBook, "standards",
        NeonGreen to NeonCyan, null, false, "v1"),

    // === V2.0 NEW MODULES ===
    CalculatorModule("📚 声学知识库", "从《实用建筑声学》提炼的知识体系", Icons.Default.LibraryBooks, "knowledge",
        NeonCyan to NeonPurple, "15篇", true, "新"),
    CalculatorModule("📐 设计实例", "120+国内外经典声学设计案例", Icons.Default.Business, "examples",
        NeonYellow to NeonOrange, "12例", true, "新"),
    CalculatorModule("🎵 房间模式", "驻波分析 · 最优比例推荐", Icons.Default.GraphicEq, "room_mode",
        NeonPurple to NeonPink, "新工具", true, "新"),
    CalculatorModule("🛡️ 声屏障", "屏障插入损失 · Kurze-Anderson", Icons.Default.Wallpaper, "barrier",
        NeonOrange to NeonRed, "新工具", true, "新"),
    CalculatorModule("🌬️ HVAC噪声", "风管噪声 · 暖通系统评估", Icons.Default.Air, "hvac",
        NeonGreen to NeonCyan, "新工具", true, "新"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val loginState by loginViewModel.uiState.collectAsState()
    var showLoginDialog by remember { mutableStateOf(false) }

    // 🎬 Animations
    val infiniteTransition = rememberInfiniteTransition(label = "dash")

    val marqueeOffset by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "marquee"
    )

    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "shimmer"
    )

    // RGB color cycling for hero border
    val rgbIndex by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rgbCycle"
    )
    val heroBorderColor = remember(rgbIndex) {
        RgbColors[rgbIndex.toInt().coerceIn(0, 7)]
    }

    // 登录成功时刷新用户信息
    LaunchedEffect(loginState.isLoggedIn) {
        if (loginState.isLoggedIn) {
            viewModel.refreshUser()
            showLoginDialog = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("建筑声学计算器", fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge.copy(
                                shadow = Shadow(Color.Black.copy(alpha = 0.5f), Offset(1f, 1f), 3f),
                                color = Color.White, fontWeight = FontWeight.Bold
                            ))
                        Spacer(Modifier.width(6.dp))
                        // 统一版本标签，从 AppVersion 读取
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NeonPink,
                        ) {
                            Text(" ${AppVersion.TITLE_TAG} ",
                                color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }
                },
                actions = {
                    // 用户头像/登录入口
                    IconButton(onClick = {
                        if (uiState.isLoggedIn) {
                            // 已登录：弹出菜单
                        } else {
                            showLoginDialog = true
                        }
                    }) {
                        if (uiState.isLoggedIn) {
                            Icon(Icons.Default.AccountCircle, "用户", tint = NeonCyan)
                        } else {
                            Icon(Icons.Default.Login, "登录", tint = Color.White.copy(alpha = 0.7f))
                        }
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, "设置", tint = Color.White)
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
            // 🌟 Deep gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(BgGradientStart, BgGradientMid, BgGradientEnd)
                        )
                    )
            )

            // ✨ Particle effect background
            ParticleBackground()

            // ✨ Decorative circles
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width; val h = size.height
                drawCircle(color = Color.White.copy(alpha = 0.02f), radius = w * 0.9f, center = Offset(w * 0.1f, -h * 0.2f))
                drawCircle(color = NeonCyan.copy(alpha = 0.03f), radius = w * 0.5f, center = Offset(w * 0.8f, h * 0.3f))
                drawCircle(color = NeonPurple.copy(alpha = 0.02f), radius = w * 0.4f, center = Offset(w * 0.5f, h * 0.7f))
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ====== 🏆 HERO HEADER ======
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(12.dp)
                    ) {
                        Box {
                            // Animated gradient background
                            Box(Modifier.matchParentSize().background(
                                Brush.horizontalGradient(
                                    colors = listOf(BgGradientStart, SurfaceDark, BgGradientMid)
                                )
                            ))

                            // Animated sound wave
                            SoundWaveBackground(waveColor = NeonCyan, amplitude = 25f)

                            Column(modifier = Modifier.padding(22.dp)) {
                                // Title with glow
                                Text(
                                    "🔊 建筑声学计算器",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        shadow = Shadow(Color.Black.copy(alpha = 0.6f), Offset(2f, 2f), 5f),
                                        color = Color.White, fontWeight = FontWeight.Bold
                                    )
                                )

                                Spacer(Modifier.height(4.dp))

                                // Marquee
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(GlassWhite)
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                            val marqueeText = "🚀 ${AppVersion.TITLE_TAG}全面升级  |  📚 内置${uiState.materialCount}种材料  |  📋 GB标准查询  |  🎯 消声器智能选型  |  📖 声学知识库  |  🏗️ 120+设计实例  |  🔄 持续更新中...  |  ⚡ 声学工程师必备  |  🏛️ 《实用建筑声学》数字化 ⚡"
                                    Text(
                                        marqueeText, color = Color.White.copy(alpha = shimmerAlpha),
                                        fontSize = 12.sp, fontWeight = FontWeight.Medium,
                                        maxLines = 1, softWrap = false, overflow = TextOverflow.Visible,
                                        modifier = Modifier.offset(x = (marqueeOffset * 500).dp)
                                    )
                                }

                                Spacer(Modifier.height(12.dp))

                                // Stats
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    StatMini("🏗️", "${uiState.recentProjects.size}", "项目")
                                    StatMini("🧱", "${uiState.materialCount}", "材料")
                                    StatMini("📖", "15", "知识")
                                    StatMini("📐", "12", "案例")
                                }
                            }
                        }
                    }
                }

                // ====== 👤 用户信息 ======
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                            .border(1.dp, if (uiState.isLoggedIn) NeonCyan.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (uiState.isLoggedIn && uiState.currentUser != null) {
                                // 已登录状态
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = NeonCyan.copy(alpha = 0.15f),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Text("📱", fontSize = 22.sp)
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(uiState.currentUser!!.nickname.ifBlank { "用户${uiState.currentUser!!.phone.takeLast(4)}" },
                                        fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                    Text("${uiState.currentUser!!.phone.take(3)}****${uiState.currentUser!!.phone.takeLast(4)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                                }
                                // 退出登录按钮
                                OutlinedButton(
                                    onClick = {
                                        viewModel.logout()
                                        loginViewModel.logout()
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonRed),
                                    border = BorderStroke(1.dp, NeonRed.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(Icons.Default.Logout, null, modifier = Modifier.size(14.dp), tint = NeonRed)
                                    Spacer(Modifier.width(4.dp))
                                    Text("退出", fontSize = 11.sp, color = NeonRed)
                                }
                            } else {
                                // 未登录状态
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = GlassWhite,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Icon(Icons.Default.PersonOutline, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(24.dp))
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text("未登录", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                    Text("登录后可保存项目和使用记录",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                                }
                                // 登录按钮
                                Button(
                                    onClick = { showLoginDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(Icons.Default.Login, null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("登录", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // ====== ⚡ 更新公告 ======
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                            .border(1.dp, heroBorderColor.copy(alpha = 0.6f), RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(NeonPink.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) { Text("🎉", fontSize = 20.sp) }

                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("${AppVersion.TITLE_TAG} 更新",
                                    fontWeight = FontWeight.Bold, color = NeonPink, fontSize = 14.sp)
                                Text(AppVersion.history.lastOrNull()?.notes ?: "",
                                    color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                            }
                            Icon(Icons.Default.Celebration, null, tint = NeonYellow, modifier = Modifier.size(24.dp))
                        }
                    }
                }

                // ====== 🎯 核心计算模块 ======
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚡ 核心模块", style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(Modifier.weight(1f))
                        Surface(shape = RoundedCornerShape(12.dp), color = GlassWhite) {
                            Text("${calculatorModules.size}个模块",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        }
                    }
                }

                // Module cards
                items(calculatorModules) { module ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate(module.route) }
                            .border(
                                width = if (module.isNew) 1.dp else 0.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(module.gradient.first, module.gradient.second)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.elevatedCardElevation(if (module.isNew) 8.dp else 4.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = SurfaceDark)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icon with glow
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = module.gradient.first.copy(alpha = 0.25f),
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(module.icon, null, tint = module.gradient.second, modifier = Modifier.size(28.dp))
                                }
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(module.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                                    if (module.isNew) {
                                        Spacer(Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = NeonPink
                                        ) {
                                            Text("NEW", modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                                color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    if (module.versionTag == "v1") {
                                        Spacer(Modifier.width(4.dp))
                                        Text("v1", color = Color.White.copy(alpha = 0.3f), fontSize = 10.sp)
                                    }
                                }
                                Text(module.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.6f))
                            }
                            module.badge?.let {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = module.gradient.first.copy(alpha = 0.3f)
                                ) {
                                    Text(it, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        color = module.gradient.second, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.width(8.dp))
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.4f))
                        }
                    }
                }

                // ====== 📊 Stats Row ======
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard("📚 材料库", "${uiState.materialCount}种", NeonCyan, Modifier.weight(1f))
                        StatCard("📂 最近项目", "${uiState.recentProjects.size}个", NeonPink, Modifier.weight(1f))
                    }
                }

                // ====== 📁 Recent Projects ======
                if (uiState.recentProjects.isNotEmpty()) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📂 最近项目",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(Modifier.weight(1f))
                            Text("查看全部 →",
                                color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                        }
                    }
                    items(uiState.recentProjects.take(3)) { project ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                                .clickable { navController.navigate(
                                    com.acoustics.calculator.ui.navigation.Screen.ProjectDetail.createRoute(project.id)) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Folder, null, tint = NeonCyan.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(project.name, fontWeight = FontWeight.Medium, color = Color.White)
                                    Text(project.projectType, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f))
                                }
                                Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // ====== Bottom padding ======
                item { Spacer(Modifier.height(20.dp)) }
            }
        }
    }

    // ====== 登录弹窗 ======
    if (showLoginDialog) {
        AlertDialog(
            onDismissRequest = { showLoginDialog = false },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.Transparent,
            title = {},
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 300.dp)
                ) {
                    LoginScreen(
                        onLoginSuccess = {
                            showLoginDialog = false
                            viewModel.refreshUser()
                        }
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLoginDialog = false }) {
                    Text("关闭", color = NeonCyan)
                }
            }
        )
    }

}

@Composable
fun StatMini(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$emoji $value", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
        Text(label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
    }
}

@Composable
fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier, shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = color)
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
        }
    }
}
