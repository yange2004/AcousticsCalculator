package com.acoustics.calculator.domain.model

import com.acoustics.calculator.core.constants.FrequencyBand
import com.acoustics.calculator.core.extensions.roundTo

/**
 * Domain model for an acoustic material with absorption coefficients.
 */
data class Material(
    val id: Long,
    val nameZh: String,
    val nameEn: String,
    val categoryId: Long,
    val description: String,
    val densityKgm3: Double?,
    val thicknessMm: Double?,
    val absorption: Map<FrequencyBand, Double>,
    val nrc: Double,
    val source: String,
    val isFavorite: Boolean
) {
    /** Average absorption across all octave bands */
    val averageAbsorption: Double by lazy {
        absorption.values.average().roundTo(3)
    }

    /** Check if material is highly absorptive (NRC >= 0.8) */
    val isHighlyAbsorptive: Boolean get() = nrc >= 0.8

    /** Check if material is reflective (NRC < 0.2) */
    val isReflective: Boolean get() = nrc < 0.2
}

data class MaterialCategory(
    val id: Long,
    val nameZh: String,
    val nameEn: String,
    val description: String,
    val parentId: Long?,
    val sortOrder: Int
)
