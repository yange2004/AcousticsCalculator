package com.acoustics.calculator.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "project_rooms",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["project_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["project_id"])]
)
data class ProjectRoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "project_id") val projectId: Long,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "width_m") val widthM: Double,
    @ColumnInfo(name = "length_m") val lengthM: Double,
    @ColumnInfo(name = "height_m") val heightM: Double,
    @ColumnInfo(name = "formula") val formula: String = "SABINE" // SABINE, EYRING, FITZROY, KNUTSEN
)
