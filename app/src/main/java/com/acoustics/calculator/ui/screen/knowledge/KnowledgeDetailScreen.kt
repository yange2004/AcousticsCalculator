package com.acoustics.calculator.ui.screen.knowledge

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.acoustics.calculator.domain.model.*
import com.acoustics.calculator.ui.components.*
import com.acoustics.calculator.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeDetailScreen(
    navController: NavController,
    article: KnowledgeArticle?
) {
    if (article == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(BgGradientStart),
            contentAlignment = Alignment.Center
        ) {
            Text("知识未找到", color = Color.White)
        }
        return
    }

    val category = remember(article.categoryId) {
        KnowledgeBaseData.categories.find { it.id == article.categoryId }
    }
    val accentColor = Color(category?.color ?: 0xFF0088FF)
    val scrollState = rememberScrollState()

    val infiniteTransition = rememberInfiniteTransition(label = "detailBg")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "shimmer"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(article.title, maxLines = 1,
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header card
                NeonCard(glowColor = accentColor) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = accentColor.copy(alpha = 0.2f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(category?.icon ?: "📚", fontSize = 24.sp)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(category?.name ?: "", color = accentColor, fontSize = 13.sp)
                            Text(article.title, color = Color.White,
                                fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(article.difficulty.color).copy(alpha = 0.15f)
                        ) {
                            Text(article.difficulty.label,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = Color(article.difficulty.color), fontSize = 11.sp)
                        }
                    }
                }

                // Article content
                NeonCard(glowColor = accentColor.copy(alpha = 0.5f)) {
                    Text(
                        text = article.content,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 15.sp,
                        lineHeight = 24.sp
                    )
                }

                // Formulas section
                if (article.formulas.isNotEmpty()) {
                    NeonCard(glowColor = NeonYellow) {
                        Text("📐 公式",
                            color = NeonYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp)
                        Spacer(Modifier.height(12.dp))

                        article.formulas.forEachIndexed { index, formula ->
                            FormulaCard(formula)
                            if (index < article.formulas.lastIndex) {
                                Spacer(Modifier.height(8.dp))
                                HorizontalDivider(color = GlassWhite)
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }

                // If it's a design example, show related standards
                if (article.relatedStandards.isNotEmpty()) {
                    NeonCard(glowColor = NeonGreen) {
                        Text("📋 相关标准", color = NeonGreen, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        article.relatedStandards.forEach { standard ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = GlassWhite
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Description, null,
                                        tint = NeonGreen, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(standard, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                // Navigation hint
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("📖 选自《实用建筑声学》项端新 编著",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center)
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun FormulaCard(formula: KnowledgeFormula) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceMedium),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Formula name
            Text(formula.name, fontWeight = FontWeight.Bold,
                color = NeonYellow, fontSize = 14.sp)

            Spacer(Modifier.height(6.dp))

            // Formula display — clean readable text
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF0A0A1A)
            ) {
                Text(
                    formula.latexExpression,
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    color = NeonCyan,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(6.dp))

            // Description
            Text(formula.description,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp)

            // Variables
            if (formula.variables.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(Modifier.height(6.dp))
                formula.variables.forEach { v ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = NeonCyan.copy(alpha = 0.15f)
                        ) {
                            Text(v.symbol,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = NeonCyan, fontSize = 12.sp,
                                fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(v.name, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp,
                            modifier = Modifier.weight(1f))
                        Text(v.unit, color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
