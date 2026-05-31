package com.acoustics.calculator.data.local.dao

import androidx.room.*
import com.acoustics.calculator.data.local.entity.StandardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StandardDao {
    @Query("SELECT * FROM standards ORDER BY standard_code, room_type")
    fun getAll(): Flow<List<StandardEntity>>

    @Query("SELECT * FROM standards WHERE id = :id")
    suspend fun getById(id: Long): StandardEntity?

    @Query("SELECT * FROM standards WHERE standard_code = :code")
    suspend fun getByCode(code: String): List<StandardEntity>

    @Query("SELECT * FROM standards WHERE category = :category ORDER BY room_type")
    fun getByCategory(category: String): Flow<List<StandardEntity>>

    @Query("SELECT * FROM standards WHERE room_type = :roomType")
    suspend fun getByRoomType(roomType: String): List<StandardEntity>

    @Query("SELECT DISTINCT standard_code FROM standards")
    fun getAllStandardCodes(): Flow<List<String>>

    @Query("SELECT DISTINCT room_type FROM standards ORDER BY room_type")
    fun getAllRoomTypes(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(standards: List<StandardEntity>)

    @Query("SELECT COUNT(*) FROM standards")
    suspend fun count(): Int
}
