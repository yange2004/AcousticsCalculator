package com.acoustics.calculator.domain.model

import com.acoustics.calculator.core.constants.FrequencyBand
import com.acoustics.calculator.core.constants.StandardsConstants

/**
 * Reverberation formula types.
 */
enum class ReverberationFormula(val label: String, val description: String) {
    SABINE("赛宾公式", "T60 = 0.161V/A，适用于平均吸声系数 ᾱ < 0.2 的扩散声场（赛宾1900年经典公式）"),
    EYRING("伊林公式", "T60 = 0.161V/[-S·ln(1-ᾱ)]，适用于 ᾱ > 0.2 的高吸声空间"),
    FITZROY("菲茨罗伊公式", "适用于吸声分布不均匀的空间（三轴独立计算）"),
    KNUTSEN("伊林-努特生公式", "T60 = 0.161V/[-S·ln(1-ᾱ)+4mV]，考虑高频空气吸声（2000Hz以上显著）")
}

/**
 * Result of reverberation time calculation.
 */
data class ReverberationResult(
    val formula: ReverberationFormula,
    val rt60ByBand: Map<FrequencyBand, Double>,
    val totalAbsorptionByBand: Map<FrequencyBand, Double>,
    val meanAbsorptionByBand: Map<FrequencyBand, Double>,
    val airAbsorptionUsed: Boolean = false
) {
    /** Mid-frequency RT60 (average of 500Hz and 1000Hz) */
    val rt60Mid: Double by lazy {
        val v500 = rt60ByBand[FrequencyBand.BAND_500] ?: 0.0
        val v1000 = rt60ByBand[FrequencyBand.BAND_1000] ?: 0.0
        if (v500.isInfinite() || v1000.isInfinite()) Double.POSITIVE_INFINITY
        else (v500 + v1000) / 2.0
    }

    /** Speech-frequency RT60 (average of 500, 1000, 2000 Hz) */
    val rt60Speech: Double by lazy {
        FrequencyBand.SPEECH_BANDS.map { rt60ByBand[it] ?: Double.POSITIVE_INFINITY }
            .let { if (it.any { v -> v.isInfinite() }) Double.POSITIVE_INFINITY else it.average() }
    }

    /** Check compliance against a standard's optimal RT60 range */
    fun checkCompliance(optimalRange: ClosedFloatingPointRange<Double>): Boolean =
        !rt60Mid.isInfinite() && rt60Mid in optimalRange

    fun checkCompliance(roomType: StandardsConstants.RoomType): Boolean {
        val range = StandardsConstants.OPTIMAL_RT60_MID[roomType] ?: return true
        return checkCompliance(range)
    }
}

/**
 * Clarity index results: C50 (speech) and C80 (music).
 */
data class ClarityResult(
    val c50ByBand: Map<FrequencyBand, Double>,
    val c80ByBand: Map<FrequencyBand, Double>
) {
    val c50Avg: Double by lazy {
        FrequencyBand.SPEECH_BANDS.map { c50ByBand[it] ?: Double.NaN }.average()
    }
    val c80Avg: Double by lazy {
        FrequencyBand.SPEECH_BANDS.map { c80ByBand[it] ?: Double.NaN }.average()
    }

    /** C50 interpretation */
    val c50Quality: String by lazy {
        when {
            c50Avg > 3.0 -> "优秀 (Excellent)"
            c50Avg > 0.0 -> "良好 (Good)"
            c50Avg > -3.0 -> "一般 (Fair)"
            else -> "较差 (Poor)"
        }
    }

    /** C80 interpretation */
    val c80Quality: String by lazy {
        when {
            c80Avg > 5.0 -> "优秀 (Excellent)"
            c80Avg > 2.0 -> "良好 (Good)"
            c80Avg > -1.0 -> "一般 (Fair)"
            else -> "较差 (Poor)"
        }
    }
}

/**
 * STIPA (Speech Transmission Index for Public Address) result.
 */
data class StipaResult(
    val stipa: Double,
    val c50Avg: Double
) {
    val grade: String by lazy {
        when {
            stipa >= 0.75 -> "A+ (极佳)"
            stipa >= 0.66 -> "A (良好)"
            stipa >= 0.60 -> "B (合格)"
            stipa >= 0.50 -> "C (一般)"
            stipa >= 0.40 -> "D (较差)"
            else -> "E (很差)"
        }
    }
}

/**
 * Bass ratio result.
 * LF = (RT125 + RT250) / (RT500 + RT1000)
 */
data class BassRatioResult(
    val bassRatio: Double,
    val rt125: Double,
    val rt250: Double,
    val rt500: Double,
    val rt1000: Double
) {
    /** Interpretation: warmth of the room */
    val interpretation: String by lazy {
        when {
            bassRatio > 1.3 -> "温暖/丰满 (Warm/Full) — 低频混响较多"
            bassRatio > 1.1 -> "适中偏暖 (Slightly Warm)"
            bassRatio >= 0.9 -> "均衡 (Balanced)"
            bassRatio >= 0.7 -> "适中偏亮 (Slightly Bright)"
            else -> "明亮 (Bright) — 低频混响较少"
        }
    }
}

/**
 * Strength factor G result.
 * G = 10 * log10(I_room / I_free_field)
 */
data class StrengthFactorResult(
    val g: Double, // dB
    val roomIntensity: Double,
    val freeFieldIntensity: Double
) {
    val interpretation: String by lazy {
        when {
            g > 6.0 -> "很强 — 适合浪漫派音乐"
            g > 3.0 -> "较强 — 适合室内乐/演讲"
            g > 0.0 -> "适中 — 适合多种用途"
            g > -3.0 -> "较弱 — 可能缺乏响度"
            else -> "很弱 — 需要电声增强"
        }
    }
}
