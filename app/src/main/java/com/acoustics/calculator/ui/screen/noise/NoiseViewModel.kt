package com.acoustics.calculator.ui.screen.noise

import androidx.lifecycle.ViewModel
import com.acoustics.calculator.domain.engine.BarrierInsertionLossEngine
import com.acoustics.calculator.domain.engine.IndoorNoiseEngine
import com.acoustics.calculator.domain.model.BarrierResult
import com.acoustics.calculator.domain.model.NoisePredictionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class NoiseUiState(
    val sourceLevelDb: String = "90",
    val sourcePosition: IndoorNoiseEngine.SourcePosition = IndoorNoiseEngine.SourcePosition.ON_FLOOR,
    val distanceM: String = "5",
    val totalAbsorptionM2: String = "50",
    val barrierHeightM: String = "",
    val barrierDistM: String = "",
    val noiseResult: NoisePredictionResult? = null,
    val barrierResult: BarrierResult? = null
)

@HiltViewModel
class NoiseViewModel @Inject constructor(
    private val indoorNoiseEngine: IndoorNoiseEngine,
    private val barrierEngine: BarrierInsertionLossEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoiseUiState())
    val uiState: StateFlow<NoiseUiState> = _uiState.asStateFlow()

    fun updateSourceLevel(v: String) { _uiState.update { it.copy(sourceLevelDb = v) } }
    fun updateDistance(v: String) { _uiState.update { it.copy(distanceM = v) } }
    fun updateAbsorption(v: String) { _uiState.update { it.copy(totalAbsorptionM2 = v) } }
    fun updateBarrierHeight(v: String) { _uiState.update { it.copy(barrierHeightM = v) } }
    fun updateBarrierDist(v: String) { _uiState.update { it.copy(barrierDistM = v) } }
    fun selectSourcePosition(pos: IndoorNoiseEngine.SourcePosition) { _uiState.update { it.copy(sourcePosition = pos) } }

    fun calculate() {
        val s = _uiState.value
        val lw = s.sourceLevelDb.toDoubleOrNull() ?: return
        val dist = s.distanceM.toDoubleOrNull() ?: return
        val a = s.totalAbsorptionM2.toDoubleOrNull() ?: return

        val noiseResult = indoorNoiseEngine.calculate(lw, dist, a, s.sourcePosition.q, sourceType = "通用声源")
        _uiState.update { it.copy(noiseResult = noiseResult) }

        // Barrier calculation if barrier parameters provided
        val bh = s.barrierHeightM.toDoubleOrNull()
        val bd = s.barrierDistM.toDoubleOrNull()
        if (bh != null && bd != null && bh > 0) {
            val barrierResult = barrierEngine.calculate(
                sourceHeightM = 0.5, // typical source height
                receiverHeightM = 1.5, // typical ear height
                barrierHeightM = bh,
                sourceBarrierDistanceM = bd,
                receiverBarrierDistanceM = dist - bd,
                frequencyHz = 500.0 // mid-frequency
            )
            _uiState.update { it.copy(barrierResult = barrierResult) }
        }
    }
}
