package com.acoustics.calculator.domain.repository

import com.acoustics.calculator.domain.model.StandardInfo
import kotlinx.coroutines.flow.Flow

interface StandardRepository {
    fun getAllStandards(): Flow<List<StandardInfo>>
    suspend fun getById(id: Long): StandardInfo?
    suspend fun getByCode(code: String): List<StandardInfo>
    fun getByCategory(category: String): Flow<List<StandardInfo>>
    suspend fun getByRoomType(roomType: String): List<StandardInfo>
    fun getAllStandardCodes(): Flow<List<String>>
    fun getAllRoomTypes(): Flow<List<String>>
}
