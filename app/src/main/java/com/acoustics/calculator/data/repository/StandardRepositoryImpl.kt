package com.acoustics.calculator.data.repository

import com.acoustics.calculator.data.local.dao.StandardDao
import com.acoustics.calculator.domain.model.StandardInfo
import com.acoustics.calculator.domain.repository.StandardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StandardRepositoryImpl @Inject constructor(
    private val standardDao: StandardDao
) : StandardRepository {

    override fun getAllStandards(): Flow<List<StandardInfo>> =
        standardDao.getAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getById(id: Long): StandardInfo? =
        standardDao.getById(id)?.toDomain()

    override suspend fun getByCode(code: String): List<StandardInfo> =
        standardDao.getByCode(code).map { it.toDomain() }

    override fun getByCategory(category: String): Flow<List<StandardInfo>> =
        standardDao.getByCategory(category).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getByRoomType(roomType: String): List<StandardInfo> =
        standardDao.getByRoomType(roomType).map { it.toDomain() }

    override fun getAllStandardCodes(): Flow<List<String>> =
        standardDao.getAllStandardCodes()

    override fun getAllRoomTypes(): Flow<List<String>> =
        standardDao.getAllRoomTypes()
}
