package com.acoustics.calculator.ui.screen.roomacoustics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acoustics.calculator.core.constants.FrequencyBand
import com.acoustics.calculator.core.extensions.roundTo
import com.acoustics.calculator.domain.engine.*
import com.acoustics.calculator.domain.model.*
import com.acoustics.calculator.domain.repository.MaterialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoomAcousticsUiState(
    val widthM: String = "",
    val lengthM: String = "",
    val heightM: String = "",
    val selectedFormula: ReverberationFormula = ReverberationFormula.SABINE,
    val useAirAbsorption: Boolean = false,
    val availableMaterials: List<Material> = emptyList(),
    val surfaceAssignments: Map<SurfaceType, List<SurfaceAssignment>> = emptyMap(),
    val isCalculating: Boolean = false,
    val reverberationResult: ReverberationResult? = null,
    val clarityResult: ClarityResult? = null,
    val stipaResult: StipaResult? = null,
    val bassRatioResult: BassRatioResult? = null,
    val error: String? = null,
    // Material picker state
    val selectedSurfaceType: SurfaceType? = null,
    val showMaterialPicker: Boolean = false,
    val materialSearchQuery: String = ""
)

@HiltViewModel
class RoomAcousticsViewModel @Inject constructor(
    private val reverberationEngine: ReverberationEngine,
    private val clarityEngine: ClarityEngine,
    private val stipaEngine: StipaEngine,
    private val bassRatioEngine: BassRatioEngine,
    private val materialRepository: MaterialRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val projectId: Long = savedStateHandle.get<Long>("projectId") ?: -1L

    private val _uiState = MutableStateFlow(RoomAcousticsUiState())
    val uiState: StateFlow<RoomAcousticsUiState> = _uiState.asStateFlow()

    init {
        loadMaterials()
    }

    fun updateWidth(value: String) { _uiState.update { it.copy(widthM = value) } }
    fun updateLength(value: String) { _uiState.update { it.copy(lengthM = value) } }
    fun updateHeight(value: String) { _uiState.update { it.copy(heightM = value) } }
    fun selectFormula(formula: ReverberationFormula) { _uiState.update { it.copy(selectedFormula = formula) } }
    fun toggleAirAbsorption() { _uiState.update { it.copy(useAirAbsorption = !it.useAirAbsorption) } }

    fun getRoomDimensions(): RoomDimensions? {
        val state = _uiState.value
        val w = state.widthM.toDoubleOrNull() ?: return null
        val l = state.lengthM.toDoubleOrNull() ?: return null
        val h = state.heightM.toDoubleOrNull() ?: return null
        if (w <= 0 || l <= 0 || h <= 0) return null
        return RoomDimensions(w, l, h)
    }

    fun openMaterialPicker(surfaceType: SurfaceType) {
        _uiState.update { it.copy(selectedSurfaceType = surfaceType, showMaterialPicker = true, materialSearchQuery = "") }
    }

    fun closeMaterialPicker() {
        _uiState.update { it.copy(showMaterialPicker = false, selectedSurfaceType = null) }
    }

    fun updateMaterialSearch(query: String) {
        _uiState.update { it.copy(materialSearchQuery = query) }
    }

    fun assignMaterial(material: Material) {
        val state = _uiState.value
        val surfaceType = state.selectedSurfaceType ?: return
        val room = getRoomDimensions() ?: return

        // Determine default area based on surface type
        val defaultArea = when (surfaceType) {
            SurfaceType.CEILING -> room.ceilingAreaM2
            SurfaceType.FLOOR -> room.floorAreaM2
            SurfaceType.FRONT_WALL, SurfaceType.REAR_WALL -> room.frontWallAreaM2
            SurfaceType.LEFT_WALL, SurfaceType.RIGHT_WALL -> room.leftWallAreaM2
            SurfaceType.ALL_WALLS -> room.wallAreaM2
        }

        val newAssignments = state.surfaceAssignments.toMutableMap()
        val current = newAssignments[surfaceType]?.toMutableList() ?: mutableListOf()
        current.add(SurfaceAssignment(material, defaultArea))
        newAssignments[surfaceType] = current

        _uiState.update { it.copy(surfaceAssignments = newAssignments, showMaterialPicker = false) }
    }

    fun removeMaterialFromSurface(surfaceType: SurfaceType, index: Int) {
        val newAssignments = _uiState.value.surfaceAssignments.toMutableMap()
        val current = newAssignments[surfaceType]?.toMutableList() ?: return
        if (index in current.indices) {
            current.removeAt(index)
            newAssignments[surfaceType] = current
            _uiState.update { it.copy(surfaceAssignments = newAssignments) }
        }
    }

    fun calculate() {
        val room = getRoomDimensions()
        if (room == null) {
            _uiState.update { it.copy(error = "请输入有效的房间尺寸") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCalculating = true, error = null) }

            try {
                val composition = SurfaceComposition(_uiState.value.surfaceAssignments)
                val formula = _uiState.value.selectedFormula
                val useAir = _uiState.value.useAirAbsorption

                val rtResult = reverberationEngine.calculate(room, composition, formula, useAir)
                val clarityResult = clarityEngine.calculate(rtResult.rt60ByBand)
                val stipaResult = stipaEngine.calculate(clarityResult.c50Avg)
                val bassResult = bassRatioEngine.calculate(rtResult.rt60ByBand)

                _uiState.update {
                    it.copy(
                        isCalculating = false,
                        reverberationResult = rtResult,
                        clarityResult = clarityResult,
                        stipaResult = stipaResult,
                        bassRatioResult = bassResult
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isCalculating = false, error = e.message ?: "计算错误") }
            }
        }
    }

    private fun loadMaterials() {
        viewModelScope.launch {
            materialRepository.getAllMaterials().collect { materials ->
                _uiState.update { it.copy(availableMaterials = materials) }
            }
        }
    }
}
