# VectorStore + RAG + RogueMap 架构设计

版本：v1.0  
组件：H2 + VectorStore(SPI) + RogueMap + EmbeddingService

---

## 1. 总体架构

```
                    AI Module

          ┌────────────┴────────────┐
          │                         │
     MemoryService          KnowledgeService
          │                         │
   roguemap-memory          VectorStore (SPI)
     (不修改)                 ├── InMemory   ← 默认/开发
                             ├── HNSW       ← 嵌入式/百万级
                             └── Qdrant     ← 生产/百万+
          │                         │
          └────────────┬────────────┘
                       │
                EmbeddingService
```

## 2. 核心设计原则

### 2.1 数据分层

| 层级 | 存储 | 职责 |
|------|------|------|
| H2 | 事实数据 | Chunk/文档内容、Metadata（唯一 Source of Truth） |
| VectorStore | 向量索引 | embedding 检索（可重建、无状态） |
| RogueMap | 图结构 | 知识点依赖、前置关系、学习路径 |

### 2.2 原则

- embedding 与业务数据解耦
- VectorStore 可插拔（零改代码切换后端）
- H2 为唯一 source of truth
- 索引可重建（无状态）

## 3. EmbeddingService

位于 `shiyu-ai-core/embedding/`，被 KnowledgeService 和 VectorStore 共用。

```java
public interface EmbeddingService {
    float[] embed(String text);
    List<float[]> embedBatch(List<String> texts);
    int dimension();
}
```

默认实现：`LangChain4jEmbeddingService`（AllMiniLmL6V2 ONNX 模型，384 维）

实现可替换：传入自定义 `EmbeddingModel` 即可。

## 4. VectorStore SPI

位于 `shiyu-ai-knowledge/vector/`。

### 4.1 接口

```java
public interface VectorStore {
    void upsert(VectorRecord record);
    default void upsertBatch(List<VectorRecord> records);
    List<VectorRecord> search(float[] queryVector, int topK);
    void delete(String id);
    default void rebuild() {}
    default boolean supportFilter() { return false; }
}
```

### 4.2 VectorRecord

```java
public record VectorRecord(
    String id,
    float[] vector,
    Map<String, Object> metadata
) {}
```

### 4.3 后端实现

| 实现 | 类名 | 算法 | 适用规模 | 依赖 |
|------|------|------|---------|------|
| InMemory | `InMemoryVectorStore` | 余弦相似度暴力扫描 O(n) | <10 万 | 无 |
| HNSW | `HnswVectorStore` | HNSW 近似最近邻 O(log n) | 10-100 万 | usearch（可选） |
| Qdrant | `QdrantVectorStore` | 内置 HNSW + payload filter | 100 万+ | qdrant-client（可选） |

HNSW 和 Qdrant 通过反射加载，库不在 classpath 时自动降级或抛明确提示。

### 4.4 配置

```yaml
shiyu:
  vector-store:
    type: inmemory          # inmemory | hnsw | qdrant
    dimension: 384
    data-dir: ${app.home}/data/vector
    qdrant:
      host: localhost
      port: 6334
      collection: knowledge
    hnsw:
      index-path: ${app.home}/data/vector/hnsw.index
```

## 5. 数据模型

### 5.1 knowledge_chunk 表（H2）

```sql
CREATE TABLE `knowledge_chunk` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `document_id`   BIGINT       NOT NULL,
    `content`       TEXT         NOT NULL,
    `embedding`     TEXT         DEFAULT NULL COMMENT 'JSON float array',
    `metadata`      TEXT         DEFAULT NULL COMMENT 'JSON map',
    `chunk_index`   INT          DEFAULT 0,
    `create_by`     VARCHAR(64)  DEFAULT NULL,
    `create_time`   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_kc_document` (`document_id`)
);
```

## 6. 文档写入链路

```
Document → ChunkSplitter → EmbeddingService → H2 (knowledge_chunk) + VectorStore
```

### 6.1 ChunkSplitter（中文优化）

位于 `shiyu-ai-knowledge/rag/`。

```
public interface ChunkSplitter {
    List<Chunk> split(String text);
    record Chunk(String content, int index, int startPos, int endPos) {}
}
```

`ChineseChunkSplitter` 实现策略：

1. 按标题切分（`# `, `第X章`, `1. `, `一、`）
2. 无标题时按段落切分（`\n\n`）
3. 合并为 300-800 tokens 的 Chunk
4. overlap 50 tokens

### 6.2 DocumentIngestionService

收到文档后：

1. `ChunkSplitter.split(content)` → 切分
2. `EmbeddingService.embed(chunk)` → 向量化
3. 写入 H2 `knowledge_chunk` 表
4. `VectorStore.upsertBatch()` → 写入向量索引

## 7. RAG 查询链路

```
User Query
    ↓
EmbeddingService.embed(query)
    ↓
VectorStore.search(queryVector, topK)
    ↓
H2 fetch chunk content by IDs
    ↓
RogueMap.enrich (补知识图谱上下文)
    ↓
LLM generate answer
```

### 7.1 RogueMap.enrich

为检索到的 Chunk 补上知识图谱结构关系：

- `GraphStore.parents(id)` → 前置知识
- `GraphStore.children(id)` → 后续知识
- `GraphStore.related(id)` → 相关知识

输出格式：
```
[知识图谱上下文]
前置知识: 函数定义 → 一次函数 → 当前
后续知识: 当前 → 斜率与截距
相关知识: 正比例函数, 反比例函数
```

### 7.2 RagOrchestrator

位于 `shiyu-ai-knowledge/rag/`，编排完整查询链路：

```java
public class RagOrchestrator {
    public RagResult retrieve(String query, int topK) {
        // 1. Embedding
        // 2. VectorStore.search
        // 3. H2 fetch
        // 4. RogueMap.enrich
    }

    public record RagResult(List<RagChunk> chunks, String graphContext) {}
}
```

### 7.3 在 Agent 中的使用

`RagRetrievalNode` 通过 `RagService` 调用 `RagOrchestrator`。

`buildContextFromDocuments()` 输出格式：

```
[文档 1]
函数定义：y = kx + b ...

[文档 2]
斜率公式：k = (y₂-y₁)/(x₂-x₁) ...

[知识图谱上下文]
前置知识: 函数定义 → 一次函数 → 当前
后续知识: 当前 → 斜率与截距
```

## 8. KnowledgeSearchService + VectorStore

知识点搜索新增 `SearchMode.VECTOR` 路径：

```java
public List<SearchResult> search(String query, int topK, SearchMode mode) {
    if (mode == SearchMode.VECTOR) {
        return vectorSearch(query, topK);
    }
    // KEYWORD / SEMANTIC / HYBRID → RogueMemory（不变）
}
```

`vectorSearch` 流程：`embed(query) → VectorStore.search → 解析 metadata → SearchResult`

知识点索引时自动同步到 VectorStore（id 格式 `kp_{id}`）。

## 9. 依赖关系

```
shiyu-ai-core (EmbeddingService)
    ↑
shiyu-ai-knowledge
  ├── vector/     (VectorStore SPI + InMemory/HNSW/Qdrant)
  ├── rag/        (ChunkSplitter + DocumentIngestion + RagOrchestrator)
  ├── search/     (KnowledgeSearchService + VECTOR mode)
  └── graph/      (RogueMap GraphStore)
    ↑
shiyu-ai-rag (RagServiceImpl → RagOrchestrator)
shiyu-ai-memory (不修改，独立 RogueMemory)
    ↑
shiyu-ai-agent (RagRetrievalNode → RagService)
```

## 10. 未来扩展

- LanceDbVectorStore / MilvusVectorStore / PgVectorVectorStore
- Hybrid Search（BM25 + Vector RRF 融合）
- Multi-Vector（chunk + sentence + doc）
- Memory + Knowledge 融合
- Graph-RAG（RogueMap + VectorStore）
- 数据迁移工具（H2 → Qdrant, HNSW → Qdrant）
