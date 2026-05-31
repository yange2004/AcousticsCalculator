package com.acoustics.calculator.domain.engine

import com.acoustics.calculator.core.constants.FrequencyBand
import com.acoustics.calculator.domain.model.ClarityResult
import javax.inject.Inject
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10

/**
 * Clarity index calculation engine.
 *
 * C50 = 10 * lg(∫₀⁵⁰ms p²(t)dt / ∫₅₀∞ms p²(t)dt)  (speech)
 * C80 = 10 * lg(∫₀⁸⁰ms p²(t)dt / ∫₈₀∞ms p²(t)dt)  (music)
 *
 * Approximated from RT60 using the relationship:
 * C50 ≈ 10 * lg((1 - e^(-0.69/RT)) / e^(-0.69/RT))
 * C80 ≈ 10 * lg((1 - e^(-1.104/RT)) / e^(-1.104/RT))
 */
class ClarityEngine @Inject constructor() {

    companion object {
        /** Time constant for C50: 0.69 = ln(2) ≈ ln(1000/50) simplified */
        private const val TC_C50 = 0.69
        /** Time constant for C80: 1.104 ≈ ln(1000/80) simplified from early-to-late energy ratio */
        private const val TC_C80 = 1.104
    }

    /**
     * Calculate C50 and C80 from RT60 values per band.
     */
    fun calculate(rt60ByBand: Map<FrequencyBand, Double>): ClarityResult {
        val c50 = rt60ByBand.mapValues { (_, rt) -> c50FromRt60(rt) }
        val c80 = rt60ByBand.mapValues { (_, rt) -> c80FromRt60(rt) }
        return ClarityResult(c50ByBand = c50, c80ByBand = c80)
    }

    /**
     * Calculate C50 from a single RT60 value.
     * C50 = 10 * lg(exp(0.69/RT) - 1)
     */
    fun c50FromRt60(rt60: Double): Double {
        if (rt60 <= 0.0 || rt60.isInfinite()) return Double.NEGATIVE_INFINITY
        val ratio = exp(TC_C50 / rt60) - 1.0
        if (ratio <= 0.0) return Double.NEGATIVE_INFINITY
        return 10.0 * log10(ratio)
    }

    /**
     * Calculate C80 from a single RT60 value.
     * C80 = 10 * lg(exp(1.104/RT) - 1)
     */
    fun c80FromRt60(rt60: Double): Double {
        if (rt60 <= 0.0 || rt60.isInfinite()) return Double.NEGATIVE_INFINITY
        val ratio = exp(TC_C80 / rt60) - 1.0
        if (ratio <= 0.0) return Double.NEGATIVE_INFINITY
        return 10.0 * log10(ratio)
    }
}
