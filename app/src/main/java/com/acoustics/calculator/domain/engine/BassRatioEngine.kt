package com.acoustics.calculator.domain.engine

import com.acoustics.calculator.core.constants.FrequencyBand
import com.acoustics.calculator.domain.model.BassRatioResult
import javax.inject.Inject

/**
 * Bass ratio calculation engine.
 *
 * LF = (RT125 + RT250) / (RT500 + RT1000)
 *
 * Indicates the warmth of a room's acoustics.
 * LF > 1.1: warm (good for music)
 * LF < 0.9: bright (good for speech)
 */
class BassRatioEngine @Inject constructor() {

    fun calculate(rt60ByBand: Map<FrequencyBand, Double>): BassRatioResult {
        val rt125 = rt60ByBand[FrequencyBand.BAND_125] ?: 0.0
        val rt250 = rt60ByBand[FrequencyBand.BAND_250] ?: 0.0
        val rt500 = rt60ByBand[FrequencyBand.BAND_500] ?: 0.0
        val rt1000 = rt60ByBand[FrequencyBand.BAND_1000] ?: 0.0

        val numerator = rt125 + rt250
        val denominator = rt500 + rt1000

        val bassRatio = if (denominator > 0.0) numerator / denominator else 1.0

        return BassRatioResult(
            bassRatio = bassRatio,
            rt125 = rt125,
            rt250 = rt250,
            rt500 = rt500,
            rt1000 = rt1000
        )
    }
}
