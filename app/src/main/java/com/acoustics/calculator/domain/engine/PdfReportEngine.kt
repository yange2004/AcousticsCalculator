package com.acoustics.calculator.domain.engine

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.acoustics.calculator.domain.model.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * PDF report generator for acoustic calculations.
 * Uses Android's built-in PdfDocument API.
 */
class PdfReportEngine {

    private val pageWidth = 595 // A4 width in points
    private val pageHeight = 842 // A4 height in points
    private val margin = 40f
    private var yPos = margin

    private val titlePaint = Paint().apply {
        textSize = 24f
        typeface = Typeface.DEFAULT_BOLD
        color = 0xFF1565C0.toInt()
    }
    private val headerPaint = Paint().apply {
        textSize = 16f
        typeface = Typeface.DEFAULT_BOLD
        color = 0xFF212121.toInt()
    }
    private val bodyPaint = Paint().apply {
        textSize = 11f
        color = 0xFF424242.toInt()
    }
    private val labelPaint = Paint().apply {
        textSize = 10f
        color = 0xFF757575.toInt()
    }
    private val linePaint = Paint().apply {
        color = 0xFFBDBDBD.toInt()
        strokeWidth = 0.5f
    }
    private val highlightPaint = Paint().apply {
        color = 0xFFE3F2FD.toInt() // light blue bg
    }

    /**
     * Generate a PDF report for silencer calculation result.
     * Returns the file path of the generated PDF.
     */
    fun generateSilencerReport(context: Context, result: InsertionLossResult): String? {
        return try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            yPos = margin
            drawReport(canvas, result)

            document.finishPage(page)

            // Save to Downloads folder
            val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "消声器计算报告_$dateStr.pdf"
            val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: context.filesDir
            val file = File(downloadsDir, fileName)
            FileOutputStream(file).use { out ->
                document.writeTo(out)
            }
            document.close()

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun drawReport(canvas: Canvas, result: InsertionLossResult) {
        // Title
        canvas.drawText("消声器设计计算报告", margin, yPos, titlePaint)
        yPos += 40f

        // Separator
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
        yPos += 20f

        // Basic info
        canvas.drawText("基本信息", margin, yPos, headerPaint)
        yPos += 22f

        drawInfoRow(canvas, "消声器类型", result.silencerType.label)
        drawInfoRow(canvas, "长度", "${"%.1f".format(result.params.lengthM)} m")
        drawInfoRow(canvas, "截面积", "${"%.4f".format(result.params.crossSectionAreaM2)} m²")
        drawInfoRow(canvas, "气流速度", "${"%.1f".format(result.params.flowVelocityMs)} m/s")
        drawInfoRow(canvas, "温度", "${"%.0f".format(result.params.temperatureC)} °C")
        result.params.material?.let { m ->
            drawInfoRow(canvas, "吸声材料", "${m.name} (${"%.0f".format(m.thicknessMm)}mm, NRC=${"%.2f".format(m.nrc)})")
        }
        result.params.fanNoiseSource?.let { fan ->
            drawInfoRow(canvas, "风机型号", fan.modelName)
        }

        yPos += 10f
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
        yPos += 20f

        // Results summary
        canvas.drawText("计算结果摘要", margin, yPos, headerPaint)
        yPos += 22f

        drawInfoRow(canvas, "A计权总降噪量", "${"%.1f".format(result.totalADbInsertionLoss)} dB(A)")
        drawInfoRow(canvas, "压力损失", "${"%.0f".format(result.pressureDropPa)} Pa")

        yPos += 10f

        // Octave band table
        canvas.drawText("各倍频程插入损失 (dB)", margin, yPos, headerPaint)
        yPos += 22f

        // Table header
        val colWidth = (pageWidth - 2 * margin) / 9f
        val bands = SilencerBand.ALL_BANDS
        canvas.drawText("频带", margin, yPos, labelPaint)
        bands.forEachIndexed { i, band ->
            canvas.drawText(band.label, margin + (i + 1) * colWidth, yPos, bodyPaint)
        }
        yPos += 12f

        drawTableRow(canvas, "IL", colWidth, bands, result.insertionLossByBand)
        drawTableRow(canvas, "修正IL", colWidth, bands, result.correctedILByBand)
        drawTableRow(canvas, "A计权", colWidth, bands, result.aWeightedILByBand)

        yPos += 10f

        // Flow noise if present
        if (result.flowNoiseByBand.values.any { it > 0 }) {
            canvas.drawText("气流再生噪声", margin, yPos, labelPaint)
            yPos += 12f
            drawTableRow(canvas, "噪声", colWidth, bands, result.flowNoiseByBand)
        }

        // After-noise
        result.afterNoiseByBand?.let { after ->
            yPos += 10f
            canvas.drawText("降噪后噪声值 (dB)", margin, yPos, headerPaint)
            yPos += 22f
            drawTableRow(canvas, "噪声", colWidth, bands, after)
            result.afterTotalADb?.let { total ->
                yPos += 5f
                canvas.drawText("降噪后总A声级: ${"%.1f".format(total)} dB(A)", margin, yPos, bodyPaint)
            }
        }

        // Footer
        yPos = pageHeight - 40f
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
        yPos += 12f
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("生成日期: $dateStr  |  AcousticsCalculator v0.2.0", margin, yPos, labelPaint)
    }

    private fun drawInfoRow(canvas: Canvas, label: String, value: String) {
        canvas.drawText(label, margin, yPos, labelPaint)
        val valueX = margin + 200f
        canvas.drawText(value, valueX, yPos, bodyPaint)
        yPos += 18f
    }

    private fun drawTableRow(
        canvas: Canvas, label: String, colWidth: Float,
        bands: List<SilencerBand>, data: Map<SilencerBand, Double>
    ) {
        if (yPos > pageHeight - 40f) return // prevent overflow (simplified)
        canvas.drawText(label, margin, yPos, labelPaint)
        bands.forEachIndexed { i, band ->
            val value = data[band] ?: 0.0
            canvas.drawText("${"%.1f".format(value)}", margin + (i + 1) * colWidth, yPos, bodyPaint)
        }
        yPos += 14f
    }
}
