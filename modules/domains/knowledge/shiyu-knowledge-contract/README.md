# shiyu-knowledge-contract 模块说明

- **模块坐标**：`com.shiyu.ai:shiyu-knowledge-contract`
- **分类**：领域模块 · Contract

## 作用

定义知识检索、知识点与关系访问以及租户初始化的跨领域协作契约，不实现索引或知识库管理流程。

## 契约内容

- `KnowledgeRetrievalService` 及检索请求/结果模型表达检索模式、命中项、引用和来源类型，供 Agent 等协作方请求知识内容。
- `KnowledgePointPort`、`KnowledgeRelationPort` 和 `KnowledgePathPort` 提供按知识点、关系及路径协作的接口。
- `KnowledgeTenantProvisioning` 定义为指定租户初始化默认知识配置的入口；契约不规定由谁存储或如何创建默认空间。
- 本模块不解析文件、不切分/嵌入文档、不查询向量数据库，也不提供知识管理 Controller。

## 边界

Agent 与教育等协作方可依赖这些接口；contract 不依赖知识 implementation、Spring、ORM 或供应商 SDK。

## 主要包

`com.shiyu.ai.knowledge.contract`、`com.shiyu.ai.knowledge.contract.api`、`com.shiyu.ai.knowledge.contract.model`

## 内部模块依赖

`shiyu-shared-kernel`

## 验证

运行本模块及其依赖模块的测试：

`mvn --batch-mode --no-transfer-progress -pl modules/domains/knowledge/shiyu-knowledge-contract -am test -Ddependency-check.skip=true`
