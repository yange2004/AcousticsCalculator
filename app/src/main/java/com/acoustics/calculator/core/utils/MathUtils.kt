package com.acoustics.calculator.core.utils

import kotlin.math.*

/**
 * Mathematical utility functions for acoustics calculations.
 */
object MathUtils {

    /**
     * Linear interpolation between two values.
     */
    fun lerp(a: Double, b: Double, t: Double): Double = a + (b - a) * t

    /**
     * Inverse linear interpolation.
     */
    fun inverseLerp(a: Double, b: Double, value: Double): Double =
        if (b - a == 0.0) 0.0 else ((value - a) / (b - a)).coerceIn(0.0, 1.0)

    /**
     * Compute the weighted average of values.
     */
    fun weightedAverage(values: List<Double>, weights: List<Double>): Double {
        require(values.size == weights.size) { "Values and weights must have the same size" }
        val totalWeight = weights.sum()
        if (totalWeight == 0.0) return 0.0
        return values.zip(weights).sumOf { (v, w) -> v * w } / totalWeight
    }

    /**
     * Compute arithmetic mean of a list of values.
     */
    fun mean(values: List<Double>): Double =
        if (values.isEmpty()) 0.0 else values.sum() / values.size

    /**
     * Compute the Fresnel number N for sound barrier calculations.
     * N = 2 * δ / λ where δ is the path difference and λ is the wavelength.
     */
    fun fresnelNumber(pathDifference: Double, frequencyHz: Double, speedOfSound: Double = 343.0): Double {
        val wavelength = speedOfSound / frequencyHz
        return 2.0 * pathDifference / wavelength
    }

    /**
     * Compute path difference for a sound barrier.
     * δ = (a + b) - d where a+b is the diffracted path and d is the direct path.
     */
    fun pathDifference(
        sourceHeight: Double,
        receiverHeight: Double,
        barrierHeight: Double,
        sourceBarrierDist: Double,
        receiverBarrierDist: Double
    ): Double {
        val diffractedPath = sqrt((sourceHeight - barrierHeight).pow(2) + sourceBarrierDist.pow(2)) +
                sqrt((receiverHeight - barrierHeight).pow(2) + receiverBarrierDist.pow(2))
        val directPath = sqrt((sourceHeight - receiverHeight).pow(2) +
                (sourceBarrierDist + receiverBarrierDist).pow(2))
        return diffractedPath - directPath
    }
}
