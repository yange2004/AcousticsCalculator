package com.acoustics.calculator.ui.screen.materials

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acoustics.calculator.domain.model.Material
import com.acoustics.calculator.domain.model.MaterialCategory
import com.acoustics.calculator.domain.repository.MaterialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MaterialListUiState(
    val searchQuery: String = "",
    val selectedCategoryId: Long? = null,
    val categories: List<MaterialCategory> = emptyList(),
    val filteredMaterials: List<Material> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class MaterialListViewModel @Inject constructor(
    private val materialRepository: MaterialRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MaterialListUiState())
    val uiState: StateFlow<MaterialListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            materialRepository.getTopLevelCategories().collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
        loadMaterials()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadMaterials() {
        viewModelScope.launch {
            combine(
                _uiState.map { it.searchQuery }.distinctUntilChanged(),
                _uiState.map { it.selectedCategoryId }.distinctUntilChanged()
            ) { query, categoryId -> query to categoryId }
            .flatMapLatest { (query, categoryId) ->
                when {
                    query.isNotBlank() -> materialRepository.searchMaterials(query)
                    categoryId != null -> materialRepository.getMaterialsByCategory(categoryId)
                    else -> materialRepository.getAllMaterials()
                }
            }
            .collect { materials ->
                _uiState.update { it.copy(filteredMaterials = materials, isLoading = false) }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectCategory(categoryId: Long?) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }
}
