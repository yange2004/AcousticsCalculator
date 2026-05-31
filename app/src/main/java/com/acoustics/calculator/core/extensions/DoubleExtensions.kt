package com.acoustics.calculator.core.extensions

import kotlin.math.*

/**
 * Extension functions for Double used in acoustics calculations.
 */

/** Round to specified number of decimal places */
fun Double.roundTo(decimals: Int): Double {
    val factor = 10.0.pow(decimals)
    return round(this * factor) / factor
}

/** Convert linear value to decibels (dB) */
fun Double.toDb(): Double = 10.0 * log10(this.coerceAtLeast(1e-10))

/** Convert decibels to linear value */
fun Double.fromDb(): Double = 10.0.pow(this / 10.0)

/** Convert to dB SPL (sound pressure level) from Pascal */
fun Double.paToDbSpl(): Double = 20.0 * log10(this / 2e-5)

/** Clamp value between min and max */
fun Double.clamp(min: Double, max: Double): Double =
    coerceIn(min, max)

/** Check if value is within the given range (inclusive) */
fun Double.isInRange(range: ClosedFloatingPointRange<Double>): Boolean =
    this in range

/** Safe division, returns 0.0 if divisor is 0 */
fun Double.safeDiv(divisor: Double): Double =
    if (divisor == 0.0) 0.0 else this / divisor

/** Format as percentage string */
fun Double.toPercentString(decimals: Int = 1): String =
    "${(this * 100).roundTo(decimals)}%"

/** Format as dB string */
fun Double.toDbString(decimals: Int = 1): String =
    "${roundTo(decimals)} dB"
