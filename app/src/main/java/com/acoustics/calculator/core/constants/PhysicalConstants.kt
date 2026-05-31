package com.acoustics.calculator.core.constants

/**
 * Physical constants used in acoustics calculations.
 *
 * Updated 2026-05-31 with comprehensive research data.
 */
object PhysicalConstants {
    /** Speed of sound in air at 20°C, 1atm (m/s) */
    const val SPEED_OF_SOUND = 343.0

    /** Speed of sound in air at 0°C (m/s) */
    const val SPEED_OF_SOUND_0C = 331.0

    /** Air density at 20°C, 1atm (kg/m³) */
    const val AIR_DENSITY = 1.205

    /** Air density at 0°C, 1atm (kg/m³) */
    const val AIR_DENSITY_0C = 1.293

    /** Reference sound pressure (Pa) - threshold of hearing */
    const val REFERENCE_PRESSURE = 2e-5

    /** Reference sound intensity (W/m²) */
    const val REFERENCE_INTENSITY = 1e-12

    /** Reference sound power (W) */
    const val REFERENCE_POWER = 1e-12

    /** Characteristic impedance of air at 20°C (Pa·s/m) */
    const val CHARACTERISTIC_IMPEDANCE = 413.0

    /** Dynamic viscosity of air at 20°C (Pa·s) */
    const val AIR_VISCOSITY = 1.8e-5

    /** Specific gas constant for dry air (J/(kg·K)) */
    const val AIR_GAS_CONSTANT = 287.058

    /**
     * Air absorption coefficient m (1/m) at 20°C, 50% RH per octave band.
     * Used in Eyring-Knutsen formula for high-frequency correction.
     * Values increase with frequency due to molecular relaxation of O₂ and N₂.
     * Significant above 2000Hz for large rooms (V > 1000m³).
     */
    val AIR_ABSORPTION_COEFFICIENT: Map<FrequencyBand, Double> = mapOf(
        FrequencyBand.BAND_125 to 0.0003,
        FrequencyBand.BAND_250 to 0.0011,
        FrequencyBand.BAND_500 to 0.0027,
        FrequencyBand.BAND_1000 to 0.0050,
        FrequencyBand.BAND_2000 to 0.0100,
        FrequencyBand.BAND_4000 to 0.0240
    )

    /**
     * A-weighting corrections per frequency band (IEC 61672-1:2013).
     */
    val A_WEIGHTING: Map<FrequencyBand, Double> = mapOf(
        FrequencyBand.BAND_125 to -16.1,
        FrequencyBand.BAND_250 to -8.6,
        FrequencyBand.BAND_500 to -3.2,
        FrequencyBand.BAND_1000 to 0.0,
        FrequencyBand.BAND_2000 to 1.2,
        FrequencyBand.BAND_4000 to 1.0
    )

    /**
     * C-weighting corrections per frequency band (IEC 61672-1:2013).
     */
    val C_WEIGHTING: Map<FrequencyBand, Double> = mapOf(
        FrequencyBand.BAND_125 to -0.2,
        FrequencyBand.BAND_250 to 0.0,
        FrequencyBand.BAND_500 to 0.0,
        FrequencyBand.BAND_1000 to 0.0,
        FrequencyBand.BAND_2000 to -0.2,
        FrequencyBand.BAND_4000 to -0.8
    )
}
