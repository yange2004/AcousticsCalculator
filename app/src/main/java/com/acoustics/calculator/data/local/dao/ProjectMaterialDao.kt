package com.acoustics.calculator.data.local.dao

import androidx.room.*
import com.acoustics.calculator.data.local.entity.ProjectMaterialCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectMaterialDao {
    @Query("SELECT * FROM project_material_cross_ref WHERE room_id = :roomId")
    fun getByRoomId(roomId: Long): Flow<List<ProjectMaterialCrossRef>>

    @Query("SELECT * FROM project_material_cross_ref WHERE room_id = :roomId AND surface_type = :surfaceType")
    fun getByRoomAndSurface(roomId: Long, surfaceType: String): Flow<List<ProjectMaterialCrossRef>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(crossRef: ProjectMaterialCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(crossRefs: List<ProjectMaterialCrossRef>)

    @Delete
    suspend fun delete(crossRef: ProjectMaterialCrossRef)

    @Query("DELETE FROM project_material_cross_ref WHERE room_id = :roomId")
    suspend fun deleteByRoomId(roomId: Long)
}
