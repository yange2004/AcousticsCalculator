package com.acoustics.calculator.ui.screen.materials

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acoustics.calculator.domain.model.Material
import com.acoustics.calculator.domain.repository.MaterialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MaterialDetailViewModel @Inject constructor(
    private val materialRepository: MaterialRepository
) : ViewModel() {

    private val _material = MutableStateFlow<Material?>(null)
    val material: StateFlow<Material?> = _material.asStateFlow()

    fun loadMaterial(id: Long) {
        viewModelScope.launch {
            _material.value = materialRepository.getMaterialById(id)
        }
    }

    fun toggleFavorite() {
        val mat = _material.value ?: return
        viewModelScope.launch {
            materialRepository.toggleFavorite(mat.id)
            _material.value = materialRepository.getMaterialById(mat.id)
        }
    }
}
