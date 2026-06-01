package com.acoustics.calculator.core.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.acoustics.calculator.core.constants.AppVersion
import com.acoustics.calculator.domain.repository.Project
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * 导出工具 — 文件存到手机「下载」文件夹 + 文件级分享
 */
object ExportUtils {

    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    /**
     * 导出项目为 CSV + 分享文件（一步完成）
     * 返回 File 对象，用于 FileProvider 分享
     */
    suspend fun exportAndShareProject(context: Context, project: Project, share: Boolean = true): File? {
        try {
            // 1. 先写入缓存（保证成功）
            val fileName = "声学项目_${sanitizeFileName(project.name)}_${dateFormat.format(Date())}.csv"
            val cacheFile = File(context.cacheDir, fileName)
            cacheFile.writeText(buildProjectCsv(project), Charsets.UTF_8)

            // 2. 同步到公共下载目录（让用户能看到）
            syncToPublicDownloads(context, fileName, "text/csv", cacheFile)

            // 3. 如果要分享，直接弹分享
            if (share) {
                shareFile(context, cacheFile, "text/csv")
            }

            return cacheFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * 导出计算结果为 CSV + 分享（一步完成）
     */
    suspend fun exportAndShareResult(
        context: Context,
        title: String,
        headers: List<String>,
        dataRows: List<List<String>>,
        share: Boolean = true
    ): File? {
        try {
            val fileName = "${sanitizeFileName(title)}_${dateFormat.format(Date())}.csv"
            val cacheFile = File(context.cacheDir, fileName)
            cacheFile.writeText(buildResultCsv(title, headers, dataRows), Charsets.UTF_8)

            syncToPublicDownloads(context, fileName, "text/csv", cacheFile)

            if (share) {
                shareFile(context, cacheFile, "text/csv")
            }

            return cacheFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * 分享文件（通过 FileProvider 生成 URI，系统分享菜单）
     */
    fun shareFile(context: Context, file: File, mimeType: String = "*/*") {
        try {
            if (!file.exists()) {
                Toast.makeText(context, "文件不存在", Toast.LENGTH_SHORT).show()
                return
            }

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

            // 用选择器确保用户能选微信/QQ/蓝牙等
            val chooser = Intent.createChooser(intent, "分享文件到")
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "分享失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 分享文件（通过文件路径）
     */
    fun shareFile(context: Context, filePath: String, mimeType: String = "*/*") {
        val file = File(filePath)
        shareFile(context, file, mimeType)
    }

    // ======================== 内容构建 ========================

    private fun buildProjectCsv(project: Project): String {
        val sb = StringBuilder()
        sb.append("﻿") // BOM for Excel
        sb.appendLine("建筑声学计算器 - 项目导出")
        sb.appendLine("导出时间,${displayDateFormat.format(Date())}")
        sb.appendLine("版本,${AppVersion.DISPLAY}")
        sb.appendLine()
        sb.appendLine("项目名称,${escapeCsv(project.name)}")
        sb.appendLine("项目类型,${escapeCsv(project.projectType)}")
        sb.appendLine("描述,${escapeCsv(project.description)}")
        sb.appendLine("创建时间,${displayDateFormat.format(Date(project.createdAt))}")
        sb.appendLine("更新时间,${displayDateFormat.format(Date(project.updatedAt))}")
        return sb.toString()
    }

    private fun buildResultCsv(title: String, headers: List<String>, dataRows: List<List<String>>): String {
        val sb = StringBuilder()
        sb.append("﻿")
        sb.appendLine("建筑声学计算器 - $title")
        sb.appendLine("导出时间,${displayDateFormat.format(Date())}")
        sb.appendLine("版本,${AppVersion.DISPLAY}")
        sb.appendLine()
        sb.appendLine(headers.joinToString(",") { escapeCsv(it) })
        dataRows.forEach { row ->
            sb.appendLine(row.joinToString(",") { escapeCsv(it) })
        }
        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else value
    }

    // ======================== 同步到公共下载目录 ========================

    /**
     * 将缓存文件同步到手机「下载」文件夹，让用户能看见
     */
    private fun syncToPublicDownloads(context: Context, fileName: String, mimeType: String, sourceFile: File) {
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                // Android 10+：MediaStore API
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, mimeType)
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/AcousticsCalculator")
                    put(MediaStore.Downloads.IS_PENDING, 0)
                    put(MediaStore.Downloads.DATE_ADDED, System.currentTimeMillis() / 1000)
                }

                val uri = context.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues
                )

                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        sourceFile.inputStream().use { input ->
                            input.copyTo(output, bufferSize = 64 * 1024)
                        }
                        output.flush()
                    }
                }
            } else {
                // Android 9-：直接写公共目录
                val dir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                )
                if (!dir.exists()) dir.mkdirs()
                val destFile = File(dir, fileName)
                sourceFile.copyTo(destFile, overwrite = true)

                // 通知系统扫描
                try {
                    val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                        data = Uri.fromFile(destFile)
                    }
                    context.sendBroadcast(intent)
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            // 同步到公共目录失败不阻塞主流程
            android.util.Log.w("ExportUtils", "syncToPublicDownloads failed: ${e.message}")
        }
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[\\\\/:*?\"<>|]"), "_").take(30)
    }
}
