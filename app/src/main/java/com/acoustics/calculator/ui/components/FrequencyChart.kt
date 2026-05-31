package com.acoustics.calculator.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.acoustics.calculator.core.constants.FrequencyBand
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

@Composable
fun FrequencyChart(
    data: Map<FrequencyBand, Double>,
    yAxisLabel: String = "",
    title: String = "",
    standardValues: Map<FrequencyBand, Double>? = null,
    modifier: Modifier = Modifier
) {
    val labels = remember { FrequencyBand.OCTAVE_BANDS.map { it.label } }

    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setTouchEnabled(true)
                setPinchZoom(true)
                setScaleEnabled(true)
                legend.isEnabled = true
                axisRight.isEnabled = false

                xAxis.apply {
                    valueFormatter = IndexAxisValueFormatter(labels)
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    setDrawGridLines(true)
                }
                axisLeft.apply {
                    axisMinimum = 0f
                    setDrawGridLines(true)
                }
                invalidate()
            }
        },
        update = { chart ->
            val entries = data.entries.mapIndexed { index, (_, value) ->
                Entry(index.toFloat(), value.toFloat().coerceAtMost(10f))
            }

            val dataSet = LineDataSet(entries, title).apply {
                color = android.graphics.Color.rgb(21, 101, 192)
                setCircleColor(android.graphics.Color.rgb(21, 101, 192))
                lineWidth = 2f
                circleRadius = 4f
                mode = LineDataSet.Mode.CUBIC_BEZIER
                setDrawValues(true)
                valueTextSize = 10f
            }

            val lineData = LineData(dataSet)

            // Add standard reference line if provided
            if (standardValues != null) {
                val standardEntries = standardValues.entries.mapIndexed { index, (_, value) ->
                    Entry(index.toFloat(), value.toFloat().coerceAtMost(10f))
                }
                val standardSet = LineDataSet(standardEntries, "标准值").apply {
                    color = android.graphics.Color.rgb(255, 152, 0)
                    setCircleColor(android.graphics.Color.rgb(255, 152, 0))
                    lineWidth = 2f
                    circleRadius = 3f
                    enableDashedLine(10f, 5f, 0f)
                    setDrawValues(false)
                }
                lineData.addDataSet(standardSet)
            }

            chart.data = lineData
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            chart.invalidate()
        },
        modifier = modifier.fillMaxWidth().height(250.dp)
    )
}
