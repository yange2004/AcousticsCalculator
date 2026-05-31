package com.acoustics.calculator.ui.screen.converter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.acoustics.calculator.core.utils.UnitConverter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen() {
    var inputValue by remember { mutableStateOf("") }
    var selectedConversion by remember { mutableStateOf(0) }

    val conversions = listOf(
        "m → ft" to { v: Double -> UnitConverter.metersToFeet(v) },
        "ft → m" to { v: Double -> UnitConverter.feetToMeters(v) },
        "Pa → dB SPL" to { v: Double -> UnitConverter.pascalToDbSpl(v) },
        "dB SPL → Pa" to { v: Double -> UnitConverter.dbSplToPascal(v) },
        "Hz → λ (波长)" to { v: Double -> UnitConverter.frequencyToWavelength(v) },
        "λ → Hz" to { v: Double -> UnitConverter.wavelengthToFrequency(v) },
        "m² → ft²" to { v: Double -> UnitConverter.sqMetersToSqFeet(v) },
        "ft² → m²" to { v: Double -> UnitConverter.sqFeetToSqMeters(v) },
        "kg/m³ → lb/ft³" to { v: Double -> UnitConverter.kgm3ToLbft3(v) },
        "m³ → ft³" to { v: Double -> UnitConverter.cubicMetersToCubicFeet(v) }
    )

    val result = remember(inputValue, selectedConversion) {
        val v = inputValue.toDoubleOrNull()
        if (v != null) {
            try {
                "%.4f".format(conversions[selectedConversion].second(v))
            } catch (e: Exception) { "—" }
        } else "—"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("单位转换器") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
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
            Text("声学常用单位转换", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = inputValue,
                onValueChange = { inputValue = it },
                label = { Text("输入值") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text("选择转换类型", fontWeight = FontWeight.SemiBold)
            conversions.forEachIndexed { index, (label, _) ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    RadioButton(
                        selected = selectedConversion == index,
                        onClick = { selectedConversion = index }
                    )
                    Text(label, modifier = Modifier.padding(start = 8.dp))
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("结果", style = MaterialTheme.typography.labelMedium)
                    Text(
                        result,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
