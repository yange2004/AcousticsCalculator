package com.acoustics.calculator.data.local.dao

import androidx.room.*
import com.acoustics.calculator.data.local.entity.ProjectRoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectRoomDao {
    @Query("SELECT * FROM project_rooms WHERE project_id = :projectId")
    fun getByProjectId(projectId: Long): Flow<List<ProjectRoomEntity>>

    @Query("SELECT * FROM project_rooms WHERE id = :id")
    suspend fun getById(id: Long): ProjectRoomEntity?

    @Insert
    suspend fun insert(room: ProjectRoomEntity): Long

    @Update
    suspend fun update(room: ProjectRoomEntity)

    @Delete
    suspend fun delete(room: ProjectRoomEntity)

    @Query("DELETE FROM project_rooms WHERE project_id = :projectId")
    suspend fun deleteByProjectId(projectId: Long)
}
