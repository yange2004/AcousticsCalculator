package com.acoustics.calculator.core.utils

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import com.acoustics.calculator.core.constants.AppVersion
import com.acoustics.calculator.domain.repository.Project
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * 导出工具 — 支持 CSV、文本导出和分享
 */
object ExportUtils {

    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    /**
     * 导出项目信息为 CSV 文件（可用 Excel 打开）
     */
    suspend fun exportProjectToCsv(context: Context, project: Project): String? {
        return try {
            val dir = getExportDir(context) ?: return null
            val fileName = "项目_${project.name}_${dateFormat.format(Date())}.csv"
            val file = File(dir, fileName)

            FileWriter(file).use { writer ->
                // BOM for Excel UTF-8 compatibility
                writer.write("﻿")
                writer.write("建筑声学计算器 - 项目导出\n")
                writer.write("导出时间,${displayDateFormat.format(Date())}\n")
                writer.write("版本,${AppVersion.DISPLAY}\n\n")

                writer.write("项目名称,${project.name}\n")
                writer.write("项目类型,${project.projectType}\n")
                writer.write("描述,${project.description}\n")
                writer.write("创建时间,${displayDateFormat.format(Date(project.createdAt))}\n")
                writer.write("更新时间,${displayDateFormat.format(Date(project.updatedAt))}\n")
                writer.write("标签,${project.tags.joinToString(";")}\n")
            }

            // 通知系统文件管理器
            notifyFileScan(context, file)

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 导出计算结果为 CSV 文件
     */
    suspend fun exportResultToCsv(
        context: Context,
        title: String,
        headers: List<String>,
        dataRows: List<List<String>>
    ): String? {
        return try {
            val dir = getExportDir(context) ?: return null
            val fileName = "${title}_${dateFormat.format(Date())}.csv"
            val file = File(dir, fileName)

            FileWriter(file).use { writer ->
                writer.write("﻿")
                writer.write("建筑声学计算器 - $title\n")
                writer.write("导出时间,${displayDateFormat.format(Date())}\n")
                writer.write("版本,${AppVersion.DISPLAY}\n\n")

                // Headers
                writer.write(headers.joinToString(",") { "\"$it\"" } + "\n")

                // Data rows
                dataRows.forEach { row ->
                    writer.write(row.joinToString(",") { "\"$it\"" } + "\n")
                }
            }

            notifyFileScan(context, file)
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 导出纯文本报告
     */
    suspend fun exportTextReport(context: Context, title: String, content: String): String? {
        return try {
            val dir = getExportDir(context) ?: return null
            val fileName = "${title}_${dateFormat.format(Date())}.txt"
            val file = File(dir, fileName)

            file.writeText(content, Charsets.UTF_8)
            notifyFileScan(context, file)

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 分享文件
     */
    fun shareFile(context: Context, filePath: String, mimeType: String = "*/*") {
        try {
            val file = File(filePath)
            if (!file.exists()) return

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(Intent.createChooser(intent, "分享文件"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 获取导出目录（外部存储 Downloads 目录）
     */
    private fun getExportDir(context: Context): File? {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: File(context.filesDir, "exports")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * 通知系统媒体库扫描文件
     */
    private fun notifyFileScan(context: Context, file: File) {
        try {
            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                data = android.net.Uri.fromFile(file)
            }
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            // 非关键操作
        }
    }

    /**
     * 格式化时间戳
     */
    fun formatTimestamp(timestamp: Long): String {
        return displayDateFormat.format(Date(timestamp))
    }
}
