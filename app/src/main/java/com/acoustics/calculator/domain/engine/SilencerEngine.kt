package com.acoustics.calculator.domain.engine

import com.acoustics.calculator.domain.model.*
import javax.inject.Inject
import kotlin.math.*

/**
 * Silencer (消声器) insertion loss calculation engine.
 *
 * Supports 3 types:
 * - RESISTIVE (阻性): Based on absorption coefficient and geometry
 * - REACTIVE (抗性): Based on transfer matrix method for expansion chambers
 * - COMPOSITE (阻抗复合式): Combined resistive + reactive calculation
 *
 * All formulas per octave band (63~8000Hz, 8 bands).
 */
class SilencerEngine @Inject constructor() {

    companion object {
        /** Speed of sound at 20°C (m/s) */
        private const val C0 = 343.0

        /** Air density at 20°C (kg/m³) */
        private const val RHO = 1.205

        /** Dynamic viscosity of air (Pa·s) */
        private const val MU = 1.8e-5

        /** Reference pressure (Pa) */
        private const val P_REF = 2e-5
    }

    /**
     * Main calculation entry point.
     * Computes insertion loss per octave band for the given silencer type.
     */
    fun calculate(params: SilencerParams): InsertionLossResult {
        // Temperature correction for speed of sound
        val c = C0 * sqrt((params.temperatureC + 273.15) / 293.15)
        val rho = RHO * (293.15 / (params.temperatureC + 273.15)) *
                (params.atmosphericPressureKpa / 101.325)

        val ilByBand = mutableMapOf<SilencerBand, Double>()

        for (band in SilencerBand.ALL_BANDS) {
            val f = band.hz.toDouble()
            val il = when (params.silencerType) {
                SilencerType.RESISTIVE -> calcResistiveIL(f, params)
                SilencerType.REACTIVE -> calcReactiveIL(f, params, c)
                SilencerType.COMPOSITE -> calcCompositeIL(f, params, c)
            }
            ilByBand[band] = il.coerceAtLeast(0.0)
        }

        // Flow regenerated noise
        val flowNoise = calcFlowNoise(params.flowVelocityMs)

        // Corrected IL (subtract flow noise in energy domain)
        val correctedIL = SilencerBand.ALL_BANDS.associate { band ->
            val il = ilByBand[band] ?: 0.0
            val fn = flowNoise[band] ?: 0.0
            // Subtract flow noise from IL (logarithmic subtraction)
            val corrected = if (fn <= 0.0) il
            else {
                val linearIL = 10.0.pow(il / 10.0)
                val linearFN = 10.0.pow(fn / 10.0)
                if (linearIL <= linearFN) 0.0
                else 10.0 * log10(linearIL - linearFN)
            }
            band to corrected.coerceAtLeast(0.0)
        }

        // A-weighted IL
        val aWeightedIL = correctedIL.mapValues { (band, il) ->
            AWeighting.apply(il, band).coerceAtLeast(0.0)
        }

        // Total A-weighted IL
        val totalAIL = 10.0 * log10(
            SilencerBand.ALL_BANDS.sumOf { band ->
                10.0.pow((aWeightedIL[band] ?: 0.0) / 10.0)
            }.coerceAtLeast(1e-12)
        )

        // Pressure drop
        val pressureDrop = calcPressureDrop(params, rho)

        // After-noise calculation (if fan noise provided)
        val afterNoise: Map<SilencerBand, Double>? = params.fanNoiseSource?.let { fan ->
            SilencerBand.ALL_BANDS.associate { band ->
                val before = fan.soundPowerByBand[band] ?: 0.0
                val reduction = correctedIL[band] ?: 0.0
                band to (before - reduction).coerceAtLeast(0.0)
            }
        }
        val afterTotalA = afterNoise?.let { noise ->
            if (noise.values.all { it <= 0.0 }) null
            else 10.0 * log10(
                SilencerBand.ALL_BANDS.sumOf { band ->
                    val aVal = AWeighting.apply(noise[band] ?: 0.0, band)
                    10.0.pow((aVal) / 10.0)
                }.coerceAtLeast(1e-12)
            )
        }

        return InsertionLossResult(
            silencerType = params.silencerType,
            params = params,
            insertionLossByBand = ilByBand,
            flowNoiseByBand = flowNoise,
            correctedILByBand = correctedIL,
            aWeightedILByBand = aWeightedIL,
            totalADbInsertionLoss = totalAIL,
            pressureDropPa = pressureDrop,
            afterNoiseByBand = afterNoise,
            afterTotalADb = afterTotalA
        )
    }

    // ==================== RESISTIVE SILENCER ====================

    /**
     * Resistive silencer insertion loss calculation.
     * Based on the empirical formula for lined ducts:
     * IL = 1.05 * (P/A) * L * α^1.4 * f(α)
     *
     * For circular ducts with center filling:
     * IL = φ(α) * (P/A) * L
     *
     * Where:
     * P = perimeter of airflow path (m)
     * A = cross-sectional area (m²)
     * L = silencer length (m)
     * α = absorption coefficient at frequency
     */
    private fun calcResistiveIL(f: Double, params: SilencerParams): Double {
        val material = params.material ?: return 0.0
        val alpha = material.absorption[SilencerBand.entries.find { it.hz == f.toInt() }
            ?: SilencerBand.BAND_1000] ?: 0.0

        if (alpha <= 0.01) return 0.0

        val P = params.perimeterM
        val A = params.crossSectionAreaM2
        val L = params.lengthM

        if (A <= 0.0 || L <= 0.0) return 0.0

        // Φ(α): empirical function of absorption coefficient
        // Based on Sabine's empirical formula for lined ducts
        val phi = when {
            alpha > 0.8 -> 1.8 * alpha
            alpha > 0.6 -> 1.6 * alpha
            alpha > 0.4 -> 1.3 * alpha
            alpha > 0.2 -> 1.0 * alpha
            else -> 0.6 * alpha
        }

        val il = phi * (P / A) * L

        // High-frequency attenuation (above 2000Hz, realistic limit)
        val fHigh = if (f > 2000) {
            // At very high frequencies, directivity reduces effectiveness
            val highFreqRolloff = exp(-(f - 2000) / 8000.0)
            highFreqRolloff.coerceIn(0.5, 1.0)
        } else 1.0

        return il * fHigh
    }

    // ==================== REACTIVE SILENCER ====================

    /**
     * Reactive silencer (expansion chamber) insertion loss.
     * Based on transfer matrix method for a single expansion chamber:
     *
     * IL = 10 * log10(1 + ((m - 1/m)^2 * sin²(kL)) / 4)
     *
     * Where:
     * m = expansion ratio (A_chamber / A_duct)
     * k = wavenumber = 2πf/c
     * L = chamber length (m)
     *
     * For multiple chambers, TL stacks additively with spacing effects.
     */
    private fun calcReactiveIL(f: Double, params: SilencerParams, speedOfSound: Double): Double {
        if (params.chambers.isEmpty()) return 0.0

        val k = 2.0 * PI * f / speedOfSound

        var totalIL = 0.0

        for (chamber in params.chambers) {
            val chamberLength = chamber.chamberLengthM
            // Expansion ratio: assume chamber CSA = chamberVolume / chamberLength
            val chamberCSA = if (chamberLength > 0) chamber.chamberVolumeM3 / chamberLength
            else params.crossSectionAreaM2 * 2.0

            val m = chamberCSA / params.crossSectionAreaM2.coerceAtLeast(0.01)

            // Transfer matrix IL for expansion chamber
            val sinKL = sin(k * chamberLength)
            val expansionTerm = (m - 1.0 / m) * sinKL
            val chamberIL = 10.0 * log10(1.0 + (expansionTerm * expansionTerm) / 4.0)

            totalIL += chamberIL
        }

        // Multiple chambers resonance tuning
        // Account for Helmholtz resonator effect with perforated elements
        if (params.chambers.isNotEmpty()) {
            val firstChamber = params.chambers.first()
            if (firstChamber.perforationRate > 0.0 && firstChamber.neckLengthMm > 0.0) {
                // Helmholtz resonator frequency and IL contribution
                val neckLenM = firstChamber.neckLengthMm / 1000.0
                val perforation = firstChamber.perforationRate.coerceIn(0.01, 0.99)
                val helmholtzFreq = (speedOfSound / (2.0 * PI)) *
                        sqrt(perforation / (neckLenM * firstChamber.chamberVolumeM3.coerceAtLeast(0.01)))

                // Additional IL near Helmholtz resonance
                val fRatio = f / helmholtzFreq.coerceAtLeast(1.0)
                if (fRatio in 0.5..2.0) {
                    val helmholtzIL = 10.0 * log10(1.0 + (fRatio * perforation * 10.0))
                    totalIL += helmholtzIL
                }
            }
        }

        return totalIL
    }

    // ==================== COMPOSITE SILENCER ====================

    /**
     * Composite (impedance compound) silencer.
     * Combines resistive and reactive mechanisms:
     * IL_composite = IL_resistive + IL_reactive - overlap_correction
     *
     * The overlap correction accounts for the fact that the two mechanisms
     * are not perfectly additive in the energy domain.
     */
    private fun calcCompositeIL(f: Double, params: SilencerParams, speedOfSound: Double): Double {
        val resistiveIL = calcResistiveIL(f, params)
        val reactiveIL = calcReactiveIL(f, params, speedOfSound)

        // Overlap correction: the two mechanisms share some of the same acoustic energy
        // Combined IL = 10*log10(10^(ILr/10) + 10^(ILrct/10)) with overlap adjustment
        val combinedLinear = 10.0.pow(resistiveIL / 10.0) + 10.0.pow(reactiveIL / 10.0)

        return if (combinedLinear > 1.0) {
            10.0 * log10(combinedLinear) - 1.5 // 1.5dB empirical overlap correction
        } else 0.0
    }

    // ==================== FLOW NOISE ====================

    /**
     * Flow-regenerated noise (气流再生噪声).
     * Based on empirical formula:
     * L_fn = 10 + 60 * log10(v) + 10 * log10(A)
     *
     * Where v = flow velocity (m/s), A = cross-sectional area (m²)
     *
     * Spectral distribution shapes the broadband noise across frequencies.
     */
    fun calcFlowNoise(velocityMs: Double): Map<SilencerBand, Double> {
        if (velocityMs <= 0.0) {
            return SilencerBand.ALL_BANDS.associateWith { 0.0 }
        }

        // Overall flow noise level
        val overallLfn = 10.0 + 60.0 * log10(velocityMs.coerceAtLeast(1.0))

        // Spectral distribution (normalized to peak at 500-1000Hz)
        val spectralShape = mapOf(
            SilencerBand.BAND_63 to -15.0,
            SilencerBand.BAND_125 to -10.0,
            SilencerBand.BAND_250 to -5.0,
            SilencerBand.BAND_500 to -2.0,
            SilencerBand.BAND_1000 to 0.0,
            SilencerBand.BAND_2000 to -3.0,
            SilencerBand.BAND_4000 to -8.0,
            SilencerBand.BAND_8000 to -14.0
        )

        return SilencerBand.ALL_BANDS.associate { band ->
            val correction = spectralShape[band] ?: -10.0
            val bandLevel = overallLfn + correction
            band to bandLevel.coerceAtLeast(0.0)
        }
    }

    // ==================== PRESSURE DROP ====================

    /**
     * Calculate silencer pressure drop (Pa).
     *
     * Δp = K * (0.5 * ρ * v²)
     *
     * Where K is the loss coefficient depending on silencer type and geometry.
     */
    fun calcPressureDrop(params: SilencerParams, density: Double = RHO): Double {
        val v = params.flowVelocityMs
        val dynamicPressure = 0.5 * density * v * v

        val K = when (params.silencerType) {
            SilencerType.RESISTIVE -> {
                // K depends on length/area ratio and material fill
                val fillFactor = params.material?.nrc?.let { 1.0 + it * 0.5 } ?: 1.0
                0.3 + (params.lengthM / sqrt(params.crossSectionAreaM2.coerceAtLeast(0.01))) * 0.08 * fillFactor
            }
            SilencerType.REACTIVE -> {
                // Each chamber adds pressure drop
                val chamberDrop = params.chambers.sumOf { chamber ->
                    val expansionRatio = (chamber.chamberVolumeM3 /
                            (chamber.chamberLengthM.coerceAtLeast(0.1))) /
                            params.crossSectionAreaM2.coerceAtLeast(0.01)
                    0.2 * (expansionRatio - 1.0).coerceAtLeast(0.1)
                }
                chamberDrop + 0.2
            }
            SilencerType.COMPOSITE -> {
                val resistiveK = 0.3 + (params.lengthM / sqrt(params.crossSectionAreaM2.coerceAtLeast(0.01))) * 0.08
                val reactiveK = params.chambers.sumOf { chamber ->
                    val ratio = (chamber.chamberVolumeM3 / chamber.chamberLengthM.coerceAtLeast(0.1)) /
                            params.crossSectionAreaM2.coerceAtLeast(0.01)
                    0.15 * (ratio - 1.0).coerceAtLeast(0.1)
                }
                resistiveK + reactiveK + 0.3
            }
        }

        return K * dynamicPressure
    }

    // ==================== DUCT DIAMETER CONVERSION ====================

    /**
     * Convert duct diameter to cross-sectional area.
     * Area = π * (d/2)²
     */
    fun diameterToArea(diameterM: Double): Double =
        PI * (diameterM / 2.0).pow(2)

    /**
     * Convert cross-sectional area to equivalent duct diameter.
     * d = 2 * sqrt(A/π)
     */
    fun areaToDiameter(areaM2: Double): Double =
        2.0 * sqrt(areaM2 / PI)

    /**
     * Calculate perimeter from cross-sectional area (assume circular).
     * P = 2 * sqrt(π * A)
     */
    fun areaToPerimeter(areaM2: Double): Double =
        2.0 * sqrt(PI * areaM2)

    // ==================== SMART SELECTION ====================

    /**
     * Smart silencer selection: iterate over all combinations and find top solutions.
     */
    fun smartSelect(criteria: SilencerSelectionCriteria): List<SilencerRecommendation> {
        val candidates = mutableListOf<SilencerRecommendation>()

        val types = if (criteria.preferredType != null) listOf(criteria.preferredType)
        else SilencerType.entries

        val materials = if (criteria.preferredMaterial != null)
            SilencerMaterialLibrary.materials.filter {
                it.name.contains(criteria.preferredMaterial, ignoreCase = true) && it.thicknessMm <= 100.0
            }.ifEmpty { SilencerMaterialLibrary.materials }
        else SilencerMaterialLibrary.materials.filter { it.thicknessMm <= 100.0 }

        // Length sweep
        val lengths = generateSequence(0.3) { (it + 0.2).coerceAtMost(criteria.maxLengthM) }
            .takeWhile { it <= criteria.maxLengthM }
            .toList()

        for (type in types) {
            for (material in materials) {
                for (length in lengths) {
                    val params = SilencerParams(
                        silencerType = type,
                        lengthM = length,
                        crossSectionAreaM2 = criteria.ductAreaM2,
                        material = material,
                        materialThicknessMm = material.thicknessMm,
                        perimeterM = areaToPerimeter(criteria.ductAreaM2),
                        flowVelocityMs = criteria.flowVelocityMs,
                        chambers = listOf(ReactiveChamber(
                            chamberVolumeM3 = criteria.ductAreaM2 * 0.5,
                            chamberLengthM = length
                        ))
                    )

                    val result = calculate(params)

                    val isCompliant = result.totalADbInsertionLoss >= criteria.targetInsertionLossDbA &&
                            result.pressureDropPa <= criteria.maxAllowablePressureDropPa &&
                            length >= criteria.minLengthM

                    val costEstimate = estimateCost(type, length, material)

                    candidates.add(SilencerRecommendation(
                        rank = 0,
                        silencerType = type,
                        lengthM = length,
                        material = material,
                        materialThicknessMm = material.thicknessMm,
                        actualILDbA = result.totalADbInsertionLoss,
                        actualPressureDropPa = result.pressureDropPa,
                        costEstimate = costEstimate,
                        isFullyCompliant = isCompliant
                    ))
                }
            }
        }

        // Filter compliant solutions, sort by compliance + cost, take top 3
        val compliant = candidates.filter { it.isFullyCompliant }
        val nonCompliant = candidates.filter { !it.isFullyCompliant }

        val sorted = if (compliant.isNotEmpty()) {
            compliant.sortedBy { it.lengthM }
        } else {
            nonCompliant.sortedByDescending { it.actualILDbA }
        }

        return sorted.take(3).mapIndexed { index, rec -> rec.copy(rank = index + 1) }
    }

    private fun estimateCost(type: SilencerType, lengthM: Double, material: SilencerMaterial?): String {
        val baseCost = when (type) {
            SilencerType.RESISTIVE -> 500.0
            SilencerType.REACTIVE -> 800.0
            SilencerType.COMPOSITE -> 1200.0
        }
        val materialCost = material?.let { 200.0 + it.thicknessMm * 2.0 } ?: 200.0
        val total = baseCost + materialCost * lengthM
        return "¥${"%.0f".format(total)}/台"
    }

    // ==================== COMPLIANCE VERIFICATION ====================

    /**
     * Verify silencer design against target requirements.
     */
    fun verifyCompliance(
        result: InsertionLossResult,
        targetPerBand: Map<SilencerBand, Double>,
        targetTotalADb: Double
    ): SilencerComplianceResult {
        val comparisons = SilencerBand.ALL_BANDS.associate { band ->
            val target = targetPerBand[band] ?: 0.0
            val actual = result.correctedILByBand[band] ?: 0.0
            val isCompliant = actual >= target - 0.5 // 0.5dB tolerance
            val deficit = (target - actual).coerceAtLeast(0.0)
            band to ComplianceBandResult(band, target, actual, isCompliant, deficit)
        }

        val isTotalCompliant = result.totalADbInsertionLoss >= targetTotalADb - 0.5
        val overallDeficit = (targetTotalADb - result.totalADbInsertionLoss).coerceAtLeast(0.0)

        val allBandsCompliant = comparisons.values.all { it.isCompliant }
        val isFullyCompliant = isTotalCompliant && allBandsCompliant

        val suggestions = mutableListOf<String>()
        if (!isFullyCompliant) {
            val worstBand = comparisons.values.filter { !it.isCompliant }
                .maxByOrNull { it.deficitDb }
            worstBand?.let {
                suggestions.add("${it.band.label}频率降噪量不足，还差 ${"%.1f".format(it.deficitDb)} dB")
            }

            // Generate adjustment suggestions
            if (overallDeficit > 0) {
                suggestions.add("建议将消声器长度增加至 ${"%.1f".format(result.params.lengthM * 1.3)}m")
            }
            if (result.params.material?.let { it.nrc < 0.7 } == true) {
                suggestions.add("建议更换吸声材料为高NRC材料（如100mm厚离心玻璃棉）")
            }
            val hasLowFreqDeficit = comparisons.filterKeys {
                it in listOf(SilencerBand.BAND_63, SilencerBand.BAND_125)
            }.values.any { !it.isCompliant }
            if (hasLowFreqDeficit && result.silencerType == SilencerType.RESISTIVE) {
                suggestions.add("低频不足，建议改用阻抗复合式消声器或增加抗性结构")
            }
            val hasHighFreqDeficit = comparisons.filterKeys {
                it in listOf(SilencerBand.BAND_4000, SilencerBand.BAND_8000)
            }.values.any { !it.isCompliant }
            if (hasHighFreqDeficit) {
                suggestions.add("高频衰减受气流再生噪声影响，建议降低气流速度")
            }
        } else {
            suggestions.add("✓ 当前选型满足降噪要求")
        }

        return SilencerComplianceResult(
            isFullyCompliant = isFullyCompliant,
            comparisons = comparisons,
            totalTargetDbA = targetTotalADb,
            totalActualDbA = result.totalADbInsertionLoss,
            overallDeficitDb = overallDeficit,
            suggestions = suggestions
        )
    }
}
