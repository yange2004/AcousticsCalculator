package com.acoustics.calculator.ui.screen.examples

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.acoustics.calculator.domain.model.DesignExample
import com.acoustics.calculator.domain.model.KnowledgeBaseData
import com.acoustics.calculator.ui.components.*
import com.acoustics.calculator.ui.theme.*

/**
 * V2.0 — Design Examples from 《实用建筑声学》 120+ cases
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExampleListScreen(navController: NavController) {
    val examples = remember { KnowledgeBaseData.designExamples }
    var selectedFilter by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val buildingTypes = remember(examples) {
        examples.map { it.buildingType }.distinct()
    }

    val filtered = remember(examples, selectedFilter, searchQuery) {
        examples.filter { e ->
            val typeMatch = selectedFilter == null || e.buildingType == selectedFilter
            val searchMatch = searchQuery.isBlank() ||
                e.title.contains(searchQuery, ignoreCase = true) ||
                e.buildingType.contains(searchQuery, ignoreCase = true) ||
                e.location.contains(searchQuery, ignoreCase = true)
            typeMatch && searchMatch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("📐 设计实例",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge.copy(
                            shadow = Shadow(Color.Black.copy(alpha = 0.5f), Offset(1f, 1f), 3f)
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "返回", tint = Color.White)
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

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Header
                item {
                    NeonCard(glowColor = NeonCyan) {
                        Text("🏗️ 《实用建筑声学》设计案例集",
                            color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("涵盖${examples.size}个国内外经典声学设计案例",
                            color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)

                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("搜索案例...", color = Color.White.copy(alpha = 0.5f)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                cursorColor = NeonCyan
                            ),
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.5f)) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Type filters
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf<String?>(null) + buildingTypes) { type ->
                            val isSelected = selectedFilter == type
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFilter = type },
                                label = { Text(type ?: "全部", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                                    selectedLabelColor = NeonCyan,
                                    containerColor = GlassWhite,
                                    labelColor = Color.White.copy(alpha = 0.8f)
                                )
                            )
                        }
                    }
                }

                item {
                    Text("共 ${filtered.size} 个案例",
                        color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                }

                // Example cards
                items(filtered) { example ->
                    ExampleCard(example = example, onClick = {
                        navController.navigate("example_detail/${example.id}")
                    })
                }
            }
        }
    }
}

@Composable
fun ExampleCard(example: DesignExample, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "exampleCard")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "borderGlowEx"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        NeonCyan.copy(alpha = borderAlpha),
                        NeonPurple.copy(alpha = borderAlpha * 0.5f)
                    )
                ),
                shape = RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Building type badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonPink.copy(alpha = 0.15f)
                ) {
                    Text(example.buildingType,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = NeonPink, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonCyan.copy(alpha = 0.15f)
                ) {
                    Text("📍${example.location}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = NeonCyan, fontSize = 11.sp)
                }
                Spacer(Modifier.weight(1f))
                Text(example.year,
                    color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
            }

            Spacer(Modifier.height(10.dp))

            Text(example.title,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 16.sp)

            Spacer(Modifier.height(6.dp))

            Text(example.description,
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 13.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis)

            if (example.keyParameters.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                // Wrap parameters
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val entries = example.keyParameters.entries.take(4).toList()
                    val rows = entries.size / 2 + if (entries.size % 2 != 0) 1 else 0
                    for (rowIdx in 0 until rows) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val start = rowIdx * 2
                            val end = minOf(start + 2, entries.size)
                            for (i in start until end) {
                                val (key, value) = entries[i]
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = GlassWhite
                                ) {
                                    Text("$key: $value",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        color = NeonYellow, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            if (example.tags.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row {
                    example.tags.take(3).forEach { tag ->
                        Text("#$tag  ",
                            color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
