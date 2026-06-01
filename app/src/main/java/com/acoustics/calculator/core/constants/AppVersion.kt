package com.acoustics.calculator.core.constants

/**
 * 应用版本中心 — 所有界面统一从此读取版本信息
 * 修改 build.gradle.kts 中的 versionCode/versionName 后同步更新此处
 */
object AppVersion {
    /** 必须与 build.gradle.kts 中的 versionCode 一致 */
    const val CODE: Int = 6

    /** 必须与 build.gradle.kts 中的 versionName 一致 */
    const val NAME: String = "2.2.2"

    /** 展示用标签（包含版本号） */
    const val DISPLAY: String = "v$NAME"

    /** 完整标题栏标签 */
    const val TITLE_TAG: String = "v$NAME"

    /** 构建号 */
    const val BUILD: String = "20260602"

    /** 版本历史 */
    val history: List<VersionEntry> = listOf(
        VersionEntry(1, "v1.0.0", "初始版本：室内声学计算、隔声计算、噪声预测"),
        VersionEntry(2, "v2.0.0", "大版本更新：声学知识库、设计实例、房间模式、声屏障、HVAC噪声、霓虹UI"),
        VersionEntry(3, "v2.1.1", "新增手机号验证码登录系统、优化更新机制"),
        VersionEntry(4, "v2.2.0", "项目系统增强、导出Excel/CSV、版本统一、主页登录/登出、全界面霓虹主题统一"),
        VersionEntry(5, "v2.2.1", "修复：材料库因ProGuard混淆导致JSON解析失败的问题"),
        VersionEntry(6, "v2.2.2", "修复：材料库因R8内联TypeToken导致材料为空的问题"),
    )
}

data class VersionEntry(
    val code: Int,
    val name: String,
    val notes: String
)
