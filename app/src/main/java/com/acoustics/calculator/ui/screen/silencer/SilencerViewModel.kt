package com.acoustics.calculator.ui.screen.silencer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acoustics.calculator.domain.engine.SilencerEngine
import com.acoustics.calculator.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SilencerUiState(
    // Type selection
    val selectedType: SilencerType = SilencerType.RESISTIVE,
    val showTypeInfo: Boolean = false,
    // General parameters
    val lengthM: String = "1.0",
    val crossSectionAreaM2: String = "0.25",
    val ductDiameterM: String = "0.56",
    val flowVelocityMs: String = "8.0",
    val temperatureC: String = "20.0",
    val pressureKpa: String = "101.325",
    // Resistive / Composite parameters
    val selectedMaterial: SilencerMaterial? = null,
    val materialThicknessMm: Double = 50.0,
    val showMaterialPicker: Boolean = false,
    // Reactive parameters
    val chamberCount: String = "1",
    val chamberVolumeM3: String = "0.1",
    val perforationRate: String = "0.3",
    val neckLengthMm: String = "50.0",
    val chamberLengthM: String = "0.3",
    // Material library
    val availableMaterials: List<SilencerMaterial> = SilencerMaterialLibrary.materials,
    // Custom material
    val customMaterialName: String = "",
    val customAlpha63: String = "", val customAlpha125: String = "",
    val customAlpha250: String = "", val customAlpha500: String = "",
    val customAlpha1000: String = "", val customAlpha2000: String = "",
    val customAlpha4000: String = "", val customAlpha8000: String = "",
    // Fan noise
    val availableFans: List<FanNoiseData> = FanNoiseDatabase.fans,
    val selectedFan: FanNoiseData? = null,
    val showFanPicker: Boolean = false,
    val fanSearchQuery: String = "",
    // Calculation
    val isCalculating: Boolean = false,
    val result: InsertionLossResult? = null,
    val error: String? = null,
    // Compliance verification
    val targetTotalILDbA: String = "",
    val targetIL63: String = "", val targetIL125: String = "",
    val targetIL250: String = "", val targetIL500: String = "",
    val targetIL1000: String = "", val targetIL2000: String = "",
    val targetIL4000: String = "", val targetIL8000: String = "",
    val complianceResult: SilencerComplianceResult? = null,
    // Smart recommendation
    val smartTargetIL: String = "25",
    val smartMaxPressure: String = "500",
    val smartPreferredType: String = "", // empty = all
    val smartPreferredMaterial: String = "",
    val smartMaxLength: String = "3.0",
    val isSearching: Boolean = false,
    val recommendations: List<SilencerRecommendation>? = null,
    // Project save/load
    val savedProjects: List<com.acoustics.calculator.data.local.entity.SilencerProjectEntity> = emptyList(),
    val showSaveDialog: Boolean = false,
    val projectName: String = "",
    val saveSuccess: Boolean = false
)

@HiltViewModel
class SilencerViewModel @Inject constructor(
    private val silencerEngine: SilencerEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(SilencerUiState())
    val uiState: StateFlow<SilencerUiState> = _uiState.asStateFlow()

    // --- Silencer Type ---
    fun selectType(type: SilencerType) {
        _uiState.update { it.copy(selectedType = type, result = null, complianceResult = null) }
    }
    fun toggleTypeInfo() { _uiState.update { it.copy(showTypeInfo = !it.showTypeInfo) } }

    // --- Parameter updates ---
    fun updateLength(v: String) { _uiState.update { it.copy(lengthM = v, result = null) } }
    fun updateArea(v: String) {
        _uiState.update {
            val area = v.toDoubleOrNull()
            val diam = if (area != null && area > 0) silencerEngine.areaToDiameter(area) else 0.0
            it.copy(crossSectionAreaM2 = v, ductDiameterM = if (diam > 0) "%.2f".format(diam) else "", result = null)
        }
    }
    fun updateDiameter(v: String) {
        _uiState.update {
            val diam = v.toDoubleOrNull()
            val area = if (diam != null && diam > 0) silencerEngine.diameterToArea(diam) else 0.0
            it.copy(ductDiameterM = v, crossSectionAreaM2 = if (area > 0) "%.4f".format(area) else "", result = null)
        }
    }
    fun updateVelocity(v: String) { _uiState.update { it.copy(flowVelocityMs = v, result = null) } }
    fun updateTemperature(v: String) { _uiState.update { it.copy(temperatureC = v) } }
    fun updatePressure(v: String) { _uiState.update { it.copy(pressureKpa = v) } }

    // --- Material ---
    fun selectMaterial(material: SilencerMaterial) {
        _uiState.update { it.copy(selectedMaterial = material, materialThicknessMm = material.thicknessMm, result = null) }
    }
    fun clearMaterial() { _uiState.update { it.copy(selectedMaterial = null, result = null) } }
    fun showMaterialPicker() { _uiState.update { it.copy(showMaterialPicker = true) } }
    fun hideMaterialPicker() { _uiState.update { it.copy(showMaterialPicker = false) } }

    // --- Custom material ---
    fun updateCustomName(v: String) { _uiState.update { it.copy(customMaterialName = v) } }
    fun updateCustomAlpha(band: SilencerBand, value: String) {
        _uiState.update { state ->
            when (band) {
                SilencerBand.BAND_63 -> state.copy(customAlpha63 = value)
                SilencerBand.BAND_125 -> state.copy(customAlpha125 = value)
                SilencerBand.BAND_250 -> state.copy(customAlpha250 = value)
                SilencerBand.BAND_500 -> state.copy(customAlpha500 = value)
                SilencerBand.BAND_1000 -> state.copy(customAlpha1000 = value)
                SilencerBand.BAND_2000 -> state.copy(customAlpha2000 = value)
                SilencerBand.BAND_4000 -> state.copy(customAlpha4000 = value)
                SilencerBand.BAND_8000 -> state.copy(customAlpha8000 = value)
            }
        }
    }
    fun addCustomMaterial() {
        val state = _uiState.value
        val name = state.customMaterialName.ifBlank { "自定义材料" }
        val alphas = mapOf(
            SilencerBand.BAND_63 to (state.customAlpha63.toDoubleOrNull() ?: 0.05),
            SilencerBand.BAND_125 to (state.customAlpha125.toDoubleOrNull() ?: 0.10),
            SilencerBand.BAND_250 to (state.customAlpha250.toDoubleOrNull() ?: 0.20),
            SilencerBand.BAND_500 to (state.customAlpha500.toDoubleOrNull() ?: 0.40),
            SilencerBand.BAND_1000 to (state.customAlpha1000.toDoubleOrNull() ?: 0.60),
            SilencerBand.BAND_2000 to (state.customAlpha2000.toDoubleOrNull() ?: 0.50),
            SilencerBand.BAND_4000 to (state.customAlpha4000.toDoubleOrNull() ?: 0.40),
            SilencerBand.BAND_8000 to (state.customAlpha8000.toDoubleOrNull() ?: 0.30)
        )
        val material = SilencerMaterial(
            name = name, thicknessMm = state.materialThicknessMm, absorption = alphas, isCustom = true
        )
        _uiState.update {
            it.copy(
                selectedMaterial = material,
                availableMaterials = listOf(material) + it.availableMaterials,
                showMaterialPicker = false
            )
        }
    }

    // --- Reactive params ---
    fun updateChamberCount(v: String) { _uiState.update { it.copy(chamberCount = v, result = null) } }
    fun updateChamberVolume(v: String) { _uiState.update { it.copy(chamberVolumeM3 = v, result = null) } }
    fun updatePerforationRate(v: String) { _uiState.update { it.copy(perforationRate = v, result = null) } }
    fun updateNeckLength(v: String) { _uiState.update { it.copy(neckLengthMm = v, result = null) } }
    fun updateChamberLength(v: String) { _uiState.update { it.copy(chamberLengthM = v, result = null) } }

    // --- Fan noise ---
    fun showFanPicker() { _uiState.update { it.copy(showFanPicker = true) } }
    fun hideFanPicker() { _uiState.update { it.copy(showFanPicker = false) } }
    fun selectFan(fan: FanNoiseData?) { _uiState.update { it.copy(selectedFan = fan, showFanPicker = false) } }
    fun updateFanSearch(q: String) { _uiState.update {
        it.copy(fanSearchQuery = q, availableFans = FanNoiseDatabase.fans.filter { f ->
            f.modelName.contains(q, ignoreCase = true) || f.manufacturer.contains(q, ignoreCase = true)
        })
    } }

    // --- Calculate ---
    fun calculate() {
        val state = _uiState.value
        val length = state.lengthM.toDoubleOrNull() ?: run {
            _uiState.update { it.copy(error = "请输入有效长度") }; return
        }
        val area = state.crossSectionAreaM2.toDoubleOrNull() ?: run {
            _uiState.update { it.copy(error = "请输入有效截面积") }; return
        }
        val velocity = state.flowVelocityMs.toDoubleOrNull() ?: 8.0
        val temp = state.temperatureC.toDoubleOrNull() ?: 20.0
        val press = state.pressureKpa.toDoubleOrNull() ?: 101.325

        val chamberCountVal = (state.chamberCount.toIntOrNull() ?: 1).coerceAtLeast(1)
        val chamberVol = state.chamberVolumeM3.toDoubleOrNull() ?: 0.1
        val perfRate = state.perforationRate.toDoubleOrNull() ?: 0.3
        val neckLen = state.neckLengthMm.toDoubleOrNull() ?: 50.0
        val chamberLen = state.chamberLengthM.toDoubleOrNull() ?: 0.3

        val chambers = (0 until chamberCountVal).map {
            ReactiveChamber(chamberVol, perfRate, neckLen, chamberLen)
        }

        val params = SilencerParams(
            silencerType = state.selectedType,
            lengthM = length,
            crossSectionAreaM2 = area,
            material = state.selectedMaterial,
            materialThicknessMm = state.materialThicknessMm,
            perimeterM = silencerEngine.areaToPerimeter(area),
            flowVelocityMs = velocity,
            chambers = chambers,
            chamberCount = chamberCountVal,
            temperatureC = temp,
            atmosphericPressureKpa = press,
            fanNoiseSource = state.selectedFan
        )

        _uiState.update { it.copy(isCalculating = true, error = null) }

        try {
            val result = silencerEngine.calculate(params)
            _uiState.update { it.copy(isCalculating = false, result = result) }
        } catch (e: Exception) {
            _uiState.update { it.copy(isCalculating = false, error = e.message ?: "计算出错") }
        }
    }

    // --- Compliance ---
    fun updateTargetTotal(v: String) { _uiState.update { it.copy(targetTotalILDbA = v) } }
    fun updateTargetBand(band: SilencerBand, v: String) {
        _uiState.update { state ->
            when (band) {
                SilencerBand.BAND_63 -> state.copy(targetIL63 = v)
                SilencerBand.BAND_125 -> state.copy(targetIL125 = v)
                SilencerBand.BAND_250 -> state.copy(targetIL250 = v)
                SilencerBand.BAND_500 -> state.copy(targetIL500 = v)
                SilencerBand.BAND_1000 -> state.copy(targetIL1000 = v)
                SilencerBand.BAND_2000 -> state.copy(targetIL2000 = v)
                SilencerBand.BAND_4000 -> state.copy(targetIL4000 = v)
                SilencerBand.BAND_8000 -> state.copy(targetIL8000 = v)
            }
        }
    }
    fun verifyCompliance() {
        val state = _uiState.value
        val result = state.result ?: return
        val total = state.targetTotalILDbA.toDoubleOrNull() ?: return

        val targets = mapOf(
            SilencerBand.BAND_63 to (state.targetIL63.toDoubleOrNull() ?: 0.0),
            SilencerBand.BAND_125 to (state.targetIL125.toDoubleOrNull() ?: 0.0),
            SilencerBand.BAND_250 to (state.targetIL250.toDoubleOrNull() ?: 0.0),
            SilencerBand.BAND_500 to (state.targetIL500.toDoubleOrNull() ?: 0.0),
            SilencerBand.BAND_1000 to (state.targetIL1000.toDoubleOrNull() ?: 0.0),
            SilencerBand.BAND_2000 to (state.targetIL2000.toDoubleOrNull() ?: 0.0),
            SilencerBand.BAND_4000 to (state.targetIL4000.toDoubleOrNull() ?: 0.0),
            SilencerBand.BAND_8000 to (state.targetIL8000.toDoubleOrNull() ?: 0.0)
        )

        try {
            val compliance = silencerEngine.verifyCompliance(result, targets, total)
            _uiState.update { it.copy(complianceResult = compliance) }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message) }
        }
    }

    // --- Smart recommendation ---
    fun updateSmartTarget(v: String) { _uiState.update { it.copy(smartTargetIL = v) } }
    fun updateSmartPressure(v: String) { _uiState.update { it.copy(smartMaxPressure = v) } }
    fun updateSmartType(v: String) { _uiState.update { it.copy(smartPreferredType = v) } }
    fun updateSmartMaterial(v: String) { _uiState.update { it.copy(smartPreferredMaterial = v) } }
    fun updateSmartMaxLength(v: String) { _uiState.update { it.copy(smartMaxLength = v) } }

    fun runSmartSelection() {
        val state = _uiState.value
        val target = state.smartTargetIL.toDoubleOrNull() ?: return
        val maxP = state.smartMaxPressure.toDoubleOrNull() ?: 500.0
        val maxL = state.smartMaxLength.toDoubleOrNull() ?: 3.0
        val area = state.crossSectionAreaM2.toDoubleOrNull() ?: 0.25
        val vel = state.flowVelocityMs.toDoubleOrNull() ?: 8.0

        val preferredType = when (state.smartPreferredType.uppercase()) {
            "RESISTIVE" -> SilencerType.RESISTIVE
            "REACTIVE" -> SilencerType.REACTIVE
            "COMPOSITE" -> SilencerType.COMPOSITE
            else -> null
        }

        val criteria = SilencerSelectionCriteria(
            targetInsertionLossDbA = target,
            maxAllowablePressureDropPa = maxP,
            preferredType = preferredType,
            preferredMaterial = state.smartPreferredMaterial.ifBlank { null },
            maxLengthM = maxL,
            ductAreaM2 = area,
            flowVelocityMs = vel
        )

        _uiState.update { it.copy(isSearching = true) }

        try {
            val recs = silencerEngine.smartSelect(criteria)
            _uiState.update { it.copy(isSearching = false, recommendations = recs) }
        } catch (e: Exception) {
            _uiState.update { it.copy(isSearching = false, error = e.message) }
        }
    }

    fun applyRecommendation(rec: SilencerRecommendation) {
        _uiState.update { state ->
            state.copy(
                selectedType = rec.silencerType,
                lengthM = "%.1f".format(rec.lengthM),
                selectedMaterial = rec.material,
                materialThicknessMm = rec.materialThicknessMm,
                result = null,
                complianceResult = null,
                recommendations = null
            )
        }
    }

    fun clearError() { _uiState.update { it.copy(error = null) } }
}
