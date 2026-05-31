package com.acoustics.calculator.data.repository

import com.acoustics.calculator.data.local.dao.MaterialCategoryDao
import com.acoustics.calculator.data.local.dao.MaterialDao
import com.acoustics.calculator.domain.model.Material
import com.acoustics.calculator.domain.model.MaterialCategory
import com.acoustics.calculator.domain.repository.MaterialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaterialRepositoryImpl @Inject constructor(
    private val materialDao: MaterialDao,
    private val categoryDao: MaterialCategoryDao
) : MaterialRepository {

    override fun getAllMaterials(): Flow<List<Material>> =
        materialDao.getAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getMaterialById(id: Long): Material? =
        materialDao.getById(id)?.toDomain()

    override fun searchMaterials(query: String): Flow<List<Material>> =
        materialDao.search(query).map { entities -> entities.map { it.toDomain() } }

    override fun getMaterialsByCategory(categoryId: Long): Flow<List<Material>> =
        materialDao.getByCategory(categoryId).map { entities -> entities.map { it.toDomain() } }

    override fun getFavorites(): Flow<List<Material>> =
        materialDao.getFavorites().map { entities -> entities.map { it.toDomain() } }

    override suspend fun toggleFavorite(materialId: Long) {
        val material = materialDao.getById(materialId) ?: return
        materialDao.setFavorite(materialId, !material.isFavorite)
    }

    override fun getAllCategories(): Flow<List<MaterialCategory>> =
        categoryDao.getAll().map { entities -> entities.map { it.toDomain() } }

    override fun getTopLevelCategories(): Flow<List<MaterialCategory>> =
        categoryDao.getTopLevel().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getMaterialsByIds(ids: List<Long>): List<Material> =
        materialDao.getByIds(ids).map { it.toDomain() }
}
