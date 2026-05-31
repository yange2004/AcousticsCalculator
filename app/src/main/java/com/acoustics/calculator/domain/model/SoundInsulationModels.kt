package com.acoustics.calculator.domain.model

import com.acoustics.calculator.core.constants.FrequencyBand

/**
 * A layer in a composite wall/floor/ceiling construction.
 */
data class WallLayer(
    val name: String,
    val material: String,
    val thicknessMm: Double,
    val densityKgm3: Double
) {
    /** Mass per unit area (kg/m²) */
    val massPerUnitAreaKgm2: Double get() = densityKgm3 * thicknessMm / 1000.0
}

/**
 * Result of sound insulation calculation.
 */
data class SoundInsulationResult(
    val layers: List<WallLayer>,
    val totalMassPerUnitAreaKgm2: Double,
    val transmissionLossByBand: Map<FrequencyBand, Double>,
    val rw: Double,       // Weighted sound reduction index
    val stc: Double,      // Sound Transmission Class
    val isComposite: Boolean = false,
    val cavityDepthMm: Double? = null,
    val insulationMaterial: String? = null
) {
    /** STC rating interpretation */
    val stcRating: String by lazy {
        when {
            stc >= 60 -> "极佳 — 大部分声音听不到"
            stc >= 55 -> "很好 — 大声说话听不到"
            stc >= 50 -> "好 — 正常说话听不到"
            stc >= 45 -> "较好 — 可听到大声说话"
            stc >= 40 -> "一般 — 可听到正常说话"
            stc >= 35 -> "较差 — 可听到小声说话"
            else -> "差 — 大部分声音可听到"
        }
    }
}

/**
 * Result of indoor noise level prediction.
 */
data class NoisePredictionResult(
    val sourceLevelDb: Double,         // Source sound power level Lw
    val distanceM: Double,             // Distance from source
    val roomVolumeM3: Double,          // Room volume
    val totalAbsorptionM2: Double,     // Total absorption A
    val directivityFactorQ: Double = 2.0, // Q factor
    val indoorLevelDb: Double,         // Resulting indoor SPL
    val sourceType: String = ""
) {
    /** Contribution breakdown */
    val directFieldDb: Double by lazy {
        sourceLevelDb + 10.0 * kotlin.math.log10(directivityFactorQ / (4.0 * kotlin.math.PI * distanceM * distanceM))
    }
    val reverberantFieldDb: Double by lazy {
        sourceLevelDb + 10.0 * kotlin.math.log10(4.0 / totalAbsorptionM2.coerceAtLeast(0.01))
    }
}

/**
 * Result of sound barrier insertion loss calculation.
 */
data class BarrierResult(
    val insertionLossDb: Double,
    val fresnelNumber: Double,
    val pathDifferenceM: Double,
    val frequencyHz: Double
) {
    val interpretation: String by lazy {
        when {
            insertionLossDb > 20.0 -> "优秀 — 降噪效果显著"
            insertionLossDb > 15.0 -> "良好 — 降噪效果明显"
            insertionLossDb > 10.0 -> "一般 — 有一定降噪效果"
            insertionLossDb > 5.0 -> "有限 — 降噪效果有限"
            else -> "不足 — 屏障高度/位置需调整"
        }
    }

    companion object {
        /** Simplified barrier IL: IL = 10 * log10(3 / (3 + 20*N)) */
        fun simplified(fresnelN: Double): Double =
            10.0 * kotlin.math.log10(3.0 / (3.0 + 20.0 * fresnelN.coerceAtLeast(0.0)))
    }
}

/**
 * Standard information for compliance checking.
 */
data class StandardInfo(
    val id: Long,
    val standardCode: String,
    val nameZh: String,
    val category: String,
    val roomType: String,
    val optimalRt60Min: Double?,
    val optimalRt60Max: Double?,
    val maxNoiseLevelDb: Double?,
    val minStc: Double?,
    val notes: String
)
