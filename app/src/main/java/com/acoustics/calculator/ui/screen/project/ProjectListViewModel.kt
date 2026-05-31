package com.acoustics.calculator.ui.screen.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acoustics.calculator.domain.repository.Project
import com.acoustics.calculator.domain.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProjectListUiState(
    val projects: List<Project> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ProjectListViewModel @Inject constructor(
    private val projectRepository: ProjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectListUiState())
    val uiState: StateFlow<ProjectListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            projectRepository.getAllProjects().collect { projects ->
                _uiState.update { it.copy(projects = projects, isLoading = false) }
            }
        }
    }

    fun createProject(name: String, description: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            projectRepository.createProject(
                Project(
                    id = 0,
                    name = name,
                    description = description,
                    projectType = "ROOM_ACOUSTICS",
                    createdAt = now,
                    updatedAt = now,
                    tags = emptyList()
                )
            )
        }
    }

    fun deleteProject(id: Long) {
        viewModelScope.launch {
            projectRepository.deleteProject(id)
        }
    }
}
