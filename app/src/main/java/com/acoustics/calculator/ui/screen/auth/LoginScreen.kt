package com.acoustics.calculator.ui.screen.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.acoustics.calculator.ui.components.ParticleBackground
import com.acoustics.calculator.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var code by remember { mutableStateOf("") }
    var countdown by remember { mutableStateOf(0) }

    // Auto-navigate on login success
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) onLoginSuccess()
    }

    // Countdown timer for resend
    LaunchedEffect(countdown) {
        if (countdown > 0) {
            delay(1000)
            countdown--
        }
    }

    // Animations
    val infiniteTransition = rememberInfiniteTransition(label = "loginAnim")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "loginGlow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgGradientStart, BgGradientMid, BgGradientEnd)))
    ) {
        // Particle background
        ParticleBackground()

        // Decorative circles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height
            drawCircle(color = NeonCyan.copy(alpha = 0.03f), radius = w * 0.7f, center = Offset(w * 0.8f, -h * 0.1f))
            drawCircle(color = NeonPurple.copy(alpha = 0.03f), radius = w * 0.5f, center = Offset(-w * 0.2f, h * 0.5f))
            drawCircle(color = NeonPink.copy(alpha = 0.02f), radius = w * 0.4f, center = Offset(w * 0.5f, h * 0.8f))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(40.dp))

            // App icon
            Surface(
                shape = CircleShape,
                color = NeonCyan.copy(alpha = 0.15f),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🔊", fontSize = 36.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Title
            Text(
                "建筑声学计算器",
                style = MaterialTheme.typography.headlineMedium.copy(
                    shadow = Shadow(NeonCyan.copy(alpha = glowAlpha), Offset(1f, 1f), 8f),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                "登录后享受完整功能体验",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp
            )

            Spacer(Modifier.height(32.dp))

            // Login card
            Card(
                modifier = Modifier.fillMaxWidth()
                    .border(
                        1.dp, NeonCyan.copy(alpha = glowAlpha * 0.5f),
                        RoundedCornerShape(20.dp)
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Phone number input
                    OutlinedTextField(
                        value = uiState.phone,
                        onValueChange = viewModel::updatePhone,
                        label = { Text("手机号码", color = Color.White.copy(alpha = 0.5f)) },
                        leadingIcon = { Icon(Icons.Default.Phone, null, tint = NeonCyan) },
                        placeholder = { Text("请输入11位手机号", color = Color.White.copy(alpha = 0.3f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Verification code with send button
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        OutlinedTextField(
                            value = code,
                            onValueChange = { if (it.length <= 6) code = it },
                            label = { Text("验证码", color = Color.White.copy(alpha = 0.5f)) },
                            leadingIcon = { Icon(Icons.Default.Lock, null, tint = NeonCyan) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = textFieldColors(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Button(
                            onClick = {
                                viewModel.sendCode()
                                countdown = 60
                            },
                            modifier = Modifier.height(56.dp),
                            enabled = uiState.phone.length >= 11 && countdown == 0,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonCyan,
                                disabledContainerColor = NeonCyan.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                if (countdown > 0) "${countdown}s" else "获取验证码",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Code sent message
                    uiState.codeSent?.let {
                        Text(it, color = NeonGreen, fontSize = 12.sp)
                    }

                    Spacer(Modifier.height(4.dp))

                    // Login button
                    Button(
                        onClick = { viewModel.loginWithCode(code) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = uiState.phone.length >= 11 && code.length >= 4 && !uiState.isLoading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            disabledContainerColor = NeonCyan.copy(alpha = 0.3f)
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Login, null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("登录", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }
                    }

                    // Error message
                    uiState.error?.let { error ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = NeonRed.copy(alpha = 0.1f)
                        ) {
                            Text(
                                error,
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                color = NeonRed,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    // Welcome message
                    uiState.loginMessage?.let { msg ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = NeonGreen.copy(alpha = 0.1f)
                        ) {
                            Text(
                                msg,
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                color = NeonGreen,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Auto-login hint
            uiState.hasSavedSession?.let {
                Text(
                    "检测到已保存的登录状态，正在自动登录...",
                    color = NeonCyan.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }

            // Version info
            Spacer(Modifier.height(24.dp))
            Text(
                "${com.acoustics.calculator.core.constants.AppVersion.DISPLAY} | 《实用建筑声学》数字化",
                color = Color.White.copy(alpha = 0.2f),
                fontSize = 11.sp
            )

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = NeonCyan,
    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
    cursorColor = NeonCyan,
    focusedLabelColor = NeonCyan,
    unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
    focusedLeadingIconColor = NeonCyan,
    unfocusedLeadingIconColor = Color.White.copy(alpha = 0.5f)
)
