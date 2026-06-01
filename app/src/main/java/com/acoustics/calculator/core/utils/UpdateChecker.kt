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

    // ===== 远程版本信息地址 =====
    private const val GH_OWNER = "yange2004"
    private const val GH_REPO = "AcousticsCalculator"

    // 主选：GitHub raw 直接获取 version.json（已验证可用，无缓存问题）
    private val GITHUB_RAW_VERSION_URL = "https://raw.githubusercontent.com/$GH_OWNER/$GH_REPO/master/version.json"
    // 主选 APK 下载地址
    private val GITHUB_RAW_APK_URL = "https://raw.githubusercontent.com/$GH_OWNER/$GH_REPO/master/%E5%BB%BA%E7%AD%91%E5%A3%B0%E5%AD%A6%E8%AE%A1%E7%AE%97%E5%99%A8.apk"

    // 备用：GitHub Releases API（需要先创建 Release）
    private val GITHUB_RELEASES_URL = "https://api.github.com/repos/$GH_OWNER/$GH_REPO/releases/latest"

    // 最后手段：Gitee version.json（API 有缓存延迟）
    private const val GITEE_OWNER = "yangyan2004"
    private const val GITEE_REPO = "acoustics-calculator"
    private const val ACCESS_TOKEN = "2fc834fb42f55b5f6c7ec15386cc238c"
    private val GITEE_API_URL = "https://gitee.com/api/v5/repos/$GITEE_OWNER/$GITEE_REPO/contents/version.json?access_token=$ACCESS_TOKEN"

    private const val APK_FILE_NAME = "acoustics_calculator_update.apk"

    /** 版本历史从 AppVersion 中心读取 */
    private val versionHistory = AppVersion.history.map { entry ->
        VersionInfo(entry.code, entry.name, entry.notes, "")
    }

    /**
     * 检查更新
     * 第一优先：GitHub raw version.json（直接下载，无缓存，已验证可用）
     * 第二优先：GitHub Releases API（需要创建 Release）
     * 第三优先：Gitee API（有缓存延迟，最后手段）
     */
    suspend fun checkForUpdate(): UpdateResult = withContext(Dispatchers.IO) {
        // 1. GitHub raw version.json（最快最可靠）
        var networkVersion = fetchVersionFromGitHubRaw()

        // 2. 回退到 GitHub Releases API
        if (networkVersion == null) {
            networkVersion = fetchVersionFromGitHubRelease()
        }

        // 3. 最后回退到 Gitee API
        if (networkVersion == null) {
            networkVersion = fetchVersionFromGiteeApi()
        }

        val hasUpdate = networkVersion != null && networkVersion.versionCode > CURRENT_VERSION_CODE

        // 如果有更新但没拿到下载地址，用默认的 GitHub raw APK 地址
        val finalDownloadUrl = if (networkVersion?.downloadUrl?.isBlank() == true || networkVersion == null) {
            GITHUB_RAW_APK_URL
        } else {
            networkVersion.downloadUrl
        }

        UpdateResult(
            hasUpdate = hasUpdate,
            isFromNetwork = networkVersion != null,
            currentVersionName = CURRENT_VERSION_NAME,
            latestVersionName = networkVersion?.versionName ?: CURRENT_VERSION_NAME,
            latestVersionCode = networkVersion?.versionCode ?: CURRENT_VERSION_CODE,
            downloadUrl = finalDownloadUrl,
            releaseNotes = if (hasUpdate) (networkVersion?.notes ?: "新版本已可用")
            else "当前已是最新版本（$CURRENT_VERSION_NAME）",
            versionHistory = versionHistory
        )
    }

    /**
     * 直接从 GitHub raw 下载 version.json（最可靠的方式）
     * GitHub raw 的 CDN 无缓存问题，且无需任何鉴权
     */
    private fun fetchVersionFromGitHubRaw(): VersionInfo? {
        return try {
            val conn = URL(GITHUB_RAW_VERSION_URL).openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.connect()

            if (conn.responseCode == 200) {
                val json = conn.inputStream.bufferedReader().use { it.readText() }
                conn.disconnect()
                val info = parseVersionJson(json)
                // GitHub raw 返回 JSON，下载 URL 用默认的 GitHub raw APK
                if (info != null) {
                    info.copy(downloadUrl = GITHUB_RAW_APK_URL)
                } else null
            } else {
                conn.disconnect()
                null
            }
        } catch (_: Exception) { null }
    }

    /**
     * 从 GitHub Releases API 获取最新版本
     * 需要先在 GitHub 上创建 Release，否则返回 404 会静默回退
     */
    private fun fetchVersionFromGitHubRelease(): VersionInfo? {
        return try {
            val conn = URL(GITHUB_RELEASES_URL).openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.requestMethod = "GET"
            conn.setRequestProperty("Accept", "application/json")
            conn.connect()

            if (conn.responseCode == 200) {
                val responseJson = conn.inputStream.bufferedReader().use { it.readText() }
                conn.disconnect()

                val jsonObj = org.json.JSONObject(responseJson)
                val tagName = jsonObj.optString("tag_name", "")
                val body = jsonObj.optString("body", "")

                val versionName = tagName.removePrefix("v")
                val versionCode = parseVersionCode(versionName) ?: return null

                // 在 release assets 中找 .apk
                val assets = jsonObj.optJSONArray("assets")
                var downloadUrl = ""
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk")) {
                            downloadUrl = asset.optString("browser_download_url", "")
                            break
                        }
                    }
                }

                VersionInfo(versionCode, tagName, body, downloadUrl)
            } else {
                conn.disconnect()
                null
            }
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
     * 解析版本JSON：{"versionCode":3,"versionName":"2.1.0","downloadUrl":"...","releaseNotes":"..."}
     */
    private fun parseVersionJson(json: String): VersionInfo? {
        return try {
            fun extract(key: String): String {
                val start = json.indexOf("\"$key\"")
                if (start < 0) return ""
                val colon = json.indexOf(':', start) + 1
                // 跳过空格
                var c = colon
                while (c < json.length && json[c] == ' ') c++
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
     */
    suspend fun downloadUpdate(
        context: Context,
        downloadUrl: String = "",
        onProgress: (Float) -> Unit = {}
    ): File? = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val destDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: return@withContext null
            if (!destDir.exists()) destDir.mkdirs()
            val destFile = File(destDir, APK_FILE_NAME)
            if (destFile.exists()) destFile.delete()

            val fallbackUrl = GITHUB_RAW_APK_URL
            val url = downloadUrl.ifBlank { fallbackUrl }
            connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 30000
            connection.connect()

            if (connection.responseCode != 200) {
                connection.disconnect()
                return@withContext null
            }

            val totalBytes = connection.contentLengthLong
            val inputStream = connection.inputStream

            FileOutputStream(destFile).use { output ->
                val buffer = ByteArray(64 * 1024) // 64KB
                var totalRead = 0L
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    totalRead += bytesRead
                    if (totalBytes > 0) {
                        val progress = (totalRead.toFloat() / totalBytes.toFloat()).coerceAtMost(1f)
                        kotlinx.coroutines.runBlocking {
                            withContext(Dispatchers.Main) { onProgress(progress) }
                        }
                    }
                }
                output.flush()
            }

            inputStream.close()
            connection.disconnect()

            if (destFile.exists() && destFile.length() > 0) destFile else null
        } catch (e: Exception) {
            e.printStackTrace()
            connection?.disconnect()
            null
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
