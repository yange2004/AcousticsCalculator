package com.acoustics.calculator.domain.helper

import com.acoustics.calculator.core.constants.FrequencyBand
import com.acoustics.calculator.domain.model.RoomDimensions
import com.acoustics.calculator.domain.model.SurfaceComposition
import com.acoustics.calculator.domain.model.SurfaceType

/**
 * Computes total absorption and mean absorption coefficients from room surface composition.
 * A = Σ(Sᵢ × αᵢ) for each frequency band
 * ᾱ = A / S_total
 */
object AbsorptionCalculator {

    /**
     * Compute total absorption A (in m² sabins) for each octave band.
     * For unassigned surfaces, uses default absorption α = 0.01.
     */
    fun computeTotalAbsorption(
        room: RoomDimensions,
        composition: SurfaceComposition
    ): Map<FrequencyBand, Double> {
        val result = mutableMapOf<FrequencyBand, Double>()

        for (band in FrequencyBand.OCTAVE_BANDS) {
            var totalA = 0.0
            var totalAssignedArea = 0.0

            for ((surfaceType, assignments) in composition.surfaces) {
                for (assignment in assignments) {
                    val alpha = assignment.material.absorption[band] ?: 0.01
                    totalA += assignment.areaM2 * alpha
                    totalAssignedArea += assignment.areaM2
                }
            }

            // Add default absorption for unassigned areas
            val unassignedArea = room.totalSurfaceAreaM2 - totalAssignedArea
            if (unassignedArea > 0.001) {
                totalA += unassignedArea * 0.01 // default: nearly reflective
            }

            result[band] = totalA
        }

        return result
    }

    /**
     * Compute mean absorption coefficient ᾱ = A / S for each band.
     */
    fun computeMeanAbsorption(
        room: RoomDimensions,
        totalAbsorption: Map<FrequencyBand, Double>
    ): Map<FrequencyBand, Double> {
        val S = room.totalSurfaceAreaM2
        if (S <= 0.0) {
            return FrequencyBand.OCTAVE_BANDS.associateWith { 0.0 }
        }
        return totalAbsorption.mapValues { (_, A) -> (A / S).coerceIn(0.0, 1.0) }
    }

    /**
     * Compute average absorption coefficient for a specific surface.
     */
    fun computeSurfaceMeanAbsorption(
        assignments: List<com.acoustics.calculator.domain.model.SurfaceAssignment>,
        band: FrequencyBand
    ): Double {
        if (assignments.isEmpty()) return 0.01
        val totalArea = assignments.sumOf { it.areaM2 }
        if (totalArea <= 0.0) return 0.01
        val weightedAlpha = assignments.sumOf { it.areaM2 * (it.material.absorption[band] ?: 0.01) }
        return (weightedAlpha / totalArea).coerceIn(0.0, 1.0)
    }

    /**
     * Compute Fitzroy axis-specific mean absorption.
     * Returns triple of (αx, αy, αz) for each band.
     */
    fun computeFitzroyAxisAbsorption(
        room: RoomDimensions,
        composition: SurfaceComposition,
        band: FrequencyBand
    ): Triple<Double, Double, Double> {
        val ax = computeSurfaceMeanAbsorption(
            (composition.surfaces[SurfaceType.LEFT_WALL] ?: emptyList()) +
            (composition.surfaces[SurfaceType.RIGHT_WALL] ?: emptyList()), band
        )
        val ay = computeSurfaceMeanAbsorption(
            (composition.surfaces[SurfaceType.FRONT_WALL] ?: emptyList()) +
            (composition.surfaces[SurfaceType.REAR_WALL] ?: emptyList()), band
        )
        val az = computeSurfaceMeanAbsorption(
            (composition.surfaces[SurfaceType.CEILING] ?: emptyList()) +
            (composition.surfaces[SurfaceType.FLOOR] ?: emptyList()), band
        )
        return Triple(ax, ay, az)
    }
}
