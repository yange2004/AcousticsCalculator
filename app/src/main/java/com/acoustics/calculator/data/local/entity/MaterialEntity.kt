package com.acoustics.calculator.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "materials",
    indices = [
        Index(value = ["category_id"]),
        Index(value = ["name_zh"]),
        Index(value = ["nrc"])
    ]
)
data class MaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "name_zh") val nameZh: String,
    @ColumnInfo(name = "name_en") val nameEn: String,
    @ColumnInfo(name = "category_id") val categoryId: Long,
    @ColumnInfo(name = "description") val description: String = "",
    @ColumnInfo(name = "density_kgm3") val densityKgm3: Double? = null,
    @ColumnInfo(name = "thickness_mm") val thicknessMm: Double? = null,
    @ColumnInfo(name = "absorption_125") val absorption125: Double,
    @ColumnInfo(name = "absorption_250") val absorption250: Double,
    @ColumnInfo(name = "absorption_500") val absorption500: Double,
    @ColumnInfo(name = "absorption_1000") val absorption1000: Double,
    @ColumnInfo(name = "absorption_2000") val absorption2000: Double,
    @ColumnInfo(name = "absorption_4000") val absorption4000: Double,
    @ColumnInfo(name = "nrc") val nrc: Double,
    @ColumnInfo(name = "source") val source: String = "",
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean = false
)
