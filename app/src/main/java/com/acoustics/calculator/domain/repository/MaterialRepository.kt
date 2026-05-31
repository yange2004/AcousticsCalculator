package com.acoustics.calculator.domain.repository

import com.acoustics.calculator.domain.model.Material
import com.acoustics.calculator.domain.model.MaterialCategory
import kotlinx.coroutines.flow.Flow

interface MaterialRepository {
    fun getAllMaterials(): Flow<List<Material>>
    suspend fun getMaterialById(id: Long): Material?
    fun searchMaterials(query: String): Flow<List<Material>>
    fun getMaterialsByCategory(categoryId: Long): Flow<List<Material>>
    fun getFavorites(): Flow<List<Material>>
    suspend fun toggleFavorite(materialId: Long)
    fun getAllCategories(): Flow<List<MaterialCategory>>
    fun getTopLevelCategories(): Flow<List<MaterialCategory>>
    suspend fun getMaterialsByIds(ids: List<Long>): List<Material>
}
