package com.acoustics.calculator.domain.engine

import com.acoustics.calculator.core.constants.FrequencyBand
import com.acoustics.calculator.core.constants.PhysicalConstants
import com.acoustics.calculator.domain.model.BarrierResult
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Sound barrier insertion loss calculation engine.
 *
 * IL = 10 * lg(3 / (3 + 20*N))  (simplified formula)
 *
 * Where N is the Fresnel number:
 * N = 2 * δ / λ
 * δ = (a + b) - d  (path difference)
 * a+b = diffracted path over barrier
 * d = direct path without barrier
 */
class BarrierInsertionLossEngine @Inject constructor() {

    /**
     * Calculate barrier insertion loss for a specific frequency.
     */
    fun calculate(
        sourceHeightM: Double,
        receiverHeightM: Double,
        barrierHeightM: Double,
        sourceBarrierDistanceM: Double,
        receiverBarrierDistanceM: Double,
        frequencyHz: Double,
        speedOfSound: Double = PhysicalConstants.SPEED_OF_SOUND
    ): BarrierResult {
        val pathDiff = calculatePathDifference(
            sourceHeightM, receiverHeightM, barrierHeightM,
            sourceBarrierDistanceM, receiverBarrierDistanceM
        )

        val wavelength = speedOfSound / frequencyHz
        val fresnelN = if (wavelength > 0.0) 2.0 * pathDiff / wavelength else 0.0

        val il = calculateInsertionLoss(fresnelN)

        return BarrierResult(
            insertionLossDb = il,
            fresnelNumber = fresnelN,
            pathDifferenceM = pathDiff,
            frequencyHz = frequencyHz
        )
    }

    /**
     * Calculate insertion loss across all octave bands.
     */
    fun calculatePerBand(
        sourceHeightM: Double,
        receiverHeightM: Double,
        barrierHeightM: Double,
        sourceBarrierDistanceM: Double,
        receiverBarrierDistanceM: Double
    ): Map<FrequencyBand, BarrierResult> {
        return FrequencyBand.OCTAVE_BANDS.associateWith { band ->
            calculate(
                sourceHeightM, receiverHeightM, barrierHeightM,
                sourceBarrierDistanceM, receiverBarrierDistanceM,
                band.hz.toDouble()
            )
        }
    }

    /**
     * Calculate path difference: δ = (a + b) - d
     */
    fun calculatePathDifference(
        sourceHeightM: Double,
        receiverHeightM: Double,
        barrierHeightM: Double,
        sourceBarrierDistanceM: Double,
        receiverBarrierDistanceM: Double
    ): Double {
        // Diffracted path: a + b
        val a = sqrt(
            (barrierHeightM - sourceHeightM) * (barrierHeightM - sourceHeightM) +
            sourceBarrierDistanceM * sourceBarrierDistanceM
        )
        val b = sqrt(
            (barrierHeightM - receiverHeightM) * (barrierHeightM - receiverHeightM) +
            receiverBarrierDistanceM * receiverBarrierDistanceM
        )

        // Direct path: d
        val d = sqrt(
            (sourceHeightM - receiverHeightM) * (sourceHeightM - receiverHeightM) +
            (sourceBarrierDistanceM + receiverBarrierDistanceM) * (sourceBarrierDistanceM + receiverBarrierDistanceM)
        )

        return (a + b) - d
    }

    /**
     * Simplified insertion loss formula:
     * IL = 10 * lg(3 / (3 + 20*N))
     *
     * Valid for N >= -0.1 (line-of-sight not significantly broken for N < -0.3)
     */
    fun calculateInsertionLoss(fresnelNumber: Double): Double {
        if (fresnelNumber < -0.3) return 0.0 // Line of sight
        val n = fresnelNumber.coerceAtLeast(0.0)
        return 10.0 * kotlin.math.log10(3.0 / (3.0 + 20.0 * n))
    }

    /**
     * Maekawa formula (more accurate for engineering):
     * IL = 10 * lg(1 + 20*N)
     *
     * Valid for N > -0.3
     */
    fun calculateInsertionLossMaekawa(fresnelNumber: Double): Double {
        if (fresnelNumber < -0.3) return 0.0
        val n = fresnelNumber.coerceAtLeast(0.0)
        return 10.0 * kotlin.math.log10(1.0 + 20.0 * n)
    }

    /**
     * Kurze-Anderson formula (more accurate for all N):
     * IL = 5 + 20 * lg(sqrt(2*π*N) / tanh(sqrt(2*π*N)))
     */
    fun calculateInsertionLossKurze(fresnelNumber: Double): Double {
        if (fresnelNumber < -0.3) return 0.0
        val n = fresnelNumber.coerceAtLeast(0.001)
        val x = sqrt(2.0 * kotlin.math.PI * n)
        val tanhX = kotlin.math.tanh(x)
        if (tanhX <= 0.0) return 0.0
        return 5.0 + 20.0 * kotlin.math.log10(x / tanhX)
    }

    /**
     * Calculate required barrier height for target insertion loss at a given frequency.
     * Uses numerical approximation from the simplified formula.
     */
    fun requiredBarrierHeight(
        sourceHeightM: Double,
        receiverHeightM: Double,
        sourceBarrierDistanceM: Double,
        receiverBarrierDistanceM: Double,
        frequencyHz: Double,
        targetILDb: Double,
        speedOfSound: Double = PhysicalConstants.SPEED_OF_SOUND
    ): Double {
        // From IL = 10*lg(3/(3+20N)), solve for N:
        // N = 3*(10^(-IL/10) - 1) / 20
        val requiredN = 3.0 * (10.0.pow(-targetILDb / 10.0) - 1.0) / 20.0

        // From N = 2*δ/λ, solve for δ:
        val wavelength = speedOfSound / frequencyHz
        val requiredPathDiff = requiredN * wavelength / 2.0

        // Iterate barrier height to achieve required path difference
        // Simplified: start from source height and increment
        var h = sourceHeightM.coerceAtLeast(receiverHeightM)
        for (i in 1..1000) {
            val pathDiff = calculatePathDifference(
                sourceHeightM, receiverHeightM, h,
                sourceBarrierDistanceM, receiverBarrierDistanceM
            )
            if (pathDiff >= requiredPathDiff) return h
            h += 0.01
        }
        return h
    }
}
