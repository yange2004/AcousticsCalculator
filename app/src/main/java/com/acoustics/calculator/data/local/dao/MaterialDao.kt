package com.acoustics.calculator.data.local.dao

import androidx.room.*
import com.acoustics.calculator.data.local.entity.MaterialEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MaterialDao {
    @Query("SELECT * FROM materials ORDER BY name_zh")
    fun getAll(): Flow<List<MaterialEntity>>

    @Query("SELECT * FROM materials WHERE id = :id")
    suspend fun getById(id: Long): MaterialEntity?

    @Query("SELECT * FROM materials WHERE category_id = :categoryId ORDER BY name_zh")
    fun getByCategory(categoryId: Long): Flow<List<MaterialEntity>>

    @Query("""
        SELECT * FROM materials
        WHERE name_zh LIKE '%' || :query || '%' OR name_en LIKE '%' || :query || '%'
        ORDER BY name_zh
    """)
    fun search(query: String): Flow<List<MaterialEntity>>

    @Query("SELECT * FROM materials WHERE nrc BETWEEN :minNrc AND :maxNrc ORDER BY nrc DESC")
    fun filterByNrcRange(minNrc: Double, maxNrc: Double): Flow<List<MaterialEntity>>

    @Query("SELECT * FROM materials WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<MaterialEntity>

    @Query("SELECT * FROM materials WHERE is_favorite = 1 ORDER BY name_zh")
    fun getFavorites(): Flow<List<MaterialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(materials: List<MaterialEntity>)

    @Update
    suspend fun update(material: MaterialEntity)

    @Query("UPDATE materials SET is_favorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("SELECT COUNT(*) FROM materials")
    suspend fun count(): Int

    @Transaction
    suspend fun replaceAll(materials: List<MaterialEntity>) {
        deleteAll()
        insertAll(materials)
    }

    @Query("DELETE FROM materials")
    suspend fun deleteAll()
}
