package com.acoustics.calculator.core.utils

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * 阿里云短信服务
 *
 * 使用前需配置：
 * 1. 在阿里云开通短信服务 https://dysms.console.aliyun.com/
 * 2. 获取 AccessKey ID 和 AccessKey Secret
 * 3. 创建短信模板并获取模板CODE
 * 4. 将以下配置填入
 */
object AliyunSmsService {

    // ===== 配置区（需要用户自行填写）=====
    // 阿里云 AccessKey
    private const val ACCESS_KEY_ID = "your-access-key-id"
    private const val ACCESS_KEY_SECRET = "your-access-key-secret"

    // 短信签名（需在阿里云短信控制台审核通过）
    private const val SIGN_NAME = "建筑声学计算器"

    // 短信模板CODE（需在阿里云短信控制台创建并审核通过）
    private const val TEMPLATE_CODE = "SMS_0000000000"

    // 模板变量名（验证码变量名，默认为 code）
    private const val TEMPLATE_VAR = "code"

    // 阿里云短信API地址
    private const val API_URL = "https://dysmsapi.aliyuncs.com"

    /**
     * 发送验证码短信
     * @param phone 手机号
     * @param code 验证码
     * @return Result 包含是否成功和信息
     */
    suspend fun sendSmsCode(phone: String, code: String): SmsResult = withContext(Dispatchers.IO) {
        // 如果未配置真实密钥，使用模拟模式
        if (ACCESS_KEY_ID == "your-access-key-id") {
            return@withContext SmsResult(true, "模拟模式：验证码 $code 已发送至 $phone（配置阿里云密钥后自动切换真实短信）")
        }

        try {
            val params = buildCommonParams().apply {
                put("Action", "SendSms")
                put("PhoneNumbers", phone)
                put("SignName", SIGN_NAME)
                put("TemplateCode", TEMPLATE_CODE)
                put("TemplateParam", "{\"$TEMPLATE_VAR\":\"$code\"}")
            }

            val sortedKeys = params.keys.sorted()
            val canonicalizedQuery = sortedKeys.joinToString("&") { key ->
                "${percentEncode(key)}=${percentEncode(params[key]!!)}"
            }

            val stringToSign = "GET&${percentEncode("/")}&${percentEncode(canonicalizedQuery)}"
            val signature = hmacSha1(stringToSign, "$ACCESS_KEY_SECRET&")
            params["Signature"] = signature

            val queryString = params.entries.joinToString("&") { (key, value) ->
                "${percentEncode(key)}=${percentEncode(value)}"
            }

            val conn = URL("$API_URL/?$queryString").openConnection() as HttpURLConnection
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.requestMethod = "GET"
            conn.connect()

            val response = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()

            // 解析响应
            val bizId = extractJsonValue(response, "BizId")
            val code2 = extractJsonValue(response, "Code")
            val message = extractJsonValue(response, "Message")

            if (code2 == "OK") {
                SmsResult(true, "验证码已发送至尾号 ${phone.takeLast(4)} 的手机")
            } else {
                SmsResult(false, "短信发送失败: $message")
            }
        } catch (e: Exception) {
            SmsResult(false, "短信发送失败: ${e.message}")
        }
    }

    private fun buildCommonParams(): LinkedHashMap<String, String> {
        val params = LinkedHashMap<String, String>()
        params["AccessKeyId"] = ACCESS_KEY_ID
        params["Timestamp"] = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())
        params["Format"] = "JSON"
        params["SignatureMethod"] = "HMAC-SHA1"
        params["SignatureVersion"] = "1.0"
        params["SignatureNonce"] = UUID.randomUUID().toString()
        params["Version"] = "2017-05-25"
        return params
    }

    private fun percentEncode(value: String): String {
        return URLEncoder.encode(value, "UTF-8")
            .replace("+", "%20")
            .replace("*", "%2A")
            .replace("%7E", "~")
    }

    private fun hmacSha1(data: String, key: String): String {
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(key.toByteArray(Charsets.UTF_8), "HmacSHA1"))
        val result = mac.doFinal(data.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(result, Base64.NO_WRAP)
    }

    private fun extractJsonValue(json: String, key: String): String {
        val search = "\"$key\":\""
        val start = json.indexOf(search)
        if (start < 0) return ""
        val valueStart = start + search.length
        val valueEnd = json.indexOf("\"", valueStart)
        return if (valueEnd > valueStart) json.substring(valueStart, valueEnd) else ""
    }
}

data class SmsResult(
    val success: Boolean,
    val message: String
)
