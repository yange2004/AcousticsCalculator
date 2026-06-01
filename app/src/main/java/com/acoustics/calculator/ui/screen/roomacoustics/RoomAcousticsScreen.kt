package com.acoustics.calculator.ui.screen.roomacoustics

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.acoustics.calculator.core.utils.ExportUtils
import com.acoustics.calculator.domain.model.ReverberationFormula
import com.acoustics.calculator.domain.model.SurfaceType
import com.acoustics.calculator.ui.components.FrequencyChart
import com.acoustics.calculator.ui.components.OctaveBandTable
import com.acoustics.calculator.ui.components.ParticleBackground
import com.acoustics.calculator.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomAcousticsScreen(
    navController: NavController,
    projectId: Long,
    viewModel: RoomAcousticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var exportResult by remember { mutableStateOf<String?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "roomAcoustics")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "roomGlow"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("室内声学计算",
                    fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = Color.White)
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

            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // --- Room Dimensions ---
                NeonSection("房间尺寸") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = uiState.widthM,
                            onValueChange = viewModel::updateWidth,
                            label = { Text("宽度 (m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = neonTextFieldColors()
                        )
                        OutlinedTextField(
                            value = uiState.lengthM,
                            onValueChange = viewModel::updateLength,
                            label = { Text("长度 (m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = neonTextFieldColors()
                        )
                        OutlinedTextField(
                            value = uiState.heightM,
                            onValueChange = viewModel::updateHeight,
                            label = { Text("高度 (m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = neonTextFieldColors()
                        )
                    }

                    // Derived values
                    viewModel.getRoomDimensions()?.let { room ->
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "体积: ${"%.1f".format(room.volumeM3)} m³ | 表面积: ${"%.1f".format(room.totalSurfaceAreaM2)} m²",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                // --- Formula Selector ---
                NeonSection("计算公式") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReverberationFormula.entries.forEach { formula ->
                            FilterChip(
                                selected = uiState.selectedFormula == formula,
                                onClick = { viewModel.selectFormula(formula) },
                                label = { Text(formula.label, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                                    selectedLabelColor = NeonCyan,
                                    containerColor = GlassWhite,
                                    labelColor = Color.White.copy(alpha = 0.7f)
                                )
                            )
                        }
                    }
                    Text(
                        uiState.selectedFormula.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )

                    // Air absorption toggle
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = uiState.useAirAbsorption,
                            onCheckedChange = { viewModel.toggleAirAbsorption() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonCyan,
                                checkedTrackColor = NeonCyan.copy(alpha = 0.3f)
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("考虑空气吸声 (大空间)",
                            color = Color.White.copy(alpha = 0.7f))
                    }
                }

                // --- Surface Material Assignment ---
                NeonSection("表面材料分配") {
                    SurfaceType.entries.filter { it != SurfaceType.ALL_WALLS }.forEach { surfaceType ->
                        SurfaceTypeRow(
                            surfaceType = surfaceType,
                            assignments = uiState.surfaceAssignments[surfaceType] ?: emptyList(),
                            onAdd = { viewModel.openMaterialPicker(surfaceType) },
                            onRemove = { index -> viewModel.removeMaterialFromSurface(surfaceType, index) }
                        )
                    }
                }

                // --- Calculate Button ---
                Button(
                    onClick = { viewModel.calculate() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = !uiState.isCalculating && viewModel.getRoomDimensions() != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        disabledContainerColor = NeonCyan.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (uiState.isCalculating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Calculate, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("计算混响时间", fontWeight = FontWeight.Bold)
                    }
                }

                // Error display
                uiState.error?.let {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = NeonRed.copy(alpha = 0.1f)
                    ) {
                        Text(it, modifier = Modifier.fillMaxWidth().padding(14.dp),
                            color = NeonRed, style = MaterialTheme.typography.bodySmall)
                    }
                }

                // --- Results ---
                uiState.reverberationResult?.let { result ->
                    NeonSection("计算结果") {
                        // RT60 Chart
                        FrequencyChart(
                            data = result.rt60ByBand,
                            yAxisLabel = "RT60 (s)",
                            title = "混响时间曲线 - ${result.formula.label}"
                        )

                        Spacer(Modifier.height(12.dp))

                        // RT60 Table
                        OctaveBandTable(
                            values = result.rt60ByBand,
                            label = "混响时间 RT60",
                            unit = "s",
                            decimals = 2
                        )

                        // Key metrics
                        Spacer(Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceMedium),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("关键指标", fontWeight = FontWeight.Bold, color = NeonCyan, fontSize = 14.sp)
                                MetricRow("中频混响时间 RT60(mid)", "${"%.2f".format(result.rt60Mid)} s")
                                MetricRow("语言频率混响时间 RT60(speech)", "${"%.2f".format(result.rt60Speech)} s")
                                MetricRow("平均吸声系数 ᾱ(500Hz)", "${"%.3f".format(result.meanAbsorptionByBand[com.acoustics.calculator.core.constants.FrequencyBand.BAND_500] ?: 0.0)}")

                                uiState.clarityResult?.let { clarity ->
                                    MetricRow("C50(平均)", "${"%.1f".format(clarity.c50Avg)} dB — ${clarity.c50Quality}")
                                    MetricRow("C80(平均)", "${"%.1f".format(clarity.c80Avg)} dB — ${clarity.c80Quality}")
                                }
                                uiState.stipaResult?.let { stipa ->
                                    MetricRow("STIPA", "${"%.2f".format(stipa.stipa)} — ${stipa.grade}")
                                }
                                uiState.bassRatioResult?.let { bass ->
                                    MetricRow("低音比 LF", "${"%.2f".format(bass.bassRatio)} — ${bass.interpretation}")
                                }
                            }
                        }
                    }

                    // Export results
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                val headers = listOf("指标", "125Hz", "250Hz", "500Hz", "1000Hz", "2000Hz", "4000Hz", "8000Hz")
                                val rows = result.rt60ByBand.entries
                                    .sortedBy { it.key.hz }
                                    .map { (band, value) ->
                                        listOf("RT60") + result.rt60ByBand.entries
                                            .sortedBy { it.key.hz }
                                            .map { "${"%.2f".format(it.value)}" }
                                    }
                                val path = ExportUtils.exportResultToCsv(context, "室内声学_${result.formula.label}", headers, rows)
                                exportResult = if (path != null) "✅ CSV已导出到手机「下载」文件夹" else "❌ 导出失败"
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGreen),
                        border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Download, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("导出结果为 CSV")
                    }
                }

                // Export result message
                exportResult?.let {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (it.contains("失败")) NeonRed.copy(alpha = 0.1f) else NeonGreen.copy(alpha = 0.1f)
                    ) {
                        Text(it,
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            color = if (it.contains("失败")) NeonRed else NeonGreen,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }

    // --- Material Picker Dialog ---
    if (uiState.showMaterialPicker) {
        AlertDialog(
            onDismissRequest = viewModel::closeMaterialPicker,
            shape = RoundedCornerShape(20.dp),
            containerColor = SurfaceDark,
            title = { Text("选择材料", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                val filtered = remember(uiState.availableMaterials, uiState.materialSearchQuery) {
                    val q = uiState.materialSearchQuery
                    if (q.isBlank()) uiState.availableMaterials
                    else uiState.availableMaterials.filter {
                        it.nameZh.contains(q, ignoreCase = true) ||
                        it.nameEn.contains(q, ignoreCase = true)
                    }.take(50)
                }

                Column {
                    OutlinedTextField(
                        value = uiState.materialSearchQuery,
                        onValueChange = viewModel::updateMaterialSearch,
                        placeholder = { Text("搜索材料...", color = Color.White.copy(alpha = 0.4f)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = neonTextFieldColors(),
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.5f)) }
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                        items(filtered) { material ->
                            ListItem(
                                headlineContent = { Text(material.nameZh, color = Color.White) },
                                supportingContent = { Text("NRC: ${"%.2f".format(material.nrc)} | ${material.nameEn}", color = Color.White.copy(alpha = 0.5f)) },
                                modifier = Modifier.clickable { viewModel.assignMaterial(material) }
                            )
                            HorizontalDivider(color = GlassWhite)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = viewModel::closeMaterialPicker) {
                    Text("取消", color = NeonCyan)
                }
            }
        )
    }
}

@Composable
fun NeonSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White)
        Spacer(Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), content = content)
        }
    }
}

@Composable
fun SurfaceTypeRow(
    surfaceType: SurfaceType,
    assignments: List<com.acoustics.calculator.domain.model.SurfaceAssignment>,
    onAdd: () -> Unit,
    onRemove: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(surfaceType.label,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.weight(1f))
            IconButton(onClick = onAdd, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Add, "添加材料", tint = NeonCyan, modifier = Modifier.size(20.dp))
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
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "NRC: ${"%.2f".format(assignment.material.nrc)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f)
                )
                IconButton(onClick = { onRemove(index) }, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.Close, "移除", modifier = Modifier.size(14.dp), tint = NeonRed.copy(alpha = 0.7f))
                }
            }
        }
        if (assignments.isEmpty()) {
            Text("暂无材料，点击 + 添加",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.3f))
        }
    }
}

@Composable
fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
        Text(value, fontWeight = FontWeight.Medium, color = Color.White, fontSize = 13.sp)
    }
}

@Composable
fun neonTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = NeonCyan,
    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
    cursorColor = NeonCyan,
    focusedLabelColor = NeonCyan,
    unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
)
