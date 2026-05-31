package com.acoustics.calculator.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "silencer_projects")
data class SilencerProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "silencer_type") val silencerType: String, // RESISTIVE, REACTIVE, COMPOSITE
    @ColumnInfo(name = "length_m") val lengthM: Double,
    @ColumnInfo(name = "cross_section_area_m2") val crossSectionAreaM2: Double,
    @ColumnInfo(name = "material_name") val materialName: String? = null,
    @ColumnInfo(name = "material_thickness_mm") val materialThicknessMm: Double? = null,
    @ColumnInfo(name = "flow_velocity_ms") val flowVelocityMs: Double = 8.0,
    @ColumnInfo(name = "chambers_json") val chambersJson: String = "[]", // JSON array of chamber params
    @ColumnInfo(name = "target_il_dba") val targetILDbA: Double? = null,
    @ColumnInfo(name = "result_json") val resultJson: String? = null, // Cached result
    @ColumnInfo(name = "temperature_c") val temperatureC: Double = 20.0,
    @ColumnInfo(name = "pressure_kpa") val pressureKpa: Double = 101.325,
    @ColumnInfo(name = "fan_model") val fanModel: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        private val gson = Gson()

        fun chambersToJson(chambers: List<Map<String, Double>>): String = gson.toJson(chambers)

        fun chambersFromJson(json: String): List<Map<String, Double>> {
            return try {
                val type = object : TypeToken<List<Map<String, Double>>>() {}.type
                gson.fromJson(json, type)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "phone") val phone: String,
    @ColumnInfo(name = "nickname") val nickname: String = "",
    @ColumnInfo(name = "is_logged_in") val isLoggedIn: Boolean = false,
    @ColumnInfo(name = "last_login") val lastLogin: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)
