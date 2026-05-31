package com.acoustics.calculator.domain.engine

import com.acoustics.calculator.core.constants.FrequencyBand
import com.acoustics.calculator.domain.model.StipaResult
import javax.inject.Inject
import kotlin.math.pow

/**
 * STIPA (Speech Transmission Index for Public Address) engine.
 *
 * Simplified engineering estimate:
 * STIPA ≈ 0.012 × C50 + 0.55
 *
 * This is a practical approximation used in the field.
 * Full STIPA measurement requires MTF (Modulation Transfer Function) analysis.
 */
class StipaEngine @Inject constructor() {

    /**
     * Calculate STIPA from C50 average value.
     */
    fun calculate(c50Avg: Double): StipaResult {
        val stipa = (0.012 * c50Avg + 0.55).coerceIn(0.0, 1.0)
        return StipaResult(stipa = stipa, c50Avg = c50Avg)
    }

    /**
     * Calculate STIPA from RT60 and background noise (alternative method).
     * STIPA ≈ f(RT60, SNR) — simplified model
     */
    fun calculateFromRt60(
        rt60Speech: Double,
        signalToNoiseRatioDb: Double = 30.0
    ): Double {
        // MTI (Modulation Transfer Index) approximation per octave band
        // m(F) ≈ 1 / sqrt(1 + (2π * F * RT60 / 13.8)²) * SNR_factor
        // For STIPA, we use the 500Hz and 2000Hz bands as representative
        val mti500 = modulationTransferIndex(500.0, rt60Speech, signalToNoiseRatioDb)
        val mti2000 = modulationTransferIndex(2000.0, rt60Speech, signalToNoiseRatioDb)

        // Average MTI then convert to STI
        val avgMti = (mti500 + mti2000) / 2.0
        return (avgMti / 0.55).coerceIn(0.0, 1.0) // Normalize
    }

    private fun modulationTransferIndex(
        modulationFreqHz: Double,
        rt60: Double,
        snrDb: Double
    ): Double {
        if (rt60 <= 0.0) return 1.0
        // MTF from reverberation
        val mtfRev = 1.0 / kotlin.math.sqrt(1.0 + kotlin.math.PI * modulationFreqHz * rt60 / 13.8)
        // MTF from noise
        val mtfNoise = 1.0 / (1.0 + 10.0.pow(-snrDb / 10.0))
        return mtfRev * mtfNoise
    }
}
