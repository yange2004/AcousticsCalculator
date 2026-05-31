package com.acoustics.calculator.domain.engine

import com.acoustics.calculator.core.constants.PhysicalConstants
import com.acoustics.calculator.domain.model.StrengthFactorResult
import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.log10

/**
 * Strength factor G calculation engine.
 *
 * G = 10 * lg(I_room / I_free_field)
 *
 * Where I_room is the sound intensity (or squared pressure) in the room
 * and I_free_field is the intensity in free field at 10m from the same source.
 *
 * G ≈ 10 * lg(Q/4πr² + 4/A) - 10 * lg(1/4π*10²)
 *   = 10 * lg( (Q/4πr² + 4/A) * 400π )
 */
class StrengthFactorEngine @Inject constructor() {

    companion object {
        const val REFERENCE_DISTANCE = 10.0 // m (standard reference for G)
    }

    /**
     * Calculate strength factor G from room acoustics parameters.
     *
     * @param roomVolumeM3 Room volume in m³
     * @param totalAbsorptionM2 Total absorption A at the relevant frequency
     * @param sourceReceiverDistanceM Distance from source to receiver in m
     * @param directivityFactorQ Source directivity factor Q (1 = omnidirectional)
     * @return StrengthFactorResult
     */
    fun calculate(
        roomVolumeM3: Double,
        totalAbsorptionM2: Double,
        sourceReceiverDistanceM: Double,
        directivityFactorQ: Double = 1.0
    ): StrengthFactorResult {
        val A = totalAbsorptionM2.coerceAtLeast(0.01)

        // Intensity in room (proportional to squared pressure)
        val directTerm = directivityFactorQ / (4.0 * PI * sourceReceiverDistanceM * sourceReceiverDistanceM)
        val reverberantTerm = 4.0 / A
        val roomIntensity = directTerm + reverberantTerm

        // Intensity in free field at 10m
        val freeFieldIntensity = 1.0 / (4.0 * PI * REFERENCE_DISTANCE * REFERENCE_DISTANCE)

        val g = 10.0 * log10(roomIntensity / freeFieldIntensity)

        return StrengthFactorResult(
            g = g,
            roomIntensity = roomIntensity,
            freeFieldIntensity = freeFieldIntensity
        )
    }

    /**
     * Simplified G calculation from RT60 using Barron's revised theory.
     * G ≈ 10*lg(31200*RT60/V) + 10*lg(1 + r²*T/(0.04*V²/³))
     * (simplified for engineering use)
     */
    fun calculateFromRt60(
        roomVolumeM3: Double,
        rt60: Double,
        sourceReceiverDistanceM: Double = 10.0
    ): Double {
        if (rt60 <= 0.0 || roomVolumeM3 <= 0.0) return Double.NEGATIVE_INFINITY

        // Direct + reverberant energy ratio
        val directEnergy = 100.0 / (sourceReceiverDistanceM * sourceReceiverDistanceM) // relative scale
        val revEnergy = 31200.0 * rt60 / roomVolumeM3

        return 10.0 * log10(directEnergy + revEnergy) - 37.0 // normalize to G scale
    }
}
