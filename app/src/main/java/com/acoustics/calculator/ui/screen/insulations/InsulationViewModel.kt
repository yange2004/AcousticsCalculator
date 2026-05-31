package com.acoustics.calculator.ui.screen.insulations

import androidx.lifecycle.ViewModel
import com.acoustics.calculator.domain.engine.WallStcEngine
import com.acoustics.calculator.domain.model.SoundInsulationResult
import com.acoustics.calculator.domain.model.WallLayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class InsulationUiState(
    val layers: List<WallLayer> = emptyList(),
    val cavityDepthMm: String = "",
    val hasInsulationFill: Boolean = false,
    val result: SoundInsulationResult? = null,
    val showAddLayerDialog: Boolean = false,
    val newLayerMaterial: String = "",
    val newLayerThickness: String = "",
    val newLayerDensity: String = ""
)

@HiltViewModel
class InsulationViewModel @Inject constructor(
    private val wallStcEngine: WallStcEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsulationUiState())
    val uiState: StateFlow<InsulationUiState> = _uiState.asStateFlow()

    fun showAddLayerDialog(show: Boolean) { _uiState.update { it.copy(showAddLayerDialog = show) } }
    fun updateNewLayerMaterial(v: String) { _uiState.update { it.copy(newLayerMaterial = v) } }
    fun updateNewLayerThickness(v: String) { _uiState.update { it.copy(newLayerThickness = v) } }
    fun updateNewLayerDensity(v: String) { _uiState.update { it.copy(newLayerDensity = v) } }
    fun updateCavityDepth(v: String) { _uiState.update { it.copy(cavityDepthMm = v) } }
    fun toggleInsulationFill() { _uiState.update { it.copy(hasInsulationFill = !it.hasInsulationFill) } }

    fun addLayer() {
        val s = _uiState.value
        val t = s.newLayerThickness.toDoubleOrNull() ?: return
        val d = s.newLayerDensity.toDoubleOrNull() ?: return
        val name = s.newLayerMaterial.ifBlank { "材料${s.layers.size + 1}" }
        _uiState.update {
            it.copy(
                layers = it.layers + WallLayer(name = name, material = name, thicknessMm = t, densityKgm3 = d),
                showAddLayerDialog = false,
                newLayerMaterial = "",
                newLayerThickness = "",
                newLayerDensity = ""
            )
        }
    }

    fun removeLayer(index: Int) {
        _uiState.update { it.copy(layers = it.layers.toMutableList().also { l -> l.removeAt(index) }) }
    }

    fun calculate() {
        val s = _uiState.value
        val cavityMm = s.cavityDepthMm.toDoubleOrNull()
        val result = wallStcEngine.compositeStc(s.layers, cavityMm, s.hasInsulationFill)
        _uiState.update { it.copy(result = result) }
    }
}
