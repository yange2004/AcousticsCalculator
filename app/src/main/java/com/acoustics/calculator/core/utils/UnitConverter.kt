package com.acoustics.calculator.core.utils

import kotlin.math.*

/**
 * Unit conversion utilities for acoustics.
 */
object UnitConverter {

    // --- Length ---
    fun metersToFeet(m: Double): Double = m * 3.28084
    fun feetToMeters(ft: Double): Double = ft / 3.28084
    fun metersToCentimeters(m: Double): Double = m * 100.0
    fun centimetersToMeters(cm: Double): Double = cm / 100.0

    // --- Area ---
    fun sqMetersToSqFeet(m2: Double): Double = m2 * 10.7639
    fun sqFeetToSqMeters(ft2: Double): Double = ft2 / 10.7639

    // --- Volume ---
    fun cubicMetersToCubicFeet(m3: Double): Double = m3 * 35.3147
    fun cubicFeetToCubicMeters(ft3: Double): Double = ft3 / 35.3147

    // --- Sound Pressure Level ---
    fun pascalToDbSpl(pa: Double): Double = 20.0 * log10(pa / 2e-5)
    fun dbSplToPascal(db: Double): Double = 2e-5 * 10.0.pow(db / 20.0)

    // --- Sound Intensity Level ---
    fun intensityToDbSil(intensity: Double): Double = 10.0 * log10(intensity / 1e-12)
    fun dbSilToIntensity(db: Double): Double = 1e-12 * 10.0.pow(db / 10.0)

    // --- Sound Power Level ---
    fun powerToDbSwl(power: Double): Double = 10.0 * log10(power / 1e-12)
    fun dbSwlToPower(db: Double): Double = 1e-12 * 10.0.pow(db / 10.0)

    // --- Frequency ↔ Wavelength ---
    fun frequencyToWavelength(hz: Double, speedOfSound: Double = 343.0): Double = speedOfSound / hz
    fun wavelengthToFrequency(lambda: Double, speedOfSound: Double = 343.0): Double = speedOfSound / lambda

    // --- Temperature ---
    fun celsiusToFahrenheit(c: Double): Double = c * 9.0 / 5.0 + 32.0
    fun fahrenheitToCelsius(f: Double): Double = (f - 32.0) * 5.0 / 9.0

    // --- Mass ---
    fun kgToLb(kg: Double): Double = kg * 2.20462
    fun lbToKg(lb: Double): Double = lb / 2.20462

    // --- Density ---
    fun kgm3ToLbft3(kgm3: Double): Double = kgm3 * 0.062428
    fun lbft3ToKgm3(lbft3: Double): Double = lbft3 / 0.062428

    // --- Reverberation ---
    /** Convert absorption units from sabins (m²) to sabins (ft²) */
    fun sabinM2ToSabinFt2(sabinM2: Double): Double = sabinM2 * 10.7639
    fun sabinFt2ToSabinM2(sabinFt2: Double): Double = sabinFt2 / 10.7639
}
