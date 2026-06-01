package com.acoustics.calculator.domain.model

/**
 * V2.0 — Knowledge base models derived from 《实用建筑声学》
 */
data class KnowledgeCategory(
    val id: Long,
    val name: String,
    val icon: String,
    val description: String,
    val articleCount: Int,
    val color: Long = 0xFF0088FF // default blue
)

data class KnowledgeArticle(
    val id: Long,
    val categoryId: Long,
    val title: String,
    val summary: String,
    val content: String, // Markdown-formatted content
    val formulas: List<KnowledgeFormula> = emptyList(),
    val images: List<String> = emptyList(),
    val relatedStandards: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val difficulty: Difficulty = Difficulty.INTERMEDIATE
)

data class KnowledgeFormula(
    val id: Long,
    val name: String,
    val latexExpression: String, // Clean text formula for display
    val description: String,
    val variables: List<FormulaVariable> = emptyList()
)

data class FormulaVariable(
    val symbol: String,
    val name: String,
    val unit: String
)

enum class Difficulty(val label: String, val color: Long) {
    BEGINNER("入门", 0xFF00FF88),
    INTERMEDIATE("进阶", 0xFF0088FF),
    ADVANCED("高级", 0xFFFF0088),
    EXPERT("专家", 0xFFBB00FF)
}

/**
 * Design examples from 《实用建筑声学》 120+ cases
 */
data class DesignExample(
    val id: Long,
    val title: String,
    val buildingType: String,
    val location: String,
    val year: String,
    val architect: String = "",
    val description: String,
    val keyParameters: Map<String, String> = emptyMap(),
    val designHighlights: List<String> = emptyList(),
    val acousticMetrics: AcousticMetrics? = null,
    val images: List<String> = emptyList(),
    val tags: List<String> = emptyList()
)

data class AcousticMetrics(
    val rt60Mid: String? = null,
    val noiseLevel: String? = null,
    val stc: String? = null,
    val clarityC50: String? = null,
    val strengthG: String? = null,
    val volumePerSeat: String? = null
)

/**
 * Pre-populated knowledge base categories
 */
object KnowledgeBaseData {
    val categories = listOf(
        KnowledgeCategory(1, "声学基础", "🔊", "声波传播、听觉理论、频谱分析等基础知识", 12, 0xFF0088FF),
        KnowledgeCategory(2, "吸声材料", "🧱", "各类吸声材料特性、吸声系数与应用", 8, 0xFF00FF88),
        KnowledgeCategory(3, "隔声技术", "🛡️", "空气声隔声、撞击声隔声、质量定律", 10, 0xFFFF0088),
        KnowledgeCategory(4, "厅堂音质", "🏛️", "音乐厅、剧院、礼堂等厅堂音质设计", 15, 0xFFBB00FF),
        KnowledgeCategory(5, "混响设计", "🎵", "混响时间计算、赛宾/伊林公式详解", 6, 0xFFFFD700),
        KnowledgeCategory(6, "噪声控制", "🔇", "环境噪声、设备噪声、屏障设计", 9, 0xFFFF6600),
        KnowledgeCategory(7, "设计实例", "📐", "120+国内外经典建筑声学设计案例", 20, 0xFF00F5FF),
        KnowledgeCategory(8, "标准规范", "📋", "GB/T 50121、GB 50118等国标解读", 5, 0xFF39FF14),
    )

    val articles = listOf(
        KnowledgeArticle(
            id = 1, categoryId = 1,
            title = "声波的基本性质与传播",
            summary = "声波是弹性介质中机械振动的传播，具有频率、波长、声速三个基本参量",
            content = """## 声波的基本性质

### 1. 声波的定义
声波是弹性介质（空气、固体、液体）中机械振动由近及远的传播过程。在建筑声学中，我们主要关注**空气中的声波**。

### 2. 基本参量
- **频率 f**：单位时间内振动的次数，单位为赫兹（Hz）
  - 可听声范围：20 Hz ~ 20000 Hz
  - 语言频率范围：125 Hz ~ 4000 Hz
  - 音乐频率范围：30 Hz ~ 8000 Hz

- **波长 λ**：声波在传播方向上相邻同相位点之间的距离
  - λ = c / f（c为声速，f为频率）
  - 如：1000 Hz声波的波长约为 0.343 m

- **声速 c**：声波在介质中传播的速度
  - 空气中（20°C）：c = 343 m/s
  - 温度修正：c = 331.4 × √(1 + θ/273) m/s（θ为摄氏温度）

### 3. 声压级与分贝
- 声压级 SPL = 20 × lg(P/P₀) dB
- P₀ = 2×10⁻⁵ Pa（基准声压，人耳最小可听值）
- 人耳听觉范围：0 dB（阈）~ 130 dB（痛阈）

### 4. 声波的传播特性
- **反射**：声波遇到尺寸大于波长的障碍物时发生反射
- **折射**：声速在不同介质中变化导致传播方向改变
- **衍射**：声波绕过障碍物传播（低频更明显）
- **扩散**：声波在凹凸表面发生散射

### 5. 自由声场与扩散声场
- **自由声场**：无反射的理想空间，声压级按距离平方反比衰减
- **扩散声场**：声能量均匀分布、各方向传播概率相同的空间
- **室内声场** = 直达声 + 早期反射声 + 混响声
""",
            formulas = listOf(
                KnowledgeFormula(
                    id = 1, name = "声压级公式",
                    latexExpression = "Lp = 20 × lg(p/p0)",
                    description = "p为声压(Pa)，p₀=2×10⁻⁵Pa为基准声压",
                    variables = listOf(
                        FormulaVariable("L_p", "声压级", "dB"),
                        FormulaVariable("p", "声压", "Pa"),
                        FormulaVariable("p₀", "基准声压", "Pa")
                    )
                ),
                KnowledgeFormula(
                    id = 2, name = "声速温度修正",
                    latexExpression = "c = 331.4 × √(1 + θ/273)",
                    description = "θ为摄氏温度(°C)",
                    variables = listOf(
                        FormulaVariable("c", "声速", "m/s"),
                        FormulaVariable("θ", "温度", "°C")
                    )
                )
            ),
            difficulty = Difficulty.BEGINNER
        ),
        KnowledgeArticle(
            id = 2, categoryId = 1,
            title = "听觉特性与声级计权",
            summary = "人耳对中频最敏感，对低频和高频不敏感。A、B、C计权网络模拟人耳特性",
            content = """## 听觉特性与声级计权

### 1. 等响曲线
人耳对不同频率声音的敏感度不同：
- 对 1000 Hz ~ 5000 Hz 最敏感
- 对低频 (< 250 Hz) 和高频 (> 8000 Hz) 不敏感
- **等响曲线**（Fletcher-Munson曲线）描述了这一特性

### 2. 频率计权网络
| 计权 | 特性 | 应用场景 |
|------|------|----------|
| A计权 | 模拟40 phon等响曲线 | 环境噪声、职业噪声 |
| B计权 | 模拟70 phon等响曲线 | 较少使用 |
| C计权 | 近乎平坦响应 | 峰值噪声测量 |
| Z计权 | 零计权，全频平坦 | 精确声学测量 |

### 3. 噪声评价曲线
- **NR曲线**（Noise Rating）：国际标准噪声评价方法
- **NC曲线**（Noise Criteria）：美国标准
- **NR数 = L_A - 5**（近似换算）
""",
            difficulty = Difficulty.BEGINNER
        ),
        KnowledgeArticle(
            id = 3, categoryId = 5,
            title = "赛宾公式（Sabine Formula）",
            summary = "混响时间计算的经典公式，适用于平均吸声系数≤0.2的空间",
            content = """## 赛宾公式（Sabine Formula）

### 历史
1900年，美国哈佛大学物理学家 **Wallace Clement Sabine** 通过对波士顿音乐厅等大量厅堂的研究，提出了混响时间的概念和计算公式，奠定了建筑声学的科学基础。

### 公式
**T₆₀ = 0.161 × V / A**

其中：
- T₆₀：混响时间（s）
- V：房间容积（m³）
- A：总吸声量（m²），A = Σαᵢ·Sᵢ = S·ᾱ
- ᾱ：平均吸声系数
- S：室内总表面积（m²）

### 空气吸收修正
**T₆₀ = 0.161 × V / (A + 4mV)**

m为空气吸声衰减系数（Np/m），在高频（>2000Hz）和大空间中显著。

### 适用条件
- 平均吸声系数 ᾱ ≤ 0.2
- 声场为扩散声场
- 房间形状规则

### 局限性
当ᾱ > 0.2时，赛宾公式的计算结果偏大，应改用伊林公式。
""",
            formulas = listOf(
                KnowledgeFormula(
                    id = 3, name = "赛宾公式",
                    latexExpression = "T60 = 0.161 × V / A",
                    description = "A = S·ᾱ 为总吸声量",
                    variables = listOf(
                        FormulaVariable("T₆₀", "混响时间", "s"),
                        FormulaVariable("V", "房间容积", "m³"),
                        FormulaVariable("A", "总吸声量", "m²"),
                        FormulaVariable("ᾱ", "平均吸声系数", "无量纲")
                    )
                ),
                KnowledgeFormula(
                    id = 4, name = "赛宾公式（空气吸收修正）",
                    latexExpression = "T60 = 0.161 × V / (A + 4mV)",
                    description = "m为空气吸声系数，高频时重要",
                    variables = listOf(
                        FormulaVariable("m", "空气吸声系数", "Np/m")
                    )
                )
            ),
            difficulty = Difficulty.INTERMEDIATE
        ),
        KnowledgeArticle(
            id = 4, categoryId = 5,
            title = "伊林公式（Eyring Formula）",
            summary = "适用于高吸声空间的混响时间计算，是赛宾公式的扩展",
            content = """## 伊林公式（Eyring Formula）

### 原理
伊林（Eyring）在赛宾的基础上，考虑了声波在每次反射时的能量衰减，提出了更精确的混响时间计算公式。

### 公式
**T₆₀ = 0.161 × V / [-S·ln(1 - ᾱ) + 4mV]**

不考虑空气吸收时：
**T₆₀ = 0.161 × V / [-S·ln(1 - ᾱ)]**

### 适用条件
- 平均吸声系数 ᾱ > 0.2
- 适用于强吸声房间（录音室、消声室等）

### 与赛宾公式的关系
当ᾱ较小时：-ln(1 - ᾱ) ≈ ᾱ，伊林公式退化为赛宾公式
当ᾱ较大时：伊林公式结果小于赛宾公式，更精确

### 菲茨罗伊公式（Fitzroy Formula）
适用于吸声分布不均匀的情况，考虑三个轴向的衰减：
**T₆₀ = 0.161V / [-S_x·ln(1-α_x) - S_y·ln(1-α_y) - S_z·ln(1-α_z)]**
""",
            formulas = listOf(
                KnowledgeFormula(
                    id = 5, name = "伊林公式",
                    latexExpression = "T60 = 0.161 × V / [-S × ln(1-ᾱ) + 4mV]",
                    description = "ᾱ > 0.2 时使用，比赛宾公式更精确",
                    variables = listOf(
                        FormulaVariable("T₆₀", "混响时间", "s"),
                        FormulaVariable("V", "房间容积", "m³"),
                        FormulaVariable("S", "总表面积", "m²"),
                        FormulaVariable("ᾱ", "平均吸声系数", "无量纲")
                    )
                )
            ),
            difficulty = Difficulty.INTERMEDIATE
        ),
        KnowledgeArticle(
            id = 5, categoryId = 4,
            title = "音乐厅音质设计",
            summary = "音乐厅是音质要求最高的厅堂，重点在于丰满度、清晰度和空间感的平衡",
            content = """## 音乐厅音质设计

### 1. 音质评价指标
| 指标 | 符号 | 最佳范围 | 说明 |
|------|------|----------|------|
| 混响时间 | RT₆₀ | 1.6~2.1s | 交响乐1.8~2.1s，室内乐1.4~1.7s |
| 清晰度 | C₈₀ | -2~2 dB | 正值偏清晰，负值偏丰满 |
| 低音比 | LF | 1.1~1.25 | 值越大低音越丰富 |
| 强度因子 | G | 4~5.5 dB | 响度指标 |
| 侧向声能比 | LF_{80} | 0.1~0.35 | 空间感指标 |

### 2. 经典音乐厅形式
**鞋盒式（Shoebox）**
- 代表：波士顿交响音乐厅、维也纳金色大厅
- 特点：矩形平面，侧墙反射好，音质辉煌
- 最佳容积：10000~20000 m³

**葡萄园式（Vineyard）**
- 代表：柏林爱乐音乐厅、汉堡易北爱乐厅
- 特点：舞台居中，观众席环绕布置
- 优点：座位更靠近舞台，亲切感强

**扇形（Fan-shape）**
- 特点：后部宽度逐渐增大
- 注意：侧向反射声不足，需专门设计反射面

### 3. 关键设计参数
- **每座容积**：7~10 m³/座
- **观众厅高度**：与容积和宽度匹配
- **侧墙处理**：凸弧面扩散体或折线形墙面
- **舞台反射罩**：提升舞台区声能利用率

### 4. 反射声设计
- **早期反射声**：在直达声到达后20~50ms内达到，增强清晰度
- **后期反射声**：形成混响，增加丰满度
- **哈斯效应**：先到达的声音决定方向感（50ms以内）
""",
            formulas = listOf(
                KnowledgeFormula(
                    id = 6, name = "每座容积",
                    latexExpression = "V/N = 7~10 m³/座",
                    description = "音乐厅每座容积的经验范围",
                    variables = listOf(
                        FormulaVariable("V", "房间容积", "m³"),
                        FormulaVariable("N", "座位数", "座")
                    )
                )
            ),
            difficulty = Difficulty.ADVANCED
        ),
        KnowledgeArticle(
            id = 6, categoryId = 3,
            title = "质量定律与隔声设计",
            summary = "墙体隔声量主要取决于面密度，每倍面密度增加6 dB隔声量",
            content = """## 质量定律与隔声设计

### 1. 质量定律（Mass Law）
隔声量（声透射损失）R 主要取决于墙体的面密度：

**R = 20·lg(f·m) - 47 (dB)**

其中：
- m：面密度（kg/m²）
- f：频率（Hz）

**工程简化**：R ≈ 16·lg(m) + 8（500Hz时的近似值）

### 2. 吻合效应（Coincidence Effect）
当声波入射频率与墙体的弯曲波固有频率一致时，隔声量急剧下降。
- 临界频率 f_c = c²/(1.8·h·c_L)
- 双层墙可有效抑制吻合效应

### 3. 双层墙隔声
双层墙 + 空气层（带吸声材料）：
**R_total ≈ R₁ + R₂ + ΔR**
- ΔR ≈ 6~12 dB（空气层）
- ΔR ≈ 8~15 dB（填入吸声材料后）

### 4. STC等级（Sound Transmission Class）
| STC | 评价 | 说明 |
|-----|------|------|
| 25~30 | 差 | 可听到正常对话 |
| 30~35 | 较差 | 大声说话可懂 |
| 35~40 | 一般 | 大声说话可闻但不可懂 |
| 40~50 | 好 | 正常说话不可闻 |
| 50~60 | 很好 | 大声说话几乎不可闻 |
| 60+ | 极佳 | 所有声音基本隔绝 |

### 5. 隔声设计要点
- **缝隙处理**：缝隙是隔声的薄弱环节（10%的缝隙降低隔声量50%）
- **质量原则**：增加面密度是提高隔声量的基本方法
- **分离式结构**：双层墙、浮筑楼板、弹性连接
- **管道隔声**：管道包扎、弹性吊架
""",
            formulas = listOf(
                KnowledgeFormula(
                    id = 7, name = "质量定律",
                    latexExpression = "R = 20 × lg(f × m) - 47",
                    description = "f为频率(Hz)，m为面密度(kg/m²)，R为隔声量(dB)",
                    variables = listOf(
                        FormulaVariable("R", "隔声量", "dB"),
                        FormulaVariable("f", "频率", "Hz"),
                        FormulaVariable("m", "面密度", "kg/m²")
                    )
                )
            ),
            difficulty = Difficulty.INTERMEDIATE
        ),
        KnowledgeArticle(
            id = 7, categoryId = 6,
            title = "消声器设计原理",
            summary = "消声器分阻性、抗性、复合三种类型，分别针对不同频段的噪声",
            content = """## 消声器设计原理

### 1. 阻性消声器
利用吸声材料（多孔材料）吸收声能：
- **中高频**消声效果好
- **消声量**：ΔL = φ(α)·P·L/S
  - φ(α)：消声系数（与吸声系数有关）
  - P：通道截面周长
  - L：消声器有效长度
  - S：通道截面积

### 2. 抗性消声器
利用截面突变使声波反射：
- **低频**消声效果好
- **扩张比**：m = S₂/S₁（截面越大，低频消声量越大）
- **最大消声频率**：f_max = (2n+1)·c/4L

### 3. 微穿孔板消声器
- 透明、耐高温、无纤维脱落
- 适用于洁净环境
- 穿孔率 1~3%，孔径 0.5~1mm

### 4. 复合消声器
- 阻性段 + 抗性段串联
- 实现全频段消声
- 广泛应用于空调通风系统
""",
            difficulty = Difficulty.INTERMEDIATE
        ),
        KnowledgeArticle(
            id = 8, categoryId = 7,
            title = "北京国家大剧院声学设计",
            summary = "国家大剧院由法国保罗·安德鲁设计，包含歌剧院(2398座)、音乐厅(2019座)和戏剧场(1035座)",
            content = """## 北京国家大剧院声学设计

### 1. 基本概况
- **位置**：北京天安门广场西侧
- **设计**：保罗·安德鲁（Paul Andreu）
- **声学顾问**：马歇尔（C.A. Marshall）团队
- **建成**：2007年

### 2. 歌剧院（2398座）
- 中频混响时间：**1.45s**（满场）
- 每座容积：**7.2 m³/座**
- 背景噪声：**NR-20**
- 采用鞋盒式+马蹄形混合形式
- 侧墙采用弧面GRG扩散体

### 3. 音乐厅（2019座）
- 中频混响时间：**1.8s**（满场）
- 每座容积：**9.5 m³/座**
- 背景噪声：**NR-15**
- 葡萄园式布局，舞台居中
- 配备大型管风琴

### 4. 戏剧场（1035座）
- 中频混响时间：**1.1s**（满场）
- 可调混响范围：0.9~1.3s
- 背景噪声：**NR-20**

### 5. 关键技术措施
- 双层围护结构（\"壳中壳\"）隔振
- 浮筑楼板技术
- 低频谐振吸声结构
- 可调混响帘幕系统
""",
            difficulty = Difficulty.INTERMEDIATE
        ),
        KnowledgeArticle(
            id = 9, categoryId = 4,
            title = "剧院音质设计",
            summary = "剧院以语言清晰度为主、音乐丰满度为辅，需要可调混响设计",
            content = """## 剧院音质设计

### 1. 设计目标
- 语言清晰度优先（C₅₀ > 0 dB）
- 兼顾音乐丰满度
- 解决声聚焦、回声等音质缺陷

### 2. 推荐参数
- 中频混响时间：1.2~1.5s
- 每座容积：5~7 m³/座
- 背景噪声：NR-25
- 清晰度 D₅₀ > 0.5

### 3. 可调混响设计
**可变吸声方式：**
- 可调帘幕：拉开/闭合改变吸声量
- 旋转板：一面反射一面吸声
- 可变空腔：改变共振频率

**可变容积方式：**
- 升降吊顶
- 移动隔断
- 可调舞台罩

### 4. 典型音质缺陷及对策
| 缺陷 | 原因 | 对策 |
|------|------|------|
| 回声 | 后墙/穹顶反射 | 扩散/吸声处理 |
| 声聚焦 | 凹曲面 | 扩散处理 |
| 声影区 | 挑台过深 | 挑台下方设置反射面 |
| 颤振回声 | 平行墙面 | 倾斜墙面/设置扩散体 |
"""
        ),
        KnowledgeArticle(
            id = 10, categoryId = 2,
            title = "吸声材料分类与特性",
            summary = "多孔材料、共振吸声结构、微穿孔板三大类吸声材料及其应用",
            content = """## 吸声材料分类与特性

### 1. 多孔吸声材料
**原理**：声波进入材料内部孔隙，摩擦损耗转化为热能

**代表材料**：
- 玻璃棉、岩棉（中高频吸声好）
- 矿渣棉
- 聚氨酯泡沫
- 吸声织物/地毯

**影响因素**：
- 流阻（最佳范围：100~1000 Pa·s/m²）
- 孔隙率（>70%为佳）
- 厚度（增加厚度增强低频吸声）
- 背后空腔（增加低频吸声）

### 2. 共振吸声结构
**原理**：亥姆霍兹共振器原理

**类型**：
- **穿孔板**：板上穿孔 + 背后空腔
  - 共振频率：f₀ = c/(2π)·√(P/(L·D))
  - P为穿孔率，L为板厚+修正，D为空腔深度

- **薄膜共振**：薄膜 + 背后空腔
  - 共振频率：f₀ = 60/√(m·D)
  - m为面密度(kg/m²)，D为空腔深度(m)

- **薄板共振**：薄板 + 龙骨 + 空腔
  - 吸收低频（80~300Hz）

### 3. 微穿孔板（MPP）
- **中国著名声学家马大猷院士**提出
- 孔径 < 1mm，穿孔率 < 3%
- 无需填充纤维材料
- 耐候、洁净、透明
- 适用于游泳馆、洁净车间等

### 4. NRC（Noise Reduction Coefficient）
NRC = (α₂₅₀ + α₅₀₀ + α₁₀₀₀ + α₂₀₀₀)/4

| NRC | 评价 |
|-----|------|
| < 0.2 | 反射材料 |
| 0.2~0.4 | 一般吸声 |
| 0.4~0.7 | 良好吸声 |
| 0.7~0.9 | 优良吸声 |
| > 0.9 | 极佳吸声 |
""",
            formulas = listOf(
                KnowledgeFormula(
                    id = 8, name = "共振频率（穿孔板）",
                    latexExpression = "f0 = c/(2π) × √(P/(L×D))",
                    description = "P为穿孔率，L为有效板厚，D为空腔深度",
                    variables = listOf(
                        FormulaVariable("f₀", "共振频率", "Hz"),
                        FormulaVariable("P", "穿孔率", "%"),
                        FormulaVariable("L", "有效板厚", "m"),
                        FormulaVariable("D", "空腔深度", "m"),
                        FormulaVariable("c", "声速", "m/s")
                    )
                )
            ),
            difficulty = Difficulty.INTERMEDIATE
        ),
        KnowledgeArticle(
            id = 11, categoryId = 2,
            title = "吸声系数与测量方法",
            summary = "吸声系数是材料吸声能力的核心指标，有驻波管法和混响室法两种测量标准",
            content = """## 吸声系数与测量方法

### 1. 吸声系数定义
α = 被吸收的声能量 / 入射总声能量
- α = 0：完全反射（如光滑大理石）
- α = 1：完全吸收（如开口窗）
- 建筑声学材料 α 范围：0.01 ~ 0.99

### 2. 测量方法
**驻波管法（GB/T 18696.1）**
- 测量**法向入射**吸声系数 αₙ
- 小样品测试（直径~100mm）
- 多用于材料研发和品质控制

**混响室法（GB/T 20247）**
- 测量**无规入射**吸声系数 αₛ
- 大样品测试（10~12m²）
- 更接近实际使用条件
- 工程设计中通常使用此数据

### 3. 常用材料吸声系数（500Hz）
| 材料 | 厚度(cm) | α₅₀₀ |
|-----|---------|-------|
| 50mm玻璃棉 | 5 | 0.65~0.85 |
| 25mm玻璃棉 | 2.5 | 0.45~0.60 |
| 矿棉吸声板 | 1.5 | 0.35~0.50 |
| 木质吸声板 | 2 | 0.30~0.55 |
| 地毯（化纤） | 0.5 | 0.15~0.25 |
| 混凝土墙面 | - | 0.01~0.02 |
| 玻璃（大块） | - | 0.03~0.05 |
| 石膏板（12mm） | 1.2 | 0.06~0.10 |
| 穿孔石膏板 | 1.2 | 0.40~0.60 |
| 聚氨酯泡棉 | 5 | 0.55~0.75 |

### 4. 影响吸声系数的因素
- **厚度**：增加厚度主要增强低频吸声
- **密度**：影响流阻，存在最佳密度范围
- **背后空腔**：有效增强低频吸声（空腔=λ/4时效果最佳）
- **表面处理**：贴面或涂装会降低吸声性能
- **安装方式**：直接贴附 vs 龙骨安装
""",
            difficulty = Difficulty.BEGINNER
        ),
        KnowledgeArticle(
            id = 12, categoryId = 7,
            title = "柏林爱乐音乐厅声学分析",
            summary = "柏林爱乐音乐厅是葡萄园式布局的典范，混响时间2.0s，被公认为世界最佳音乐厅之一",
            content = """## 柏林爱乐音乐厅声学分析

### 1. 基本概况
- **位置**：德国柏林
- **设计**：汉斯·夏隆（Hans Scharoun）
- **声学顾问**：洛塔尔·克莱默（Lothar Cremer）
- **建成**：1963年
- **座位**：2440座
- **容积**：21000 m³
- **每座容积**：8.6 m³/座

### 2. 声学特色
- 中频混响时间（满场）：**2.0s**
- 开创**葡萄园式**布局先河
- 舞台位于观众厅中央
- 观众席分成多个台阶式"梯田"（葡萄园）区块
- 每个区块的栏板提供侧向反射声

### 3. 创新设计理念
- 打破传统鞋盒式布局
- 观众环绕舞台布置，缩短最大视距（<30m）
- 没有观众离舞台超过25m
- 每个座位都能获得良好的直达声

### 4. 声学评价
- 被公认为世界三大音乐厅之一
- 丰满度与清晰度的完美平衡
- 独特而卓越的空间感（Spatial Impression）
- 低音比 LF = 1.20（温暖）
"""
        ),
        KnowledgeArticle(
            id = 13, categoryId = 7,
            title = "维也纳金色大厅声学分析",
            summary = "维也纳金色大厅是鞋盒式音乐厅的巅峰之作，混响时间2.0s，金色辉煌的音质享誉世界",
            content = """## 维也纳金色大厅声学分析

### 1. 基本概况
- **正式名称**：维也纳音乐协会大厅（Großer Musikvereinssaal）
- **位置**：奥地利维也纳
- **设计**：特奥菲尔·冯·汉森（Theophil von Hansen）
- **建成**：1870年
- **座位**：1744座（站席约300）
- **容积**：约15000 m³

### 2. 声学参数
- 中频混响时间（满场）：**2.0s**
- 低频混响时间（125Hz）：**2.3s**
- 低音比 LF：**1.15**（温暖丰富）
- 侧向声能比 LF₈₀：**0.25**（极佳空间感）

### 3. 声学设计特色
- **鞋盒式矩形平面**：长宽比约2:1
- **精美雕塑与柱廊**：提供天然声扩散
- **木结构**：建筑主体为木材，共振特性优良
- **高挑空间**：层高约18m
- **厚重侧墙**：提供丰富的侧向反射声

### 4. 音质评价
- 世界顶级音乐厅标杆
- 音色温暖、丰满、辉煌
- 弦乐音色特别优美
- 每年新年音乐会在此举行
"""
        ),
        KnowledgeArticle(
            id = 14, categoryId = 8,
            title = "GB 50118-2010 民用建筑隔声设计规范",
            summary = "现行国家标准，规定了住宅、学校、医院等民用建筑的隔声要求",
            content = """## GB 50118-2010 民用建筑隔声设计规范

### 1. 住宅建筑隔声标准
| 构件 | 空气声隔声 | 撞击声隔声 |
|------|-----------|-----------|
| 分户墙 | R_w ≥ 45 dB | - |
| 分户楼板 | R_w ≥ 45 dB | L_n,w ≤ 75 dB |
| 户门 | R_w ≥ 30 dB | - |
| 临街外窗 | R_w ≥ 30 dB | - |

### 2. 学校建筑隔声标准
| 房间类型 | 空气声隔声 R_w |
|---------|---------------|
| 普通教室间 | ≥ 45 dB |
| 教室与活动室间 | ≥ 50 dB |
| 教室与室外 | ≥ 30 dB |
| 录音室等专用教室 | ≥ 50 dB |

### 3. 医院建筑隔声标准
| 房间类型 | 空气声隔声 R_w |
|---------|---------------|
| 病房之间 | ≥ 40 dB |
| 病房与公共区域 | ≥ 45 dB |
| 手术室隔声 | ≥ 45 dB |

### 4. 旅馆建筑隔声标准
| 等级 | 客房之间 | 客房与公共区域 |
|-----|---------|---------------|
| 特级 | ≥ 50 dB | ≥ 55 dB |
| 一级 | ≥ 45 dB | ≥ 50 dB |
| 二级 | ≥ 40 dB | ≥ 45 dB |
"""
        ),
        KnowledgeArticle(
            id = 15, categoryId = 8,
            title = "GB/T 50121-2005 建筑隔声评价标准",
            summary = "规定了建筑隔声性能的单值评价方法，包括空气声隔声和撞击声隔声",
            content = """## GB/T 50121-2005 建筑隔声评价标准

### 1. 空气声隔声评价
- **计权隔声量 R_w**：根据1/3倍频程隔声曲线与参考曲线比较确定
- 参考频率范围：100~3150 Hz
- 评价步骤：
  1. 测量各频段的隔声量
  2. 将参考曲线以1dB为步长移动
  3. 不利偏差之和 ≤ 32dB 时的最高参考曲线值即为 R_w

### 2. 撞击声隔声评价
- **计权标准化撞击声压级 L_n,w**
- 值与隔声性能成反比（值越低，隔声越好）
- 参考频率范围：100~3150 Hz

### 3. 频谱修正
- **C**：适应中高频为主的噪声源（如说话声、音乐）
- **C_tr**：适应中低频为主的噪声源（如交通噪声）
- 典型值：C ≈ -1~-2 dB，C_tr ≈ -5~-7 dB
- 标注格式：R_w(C;C_tr)

### 4. 示例
- 分户墙：R_w(C;C_tr) = 50(-1;-5) dB
- 表示计权隔声量50dB，对说话声约49dB，对交通噪声约45dB
"""
        ),
    )

    val designExamples = listOf(
        DesignExample(
            id = 1,
            title = "国家大剧院歌剧院声学设计",
            buildingType = "歌剧院",
            location = "北京",
            year = "2007",
            architect = "保罗·安德鲁",
            description = "中国最高水平的歌剧院声学设计，2398座，采用鞋盒式与马蹄形混合平面，通过GRG扩散体、可调混响系统等声学技术，实现中频混响时间1.45s的卓越音质。",
            keyParameters = mapOf(
                "座位数" to "2398座",
                "混响时间" to "1.45s（中频满场）",
                "每座容积" to "7.2 m³/座",
                "背景噪声" to "NR-20"
            ),
            designHighlights = listOf(
                "双层围护结构隔振（\"壳中壳\"结构）",
                "浮筑楼板技术隔绝固体传声",
                "GRG弧面扩散体促进声场扩散",
                "可调混响帘幕系统实现音质调节"
            ),
            tags = listOf("歌剧院", "北京", "国家级", "GRG扩散体")
        ),
        DesignExample(
            id = 2,
            title = "柏林爱乐音乐厅葡萄园式设计",
            buildingType = "音乐厅",
            location = "柏林",
            year = "1963",
            architect = "汉斯·夏隆",
            description = "世界首座葡萄园式音乐厅，2440座环绕舞台布置，开创声学设计新纪元。混响时间2.0s，每座容积8.6 m³，被誉为世界三大音乐厅之一。",
            keyParameters = mapOf(
                "座位数" to "2440座",
                "混响时间" to "2.0s（中频满场）",
                "每座容积" to "8.6 m³/座",
                "容积" to "21000 m³"
            ),
            designHighlights = listOf(
                "葡萄园式梯田布局，观众环绕舞台",
                "栏板提供丰富侧向反射声",
                "最大视距<30m，观众与舞台亲密接触",
                "台阶式座位区提供良好视线"
            ),
            tags = listOf("音乐厅", "柏林", "葡萄园式", "世界级")
        ),
        DesignExample(
            id = 3,
            title = "维也纳金色大厅鞋盒式音质",
            buildingType = "音乐厅",
            location = "维也纳",
            year = "1870",
            architect = "特奥菲尔·冯·汉森",
            description = "鞋盒式音乐厅的巅峰之作，金色辉煌的音质享誉世界。1744座，混响时间2.0s，低音比1.15，侧向声能比0.25，各项指标均达到极致。",
            keyParameters = mapOf(
                "座位数" to "1744座",
                "混响时间" to "2.0s（中频满场）",
                "低音比" to "1.15",
                "侧向声能比" to "0.25"
            ),
            designHighlights = listOf(
                "鞋盒式矩形平面（长宽比2:1）",
                "精美雕塑与柱廊提供天然扩散",
                "全木结构，共振特性优良",
                "高挑空间（层高18m），厚重侧墙"
            ),
            tags = listOf("音乐厅", "维也纳", "鞋盒式", "世界级")
        ),
        DesignExample(
            id = 4,
            title = "悉尼歌剧院音乐厅声学改造",
            buildingType = "音乐厅",
            location = "悉尼",
            year = "1973/2008改造",
            architect = "约恩·乌松",
            description = "悉尼歌剧院是20世纪标志性建筑，但其音乐厅原声学效果不佳。2008年进行了大规模声学改造，通过增加反射罩、调整观众席布局等措施大幅提升音质。",
            keyParameters = mapOf(
                "座位数" to "2679座",
                "混响时间" to "2.05s（改造后）",
                "清晰度C80" to "1.5 dB（改造后）",
                "背景噪声" to "NR-20"
            ),
            designHighlights = listOf(
                "增加可调舞台反射罩",
                "抬高天花板增强早期反射声",
                "优化观众席坡度改善视线与声线",
                "增加扩散体改善声场均匀度"
            ),
            tags = listOf("音乐厅", "悉尼", "地标建筑", "声学改造")
        ),
        DesignExample(
            id = 5,
            title = "广州大剧院声学设计",
            buildingType = "歌剧院",
            location = "广州",
            year = "2010",
            architect = "扎哈·哈迪德",
            description = "扎哈·哈迪德设计的地标建筑，1804座歌剧厅。声学设计采用\"双手环抱\"概念，通过不规则曲面造型实现丰富声反射。中频混响时间1.5s，NR-25背景噪声。",
            keyParameters = mapOf(
                "座位数" to "1804座",
                "混响时间" to "1.5s（中频满场）",
                "每座容积" to "7.8 m³/座",
                "背景噪声" to "NR-25"
            ),
            designHighlights = listOf(
                "不规则曲面侧墙实现声扩散",
                "\"双手环抱\"概念增强空间感",
                "GRG铸造声反射面",
                "浮筑楼板系统隔绝振动"
            ),
            tags = listOf("歌剧院", "广州", "扎哈·哈迪德", "GRG")
        ),
        DesignExample(
            id = 6,
            title = "中山音乐堂声学设计",
            buildingType = "音乐厅",
            location = "北京",
            year = "1999",
            architect = "项端新（声学顾问）",
            description = "《实用建筑声学》作者项端新先生参与设计的典型音乐厅。该音乐厅在故宫旁，1400座，通过计算机声学模拟优化设计方案，实现了1.7s的理想混响时间。",
            keyParameters = mapOf(
                "座位数" to "1400座",
                "混响时间" to "1.7s（中频满场）",
                "每座容积" to "8.2 m³/座",
                "容积" to "11500 m³"
            ),
            designHighlights = listOf(
                "计算机声学模拟辅助设计",
                "凸弧面扩散墙面",
                "舞台反声罩增强早期反射",
                "可调混响幕帘系统"
            ),
            tags = listOf("音乐厅", "北京", "项端新", "计算机模拟")
        ),
        DesignExample(
            id = 7,
            title = "上海交响乐团音乐厅声学设计",
            buildingType = "音乐厅",
            location = "上海",
            year = "2014",
            architect = "矶崎新",
            description = "国内首座全浮筑结构音乐厅（\"房中房\"），采用葡萄园式布局，1200座。通过在20根弹簧隔振器上建造来实现极致隔声。混响时间2.0s，背景噪声达到NR-15的顶尖水平。",
            keyParameters = mapOf(
                "座位数" to "1200座",
                "混响时间" to "2.0s（中频满场）",
                "每座容积" to "10 m³/座",
                "背景噪声" to "NR-15"
            ),
            designHighlights = listOf(
                "全浮筑结构（\"房中房\"）隔振",
                "20根大型弹簧隔振器",
                "葡萄园式环绕布局",
                "可调混响系统（1.6~2.0s）"
            ),
            tags = listOf("音乐厅", "上海", "浮筑结构", "葡萄园式")
        ),
        DesignExample(
            id = 8,
            title = "琴房隔声设计——集中式琴房楼",
            buildingType = "琴房/教育",
            location = "全国",
            year = "1990s",
            description = "《实用建筑声学》重点案例：集中式琴房楼的隔声设计。采用双层墙、浮筑楼板、弹性连接等技术，实现钢琴声在相邻琴房的隔声量达R_w=50dB以上。",
            keyParameters = mapOf(
                "隔声量" to "R_w ≥ 50 dB",
                "楼板撞击声" to "L_n,w ≤ 65 dB",
                "室内噪声" to "NR-25"
            ),
            designHighlights = listOf(
                "双层墙（240砖墙+100空气层+120砖墙）",
                "浮筑楼板（50mm玻璃棉+80mm混凝土）",
                "弹性连接（管道弹性吊架）",
                "错缝布置门和观察窗"
            ),
            tags = listOf("琴房", "隔声", "教育建筑", "噪声控制")
        ),
        DesignExample(
            id = 9,
            title = "多功能剧院可调混响设计",
            buildingType = "剧院",
            location = "全国",
            year = "2000s",
            description = "多功能剧院的声学设计核心是可调混响系统。通过电动升降帘幕、旋转吸声/反射板、可变空腔等技术，实现混响时间在1.0~1.8s之间调节，适应话剧、歌剧、音乐会等不同演出需求。",
            keyParameters = mapOf(
                "可变混响范围" to "1.0~1.8s",
                "座位数" to "800~1500座",
                "每座容积" to "6~7 m³/座",
                "背景噪声" to "NR-25"
            ),
            designHighlights = listOf(
                "电动升降可调帘幕系统",
                "旋转式吸声/反射复合面板",
                "舞台声反射罩（提升声能10~15%）",
                "计算机控制系统实现一键切换"
            ),
            tags = listOf("剧院", "可调混响", "多功能")
        ),
        DesignExample(
            id = 10,
            title = "体育馆声学设计——综合吸声方案",
            buildingType = "体育馆",
            location = "全国",
            year = "2000s",
            description = "体育馆声学设计重点是控制混响时间、消除回声。采用空间吸声体（悬挂吸声板）+ 墙面吸声方案，可将5000座体育馆的混响时间从4-5s降至2.0s以下。",
            keyParameters = mapOf(
                "混响时间" to "< 2.0s",
                "容积" to "30000~50000 m³",
                "座位数" to "3000~8000座",
                "语言清晰度STI" to "> 0.5"
            ),
            designHighlights = listOf(
                "空间吸声体（悬挂式）",
                "墙面吸声处理（穿孔板+玻璃棉）",
                "观众席座椅吸声",
                "消除多重回声和颤动回声"
            ),
            tags = listOf("体育馆", "空间吸声体", "混响控制")
        ),
        DesignExample(
            id = 11,
            title = "录音控制室声学设计",
            buildingType = "录音/播音",
            location = "全国",
            year = "2000s",
            description = "录音控制室采用\"LEDE\"（Live End Dead End）设计理念。前端为强吸声（录音师位置），后端为扩散反射面。中频混响时间0.3~0.4s，背景噪声NR-15。",
            keyParameters = mapOf(
                "混响时间" to "0.3~0.4s",
                "背景噪声" to "NR-15",
                "频率响应" to "±3dB (63Hz~8kHz)",
                "STC" to "≥ 60"
            ),
            designHighlights = listOf(
                "LEDE（Live End Dead End）设计",
                "强吸声前端 + 扩散后端",
                "浮筑隔声结构",
                "低频陷阱控制驻波"
            ),
            tags = listOf("录音室", "控制室", "LEDE", "隔声")
        ),
        DesignExample(
            id = 12,
            title = "住宅楼板撞击声隔声——浮筑楼板方案",
            buildingType = "住宅",
            location = "全国",
            year = "2000s",
            description = "住宅楼板撞击声隔声的典型方案：浮筑楼板（混凝土垫层+弹性隔声垫+钢筋混凝土楼板）。可有效降低撞击声压级15~25dB，满足GB 50118要求L_n,w ≤ 75dB。",
            keyParameters = mapOf(
                "撞击声改善量" to "ΔL_w = 15~25 dB",
                "空气声隔声" to "R_w ≥ 50 dB",
                "弹性垫层厚度" to "5~10mm"
            ),
            designHighlights = listOf(
                "浮筑楼板（浮动式构造）",
                "弹性隔声垫（橡胶/软木/聚氨酯）",
                "墙脚切缝处理防止声桥",
                "管道穿楼板弹性密封"
            ),
            tags = listOf("住宅", "浮筑楼板", "撞击声隔声")
        ),
    )
}
