package com.acoustics.calculator.ui.screen.insulations

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.acoustics.calculator.domain.model.WallLayer
import com.acoustics.calculator.ui.components.FrequencyChart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsulationScreen(
    navController: NavController,
    projectId: Long,
    viewModel: InsulationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("隔声计算") },
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
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("墙体构造", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            // Layer list
            uiState.layers.forEachIndexed { index, layer ->
                WallLayerCard(
                    index = index,
                    layer = layer,
                    onRemove = { viewModel.removeLayer(index) }
                )
            }

            // Add layer
            OutlinedButton(
                onClick = { viewModel.showAddLayerDialog(true) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("添加材料层")
            }

            // Cavity & insulation options
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("空腔深度 (mm):", modifier = Modifier.width(120.dp))
                OutlinedTextField(
                    value = uiState.cavityDepthMm,
                    onValueChange = viewModel::updateCavityDepth,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = uiState.hasInsulationFill, onCheckedChange = { viewModel.toggleInsulationFill() })
                Text("空腔填充吸声棉")
            }

            // Mass law quick check
            if (uiState.layers.isNotEmpty()) {
                val totalMass = uiState.layers.sumOf { it.massPerUnitAreaKgm2 }
                Text("总面密度: ${"%.1f".format(totalMass)} kg/m²", style = MaterialTheme.typography.bodyMedium)
            }

            HorizontalDivider()

            // Calculate
            Button(
                onClick = { viewModel.calculate() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = uiState.layers.isNotEmpty()
            ) {
                Icon(Icons.Default.Shield, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("计算隔声量")
            }

            // Results
            uiState.result?.let { result ->
                HorizontalDivider()
                Text("计算结果", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("计权隔声量 Rw/STC: ${"%.0f".format(result.stc)} dB", fontWeight = FontWeight.Bold)
                        Text("等级: ${result.stcRating}", color = MaterialTheme.colorScheme.primary)
                        if (result.isComposite) {
                            Text("空腔: ${result.cavityDepthMm} mm")
                            Text("填充: ${result.insulationMaterial ?: "无"}")
                        }
                    }
                }

                FrequencyChart(
                    data = result.transmissionLossByBand,
                    yAxisLabel = "隔声量 R (dB)",
                    title = "隔声量频率曲线"
                )
            }
        }
    }

    // Add layer dialog
    if (uiState.showAddLayerDialog) {
        AddLayerDialog(
            material = uiState.newLayerMaterial,
            thickness = uiState.newLayerThickness,
            density = uiState.newLayerDensity,
            onMaterialChange = viewModel::updateNewLayerMaterial,
            onThicknessChange = viewModel::updateNewLayerThickness,
            onDensityChange = viewModel::updateNewLayerDensity,
            onAdd = viewModel::addLayer,
            onDismiss = { viewModel.showAddLayerDialog(false) }
        )
    }
}

@Composable
fun WallLayerCard(index: Int, layer: WallLayer, onRemove: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("层 ${index + 1}: ${layer.material}", fontWeight = FontWeight.SemiBold)
                Text("${layer.thicknessMm} mm | ${layer.densityKgm3} kg/m³ | ${"%.1f".format(layer.massPerUnitAreaKgm2)} kg/m²",
                    style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, "删除", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AddLayerDialog(
    material: String, thickness: String, density: String,
    onMaterialChange: (String) -> Unit,
    onThicknessChange: (String) -> Unit,
    onDensityChange: (String) -> Unit,
    onAdd: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加材料层") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = material, onValueChange = onMaterialChange,
                    label = { Text("材料名称") }, singleLine = true)
                OutlinedTextField(value = thickness, onValueChange = onThicknessChange,
                    label = { Text("厚度 (mm)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                OutlinedTextField(value = density, onValueChange = onDensityChange,
                    label = { Text("密度 (kg/m³)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
            }
        },
        confirmButton = { TextButton(onClick = onAdd) { Text("添加") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
