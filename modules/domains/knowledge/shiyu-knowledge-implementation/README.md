# shiyu-knowledge-implementation 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-knowledge-implementation`
- **分类**：领域模块 · Implementation

## 作用

实现租户知识空间、文档入库、知识图谱与检索/RAG 支撑，并通过知识 API 向管理界面和 Agent 等调用方提供服务。

## 功能说明

- 知识组织：维护知识空间、成员和租户归属；创建知识点、关系、路径与前置条件数据。
- 内容导入：接收文件或 URL，管理可续传上传与导入任务；解析文本、Markdown、HTML、PDF、Word 等支持的内容来源。
- 检索构建：执行清洗、分块、向量化、重排和搜索流程，将向量适配交给 common-vector，并通过模型 contract 使用嵌入/重排能力。
- 生命周期治理：支持知识审核、版本、发布与回滚，并提供审计、评估及备份相关流程。
- Web 与持久化：知识 Controller 提供知识空间、文档、点/关系与检索管理入口；MyBatis 保存领域元数据，文件存储和向量索引使用公共存储契约。H2 schema/seed 提供开发基线。
- Agent 等调用方通过 `shiyu-knowledge-contract` 查询内容，不依赖知识模块内部 Controller DTO、数据库实体或索引适配器。

## 文档入库与检索链路

1. `KnowledgeSpaceController` 和 `KnowledgeSpaceServiceImpl` 管理租户空间；文档、知识点、关系、路径 Controller 分别面向各自的业务对象，不能把空间管理权限等同于全部文档读取权限。
2. `KnowledgeDocumentUploadServiceImpl` 接收文件并建立入库任务，`DocumentIngestionService` 执行解析与切块；`EmbeddedIngestionWorker` 负责嵌入式任务处理。
3. `KnowledgeIndexService` 协调 `VectorIndex` 与 `FullTextIndex`，`EmbeddedIndexRegistry` 管理嵌入式索引；模型嵌入能力和通用向量存储由外部 contract/基础设施提供。
4. `EmbeddedKnowledgeRetrievalService` 执行知识检索，`KnowledgeContextRetrievalAdapter` 将检索结果接入 Agent 上下文。结果应保留片段来源、引用及空间/租户范围。
5. 审核、评估、任务和企业文档版本由相应服务与 Controller 管理；知识元数据的 JDBC Repository 与文件内容/向量索引分开存放。

文档上传成功、解析完成、索引可检索是不同状态。跨租户、跨空间访问必须由业务服务校验，不能只依赖向量过滤器或客户端传入的空间 ID。

## 使用前提与示例

- **前提**：知识表、文件存储、全文/向量索引和可用嵌入模型已就绪；用户必须属于允许访问的知识空间，并具有对应操作权限。上传成功后仍需等待解析和索引任务完成。
- **使用**：`GET /api/knowledge/spaces` 查询空间（`knowledge:list`），`POST /api/knowledge/spaces` 创建空间（`knowledge:create`）；随后上传文档并在 `/api/knowledge/ingestion-jobs` 跟踪任务状态。Agent 检索使用 `KnowledgeRetrievalService`，并提供可信 `ActorContext` 和可访问空间列表。
- **注意**：空间、文档、索引可能处于不同生命周期状态；重新选择向量 provider 或嵌入维度后需要重建索引。检索命中还需按空间和对象归属过滤。

## 边界

空间成员和租户归属必须由知识服务验证；底层文件/向量 provider 不会替代这些对象权限检查。跨领域检索通过 contract 协作。

## 主要包

`com.shiyu.ai.knowledge.implementation`、`com.shiyu.ai.knowledge.implementation.application`、`com.shiyu.ai.knowledge.implementation.domain`、`com.shiyu.ai.knowledge.implementation.infrastructure`、`com.shiyu.ai.knowledge.implementation.persistence`、`com.shiyu.ai.knowledge.implementation.web`

## 内部模块依赖

`shiyu-knowledge-contract`、`shiyu-shared-kernel`、`shiyu-agent-contract`、`shiyu-model-contract`、`shiyu-common-foundation`、`shiyu-common-web`、`shiyu-common-mybatis`、`shiyu-common-storage`、`shiyu-common-thread`、`shiyu-common-vector`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/domains/knowledge/shiyu-knowledge-implementation -am test '-Ddependency-check.skip=true'`
