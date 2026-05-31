package com.acoustics.calculator.data.repository

import com.acoustics.calculator.core.constants.FrequencyBand
import com.acoustics.calculator.data.local.entity.*
import com.acoustics.calculator.domain.model.*
import com.acoustics.calculator.domain.repository.Project
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// --- Material Entity → Domain ---
fun MaterialEntity.toDomain(): Material = Material(
    id = id,
    nameZh = nameZh,
    nameEn = nameEn,
    categoryId = categoryId,
    description = description,
    densityKgm3 = densityKgm3,
    thicknessMm = thicknessMm,
    absorption = mapOf(
        FrequencyBand.BAND_125 to absorption125,
        FrequencyBand.BAND_250 to absorption250,
        FrequencyBand.BAND_500 to absorption500,
        FrequencyBand.BAND_1000 to absorption1000,
        FrequencyBand.BAND_2000 to absorption2000,
        FrequencyBand.BAND_4000 to absorption4000
    ),
    nrc = nrc,
    source = source,
    isFavorite = isFavorite
)

// --- MaterialCategory Entity → Domain ---
fun MaterialCategoryEntity.toDomain(): MaterialCategory = MaterialCategory(
    id = id,
    nameZh = nameZh,
    nameEn = nameEn,
    description = description,
    parentId = parentId,
    sortOrder = sortOrder
)

// --- Project Entity → Domain ---
fun ProjectEntity.toDomain(): Project {
    val tagsList: List<String> = try {
        Gson().fromJson(tags, object : TypeToken<List<String>>() {}.type)
    } catch (e: Exception) { emptyList() }

    return Project(
        id = id,
        name = name,
        description = description,
        projectType = projectType,
        createdAt = createdAt,
        updatedAt = updatedAt,
        tags = tagsList
    )
}

fun Project.toEntity(): ProjectEntity = ProjectEntity(
    id = if (id > 0) id else 0,
    name = name,
    description = description,
    projectType = projectType,
    createdAt = createdAt,
    updatedAt = updatedAt,
    tags = Gson().toJson(tags)
)

// --- Standard Entity → Domain ---
fun StandardEntity.toDomain(): StandardInfo = StandardInfo(
    id = id,
    standardCode = standardCode,
    nameZh = nameZh,
    category = category,
    roomType = roomType,
    optimalRt60Min = optimalRt60Min,
    optimalRt60Max = optimalRt60Max,
    maxNoiseLevelDb = maxNoiseLevelDb,
    minStc = minStc,
    notes = notes
)

// --- ProjectRoom Entity → Domain ---
fun ProjectRoomEntity.toDomain(): com.acoustics.calculator.domain.model.RoomDimensions =
    com.acoustics.calculator.domain.model.RoomDimensions(
        widthM = widthM,
        lengthM = lengthM,
        heightM = heightM
    )
