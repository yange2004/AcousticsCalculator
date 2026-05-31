package com.acoustics.calculator.ui.screen.roomacoustics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.acoustics.calculator.domain.model.ReverberationFormula
import com.acoustics.calculator.domain.model.SurfaceType
import com.acoustics.calculator.ui.components.FrequencyChart
import com.acoustics.calculator.ui.components.OctaveBandTable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomAcousticsScreen(
    navController: NavController,
    projectId: Long,
    viewModel: RoomAcousticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("室内声学计算") },
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
            // --- Room Dimensions ---
            Text("房间尺寸", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.widthM,
                    onValueChange = viewModel::updateWidth,
                    label = { Text("宽度 (m)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.lengthM,
                    onValueChange = viewModel::updateLength,
                    label = { Text("长度 (m)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.heightM,
                    onValueChange = viewModel::updateHeight,
                    label = { Text("高度 (m)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Derived values
            viewModel.getRoomDimensions()?.let { room ->
                Text(
                    "体积: ${"%.1f".format(room.volumeM3)} m³ | 表面积: ${"%.1f".format(room.totalSurfaceAreaM2)} m²",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()

            // --- Formula Selector ---
            Text("计算公式", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReverberationFormula.entries.forEach { formula ->
                    FilterChip(
                        selected = uiState.selectedFormula == formula,
                        onClick = { viewModel.selectFormula(formula) },
                        label = { Text(formula.label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
            Text(
                uiState.selectedFormula.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Air absorption toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = uiState.useAirAbsorption, onCheckedChange = { viewModel.toggleAirAbsorption() })
                Spacer(modifier = Modifier.width(8.dp))
                Text("考虑空气吸声 (大空间)", style = MaterialTheme.typography.bodyMedium)
            }

            HorizontalDivider()

            // --- Surface Material Assignment ---
            Text("表面材料分配", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            SurfaceType.entries.filter { it != SurfaceType.ALL_WALLS }.forEach { surfaceType ->
                SurfaceAssignmentRow(
                    surfaceType = surfaceType,
                    assignments = uiState.surfaceAssignments[surfaceType] ?: emptyList(),
                    onAdd = { viewModel.openMaterialPicker(surfaceType) },
                    onRemove = { index -> viewModel.removeMaterialFromSurface(surfaceType, index) }
                )
            }

            HorizontalDivider()

            // --- Calculate Button ---
            Button(
                onClick = { viewModel.calculate() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = !uiState.isCalculating && viewModel.getRoomDimensions() != null
            ) {
                if (uiState.isCalculating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Calculate, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("计算混响时间")
                }
            }

            // Error display
            uiState.error?.let {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(it, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }

            // --- Results ---
            uiState.reverberationResult?.let { result ->
                HorizontalDivider()
                Text("计算结果", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                // RT60 Chart
                FrequencyChart(
                    data = result.rt60ByBand,
                    yAxisLabel = "RT60 (s)",
                    title = "混响时间曲线 - ${result.formula.label}"
                )

                // RT60 Table
                OctaveBandTable(
                    values = result.rt60ByBand,
                    label = "混响时间 RT60",
                    unit = "s",
                    decimals = 2
                )

                // Key metrics
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("关键指标", fontWeight = FontWeight.Bold)
                        Text("中频混响时间 RT60(mid): ${"%.2f".format(result.rt60Mid)} s")
                        Text("语言频率混响时间 RT60(speech): ${"%.2f".format(result.rt60Speech)} s")
                        Text("平均吸声系数 ᾱ(500Hz): ${"%.3f".format(result.meanAbsorptionByBand[com.acoustics.calculator.core.constants.FrequencyBand.BAND_500] ?: 0.0)}")

                        uiState.clarityResult?.let { clarity ->
                            Text("C50(平均): ${"%.1f".format(clarity.c50Avg)} dB — ${clarity.c50Quality}")
                            Text("C80(平均): ${"%.1f".format(clarity.c80Avg)} dB — ${clarity.c80Quality}")
                        }
                        uiState.stipaResult?.let { stipa ->
                            Text("STIPA: ${"%.2f".format(stipa.stipa)} — ${stipa.grade}")
                        }
                        uiState.bassRatioResult?.let { bass ->
                            Text("低音比 LF: ${"%.2f".format(bass.bassRatio)} — ${bass.interpretation}")
                        }
                    }
                }
            }
        }
    }

    // --- Material Picker Dialog ---
    if (uiState.showMaterialPicker) {
        MaterialPickerDialog(
            materials = uiState.availableMaterials,
            searchQuery = uiState.materialSearchQuery,
            onSearchChange = viewModel::updateMaterialSearch,
            onSelect = viewModel::assignMaterial,
            onDismiss = viewModel::closeMaterialPicker
        )
    }
}

@Composable
fun SurfaceAssignmentRow(
    surfaceType: SurfaceType,
    assignments: List<com.acoustics.calculator.domain.model.SurfaceAssignment>,
    onAdd: () -> Unit,
    onRemove: (Int) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(surfaceType.label, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                IconButton(onClick = onAdd, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Add, "添加材料", tint = MaterialTheme.colorScheme.primary)
                }
            }
            assignments.forEachIndexed { index, assignment ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${assignment.material.nameZh} (${"%.1f".format(assignment.areaM2)} m²)",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "NRC: ${"%.2f".format(assignment.material.nrc)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(onClick = { onRemove(index) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, "移除", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialPickerDialog(
    materials: List<com.acoustics.calculator.domain.model.Material>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSelect: (com.acoustics.calculator.domain.model.Material) -> Unit,
    onDismiss: () -> Unit
) {
    val filtered = remember(materials, searchQuery) {
        if (searchQuery.isBlank()) materials
        else materials.filter {
            it.nameZh.contains(searchQuery, ignoreCase = true) ||
            it.nameEn.contains(searchQuery, ignoreCase = true)
        }.take(50)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择材料") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("搜索材料...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, null) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(filtered) { material ->
                        ListItem(
                            headlineContent = { Text(material.nameZh) },
                            supportingContent = { Text("NRC: ${"%.2f".format(material.nrc)} | ${material.nameEn}") },
                            modifier = Modifier.clickable { onSelect(material) }
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
