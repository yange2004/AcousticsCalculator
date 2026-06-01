package com.acoustics.calculator.ui.screen.roommode

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.acoustics.calculator.domain.engine.RoomModeEngine
import com.acoustics.calculator.ui.components.*
import com.acoustics.calculator.ui.theme.*

/**
 * V2.0 — Room Mode (Standing Wave) Calculator
 * Based on 《实用建筑声学》 Chapter 1 — Modal Analysis
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomModeScreen(navController: NavController) {
    val engine = remember { RoomModeEngine() }

    var width by remember { mutableStateOf("") }
    var length by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var maxFreq by remember { mutableStateOf("200") }

    var result by remember { mutableStateOf<RoomModeEngine.RoomModeResult?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🎵 房间模式计算", fontWeight = FontWeight.Bold, color = Color.White) },
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
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(BgGradientStart, BgGradientMid, BgGradientEnd)))
        ) {
            ParticleBackground()

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Info card
                item {
                    NeonCard(glowColor = NeonCyan) {
                        Text("🎵 房间驻波模式分析", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("分析房间的轴向、切向和斜向共振模式，帮助识别有害驻波",
                            color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                    }
                }

                // Input card
                item {
                    NeonCard(glowColor = NeonPurple) {
                        Text("📐 房间尺寸", color = NeonPurple, fontWeight = FontWeight.Bold)

                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = width, onValueChange = { width = it },
                                label = { Text("宽度 (m)", color = Color.White.copy(alpha = 0.5f)) },
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f), singleLine = true,
                                colors = neonTextFieldColors()
                            )
                            OutlinedTextField(
                                value = length, onValueChange = { length = it },
                                label = { Text("长度 (m)", color = Color.White.copy(alpha = 0.5f)) },
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f), singleLine = true,
                                colors = neonTextFieldColors()
                            )
                            OutlinedTextField(
                                value = height, onValueChange = { height = it },
                                label = { Text("高度 (m)", color = Color.White.copy(alpha = 0.5f)) },
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f), singleLine = true,
                                colors = neonTextFieldColors()
                            )
                        }

                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = maxFreq, onValueChange = { maxFreq = it },
                            label = { Text("最高频率 (Hz)", color = Color.White.copy(alpha = 0.5f)) },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(), singleLine = true,
                            colors = neonTextFieldColors()
                        )

                        Spacer(Modifier.height(12.dp))
                        NeonButton(
                            onClick = {
                                val w = width.toDoubleOrNull() ?: return@NeonButton
                                val l = length.toDoubleOrNull() ?: return@NeonButton
                                val h = height.toDoubleOrNull() ?: return@NeonButton
                                val f = maxFreq.toDoubleOrNull() ?: 200.0
                                result = engine.calculate(w, l, h, f)
                            },
                            text = "计算房间模式",
                            icon = { Icon(Icons.Default.Calculate, null) },
                            modifier = Modifier.fillMaxWidth(),
                            gradientColors = listOf(NeonPurple, NeonCyan)
                        )
                    }
                }

                // Results
                result?.let { r ->
                    // Summary card
                    item {
                        NeonCard(glowColor = NeonGreen) {
                            Text("📊 分析结果", color = NeonGreen, fontWeight = FontWeight.Bold)

                            Spacer(Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                StatItem("体积", "${"%.1f".format(r.volumeM3)} m³", NeonCyan)
                                StatItem("模式数", "${r.allModes.size}", NeonPink)
                                StatItem("Schröder", "${"%.0f".format(r.schroederFrequency)} Hz", NeonYellow)
                            }

                            Spacer(Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                StatItem("轴向", "${r.axialModes.size}", NeonRed)
                                StatItem("切向", "${r.tangentialModes.size}", NeonOrange)
                                StatItem("斜向", "${r.obliqueModes.size}", NeonPurple)
                            }

                            Spacer(Modifier.height(8.dp))

                            // Room ratio score
                            val score = engine.evaluateRatioScore(r.widthM, r.lengthM, r.heightM)
                            val scoreColor = when {
                                score >= 0.7 -> NeonGreen
                                score >= 0.4 -> NeonYellow
                                else -> NeonRed
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = scoreColor.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("房间比例评分", color = scoreColor, fontSize = 13.sp)
                                    Text("${"%.0f".format(score * 100)}/100",
                                        color = scoreColor, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (!r.goodRatio) {
                                Spacer(Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = NeonYellow.copy(alpha = 0.1f)
                                ) {
                                    Text("⚠️ 建议避免整数比尺寸（推荐 1:1.14:1.39 或 1:1.26:1.59）",
                                        modifier = Modifier.padding(10.dp),
                                        color = NeonYellow, fontSize = 12.sp)
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            Text("推荐比例：", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                            engine.bestRatios().forEach { (a, b, c) ->
                                Text("  $a : $b : $c",
                                    color = NeonCyan.copy(alpha = 0.7f), fontSize = 12.sp)
                            }
                        }
                    }

                    // Mode list by type
                    item {
                        NeonCard(glowColor = NeonRed) {
                            Text("🔴 轴向模式（最强）", color = NeonRed, fontWeight = FontWeight.Bold)
                            Text("能量最强，最易感知的驻波",
                                color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                            Spacer(Modifier.height(6.dp))

                            r.axialModes.take(10).forEach { mode ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("(${mode.nx},${mode.ny},${mode.nz})",
                                        color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                                    Text("${"%.1f".format(mode.frequencyHz)} Hz",
                                        color = NeonRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    item {
                        NeonCard(glowColor = NeonOrange) {
                            Text("🟠 切向模式", color = NeonOrange, fontWeight = FontWeight.Bold)
                            Text("能量中等",
                                color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                            Spacer(Modifier.height(6.dp))

                            r.tangentialModes.take(10).forEach { mode ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("(${mode.nx},${mode.ny},${mode.nz})",
                                        color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                                    Text("${"%.1f".format(mode.frequencyHz)} Hz",
                                        color = NeonOrange, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, color = color, fontSize = 18.sp)
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
    }
}

@Composable
private fun neonTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = NeonCyan,
    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
    cursorColor = NeonCyan,
    focusedLabelColor = NeonCyan,
    unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
)
