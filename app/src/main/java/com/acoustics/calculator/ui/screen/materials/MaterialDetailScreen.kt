package com.acoustics.calculator.ui.screen.materials

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.acoustics.calculator.ui.components.FrequencyChart
import com.acoustics.calculator.ui.components.OctaveBandTable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialDetailScreen(
    navController: NavController,
    materialId: Long,
    viewModel: MaterialDetailViewModel = hiltViewModel()
) {
    val material by viewModel.material.collectAsState()

    LaunchedEffect(materialId) { viewModel.loadMaterial(materialId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(material?.nameZh ?: "材料详情") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    material?.let { mat ->
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                Icons.Default.Star,
                                "收藏",
                                tint = if (mat.isFavorite) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
        material?.let { mat ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Basic info
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(mat.nameZh, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(mat.nameEn, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (mat.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(mat.description, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // Properties
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("物理参数", fontWeight = FontWeight.Bold)
                        mat.densityKgm3?.let {
                            Text("密度: $it kg/m³")
                        }
                        mat.thicknessMm?.let {
                            Text("厚度: $it mm")
                        }
                        Text("NRC (降噪系数): ${"%.2f".format(mat.nrc)}")
                        Text("平均吸声系数: ${"%.3f".format(mat.averageAbsorption)}")
                        if (mat.source.isNotBlank()) {
                            Text("数据来源: ${mat.source}", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Absorption chart
                FrequencyChart(
                    data = mat.absorption,
                    yAxisLabel = "吸声系数 α",
                    title = "${mat.nameZh} - 吸声系数频谱"
                )

                // Absorption table
                OctaveBandTable(
                    values = mat.absorption,
                    label = "吸声系数",
                    decimals = 2
                )
            }
        } ?: run {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}
