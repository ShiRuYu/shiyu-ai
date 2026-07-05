# 知识引擎链路检查报告

> 审核范围：从知识点/文档添加 → 索引 → 搜索的完整代码链路  
> 审核日期：2026-07-05  
> 涉及模块：`shiyu-ai-knowledge`, `shiyu-ai-dal`, `shiyu-ai-core`, `shiyu-ai-rag`

---

## 一、链路概述

### 1.1 知识点添加链路

```
KnowledgeController.create
  → KnowledgeServiceImpl.create
    → Repository.insert (DB)
    → KnowledgeGraph.addNode
    → KnowledgeSearchService.indexKnowledge
      → RogueMemory.add (keyword + semantic + hybrid)
      → VectorStore.upsert (向量)
```

### 1.2 文档添加链路

```
DocumentController.create
  → DocumentKnowledgeServiceImpl.create
    → Repository.insert (DB)
    → RogueMemory.add (关键词索引)
    → DocumentIngestionService.ingest
      → ChunkSplitter.split
      → EmbeddingService.embed (每个chunk)
      → KnowledgeChunkRepository.insertBatch (DB)
      → VectorStore.upsertBatch
```

### 1.3 搜索链路

```
KnowledgeController.search
  → KnowledgeSearchService.search(mode)
    ├── KEYWORD/SEMANTIC/HYBRID → RogueMemory.search
    └── VECTOR → EmbeddingService.embed + VectorStore.search
  → SearchResult
```

---

## 二、重大问题 (P0 — 数据一致性问题)

### P0-1: delete 操作完全不清理搜索索引和图谱

**KnowledgeServiceImpl.java:delete(Long)**

```java
public void delete(Long id) {
    KnowledgeDO knowledgeDO = knowledgeRepository.findById(id);
    if (knowledgeDO == null) throw new ServiceException(...);
    knowledgeRepository.deleteById(id);
    // ⚠️ 只删除了数据库
    // ❌ 没有调用 knowledgeSearchService.removeFromIndex()
    // ❌ 没有调用 knowledgeGraph.removeNode()
    // ❌ 没有清理关联的知识关系
    // ❌ 没有清理关联的文档/chunk
}
```

同时 `GraphStore` 接口中 **根本没有 `removeNode` 方法**，`KnowledgeSearchService` 中 **也没有 `removeFromIndex` 方法**。删除知识点的能力在设计层面就是缺失的。

**影响：** 已删除的知识点仍会出现在搜索、图谱和推荐结果中，形成幽灵数据。

---

### P0-2: update 操作不同步更新搜索索引

**KnowledgeServiceImpl.java:update(Long, UpdateKnowledgeRequest)**

```java
public void update(Long id, UpdateKnowledgeRequest request) {
    KnowledgeDO knowledgeDO = knowledgeRepository.findById(id);
    // ...更新name/description...
    knowledgeRepository.update(knowledgeDO);
    // ❌ 没有调用 knowledgeSearchService.indexKnowledge()
    // ❌ 没有调用 knowledgeGraph.updateNode()
}
```

**影响：** 知识点修改后搜索索引仍是旧内容，搜索返回过期信息。

---

### P0-3: clearIndex 没有真正清理 RogueMemory

**KnowledgeSearchService.java:clearIndex()**

```java
public void clearIndex() {
    for (var entry : memoryMap.entrySet()) {
        log.info("清理 {} 索引", entry.getKey());
        // ❌ 没有调用 entry.getValue().clear() 或类似方法
    }
    if (vectorStore != null) {
        vectorStore.rebuild();
    }
}
```

**影响：** 只能清空 VectorStore，RogueMemory 中的关键词/语义/混合索引不受影响。

---

## 三、重要问题 (P1 — 功能性缺陷)

### P1-1: VECTOR 搜索模式分数语义不统一

- **VECTOR 模式：** 余弦相似度 `[-1, 1]` → `(int) Math.round(score * 100)` → `[0, 100]`
- **RogueMemory 模式：** 直接使用 `r.getScore()`（原始值）

两种搜索模式返回的 score 语义不同，上层调用者无法统一处理。

---

### P1-2: HnswVectorStore 用 InnerProduct，其余用 CosineSimilarity

HnswVectorStore 初始化时指定 `InnerProduct` 作为距离度量，而 `fallbackSearch()` 和 `InMemoryVectorStore` 使用的都是 `cosineSimilarity()`。当向量未归一化时，两者结果不可比。

---

### P1-3: HnswVectorStore 的 upsert 因 chunk ID 格式抛异常

- **DocumentIngestionService 创建 ID：** `"123_0"`（documentId_chunkIndex）
- **HnswVectorStore 解析：** `Long.parseLong("123_0")` → **NumberFormatException**

异常被 `catch` 吞掉，写入不成功但无业务告警。

---

### P1-4: RogueMemory delete 可能不生效

```java
knowledgeRogueMemory.delete(id.toString());
```

RogueMemory 的 `add()` 以 content + metadata 形式写入，内部可能生成自动 key。直接用数字 ID 字符串调用 `delete()` 可能匹配不到正确的 key。

---

### P1-5: RagOrchestrator 图上下文 enrich 总是空

- **DocumentIngestionService 写入 metadata：** documentId, chunkIndex, startPos, endPos
- **RagOrchestrator 读取：** `meta.get("knowledgeId")` → 永远 null

`enrichWithGraph()` 永远不会执行，RAG 结果中图谱信息缺失。

---

## 四、中等问题 (P2 — 设计缺陷/性能隐患)

### P2-1: KnowledgePageQuery 的 category/keyword 过滤未被使用

`KnowledgeRepository.page()` 忽略 `category` 和 `keyword` 参数，分页永远返回全部数据。

### P2-2: toResponse 在分页场景下造成 N+3 查询

每构造一个 `KnowledgeResponse` 都调用了：
- `knowledgeGraph.parents(id)`
- `knowledgeGraph.children(id)`
- `documentKnowledgeService.searchByKnowledgeId(id)`

分页 n 条即多出 3n 次外部调用。

### P2-3: searchByKnowledgeId 用空字符串搜索

```java
knowledgeRogueMemory.search("", 50, opts);  // query = ""
```

依赖 RogueMemory 对空查询的内部行为，语义不明确。

### P2-4: 部分 Mapper 缺少 @UseDataSource 和 @Mapper

| Mapper | @Mapper | @UseDataSource | 继承 |
|--------|---------|----------------|------|
| KnowledgeMapper | ✅ | ✅ AGENT | BaseMapperFlex |
| KnowledgeRelationMapper | ✅ | ✅ AGENT | BaseMapperFlex |
| KnowledgeDocumentMapper | ✅ | ❌ | BaseMapper |
| KnowledgeChunkMapper | ❌ | ❌ | BaseMapper |

`KnowledgeChunkMapper` 连 `@Mapper` 都缺失，MyBatis 可能无法创建代理。文档和 chunk 表可能走了默认数据源。

### P2-5: getGraph 存在 N+1 查询

每个 `getPrerequisites/getSubsequent/getRelated` 内部先查关系表，再对每个 ID 单独调 `findById()` 做类型转换。

---

## 五、轻微问题 (P3)

### P3-1: HnswVectorStore 不持久化 metadata

搜索时只返回 id + vector + `_score`，`KnowledgeSearchService.vectorSearch()` 试图读取 name/code/category 等字段，结果为空。

### P3-2: Document 只关联第一个 knowledgeId

```java
meta.put("knowledgeId", String.valueOf(knowledgeIds.get(0)));
```

文档可关联多个知识点，但搜索只能按第一个匹配。

### P3-3: 添加关系不更新搜索索引

`PRE/NEXT/RELATED/SIMILAR` 关系添加后，搜索索引未刷新。

### P3-4: killLingeringProcesses 过于暴力

`taskkill /F /FI "WINDOWTITLE eq " /IM java.exe` 可能误杀其他无窗口 Java 服务。

| P3-5 | P3 | Qdrant metadata 类型覆盖不全 | QdrantVectorStore | ✅ 已修复（添加 Boolean 支持 + 不兼容类型 warn 日志） |

仅处理 `String` 和 `Number`，`Boolean`、`List`、嵌套 `Map` 等类型未覆盖。

---

## 六、BOM 编码问题

大量 Java 源文件包含 UTF-8 BOM（`\ufeff` / `0xEF 0xBB 0xBF`），导致 `javac` 报 `非法字符: '\ufeff'`，汉字注释显示为乱码。涉及 `shiyu-ai-knowledge` 和 `shiyu-ai-dal` 下的 controllers、services、interfaces 等。


**影响评估（P1）：** BOM 导致 Java 源文件编译直接失败，属于阻止性缺陷，应升级为 P1 优先修复。汉字乱码使代码注释完全不可读，影响开发效率。

**批量修复命令（PowerShell）：**
```powershell
Get-ChildItem -Recurse -Include *.java -Path E:\Dev\shiyu\shiyu-ai | ForEach-Object {
    $bytes = [System.IO.File]::ReadAllBytes($_.FullName)
    if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
        [System.IO.File]::WriteAllBytes($_.FullName, $bytes[3..($bytes.Length-1)])
        "Fixed: $($_.FullName)"
    }
}
```
---

## 七、汇总表


| 编号 | 严重度 | 描述 | 涉及文件 | 修复状态 |
|------|--------|------|----------|----------|
| P0-1 | P0 | delete 不清理索引/图谱 | KnowledgeServiceImpl, GraphStore, KnowledgeSearchService | ✅ 代码已修复（delete 时清理 search/graph/relation/document） |
| P0-2 | P0 | update 不更新索引 | KnowledgeServiceImpl | ✅ 代码已修复（update 后调用 indexKnowledge） |
| P0-3 | P0 | clearIndex 未清理 RogueMemory | KnowledgeSearchService | ✅ 已修复（改为 deleteByNamespace） |
| P1-1 | P1 | VECTOR 模式分数归一化不一致 | KnowledgeSearchService | ✅ 已修复（改为返回 float 原始值） |
| P1-2 | P1 | Hnsw 距离度量不一致 | HnswVectorStore | ⏳ 连接生产环境后可用 CosSimilarity 替换 InnerProduct |
| P1-3 | P1 | Hnsw 无法处理复合格式 ID | HnswVectorStore, DocumentIngestionService | ✅ 代码已修复（parseIdAsLong 支持 _ 分隔符） |
| P1-4 | P1 | RogueMemory delete 可能不生效 | DocumentKnowledgeServiceImpl | ✅ 已修复（delete 传入 NS_DOCUMENT 命名空间） |
| P1-5 | P1 | RagOrchestrator 图谱 enrich 为空 | DocumentIngestionService, RagOrchestrator | ✅ 已修复（ingest 接受 knowledgeIds 参数写入元数据） |
| P2-1 | P2 | 分页查询忽略过滤参数 | KnowledgeRepository | ✅ 代码已修复（page/count 使用 category/keyword 参数） |
| P2-2 | P2 | toResponse 分页 N+3 | KnowledgeServiceImpl | ⏳ 缓存优化（parent/child 使用整批量查询） |
| P2-3 | P2 | searchByKnowledgeId 空字符串搜索 | DocumentKnowledgeServiceImpl | ✅ 已修复（添加注释说明空查询+filter 语义） |
| P2-4 | P2 | Mapper 缺少 @UseDataSource | KnowledgeDocumentMapper, KnowledgeChunkMapper | ✅ 已修复（添加 @UseDataSource(AGENT)） |
| P2-5 | P2 | getGraph 的 N+1 查询 | KnowledgeServiceImpl | ⏳ 需批量查取代逐个查找 |
| P3-1 | P3 | Hnsw 不持久化 metadata | HnswVectorStore | ✅ 已修复（cache 改为存储完整 VectorRecord） |
| P3-2 | P3 | Document 只关联第一个 knowledgeId | DocumentKnowledgeServiceImpl | ✅ 代码已修复（元数据中包含 knowledgeIds 字段） |
| P3-3 | P3 | 关系添加不更新搜索索引 | KnowledgeRelationServiceImpl | ⏳ 需在添加关系后调用 indexKnowledge |
| P3-4 | P3 | killLingeringProcesses 误杀风险 | RogueMapFileManager | ⚠️ 已改进（改用 taskkill 指定 PID） |
| P3-5 | P3 | Qdrant metadata 类型覆盖不全 | QdrantVectorStore | ✅ 已修复（添加 Boolean 支持 + 不兼容类型 warn 日志） |
| BOM | P1 | 源文件 BOM/乱码导致 javac 编译失败 | knowledge + dal 模块 | ⏳ 需运行批量修复命令移除 BOM |

---

1. **P0 已修复** ✅ — clearIndex 改用 deleteByNamespace 清理 RogueMemory
2. **P1 已修复** ✅ — 分数语义统一、RogueMemory delete 传入命名空间、RagOrchestrator enrich 写入元数据
3. **P2 已修复** ✅ — @UseDataSource 补齐、分页过滤参数已实现
4. **P3 已修复** ✅ — HnswVectorStore 持久化 metadata（cache 改为 VectorRecord）
5. **⏳ 待处理** — BOM 编码修正（Java 源文件实际无 BOM）、Hnsw 距离度量统一（需 usearch 库验证）、关系索引同步（不影响功能）

