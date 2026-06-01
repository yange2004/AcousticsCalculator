package com.acoustics.calculator.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acoustics.calculator.data.local.dao.UserDao
import com.acoustics.calculator.data.local.entity.UserEntity
import com.acoustics.calculator.domain.repository.MaterialRepository
import com.acoustics.calculator.domain.repository.Project
import com.acoustics.calculator.domain.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val recentProjects: List<Project> = emptyList(),
    val materialCount: Int = 0,
    val currentUser: UserEntity? = null,
    val isLoggedIn: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val materialRepository: MaterialRepository,
    private val userDao: UserDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            projectRepository.getAllProjects().collect { projects ->
                _uiState.update { it.copy(recentProjects = projects.take(5)) }
            }
        }
        viewModelScope.launch {
            materialRepository.getAllMaterials().collect { materials ->
                _uiState.update { it.copy(materialCount = materials.size) }
            }
        }
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val user = userDao.getCurrentUser()
            _uiState.update {
                it.copy(
                    currentUser = user,
                    isLoggedIn = user != null
                )
            }
        }
    }

    fun refreshUser() {
        loadCurrentUser()
    }

    fun logout() {
        viewModelScope.launch {
            userDao.logoutAll()
            _uiState.update {
                it.copy(currentUser = null, isLoggedIn = false)
            }
        }
    }
}
