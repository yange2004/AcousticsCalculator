package com.acoustics.calculator.domain.model

/**
 * Silencer types supported by the calculation engine.
 */
enum class SilencerType(val label: String, val description: String, val tip: String) {
    RESISTIVE("阻性消声器", "利用多孔吸声材料消耗声能", "适用场景：中高频噪声（500Hz以上）\n如：风机进/排风口、空调管道系统\n特点：中高频消声效果好，低频效果有限"),
    REACTIVE("抗性消声器", "利用声抗元件（腔室、旁支）反射声波", "适用场景：低频噪声（500Hz以下）\n如：发动机排气、压缩机管路\n特点：低频消声好，高频效果有限，阻力损失小"),
    COMPOSITE("阻抗复合式消声器", "结合阻性和抗性两种机理", "适用场景：宽频带噪声（全频段）\n如：柴油发电机、大型风机系统\n特点：全频段消声好，但体积较大")
}

/**
 * Silencer octave bands (63~8000Hz, 8 bands).
 */
enum class SilencerBand(val hz: Int, val label: String) {
    BAND_63(63, "63 Hz"),
    BAND_125(125, "125 Hz"),
    BAND_250(250, "250 Hz"),
    BAND_500(500, "500 Hz"),
    BAND_1000(1000, "1 kHz"),
    BAND_2000(2000, "2 kHz"),
    BAND_4000(4000, "4 kHz"),
    BAND_8000(8000, "8 kHz");

    companion object {
        val ALL_BANDS: List<SilencerBand> = entries.toList()
        val ALL_FREQUENCIES: List<Int> = ALL_BANDS.map { it.hz }

        /** Map to core FrequencyBand where overlapping */
        fun fromCoreBand(band: com.acoustics.calculator.core.constants.FrequencyBand): SilencerBand =
            when (band) {
                com.acoustics.calculator.core.constants.FrequencyBand.BAND_125 -> BAND_125
                com.acoustics.calculator.core.constants.FrequencyBand.BAND_250 -> BAND_250
                com.acoustics.calculator.core.constants.FrequencyBand.BAND_500 -> BAND_500
                com.acoustics.calculator.core.constants.FrequencyBand.BAND_1000 -> BAND_1000
                com.acoustics.calculator.core.constants.FrequencyBand.BAND_2000 -> BAND_2000
                com.acoustics.calculator.core.constants.FrequencyBand.BAND_4000 -> BAND_4000
            }
    }
}

/**
 * A-weighting correction values (dB) per silencer band.
 * Based on IEC 61672-1:2013.
 */
object AWeighting {
    val VALUES: Map<SilencerBand, Double> = mapOf(
        SilencerBand.BAND_63 to -26.2,
        SilencerBand.BAND_125 to -16.1,
        SilencerBand.BAND_250 to -8.6,
        SilencerBand.BAND_500 to -3.2,
        SilencerBand.BAND_1000 to 0.0,
        SilencerBand.BAND_2000 to 1.2,
        SilencerBand.BAND_4000 to 1.0,
        SilencerBand.BAND_8000 to -1.1
    )

    /** Apply A-weighting to a dB level at a specific band */
    fun apply(dB: Double, band: SilencerBand): Double =
        dB + (VALUES[band] ?: 0.0)
}

/**
 * Material absorption coefficients for silencer calculation (8 octave bands).
 */
data class SilencerMaterial(
    val id: Long = 0,
    val name: String,
    val thicknessMm: Double,
    val densityKgm3: Double? = null,
    /** Absorption coefficients per octave band (63~8000Hz) */
    val absorption: Map<SilencerBand, Double>,
    val isCustom: Boolean = false
) {
    val nrc: Double by lazy {
        listOf(
            absorption[SilencerBand.BAND_250] ?: 0.0,
            absorption[SilencerBand.BAND_500] ?: 0.0,
            absorption[SilencerBand.BAND_1000] ?: 0.0,
            absorption[SilencerBand.BAND_2000] ?: 0.0
        ).average()
    }
}

/**
 * Built-in material library for silencer design.
 * Contains common silencer materials with their absorption coefficients per thickness.
 */
object SilencerMaterialLibrary {
    /** All built-in materials */
    val materials: List<SilencerMaterial> by lazy { buildMaterials() }

    /** Get materials by name */
    fun findByName(name: String): List<SilencerMaterial> =
        materials.filter { it.name.contains(name, ignoreCase = true) }

    /** Get materials by thickness */
    fun findByThickness(mm: Double): List<SilencerMaterial> =
        materials.filter { it.thicknessMm == mm }

    private fun buildMaterials(): List<SilencerMaterial> = listOf(
        // 离心玻璃棉, 25mm
        SilencerMaterial(1, "离心玻璃棉", 25.0, 24.0, mapOf(
            SilencerBand.BAND_63 to 0.05, SilencerBand.BAND_125 to 0.15, SilencerBand.BAND_250 to 0.35,
            SilencerBand.BAND_500 to 0.85, SilencerBand.BAND_1000 to 0.85, SilencerBand.BAND_2000 to 0.85,
            SilencerBand.BAND_4000 to 0.90, SilencerBand.BAND_8000 to 0.85
        )),
        // 离心玻璃棉, 50mm
        SilencerMaterial(2, "离心玻璃棉", 50.0, 24.0, mapOf(
            SilencerBand.BAND_63 to 0.10, SilencerBand.BAND_125 to 0.25, SilencerBand.BAND_250 to 0.60,
            SilencerBand.BAND_500 to 0.85, SilencerBand.BAND_1000 to 0.90, SilencerBand.BAND_2000 to 0.90,
            SilencerBand.BAND_4000 to 0.95, SilencerBand.BAND_8000 to 0.90
        )),
        // 离心玻璃棉, 100mm
        SilencerMaterial(3, "离心玻璃棉", 100.0, 24.0, mapOf(
            SilencerBand.BAND_63 to 0.15, SilencerBand.BAND_125 to 0.40, SilencerBand.BAND_250 to 0.75,
            SilencerBand.BAND_500 to 0.90, SilencerBand.BAND_1000 to 0.95, SilencerBand.BAND_2000 to 0.95,
            SilencerBand.BAND_4000 to 0.95, SilencerBand.BAND_8000 to 0.90
        )),
        // 离心玻璃棉, 150mm
        SilencerMaterial(4, "离心玻璃棉", 150.0, 24.0, mapOf(
            SilencerBand.BAND_63 to 0.20, SilencerBand.BAND_125 to 0.50, SilencerBand.BAND_250 to 0.85,
            SilencerBand.BAND_500 to 0.95, SilencerBand.BAND_1000 to 0.95, SilencerBand.BAND_2000 to 0.95,
            SilencerBand.BAND_4000 to 0.95, SilencerBand.BAND_8000 to 0.90
        )),
        // 岩棉, 50mm
        SilencerMaterial(5, "岩棉", 50.0, 100.0, mapOf(
            SilencerBand.BAND_63 to 0.08, SilencerBand.BAND_125 to 0.12, SilencerBand.BAND_250 to 0.32,
            SilencerBand.BAND_500 to 0.70, SilencerBand.BAND_1000 to 0.85, SilencerBand.BAND_2000 to 0.90,
            SilencerBand.BAND_4000 to 0.90, SilencerBand.BAND_8000 to 0.85
        )),
        // 岩棉, 100mm
        SilencerMaterial(6, "岩棉", 100.0, 100.0, mapOf(
            SilencerBand.BAND_63 to 0.12, SilencerBand.BAND_125 to 0.25, SilencerBand.BAND_250 to 0.55,
            SilencerBand.BAND_500 to 0.80, SilencerBand.BAND_1000 to 0.90, SilencerBand.BAND_2000 to 0.90,
            SilencerBand.BAND_4000 to 0.90, SilencerBand.BAND_8000 to 0.85
        )),
        // 聚酯纤维板, 25mm
        SilencerMaterial(7, "聚酯纤维板", 25.0, 30.0, mapOf(
            SilencerBand.BAND_63 to 0.04, SilencerBand.BAND_125 to 0.10, SilencerBand.BAND_250 to 0.25,
            SilencerBand.BAND_500 to 0.55, SilencerBand.BAND_1000 to 0.80, SilencerBand.BAND_2000 to 0.75,
            SilencerBand.BAND_4000 to 0.70, SilencerBand.BAND_8000 to 0.65
        )),
        // 聚酯纤维板, 50mm
        SilencerMaterial(8, "聚酯纤维板", 50.0, 30.0, mapOf(
            SilencerBand.BAND_63 to 0.08, SilencerBand.BAND_125 to 0.15, SilencerBand.BAND_250 to 0.32,
            SilencerBand.BAND_500 to 0.70, SilencerBand.BAND_1000 to 0.85, SilencerBand.BAND_2000 to 0.80,
            SilencerBand.BAND_4000 to 0.75, SilencerBand.BAND_8000 to 0.70
        )),
        // 微孔铝板, 1mm (穿孔率30%)
        SilencerMaterial(9, "微孔铝板", 1.0, 2700.0, mapOf(
            SilencerBand.BAND_63 to 0.10, SilencerBand.BAND_125 to 0.25, SilencerBand.BAND_250 to 0.50,
            SilencerBand.BAND_500 to 0.60, SilencerBand.BAND_1000 to 0.55, SilencerBand.BAND_2000 to 0.45,
            SilencerBand.BAND_4000 to 0.35, SilencerBand.BAND_8000 to 0.25
        )),
        // 泡沫铝, 25mm
        SilencerMaterial(10, "泡沫铝", 25.0, 500.0, mapOf(
            SilencerBand.BAND_63 to 0.06, SilencerBand.BAND_125 to 0.12, SilencerBand.BAND_250 to 0.28,
            SilencerBand.BAND_500 to 0.50, SilencerBand.BAND_1000 to 0.65, SilencerBand.BAND_2000 to 0.60,
            SilencerBand.BAND_4000 to 0.55, SilencerBand.BAND_8000 to 0.50
        )),
        // 泡沫铝, 50mm
        SilencerMaterial(11, "泡沫铝", 50.0, 500.0, mapOf(
            SilencerBand.BAND_63 to 0.10, SilencerBand.BAND_125 to 0.20, SilencerBand.BAND_250 to 0.40,
            SilencerBand.BAND_500 to 0.65, SilencerBand.BAND_1000 to 0.75, SilencerBand.BAND_2000 to 0.70,
            SilencerBand.BAND_4000 to 0.60, SilencerBand.BAND_8000 to 0.55
        ))
    )

    val thicknessOptions: List<Double> = listOf(25.0, 50.0, 100.0, 150.0)
}

/**
 * Reactive silencer chamber configuration.
 */
data class ReactiveChamber(
    val chamberVolumeM3: Double = 0.1,
    val perforationRate: Double = 0.3,
    val neckLengthMm: Double = 50.0,
    val chamberLengthM: Double = 0.3
)

/**
 * Input parameters for silencer calculation.
 */
data class SilencerParams(
    val silencerType: SilencerType = SilencerType.RESISTIVE,
    val lengthM: Double = 1.0,
    val crossSectionAreaM2: Double = 0.25,
    val ductDiameterM: Double = 0.56, // derived from area
    val material: SilencerMaterial? = null,
    val materialThicknessMm: Double = 50.0,
    // Resistive / Composite parameters
    val perimeterM: Double = 2.0,
    val flowVelocityMs: Double = 8.0,
    // Reactive parameters
    val chambers: List<ReactiveChamber> = listOf(ReactiveChamber()),
    val chamberCount: Int = 1,
    // Environment correction
    val temperatureC: Double = 20.0,
    val atmosphericPressureKpa: Double = 101.325,
    // Optional: source fan noise for "after" calculation
    val fanNoiseSource: FanNoiseData? = null
)

/**
 * Calculated insertion loss results per octave band.
 */
data class InsertionLossResult(
    val silencerType: SilencerType,
    val params: SilencerParams,
    /** Insertion loss per octave band (dB) */
    val insertionLossByBand: Map<SilencerBand, Double>,
    /** Flow-regenerated noise per band (dB) */
    val flowNoiseByBand: Map<SilencerBand, Double>,
    /** Corrected insertion loss after flow noise subtraction */
    val correctedILByBand: Map<SilencerBand, Double>,
    /** A-weighted insertion loss per band */
    val aWeightedILByBand: Map<SilencerBand, Double>,
    /** Total A-weighted insertion loss (dB(A)) */
    val totalADbInsertionLoss: Double,
    /** Absolute pressure drop (Pa) */
    val pressureDropPa: Double,
    /** If fan noise is provided */
    val afterNoiseByBand: Map<SilencerBand, Double>? = null,
    val afterTotalADb: Double? = null
)

/**
 * Selection criteria for smart silencer recommendation.
 */
data class SilencerSelectionCriteria(
    val targetInsertionLossDbA: Double,
    val maxAllowablePressureDropPa: Double = 500.0,
    val preferredType: SilencerType? = null,
    val preferredMaterial: String? = null,
    val maxLengthM: Double = 3.0,
    val minLengthM: Double = 0.3,
    val ductAreaM2: Double = 0.25,
    val flowVelocityMs: Double = 8.0
)

/**
 * Recommended silencer solution from smart selection.
 */
data class SilencerRecommendation(
    val rank: Int,
    val silencerType: SilencerType,
    val lengthM: Double,
    val material: SilencerMaterial?,
    val materialThicknessMm: Double,
    val actualILDbA: Double,
    val actualPressureDropPa: Double,
    val costEstimate: String,
    val isFullyCompliant: Boolean
)

/**
 * Fan noise data model (built-in database).
 */
data class FanNoiseData(
    val id: Long = 0,
    val modelName: String,
    val manufacturer: String = "",
    val fanType: FanType,
    val powerKw: Double = 0.0,
    val airflowM3h: Double = 0.0,
    val pressurePa: Double = 0.0,
    /** Sound power levels per octave band (dB) */
    val soundPowerByBand: Map<SilencerBand, Double>,
    /** Total A-weighted sound power level */
    val totalLwADb: Double = 0.0
)

enum class FanType(val label: String) {
    CENTRIFUGAL_FORWARD("前向离心风机"),
    CENTRIFUGAL_BACKWARD("后向离心风机"),
    AXIAL("轴流风机"),
    MIXED_FLOW("混流风机"),
    ROOF("屋顶风机"),
    CROSS_FLOW("贯流风机")
}

/**
 * Built-in fan noise database with common fan models.
 */
object FanNoiseDatabase {
    val fans: List<FanNoiseData> by lazy { buildFanDatabase() }

    fun findByType(type: FanType): List<FanNoiseData> = fans.filter { it.fanType == type }
    fun findByModel(query: String): List<FanNoiseData> =
        fans.filter { it.modelName.contains(query, ignoreCase = true) }

    private fun buildFanDatabase(): List<FanNoiseData> = listOf(
        // 前向离心风机
        FanNoiseData(1, "CF-4-72-3.6A", "通用", FanType.CENTRIFUGAL_FORWARD, 3.0, 2864.0, 1569.0, mapOf(
            SilencerBand.BAND_63 to 72.0, SilencerBand.BAND_125 to 80.0, SilencerBand.BAND_250 to 85.0,
            SilencerBand.BAND_500 to 89.0, SilencerBand.BAND_1000 to 87.0, SilencerBand.BAND_2000 to 82.0,
            SilencerBand.BAND_4000 to 76.0, SilencerBand.BAND_8000 to 70.0
        ), 92.3),
        FanNoiseData(2, "CF-4-72-5.0A", "通用", FanType.CENTRIFUGAL_FORWARD, 7.5, 7728.0, 2010.0, mapOf(
            SilencerBand.BAND_63 to 76.0, SilencerBand.BAND_125 to 84.0, SilencerBand.BAND_250 to 89.0,
            SilencerBand.BAND_500 to 93.0, SilencerBand.BAND_1000 to 91.0, SilencerBand.BAND_2000 to 86.0,
            SilencerBand.BAND_4000 to 80.0, SilencerBand.BAND_8000 to 74.0
        ), 96.3),
        FanNoiseData(3, "CF-4-72-8.0C", "通用", FanType.CENTRIFUGAL_FORWARD, 18.5, 20178.0, 2030.0, mapOf(
            SilencerBand.BAND_63 to 82.0, SilencerBand.BAND_125 to 90.0, SilencerBand.BAND_250 to 95.0,
            SilencerBand.BAND_500 to 99.0, SilencerBand.BAND_1000 to 97.0, SilencerBand.BAND_2000 to 92.0,
            SilencerBand.BAND_4000 to 86.0, SilencerBand.BAND_8000 to 80.0
        ), 102.3),
        // 后向离心风机
        FanNoiseData(4, "BC-9-19-5.0A", "通用", FanType.CENTRIFUGAL_BACKWARD, 7.5, 3488.0, 3250.0, mapOf(
            SilencerBand.BAND_63 to 74.0, SilencerBand.BAND_125 to 82.0, SilencerBand.BAND_250 to 87.0,
            SilencerBand.BAND_500 to 91.0, SilencerBand.BAND_1000 to 89.0, SilencerBand.BAND_2000 to 84.0,
            SilencerBand.BAND_4000 to 78.0, SilencerBand.BAND_8000 to 72.0
        ), 94.3),
        FanNoiseData(5, "BC-9-19-7.1D", "通用", FanType.CENTRIFUGAL_BACKWARD, 18.5, 8850.0, 3480.0, mapOf(
            SilencerBand.BAND_63 to 80.0, SilencerBand.BAND_125 to 88.0, SilencerBand.BAND_250 to 93.0,
            SilencerBand.BAND_500 to 97.0, SilencerBand.BAND_1000 to 95.0, SilencerBand.BAND_2000 to 90.0,
            SilencerBand.BAND_4000 to 84.0, SilencerBand.BAND_8000 to 78.0
        ), 100.3),
        // 轴流风机
        FanNoiseData(6, "AX-T35-11-5.0", "通用", FanType.AXIAL, 2.2, 8862.0, 176.0, mapOf(
            SilencerBand.BAND_63 to 68.0, SilencerBand.BAND_125 to 76.0, SilencerBand.BAND_250 to 81.0,
            SilencerBand.BAND_500 to 85.0, SilencerBand.BAND_1000 to 83.0, SilencerBand.BAND_2000 to 78.0,
            SilencerBand.BAND_4000 to 72.0, SilencerBand.BAND_8000 to 66.0
        ), 88.3),
        FanNoiseData(7, "AX-T35-11-7.1", "通用", FanType.AXIAL, 5.5, 24879.0, 208.0, mapOf(
            SilencerBand.BAND_63 to 74.0, SilencerBand.BAND_125 to 82.0, SilencerBand.BAND_250 to 87.0,
            SilencerBand.BAND_500 to 91.0, SilencerBand.BAND_1000 to 89.0, SilencerBand.BAND_2000 to 84.0,
            SilencerBand.BAND_4000 to 78.0, SilencerBand.BAND_8000 to 72.0
        ), 94.3),
        // 混流风机
        FanNoiseData(8, "MF-SWG-I-5.0", "通用", FanType.MIXED_FLOW, 3.0, 10579.0, 450.0, mapOf(
            SilencerBand.BAND_63 to 70.0, SilencerBand.BAND_125 to 78.0, SilencerBand.BAND_250 to 83.0,
            SilencerBand.BAND_500 to 87.0, SilencerBand.BAND_1000 to 85.0, SilencerBand.BAND_2000 to 80.0,
            SilencerBand.BAND_4000 to 74.0, SilencerBand.BAND_8000 to 68.0
        ), 90.3),
        FanNoiseData(9, "MF-SWG-I-7.0", "通用", FanType.MIXED_FLOW, 7.5, 31000.0, 520.0, mapOf(
            SilencerBand.BAND_63 to 76.0, SilencerBand.BAND_125 to 84.0, SilencerBand.BAND_250 to 89.0,
            SilencerBand.BAND_500 to 93.0, SilencerBand.BAND_1000 to 91.0, SilencerBand.BAND_2000 to 86.0,
            SilencerBand.BAND_4000 to 80.0, SilencerBand.BAND_8000 to 74.0
        ), 96.3)
    )
}

/**
 * Compliance verification result for silencer design.
 */
data class SilencerComplianceResult(
    val isFullyCompliant: Boolean,
    val comparisons: Map<SilencerBand, ComplianceBandResult>,
    val totalTargetDbA: Double,
    val totalActualDbA: Double,
    val overallDeficitDb: Double,
    val suggestions: List<String>
)

data class ComplianceBandResult(
    val band: SilencerBand,
    val targetIL: Double,
    val actualIL: Double,
    val isCompliant: Boolean,
    val deficitDb: Double
)

/**
 * Silencer project saved data.
 */
data class SilencerProject(
    val id: Long = 0,
    val name: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val params: SilencerParams,
    val result: InsertionLossResult? = null
)
