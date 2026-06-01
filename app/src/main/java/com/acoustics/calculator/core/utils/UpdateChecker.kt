package com.acoustics.calculator.core.utils

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import com.acoustics.calculator.core.constants.AppVersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * 应用内版本更新：真实网络检测 + 下载 + 安装
 */
object UpdateChecker {

    // ===== 当前版本（从 AppVersion 中心读取，所有界面统一） =====
    val CURRENT_VERSION_CODE: Int = AppVersion.CODE
    val CURRENT_VERSION_NAME: String = AppVersion.NAME

    // ===== 多 CDN 下载地址（按速度优先级排序） =====
    private const val GH_OWNER = "yange2004"
    private const val GH_REPO = "AcousticsCalculator"
    private const val APK_FILE_NAME = "acoustics_calculator_update.apk"

    // version.json 来源（哪个快用哪个）
    private val VERSION_JSON_URLS = listOf(
        "https://cdn.jsdelivr.net/gh/$GH_OWNER/$GH_REPO@master/version.json",
        "https://raw.githubusercontent.com/$GH_OWNER/$GH_REPO/master/version.json",
        "https://ghproxy.net/https://raw.githubusercontent.com/$GH_OWNER/$GH_REPO/master/version.json",
    )

    // APK 下载地址（按速度优先级排序）
    private val APK_DOWNLOAD_URLS = listOf(
        "https://cdn.jsdelivr.net/gh/$GH_OWNER/$GH_REPO@master/建筑声学计算器.apk",
        "https://raw.githubusercontent.com/$GH_OWNER/$GH_REPO/master/建筑声学计算器.apk",
        "https://ghproxy.net/https://raw.githubusercontent.com/$GH_OWNER/$GH_REPO/master/建筑声学计算器.apk",
        "https://github.com/$GH_OWNER/$GH_REPO/releases/download/v${AppVersion.NAME}/建筑声学计算器.apk",
    )

    // Gitee 回退（最后手段）
    private const val GITEE_OWNER = "yangyan2004"
    private const val GITEE_REPO = "acoustics-calculator"
    private const val ACCESS_TOKEN = "2fc834fb42f55b5f6c7ec15386cc238c"
    private val GITEE_API_URL = "https://gitee.com/api/v5/repos/$GITEE_OWNER/$GITEE_REPO/contents/version.json?access_token=$ACCESS_TOKEN"

    /** 版本历史从 AppVersion 中心读取 */
    private val versionHistory = AppVersion.history.map { entry ->
        VersionInfo(entry.code, entry.name, entry.notes, "")
    }

    /**
     * 检查更新 — 遍历多个 CDN 来源，哪个先返回有效数据就用哪个
     */
    suspend fun checkForUpdate(): UpdateResult = withContext(Dispatchers.IO) {
        var networkVersion: VersionInfo? = null

        for (url in VERSION_JSON_URLS) {
            networkVersion = fetchJsonFromUrl(url)
            if (networkVersion != null) break
        }

        if (networkVersion == null) {
            networkVersion = fetchVersionFromGiteeApi()
        }

        val hasUpdate = networkVersion != null && networkVersion.versionCode > CURRENT_VERSION_CODE

        UpdateResult(
            hasUpdate = hasUpdate,
            isFromNetwork = networkVersion != null,
            currentVersionName = CURRENT_VERSION_NAME,
            latestVersionName = networkVersion?.versionName ?: CURRENT_VERSION_NAME,
            latestVersionCode = networkVersion?.versionCode ?: CURRENT_VERSION_CODE,
            downloadUrl = networkVersion?.downloadUrl ?: "",
            releaseNotes = if (hasUpdate) (networkVersion?.notes ?: "新版本已可用")
            else "当前已是最新版本（$CURRENT_VERSION_NAME）",
            versionHistory = versionHistory
        )
    }

    /**
     * 从指定 URL 获取 version.json（3 秒超时）
     */
    private fun fetchJsonFromUrl(url: String): VersionInfo? {
        return try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.setRequestProperty("User-Agent", "AcousticsCalculator/2.2.0")
            conn.connect()
            if (conn.responseCode == 200) {
                val json = conn.inputStream.bufferedReader().use { it.readText() }
                conn.disconnect()
                parseVersionJson(json)
            } else { conn.disconnect(); null }
        } catch (_: Exception) { null }
    }

    /** 从版本名（如 "2.2.0"）解析 versionCode */
    private fun parseVersionCode(versionName: String): Int? {
        val parts = versionName.split(".").take(3)
        if (parts.size < 3) return null
        return when {
            parts[0] == "2" && parts[1] == "2" && parts[2] == "0" -> 4
            parts[0] == "2" && parts[1] == "1" && parts[2] == "1" -> 3
            parts[0] == "2" && parts[1] == "0" && parts[2] == "0" -> 2
            parts[0] == "1" && parts[1] == "0" && parts[2] == "0" -> 1
            else -> null
        }
    }

    /**
     * 从 Gitee API 获取 version.json（最后手段，有缓存延迟）
     */
    private fun fetchVersionFromGiteeApi(): VersionInfo? {
        return try {
            val conn = URL(GITEE_API_URL).openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.connect()

            if (conn.responseCode == 200) {
                val responseJson = conn.inputStream.bufferedReader().use { it.readText() }
                conn.disconnect()
                // Gitee API 返回 base64 编码的内容
                val contentField = "\"content\":"
                val contentStart = responseJson.indexOf(contentField)
                if (contentStart < 0) return null
                val quote1 = responseJson.indexOf('"', contentStart + contentField.length)
                val quote2 = responseJson.indexOf('"', quote1 + 1)
                if (quote1 < 0 || quote2 <= quote1) return null
                val base64Content = responseJson.substring(quote1 + 1, quote2)
                    .replace("\\n", "").replace("\n", "")
                val decodedBytes = android.util.Base64.decode(base64Content, android.util.Base64.DEFAULT)
                val decodedJson = String(decodedBytes, Charsets.UTF_8)
                parseVersionJson(decodedJson)
            } else {
                conn.disconnect()
                null
            }
        } catch (_: Exception) { null }
    }

    /**
     * 解析版本JSON：{"versionCode":"4","versionName":"2.2.0",...}
     * 兼容 versionCode 为数字或字符串两种格式
     */
    private fun parseVersionJson(json: String): VersionInfo? {
        return try {
            fun extract(key: String): String {
                val start = json.indexOf("\"$key\"")
                if (start < 0) return ""
                val colon = json.indexOf(':', start) + 1
                var c = colon
                while (c < json.length && json[c] == ' ') c++
                // 数字值：直接读到逗号/括号
                if (c < json.length && json[c] != '"') {
                    val end = json.indexOfAny(charArrayOf(',', '}', ']', '\n', '\r'), c)
                    return if (end > c) json.substring(c, end).trim() else ""
                }
                // 字符串值：取引号内的内容
                val quote1 = json.indexOf('"', c)
                val quote2 = json.indexOf('"', quote1 + 1)
                return if (quote1 >= 0 && quote2 > quote1) json.substring(quote1 + 1, quote2) else ""
            }
            val code = extract("versionCode").toIntOrNull() ?: return null
            val name = extract("versionName")
            val url = extract("downloadUrl")
            val notes = extract("releaseNotes")
            if (code <= 0 || name.isBlank()) return null
            VersionInfo(code, name, notes, url)
        } catch (_: Exception) { null }
    }

    /**
     * 从网络下载APK更新包
     * 自动遍历多个 CDN 源，哪个能下载用哪个（智能容灾）
     */
    suspend fun downloadUpdate(
        context: Context,
        downloadUrl: String = "",
        onProgress: (Float) -> Unit = {}
    ): File? = withContext(Dispatchers.IO) {
        try {
            val destDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: return@withContext null
            if (!destDir.exists()) destDir.mkdirs()
            val destFile = File(destDir, APK_FILE_NAME)
            if (destFile.exists()) destFile.delete()

            // 构建待尝试的 URL 列表
            val urlsToTry = mutableListOf<String>()
            if (downloadUrl.isNotBlank()) urlsToTry.add(downloadUrl)
            urlsToTry.addAll(APK_DOWNLOAD_URLS)

            var lastError: String? = null

            for (url in urlsToTry) {
                try {
                    val result = tryDownloadFromUrl(url, destFile, onProgress)
                    if (result) {
                        if (destFile.exists() && destFile.length() > 0) return@withContext destFile
                    }
                } catch (e: Exception) {
                    lastError = "${e.message}"
                    continue
                }
            }

            android.util.Log.e("UpdateChecker", "All download sources failed: $lastError")
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /** 从单个 URL 下载 APK */
    private fun tryDownloadFromUrl(
        url: String,
        destFile: File,
        onProgress: (Float) -> Unit
    ): Boolean {
        var connection: HttpURLConnection? = null
        return try {
            connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = 10000
            connection.readTimeout = 30000
            connection.setRequestProperty("User-Agent", "AcousticsCalculator/2.2.0")
            connection.instanceFollowRedirects = true
            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode != 200 && responseCode != 206) {
                connection.disconnect()
                return false
            }

            val totalBytes = connection.contentLengthLong
            val inputStream = connection.inputStream

            FileOutputStream(destFile).use { output ->
                val buffer = ByteArray(64 * 1024)
                var totalRead = 0L
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    totalRead += bytesRead
                    if (totalBytes > 0) {
                        val progress = (totalRead.toFloat() / totalBytes.toFloat()).coerceAtMost(1f)
                        if ((progress * 100).toInt() % 2 == 0) {
                            kotlinx.coroutines.runBlocking {
                                withContext(Dispatchers.Main) { onProgress(progress) }
                            }
                        }
                    }
                }
                output.flush()
            }

            inputStream.close()
            connection.disconnect()
            true
        } catch (e: Exception) {
            connection?.disconnect()
            throw e
        }
    }

    private fun getDownloadedFile(context: Context): File? {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), APK_FILE_NAME)
        return if (file.exists() && file.length() > 0) file else null
    }

    /** 调用系统安装器安装APK */
    fun installApk(context: Context): Boolean {
        return try {
            val file = getDownloadedFile(context) ?: return false
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun isDownloaded(context: Context): Boolean = getDownloadedFile(context) != null
    fun getApkSize(context: Context): String {
        val bytes = try { File(context.packageCodePath).length() } catch (_: Exception) { 0L }
        return when {
            bytes > 1_000_000 -> "%.1f MB".format(bytes / 1_000_000.0)
            bytes > 1_000 -> "%.0f KB".format(bytes / 1000.0)
            else -> "0 B"
        }
    }
}

data class UpdateResult(
    val hasUpdate: Boolean,
    val isFromNetwork: Boolean = false,
    val currentVersionName: String,
    val latestVersionName: String,
    val latestVersionCode: Int,
    val downloadUrl: String,
    val releaseNotes: String,
    val versionHistory: List<VersionInfo>
)

data class VersionInfo(
    val versionCode: Int,
    val versionName: String,
    val notes: String,
    val downloadUrl: String
)
