package com.acoustics.calculator.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "standards")
data class StandardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "standard_code") val standardCode: String,
    @ColumnInfo(name = "name_zh") val nameZh: String,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "room_type") val roomType: String,
    @ColumnInfo(name = "optimal_rt60_min") val optimalRt60Min: Double? = null,
    @ColumnInfo(name = "optimal_rt60_max") val optimalRt60Max: Double? = null,
    @ColumnInfo(name = "max_noise_level_db") val maxNoiseLevelDb: Double? = null,
    @ColumnInfo(name = "min_stc") val minStc: Double? = null,
    @ColumnInfo(name = "notes") val notes: String = ""
)
