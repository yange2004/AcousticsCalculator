package com.acoustics.calculator.domain.engine

import com.acoustics.calculator.domain.model.NoisePredictionResult
import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.log10
import kotlin.math.pow

/**
 * Indoor noise level prediction engine.
 *
 * Steady-state SPL in a room:
 * Lp = Lw + 10·lg( Q/(4πr²) + 4/R )
 *
 * Where:
 * - Lp: Sound pressure level at receiver (dB)
 * - Lw: Sound power level of source (dB re 1pW)
 * - Q: Directivity factor (1=omnidirectional, 2=floor, 4=edge, 8=corner)
 * - r: Distance from source to receiver (m)
 * - R: Room constant R = S·ᾱ/(1-ᾱ) (m²), NOT total absorption A
 *
 * Critical distance (混响半径): rc = √(Q·R/16π) ≈ 0.14·√(Q·R)
 * - Inside rc: direct field dominates (吸声降噪效果不明显)
 * - Outside rc: reverberant field dominates (吸声降噪显著)
 *
 * Outdoor attenuation:
 * - Point source: -6 dB per distance doubling
 * - Line source: -3 dB per distance doubling
 * - Plane source: no attenuation
 */
class IndoorNoiseEngine @Inject constructor() {

    /**
     * Calculate indoor sound pressure level from a point source.
     */
    fun calculate(
        sourceLevelDb: Double,       // Lw in dB
        distanceM: Double,            // r in meters
        totalAbsorptionM2: Double,    // A in m²
        directivityFactorQ: Double = 2.0,
        roomVolumeM3: Double = 0.0,
        sourceType: String = ""
    ): NoisePredictionResult {
        val A = totalAbsorptionM2.coerceAtLeast(0.01)

        // Direct field: Q / (4πr²)
        val directTerm = directivityFactorQ / (4.0 * PI * distanceM * distanceM)
        // Reverberant field: 4 / A
        val reverberantTerm = 4.0 / A

        val indoorLevel = sourceLevelDb + 10.0 * log10(directTerm + reverberantTerm)

        return NoisePredictionResult(
            sourceLevelDb = sourceLevelDb,
            distanceM = distanceM,
            roomVolumeM3 = roomVolumeM3,
            totalAbsorptionM2 = A,
            directivityFactorQ = directivityFactorQ,
            indoorLevelDb = indoorLevel,
            sourceType = sourceType
        )
    }

    /**
     * Calculate noise level at multiple receiver positions.
     */
    fun calculateMultipleReceivers(
        sourceLevelDb: Double,
        distances: List<Double>,
        totalAbsorptionM2: Double,
        directivityFactorQ: Double = 2.0
    ): List<NoisePredictionResult> {
        return distances.map { distance ->
            calculate(sourceLevelDb, distance, totalAbsorptionM2, directivityFactorQ)
        }
    }

    /**
     * Calculate required absorption to achieve target noise level.
     *
     * From Lp = Lw + 10*lg(Q/4πr² + 4/A), solve for A:
     * A = 4 / (10^((Lp - Lw)/10) - Q/4πr²)
     */
    fun requiredAbsorption(
        sourceLevelDb: Double,
        targetLevelDb: Double,
        distanceM: Double,
        directivityFactorQ: Double = 2.0
    ): Double {
        val directTerm = directivityFactorQ / (4.0 * PI * distanceM * distanceM)
        val requiredTotal = 10.0.pow((targetLevelDb - sourceLevelDb) / 10.0)
        val revNeeded = requiredTotal - directTerm
        if (revNeeded <= 0.0) return Double.POSITIVE_INFINITY // Direct field already exceeds target
        return 4.0 / revNeeded
    }

    /**
     * Calculate critical distance (where direct = reverberant field).
     * rc = sqrt(Q * A / (16 * π))
     */
    fun criticalDistance(totalAbsorptionM2: Double, directivityFactorQ: Double = 1.0): Double {
        if (totalAbsorptionM2 <= 0.0) return Double.POSITIVE_INFINITY
        return kotlin.math.sqrt(directivityFactorQ * totalAbsorptionM2 / (16.0 * PI))
    }

    /**
     * Common directivity factor Q values
     */
    enum class SourcePosition(val q: Double, val label: String) {
        FREE_FIELD(1.0, "自由空间中央"),
        ON_FLOOR(2.0, "地面中央"),
        ON_FLOOR_NEAR_WALL(4.0, "地面靠墙"),
        IN_CORNER(8.0, "墙角")
    }
}
