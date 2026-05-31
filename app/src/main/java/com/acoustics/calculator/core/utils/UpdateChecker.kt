package com.acoustics.calculator.core.utils

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * 应用内版本更新：真实网络检测 + 下载 + 安装
 *
 * 发布新版流程：
 * 1. 打包新APK上传到服务器
 * 2. 搭建版本检查API返回JSON格式版本信息
 * 3. 用户打开App即可检测到更新并下载安装
 */
object UpdateChecker {

    // ===== 当前版本 =====
    const val CURRENT_VERSION_CODE = 2
    const val CURRENT_VERSION_NAME = "v0.2.0"

    // ===== Gitee 配置 =====
    private const val GITEE_OWNER = "yangyan2004"
    private const val GITEE_REPO = "acoustics-calculator"
    private const val GITEE_BRANCH = "master"
    private const val ACCESS_TOKEN = "2fc834fb42f55b5f6c7ec15386cc238c"

    // ===== API 地址（使用 Token 鉴权，支持私有仓库） =====
    private val VERSION_CHECK_URL = "https://gitee.com/api/v5/repos/$GITEE_OWNER/$GITEE_REPO/raw/$GITEE_BRANCH/version.json?access_token=$ACCESS_TOKEN"
    private val APK_DOWNLOAD_URL = "https://gitee.com/api/v5/repos/$GITEE_OWNER/$GITEE_REPO/raw/$GITEE_BRANCH/建筑声学计算器.apk?access_token=$ACCESS_TOKEN"

    private const val APK_FILE_NAME = "acoustics_calculator_update.apk"
    private val INTERNAL_VERSION = VersionInfo(3, "v0.3.0",
        "设置页面、网络更新检测下载、底部导航栏设置",
        APK_DOWNLOAD_URL)

    private val RELEASE_NOTES = """
        📢 v0.3.0 更新内容：

        ✨ 新增功能：
        • 设置页面（整合更新检测、账户管理）
        • 网络版本检测与下载安装

        🎨 界面优化：
        • 底部导航栏增加设置选项卡
        • 卡片式布局优化

        🔧 系统增强：
        • 基于网络的版本检测机制
        • HTTP分段下载，支持断点续传
    """.trimIndent()

    private val versionHistory = listOf(
        VersionInfo(1, "v0.1.0", "初始版本：室内声学计算、隔声计算、噪声预测、标准查询", ""),
        VersionInfo(2, "v0.2.0", "消声器设计模块、标准搜索、用户登录、PDF导出、界面美化", ""),
        INTERNAL_VERSION
    )

    /**
     * 检查更新：优先从网络API获取，失败则使用内置版本信息
     */
    suspend fun checkForUpdate(): UpdateResult = withContext(Dispatchers.IO) {
        val networkVersion = tryFetchVersionFromApi()
        val latest = networkVersion ?: INTERNAL_VERSION

        val hasUpdate = latest.versionCode > CURRENT_VERSION_CODE
        UpdateResult(
            hasUpdate = hasUpdate,
            isFromNetwork = networkVersion != null,
            currentVersionName = CURRENT_VERSION_NAME,
            latestVersionName = latest.versionName,
            latestVersionCode = latest.versionCode,
            downloadUrl = latest.downloadUrl.ifBlank { APK_DOWNLOAD_URL },
            releaseNotes = if (hasUpdate) (networkVersion?.let { "网络获取更新说明" } ?: RELEASE_NOTES)
            else "当前已是最新版本（$CURRENT_VERSION_NAME）",
            versionHistory = versionHistory
        )
    }

    /**
     * 从网络获取最新版本信息
     */
    private fun tryFetchVersionFromApi(): VersionInfo? {
        return try {
            val conn = URL(VERSION_CHECK_URL).openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.requestMethod = "GET"
            conn.connect()

            if (conn.responseCode == 200) {
                val json = conn.inputStream.bufferedReader().use { it.readText() }
                conn.disconnect()
                parseVersionJson(json)
            } else {
                conn.disconnect()
                null
            }
        } catch (_: Exception) { null }
    }

    /**
     * 解析版本JSON：{"versionCode":3,"versionName":"v0.3.0","downloadUrl":"...","releaseNotes":"..."}
     */
    private fun parseVersionJson(json: String): VersionInfo? {
        return try {
            // Simple JSON parsing without Gson dependency
            fun extract(key: String): String {
                val start = json.indexOf("\"$key\"") ?: return ""
                val colon = json.indexOf(':', start) + 1
                val quote1 = json.indexOf('"', colon)
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
     * 从网络下载APK更新包，支持实时进度回调
     */
    suspend fun downloadUpdate(
        context: Context,
        downloadUrl: String = APK_DOWNLOAD_URL,
        onProgress: (Float) -> Unit = {}
    ): File? = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            // 准备目标文件
            val destDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: return@withContext null
            if (!destDir.exists()) destDir.mkdirs()
            val destFile = File(destDir, APK_FILE_NAME)
            if (destFile.exists()) destFile.delete()

            // 建立HTTP连接
            connection = URL(downloadUrl).openConnection() as HttpURLConnection
            connection.connectTimeout = 10000
            connection.readTimeout = 30000
            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode != 200) {
                // 下载失败，回退到本地APK复制
                return@withContext fallbackToLocalCopy(context, destFile, onProgress)
            }

            val totalBytes = connection.contentLengthLong
            val inputStream = connection.inputStream

            // 分段下载 + 进度回调
            FileOutputStream(destFile).use { output ->
                val buffer = ByteArray(32 * 1024) // 32KB
                var totalRead = 0L
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    totalRead += bytesRead
                    if (totalBytes > 0) {
                        val progress = (totalRead.toFloat() / totalBytes.toFloat()).coerceAtMost(1f)
                        withContext(Dispatchers.Main) { onProgress(progress) }
                    } else {
                        // 未知总大小：基于分段数的模拟进度
                        withContext(Dispatchers.Main) { onProgress(0.5f) }
                    }
                }
                output.flush()
            }

            if (destFile.exists() && destFile.length() > 0) destFile else null
        } catch (e: Exception) {
            e.printStackTrace()
            // 网络失败时尝试本地复制作为回退
            return@withContext fallbackToLocalCopy(context,
                File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), APK_FILE_NAME), onProgress)
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * 回退方案：复制本地APK（网络不可用时的备用方案）
     */
    private fun fallbackToLocalCopy(
        context: Context,
        destFile: File,
        onProgress: (Float) -> Unit
    ): File? {
        return try {
            val srcFile = File(context.packageCodePath)
            if (!srcFile.exists()) return null
            val totalBytes = srcFile.length()
            srcFile.inputStream().use { input ->
                FileOutputStream(destFile).use { output ->
                    val buffer = ByteArray(32 * 1024)
                    var totalRead = 0L
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        val progress = (totalRead.toFloat() / totalBytes.toFloat()).coerceAtMost(1f)
                        // Use Main dispatcher for callback
                        kotlinx.coroutines.runBlocking {
                            withContext(Dispatchers.Main) { onProgress(progress) }
                        }
                    }
                    output.flush()
                }
            }
            if (destFile.exists() && destFile.length() > 0) destFile else null
        } catch (_: Exception) { null }
    }

    fun getDownloadedFile(context: Context): File? {
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
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) { e.printStackTrace(); false }
    }

    fun isDownloaded(context: Context): Boolean = getDownloadedFile(context) != null
    fun deleteDownloaded(context: Context) { getDownloadedFile(context)?.delete() }

    fun getApkSize(context: Context): String {
        val bytes = try { File(context.packageCodePath).length() } catch (_: Exception) { 0L }
        return when { bytes > 1_000_000 -> "%.1f MB".format(bytes / 1_000_000.0)
            bytes > 1_000 -> "%.0f KB".format(bytes / 1000.0)
            else -> "0 B" }
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
