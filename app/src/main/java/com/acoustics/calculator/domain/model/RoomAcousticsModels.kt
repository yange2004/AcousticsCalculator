package com.acoustics.calculator.domain.model

import com.acoustics.calculator.core.constants.FrequencyBand

/**
 * Room dimensions and derived properties.
 */
data class RoomDimensions(
    val widthM: Double,
    val lengthM: Double,
    val heightM: Double
) {
    val volumeM3: Double get() = widthM * lengthM * heightM
    val totalSurfaceAreaM2: Double get() =
        2.0 * (widthM * lengthM + widthM * heightM + lengthM * heightM)
    val floorAreaM2: Double get() = widthM * lengthM
    val ceilingAreaM2: Double get() = widthM * lengthM
    val wallAreaM2: Double get() = 2.0 * heightM * (widthM + lengthM)

    /** Individual wall areas */
    val frontWallAreaM2: Double get() = widthM * heightM
    val rearWallAreaM2: Double get() = widthM * heightM
    val leftWallAreaM2: Double get() = lengthM * heightM
    val rightWallAreaM2: Double get() = lengthM * heightM

    /** Fitzroy axis surface areas */
    val sx: Double get() = 2.0 * widthM * heightM  // two walls perpendicular to X
    val sy: Double get() = 2.0 * lengthM * heightM // two walls perpendicular to Y
    val sz: Double get() = 2.0 * widthM * lengthM   // floor + ceiling
}

/**
 * Surface assignment: a material + area on a specific surface.
 */
data class SurfaceAssignment(
    val material: Material,
    val areaM2: Double
)

/**
 * Type of surface in a room.
 */
enum class SurfaceType(val label: String) {
    CEILING("天花板"),
    FLOOR("地板"),
    FRONT_WALL("前墙"),
    REAR_WALL("后墙"),
    LEFT_WALL("左墙"),
    RIGHT_WALL("右墙"),
    ALL_WALLS("所有墙面")
}

/**
 * Composition of all surfaces in a room.
 */
data class SurfaceComposition(
    val surfaces: Map<SurfaceType, List<SurfaceAssignment>> = emptyMap()
) {
    /** Get all materials assigned to a specific surface type */
    fun getAssignments(surfaceType: SurfaceType): List<SurfaceAssignment> =
        surfaces[surfaceType] ?: emptyList()

    /** Total assigned area for a surface type */
    fun getTotalArea(surfaceType: SurfaceType): Double =
        getAssignments(surfaceType).sumOf { it.areaM2 }

    /** True if all surface areas match room dimensions */
    fun isComplete(room: RoomDimensions): Boolean {
        val ceilingOk = getTotalArea(SurfaceType.CEILING).let { it == 0.0 || abs(it - room.ceilingAreaM2) < 0.01 }
        val floorOk = getTotalArea(SurfaceType.FLOOR).let { it == 0.0 || abs(it - room.floorAreaM2) < 0.01 }
        return ceilingOk && floorOk
    }
}

private fun abs(d: Double) = kotlin.math.abs(d)
