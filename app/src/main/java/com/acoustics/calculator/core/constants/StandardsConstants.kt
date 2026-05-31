package com.acoustics.calculator.core.constants

/**
 * Chinese national standards (GB) for architectural acoustics.
 * Contains reference RT60 values per octave band for different room types.
 *
 * Updated 2026-05-31 with latest standards information.
 */
object StandardsConstants {

    // ============ Standard Codes ============

    /** GB 50118-2010: 民用建筑隔声设计规范（目前正在修订中） */
    const val GB_50118_2010 = "GB 50118-2010"

    /** GB/T 50121-2005: 建筑隔声评价标准 */
    const val GB_T_50121_2005 = "GB/T 50121-2005"

    /** GB/T 50356-2005: 剧场、电影院和多用途厅堂建筑声学设计规范 */
    const val GB_T_50356_2005 = "GB/T 50356-2005"

    /** GB 50371-2006: 厅堂音质设计规范 */
    const val GB_50371_2006 = "GB 50371-2006"

    /** GB 3096-2008: 声环境质量标准 */
    const val GB_3096_2008 = "GB 3096-2008"

    /** GB 22337-2008: 社会生活环境噪声排放标准 */
    const val GB_22337_2008 = "GB 22337-2008"

    /** GB/T 45305.2-2025: 建筑构件隔声实验室测量 第2部分：空气声隔声（最新2025版） */
    const val GB_T_45305_2_2025 = "GB/T 45305.2-2025"

    /** GB/T 45305.4-2025: 建筑构件隔声实验室测量 第4部分：测量程序和要求（最新2025版） */
    const val GB_T_45305_4_2025 = "GB/T 45305.4-2025"

    /** GB/T 36075.2-2018: 室内声学参量测量 第2部分：普通房间混响时间 */
    const val GB_T_36075_2_2018 = "GB/T 36075.2-2018"

    /** GB/T 19889 系列标准（部分已更新为GB/T 45305系列） */
    const val GB_T_19889 = "GB/T 19889"

    // ============ Room Types ============

    enum class RoomType(val label: String) {
        CLASSROOM("教室"),
        LECTURE_HALL("讲堂/阶梯教室"),
        CONCERT_HALL("音乐厅"),
        THEATER("剧院"),
        MULTIPURPOSE_HALL("多功能厅"),
        CINEMA("电影院"),
        RECORDING_STUDIO("录音室"),
        CONFERENCE_ROOM("会议室"),
        OFFICE("办公室"),
        BEDROOM("卧室"),
        LIVING_ROOM("起居室"),
        HOSPITAL_WARD("医院病房"),
        HOTEL_ROOM("酒店客房"),
        GYMNASIUM("体育馆"),
        SWIMMING_POOL("游泳馆"),
        LIBRARY("图书馆"),
        RESTAURANT("餐厅"),
        SHOPPING_MALL("商场"),
        CHURCH("教堂"),
        KTV_ROOM("KTV包房"),
        STUDIO_ROOM("演播室")
    }

    /**
     * Optimal RT60 mid-frequency (500-1000Hz) values in seconds per room type.
     * Based on:
     * - GB/T 50356-2005 剧场、电影院和多用途厅堂建筑声学设计规范
     * - GB 50371-2006 厅堂音质设计规范
     * - ISO 3382-1 室内声学参量测量
     *
     * Low frequencies (125, 250Hz): may be 1.2~1.3× mid-frequency value
     * High frequencies (2000, 4000Hz): may be 0.9× mid-frequency value
     */
    val OPTIMAL_RT60_MID: Map<RoomType, ClosedFloatingPointRange<Double>> = mapOf(
        RoomType.CLASSROOM to (0.6..0.8),
        RoomType.LECTURE_HALL to (0.8..1.0),
        RoomType.CONCERT_HALL to (1.8..2.2),
        RoomType.THEATER to (1.0..1.4),
        RoomType.MULTIPURPOSE_HALL to (1.0..1.3),
        RoomType.CINEMA to (0.5..0.8),
        RoomType.RECORDING_STUDIO to (0.2..0.4),
        RoomType.CONFERENCE_ROOM to (0.6..0.8),
        RoomType.OFFICE to (0.5..0.7),
        RoomType.BEDROOM to (0.4..0.6),
        RoomType.LIVING_ROOM to (0.5..0.7),
        RoomType.HOSPITAL_WARD to (0.4..0.6),
        RoomType.HOTEL_ROOM to (0.4..0.6),
        RoomType.GYMNASIUM to (1.5..2.0),
        RoomType.SWIMMING_POOL to (1.5..2.5),
        RoomType.LIBRARY to (0.6..0.9),
        RoomType.RESTAURANT to (0.7..1.0),
        RoomType.SHOPPING_MALL to (0.8..1.2),
        RoomType.CHURCH to (2.0..10.0),
        RoomType.KTV_ROOM to (0.4..0.6),
        RoomType.STUDIO_ROOM to (0.3..0.5)
    )

    /**
     * GB 50118-2010 minimum STC/Rw+C values for air-borne sound insulation.
     * Updated from the 2010 standard requirements.
     */
    val MIN_STC_REQUIREMENTS: Map<String, Double> = mapOf(
        "住宅分户墙" to 50.0,
        "住宅分户楼板" to 50.0,
        "住宅户门" to 25.0,
        "住宅分室墙" to 35.0,
        "住宅外墙" to 45.0,
        "住宅临交通干线外窗" to 30.0,
        "住宅其他外窗" to 25.0,
        "旅馆特级客房隔墙" to 50.0,
        "旅馆一级客房隔墙" to 45.0,
        "旅馆二级客房隔墙" to 40.0,
        "学校教室隔墙" to 45.0,
        "学校噪声房间与教室之间" to 50.0,
        "医院病房隔墙" to 45.0,
        "办公室隔墙" to 45.0,
        "会议室隔墙" to 50.0,
        "商业用房间隔墙" to 50.0
    )

    /**
     * GB 50118-2010 maximum impact sound pressure levels (Ln,w).
     * Lower is better for impact sound.
     */
    val MAX_IMPACT_SOUND: Map<String, Double> = mapOf(
        "住宅低限" to 75.0,
        "住宅高要求" to 65.0,
        "学校普通教室" to 75.0,
        "学校语言教室" to 65.0,
        "办公低限" to 75.0,
        "办公高要求" to 65.0
    )
}
