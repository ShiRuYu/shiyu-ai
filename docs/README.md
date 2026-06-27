# Shiyu AI Education Platform 文档目录

> 本文档是项目从 **AI Agent 单体** 演进为 **AI 平台 + K12 教育系统** 的完整开发蓝图。

## 文档索引

### 架构设计（已完成）

| 文档 | 说明 |
|------|------|
| [00-项目介绍.md](./00-项目介绍.md) | 项目背景、技术栈、选型决策 |
| [01-总体架构.md](./01-总体架构.md) | 四层架构、模块划分、调用链、DDD 分层 |
| [02-知识引擎设计.md](./02-知识引擎设计.md) | 知识图谱(DAG)、图算法、能力模型、遗忘曲线 |
| [03-AI架构设计.md](./03-AI架构设计.md) | ChatEngine、Agent 框架、LiteFlow、LangGraph4j、MCP |
| [04-教育系统设计.md](./04-教育系统设计.md) | K12 学段/学科/教材、题库、考试、复习、学习计划 |
| [05-数据库设计.md](./05-数据库设计.md) | 全部表结构 SQL、RogueMap Key 规范 |
| [06-接口规范.md](./06-接口规范.md) | REST API 全量定义(学习/题库/考试/AI Agent) |
| [07-开发规范.md](./07-开发规范.md) | DDD 分层、命名、异常处理、日志、测试规范 |

### 改造实施（待执行）

| 文档 | 说明 | 状态 |
|------|------|------|
| [08-改造方案.md](./08-改造方案.md) | **当前状态分析 → 目标架构 → 5 个 Phase 详细执行计划** | ✅ 已定稿 |
| [09-模块拆分指南.md](./09-模块拆分指南.md) | **Phase 1 实操：6 个新模块 pom + 160+ 文件迁移 + 包名映射** | ✅ 已定稿 |
| [10-教育域开发指南.md](./10-教育域开发指南.md) | Phase 2-4 实操：Knowledge/Education 模块 + Agent 对接 | ✅ 已定稿 |
| [11-验证检查清单.md](./11-验证检查清单.md) | 每 Phase 验收命令 + 12 项启动检查 + 常见故障速查 | ✅ 已定稿 |

### 路线图

| 文档 | 说明 |
|------|------|
| [roadmap.md](./roadmap.md) | V1 (MVP) → V2 (Smart) → V3 (AI) 三阶段演进 |

---

## 推荐执行顺序

```
Week 1-2: 阅读 00-07 文档，理解架构和接口
              ↓
Week 3-4: 执行 08 + 09 → Phase 1 (模块拆分)
              ↓
Week 5-8: 执行 10   → Phase 2 (知识引擎)
              ↓
Week 9-14: 执行 10  → Phase 3 (教育业务域)
              ↓
Week 15-20: 执行 10 → Phase 4 (Agent/LiteFlow/MCP)
```

**每个 Phase 结束后，用 11-验证检查清单.md 验收。**

---

## 当前项目状态

| 项 | 状态 |
|----|------|
| Java 21 + Spring Boot 4.1 + Spring AI 2.0 | ✅ 已就位 |
| LangChain4j 多平台适配 (5 平台) | ✅ 已就位 |
| LangGraph4j 图引擎 (12 种节点) | ✅ 已就位 |
| Sa-Token 多租户 RBAC | ✅ 已就位 |
| MyBatis-Flex + H2 (可切 MySQL) | ✅ 已就位 |
| LiteFlow 依赖声明 | ✅ 已声明，未实现 Chain |
| MCP 配置 | ✅ 已声明，未接入 |
| **模块拆分（Phase 1）** | 🔜 **下一步** |
| **知识引擎（Phase 2）** | ⏳ 待启动 |
| **教育业务域（Phase 3）** | ⏳ 待启动 |
| **教育 Agent（Phase 4）** | ⏳ 待启动 |

---

## 快速开始

```bash
# 1. 拉取代码
git clone <repo>
cd shiyu-ai

# 2. 开发环境启动
cd shiyu-agent
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 3. 访问
# - API 文档：http://localhost:9000/doc.html
# - H2 控制台：http://localhost:9000/h2-console
# - Agent 列表：GET /api/agent/list
```

---

## 联系与反馈

- 架构问题：参考 [01-总体架构.md](./01-总体架构.md)
- 接口规范：参考 [06-接口规范.md](./06-接口规范.md)
- 改造疑问：优先看 [08-改造方案.md](./08-改造方案.md) 的风险控制章节
- 验证失败：参考 [11-验证检查清单.md](./11-验证检查清单.md) 的常见故障速查表
