package com.acoustics.calculator.data.local.dao

import androidx.room.*
import com.acoustics.calculator.data.local.entity.SilencerProjectEntity
import com.acoustics.calculator.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SilencerDao {
    @Query("SELECT * FROM silencer_projects ORDER BY updated_at DESC")
    fun getAll(): Flow<List<SilencerProjectEntity>>

    @Query("SELECT * FROM silencer_projects WHERE id = :id")
    suspend fun getById(id: Long): SilencerProjectEntity?

    @Query("SELECT * FROM silencer_projects WHERE silencer_type = :type ORDER BY updated_at DESC")
    fun getByType(type: String): Flow<List<SilencerProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: SilencerProjectEntity): Long

    @Update
    suspend fun update(project: SilencerProjectEntity)

    @Delete
    suspend fun delete(project: SilencerProjectEntity)

    @Query("DELETE FROM silencer_projects WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM silencer_projects")
    suspend fun count(): Int
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun findByPhone(phone: String): UserEntity?

    @Query("SELECT * FROM users WHERE is_logged_in = 1 LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity): Long

    @Update
    suspend fun update(user: UserEntity)

    @Query("UPDATE users SET is_logged_in = 0")
    suspend fun logoutAll()

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int
}
