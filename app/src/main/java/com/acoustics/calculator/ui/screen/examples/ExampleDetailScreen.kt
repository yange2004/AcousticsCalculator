package com.acoustics.calculator.ui.screen.examples

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.acoustics.calculator.domain.model.DesignExample
import com.acoustics.calculator.ui.components.*
import com.acoustics.calculator.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExampleDetailScreen(
    navController: NavController,
    example: DesignExample?
) {
    if (example == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(BgGradientStart),
            contentAlignment = Alignment.Center
        ) {
            Text("案例未找到", color = Color.White)
        }
        return
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(example.title, maxLines = 1,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(
                            shadow = Shadow(Color.Black.copy(alpha = 0.5f), Offset(1f, 1f), 2f)
                        ))
                },
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
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header card
                NeonCard(glowColor = NeonCyan) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = NeonPink.copy(alpha = 0.2f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🏗️", fontSize = 28.sp)
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(example.title, color = Color.White,
                                fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("${example.buildingType} · ${example.location} · ${example.year}",
                                color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                        }
                    }

                    if (example.architect.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text("设计：${example.architect}",
                            color = NeonCyan.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                }

                // Description
                NeonCard(glowColor = NeonPurple.copy(alpha = 0.5f)) {
                    Text("📋 概况", color = NeonPurple, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(example.description, color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp, lineHeight = 22.sp)
                }

                // Key Parameters
                if (example.keyParameters.isNotEmpty()) {
                    NeonCard(glowColor = NeonYellow) {
                        Text("📊 关键参数", color = NeonYellow, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(Modifier.height(10.dp))
                        example.keyParameters.entries.forEach { (key, value) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(key, color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = NeonYellow.copy(alpha = 0.15f)
                                ) {
                                    Text(value,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                        color = NeonYellow, fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Design Highlights
                if (example.designHighlights.isNotEmpty()) {
                    NeonCard(glowColor = NeonGreen) {
                        Text("✨ 设计亮点", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(Modifier.height(8.dp))
                        example.designHighlights.forEachIndexed { i, highlight ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = NeonGreen.copy(alpha = 0.2f),
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("${i + 1}",
                                            color = NeonGreen, fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(highlight, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                            }
                        }
                    }
                }

                // Tags
                if (example.tags.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        example.tags.forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = GlassWhite
                            ) {
                                Text("#$tag",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Source reference
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("📖 案例选自《实用建筑声学》项端新 编著",
                        color = Color.White.copy(alpha = 0.3f), fontSize = 11.sp)
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}
