package com.acoustics.calculator.core.constants

/**
 * Octave band frequency bands used in architectural acoustics.
 */
enum class FrequencyBand(val hz: Int, val label: String) {
    BAND_125(125, "125 Hz"),
    BAND_250(250, "250 Hz"),
    BAND_500(500, "500 Hz"),
    BAND_1000(1000, "1 kHz"),
    BAND_2000(2000, "2 kHz"),
    BAND_4000(4000, "4 kHz");

    companion object {
        val OCTAVE_BANDS: List<FrequencyBand> = entries.toList()

        /** Bands most relevant for speech intelligibility (500, 1000, 2000 Hz) */
        val SPEECH_BANDS: List<FrequencyBand> = listOf(BAND_500, BAND_1000, BAND_2000)

        /** Bands for mid-frequency RT60 average (500, 1000 Hz) */
        val MID_BANDS: List<FrequencyBand> = listOf(BAND_500, BAND_1000)

        /** Bands for bass ratio (125, 250, 500, 1000 Hz) */
        val BASS_BANDS: List<FrequencyBand> = listOf(BAND_125, BAND_250)

        /** Bands for NRC calculation (250, 500, 1000, 2000 Hz) */
        val NRC_BANDS: List<FrequencyBand> = listOf(BAND_250, BAND_500, BAND_1000, BAND_2000)

        /** Convert from Hz value to enum */
        fun fromHz(hz: Int): FrequencyBand =
            entries.find { it.hz == hz } ?: throw IllegalArgumentException("Unknown frequency: $hz Hz")
    }
}
