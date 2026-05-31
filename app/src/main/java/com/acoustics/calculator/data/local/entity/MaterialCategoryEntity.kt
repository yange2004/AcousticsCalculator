package com.acoustics.calculator.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "material_categories")
data class MaterialCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "name_zh") val nameZh: String,
    @ColumnInfo(name = "name_en") val nameEn: String,
    @ColumnInfo(name = "description") val description: String = "",
    @ColumnInfo(name = "parent_id") val parentId: Long? = null,
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0
)
