package com.acoustics.calculator.ui.screen.settings

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.acoustics.calculator.core.utils.UpdateChecker
import com.acoustics.calculator.core.utils.UpdateResult
import com.acoustics.calculator.ui.components.ParticleBackground
import com.acoustics.calculator.ui.screen.auth.LoginViewModel
import com.acoustics.calculator.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val loginState by loginViewModel.uiState.collectAsState()

    var updateResult by remember { mutableStateOf<UpdateResult?>(null) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var downloadComplete by remember { mutableStateOf(false) }
    var downloadError by remember { mutableStateOf(false) }
    var isChecking by remember { mutableStateOf(false) }
    var checkTrigger by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val ctx = androidx.compose.ui.platform.LocalContext.current

    // 进入页面 + 点击检查时触发
    LaunchedEffect(checkTrigger) {
        isChecking = true
        updateResult = UpdateChecker.checkForUpdate()
        isChecking = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置", fontWeight = FontWeight.Bold, color = Color.White) },
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // ===== 账户信息 =====
            Text("👤 账户管理", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    loginState.currentUser?.let { user ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = NeonCyan.copy(alpha = 0.15f),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Text("📱", fontSize = 24.sp)
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(user.nickname.ifBlank { "用户${user.phone.takeLast(4)}" },
                                    fontWeight = FontWeight.Bold, color = Color.White)
                                Text("${user.phone.take(3)}****${user.phone.takeLast(4)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.5f))
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { loginViewModel.logout() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonRed.copy(alpha = 0.8f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("退出登录", fontWeight = FontWeight.Medium)
                        }
                    } ?: run {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PersonOutline, null, modifier = Modifier.size(40.dp),
                                tint = Color.White.copy(alpha = 0.5f))
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text("未登录", fontWeight = FontWeight.Bold, color = Color.White)
                                Text("登录后可保存使用记录",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }

            // ===== 版本更新 =====
            Text("🔄 版本更新", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (updateResult?.hasUpdate == true) Icons.Default.Update else Icons.Default.CheckCircle,
                            null,
                            tint = if (updateResult?.hasUpdate == true) NeonRed else NeonGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("当前版本: ${updateResult?.currentVersionName ?: "..."}",
                                fontWeight = FontWeight.Medium, color = Color.White)
                            if (updateResult?.hasUpdate == true) {
                                Text("最新版本: ${updateResult?.latestVersionName} 可供更新",
                                    color = NeonRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            } else {
                                Text(if (isChecking) "正在检查..." else "已是最新版本",
                                    color = if (isChecking) NeonCyan else NeonGreen, fontSize = 13.sp)
                            }
                        }
                        // 手动刷新按钮
                        IconButton(onClick = { checkTrigger++ }) {
                            if (isChecking) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = NeonCyan,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Refresh, "检查更新", tint = NeonCyan)
                            }
                        }
                    }

                    if (updateResult?.hasUpdate == true) {
                        when {
                            downloadComplete -> {
                                Button(onClick = { UpdateChecker.installApk(ctx) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                    shape = RoundedCornerShape(12.dp)) {
                                    Icon(Icons.Default.Update, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("安装 ${updateResult?.latestVersionName ?: ""}")
                                }
                            }
                            isDownloading -> {
                                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    LinearProgressIndicator(
                                        progress = downloadProgress,
                                        modifier = Modifier.fillMaxWidth().height(6.dp),
                                        color = NeonCyan,
                                        trackColor = GlassWhite
                                    )
                                    Text("正在下载更新 (${(downloadProgress * 100).toInt()}%)",
                                        style = MaterialTheme.typography.bodySmall, color = NeonCyan)
                                }
                            }
                            else -> {
                                Button(onClick = {
                                    isDownloading = true; downloadError = false; downloadProgress = 0f
                                    scope.launch {
                                        val result = UpdateChecker.downloadUpdate(ctx, updateResult?.downloadUrl ?: "") { p -> downloadProgress = p }
                                        if (result != null) { downloadProgress = 1f; downloadComplete = true }
                                        else { downloadError = true }
                                        isDownloading = false
                                    }
                                }, modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                    shape = RoundedCornerShape(12.dp)) {
                                    Icon(Icons.Default.Download, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("下载 ${updateResult?.latestVersionName ?: ""}", color = Color.White)
                                }
                            }
                        }
                        if (downloadError) {
                            Surface(shape = RoundedCornerShape(10.dp), color = NeonRed.copy(alpha = 0.1f)) {
                                Text("下载失败，请检查网络后重试",
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    color = NeonRed, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    } else {
                        OutlinedButton(onClick = { },
                            modifier = Modifier.fillMaxWidth(), enabled = false,
                            shape = RoundedCornerShape(12.dp)) {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("已是最新版本")
                        }
                    }

                    HorizontalDivider(color = GlassWhite)
                    Text("更新内容", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f))
                    Text(updateResult?.releaseNotes ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f))
                }
            }

            // ===== 关于 =====
            Text("ℹ️ 关于", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsAboutRow(Icons.Default.Info, "应用名称", "建筑声学计算器")
                    SettingsAboutRow(Icons.Default.Code, "版本号", "${com.acoustics.calculator.core.constants.AppVersion.DISPLAY} (Code ${com.acoustics.calculator.core.constants.AppVersion.CODE})")
                    SettingsAboutRow(Icons.Default.Build, "开发框架", "Jetpack Compose + Material 3")
                    SettingsAboutRow(Icons.Default.Storage, "数据引擎", "Room + Hilt DI")
                    SettingsAboutRow(Icons.Default.MenuBook, "数据来源", "《实用建筑声学》项端新 编著")
                }
            }

            // ===== 底部退出 =====
            Spacer(Modifier.height(16.dp))
            Text(
                "检测到更新时，可在本页面下载并安装新版APK",
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 11.sp
            )

            Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SettingsAboutRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = NeonCyan)
        Spacer(Modifier.width(12.dp))
        Text(label, modifier = Modifier.width(80.dp), fontSize = 13.sp, color = Color.White.copy(alpha = 0.5f))
        Text(value, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = Color.White)
    }
}
