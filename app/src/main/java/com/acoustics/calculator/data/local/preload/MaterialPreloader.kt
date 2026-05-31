package com.acoustics.calculator.data.local.preload

import android.content.Context
import android.content.SharedPreferences
import com.acoustics.calculator.data.local.dao.MaterialCategoryDao
import com.acoustics.calculator.data.local.dao.MaterialDao
import com.acoustics.calculator.data.local.dao.StandardDao
import com.acoustics.calculator.data.local.entity.MaterialCategoryEntity
import com.acoustics.calculator.data.local.entity.MaterialEntity
import com.acoustics.calculator.data.local.entity.StandardEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaterialPreloader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val materialDao: MaterialDao,
    private val categoryDao: MaterialCategoryDao,
    private val standardDao: StandardDao
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("preload_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_DATA_VERSION = "data_version"
        private const val CURRENT_DATA_VERSION = 2
    }

    suspend fun preloadIfNeeded() = withContext(Dispatchers.IO) {
        val savedVersion = prefs.getInt(KEY_DATA_VERSION, 0)
        if (savedVersion >= CURRENT_DATA_VERSION && materialDao.count() > 0) {
            return@withContext // Already loaded
        }

        try {
            preloadCategories()
            preloadMaterials()
            preloadStandards()
            prefs.edit().putInt(KEY_DATA_VERSION, CURRENT_DATA_VERSION).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun preloadCategories() {
        if (categoryDao.count() > 0) return

        val categories = listOf(
            MaterialCategoryEntity(1, "多孔吸声材料", "Porous Absorbers", "纤维状、颗粒状、泡沫状多孔材料", null, 1),
            MaterialCategoryEntity(2, "穿孔板共振吸声结构", "Perforated Panel Resonators", "穿孔石膏板、金属穿孔板等", null, 2),
            MaterialCategoryEntity(3, "薄膜共振吸声结构", "Membrane Absorbers", "塑料薄膜、人造革等", null, 3),
            MaterialCategoryEntity(4, "空间吸声体", "Suspended Absorbers", "空间吸声体、吸声障等", null, 4),
            MaterialCategoryEntity(5, "常用建筑材料", "Common Building Materials", "混凝土、砖墙、玻璃、木材等", null, 5),
            MaterialCategoryEntity(6, "座椅与观众", "Seating and Audience", "空座椅、有观众座椅等", null, 6),
            MaterialCategoryEntity(7, "特殊吸声材料", "Special Absorbers", "微穿孔板等特殊材料", null, 7)
        )
        categoryDao.insertAll(categories)
    }

    private suspend fun preloadMaterials() {
        if (materialDao.count() > 0) return

        val materials = loadMaterialsFromJson()
        if (materials.isNotEmpty()) {
            materialDao.insertAll(materials)
        }
    }

    private fun loadMaterialsFromJson(): List<MaterialEntity> {
        return try {
            val inputStream = context.assets.open("database/materials_preload.json")
            val reader = InputStreamReader(inputStream, "UTF-8")
            val type = object : TypeToken<List<MaterialJsonDto>>() {}.type
            val dtos: List<MaterialJsonDto> = gson.fromJson(reader, type)
            reader.close()
            dtos.map { it.toEntity() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private suspend fun preloadStandards() {
        if (standardDao.count() > 0) return

        val standards = listOf(
            // ========== GB 50118-2010 民用建筑隔声设计规范 ==========
            // 住宅空气声隔声
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "STC", "住宅分户墙/分户楼板", null, null, null, 50.0, "Rw+C ≥ 50dB（现场 DnT,w+C ≥ 50dB）强制性条文"),
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "STC", "住宅分隔非居住空间楼板", null, null, null, 55.0, "Rw+Ctr ≥ 55dB（现场 DnT,w+Ctr ≥ 51dB）强制性条文"),
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "STC", "住宅高要求分户墙", null, null, null, 55.0, "高要求住宅 Rw+C ≥ 55dB"),
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "STC", "住宅外墙", null, null, null, 45.0, "Rw+Ctr ≥ 45dB"),
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "STC", "住宅分室墙", null, null, null, 35.0, "户内分室墙 Rw+C ≥ 35dB"),
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "STC", "住宅户门", null, null, null, 25.0, "户（套）门 Rw+C ≥ 25dB（建议30dB）"),
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "STC", "住宅临交通干线外窗", null, null, null, 30.0, "Rw+Ctr ≥ 30dB（强制性条文）"),
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "STC", "住宅其他外窗", null, null, null, 25.0, "Rw+Ctr ≥ 25dB（强制性条文）"),
            // 住宅撞击声隔声
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "IMPACT", "住宅楼板（低限）", null, null, null, 75.0, "撞击声 Ln,w < 75dB（现场 L'nT,w ≤ 75dB）"),
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "IMPACT", "住宅楼板（高要求）", null, null, null, 65.0, "撞击声 Ln,w < 65dB（现场 L'nT,w ≤ 65dB）"),
            // 住宅室内允许噪声
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "NOISE", "住宅卧室（高要求）", null, null, 30.0, null, "夜间 ≤ 30dB(A) 昼间 ≤ 40dB(A)"),
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "NOISE", "住宅卧室（低限）", null, null, 37.0, null, "夜间 ≤ 37dB(A) 昼间 ≤ 45dB(A)"),
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "NOISE", "住宅起居室（高要求）", null, null, 40.0, null, "昼间 ≤ 40dB(A)"),
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "NOISE", "住宅起居室（低限）", null, null, 45.0, null, "昼间 ≤ 45dB(A)"),
            // 学校隔声
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "STC", "学校教室之间隔墙", null, null, null, 45.0, "普通教室之间 Rw+C ≥ 45dB"),
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "STC", "学校噪声房间与教室之间", null, null, null, 50.0, "产生噪声房间与教学用房之间 Rw+C ≥ 50dB"),
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "IMPACT", "学校普通教室楼板", null, null, null, 75.0, "撞击声 Ln,w < 75dB"),
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "IMPACT", "学校语言/阅览室楼板", null, null, null, 65.0, "撞击声 Ln,w < 65dB"),
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "NOISE", "学校普通教室", null, null, 45.0, null, "室内允许噪声级昼间 ≤ 45dB(A)"),
            // 医院隔声
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "STC", "医院病房隔墙", null, null, null, 45.0, "病房之间 Rw+C ≥ 45dB（现场 DnT,w+C ≥ 45dB）"),
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "STC", "医院手术室隔墙", null, null, null, 50.0, "手术室与相邻房间 Rw+C ≥ 50dB"),
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "NOISE", "医院病房（高要求）", null, null, 35.0, null, "昼间 ≤ 40dB(A) 夜间 ≤ 35dB(A)"),
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "NOISE", "医院病房（低限）", null, null, 40.0, null, "昼间 ≤ 45dB(A) 夜间 ≤ 40dB(A)"),
            // 旅馆隔声（三级分级）
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "STC", "旅馆特级客房隔墙", null, null, null, 50.0, "特级：客房之间 Rw+C ≥ 50dB"),
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "STC", "旅馆一级客房隔墙", null, null, null, 45.0, "一级：客房之间 Rw+C ≥ 45dB"),
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "STC", "旅馆二级客房隔墙", null, null, null, 40.0, "二级：客房之间 Rw+C ≥ 40dB"),
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "STC", "旅馆特级客房走廊墙", null, null, null, 40.0, "特级：客房与走廊隔墙 Rw+C ≥ 40dB"),
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "STC", "旅馆一级客房走廊墙", null, null, null, 35.0, "一级：客房与走廊隔墙 Rw+C ≥ 35dB"),
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "STC", "旅馆二级客房走廊墙", null, null, null, 30.0, "二级：客房与走廊隔墙 Rw+C ≥ 30dB"),
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "NOISE", "旅馆特级客房", null, null, 25.0, null, "夜间 ≤ 25dB(A) 昼间 ≤ 35dB(A)"),
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "NOISE", "旅馆一级客房", null, null, 30.0, null, "夜间 ≤ 30dB(A) 昼间 ≤ 40dB(A)"),
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "NOISE", "旅馆二级客房", null, null, 35.0, null, "夜间 ≤ 35dB(A) 昼间 ≤ 45dB(A)"),
            // 办公隔声
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "STC", "办公会议室隔墙", null, null, null, 50.0, "会议室/办公室之间 Rw+C ≥ 50dB（高要求）"),
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "STC", "办公室隔墙（低限）", null, null, null, 45.0, "普通办公室 Rw+C ≥ 45dB"),
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "IMPACT", "办公高要求楼板", null, null, null, 65.0, "撞击声 Ln,w < 65dB"),
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "IMPACT", "办公低限楼板", null, null, null, 75.0, "撞击声 Ln,w < 75dB"),
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "NOISE", "办公室（高要求）", null, null, 40.0, null, "室内允许噪声级 ≤ 40dB(A)"),
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "NOISE", "办公室（低限）", null, null, 45.0, null, "室内允许噪声级 ≤ 45dB(A)"),
            // 商业隔声
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "STC", "商业用房间隔墙", null, null, null, 50.0, "各类商业用房之间 Rw+C ≥ 50dB"),
            // 商业吸声要求
            StandardEntity(0, "GB 50118-2010", "民用建筑隔声设计规范", "ABSORPTION", "商业大空间", null, null, null, null, "容积>400m³且人均占地<20m²应装吸声顶棚"),

            // ========== GB/T 50121-2005 建筑隔声评价标准 ==========
            // 构件空气声隔声分级（9级）
            StandardEntity(0, "GB/T 50121-2005", "建筑隔声评价标准", "GRADE", "构件空气声1级", 20.0, 25.0, null, null, "20dB ≤ Rw+Cj < 25dB"),
            StandardEntity(0, "GB/T 50121-2005", "建筑隔声评价标准", "GRADE", "构件空气声2级", 25.0, 30.0, null, null, "25dB ≤ Rw+Cj < 30dB"),
            StandardEntity(0, "GB/T 50121-2005", "建筑隔声评价标准", "GRADE", "构件空气声3级", 30.0, 35.0, null, null, "30dB ≤ Rw+Cj < 35dB"),
            StandardEntity(0, "GB/T 50121-2005", "建筑隔声评价标准", "GRADE", "构件空气声4级", 35.0, 40.0, null, null, "35dB ≤ Rw+Cj < 40dB"),
            StandardEntity(0, "GB/T 50121-2005", "建筑隔声评价标准", "GRADE", "构件空气声5级", 40.0, 45.0, null, null, "40dB ≤ Rw+Cj < 45dB"),
            StandardEntity(0, "GB/T 50121-2005", "建筑隔声评价标准", "GRADE", "构件空气声6级", 45.0, 50.0, null, null, "45dB ≤ Rw+Cj < 50dB"),
            StandardEntity(0, "GB/T 50121-2005", "建筑隔声评价标准", "GRADE", "构件空气声7级", 50.0, 55.0, null, null, "50dB ≤ Rw+Cj < 55dB"),
            StandardEntity(0, "GB/T 50121-2005", "建筑隔声评价标准", "GRADE", "构件空气声8级", 55.0, 60.0, null, null, "55dB ≤ Rw+Cj < 60dB"),
            StandardEntity(0, "GB/T 50121-2005", "建筑隔声评价标准", "GRADE", "构件空气声9级", 60.0, null, null, null, "Rw+Cj ≥ 60dB"),
            // 楼板撞击声隔声分级（8级）
            StandardEntity(0, "GB/T 50121-2005", "建筑隔声评价标准", "GRADE", "构件撞击声1级", null, 65.0, null, null, "Lpn,w < 65dB（最好）"),
            StandardEntity(0, "GB/T 50121-2005", "建筑隔声评价标准", "GRADE", "构件撞击声2级", 65.0, 70.0, null, null, "65dB ≤ Lpn,w < 70dB"),
            StandardEntity(0, "GB/T 50121-2005", "建筑隔声评价标准", "GRADE", "构件撞击声3级", 70.0, 75.0, null, null, "70dB ≤ Lpn,w < 75dB"),
            StandardEntity(0, "GB/T 50121-2005", "建筑隔声评价标准", "GRADE", "构件撞击声4级", 75.0, 80.0, null, null, "75dB ≤ Lpn,w < 80dB"),
            StandardEntity(0, "GB/T 50121-2005", "建筑隔声评价标准", "GRADE", "构件撞击声5级", 80.0, 85.0, null, null, "80dB ≤ Lpn,w < 85dB"),
            StandardEntity(0, "GB/T 50121-2005", "建筑隔声评价标准", "GRADE", "构件撞击声6级", 85.0, 90.0, null, null, "85dB ≤ Lpn,w < 90dB"),
            StandardEntity(0, "GB/T 50121-2005", "建筑隔声评价标准", "GRADE", "构件撞击声7级", 90.0, 95.0, null, null, "90dB ≤ Lpn,w < 95dB"),
            StandardEntity(0, "GB/T 50121-2005", "建筑隔声评价标准", "GRADE", "构件撞击声8级", 95.0, null, null, null, "Lpn,w ≥ 95dB（最差）"),

            // ========== GB/T 50356-2005 厅堂声学设计规范 ==========
            StandardEntity(0, "GB/T 50356-2005", "厅堂建筑声学设计规范", "RT60", "剧场（话剧）", 1.0, 1.2, 30.0, null, "话剧院满场中频混响时间"),
            StandardEntity(0, "GB/T 50356-2005", "厅堂建筑声学设计规范", "RT60", "剧场（歌剧）", 1.3, 1.6, 30.0, null, "歌剧院满场中频混响时间"),
            StandardEntity(0, "GB/T 50356-2005", "厅堂建筑声学设计规范", "RT60", "多用途厅堂", 1.0, 1.4, 35.0, null, "多功能厅满场中频混响时间"),
            StandardEntity(0, "GB/T 50356-2005", "厅堂建筑声学设计规范", "RT60", "电影院", 0.6, 1.0, 30.0, null, "电影院中频混响时间"),

            // ========== GB 3096-2008 声环境质量标准（补充夜间值）==========
            StandardEntity(0, "GB 3096-2008", "声环境质量标准", "NOISE", "0类(康复疗养)", null, null, 40.0, null, "夜间限值 ≤ 40dB(A)"),
            StandardEntity(0, "GB 3096-2008", "声环境质量标准", "NOISE", "1类(居住区)", null, null, 45.0, null, "夜间限值 ≤ 45dB(A)"),
            StandardEntity(0, "GB 3096-2008", "声环境质量标准", "NOISE", "2类(混合区)", null, null, 50.0, null, "夜间限值 ≤ 50dB(A)"),
            StandardEntity(0, "GB 3096-2008", "声环境质量标准", "NOISE", "3类(工业区)", null, null, 55.0, null, "夜间限值 ≤ 55dB(A)"),
            StandardEntity(0, "GB 3096-2008", "声环境质量标准", "NOISE", "4类(交通干线)", null, null, 60.0, null, "夜间限值 ≤ 60dB(A)"),

            // ========== GB 22337-2008 社会生活环境噪声 ==========
            StandardEntity(0, "GB 22337-2008", "社会生活环境噪声排放标准", "NOISE", "社会生活噪声排放", null, null, 55.0, null, "结构传播固定设备室内噪声排放限值"),

            // ========== GB/T 36075.2-2018 室内声学参量测量 ==========
            StandardEntity(0, "GB/T 36075.2-2018", "室内声学参量测量", "RT60", "普通房间混响时间", 0.3, 0.8, null, null, "普通房间混响时间测量方法"),

            // ========== JGJ 310-2013 教育建筑声环境 ==========
            StandardEntity(0, "JGJ 310-2013", "教育建筑声环境设计标准", "STC", "教育建筑隔声", null, null, null, 45.0, "教育建筑隔声设计行业标准"),

            // ========== GB/T 8485-2008 建筑门窗隔声 ==========
            StandardEntity(0, "GB/T 8485-2008", "建筑门窗空气声隔声分级", "GRADE", "门窗隔声1级", 20.0, 25.0, null, null, "1级"),
            StandardEntity(0, "GB/T 8485-2008", "建筑门窗空气声隔声分级", "GRADE", "门窗隔声2级", 25.0, 30.0, null, null, "2级"),
            StandardEntity(0, "GB/T 8485-2008", "建筑门窗空气声隔声分级", "GRADE", "门窗隔声3级", 30.0, 35.0, null, null, "3级"),
            StandardEntity(0, "GB/T 8485-2008", "建筑门窗空气声隔声分级", "GRADE", "门窗隔声4级", 35.0, 40.0, null, null, "4级"),
            StandardEntity(0, "GB/T 8485-2008", "建筑门窗空气声隔声分级", "GRADE", "门窗隔声5级", 40.0, 45.0, null, null, "5级"),
            StandardEntity(0, "GB/T 8485-2008", "建筑门窗空气声隔声分级", "GRADE", "门窗隔声6级", 45.0, null, null, null, "≥45dB")
        )
        standardDao.insertAll(standards)
    }

    // JSON DTO for parsing materials from JSON
    data class MaterialJsonDto(
        val nameZh: String,
        val nameEn: String,
        val categoryId: Long,
        val description: String = "",
        val densityKgm3: Double? = null,
        val thicknessMm: Double? = null,
        val absorption125: Double,
        val absorption250: Double,
        val absorption500: Double,
        val absorption1000: Double,
        val absorption2000: Double,
        val absorption4000: Double,
        val source: String = ""
    ) {
        fun toEntity(): MaterialEntity {
            val nrc = listOf(absorption250, absorption500, absorption1000, absorption2000).average()
            return MaterialEntity(
                nameZh = nameZh,
                nameEn = nameEn,
                categoryId = categoryId,
                description = description,
                densityKgm3 = densityKgm3,
                thicknessMm = thicknessMm,
                absorption125 = absorption125,
                absorption250 = absorption250,
                absorption500 = absorption500,
                absorption1000 = absorption1000,
                absorption2000 = absorption2000,
                absorption4000 = absorption4000,
                nrc = Math.round(nrc * 100.0) / 100.0,
                source = source
            )
        }
    }
}
