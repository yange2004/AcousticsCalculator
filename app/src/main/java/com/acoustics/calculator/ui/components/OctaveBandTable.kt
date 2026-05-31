package com.acoustics.calculator.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.acoustics.calculator.core.constants.FrequencyBand
import com.acoustics.calculator.core.extensions.roundTo

@Composable
fun OctaveBandTable(
    values: Map<FrequencyBand, Double>,
    label: String,
    unit: String = "",
    decimals: Int = 2,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))

            // Header row
            Row(modifier = Modifier.fillMaxWidth()) {
                FrequencyBand.OCTAVE_BANDS.forEach { band ->
                    Text(
                        band.label,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))

            // Value row
            Row(modifier = Modifier.fillMaxWidth()) {
                FrequencyBand.OCTAVE_BANDS.forEach { band ->
                    val value = values[band]
                    val text = if (value == null || value.isInfinite()) "∞"
                    else "${value.roundTo(decimals)}$unit"
                    Text(
                        text,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
