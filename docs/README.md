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

### 改造实施

| 文档 | 说明 | 状态 |
|------|------|------|
| [08-改造方案.md](./08-改造方案.md) | **架构演进：单体 → 14 模块 → 教育平台** | ✅ |
| [09-模块拆分指南.md](./09-模块拆分指南.md) | **Phase 1 实操：实际模块结构 + 包名映射 + 依赖关系** | ✅ |
| [10-教育域开发指南.md](./10-教育域开发指南.md) | Phase 2-4 实操：Knowledge/Education 模块 + Agent 对接 | ✅ |
| [11-验证检查清单.md](./11-验证检查清单.md) | 每 Phase 验收命令 + 12 项启动检查 + 常见故障速查 | ✅ |

### 路线图

| 文档 | 说明 |
|------|------|
| [roadmap.md](./roadmap.md) | V1 (MVP) → V2 (Smart) → V3 (AI) 三阶段演进 |

---

## 推荐执行顺序

```
Phase 1 ✅ — 模块拆分 (14 模块，编译通过)
     ↓
Phase 2 ✅ — 知识引擎 (shiyu-ai-knowledge, RogueMap DAG)
     ↓
Phase 3 ✅ — 教育业务域 (shiyu-ai-education)
     ↓
Phase 4 ✅ — 教育 Agent + LiteFlow + MCP
```

## 当前项目状态

| 项 | 状态 |
|----|------|
| Java 21 + Spring Boot 4.1 | ✅ |
| LangChain4j 多平台适配 (5 平台) | ✅ |
| LangGraph4j 图引擎 (12 种节点) | ✅ |
| Sa-Token 多租户 RBAC | ✅ |
| MyBatis-Flex + H2 (可切 MySQL) | ✅ |
| **模块拆分 (Phase 1)** | ✅ **14 模块编译通过** |
| **知识引擎 (Phase 2)** | ✅ **shiyu-ai-knowledge 已就绪** |
| LiteFlow 依赖声明 | ✅ 已声明 |
| MCP 配置 | ✅ 已声明 |
| **教育业务域 (Phase 3)** | ✅ **92 Java文件，教育域完整** |
| **教育 Agent (Phase 4)** | ✅ **6 Agent + 3条LiteFlow链 + MCP框架** |

## 快速开始

```bash
# 1. 拉取代码
git clone <repo>
cd shiyu-ai

# 2. 开发环境启动
cd shiyu-ai-bootstrap
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 3. 访问
# - API 文档：http://localhost:9000/doc.html
# - H2 控制台：http://localhost:9000/h2-console
```

---

## 联系与反馈

- 架构问题：参考 [01-总体架构.md](./01-总体架构.md)
- 接口规范：参考 [06-接口规范.md](./06-接口规范.md)
- 改造疑问：参考 [08-改造方案.md](./08-改造方案.md)
- 验证失败：参考 [11-验证检查清单.md](./11-验证检查清单.md)


### HTTP 请求文件

| 文件 | 说明 |
|------|------|
| [http/00-认证中心.http](./http/00-认证中心.http) | 登录/登出/Token/验证码 |
| [http/01-Agent引擎.http](./http/01-Agent引擎.http) | Agent 注册/执行/版本/Graph |
| [http/02-知识图谱.http](./http/02-知识图谱.http) | 知识点CRUD/图谱/搜索 |
| [http/03-教育业务域.http](./http/03-教育业务域.http) | 学科/教材/课程/题目/考试 |
| [http/04-教育Agent.http](./http/04-教育Agent.http) | AI 讲解/出题/组卷/复习 |
| [http/05-AI对话与模型.http](./http/05-AI对话与模型.http) | Chat/平台/模型管理 |
| [http/06-用户与权限管理.http](./http/06-用户与权限管理.http) | 用户/角色/菜单/租户 |
| [http/07-人物记录与人脉.http](./http/07-人物记录与人脉.http) | Profile/记录/时间线 |

> 使用方法：在 VS Code 中安装 **REST Client** 插件，打开任意 `.http` 文件，先执行顶部登录请求获取 Token，再调用其他接口。
