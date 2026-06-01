package com.acoustics.calculator.ui.screen.barrier

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.acoustics.calculator.ui.components.*
import com.acoustics.calculator.ui.theme.*
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarrierScreen(navController: NavController) {
    var sourceHeight by remember { mutableStateOf("") }
    var receiverHeight by remember { mutableStateOf("") }
    var barrierHeight by remember { mutableStateOf("") }
    var srcBarrierDist by remember { mutableStateOf("") }
    var recvBarrierDist by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("1000") }

    var insertionLoss by remember { mutableStateOf<Double?>(null) }
    var fresnelN by remember { mutableStateOf<Double?>(null) }
    var pathDiff by remember { mutableStateOf<Double?>(null) }
    var interpretation by remember { mutableStateOf("") }

    fun calculate() {
        val Hs = sourceHeight.toDoubleOrNull() ?: return
        val Hr = receiverHeight.toDoubleOrNull() ?: return
        val Hb = barrierHeight.toDoubleOrNull() ?: return
        val Dsb = srcBarrierDist.toDoubleOrNull() ?: return
        val Drb = recvBarrierDist.toDoubleOrNull() ?: return
        val f = frequency.toDoubleOrNull() ?: 1000.0

        val c = 343.0
        val lambda = c / f

        // Path difference
        val diffracted = sqrt((Hs - Hb).pow(2) + Dsb.pow(2)) + sqrt((Hr - Hb).pow(2) + Drb.pow(2))
        val direct = sqrt((Hs - Hr).pow(2) + (Dsb + Drb).pow(2))
        val delta = diffracted - direct

        // Fresnel number
        val N = if (lambda > 0) 2.0 * delta / lambda else 0.0

        // Kurze-Anderson formula for barrier insertion loss
        val IL = if (N > 0) {
            val sqrt2PiN = sqrt(2.0 * PI * maxOf(N, 0.001))
            10.0 * log10(3.0 + 20.0 * N)
        } else 0.0

        pathDiff = delta
        fresnelN = N
        insertionLoss = IL.coerceAtLeast(0.0)

        interpretation = when {
            IL > 20 -> "优秀 — 降噪效果显著"
            IL > 15 -> "良好 — 降噪效果明显"
            IL > 10 -> "一般 — 有一定降噪效果"
            IL > 5 -> "有限 — 降噪效果有限"
            else -> "不足 — 屏障高度/位置需调整"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🛡️ 声屏障计算", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "返回", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize()
                .background(Brush.verticalGradient(listOf(BgGradientStart, BgGradientMid, BgGradientEnd)))
        ) {
            ParticleBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NeonCard(glowColor = NeonOrange) {
                    Text("🛡️ 声屏障插入损失计算", color = NeonOrange, fontWeight = FontWeight.Bold)
                    Text("基于Kurze-Anderson公式，预测屏障降噪效果",
                        color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                }

                NeonCard(glowColor = NeonCyan) {
                    Text("📐 几何参数", color = NeonCyan, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = sourceHeight, onValueChange = { sourceHeight = it },
                            label = { Text("声源高(m)", color = Color.White.copy(alpha = 0.5f)) },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f), singleLine = true, colors = neonColors())
                        OutlinedTextField(value = receiverHeight, onValueChange = { receiverHeight = it },
                            label = { Text("接收高(m)", color = Color.White.copy(alpha = 0.5f)) },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f), singleLine = true, colors = neonColors())
                    }
                    OutlinedTextField(value = barrierHeight, onValueChange = { barrierHeight = it },
                        label = { Text("屏障高度 (m)", color = Color.White.copy(alpha = 0.5f)) },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(), singleLine = true, colors = neonColors())
                    OutlinedTextField(value = srcBarrierDist, onValueChange = { srcBarrierDist = it },
                        label = { Text("声源到屏障距离 (m)", color = Color.White.copy(alpha = 0.5f)) },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(), singleLine = true, colors = neonColors())
                    OutlinedTextField(value = recvBarrierDist, onValueChange = { recvBarrierDist = it },
                        label = { Text("屏障到接收点距离 (m)", color = Color.White.copy(alpha = 0.5f)) },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(), singleLine = true, colors = neonColors())
                    OutlinedTextField(value = frequency, onValueChange = { frequency = it },
                        label = { Text("频率 (Hz)", color = Color.White.copy(alpha = 0.5f)) },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(), singleLine = true, colors = neonColors())

                    Spacer(Modifier.height(12.dp))
                    NeonButton(onClick = { calculate() },
                        text = "计算插入损失",
                        icon = { Icon(Icons.Default.Calculate, null) },
                        modifier = Modifier.fillMaxWidth(),
                        gradientColors = listOf(NeonOrange, NeonPurple)
                    )
                }

                insertionLoss?.let { il ->
                    NeonCard(glowColor = NeonGreen) {
                        Text("📊 计算结果", color = NeonGreen, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${"%.1f".format(il)} dB",
                                    fontWeight = FontWeight.Bold, color = NeonGreen, fontSize = 28.sp)
                                Text("插入损失", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${"%.3f".format(fresnelN ?: 0.0)}",
                                    fontWeight = FontWeight.Bold, color = NeonCyan, fontSize = 20.sp)
                                Text("菲涅尔数N", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${"%.3f".format((pathDiff ?: 0.0) * 1000)} mm",
                                    fontWeight = FontWeight.Bold, color = NeonYellow, fontSize = 16.sp)
                                Text("声程差", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when {
                                il > 15 -> NeonGreen.copy(alpha = 0.15f)
                                il > 10 -> NeonYellow.copy(alpha = 0.15f)
                                else -> NeonRed.copy(alpha = 0.15f)
                            }
                        ) {
                            Text(interpretation,
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                color = Color.White, fontSize = 13.sp)
                        }
                    }

                    // Kurze-Anderson formula display
                    NeonCard(glowColor = NeonPurple) {
                        Text("📐 计算公式", color = NeonPurple, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text("IL = 10·lg(3 + 20N)  [Kurze-Anderson]",
                            color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("N = 2δ/λ  (δ为声程差，λ为波长)",
                            color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun neonColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
    focusedBorderColor = NeonCyan, unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
    cursorColor = NeonCyan, focusedLabelColor = NeonCyan,
    unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
)
