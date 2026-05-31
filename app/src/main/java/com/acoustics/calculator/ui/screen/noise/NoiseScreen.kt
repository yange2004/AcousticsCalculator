package com.acoustics.calculator.ui.screen.noise

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoiseScreen(
    navController: NavController,
    projectId: Long,
    viewModel: NoiseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("噪声预测") },
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
            Text("声源参数", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = uiState.sourceLevelDb,
                onValueChange = viewModel::updateSourceLevel,
                label = { Text("声源声功率级 Lw (dB)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )

            Text("声源位置", fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                com.acoustics.calculator.domain.engine.IndoorNoiseEngine.SourcePosition.entries.forEach { pos ->
                    FilterChip(
                        selected = uiState.sourcePosition == pos,
                        onClick = { viewModel.selectSourcePosition(pos) },
                        label = { Text(pos.label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            OutlinedTextField(
                value = uiState.distanceM,
                onValueChange = viewModel::updateDistance,
                label = { Text("声源到接收点距离 (m)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )

            HorizontalDivider()

            Text("房间参数", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = uiState.totalAbsorptionM2,
                onValueChange = viewModel::updateAbsorption,
                label = { Text("总吸声量 A (m² sabins)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )

            HorizontalDivider()

            // Barrier parameters
            Text("声屏障参数 (可选)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.barrierHeightM,
                    onValueChange = viewModel::updateBarrierHeight,
                    label = { Text("屏障高度 (m)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f), singleLine = true
                )
                OutlinedTextField(
                    value = uiState.barrierDistM,
                    onValueChange = viewModel::updateBarrierDist,
                    label = { Text("屏障距离 (m)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f), singleLine = true
                )
            }

            Button(
                onClick = { viewModel.calculate() },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.VolumeUp, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("计算噪声级")
            }

            // Results
            uiState.noiseResult?.let { result ->
                HorizontalDivider()
                Text("计算结果", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("室内声压级: ${"%.1f".format(result.indoorLevelDb)} dB", fontWeight = FontWeight.Bold)
                        Text("直达声场: ${"%.1f".format(result.directFieldDb)} dB")
                        Text("混响声场: ${"%.1f".format(result.reverberantFieldDb)} dB")
                        Text("临界距离: ${"%.1f".format(
                            com.acoustics.calculator.domain.engine.IndoorNoiseEngine(/*mock*/).criticalDistance(
                                result.totalAbsorptionM2, result.directivityFactorQ
                            )
                        )} m")
                    }
                }
            }

            uiState.barrierResult?.let { barrier ->
                Card(modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("声屏障插入损失", fontWeight = FontWeight.Bold)
                        Text("IL = ${"%.1f".format(barrier.insertionLossDb)} dB")
                        Text("菲涅尔数 N = ${"%.2f".format(barrier.fresnelNumber)}")
                        Text("路径差 δ = ${"%.3f".format(barrier.pathDifferenceM)} m")
                        Text(barrier.interpretation)
                    }
                }
            }
        }
    }
}
