package com.acoustics.calculator.core.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.acoustics.calculator.core.constants.AppVersion
import com.acoustics.calculator.domain.repository.Project
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * 导出工具 — 文件存到手机真正的「下载」文件夹，用户能看见的那种
 */
object ExportUtils {

    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    /**
     * 导出项目信息为 CSV 文件（存到手机「下载」文件夹）
     */
    suspend fun exportProjectToCsv(context: Context, project: Project): String? {
        return try {
            val fileName = "声学项目_${sanitizeFileName(project.name)}_${dateFormat.format(Date())}.csv"
            val csvContent = buildProjectCsv(project)
            if (Build.VERSION.SDK_INT >= 29) {
                saveToPublicDownloads(context, fileName, "text/csv", csvContent)
            } else {
                saveToLegacyDownloads(context, fileName, csvContent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 导出计算结果为 CSV 文件（存到手机「下载」文件夹）
     */
    suspend fun exportResultToCsv(
        context: Context,
        title: String,
        headers: List<String>,
        dataRows: List<List<String>>
    ): String? {
        return try {
            val fileName = "${sanitizeFileName(title)}_${dateFormat.format(Date())}.csv"
            val csvContent = buildResultCsv(title, headers, dataRows)
            if (Build.VERSION.SDK_INT >= 29) {
                saveToPublicDownloads(context, fileName, "text/csv", csvContent)
            } else {
                saveToLegacyDownloads(context, fileName, csvContent)
            }
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
            val fileName = "${sanitizeFileName(title)}_${dateFormat.format(Date())}.txt"
            if (Build.VERSION.SDK_INT >= 29) {
                saveToPublicDownloads(context, fileName, "text/plain", content)
            } else {
                saveToLegacyDownloads(context, fileName, content)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 分享文件（直接弹出系统分享菜单）
     */
    fun shareFile(context: Context, filePath: String, mimeType: String = "*/*") {
        try {
            val file = File(filePath)
            if (!file.exists()) return
            val uri = FileProvider.getUriForFile(
                context, "${context.packageName}.fileprovider", file
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
     * 直接分享文本内容（不用先存文件）
     */
    fun shareText(context: Context, title: String, text: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(intent, "分享到"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ======================== 构建文件内容 ========================

    private fun buildProjectCsv(project: Project): String {
        val sb = StringBuilder()
        sb.append("﻿") // BOM for Excel UTF-8
        sb.appendLine("建筑声学计算器 - 项目导出")
        sb.appendLine("导出时间,${displayDateFormat.format(Date())}")
        sb.appendLine("版本,${AppVersion.DISPLAY}")
        sb.appendLine()
        sb.appendLine("项目名称,${project.name}")
        sb.appendLine("项目类型,${project.projectType}")
        sb.appendLine("描述,${project.description}")
        sb.appendLine("创建时间,${displayDateFormat.format(Date(project.createdAt))}")
        sb.appendLine("更新时间,${displayDateFormat.format(Date(project.updatedAt))}")
        sb.appendLine("标签,${project.tags.joinToString(";")}")
        return sb.toString()
    }

    private fun buildResultCsv(title: String, headers: List<String>, dataRows: List<List<String>>): String {
        val sb = StringBuilder()
        sb.append("﻿")
        sb.appendLine("建筑声学计算器 - $title")
        sb.appendLine("导出时间,${displayDateFormat.format(Date())}")
        sb.appendLine("版本,${AppVersion.DISPLAY}")
        sb.appendLine()
        sb.appendLine(headers.joinToString(",") { "\"$it\"" })
        dataRows.forEach { row ->
            sb.appendLine(row.joinToString(",") { "\"$it\"" })
        }
        return sb.toString()
    }

    // ======================== 保存到公共目录（Android 10+） ========================

    /**
     * Android 10+ (API 29+)：使用 MediaStore 存到「下载」文件夹
     * 用户在任何文件管理器都能看到
     */
    private fun saveToPublicDownloads(
        context: Context,
        fileName: String,
        mimeType: String,
        content: String
    ): String? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, mimeType)
            put(MediaStore.Downloads.DATE_ADDED, System.currentTimeMillis() / 1000)
            put(MediaStore.Downloads.DATE_MODIFIED, System.currentTimeMillis() / 1000)
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: return null

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(content.toByteArray(Charsets.UTF_8))
            outputStream.flush()
        }

        return uri.toString()
    }

    /**
     * Android 9 及以下：直接写公共 Downloads 目录
     */
    private fun saveToLegacyDownloads(
        context: Context,
        fileName: String,
        content: String
    ): String? {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, fileName)
        FileOutputStream(file).use { output ->
            output.write(content.toByteArray(Charsets.UTF_8))
            output.flush()
        }
        // 通知系统扫描
        try {
            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                data = android.net.Uri.fromFile(file)
            }
            context.sendBroadcast(intent)
        } catch (_: Exception) {}
        return file.absolutePath
    }

    /**
     * 返回导出文件所在目录的路径描述（用于界面显示）
     */
    fun getExportPathDescription(): String {
        return if (Build.VERSION.SDK_INT >= 29) {
            "手机存储/Download/"
        } else {
            "手机存储/Download/"
        }
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[\\\\/:*?\"<>|]"), "_")
            .take(30)
    }

    fun formatTimestamp(timestamp: Long): String {
        return displayDateFormat.format(Date(timestamp))
    }
}
