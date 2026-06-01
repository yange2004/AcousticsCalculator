package com.acoustics.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.acoustics.calculator.ui.navigation.AcousticNavGraph
import com.acoustics.calculator.ui.screen.auth.LoginScreen
import com.acoustics.calculator.ui.screen.auth.LoginViewModel
import com.acoustics.calculator.ui.theme.AcousticTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AcousticTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppGate()
                }
            }
        }
    }
}

/**
 * 登录门禁：未登录只能看到登录页，登录后才能进入主页
 */
@Composable
fun AppGate(viewModel: LoginViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    // 正在检查登录状态
    if (uiState.hasSavedSession == null) {
        // 显示启动画面或空白（短暂等待 Room 查询）
        return
    }

    if (uiState.isLoggedIn) {
        // 已登录 → 进入主应用
        AcousticNavGraph()
    } else {
        // 未登录 → 显示登录页
        LoginScreen(
            onLoginSuccess = { /* isLoggedIn 变为 true 后自动切换 */ }
        )
    }
}
