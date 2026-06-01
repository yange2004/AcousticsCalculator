package com.acoustics.calculator.domain.engine

import com.acoustics.calculator.core.constants.FrequencyBand
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Room mode / standing wave calculation engine.
 *
 * Based on Chapter 1 of 实用建筑声学 — Modal Analysis
 *
 * Room modes occur when room dimensions are integer multiples of half-wavelengths.
 * f = c/2 × √[(nx/Lx)² + (ny/Ly)² + (nz/Lz)²]
 */
class RoomModeEngine @Inject constructor() {

    companion object {
        private const val SPEED_OF_SOUND = 343.0 // m/s
        private const val MAX_MODES = 20
    }

    data class RoomMode(
        val nx: Int, val ny: Int, val nz: Int,
        val frequencyHz: Double,
        val type: ModeType
    ) {
        val label: String get() = "($nx, $ny, $nz)"
        val nonZeroCount: Int get() = listOf(nx > 0, ny > 0, nz > 0).count { it }
        val axial: Boolean get() = nonZeroCount == 1
        val tangential: Boolean get() = nonZeroCount == 2
        val oblique: Boolean get() = nonZeroCount == 3
    }

    enum class ModeType {
        AXIAL,      // Only one dimension non-zero (strongest)
        TANGENTIAL, // Two dimensions non-zero
        OBLIQUE     // Three dimensions non-zero (weakest)
    }

    data class RoomModeResult(
        val widthM: Double,
        val lengthM: Double,
        val heightM: Double,
        val volumeM3: Double,
        val allModes: List<RoomMode>,
        val axialModes: List<RoomMode>,
        val tangentialModes: List<RoomMode>,
        val obliqueModes: List<RoomMode>,
        val schroederFrequency: Double,
        val modeDensity: Double,
        val problematicModes: List<RoomMode>, // Modes below Schroeder
        val goodRatio: Boolean // True if room ratios avoid mode clustering
    )

    /**
     * Calculate all room modes up to a given frequency.
     */
    fun calculate(
        widthM: Double,
        lengthM: Double,
        heightM: Double,
        maxFrequencyHz: Double = 300.0
    ): RoomModeResult {
        val modes = mutableListOf<RoomMode>()

        // n_max per dimension
        val nxMax = (maxFrequencyHz * 2.0 * widthM / SPEED_OF_SOUND).toInt() + 1
        val nyMax = (maxFrequencyHz * 2.0 * lengthM / SPEED_OF_SOUND).toInt() + 1
        val nzMax = (maxFrequencyHz * 2.0 * heightM / SPEED_OF_SOUND).toInt() + 1

        for (nx in 0..nxMax) {
            for (ny in 0..nyMax) {
                for (nz in 0..nzMax) {
                    if (nx == 0 && ny == 0 && nz == 0) continue

                    val f = calculateFrequency(widthM, lengthM, heightM, nx, ny, nz)
                    if (f > maxFrequencyHz) continue

                    val nonZero = listOf(nx > 0, ny > 0, nz > 0).count { it }
                    val type = when (nonZero) {
                        1 -> ModeType.AXIAL
                        2 -> ModeType.TANGENTIAL
                        else -> ModeType.OBLIQUE
                    }

                    modes.add(RoomMode(nx, ny, nz, f, type))
                }
            }
        }

        modes.sortBy { it.frequencyHz }

        val V = widthM * lengthM * heightM
        val axial = modes.filter { it.axial }
        val tangential = modes.filter { it.tangential }
        val oblique = modes.filter { it.oblique }

        // Schroeder frequency — above this, modes overlap enough for statistical treatment
        val schroeder = 2000.0 * sqrt(calculateMeanRt60Approx(V) / V)
        // Mode density per Hz
        val modeDensity = calculateModeDensity(V, 500.0)

        // Problematic modes: below Schroeder frequency
        val problematic = modes.filter { it.frequencyHz <= schroeder }

        // Check if room ratios are good (avoid integer ratios)
        val ratios = doubleArrayOf(
            maxOf(widthM, lengthM, heightM) / minOf(widthM, lengthM, heightM),
            sortedDimensions(widthM, lengthM, heightM).let {
                it[1] / it[0]
            }
        )
        val goodRatio = !hasIntegerRatio(widthM, lengthM, heightM)

        return RoomModeResult(
            widthM = widthM, lengthM = lengthM, heightM = heightM,
            volumeM3 = V,
            allModes = modes.take(MAX_MODES),
            axialModes = axial.take(MAX_MODES),
            tangentialModes = tangential.take(MAX_MODES),
            obliqueModes = oblique.take(MAX_MODES),
            schroederFrequency = schroeder,
            modeDensity = modeDensity,
            problematicModes = problematic,
            goodRatio = goodRatio
        )
    }

    /**
     * f = c/2 × √[(nx/Lx)² + (ny/Ly)² + (nz/Lz)²]
     */
    fun calculateFrequency(
        widthM: Double, lengthM: Double, heightM: Double,
        nx: Int, ny: Int, nz: Int
    ): Double {
        return SPEED_OF_SOUND / 2.0 * sqrt(
            (nx.toDouble() / widthM).let { it * it } +
            (ny.toDouble() / lengthM).let { it * it } +
            (nz.toDouble() / heightM).let { it * it }
        )
    }

    /**
     * Mode density at frequency f:
     * dN/df = 4πV·f²/c³ + πS·f/(2c²) + L/(8c)
     */
    fun calculateModeDensity(V: Double, f: Double, S: Double = 0.0, L: Double = 0.0): Double {
        return 4.0 * Math.PI * V * f * f / (SPEED_OF_SOUND * SPEED_OF_SOUND * SPEED_OF_SOUND) +
               Math.PI * S * f / (2.0 * SPEED_OF_SOUND * SPEED_OF_SOUND) +
               L / (8.0 * SPEED_OF_SOUND)
    }

    /**
     * Approximate mean RT60 for mode density calculation.
     */
    private fun calculateMeanRt60Approx(V: Double): Double {
        return when {
            V < 50 -> 0.4
            V < 200 -> 0.6
            V < 1000 -> 0.8
            V < 5000 -> 1.2
            else -> 1.5
        }
    }

    private fun sortedDimensions(vararg dims: Double): List<Double> = dims.sorted()

    /**
     * Recommended room dimension ratios to avoid mode clustering.
     */
    fun getRecommendedRatio(): Triple<Double, Double, Double> = Triple(1.0, 1.26, 1.59)

    /**
     * Evaluate how good the room ratios are (1.0 best, 0.0 worst).
     */
    fun evaluateRatioScore(widthM: Double, lengthM: Double, heightM: Double): Double {
        val dims = sortedDimensions(widthM, lengthM, heightM)
        val ideal = listOf(1.0, 1.26, 1.59)
        val ratios = listOf(dims[1] / dims[0], dims[2] / dims[0])

        // Compare to ideal 1:1.26:1.59
        val score1 = 1.0 - minOf(1.0, abs(ratios[0] - ideal[1]) / 0.5)
        val score2 = 1.0 - minOf(1.0, abs(ratios[1] - ideal[2]) / 0.5)

        // Penalty for integer ratios
        val penalty = if (hasIntegerRatio(widthM, lengthM, heightM)) 0.3 else 0.0

        return maxOf(0.0, (score1 + score2) / 2.0 - penalty)
    }

    private fun hasIntegerRatio(vararg dims: Double): Boolean {
        val ratios = sortedDimensions(*dims).let {
            listOf(it[1] / it[0], it[2] / it[0], it[2] / it[1])
        }
        return ratios.any { r ->
            val rounded = kotlin.math.round(r)
            kotlin.math.abs(r - rounded) < 0.05 && rounded <= 3
        }
    }

    /**
     * Best room ratios from the book / literature:
     * - 1:1.14:1.39 (Bolt area)
     * - 1:1.26:1.59 (Louden)
     * - 1:1.45:2.10 (Sepmeyer)
     * - 1:1.28:1.54 (Volkmann)
     */
    fun bestRatios(): List<Triple<Double, Double, Double>> = listOf(
        Triple(1.0, 1.14, 1.39),
        Triple(1.0, 1.26, 1.59),
        Triple(1.0, 1.45, 2.10),
        Triple(1.0, 1.28, 1.54)
    )
}
