package com.acoustics.calculator.ui.screen.standards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acoustics.calculator.domain.model.StandardInfo
import com.acoustics.calculator.domain.repository.StandardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StandardsUiState(
    val allStandards: List<StandardInfo> = emptyList(),
    val filteredStandards: List<StandardInfo> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = ""
)

@HiltViewModel
class StandardsViewModel @Inject constructor(
    private val standardRepository: StandardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StandardsUiState())
    val uiState: StateFlow<StandardsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            standardRepository.getAllStandards().collect { standards ->
                _uiState.update {
                    it.copy(
                        allStandards = standards,
                        filteredStandards = filterStandards(standards, it.searchQuery),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredStandards = filterStandards(it.allStandards, query)
            )
        }
    }

    private fun filterStandards(standards: List<StandardInfo>, query: String): List<StandardInfo> {
        if (query.isBlank()) return standards
        val q = query.lowercase()
        return standards.filter { std ->
            std.standardCode.lowercase().contains(q) ||
            std.nameZh.lowercase().contains(q) ||
            std.roomType.lowercase().contains(q) ||
            std.category.lowercase().contains(q) ||
            std.notes.lowercase().contains(q)
        }
    }
}
