package com.acoustics.calculator.domain.repository

import kotlinx.coroutines.flow.Flow

data class Project(
    val id: Long,
    val name: String,
    val description: String,
    val projectType: String,
    val createdAt: Long,
    val updatedAt: Long,
    val tags: List<String>
)

interface ProjectRepository {
    fun getAllProjects(): Flow<List<Project>>
    suspend fun getProjectById(id: Long): Project?
    fun searchProjects(query: String): Flow<List<Project>>
    suspend fun createProject(project: Project): Long
    suspend fun updateProject(project: Project)
    suspend fun deleteProject(id: Long)
}
