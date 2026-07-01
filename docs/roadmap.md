# Roadmap

## 1. 概述

项目分三个阶段演进，兼顾可落地性与长期扩展。**当前状态：Phase 1 + Phase 2 + Phase 3 + Phase 4 已完成 ✅，模块职责重构完成 ✅**

```
V1 (MVP)     → ✅ 模块拆分 + 知识引擎
V2 (Smart)   → 🔜 教育业务域 + 智能推荐 (Phase 3)
V3 (AI)      → ✅ 多Agent + 数字教师 + 长期记忆 (Phase 4)
重构          → ✅ 学习进度相关类迁移到 education 模块
```

## 2. V1 - MVP 阶段

### 当前进度

| 里程碑 | 状态 | 内容 |
| --- | --- | --- |
| Phase 1 — 模块拆分 | ✅ 已完成 | 单体 → 12 模块，编译通过 |
| Phase 2 — 知识引擎 | ✅ 已完成 | RogueMap 图存储 + 知识图谱 API |

### Phase 1 产出（12 模块）

```
shiyu-ai
├── shiyu-common        (公共基础)
├── shiyu-ai-model      (跨模块共用模型)
├── shiyu-ai-dal        (数据访问层)
├── shiyu-ai-core       (AI 引擎)
├── shiyu-ai-agent      (Agent 核心)
├── shiyu-ai-auth       (认证授权)
├── shiyu-ai-record     (学习记录)
├── shiyu-ai-memory     (记忆服务)
├── shiyu-ai-rag        (RAG 检索)
├── shiyu-ai-mcp        (MCP 工具)
├── shiyu-ai-knowledge  (知识图谱)
└── shiyu-ai-bootstrap  (启动入口)
```

### Phase 2 产出（知识引擎）

```
shiyu-ai-knowledge
├── GraphStore (RogueMap DAG 持久化)
├── 图算法: DFS/BFS/拓扑排序
├── 学习路径: 前置知识检测 + 路径生成
├── 能力模型: Bloom Taxonomy 六维度
├── 遗忘曲线: Ebbinghaus 1/3/7/15/30/90 天
├── REST API: 12 个端点
└── 种子数据: 10 知识点 + 12 关系边
```

### 剩余核心功能（待 V1 后续完成）

```
[1] 知识点体系
    +-- 学段 / 学科 / 教材版本        ← 需要 shiyu-ai-education
    +-- 章节 / 知识点 录入
    +-- 知识点列表 / 搜索

[2] 教材映射 (同样在 shiyu-ai-education)

[3] 课程 (同样在 shiyu-ai-education)

[4] 题库 (同样在 shiyu-ai-education)

[5] 学习记录 (shiyu-ai-record 已有)

[6] 考试 (同样在 shiyu-ai-education)

[7] AI 聊天 (shiyu-ai-core + shiyu-ai-agent 已有)

[8] 用户系统 (shiyu-ai-auth 已有)

[9] 学习计划 (基础)
```

## 3. V2 - Smart 阶段 → Phase 3 已完成

### 目标

引入知识图谱、能力模型、智能推荐、遗忘复习，从"能用"升级为"好用"。

> **注**：知识图谱、能力模型、遗忘曲线已在 Phase 2 完成✅
> Phase 3 教育业务域已完成 ✅，智能推荐待 Phase 6 实现

### 核心功能

```
[1] 知识图谱      → ✅ Phase 2 已完成
[2] 能力模型      → ✅ Phase 2 已完成
[3] 学习路径      → ✅ Phase 2 已完成
[4] 智能出题      → ✅ Phase 3 完成 (PracticeAgent + PracticeNode)
[5] 错题本        → ✅ Phase 3 完成 (WrongQuestionController)
[6] 艾宾浩斯复习  → ✅ Phase 3 完成 (ReviewScheduler + ReviewAgent)
[7] 推荐系统      → 🔜 待实现
[8] 学习分析      → ✅ Phase 3 完成 (AnalyticsController + ReportAgent)
[9] LiteFlow 工作流 → ✅ Phase 4 完成 (learningChain 完整，reviewChain/examChain 占位)
[10] RAG          → ✅ 已有
[11] Embedding    → ⏳ 待实现
```

## 4. V3 - AI 阶段 → Phase 4 已完成

### 目标

构建完整的 AI Tutor 多 Agent 系统，实现个性化教学、自动课程规划、智能出题、学习诊断和长期学习记忆。

### 核心功能

```
[1] AI Tutor 多 Agent   → ✅ Phase 4 完成 (6 个教育 Agent)
[2] LangGraph4j         → ✅ 已有 (12 种节点 + 6 个教育节点)
[3] 长期记忆            → ✅ 已有 (shiyu-ai-memory)
[4] MCP 工具            → ✅ 已有 (8 个教育工具)
[5] 数字教师            → ✅ Phase 4 完成 (TeacherAgent + TeachNode)
[6] 自适应学习          → ⏳ 待实现
[7] 成长档案            → ✅ Phase 3 完成 (ReportAgent)
[8] 知识森林            → 🔜 待定
[9] 多模态学习          → 🔜 待定
```

## 5. 关键里程碑（更新）

| 时间 | 里程碑 | 主要内容 | 状态 |
| --- | --- | --- | --- |
| M1 | 模块拆分 | 单体 → 12 模块 | ✅ |
| M2 | 知识引擎 | RogueMap + 知识图谱 API | ✅ |
| M3 | 教育业务域 | 教材/课程/题库/考试/复习 | ✅ |
| M4 | Phase 4 | 教育 Agent + LiteFlow + MCP | ✅ |
| M4.5 | 模块职责重构 | 学习状态/复习调度/遗忘曲线迁移到 education | ✅ |
| M5 | V1 上线 | MVP 可用版本 | ⏳ |
| M6 | 智能推荐 | 题目/知识点/资源推荐 | ⏳ |
| M7 | AI Tutor | 多 Agent 教学系统 | ⏳ |
