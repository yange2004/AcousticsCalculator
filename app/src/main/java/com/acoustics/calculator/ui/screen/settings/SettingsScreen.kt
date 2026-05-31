package com.acoustics.calculator.ui.screen.settings

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch
import com.acoustics.calculator.core.utils.UpdateResult
import com.acoustics.calculator.core.utils.VersionInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.acoustics.calculator.core.utils.UpdateChecker
import com.acoustics.calculator.ui.screen.auth.LoginScreen
import com.acoustics.calculator.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var updateResult by remember { mutableStateOf<UpdateResult?>(null) }
    val ctx = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) { updateResult = UpdateChecker.checkForUpdate() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ===== 用户账户 =====
            Text("👤 账户管理", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    uiState.currentUser?.let { user ->
                        // Logged in
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(Icons.Default.Person, null, modifier = Modifier.size(28.dp),
                                        tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(user.nickname.ifBlank { "用户${user.phone.takeLast(4)}" },
                                    fontWeight = FontWeight.Bold)
                                Text(user.phone, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { viewModel.logout() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NonCompliantRed)
                        ) {
                            Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("退出登录")
                        }
                    } ?: run {
                        // Not logged in
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PersonOutline, null, modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text("未登录", fontWeight = FontWeight.Bold)
                                Text("登录后可同步保存项目数据",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.showLogin() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Login, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("登录 / 注册")
                        }
                    }
                }
            }

            // ===== 版本更新 =====
            Text("🔄 版本更新", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Current version
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (updateResult?.hasUpdate == true) Icons.Default.Update else Icons.Default.CheckCircle,
                            null,
                            tint = if (updateResult?.hasUpdate == true) NonCompliantRed else CompliantGreen,
                            modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("当前版本: ${updateResult?.currentVersionName ?: "..."}",
                                fontWeight = FontWeight.Medium)
                            if (updateResult?.hasUpdate == true) {
                                Text("最新版本: ${updateResult?.latestVersionName} 可供更新",
                                    color = NonCompliantRed, fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp)
                            } else {
                                Text("已是最新版本", color = CompliantGreen, fontSize = 13.sp)
                            }
                        }
                    }

                    // Update button with download + install flow
                    var isDownloading by remember { mutableStateOf(false) }
                    var downloadProgress by remember { mutableStateOf(0f) }
                    var downloadComplete by remember { mutableStateOf(false) }
                    var downloadError by remember { mutableStateOf(false) }
                    val scope = rememberCoroutineScope()

                    if (updateResult?.hasUpdate == true) {
                        when {
                            downloadComplete -> {
                                Button(
                                    onClick = { UpdateChecker.installApk(ctx) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = CompliantGreen)
                                ) {
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
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    Text("正在下载更新 (${(downloadProgress * 100).toInt()}%)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            else -> {
                                Button(
                                    onClick = {
                                        isDownloading = true
                                        downloadError = false
                                        downloadProgress = 0f
                                        scope.launch {
                                            val result = UpdateChecker.downloadUpdate(ctx, updateResult?.downloadUrl ?: "") { progress ->
                                                downloadProgress = progress
                                            }
                                            if (result != null) {
                                                downloadProgress = 1f
                                                downloadComplete = true
                                            } else {
                                                downloadError = true
                                            }
                                            isDownloading = false
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = NonCompliantRed)
                                ) {
                                    Icon(Icons.Default.Download, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("下载 ${updateResult?.latestVersionName ?: ""}")
                                }
                            }
                        }

                        if (downloadError) {
                            Card(colors = CardDefaults.cardColors(containerColor = NonCompliantRedBg),
                                modifier = Modifier.fillMaxWidth()) {
                                Text("下载失败，请重试",
                                    modifier = Modifier.padding(12.dp),
                                    color = NonCompliantRed,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = { /* Already latest */ },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false
                        ) {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("已是最新版本")
                        }
                    }

                    // Release notes
                    HorizontalDivider()
                    Text("更新内容", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(updateResult?.releaseNotes ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)

                    // Version history
                    HorizontalDivider()
                    Text("版本历史", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    (updateResult?.versionHistory ?: emptyList<VersionInfo>()).reversed().forEach { v ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Text("${v.versionCode}", fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(v.versionName, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                Text(v.notes, fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2)
                            }
                        }
                    }
                }
            }

            // ===== 关于 =====
            Text("ℹ️ 关于", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsAboutRow(Icons.Default.Info, "应用名称", "建筑声学计算器")
                    SettingsAboutRow(Icons.Default.Code, "版本号", "${UpdateChecker.CURRENT_VERSION_NAME} (Code ${UpdateChecker.CURRENT_VERSION_CODE})")
                    SettingsAboutRow(Icons.Default.Build, "开发框架", "Jetpack Compose + Material 3")
                    SettingsAboutRow(Icons.Default.Storage, "数据引擎", "Room + Hilt DI")
                    SettingsAboutRow(Icons.Default.Star, "数据来源", "GB 50118-2010 / GB/T 50121-2005")
                }
            }

            // Bottom padding
            Spacer(Modifier.height(32.dp))
        }
    }

    // Login dialog
    if (uiState.showLogin) {
        AlertDialog(
            onDismissRequest = viewModel::hideLogin,
            title = {},
            text = { LoginScreen(onLoginSuccess = { viewModel.onLoginSuccess(); viewModel.hideLogin() }) },
            confirmButton = {},
            dismissButton = { TextButton(onClick = viewModel::hideLogin) { Text("关闭") } }
        )
    }
}

@Composable
fun SettingsAboutRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Text(label, modifier = Modifier.width(80.dp), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium, fontSize = 13.sp)
    }
}
