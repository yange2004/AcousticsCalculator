package com.acoustics.calculator.ui.screen.standards

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.acoustics.calculator.domain.model.StandardInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardsListScreen(
    navController: NavController,
    viewModel: StandardsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("标准查询", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(horizontal = 16.dp)
        ) {
            // 🔍 Search bar
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                placeholder = { Text("搜索标准编号、名称、建筑类型...") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Close, "清除")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (uiState.searchQuery.isNotEmpty()) "找到 ${uiState.filteredStandards.size} 条结果"
                else "共 ${uiState.allStandards.size} 条标准",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredStandards.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Text("未找到匹配标准", style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val grouped = uiState.filteredStandards.groupBy { it.standardCode }
                    grouped.forEach { (code, standards) ->
                        item(key = code) {
                            StandardGroupCard(code = code, nameZh = standards.firstOrNull()?.nameZh ?: "",
                                standards = standards)
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
fun StandardGroupCard(code: String, nameZh: String, standards: List<StandardInfo>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Default.MenuBook, null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(code, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(nameZh, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            // Group by category within each standard
            val byCategory = standards.groupBy { it.category }
            byCategory.forEach { (category, items) ->
                val categoryIcon = when (category) {
                    "STC" -> Icons.Default.Shield
                    "IMPACT" -> Icons.Default.Speaker
                    "NOISE" -> Icons.Default.VolumeDown
                    "RT60" -> Icons.Default.Timer
                    "GRADE" -> Icons.Default.Star
                    "ABSORPTION" -> Icons.Default.AudioFile
                    else -> Icons.Default.Info
                }
                Text(categoryLabel(category), fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                items.forEach { std ->
                    Row(modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 2.dp)) {
                        Icon(categoryIcon, null, modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.width(6.dp))
                        Column {
                            Text("${std.roomType}", fontWeight = FontWeight.Medium,
                                fontSize = 13.sp)
                            val detail = buildDetailString(std)
                            if (detail.isNotBlank()) {
                                Text(detail, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp)
                            }
                            if (std.notes.isNotBlank()) {
                                Text(std.notes, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp, maxLines = 2)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

private fun categoryLabel(category: String): String = when (category) {
    "STC" -> "🔇 空气声隔声"
    "IMPACT" -> "🔨 撞击声隔声"
    "NOISE" -> "🔊 噪声限值"
    "RT60" -> "⏱ 混响时间"
    "GRADE" -> "⭐ 性能分级"
    "ABSORPTION" -> "🎵 吸声要求"
    else -> "📋 $category"
}

private fun buildDetailString(std: StandardInfo): String {
    val parts = mutableListOf<String>()
    std.minStc?.let { parts.add("STC ≥ ${it.toInt()}dB") }
    std.maxNoiseLevelDb?.let { parts.add("≤ ${it.toInt()}dB(A)") }
    std.optimalRt60Min?.let { min ->
        std.optimalRt60Max?.let { max ->
            parts.add("RT60: $min~${max}s")
        }
    }
    return parts.joinToString(" | ")
}
