package com.acoustics.calculator.ui.screen.knowledge

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.acoustics.calculator.domain.model.Difficulty
import com.acoustics.calculator.domain.model.KnowledgeArticle
import com.acoustics.calculator.domain.model.KnowledgeBaseData
import com.acoustics.calculator.domain.model.KnowledgeCategory
import com.acoustics.calculator.ui.components.NeonCard
import com.acoustics.calculator.ui.components.ParticleBackground
import com.acoustics.calculator.ui.theme.*

/**
 * V2.0 Knowledge Hub — Learn architectural acoustics from 《实用建筑声学》
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun KnowledgeScreen(navController: NavController) {
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showFavoritesOnly by remember { mutableStateOf(false) }

    val categories = remember { KnowledgeBaseData.categories }
    val allArticles = remember { KnowledgeBaseData.articles }

    val filteredArticles = remember(selectedCategoryId, searchQuery, showFavoritesOnly, allArticles) {
        allArticles.filter { article ->
            val catMatch = selectedCategoryId == null || article.categoryId == selectedCategoryId
            val searchMatch = searchQuery.isBlank() ||
                article.title.contains(searchQuery, ignoreCase = true) ||
                article.summary.contains(searchQuery, ignoreCase = true)
            catMatch && searchMatch
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "knowledgeBg")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 200f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "knowledgeGradient"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("📚 声学知识库",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge.copy(
                            shadow = Shadow(Color.Black.copy(alpha = 0.5f), Offset(1f, 1f), 3f)
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            if (selectedCategoryId != null) {
                FloatingActionButton(
                    onClick = { selectedCategoryId = null },
                    containerColor = NeonPink,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Refresh, "全部显示")
                }
            }
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
            // Particle background
            ParticleBackground()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Header card
                item {
                    NeonCard(
                        glowColor = NeonCyan,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "🎓 从《实用建筑声学》提炼",
                                style = MaterialTheme.typography.titleMedium,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "涵盖声学基础、吸声材料、隔声技术、厅堂音质、噪声控制等8大领域，共${allArticles.size}篇精华知识",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Spacer(Modifier.height(4.dp))
                            // Search bar
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("搜索知识点...", color = Color.White.copy(alpha = 0.5f)) },
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

                            // Difficulty filter chips
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Difficulty.entries.forEach { difficulty ->
                                    FilterChip(
                                        selected = false,
                                        onClick = { },
                                        label = { Text(difficulty.label, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(difficulty.color).copy(alpha = 0.3f),
                                            selectedLabelColor = Color(difficulty.color)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Category chips
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { category ->
                            val isSelected = selectedCategoryId == category.id
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedCategoryId = if (isSelected) null else category.id
                                },
                                label = {
                                    Text(
                                        "${category.icon} ${category.name}",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(category.color).copy(alpha = 0.3f),
                                    selectedLabelColor = Color(category.color),
                                    containerColor = GlassWhite,
                                    labelColor = Color.White.copy(alpha = 0.8f)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (isSelected) Color(category.color).copy(alpha = 0.5f)
                                        else Color.White.copy(alpha = 0.1f),
                                    selectedBorderColor = Color(category.color),
                                    enabled = true,
                                    selected = isSelected
                                )
                            )
                        }
                    }
                }

                // Article count
                item {
                    Text(
                        "共 ${filteredArticles.size} 篇知识",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }

                // Article list
                items(filteredArticles) { article ->
                    ArticleCard(article = article, onClick = {
                        navController.navigate("knowledge_detail/${article.id}")
                    })
                }
            }
        }
    }
}

@Composable
fun ArticleCard(article: KnowledgeArticle, onClick: () -> Unit) {
    val category = remember(article.categoryId) {
        KnowledgeBaseData.categories.find { it.id == article.categoryId }
    }

    val categoryColor = Color(category?.color ?: 0xFF0088FF)
    val difficultyColor = Color(article.difficulty.color)

    val infiniteTransition = rememberInfiniteTransition(label = "articleCard")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "borderGlow"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        categoryColor.copy(alpha = borderAlpha),
                        difficultyColor.copy(alpha = borderAlpha * 0.5f)
                    )
                ),
                shape = RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Category badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = categoryColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        "${category?.icon} ${category?.name}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = categoryColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.weight(1f))
                // Difficulty badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = difficultyColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        article.difficulty.label,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = difficultyColor,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Title
            Text(
                article.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(6.dp))

            // Summary
            Text(
                article.summary,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.65f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Formula count & read more
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (article.formulas.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = NeonYellow.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📐", fontSize = 10.sp)
                            Spacer(Modifier.width(3.dp))
                            Text("${article.formulas.size}公式",
                                color = NeonYellow, fontSize = 10.sp)
                        }
                    }
                    Spacer(Modifier.width(6.dp))
                }
                Text("阅读全文 →",
                    color = categoryColor, fontSize = 12.sp,
                    fontWeight = FontWeight.Medium)
            }
        }
    }
}
