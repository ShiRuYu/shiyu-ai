# ShiYu AI 重构任务清单

> **文档版本**: 1.0  
> **创建日期**: 2026-07-09  
> **基于**: Architecture Design Document v2.0  
> **状态**: 待执行

---

## 概览

### 任务统计

| 优先级 | 任务数 | 子任务数 | 预估工时 |
|--------|--------|----------|----------|
| **P0 - 阻断性** | 7 | 35 | 80h |
| **P1 - 重要** | 6 | 18 | 60h |
| **P2 - 优化** | 8 | 16 | 60h |
| **总计** | **21** | **69** | **200h** |

### 进度概览

```
P0 任务:  [░░░░░░░░░░] 0/7 完成
P1 任务:  [░░░░░░░░░░] 0/6 完成
P2 任务:  [░░░░░░░░░░] 0/8 完成
总体进度: [░░░░░░░░░░] 0/21 完成
```

### 已完成任务（FIX-PLAN）

以下 9 项任务已在 FIX-PLAN 中完成，不再列入重构清单：

- [x] API Key 环境变量化
- [x] Password 字段不返前端
- [x] Token 纯随机化
- [x] Java 反序列化 → JSON
- [x] Caffeine 缓存对齐
- [x] 日志脱敏（验证码）
- [x] MySQL 坐标修正
- [x] XSS 验证器升级
- [x] 默认密码不共享

---

## P0 任务 - 阻断性（必须立即修复）

### P0-1: Core 模块拆分

**描述**: `shiyu-ai-core` 同时承担模型适配、记忆管理、工具服务、嵌入服务等多项职责，违反单一职责原则。需拆分为 4 个独立模块。

**涉及文件**:
- `shiyu-ai-core/src/main/java/com/shiyu/ai/core/` (21 个文件, 1994 行)

**文件分类**:

| 目标模块 | 文件 | 行数 |
|---------|------|------|
| `shiyu-ai-model` | `langchain4j/ModelAdapter.java`<br>`langchain4j/AbstractModelAdapter.java`<br>`langchain4j/ModelManager.java`<br>`langchain4j/impl/OllamaPlatformAdapter.java`<br>`langchain4j/impl/GenericPlatformAdapter.java`<br>`langchain4j/config/PlatformConfig.java`<br>`langchain4j/config/ChatEngineConfiguration.java`<br>`embedding/EmbeddingService.java`<br>`embedding/impl/LangChain4jEmbeddingService.java`<br>`config/PlatformProperties.java` | 815 |
| `shiyu-ai-memory` | `memory/MemoryService.java`<br>`memory/impl/MemoryServiceImpl.java`<br>`ChatMemoryProvider.java` | 297 |
| `shiyu-ai-tool` | `mcp/ToolService.java`<br>`mcp/impl/ToolServiceImpl.java` | 389 |
| 保留在 core | `ChatEngine.java`<br>`ChatRequest.java`<br>`ChatResponse.java`<br>`impl/ChatEngineImpl.java`<br>`impl/helper/ChatEngineHelper.java`<br>`controller/ChatDemoController.java` | 493 |

**子任务**:

- [ ] 创建 `shiyu-ai-model` 模块
  - [ ] 创建 `pom.xml`，依赖 `shiyu-ai-dal`, `shiyu-common-core`
  - [ ] 移动 `langchain4j/` 包到 `shiyu-ai-model`
  - [ ] 移动 `embedding/` 包到 `shiyu-ai-model`
  - [ ] 移动 `config/PlatformProperties.java` 到 `shiyu-ai-model`
  - [ ] 更新包名：`com.shiyu.ai.core.langchain4j` → `com.shiyu.ai.model.adapter`
  - [ ] 更新包名：`com.shiyu.ai.core.embedding` → `com.shiyu.ai.model.embedding`
  - [ ] 更新所有引用这些类的 import 语句
  
- [ ] 创建 `shiyu-ai-memory` 模块
  - [ ] 创建 `pom.xml`，依赖 `shiyu-ai-dal`, `shiyu-ai-model`, `shiyu-common-core`
  - [ ] 移动 `memory/` 包到 `shiyu-ai-memory`
  - [ ] 移动 `ChatMemoryProvider.java` 到 `shiyu-ai-memory`
  - [ ] 更新包名：`com.shiyu.ai.core.memory` → `com.shiyu.ai.memory`
  - [ ] 更新所有引用这些类的 import 语句
  
- [ ] 创建 `shiyu-ai-tool` 模块
  - [ ] 创建 `pom.xml`，依赖 `shiyu-ai-dal`, `shiyu-common-core`
  - [ ] 移动 `mcp/` 包到 `shiyu-ai-tool`
  - [ ] 更新包名：`com.shiyu.ai.core.mcp` → `com.shiyu.ai.tool`
  - [ ] 更新所有引用这些类的 import 语句
  
- [ ] 更新 `shiyu-ai-core/pom.xml`
  - [ ] 移除已移动类的依赖（langchain4j, jvector 等）
  - [ ] 添加对新模块的依赖
  
- [ ] 更新 `pom.xml`（根目录）
  - [ ] 在 `<modules>` 中添加 3 个新模块
  - [ ] 在 `<dependencyManagement>` 中添加 3 个新模块的声明
  
- [ ] 更新 `shiyu-ai-bootstrap/pom.xml`
  - [ ] 添加对 3 个新模块的依赖
  
- [ ] 编译验证
  - [ ] `mvn clean compile` 通过
  - [ ] `mvn test` 通过（如果有测试）

**预估工时**: 16h

**验收标准**:
- 3 个新模块独立编译通过
- 原 `shiyu-ai-core` 模块仅保留 ChatEngine 相关代码
- 所有引用新模块的类能正常导入
- 应用能正常启动

**依赖关系**: 无

---

### P0-2: Agent Runtime 完善

**描述**: 当前 Agent 执行缺少生命周期管理、检查点、暂停/恢复等核心能力。需补充完整的 Agent Runtime 体系。

**涉及文件**:
- `shiyu-ai-agent/src/main/java/com/shiyu/ai/aiagent/service/AgentService.java`
- `shiyu-ai-agent/src/main/java/com/shiyu/ai/aiagent/service/impl/AgentServiceImpl.java`
- `shiyu-ai-agent/src/main/java/com/shiyu/ai/aiagent/node/BaseNode.java`
- `shiyu-ai-agent/src/main/java/com/shiyu/ai/aiagent/graph/Graph.java`

**新建文件**:

```
shiyu-ai-agent/src/main/java/com/shiyu/ai/agent/
├── runtime/
│   ├── AgentRuntime.java              # 运行时接口
│   ├── AgentRuntimeImpl.java          # 运行时实现
│   ├── AgentExecutor.java             # 执行器
│   ├── AgentScheduler.java            # 调度器
│   └── AgentWorker.java               # 工作线程
├── execution/
│   ├── Execution.java                 # 执行实例
│   ├── ExecutionStatus.java           # 状态枚举
│   ├── NodeExecution.java             # 节点执行记录
│   ├── ExecutionRepository.java       # 执行仓储接口
│   └── ExecutionHistoryService.java   # 执行历史服务
├── checkpoint/
│   ├── Checkpoint.java                # 检查点数据
│   ├── CheckpointStore.java           # 存储接口
│   ├── DbCheckpointStore.java         # DB 实现
│   └── CheckpointManager.java         # 管理器
├── lifecycle/
│   ├── AgentLifecycle.java            # 生命周期接口
│   ├── AgentStateMachine.java         # 状态机
│   └── AgentState.java                # 状态枚举
├── retry/
│   ├── RetryPolicy.java               # 重试策略
│   └── RetryConfig.java               # 重试配置
├── timeout/
│   ├── TimeoutPolicy.java             # 超时策略
│   └── TimeoutConfig.java             # 超时配置
└── compensation/
    ├── CompensationAction.java        # 补偿动作
    └── CompensationManager.java       # 补偿管理器
```

**数据库表**:

```sql
-- 执行记录表
CREATE TABLE agent_execution (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    execution_id VARCHAR(128) NOT NULL UNIQUE,
    agent_id VARCHAR(128) NOT NULL,
    version VARCHAR(32),
    status VARCHAR(32) NOT NULL,
    input_data CLOB,
    output_data CLOB,
    error_message VARCHAR(1024),
    user_id BIGINT,
    session_id VARCHAR(128),
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    duration_ms BIGINT,
    tenant_id BIGINT,
    workspace_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 节点执行记录表
CREATE TABLE node_execution (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    execution_id VARCHAR(128) NOT NULL,
    node_id VARCHAR(128) NOT NULL,
    node_type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    input_data CLOB,
    output_data CLOB,
    error_message VARCHAR(1024),
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    duration_ms BIGINT,
    retry_count INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 检查点表
CREATE TABLE agent_checkpoint (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    checkpoint_id VARCHAR(128) NOT NULL UNIQUE,
    execution_id VARCHAR(128) NOT NULL,
    node_id VARCHAR(128) NOT NULL,
    state_data CLOB,
    serialized_state BLOB,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**子任务**:

- [ ] 创建 `Execution` 领域模型
  - [ ] 定义 `ExecutionStatus` 枚举（PENDING, RUNNING, PAUSED, COMPLETED, FAILED, CANCELLED）
  - [ ] 定义 `Execution` 类（executionId, agentId, version, status, input, output, error, userId, sessionId, startTime, endTime, durationMs, nodeExecutions, lastCheckpoint）
  - [ ] 定义 `NodeExecution` 类（nodeId, nodeType, status, input, output, startTime, endTime, durationMs, retryCount, errorMessage）
  
- [ ] 创建 `Checkpoint` 机制
  - [ ] 定义 `Checkpoint` 类（checkpointId, executionId, nodeId, state, createdAt, serializedState）
  - [ ] 定义 `CheckpointStore` 接口（save, load, delete, list）
  - [ ] 实现 `DbCheckpointStore`（使用 MyBatis-Flex 操作 agent_checkpoint 表）
  - [ ] 实现 `CheckpointManager`（createCheckpoint, saveCheckpoint, loadCheckpoint）
  
- [ ] 创建 `AgentRuntime` 接口
  - [ ] 定义 `execute(agentId, input)` 方法
  - [ ] 定义 `executeStream(agentId, input)` 方法（返回 Flux）
  - [ ] 定义 `pause(executionId)` 方法
  - [ ] 定义 `resume(executionId)` 方法
  - [ ] 定义 `cancel(executionId)` 方法
  - [ ] 定义 `getStatus(executionId)` 方法
  - [ ] 定义 `getHistory(agentId, limit)` 方法
  
- [ ] 实现 `AgentRuntimeImpl`
  - [ ] 实现 `execute()` 方法（创建 Execution，调用 AgentExecutor）
  - [ ] 实现 `executeStream()` 方法（流式执行）
  - [ ] 实现 `pause()` 方法（保存 Checkpoint，取消 Future）
  - [ ] 实现 `resume()` 方法（从 Checkpoint 恢复执行）
  - [ ] 实现 `cancel()` 方法（标记 CANCELLED，取消 Future）
  
- [ ] 实现 `AgentExecutor`
  - [ ] 实现节点循环执行逻辑
  - [ ] 集成 `CheckpointManager`（每个节点执行前保存 Checkpoint）
  - [ ] 集成 `RetryPolicy`（节点失败时重试）
  - [ ] 集成 `TimeoutPolicy`（全局超时控制）
  
- [ ] 实现 `AgentStateMachine`
  - [ ] 定义状态转换规则
  - [ ] 实现 `transition(currentState, event)` 方法
  
- [ ] 实现 `RetryPolicy`
  - [ ] 定义 `RetryConfig`（maxRetries, initialDelayMs, backoffMultiplier）
  - [ ] 实现 `executeWithRetry(Supplier, RetryConfig)` 方法
  
- [ ] 实现 `TimeoutPolicy`
  - [ ] 定义 `TimeoutConfig`（globalTimeoutMs, nodeTimeoutMs）
  - [ ] 实现 `executeWithTimeout(Supplier, TimeoutConfig)` 方法
  
- [ ] 创建数据库表
  - [ ] 编写 `agent_execution` 表 DDL
  - [ ] 编写 `node_execution` 表 DDL
  - [ ] 编写 `agent_checkpoint` 表 DDL
  - [ ] 创建对应的 DO、Mapper、Repository
  
- [ ] 更新 `AgentService`
  - [ ] 将 `execute()` 委托给 `AgentRuntime`
  - [ ] 将 `executeStream()` 委托给 `AgentRuntime`
  
- [ ] 添加 REST API
  - [ ] `POST /api/agent/{agentId}/execute` - 同步执行
  - [ ] `POST /api/agent/{agentId}/execute/stream` - 流式执行
  - [ ] `POST /api/agent/execution/{executionId}/pause` - 暂停
  - [ ] `POST /api/agent/execution/{executionId}/resume` - 恢复
  - [ ] `POST /api/agent/execution/{executionId}/cancel` - 取消
  - [ ] `GET /api/agent/execution/{executionId}/status` - 查询状态
  - [ ] `GET /api/agent/{agentId}/executions` - 查询历史

**预估工时**: 24h

**验收标准**:
- Agent 执行支持暂停/恢复
- 每个节点执行前保存 Checkpoint
- 节点失败时自动重试（可配置）
- 全局超时控制生效
- 执行历史可查询
- REST API 可用

**依赖关系**: 无

---

### P0-3: Memory 五层体系

**描述**: 当前仅有短期记忆（对话历史）和长期记忆（持久化），缺少工作记忆、语义记忆、情景记忆。需扩展为五层记忆体系。

**涉及文件**:
- `shiyu-ai-core/src/main/java/com/shiyu/ai/core/memory/MemoryService.java` (60 行)
- `shiyu-ai-core/src/main/java/com/shiyu/ai/core/memory/impl/MemoryServiceImpl.java` (225 行)

**新建文件**:

```
shiyu-ai-memory/src/main/java/com/shiyu/ai/memory/
├── spi/
│   ├── MemoryStore.java               # 统一存储接口
│   ├── Memory.java                    # 记忆实体
│   ├── MemoryType.java                # 记忆类型枚举
│   ├── MemoryQuery.java               # 查询条件
│   └── MemoryCodec.java               # 序列化接口
├── shortterm/
│   └── ShortTermMemoryStore.java      # 短期记忆（DB + Caffeine）
├── working/
│   └── WorkingMemoryStore.java        # 工作记忆（内存）
├── longterm/
│   └── LongTermMemoryStore.java       # 长期记忆（DB）
├── semantic/
│   └── SemanticMemoryStore.java       # 语义记忆（VectorStore）
├── episodic/
│   └── EpisodicMemoryStore.java       # 情景记忆（DB）
├── compressor/
│   ├── MemoryCompressor.java          # 压缩接口
│   ├── LlmSummarizeCompressor.java    # LLM 摘要压缩
│   └── SlidingWindowCompressor.java   # 滑动窗口压缩
├── recall/
│   ├── MemoryRecallStrategy.java      # 召回策略接口
│   ├── SimilarityRecallStrategy.java  # 相似度召回
│   ├── ImportanceRecallStrategy.java  # 重要性召回
│   └── HybridRecallStrategy.java      # 混合召回
└── config/
    └── MemoryAutoConfiguration.java   # 自动配置
```

**数据库表**:

```sql
-- 短期记忆（对话消息）
CREATE TABLE conversation_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id VARCHAR(128) NOT NULL,
    user_id BIGINT,
    agent_id VARCHAR(128),
    role VARCHAR(32) NOT NULL,
    content CLOB NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 长期记忆
CREATE TABLE long_term_memory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    agent_id VARCHAR(128),
    category VARCHAR(64),
    memory_key VARCHAR(256),
    content CLOB NOT NULL,
    importance DOUBLE DEFAULT 0.5,
    source VARCHAR(256),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 情景记忆（执行历史）
CREATE TABLE episodic_memory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    execution_id VARCHAR(128) NOT NULL,
    agent_id VARCHAR(128) NOT NULL,
    user_id BIGINT,
    session_id VARCHAR(128),
    task_type VARCHAR(64),
    task_description CLOB,
    status VARCHAR(32),
    result_summary CLOB,
    error_message VARCHAR(1024),
    duration_ms BIGINT,
    node_count INT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**子任务**:

- [ ] 定义 `MemoryStore` SPI 接口
  - [ ] 定义 `save(Memory)` 方法
  - [ ] 定义 `saveBatch(List<Memory>)` 方法
  - [ ] 定义 `query(MemoryQuery)` 方法
  - [ ] 定义 `delete(String memoryId)` 方法
  - [ ] 定义 `deleteBySession(String sessionId)` 方法
  
- [ ] 定义 `Memory` 实体
  - [ ] 定义字段：memoryId, type, sessionId, userId, agentId, role, content, embedding, category, memoryKey, importance, source, createdAt, accessedAt, accessCount, metadata
  
- [ ] 定义 `MemoryType` 枚举
  - [ ] SHORT_TERM, WORKING, LONG_TERM, SEMANTIC, EPISODIC
  
- [ ] 实现 `ShortTermMemoryStore`
  - [ ] 使用 DB + Caffeine 缓存
  - [ ] 实现滑动窗口压缩（默认 10 条）
  
- [ ] 实现 `WorkingMemoryStore`
  - [ ] 使用内存 ConcurrentHashMap
  - [ ] 实现 `setVariable(sessionId, key, value)` 方法
  - [ ] 实现 `getVariable(sessionId, key)` 方法
  - [ ] 实现 `clear(sessionId)` 方法
  
- [ ] 实现 `LongTermMemoryStore`
  - [ ] 使用 DB 持久化
  - [ ] 实现关键词搜索
  - [ ] 实现重要性排序
  
- [ ] 实现 `SemanticMemoryStore`
  - [ ] 使用 VectorStore + EmbeddingService
  - [ ] 实现向量检索
  - [ ] 实现元数据过滤
  
- [ ] 实现 `EpisodicMemoryStore`
  - [ ] 使用 DB 持久化
  - [ ] 记录任务执行经历
  
- [ ] 实现 `MemoryCompressor`
  - [ ] 定义 `compress(List<ChatMessage>)` 方法
  - [ ] 实现 `LlmSummarizeCompressor`（调用 LLM 生成摘要）
  - [ ] 实现 `SlidingWindowCompressor`（滑动窗口）
  
- [ ] 实现 `MemoryRecallStrategy`
  - [ ] 定义 `recall(MemoryRecallRequest)` 方法
  - [ ] 实现 `SimilarityRecallStrategy`（向量相似度）
  - [ ] 实现 `ImportanceRecallStrategy`（重要性排序）
  - [ ] 实现 `HybridRecallStrategy`（混合召回）
  
- [ ] 创建数据库表
  - [ ] 编写 `conversation_message` 表 DDL
  - [ ] 编写 `long_term_memory` 表 DDL
  - [ ] 编写 `episodic_memory` 表 DDL
  - [ ] 创建对应的 DO、Mapper、Repository
  
- [ ] 迁移现有 `MemoryService` 逻辑
  - [ ] 将 `MemoryServiceImpl` 的逻辑迁移到各层 MemoryStore
  - [ ] 更新 `MemoryService` 接口，委托给各层 Store
  
- [ ] 添加配置
  - [ ] `shiyu.memory.short-term.max-size=10`
  - [ ] `shiyu.memory.long-term.enabled=true`
  - [ ] `shiyu.memory.semantic.enabled=true`
  - [ ] `shiyu.memory.episodic.enabled=true`

**预估工时**: 20h

**验收标准**:
- 五层记忆均可独立使用
- 短期记忆支持滑动窗口压缩
- 语义记忆支持向量检索
- 记忆召回策略可切换
- 现有 `MemoryService` 功能不受影响

**依赖关系**: P0-1（Core 模块拆分后，Memory 模块独立）

---

### P0-4: VectorStore SPI

**描述**: `HnswVectorStore` 直接依赖 JVector API，无法切换到其他向量数据库。需抽象统一 SPI 接口，支持多种实现。

**涉及文件**:
- `shiyu-ai-knowledge/src/main/java/com/shiyu/ai/knowledge/vector/VectorStore.java` (25 行)
- `shiyu-ai-knowledge/src/main/java/com/shiyu/ai/knowledge/vector/impl/HnswVectorStore.java` (364 行)
- `shiyu-ai-knowledge/src/main/java/com/shiyu/ai/knowledge/vector/impl/InMemoryVectorStore.java`

**新建文件**:

```
shiyu-ai-vector/src/main/java/com/shiyu/ai/vector/
├── spi/
│   ├── VectorStore.java               # 增强接口
│   ├── VectorRecord.java              # 向量记录
│   └── VectorSearchRequest.java       # 搜索请求
├── impl/
│   ├── jvector/
│   │   └── JVectorStore.java          # JVector 实现
│   ├── pgvector/
│   │   └── PgVectorStore.java         # PGVector 实现
│   └── memory/
│       └── InMemoryVectorStore.java   # 内存实现
├── factory/
│   └── VectorStoreFactory.java        # 工厂
└── config/
    ├── VectorStoreProperties.java     # 配置
    └── VectorStoreAutoConfiguration.java  # 自动配置
```

**子任务**:

- [ ] 定义增强版 `VectorStore` 接口
  - [ ] 定义 `upsert(VectorRecord)` 方法
  - [ ] 定义 `upsertBatch(List<VectorRecord>)` 方法
  - [ ] 定义 `search(VectorSearchRequest)` 方法
  - [ ] 定义 `delete(String id)` 方法
  - [ ] 定义 `deleteBatch(List<String> ids)` 方法
  - [ ] 定义 `rebuild()` 方法
  - [ ] 定义 `size()` 方法
  
- [ ] 定义 `VectorRecord`
  - [ ] 使用 Java Record：`record VectorRecord(String id, float[] vector, Map<String, Object> metadata)`
  
- [ ] 定义 `VectorSearchRequest`
  - [ ] 定义字段：queryVector, topK, minScore, filter, searchType
  - [ ] 实现 Builder 模式
  - [ ] 定义 `VectorSearchType` 枚举（ANN, EXACT）
  
- [ ] 实现 `JVectorStore`
  - [ ] 迁移 `HnswVectorStore` 逻辑
  - [ ] 实现 `search(VectorSearchRequest)` 方法
  - [ ] 支持元数据过滤
  
- [ ] 实现 `PgVectorStore`
  - [ ] 使用 JdbcTemplate 操作 PostgreSQL
  - [ ] 实现 pgvector 类型的读写
  - [ ] 实现向量距离计算（<=> 操作符）
  
- [ ] 实现 `InMemoryVectorStore`
  - [ ] 使用内存 List 存储
  - [ ] 实现余弦相似度计算
  - [ ] 用于测试
  
- [ ] 实现 `VectorStoreFactory`
  - [ ] 根据配置创建对应的 VectorStore 实例
  
- [ ] 实现自动配置
  - [ ] `@ConditionalOnProperty(name = "shiyu.vector.type", havingValue = "jvector")`
  - [ ] `@ConditionalOnProperty(name = "shiyu.vector.type", havingValue = "pgvector")`
  - [ ] `@ConditionalOnProperty(name = "shiyu.vector.type", havingValue = "memory")`
  
- [ ] 更新 `shiyu-ai-knowledge` 依赖
  - [ ] 移除对 JVector 的直接依赖
  - [ ] 添加对 `shiyu-ai-vector` 的依赖
  - [ ] 更新 `RagOrchestrator` 使用新的 `VectorStore` 接口
  
- [ ] 添加配置
  - [ ] `shiyu.vector.type=jvector`（默认）
  - [ ] `shiyu.vector.dimension=512`
  - [ ] `shiyu.vector.data-dir=${app.home}/data/vector`
  - [ ] `shiyu.vector.table-name=vector_store`
  - [ ] `shiyu.vector.qdrant.host=localhost`
  - [ ] `shiyu.vector.qdrant.port=6334`

**预估工时**: 12h

**验收标准**:
- `VectorStore` SPI 接口统一
- JVector 实现可用（开发环境）
- PGVector 实现可用（生产环境）
- 可通过配置切换实现
- 现有 RAG 功能不受影响

**依赖关系**: 无

---

### P0-5: Usage Center

**描述**: 无 Token 用量统计、成本计算、配额管理。需建立完整的 Usage Center。

**新建文件**:

```
shiyu-ai-usage/src/main/java/com/shiyu/ai/usage/
├── collector/
│   ├── UsageCollector.java            # 采集接口
│   ├── TokenUsageCollector.java       # Token 采集
│   ├── ToolUsageCollector.java        # 工具采集
│   └── EmbeddingUsageCollector.java   # 嵌入采集
├── statistics/
│   ├── UsageStatisticsService.java    # 统计服务
│   ├── UsageDimension.java            # 统计维度
│   └── UsageAggregator.java           # 聚合器
├── cost/
│   ├── CostCalculator.java            # 成本计算
│   ├── ModelPricing.java              # 模型定价
│   └── CostReport.java                # 成本报告
├── quota/
│   ├── QuotaManager.java              # 配额管理
│   ├── QuotaPolicy.java               # 配额策略
│   └── QuotaChecker.java              # 配额检查
├── controller/
│   └── UsageController.java           # REST API
├── service/
│   └── UsageService.java              # 业务服务
└── config/
    └── UsageAutoConfiguration.java    # 自动配置
```

**数据库表**:

```sql
-- Token 用量记录
CREATE TABLE token_usage (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    agent_id VARCHAR(128),
    session_id VARCHAR(128),
    platform VARCHAR(64) NOT NULL,
    model VARCHAR(128) NOT NULL,
    prompt_tokens INT NOT NULL DEFAULT 0,
    completion_tokens INT NOT NULL DEFAULT 0,
    total_tokens INT NOT NULL DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 模型定价
CREATE TABLE model_pricing (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    platform VARCHAR(64) NOT NULL,
    model VARCHAR(128) NOT NULL,
    prompt_price_per_token DECIMAL(10, 8) NOT NULL,
    completion_price_per_token DECIMAL(10, 8) NOT NULL,
    currency VARCHAR(8) DEFAULT 'CNY',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_platform_model (platform, model)
);

-- 配额策略
CREATE TABLE quota_policy (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT,
    user_id BIGINT,
    daily_token_limit BIGINT,
    monthly_token_limit BIGINT,
    daily_call_limit INT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**子任务**:

- [ ] 定义 `UsageCollector` 接口
  - [ ] 定义 `record(UsageRecord)` 方法
  
- [ ] 实现 `TokenUsageCollector`
  - [ ] 监听 `ModelCallEvent`
  - [ ] 记录 Token 用量到 `token_usage` 表
  
- [ ] 实现 `UsageStatisticsService`
  - [ ] 实现 `getStatistics(UsageQuery)` 方法
  - [ ] 实现 `getStatisticsByDimension(UsageQuery, Dimension)` 方法
  - [ ] 支持按 USER/AGENT/MODEL/PLATFORM/DATE 分组统计
  
- [ ] 实现 `CostCalculator`
  - [ ] 实现 `calculateCost(UsageStatistics)` 方法
  - [ ] 根据 `model_pricing` 表计算成本
  
- [ ] 实现 `QuotaManager`
  - [ ] 实现 `checkQuota(userId, platform)` 方法
  - [ ] 检查日/月配额
  
- [ ] 创建数据库表
  - [ ] 编写 `token_usage` 表 DDL
  - [ ] 编写 `model_pricing` 表 DDL
  - [ ] 编写 `quota_policy` 表 DDL
  - [ ] 创建对应的 DO、Mapper、Repository
  
- [ ] 添加 REST API
  - [ ] `GET /api/usage/statistics` - 查询统计
  - [ ] `GET /api/usage/cost` - 查询成本
  - [ ] `GET /api/usage/quota/check` - 检查配额
  
- [ ] 添加配置
  - [ ] `shiyu.usage.enabled=true`
  - [ ] `shiyu.usage.quota.enabled=false`（默认关闭）

**预估工时**: 12h

**验收标准**:
- Token 用量自动采集
- 支持多维度统计
- 成本计算准确
- 配额检查生效
- REST API 可用

**依赖关系**: P0-1（Core 模块拆分后，Usage 模块独立）

---

### P0-6: 事件中心

**描述**: 仅有 `DomainEvent` 抽象类，无事件发布/订阅机制。需建立完整的事件中心。

**涉及文件**:
- `shiyu-common/shiyu-common-core/src/main/java/com/shiyu/ai/common/core/tx/event/DomainEvent.java` (15 行)

**新建文件**:

```
shiyu-common/shiyu-common-core/src/main/java/com/shiyu/ai/common/core/event/
├── DomainEvent.java                   # 基础事件（迁移）
├── EventPublisher.java                # 事件发布器
├── AgentExecutionStartedEvent.java    # Agent 执行开始
├── AgentExecutionCompletedEvent.java  # Agent 执行完成
├── AgentExecutionFailedEvent.java     # Agent 执行失败
├── ModelCallEvent.java                # 模型调用
└── MemorySavedEvent.java              # 记忆保存
```

**子任务**:

- [ ] 迁移 `DomainEvent` 到 `event` 包
  - [ ] 更新包名：`com.shiyu.ai.common.core.tx.event` → `com.shiyu.ai.common.core.event`
  - [ ] 更新所有引用
  
- [ ] 实现 `EventPublisher`
  - [ ] 注入 `ApplicationEventPublisher`
  - [ ] 实现 `publish(DomainEvent)` 方法
  
- [ ] 定义事件类
  - [ ] `AgentExecutionStartedEvent`（executionId, agentId, input）
  - [ ] `AgentExecutionCompletedEvent`（executionId, output, durationMs）
  - [ ] `AgentExecutionFailedEvent`（executionId, error）
  - [ ] `ModelCallEvent`（platform, model, promptTokens, completionTokens, latencyMs）
  - [ ] `MemorySavedEvent`（sessionId, type, content）
  
- [ ] 集成到业务代码
  - [ ] 在 `AgentRuntimeImpl` 中发布 Agent 执行事件
  - [ ] 在 `ModelManager` 中发布模型调用事件
  - [ ] 在 `MemoryService` 中发布记忆保存事件

**预估工时**: 4h

**验收标准**:
- 事件可正常发布和订阅
- 各业务模块能监听事件
- 事件异步处理不阻塞主流程

**依赖关系**: 无

---

### P0-7: BO 收归 DAL

**描述**: BO 散落在各业务模块顶层包，未统一归到 DAL 模块。需收归到 `dal.bo.{domain}/` 下。

**涉及文件**: 20 个 BO 文件，分布在 4 个包中

**当前分布**:

| 包 | 文件数 | 文件 |
|----|--------|------|
| `com.shiyu.ai.auth.bo` | 6 | UserBO, RoleBO, TenantBO, MenuBO, WorkspaceBO, DictBO |
| `com.shiyu.ai.aiagent.bo` | 4 | AgentDefBO, AgentVersionBO, AgentExecutionBO, IntentDefBO |
| `com.shiyu.ai.record.bo` | 6 | RecordBO, ProfileBO, ProfileMemberBO, MediaBO, TagBO, TimelineEventBO |
| `com.shiyu.ai.model.bo` | 4 | AiPlatformBO, AiModelBO, ConversationMessageBO, LongTermMemoryBO |

**目标结构**:

```
shiyu-ai-dal/src/main/java/com/shiyu/ai/dal/bo/
├── auth/
│   ├── UserBO.java
│   ├── RoleBO.java
│   ├── TenantBO.java
│   ├── MenuBO.java
│   ├── WorkspaceBO.java
│   └── DictBO.java
├── agent/
│   ├── AgentDefBO.java
│   ├── AgentVersionBO.java
│   ├── AgentExecutionBO.java
│   └── IntentDefBO.java
├── record/
│   ├── RecordBO.java
│   ├── ProfileBO.java
│   ├── ProfileMemberBO.java
│   ├── MediaBO.java
│   ├── TagBO.java
│   └── TimelineEventBO.java
├── memory/
│   ├── ConversationMessageBO.java
│   └── LongTermMemoryBO.java
├── model/
│   ├── AiPlatformBO.java
│   └── AiModelBO.java
├── education/
│   └── (后续添加)
├── knowledge/
│   └── (后续添加)
└── usage/
    └── (后续添加)
```

**子任务**:

- [ ] 创建 `dal/bo/` 目录结构
  - [ ] 创建 `auth/`, `agent/`, `record/`, `memory/`, `model/` 子目录
  
- [ ] 移动 `auth.bo` 包
  - [ ] 移动 6 个文件到 `dal.bo.auth`
  - [ ] 更新包声明
  - [ ] 更新所有引用
  
- [ ] 移动 `aiagent.bo` 包
  - [ ] 移动 4 个文件到 `dal.bo.agent`
  - [ ] 更新包声明
  - [ ] 更新所有引用
  
- [ ] 移动 `record.bo` 包
  - [ ] 移动 6 个文件到 `dal.bo.record`
  - [ ] 更新包声明
  - [ ] 更新所有引用
  
- [ ] 移动 `model.bo` 包
  - [ ] 移动 4 个文件到 `dal.bo.memory` 和 `dal.bo.model`
  - [ ] `ConversationMessageBO`, `LongTermMemoryBO` → `dal.bo.memory`
  - [ ] `AiPlatformBO`, `AiModelBO` → `dal.bo.model`
  - [ ] 更新包声明
  - [ ] 更新所有引用
  
- [ ] 删除旧包目录
  - [ ] 删除 `auth/bo/`, `aiagent/bo/`, `record/bo/`, `model/bo/`
  
- [ ] 编译验证
  - [ ] `mvn clean compile` 通过

**预估工时**: 4h

**验收标准**:
- 所有 BO 统一到 `dal.bo.{domain}/` 下
- 编译通过
- 无遗留的旧包引用

**依赖关系**: 无

---

## P1 任务 - 重要（影响扩展性）

### P1-1: NodeFactory 注册式重构

**描述**: `NodeFactory.createNodeWithDependencies()` 使用大型 switch 表达式（176-272 行），违反开闭原则。需改为注册式工厂。

**涉及文件**:
- `shiyu-ai-agent/src/main/java/com/shiyu/ai/aiagent/node/NodeFactory.java` (597 行)
  - Switch 表达式位于 176-272 行

**新建文件**:

```
shiyu-ai-agent/src/main/java/com/shiyu/ai/agent/node/
└── creator/
    ├── NodeCreator.java               # 创建器接口
    ├── IntentNodeCreator.java         # 意图节点创建器
    ├── LlmCallNodeCreator.java        # LLM 调用节点创建器
    ├── ToolCallNodeCreator.java       # 工具调用节点创建器
    ├── RagRetrievalNodeCreator.java   # RAG 检索节点创建器
    ├── AgentCallNodeCreator.java      # Agent 调用节点创建器
    ├── ShortTermMemoryNodeCreator.java
    ├── LongTermMemoryNodeCreator.java
    └── MemoryRetrievalNodeCreator.java
```

**子任务**:

- [ ] 定义 `NodeCreator` 接口
  - [ ] 定义 `getType()` 方法（返回 `NodeType`）
  - [ ] 定义 `create(NodeConfig)` 方法（返回 `BaseNode`）
  
- [ ] 为每种节点类型实现 `NodeCreator`
  - [ ] `IntentNodeCreator`（注入 `IntentService`）
  - [ ] `LlmCallNodeCreator`（注入 `ChatEngine`, `ModelManager`）
  - [ ] `ToolCallNodeCreator`（注入 `ToolService`）
  - [ ] `RagRetrievalNodeCreator`（注入 `RagService`）
  - [ ] `AgentCallNodeCreator`（注入 `AgentService`）
  - [ ] `ShortTermMemoryNodeCreator`（注入 `MemoryService`）
  - [ ] `LongTermMemoryNodeCreator`（注入 `MemoryService`）
  - [ ] `MemoryRetrievalNodeCreator`（注入 `MemoryService`）
  
- [ ] 重构 `NodeFactory`
  - [ ] 添加 `Map<NodeType, NodeCreator> creators` 字段
  - [ ] 添加 `@Autowired registerCreators(List<NodeCreator>)` 方法
  - [ ] 修改 `createNodeWithDependencies()` 使用 `creators.get(type).create(config)`
  - [ ] 删除 switch 表达式
  
- [ ] 测试验证
  - [ ] 所有节点类型可正常创建
  - [ ] Agent 执行正常

**预估工时**: 8h

**验收标准**:
- switch 表达式消除
- 新增节点类型无需修改 `NodeFactory`
- 现有功能不受影响

**依赖关系**: 无

---

### P1-2: Observability 集成

**描述**: OpenTelemetry 和 Micrometer 已配置但未实际集成到业务代码。需实现 Trace、Metrics、Audit、Timeline。

**新建文件**:

```
shiyu-ai-observation/src/main/java/com/shiyu/ai/observation/
├── trace/
│   ├── TracingConfiguration.java      # OTel 配置
│   └── AgentExecutionTraceAspect.java # 追踪切面
├── metrics/
│   ├── AgentMetrics.java              # Agent 指标
│   ├── ModelMetrics.java              # 模型指标
│   ├── KnowledgeMetrics.java          # 知识库指标
│   └── MemoryMetrics.java             # 记忆指标
├── audit/
│   ├── AuditEvent.java                # 审计事件
│   ├── AuditService.java              # 审计服务
│   └── AuditInterceptor.java          # 审计拦截器
├── timeline/
│   ├── ExecutionTimeline.java         # 时间线实体
│   └── TimelineService.java           # 时间线服务
└── config/
    └── ObservationAutoConfiguration.java
```

**数据库表**:

```sql
-- 审计日志表
CREATE TABLE audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT,
    user_id BIGINT,
    action VARCHAR(64) NOT NULL,
    target_type VARCHAR(64),
    target_id VARCHAR(128),
    detail CLOB,
    ip VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 执行时间线表
CREATE TABLE execution_timeline (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT,
    execution_id VARCHAR(128) NOT NULL,
    agent_id VARCHAR(128),
    node_id VARCHAR(128),
    event_type VARCHAR(32) NOT NULL,
    payload CLOB,
    duration_ms BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**子任务**:

- [ ] 实现 Trace 链路追踪
  - [ ] 配置 `TracingConfiguration`（OpenTelemetry SDK）
  - [ ] 实现 `AgentExecutionTraceAspect`（@Aspect 切面）
  - [ ] 在关键方法添加 `@Traced` 注解
  
- [ ] 实现 Metrics 指标
  - [ ] 实现 `AgentMetrics`（Counter: agent.execution.total, Timer: agent.execution.duration）
  - [ ] 实现 `ModelMetrics`（Counter: model.call.total, Timer: model.call.duration, Counter: model.token.total）
  - [ ] 在业务代码中埋点
  
- [ ] 实现 Audit 审计
  - [ ] 定义 `AuditEvent` 事件
  - [ ] 实现 `AuditService`（@Async @EventListener）
  - [ ] 实现 `AuditInterceptor`（HandlerInterceptor）
  - [ ] 创建 `audit_log` 表
  
- [ ] 实现 Timeline 时间线
  - [ ] 定义 `ExecutionTimeline` 实体
  - [ ] 实现 `TimelineService`（记录 NODE_START, NODE_END 事件）
  - [ ] 创建 `execution_timeline` 表
  
- [ ] 集成到 Grafana
  - [ ] 配置 Prometheus 数据源
  - [ ] 创建 Dashboard

**预估工时**: 16h

**验收标准**:
- Trace 链路可在 Jaeger 中查看
- Metrics 指标可在 Prometheus/Grafana 中查看
- Audit 日志记录到 DB
- Timeline 记录到 DB

**依赖关系**: P0-2（Agent Runtime 完善后，才能记录完整的执行时间线）

---

### P1-3: Flyway 数据库迁移

**描述**: DDL 脚本手动管理，无版本控制。需引入 Flyway 实现自动化迁移。

**涉及文件**:
- `shiyu-ai-dal/src/main/resources/db/migration/ddl/` (8 个 SQL 文件)

**目标结构**:

```
shiyu-ai-dal/src/main/resources/db/migration/
├── V001__create_schema_common.sql
├── V002__create_schema_auth.sql
├── V003__create_schema_agent.sql
├── V004__create_schema_memory.sql
├── V005__create_schema_knowledge.sql
├── V006__create_schema_education.sql
├── V007__create_schema_record.sql
├── V008__create_schema_usage.sql
├── V009__create_schema_observation.sql
└── V010__create_schema_vector.sql
```

**子任务**:

- [ ] 添加 Flyway 依赖
  - [ ] 在 `shiyu-ai-dal/pom.xml` 添加 `flyway-core` 依赖
  
- [ ] 配置 Flyway
  - [ ] 在 `application.yml` 添加 Flyway 配置
  - [ ] `spring.flyway.enabled=true`
  - [ ] `spring.flyway.locations=classpath:db/migration`
  
- [ ] 迁移现有 DDL
  - [ ] 重命名 `01__schema_common.sql` → `V001__create_schema_common.sql`
  - [ ] 重命名其他文件
  - [ ] 添加新的 Schema（usage, observation）
  
- [ ] 测试迁移
  - [ ] 清空数据库
  - [ ] 启动应用
  - [ ] 验证所有表创建成功

**预估工时**: 4h

**验收标准**:
- Flyway 自动执行迁移
- 所有表创建成功
- 版本控制生效

**依赖关系**: 无

---

### P1-4: LoginRateLimiter 增强

**描述**: `LoginRateLimiter` 仅单机有效，无条目清理机制。需增强为滑动窗口 + 定期清理。

**涉及文件**:
- `shiyu-ai-auth/src/main/java/com/shiyu/ai/auth/handler/LoginRateLimiter.java` (83 行)

**子任务**:

- [ ] 实现滑动窗口
  - [ ] 修改 `isAllowed(String ip)` 方法
  - [ ] 使用滑动窗口算法（60 秒窗口，5 次限制）
  
- [ ] 添加定期清理
  - [ ] 添加 `@PostConstruct` 初始化方法
  - [ ] 使用 `ScheduledExecutorService` 每 5 分钟清理过期条目
  
- [ ] 添加随机抖动
  - [ ] 锁定时间添加随机抖动（±10 秒）

**预估工时**: 4h

**验收标准**:
- 滑动窗口限流生效
- 过期条目自动清理
- 多实例场景评估（是否需要 Redis）

**依赖关系**: 无

---

### P1-5: Captcha 增强

**描述**: 验证码需增强为 6 位 + 尝试限制 + UUID key。

**涉及文件**:
- `shiyu-ai-auth/src/main/java/com/shiyu/ai/auth/service/impl/CaptchaServiceImpl.java` (342 行)

**子任务**:

- [ ] 增加验证码长度
  - [ ] 修改 `CAPTCHA_LENGTH` 为 6
  
- [ ] 增加尝试次数限制
  - [ ] 添加 `Map<String, Integer> attemptCount`
  - [ ] 修改 `validateCaptcha()` 检查尝试次数
  - [ ] 超过 3 次销毁验证码
  
- [ ] 使用 UUID 作为 key
  - [ ] 修改 `generateCaptchaKey()` 使用 `UUID.randomUUID()`
  
- [ ] 添加定期清理
  - [ ] 清理 `captchaStore` 和 `attemptCount` 中的过期条目

**预估工时**: 4h

**验收标准**:
- 验证码 6 位
- 最多 3 次尝试
- key 使用 UUID
- 过期条目自动清理

**依赖关系**: 无

---

### P1-6: Model Provider 弹性策略

**描述**: 模型调用缺少降级、熔断、限流、负载均衡等弹性策略。

**新建文件**:

```
shiyu-ai-model/src/main/java/com/shiyu/ai/model/resilience/
├── FallbackStrategy.java              # 降级策略
├── CircuitBreaker.java                # 熔断器
├── RateLimiter.java                   # 限流器
└── LoadBalancer.java                  # 负载均衡器
```

**子任务**:

- [ ] 实现 `FallbackStrategy`
  - [ ] 定义 `execute(Supplier<T> primary, Supplier<T> fallback)` 方法
  - [ ] 主调用失败时执行降级
  
- [ ] 实现 `CircuitBreaker`
  - [ ] 定义状态：CLOSED, OPEN, HALF_OPEN
  - [ ] 实现 `execute(Supplier<T>)` 方法
  - [ ] 失败次数超过阈值时打开熔断器
  - [ ] 超时后进入半开状态
  
- [ ] 实现 `RateLimiter`
  - [ ] 使用滑动窗口算法
  - [ ] 实现 `tryAcquire()` 方法
  
- [ ] 实现 `LoadBalancer`
  - [ ] 实现轮询策略
  - [ ] 实现 `select(List<String> platforms)` 方法
  
- [ ] 集成到 `ModelRegistry`
  - [ ] 在 `getChatModel()` 中应用弹性策略

**预估工时**: 12h

**验收标准**:
- 降级策略生效
- 熔断器状态转换正确
- 限流器限制请求速率
- 负载均衡器轮询选择平台

**依赖关系**: P0-1（Core 模块拆分后，Model 模块独立）

---

## P2 任务 - 优化（提升代码质量）

### P2-1: 线程池拒绝策略统一

**描述**: 部分线程池未配置拒绝策略。需统一配置为 `CallerRunsPolicy`。

**涉及文件**:
- `shiyu-common/shiyu-common-thread/src/main/java/com/shiyu/ai/common/thread/`

**子任务**:

- [ ] 检查所有线程池配置
  - [ ] `ThreadPoolConfig.java`
  - [ ] `AsyncConfig.java`
  
- [ ] 统一配置拒绝策略
  - [ ] 所有线程池使用 `CallerRunsPolicy`

**预估工时**: 2h

**验收标准**:
- 所有线程池配置了拒绝策略
- 拒绝策略为 `CallerRunsPolicy`

**依赖关系**: 无

---

### P2-2: 异常传播统一

**描述**: 部分 Service 吞掉异常。需统一异常传播策略。

**涉及文件**: 各模块的 Service 实现类

**子任务**:

- [ ] 扫描所有 Service 实现
  - [ ] 查找 `catch (Exception e)` 后无处理的情况
  
- [ ] 统一异常处理
  - [ ] 业务异常抛出 `ServiceException`
  - [ ] 系统异常记录日志后抛出
  - [ ] 降级场景明确标注

**预估工时**: 4h

**验收标准**:
- 无吞掉的异常
- 异常处理策略统一

**依赖关系**: 无

---

### P2-3: 单元测试补充

**描述**: 当前 0% 测试覆盖率。需补充核心逻辑的单元测试。

**子任务**:

- [ ] 为核心模块编写测试
  - [ ] `AgentRuntime` 测试
  - [ ] `MemoryService` 测试
  - [ ] `VectorStore` 测试
  - [ ] `ModelRegistry` 测试
  
- [ ] 引入 Testcontainers
  - [ ] 用于集成测试（PostgreSQL, Redis 等）
  
- [ ] 配置测试覆盖率
  - [ ] 目标：V2 达到 50%，V5 达到 80%

**预估工时**: 20h

**验收标准**:
- 核心模块有单元测试
- 测试覆盖率达标

**依赖关系**: P0 任务完成后

---

### P2-4: Knowledge 内部重构

**描述**: Knowledge 模块需拆分为 document/chunk/ingestion/rag 子包。

**涉及文件**:
- `shiyu-ai-knowledge/src/main/java/com/shiyu/ai/knowledge/`

**新建文件**:

```
shiyu-ai-knowledge/src/main/java/com/shiyu/ai/knowledge/
├── document/
│   ├── Document.java
│   ├── DocumentLoader.java
│   ├── DocumentParser.java
│   ├── PdfDocumentParser.java
│   ├── WordDocumentParser.java
│   └── MarkdownDocumentParser.java
├── chunk/
│   ├── ChunkSplitter.java
│   ├── ChineseChunkSplitter.java
│   ├── TokenChunkSplitter.java
│   └── SemanticChunkSplitter.java
├── ingestion/
│   ├── DocumentIngestionPipeline.java
│   ├── IngestionStep.java
│   └── IngestionContext.java
└── rag/
    ├── RagOrchestrator.java
    ├── retriever/
    │   ├── Retriever.java
    │   ├── VectorRetriever.java
    │   ├── GraphRetriever.java
    │   └── HybridRetriever.java
    ├── reranker/
    │   ├── Reranker.java
    │   └── LlmReranker.java
    └── enhancer/
        ├── RagEnhancer.java
        └── ContextWindowEnhancer.java
```

**子任务**:

- [ ] 创建 `document/` 包
  - [ ] 定义 `Document` 实体
  - [ ] 定义 `DocumentParser` SPI
  - [ ] 实现 `PdfDocumentParser`, `WordDocumentParser`, `MarkdownDocumentParser`
  
- [ ] 创建 `chunk/` 包
  - [ ] 定义 `ChunkSplitter` SPI
  - [ ] 实现 `ChineseChunkSplitter`, `TokenChunkSplitter`, `SemanticChunkSplitter`
  
- [ ] 创建 `ingestion/` 包
  - [ ] 实现 `DocumentIngestionPipeline`
  
- [ ] 重构 `rag/` 包
  - [ ] 定义 `Retriever` SPI
  - [ ] 实现 `VectorRetriever`, `GraphRetriever`, `HybridRetriever`
  - [ ] 定义 `Reranker` SPI
  - [ ] 实现 `LlmReranker`
  - [ ] 重构 `RagOrchestrator` 使用 Retriever → Reranker → Enhancer 流水线

**预估工时**: 12h

**验收标准**:
- 文档解析支持多种格式
- 分块策略可切换
- RAG 检索支持混合检索
- 支持重排序

**依赖关系**: P0-4（VectorStore SPI）

---

### P2-5: Agent 包名调整

**描述**: 将 `aiagent` 包名改为 `agent`。

**涉及文件**:
- `shiyu-ai-agent/src/main/java/com/shiyu/ai/aiagent/` (所有文件)

**子任务**:

- [ ] 重命名包
  - [ ] `com.shiyu.ai.aiagent` → `com.shiyu.ai.agent`
  
- [ ] 更新所有引用
  - [ ] 更新 import 语句
  - [ ] 更新配置文件中的包名

**预估工时**: 2h

**验收标准**:
- 包名统一为 `agent`
- 编译通过

**依赖关系**: 无

---

### P2-6: RAG 重排序

**描述**: RAG 检索缺少重排序机制。需实现 LLM 重排序。

**新建文件**:
- `shiyu-ai-knowledge/src/main/java/com/shiyu/ai/knowledge/rag/reranker/Reranker.java`
- `shiyu-ai-knowledge/src/main/java/com/shiyu/ai/knowledge/rag/reranker/LlmReranker.java`

**子任务**:

- [ ] 定义 `Reranker` SPI
  - [ ] 定义 `rerank(String query, List<RetrievedChunk> chunks, int topK)` 方法
  
- [ ] 实现 `LlmReranker`
  - [ ] 调用 LLM 对检索结果重排序
  - [ ] 解析 LLM 返回的评分
  
- [ ] 集成到 `RagOrchestrator`
  - [ ] 在检索后调用重排序

**预估工时**: 6h

**验收标准**:
- 重排序生效
- 检索质量提升

**依赖关系**: P2-4（Knowledge 内部重构）

---

### P2-7: MCP 工具市场

**描述**: MCP 工具需支持注册与发现。

**新建文件**:
- `shiyu-ai-tool/src/main/java/com/shiyu/ai/tool/mcp/McpToolRegistry.java`
- `shiyu-ai-tool/src/main/java/com/shiyu/ai/tool/mcp/McpToolDescriptor.java`

**子任务**:

- [ ] 实现 `McpToolRegistry`
  - [ ] 注册 MCP 工具
  - [ ] 查询可用工具
  
- [ ] 实现工具发现
  - [ ] 从 MCP Server 获取工具列表
  - [ ] 缓存工具描述

**预估工时**: 8h

**验收标准**:
- MCP 工具可注册
- 工具列表可查询

**依赖关系**: P0-1（Core 模块拆分后，Tool 模块独立）

---

### P2-8: Plugin 系统（V3+）

**描述**: 支持第三方扩展热加载。优先级低，V3 之后再考虑。

**新建文件**:

```
shiyu-ai-plugin/src/main/java/com/shiyu/ai/plugin/
├── spi/
│   ├── Plugin.java
│   ├── PluginContext.java
│   └── PluginDescriptor.java
├── lifecycle/
│   ├── PluginManager.java
│   ├── PluginLoader.java
│   └── PluginState.java
├── registry/
│   └── PluginRegistry.java
└── sandbox/
    ├── PluginSandbox.java
    └── SecurityManager.java
```

**子任务**:

- [ ] 定义 `Plugin` SPI
- [ ] 实现 `PluginManager`
- [ ] 实现 `PluginLoader`（JAR 热加载）
- [ ] 实现沙箱隔离

**预估工时**: 20h

**验收标准**:
- Plugin 可热加载
- 沙箱隔离生效

**依赖关系**: V3 阶段任务

---

## 附录

### A. 已完成任务清单

以下任务已在 FIX-PLAN 中完成：

| 任务 | 文件 | 状态 |
|------|------|------|
| API Key 环境变量化 | `application-ai.yml` | ✅ |
| Password 字段不返前端 | `UserVO.java` | ✅ |
| Token 纯随机化 | `SaTokenConfig.java` | ✅ |
| Java 反序列化 → JSON | `SaTokenDaoImpl.java:574-583` | ✅ |
| Caffeine 缓存对齐 | `SaTokenDaoImpl.java:27-30` | ✅ |
| 日志脱敏（验证码） | `CaptchaServiceImpl.java` | ✅ |
| MySQL 坐标修正 | `pom.xml:281` | ✅ |
| XSS 验证器升级 | `XssValidator.java` | ✅ |
| 默认密码不共享 | `PasswordUtils.java` | ✅ |

### B. 技术引入清单

| 技术 | 用途 | 优先级 |
|------|------|--------|
| Flyway | 数据库迁移 | P1 |
| Resilience4j | 熔断、限流、降级 | P1 |
| Testcontainers | 集成测试 | P2 |
| MapStruct | 对象映射（已引入，需规范使用） | P2 |

### C. 执行顺序建议

```
Week 1-2: P0-1 (Core 拆分) + P0-7 (BO 收归)
Week 3-4: P0-2 (Agent Runtime) + P0-6 (事件中心)
Week 5-6: P0-3 (Memory) + P0-4 (VectorStore)
Week 7-8: P0-5 (Usage) + P1-2 (Observability)
Week 9-10: P1-1 (NodeFactory) + P1-3 (Flyway)
Week 11-12: P1-4~P1-6 + P2 任务
```

---

> **文档版本**: 1.0  
> **最后更新**: 2026-07-09  
> **维护者**: ShiYu AI Team
