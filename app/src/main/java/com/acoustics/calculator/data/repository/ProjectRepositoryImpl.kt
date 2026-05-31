package com.acoustics.calculator.data.repository

import com.acoustics.calculator.data.local.dao.ProjectDao
import com.acoustics.calculator.domain.repository.Project
import com.acoustics.calculator.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepositoryImpl @Inject constructor(
    private val projectDao: ProjectDao
) : ProjectRepository {

    override fun getAllProjects(): Flow<List<Project>> =
        projectDao.getAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getProjectById(id: Long): Project? =
        projectDao.getById(id)?.toDomain()

    override fun searchProjects(query: String): Flow<List<Project>> =
        projectDao.search(query).map { entities -> entities.map { it.toDomain() } }

    override suspend fun createProject(project: Project): Long {
        val entity = project.toEntity()
        return projectDao.insert(entity)
    }

    override suspend fun updateProject(project: Project) {
        projectDao.update(project.toEntity())
    }

    override suspend fun deleteProject(id: Long) {
        projectDao.deleteById(id)
    }
}
