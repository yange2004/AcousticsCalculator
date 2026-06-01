package com.acoustics.calculator.ui.screen.hvac

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.acoustics.calculator.ui.components.*
import com.acoustics.calculator.ui.theme.*
import kotlin.math.log10

/**
 * HVAC/Duct noise calculation result.
 */
data class HvacNoiseResult(
    val ductArea: Double,
    val velocity: Double,
    val regeneratedNoise: Double,
    val totalAttenuation: Double,
    val finalLevel: Double
)

/**
 * V2.0 — HVAC / Duct Noise Calculator
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HvacScreen(navController: NavController) {
    var ductLength by remember { mutableStateOf("") }
    var ductWidth by remember { mutableStateOf("") }
    var ductHeight by remember { mutableStateOf("") }
    var airVelocity by remember { mutableStateOf("") }
    var fanSoundPower by remember { mutableStateOf("") }

    var result by remember { mutableStateOf<HvacNoiseResult?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🌬️ HVAC噪声计算", fontWeight = FontWeight.Bold, color = Color.White) },
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
                NeonCard(glowColor = NeonCyan) {
                    Text("🌬️ 暖通空调噪声预估", color = NeonCyan, fontWeight = FontWeight.Bold)
                    Text("估算风管系统再生噪声与衰减量",
                        color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                }

                NeonCard(glowColor = NeonOrange) {
                    Text("📐 风管参数", color = NeonOrange, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(value = ductLength, onValueChange = { ductLength = it },
                        label = { Text("风管长度 (m)", color = Color.White.copy(alpha = 0.5f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(), singleLine = true, colors = hnc())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = ductWidth, onValueChange = { ductWidth = it },
                            label = { Text("宽 (m)", color = Color.White.copy(alpha = 0.5f)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f), singleLine = true, colors = hnc())
                        OutlinedTextField(value = ductHeight, onValueChange = { ductHeight = it },
                            label = { Text("高 (m)", color = Color.White.copy(alpha = 0.5f)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f), singleLine = true, colors = hnc())
                    }
                    OutlinedTextField(value = airVelocity, onValueChange = { airVelocity = it },
                        label = { Text("风速 (m/s)", color = Color.White.copy(alpha = 0.5f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(), singleLine = true, colors = hnc())
                    OutlinedTextField(value = fanSoundPower, onValueChange = { fanSoundPower = it },
                        label = { Text("风机声功率级 (dB)，默认80", color = Color.White.copy(alpha = 0.5f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(), singleLine = true, colors = hnc())

                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val L = ductLength.toDoubleOrNull() ?: return@Button
                            val W = ductWidth.toDoubleOrNull() ?: return@Button
                            val H = ductHeight.toDoubleOrNull() ?: return@Button
                            val v = airVelocity.toDoubleOrNull() ?: return@Button
                            val Lw = fanSoundPower.toDoubleOrNull() ?: 80.0
                            val area = W * H
                            val L_reg = 10 + 20 * log10(v.coerceAtLeast(1.0)) + 10 * log10(area.coerceAtLeast(0.01))
                            val ductAtten = 0.3 * L
                            val totalAtten = ductAtten + 3.0
                            val finalLevel = maxOf(0.0, Lw - totalAtten)
                            result = HvacNoiseResult(area, v, L_reg, totalAtten, finalLevel)
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonOrange)
                    ) {
                        Icon(Icons.Default.Calculate, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("计算噪声", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                result?.let { r ->
                    NeonCard(glowColor = NeonGreen) {
                        Text("📊 计算结果", color = NeonGreen, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${"%.1f".format(r.finalLevel)} dB",
                                    fontWeight = FontWeight.Bold, color = if (r.finalLevel > 50) NeonRed else NeonGreen,
                                    fontSize = 28.sp)
                                Text("末端噪声级", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${"%.1f".format(r.totalAttenuation)} dB",
                                    fontWeight = FontWeight.Bold, color = NeonCyan, fontSize = 20.sp)
                                Text("总衰减量", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${"%.1f".format(r.regeneratedNoise)} dB",
                                    fontWeight = FontWeight.Bold, color = NeonYellow, fontSize = 20.sp)
                                Text("再生噪声", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        val nrRating = ((r.finalLevel - 5.0) / 5.0).toInt() * 5
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (nrRating <= 30) NeonGreen.copy(alpha = 0.15f) else NeonRed.copy(alpha = 0.15f)
                        ) {
                            Text(
                                if (nrRating <= 30) "✅ 满足 NR-$nrRating 标准要求（推荐NR≤30）"
                                else "⚠️ 噪声级偏高（NR-$nrRating），建议加装消声器",
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                color = Color.White, fontSize = 13.sp
                            )
                        }
                    }

                    NeonCard(glowColor = NeonPurple) {
                        Text("💡 建议", color = NeonPurple, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        HvacSuggestions(r)
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun HvacSuggestions(r: HvacNoiseResult) {
    Column {
        if (r.velocity > 8.0) {
            Text("• 风速偏高（>8m/s），建议降低风速以减小再生噪声",
                color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, lineHeight = 20.sp)
        }
        if (r.ductArea < 0.1) {
            Text("• 风管截面积偏小，建议增大尺寸",
                color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, lineHeight = 20.sp)
        }
        if (r.finalLevel > 50) {
            Text("• 末端噪声超过50dB，建议增设消声器",
                color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, lineHeight = 20.sp)
        }
        Text("• 弯头处加导流叶片可降低湍流噪声",
            color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, lineHeight = 20.sp)
        Text("• 风机与风管间采用软连接可隔振",
            color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, lineHeight = 20.sp)
    }
}

@Composable
private fun hnc() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
    focusedBorderColor = NeonCyan, unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
    cursorColor = NeonCyan, focusedLabelColor = NeonCyan,
    unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
)
