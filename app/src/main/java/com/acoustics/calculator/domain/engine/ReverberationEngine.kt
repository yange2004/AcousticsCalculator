package com.acoustics.calculator.domain.engine

import com.acoustics.calculator.core.constants.FrequencyBand
import com.acoustics.calculator.core.constants.PhysicalConstants
import com.acoustics.calculator.domain.helper.AbsorptionCalculator
import com.acoustics.calculator.domain.model.ReverberationFormula
import com.acoustics.calculator.domain.model.ReverberationResult
import com.acoustics.calculator.domain.model.RoomDimensions
import com.acoustics.calculator.domain.model.SurfaceComposition
import javax.inject.Inject
import kotlin.math.ln

/**
 * Reverberation time calculation engine.
 * Implements Sabine, Eyring, Fitzroy, and Eyring-Knutsen formulas.
 *
 * All formulas produce RT60 in seconds per octave band.
 *
 * Formula references (2026 research update):
 * - Sabine:    T60 = 0.161V/A, valid for ᾱ < 0.2
 * - Eyring:    T60 = 0.161V/[-S·ln(1-ᾱ)], valid for ᾱ > 0.2
 * - Fitzroy:   for non-uniform absorption distribution (3-axis)
 * - Knutsen:   T60 = 0.161V/[-S·ln(1-ᾱ) + 4mV], accounts for air absorption at HF
 */
class ReverberationEngine @Inject constructor() {

    /**
     * Calculate RT60 for all octave bands using the selected formula.
     *
     * @param room Room dimensions
     * @param composition Surface material assignments
     * @param formula The reverberation formula to use
     * @param useAirAbsorption Include air absorption (important for large rooms / high frequencies)
     * @return ReverberationResult with per-band RT60, absorption, and mean absorption
     */
    fun calculate(
        room: RoomDimensions,
        composition: SurfaceComposition,
        formula: ReverberationFormula,
        useAirAbsorption: Boolean = false
    ): ReverberationResult {
        val totalAbsorption = AbsorptionCalculator.computeTotalAbsorption(room, composition)
        val meanAbsorption = AbsorptionCalculator.computeMeanAbsorption(room, totalAbsorption)

        val airAbsorption = if (useAirAbsorption) {
            PhysicalConstants.AIR_ABSORPTION_COEFFICIENT
        } else {
            FrequencyBand.OCTAVE_BANDS.associateWith { 0.0 }
        }

        val V = room.volumeM3

        val rt60ByBand = FrequencyBand.OCTAVE_BANDS.associateWith { band ->
            val A = totalAbsorption[band] ?: 0.0
            val aBar = meanAbsorption[band] ?: 0.0
            val m = airAbsorption[band] ?: 0.0

            when (formula) {
                ReverberationFormula.SABINE -> sabine(V, A, m)
                ReverberationFormula.EYRING -> eyring(V, room.totalSurfaceAreaM2, aBar, m)
                ReverberationFormula.FITZROY -> fitzroy(V, room, composition, band, m)
                ReverberationFormula.KNUTSEN -> knutsen(V, room.totalSurfaceAreaM2, A, aBar, m)
            }
        }

        return ReverberationResult(
            formula = formula,
            rt60ByBand = rt60ByBand,
            totalAbsorptionByBand = totalAbsorption,
            meanAbsorptionByBand = meanAbsorption,
            airAbsorptionUsed = useAirAbsorption
        )
    }

    /**
     * Sabine formula: RT60 = 0.161 * V / A
     * Extended with air absorption: RT60 = 0.161 * V / (A + 4mV)
     *
     * Applicable when ᾱ < 0.2 (live rooms)
     */
    fun sabine(V: Double, A: Double, m: Double = 0.0): Double {
        if (A + 4.0 * m * V <= 0.0) return Double.POSITIVE_INFINITY
        return 0.161 * V / (A + 4.0 * m * V)
    }

    /**
     * Eyring formula: RT60 = 0.161 * V / (-S * ln(1 - ᾱ) + 4mV)
     *
     * Applicable when ᾱ > 0.2 (dead rooms). Reduces to Sabine when ᾱ → 0.
     */
    fun eyring(V: Double, S: Double, aBar: Double, m: Double = 0.0): Double {
        if (aBar >= 1.0) return 0.0 // Fully absorptive
        if (aBar <= 0.0) return sabine(V, 0.0, m) // No absorption → infinite RT

        val denominator = -S * ln(1.0 - aBar) + 4.0 * m * V
        if (denominator <= 0.0) return Double.POSITIVE_INFINITY
        return 0.161 * V / denominator
    }

    /**
     * Fitzroy formula for non-uniform absorption distribution:
     * RT60 = 0.161V / (-Sx*ln(1-αx) - Sy*ln(1-αy) - Sz*ln(1-αz) + 4mV)
     *
     * Where Sx, Sy, Sz are surface areas of opposite wall pairs,
     * and αx, αy, αz are the mean absorption of each pair.
     */
    fun fitzroy(
        V: Double,
        room: RoomDimensions,
        composition: SurfaceComposition,
        band: FrequencyBand,
        m: Double = 0.0
    ): Double {
        val (ax, ay, az) = AbsorptionCalculator.computeFitzroyAxisAbsorption(room, composition, band)

        val termX = if (ax < 1.0) -room.sx * ln(1.0 - ax.coerceAtMost(0.999)) else Double.POSITIVE_INFINITY
        val termY = if (ay < 1.0) -room.sy * ln(1.0 - ay.coerceAtMost(0.999)) else Double.POSITIVE_INFINITY
        val termZ = if (az < 1.0) -room.sz * ln(1.0 - az.coerceAtMost(0.999)) else Double.POSITIVE_INFINITY

        val denominator = termX + termY + termZ + 4.0 * m * V
        if (denominator <= 0.0 || denominator.isInfinite()) return Double.POSITIVE_INFINITY
        return 0.161 * V / denominator
    }

    /**
     * Knutsen formula (modified Eyring for complex shapes):
     * RT60 = 0.161 * V / (A + 4mV)
     * This is essentially identical to Sabine with explicit air absorption term.
     * Some texts present Knutsen as: RT60 = 0.161*V / (-S*ln(1-ᾱ) + 4mV)
     *
     * We use the common form: RT60 = 0.161V / (-S*ln(1-ᾱ) + 4mV)
     * which is similar to Eyring but with emphasis on air absorption.
     */
    fun knutsen(V: Double, S: Double, A: Double, aBar: Double, m: Double = 0.0): Double {
        if (aBar >= 1.0) return 0.0
        val denominator = -S * ln(1.0 - aBar.coerceAtMost(0.999)) + 4.0 * m * V
        if (denominator <= 0.0) return Double.POSITIVE_INFINITY
        return 0.161 * V / denominator
    }
}
