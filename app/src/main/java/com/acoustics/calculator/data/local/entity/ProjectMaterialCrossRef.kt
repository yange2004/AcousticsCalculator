package com.acoustics.calculator.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "project_material_cross_ref",
    primaryKeys = ["room_id", "material_id", "surface_type"],
    foreignKeys = [
        ForeignKey(
            entity = ProjectRoomEntity::class,
            parentColumns = ["id"],
            childColumns = ["room_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MaterialEntity::class,
            parentColumns = ["id"],
            childColumns = ["material_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["room_id"]),
        Index(value = ["material_id"])
    ]
)
data class ProjectMaterialCrossRef(
    @ColumnInfo(name = "room_id") val roomId: Long,
    @ColumnInfo(name = "material_id") val materialId: Long,
    @ColumnInfo(name = "surface_type") val surfaceType: String, // CEILING, FLOOR, FRONT_WALL, etc.
    @ColumnInfo(name = "area_m2") val areaM2: Double
)
