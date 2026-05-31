package com.acoustics.calculator.domain.engine

import com.acoustics.calculator.core.constants.FrequencyBand
import com.acoustics.calculator.domain.model.SoundInsulationResult
import com.acoustics.calculator.domain.model.WallLayer
import javax.inject.Inject
import kotlin.math.log10
import kotlin.math.pow

/**
 * Wall Sound Transmission Class (STC) calculation engine.
 *
 * Implements (updated 2026 research):
 * 1. Mass law for single-leaf walls: R = 20*lg(m·f) - 47 (dB)
 *    - Field incidence form, m = surface density (kg/m²), f = frequency (Hz)
 *    - Mass doubling → +6 dB per octave
 * 2. Composite wall STC for multi-layer constructions
 * 3. Combined STC: τ̄ = Σ(Si·τi)/ΣSi → R̄ = 10·lg(1/τ̄)
 * 4. Double-leaf resonance frequency: f0 = (1/2π)·√(1.8ρc²/m·d)
 *
 * Reference: GB/T 50121-2005 建筑隔声评价标准
 */
class WallStcEngine @Inject constructor() {

    companion object {
        // Reference frequencies for STC calculation
        val STC_FREQUENCIES = listOf(125, 160, 200, 250, 315, 400, 500, 630,
            800, 1000, 1250, 1600, 2000, 2500, 3150, 4000)
    }

    /**
     * Mass law for a single homogeneous wall:
     * R = 20 * lg(m * f) - 47 (field incidence)
     * or R = 20 * lg(m * f) - 42 (normal incidence, laboratory)
     *
     * @param massPerUnitAreaKgm2 Mass per unit area (kg/m²)
     * @param frequencyHz Frequency in Hz
     * @return Transmission loss R in dB
     */
    fun massLawR(massPerUnitAreaKgm2: Double, frequencyHz: Double): Double {
        if (massPerUnitAreaKgm2 <= 0.0) return 0.0
        return 20.0 * log10(massPerUnitAreaKgm2 * frequencyHz) - 47.0
    }

    /**
     * Simplified STC estimate from mass law:
     * For m >= 150 kg/m²: STC ≈ 14.5 * lg(m) + 23
     * For m < 150 kg/m²: STC ≈ 20 * lg(m) + 10
     */
    fun singleLeafStc(massPerUnitAreaKgm2: Double): Double {
        if (massPerUnitAreaKgm2 <= 0.0) return 0.0
        return if (massPerUnitAreaKgm2 >= 150.0) {
            14.5 * log10(massPerUnitAreaKgm2) + 23.0
        } else {
            20.0 * log10(massPerUnitAreaKgm2) + 10.0
        }
    }

    /**
     * Calculate transmission loss for a single-layer wall across all octave bands.
     */
    fun singleLeafTransmissionLoss(massPerUnitAreaKgm2: Double): Map<FrequencyBand, Double> {
        return FrequencyBand.OCTAVE_BANDS.associateWith { band ->
            massLawR(massPerUnitAreaKgm2, band.hz.toDouble())
        }
    }

    /**
     * Calculate STC for a composite (multi-layer) wall.
     * Simplified method: sum mass per unit area, apply mass law.
     *
     * For double-leaf with cavity:
     * R_total ≈ R1 + R2 + ΔR_cavity
     * where ΔR_cavity depends on cavity depth and insulation fill.
     */
    fun compositeStc(
        layers: List<WallLayer>,
        cavityDepthMm: Double? = null,
        hasInsulationFill: Boolean = false
    ): SoundInsulationResult {
        if (layers.isEmpty()) {
            return SoundInsulationResult(
                layers = emptyList(),
                totalMassPerUnitAreaKgm2 = 0.0,
                transmissionLossByBand = emptyMap(),
                rw = 0.0,
                stc = 0.0
            )
        }

        val totalMass = layers.sumOf { it.massPerUnitAreaKgm2 }
        val tlByBand = FrequencyBand.OCTAVE_BANDS.associateWith { band ->
            val f = band.hz.toDouble()

            if (layers.size == 1 || cavityDepthMm == null) {
                // Single leaf or no cavity: use mass law
                massLawR(totalMass, f)
            } else {
                // Double leaf with cavity: mass-air-mass resonance
                val m1 = layers.first().massPerUnitAreaKgm2
                val m2 = layers.last().massPerUnitAreaKgm2
                val d = cavityDepthMm / 1000.0 // cavity depth in meters

                // Mass-air-mass resonance frequency
                val fres = 60.0 / kotlin.math.sqrt((m1 * m2 / (m1 + m2)) * d)

                val r1 = massLawR(m1, f)
                val r2 = massLawR(m2, f)

                if (f < fres) {
                    // Below resonance: behaves as single mass m1+m2
                    massLawR(totalMass, f)
                } else {
                    // Above resonance: double-leaf benefit
                    val cavityGain = if (hasInsulationFill) 6.0 else 3.0
                    r1 + r2 + cavityGain + 10.0 * log10(f / fres.coerceAtLeast(1.0))
                }
            }
        }

        // Estimate STC from the transmission loss curve
        // Simplified: STC ≈ average TL at 500Hz + correction
        val tl500 = tlByBand[FrequencyBand.BAND_500] ?: 30.0
        val stc = tl500 - 3.0 // Simplified approximation

        return SoundInsulationResult(
            layers = layers,
            totalMassPerUnitAreaKgm2 = totalMass,
            transmissionLossByBand = tlByBand,
            rw = stc, // Rw is close to STC for most constructions
            stc = stc,
            isComposite = layers.size > 1,
            cavityDepthMm = cavityDepthMm,
            insulationMaterial = if (hasInsulationFill) "吸声棉填充" else null
        )
    }

    /**
     * Combined STC for a wall with door/window (area-weighted).
     * R_total = 10 * lg( (S1 + S2) / (S1*10^(-R1/10) + S2*10^(-R2/10)) )
     *
     * @param stcWall STC of the main wall
     * @param areaWallM2 Area of the main wall (m²)
     * @param stcOpening STC of the door/window
     * @param areaOpeningM2 Area of the door/window (m²)
     * @return Combined STC
     */
    fun combinedStc(
        stcWall: Double,
        areaWallM2: Double,
        stcOpening: Double,
        areaOpeningM2: Double
    ): Double {
        if (areaWallM2 + areaOpeningM2 <= 0.0) return 0.0
        val numerator = areaWallM2 + areaOpeningM2
        val denominator = areaWallM2 * 10.0.pow(-stcWall / 10.0) +
                areaOpeningM2 * 10.0.pow(-stcOpening / 10.0)
        if (denominator <= 0.0) return 0.0
        return 10.0 * log10(numerator / denominator)
    }
}
