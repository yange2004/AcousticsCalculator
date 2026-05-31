package com.acoustics.calculator.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.acoustics.calculator.data.local.entity.MaterialCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MaterialCategoryDao {
    @Query("SELECT * FROM material_categories ORDER BY sort_order")
    fun getAll(): Flow<List<MaterialCategoryEntity>>

    @Query("SELECT * FROM material_categories WHERE id = :id")
    suspend fun getById(id: Long): MaterialCategoryEntity?

    @Query("SELECT * FROM material_categories WHERE parent_id IS NULL ORDER BY sort_order")
    fun getTopLevel(): Flow<List<MaterialCategoryEntity>>

    @Query("SELECT * FROM material_categories WHERE parent_id = :parentId ORDER BY sort_order")
    fun getByParentId(parentId: Long): Flow<List<MaterialCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<MaterialCategoryEntity>)

    @Query("SELECT COUNT(*) FROM material_categories")
    suspend fun count(): Int
}
