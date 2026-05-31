package com.acoustics.calculator.ui.screen.silencer

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.acoustics.calculator.domain.engine.PdfReportEngine
import com.acoustics.calculator.domain.model.*
import com.acoustics.calculator.ui.theme.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SilencerScreen(
    navController: NavController,
    viewModel: SilencerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("消声器设计计算", fontWeight = FontWeight.Bold) },
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
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ======== SECTION 1: TYPE SELECTOR ========
            item {
                SilencerTypeSelector(
                    selectedType = uiState.selectedType,
                    showInfo = uiState.showTypeInfo,
                    onSelect = viewModel::selectType,
                    onToggleInfo = viewModel::toggleTypeInfo
                )
            }

            // ======== SECTION 2: GENERAL PARAMETERS ========
            item {
                GeneralParamsCard(
                    lengthM = uiState.lengthM,
                    areaM2 = uiState.crossSectionAreaM2,
                    diameterM = uiState.ductDiameterM,
                    velocityMs = uiState.flowVelocityMs,
                    temperatureC = uiState.temperatureC,
                    pressureKpa = uiState.pressureKpa,
                    onLengthChange = viewModel::updateLength,
                    onAreaChange = viewModel::updateArea,
                    onDiameterChange = viewModel::updateDiameter,
                    onVelocityChange = viewModel::updateVelocity,
                    onTemperatureChange = viewModel::updateTemperature,
                    onPressureChange = viewModel::updatePressure
                )
            }

            // ======== SECTION 3: RESISTIVE / COMPOSITE PARAMS ========
            if (uiState.selectedType != SilencerType.REACTIVE) {
                item {
                    ResistiveParamsCard(
                        selectedMaterial = uiState.selectedMaterial,
                        thicknessMm = uiState.materialThicknessMm,
                        onShowPicker = viewModel::showMaterialPicker,
                        onClear = viewModel::clearMaterial
                    )
                }
            }

            // ======== SECTION 4: REACTIVE PARAMS ========
            if (uiState.selectedType != SilencerType.RESISTIVE) {
                item {
                    ReactiveParamsCard(
                        chamberCount = uiState.chamberCount,
                        chamberVolume = uiState.chamberVolumeM3,
                        perforationRate = uiState.perforationRate,
                        neckLengthMm = uiState.neckLengthMm,
                        chamberLengthM = uiState.chamberLengthM,
                        onChamberCountChange = viewModel::updateChamberCount,
                        onVolumeChange = viewModel::updateChamberVolume,
                        onPerforationChange = viewModel::updatePerforationRate,
                        onNeckLengthChange = viewModel::updateNeckLength,
                        onChamberLengthChange = viewModel::updateChamberLength
                    )
                }
            }

            // ======== SECTION 5: FAN SELECTOR ========
            item {
                FanSelectorCard(
                    selectedFan = uiState.selectedFan,
                    onShowPicker = viewModel::showFanPicker,
                    onClear = { viewModel.selectFan(null) }
                )
            }

            // ======== SECTION 6: CALCULATE BUTTON ========
            item {
                Button(
                    onClick = { viewModel.calculate() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = !uiState.isCalculating,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isCalculating) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(Icons.Default.Calculate, null)
                        Spacer(Modifier.width(8.dp))
                        Text("计算消声器降噪量", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            // Error display
            uiState.error?.let { error ->
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(8.dp))
                            Text(error, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
                            TextButton(onClick = viewModel::clearError) { Text("关闭") }
                        }
                    }
                }
            }

            // ======== SECTION 7: RESULTS ========
            uiState.result?.let { result ->
                item { ResultDisplayCard(result) }

                // ======== SECTION 8: COMPLIANCE ========
                item { ComplianceVerificationCard(
                    targetTotalIL = uiState.targetTotalILDbA,
                    result = result,
                    complianceResult = uiState.complianceResult,
                    onTargetTotalChange = viewModel::updateTargetTotal,
                    onTargetBandChange = viewModel::updateTargetBand,
                    onVerify = viewModel::verifyCompliance,
                    targetIL63 = uiState.targetIL63, targetIL125 = uiState.targetIL125,
                    targetIL250 = uiState.targetIL250, targetIL500 = uiState.targetIL500,
                    targetIL1000 = uiState.targetIL1000, targetIL2000 = uiState.targetIL2000,
                    targetIL4000 = uiState.targetIL4000, targetIL8000 = uiState.targetIL8000
                ) }

                // ======== SECTION 9: SMART RECOMMENDATION ========
                item { SmartRecommendationCard(
                    targetIL = uiState.smartTargetIL,
                    maxPressure = uiState.smartMaxPressure,
                    preferredType = uiState.smartPreferredType,
                    preferredMaterial = uiState.smartPreferredMaterial,
                    maxLength = uiState.smartMaxLength,
                    isSearching = uiState.isSearching,
                    recommendations = uiState.recommendations,
                    onTargetChange = viewModel::updateSmartTarget,
                    onPressureChange = viewModel::updateSmartPressure,
                    onTypeChange = viewModel::updateSmartType,
                    onMaterialChange = viewModel::updateSmartMaterial,
                    onMaxLengthChange = viewModel::updateSmartMaxLength,
                    onSearch = viewModel::runSmartSelection,
                    onApply = viewModel::applyRecommendation
                ) }
            }
        }
    }

    // Material picker dialog
    if (uiState.showMaterialPicker) {
        MaterialPickerSheet(
            materials = uiState.availableMaterials,
            onSelect = { viewModel.selectMaterial(it); viewModel.hideMaterialPicker() },
            onDismiss = viewModel::hideMaterialPicker,
            // Custom material
            customName = uiState.customMaterialName,
            onCustomNameChange = viewModel::updateCustomName,
            customAlpha63 = uiState.customAlpha63, customAlpha125 = uiState.customAlpha125,
            customAlpha250 = uiState.customAlpha250, customAlpha500 = uiState.customAlpha500,
            customAlpha1000 = uiState.customAlpha1000, customAlpha2000 = uiState.customAlpha2000,
            customAlpha4000 = uiState.customAlpha4000, customAlpha8000 = uiState.customAlpha8000,
            onCustomAlphaChange = viewModel::updateCustomAlpha,
            onAddCustom = viewModel::addCustomMaterial
        )
    }

    // Fan picker dialog
    if (uiState.showFanPicker) {
        FanPickerDialog(
            fans = uiState.availableFans,
            searchQuery = uiState.fanSearchQuery,
            onSearchChange = viewModel::updateFanSearch,
            onSelect = viewModel::selectFan,
            onDismiss = viewModel::hideFanPicker
        )
    }
}

// ==================== COMPONENT: TYPE SELECTOR ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SilencerTypeSelector(
    selectedType: SilencerType, showInfo: Boolean,
    onSelect: (SilencerType) -> Unit, onToggleInfo: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("消声器类型", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = onToggleInfo, modifier = Modifier.size(28.dp)) {
                    Icon(if (showInfo) Icons.Default.Close else Icons.Default.Info,
                        "类型说明", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.height(8.dp))

            // Dropdown-style selector (simulated with chips)
            val expanded = remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded.value, onExpandedChange = { expanded.value = it }) {
                OutlinedTextField(
                    value = selectedType.label,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    singleLine = true
                )
                ExposedDropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
                    SilencerType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(type.label, fontWeight = FontWeight.SemiBold)
                                    Text(type.description, style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            },
                            onClick = { onSelect(type); expanded.value = false }
                        )
                    }
                }
            }

            // Info tooltip
            if (showInfo) {
                Spacer(Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        selectedType.tip,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

// ==================== COMPONENT: GENERAL PARAMS ====================
@Composable
fun GeneralParamsCard(
    lengthM: String, areaM2: String, diameterM: String,
    velocityMs: String, temperatureC: String, pressureKpa: String,
    onLengthChange: (String) -> Unit, onAreaChange: (String) -> Unit,
    onDiameterChange: (String) -> Unit, onVelocityChange: (String) -> Unit,
    onTemperatureChange: (String) -> Unit, onPressureChange: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("通用参数", fontWeight = FontWeight.Bold)

            // Length with common values
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = lengthM, onValueChange = onLengthChange,
                    label = { Text("长度 (m)") }, modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                Text("常用:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                listOf("0.5", "1.0", "1.5", "2.0").forEach { v ->
                    TextButton(onClick = { onLengthChange(v) }, modifier = Modifier.size(width = 36.dp, height = 24.dp)) {
                        Text(v, fontSize = 11.sp)
                    }
                }
            }

            // Area + Diameter (auto conversion)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = areaM2, onValueChange = onAreaChange,
                    label = { Text("截面积 (m²)") }, modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                OutlinedTextField(value = diameterM, onValueChange = onDiameterChange,
                    label = { Text("管径 (m)") }, modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
            }

            // Velocity
            OutlinedTextField(value = velocityMs, onValueChange = onVelocityChange,
                label = { Text("气流速度 (m/s)") }, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)

            // Temperature + Pressure
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = temperatureC, onValueChange = onTemperatureChange,
                    label = { Text("温度 (°C)") }, modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                OutlinedTextField(value = pressureKpa, onValueChange = onPressureChange,
                    label = { Text("气压 (kPa)") }, modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
            }
        }
    }
}

// ==================== COMPONENT: RESISTIVE PARAMS ====================
@Composable
fun ResistiveParamsCard(
    selectedMaterial: SilencerMaterial?, thicknessMm: Double,
    onShowPicker: () -> Unit, onClear: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(if (selectedMaterial != null) "吸声材料 (已选)" else "阻性参数 · 吸声材料",
                fontWeight = FontWeight.Bold)

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = if (selectedMaterial != null) "${selectedMaterial.name} (${selectedMaterial.nrc.roundToInt()*5}% NRC)" else "未选择材料",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("吸声材料") },
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    singleLine = true,
                    trailingIcon = { if (selectedMaterial != null) IconButton(onClick = onClear) { Icon(Icons.Default.Close, "清除") } }
                )
                Button(onClick = onShowPicker) { Text("选择") }
            }

            if (selectedMaterial != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(selected = thicknessMm == 25.0, onClick = {}, label = { Text("25mm") }, modifier = Modifier.height(28.dp))
                    FilterChip(selected = thicknessMm == 50.0, onClick = {}, label = { Text("50mm") }, modifier = Modifier.height(28.dp))
                    FilterChip(selected = thicknessMm == 100.0, onClick = {}, label = { Text("100mm") }, modifier = Modifier.height(28.dp))
                    FilterChip(selected = thicknessMm == 150.0, onClick = {}, label = { Text("150mm") }, modifier = Modifier.height(28.dp))
                }
                Text("NRC: ${"%.2f".format(selectedMaterial.nrc)}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// ==================== COMPONENT: REACTIVE PARAMS ====================
@Composable
fun ReactiveParamsCard(
    chamberCount: String, chamberVolume: String, perforationRate: String,
    neckLengthMm: String, chamberLengthM: String,
    onChamberCountChange: (String) -> Unit, onVolumeChange: (String) -> Unit,
    onPerforationChange: (String) -> Unit, onNeckLengthChange: (String) -> Unit,
    onChamberLengthChange: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("抗性参数 · 扩张室", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = chamberCount, onValueChange = onChamberCountChange,
                    label = { Text("腔室数量") }, modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(value = chamberLengthM, onValueChange = onChamberLengthChange,
                    label = { Text("腔室长度 (m)") }, modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = chamberVolume, onValueChange = onVolumeChange,
                    label = { Text("腔室容积 (m³)") }, modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                OutlinedTextField(value = perforationRate, onValueChange = onPerforationChange,
                    label = { Text("穿孔率") }, modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
            }
            OutlinedTextField(value = neckLengthMm, onValueChange = onNeckLengthChange,
                label = { Text("颈长 (mm)") }, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)

            // Reactive presets info
            Text("经验参考: 单腔扩张室最佳消声频率 f = c/2L", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ==================== COMPONENT: FAN SELECTOR ====================
@Composable
fun FanSelectorCard(selectedFan: FanNoiseData?, onShowPicker: () -> Unit, onClear: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Engineering, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("风机噪声源（选填）", fontWeight = FontWeight.SemiBold)
                Text(selectedFan?.modelName ?: "未选择", style = MaterialTheme.typography.bodySmall,
                    color = if (selectedFan != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (selectedFan != null) {
                IconButton(onClick = onClear) { Icon(Icons.Default.Close, "清除", modifier = Modifier.size(20.dp)) }
            }
            TextButton(onClick = onShowPicker) { Text(if (selectedFan == null) "选择" else "更换") }
        }
    }
}

// ==================== COMPONENT: RESULT DISPLAY ====================
@Composable
fun ResultDisplayCard(result: InsertionLossResult) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("计算结果", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

            // Key metrics
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricBox("A计权总降噪量", "${"%.1f".format(result.totalADbInsertionLoss)} dB(A)", CompliantGreen, Modifier.weight(1f))
                MetricBox("压力损失", "${"%.0f".format(result.pressureDropPa)} Pa", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
            }

            // Insertion loss table
            Text("各倍频程插入损失 (dB)", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
            ILTable(
                label = "插入损失",
                values = result.insertionLossByBand,
                corrected = result.correctedILByBand,
                aWeighted = result.aWeightedILByBand
            )

            // Flow noise display
            if (result.flowNoiseByBand.values.any { it > 0 }) {
                Text("气流再生噪声 (dB)", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                OctaveBandRow(result.flowNoiseByBand, "dB")
            }

            // After-noise display
            result.afterNoiseByBand?.let { after ->
                Text("降噪后噪声 (dB)", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                OctaveBandRow(after, "dB")
                result.afterTotalADb?.let { t ->
                    Text("降噪后总A声级: ${"%.1f".format(t)} dB(A)", fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                }
            }

            // PDF Export button
            Spacer(Modifier.height(4.dp))
            val context = LocalContext.current
            var pdfPath by remember { mutableStateOf<String?>(null) }
            var showPdfSuccess by remember { mutableStateOf(false) }

            OutlinedButton(
                onClick = {
                    val engine = PdfReportEngine()
                    val path = engine.generateSilencerReport(context, result)
                    pdfPath = path
                    showPdfSuccess = path != null
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("导出PDF报告")
            }

            if (showPdfSuccess) {
                Card(colors = CardDefaults.cardColors(containerColor = CompliantGreenBg)) {
                    Text(
                        "✅ PDF报告已保存: ${pdfPath ?: ""}",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = CompliantGreen
                    )
                }
            }
        }
    }
}

@Composable
fun MetricBox(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        modifier = modifier
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = color)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ILTable(label: String, values: Map<SilencerBand, Double>, corrected: Map<SilencerBand, Double>? = null, aWeighted: Map<SilencerBand, Double>? = null) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(8.dp)) {
            // Header
            Row {
                Text("频带", modifier = Modifier.width(50.dp), fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                SilencerBand.ALL_BANDS.forEach { b ->
                    Text(b.label, modifier = Modifier.weight(1f), fontSize = 10.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
            Spacer(Modifier.height(2.dp))
            HorizontalDivider()
            // IL values
            RowVal("IL", values)
            corrected?.let { RowVal("修正", it, MaterialTheme.colorScheme.primary) }
            aWeighted?.let { RowVal("A计权", it, CompliantGreen) }
        }
    }
}

@Composable
private fun RowVal(label: String, data: Map<SilencerBand, Double>, color: Color = MaterialTheme.colorScheme.onSurface) {
    Row {
        Text(label, modifier = Modifier.width(50.dp), fontSize = 10.sp, fontWeight = FontWeight.Medium, color = color)
        SilencerBand.ALL_BANDS.forEach { b ->
            Text("${"%.1f".format(data[b] ?: 0.0)}", modifier = Modifier.weight(1f), fontSize = 10.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = color)
        }
    }
}

@Composable
fun OctaveBandRow(data: Map<SilencerBand, Double>, unit: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(Modifier.padding(8.dp)) {
            SilencerBand.ALL_BANDS.forEach { b ->
                Text("${"%.0f".format(data[b] ?: 0.0)}", modifier = Modifier.weight(1f),
                    fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}

// ==================== COMPONENT: COMPLIANCE VERIFICATION ====================
@Composable
fun ComplianceVerificationCard(
    targetTotalIL: String, result: InsertionLossResult,
    complianceResult: SilencerComplianceResult?,
    onTargetTotalChange: (String) -> Unit,
    onTargetBandChange: (SilencerBand, String) -> Unit,
    onVerify: () -> Unit,
    targetIL63: String, targetIL125: String, targetIL250: String, targetIL500: String,
    targetIL1000: String, targetIL2000: String, targetIL4000: String, targetIL8000: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("达标验证", fontWeight = FontWeight.Bold)
            OutlinedTextField(value = targetTotalIL, onValueChange = onTargetTotalChange,
                label = { Text("目标总降噪量 dB(A)") }, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)

            // Per-band targets (expandable)
            var expanded by remember { mutableStateOf(false) }
            TextButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "收起分频带目标" else "设置分频带目标（可选）")
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, modifier = Modifier.size(16.dp))
            }
            if (expanded) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val bands = SilencerBand.ALL_BANDS
                    val targets = listOf(targetIL63, targetIL125, targetIL250, targetIL500, targetIL1000, targetIL2000, targetIL4000, targetIL8000)
                    bands.forEachIndexed { i, band ->
                        OutlinedTextField(value = targets[i], onValueChange = { onTargetBandChange(band, it) },
                            label = { Text(band.label, fontSize = 9.sp) }, modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Button(onClick = onVerify, modifier = Modifier.fillMaxWidth()) { Text("校验达标情况") }

            complianceResult?.let { cr ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (cr.isFullyCompliant) CompliantGreenBg else NonCompliantRedBg
                    )
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            if (cr.isFullyCompliant) "✅ 当前选型满足降噪要求"
                            else "❌ 未完全达标",
                            fontWeight = FontWeight.Bold,
                            color = if (cr.isFullyCompliant) CompliantGreen else NonCompliantRed
                        )
                        HorizontalDivider()
                        cr.suggestions.forEach { suggestion ->
                            Text("• $suggestion", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

// ==================== COMPONENT: SMART RECOMMENDATION ====================
@Composable
fun SmartRecommendationCard(
    targetIL: String, maxPressure: String, preferredType: String,
    preferredMaterial: String, maxLength: String, isSearching: Boolean,
    recommendations: List<SilencerRecommendation>?,
    onTargetChange: (String) -> Unit, onPressureChange: (String) -> Unit,
    onTypeChange: (String) -> Unit, onMaterialChange: (String) -> Unit,
    onMaxLengthChange: (String) -> Unit, onSearch: () -> Unit,
    onApply: (SilencerRecommendation) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("智能选型推荐", fontWeight = FontWeight.Bold)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = targetIL, onValueChange = onTargetChange,
                    label = { Text("目标 dB(A)") }, modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                OutlinedTextField(value = maxPressure, onValueChange = onPressureChange,
                    label = { Text("最大阻力 Pa") }, modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = preferredType, onValueChange = onTypeChange,
                    label = { Text("偏好的类型") }, modifier = Modifier.weight(1f),
                    placeholder = { Text("留空=全部") }, singleLine = true)
                OutlinedTextField(value = maxLength, onValueChange = onMaxLengthChange,
                    label = { Text("最大长度 m") }, modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
            }
            OutlinedTextField(value = preferredMaterial, onValueChange = onMaterialChange,
                label = { Text("偏好材料") }, modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("留空=全部") }, singleLine = true)

            Button(
                onClick = onSearch,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSearching
            ) {
                if (isSearching) CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                else Text("搜索最优方案")
            }

            recommendations?.let { recs ->
                if (recs.isEmpty()) {
                    Text("没有找到满足条件的方案", color = MaterialTheme.colorScheme.error)
                } else {
                    Text("Top ${recs.size} 推荐方案", fontWeight = FontWeight.SemiBold)
                    recs.forEach { rec ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth().clickable { onApply(rec) },
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = if (rec.isFullyCompliant) CompliantGreenBg else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("第${rec.rank}名", fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(40.dp), fontSize = 13.sp)
                                Column(Modifier.weight(1f)) {
                                    Text("${rec.silencerType.label} | ${"%.1f".format(rec.lengthM)}m",
                                        fontWeight = FontWeight.SemiBold)
                                    Text("降噪: ${"%.1f".format(rec.actualILDbA)} dB(A) | 阻力: ${"%.0f".format(rec.actualPressureDropPa)} Pa | ${rec.costEstimate}",
                                        style = MaterialTheme.typography.bodySmall)
                                }
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, "应用",
                                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                    Text("点击方案可直接加载参数", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// ==================== MATERIAL PICKER SHEET ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialPickerSheet(
    materials: List<SilencerMaterial>,
    onSelect: (SilencerMaterial) -> Unit, onDismiss: () -> Unit,
    customName: String, onCustomNameChange: (String) -> Unit,
    customAlpha63: String, customAlpha125: String, customAlpha250: String,
    customAlpha500: String, customAlpha1000: String, customAlpha2000: String,
    customAlpha4000: String, customAlpha8000: String,
    onCustomAlphaChange: (SilencerBand, String) -> Unit,
    onAddCustom: () -> Unit
) {
    var tab by remember { mutableStateOf(0) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("吸声材料库", fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.heightIn(max = 500.dp)) {
                TabRow(selectedTabIndex = tab) {
                    Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("内置材料") })
                    Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("自定义") })
                }
                Spacer(Modifier.height(8.dp))

                when (tab) {
                    0 -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(materials) { material ->
                                ListItem(
                                    headlineContent = { Text("${material.name} · ${"%.0f".format(material.thicknessMm)}mm") },
                                    supportingContent = { Text("NRC: ${"%.2f".format(material.nrc)} | ${material.isCustom.str()}") },
                                    trailingContent = { Text("选择", color = MaterialTheme.colorScheme.primary) },
                                    modifier = Modifier.clickable { onSelect(material) }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                    1 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedTextField(value = customName, onValueChange = onCustomNameChange,
                                label = { Text("材料名称") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            Text("各频带吸声系数 (0~1):", style = MaterialTheme.typography.bodySmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                val bands = SilencerBand.ALL_BANDS
                                val values = listOf(customAlpha63, customAlpha125, customAlpha250, customAlpha500,
                                    customAlpha1000, customAlpha2000, customAlpha4000, customAlpha8000)
                                bands.forEachIndexed { i, band ->
                                    OutlinedTextField(value = values[i], onValueChange = { onCustomAlphaChange(band, it) },
                                        label = { Text(band.label, fontSize = 8.sp) }, modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true, textStyle = MaterialTheme.typography.bodySmall)
                                }
                            }
                            Button(onClick = onAddCustom, modifier = Modifier.fillMaxWidth()) { Text("添加自定义材料") }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

private fun Boolean.str() = if (this) "自定义" else "内置"

// ==================== FAN PICKER DIALOG ====================
@Composable
fun FanPickerDialog(
    fans: List<FanNoiseData>, searchQuery: String,
    onSearchChange: (String) -> Unit, onSelect: (FanNoiseData) -> Unit, onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择风机型号") },
        text = {
            Column(Modifier.heightIn(max = 450.dp)) {
                OutlinedTextField(value = searchQuery, onValueChange = onSearchChange,
                    placeholder = { Text("搜索型号...") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, null) })
                Spacer(Modifier.height(8.dp))
                FanType.entries.forEach { type ->
                    Text(type.label, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
                    fans.filter { it.fanType == type }.forEach { fan ->
                        ListItem(
                            headlineContent = { Text(fan.modelName) },
                            supportingContent = { Text("${fan.powerKw}kW | ${fan.airflowM3h}m³/h | LW=${"%.0f".format(fan.totalLwADb)}dB(A)") },
                            modifier = Modifier.clickable { onSelect(fan) }
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
