package com.acoustics.calculator.ui.screen.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acoustics.calculator.domain.repository.Project
import com.acoustics.calculator.domain.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProjectDetailViewModel @Inject constructor(
    private val projectRepository: ProjectRepository
) : ViewModel() {

    private val _project = MutableStateFlow<Project?>(null)
    val project: StateFlow<Project?> = _project.asStateFlow()

    private var projectId: Long = 0

    fun loadProject(id: Long) {
        projectId = id
        viewModelScope.launch {
            _project.value = projectRepository.getProjectById(id)
        }
    }

    fun deleteProject() {
        viewModelScope.launch {
            projectRepository.deleteProject(projectId)
        }
    }
}
