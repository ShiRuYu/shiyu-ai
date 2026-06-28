package com.shiyu.ai.rag.impl;

import com.shiyu.ai.rag.RagService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * In-memory 模拟 RAG 检索服务实现
 * <p>
 * 在内存中维护文档存储，使用基于关键词的简单 TF (Term Frequency) 算法
 * 模拟向量数据库的相似度检索行为，支持多知识库隔离。
 * 适合开发/测试环境快速验证 RAG 流程。
 */
@Slf4j
@Service
public class RagServiceImpl implements RagService {

    /** 知识库 -> 文档列表 */
    private final Map<String, List<Document>> knowledgeBases = new HashMap<>();

    /** 默认知识库 ID */
    private static final String DEFAULT_KB = "default";

    @PostConstruct
    public void init() {
        log.info("初始化内存 RAG 检索服务，加载内置示例文档");
        loadDemoDocuments();
        log.info("内存 RAG 检索服务初始化完成，知识库数: {}", knowledgeBases.size());
    }

    // ======================== 公开管理 API ========================

    /**
     * 向指定知识库添加文档
     */
    public void addDocument(String knowledgeBaseId, Document document) {
        knowledgeBases.computeIfAbsent(knowledgeBaseId, k -> new CopyOnWriteArrayList<>())
                .add(document);
        log.debug("文档已添加到知识库 [{}]: id={}", knowledgeBaseId, document.id());
    }

    /**
     * 向指定知识库批量添加文档
     */
    public void addDocuments(String knowledgeBaseId, List<Document> documents) {
        knowledgeBases.computeIfAbsent(knowledgeBaseId, k -> new CopyOnWriteArrayList<>())
                .addAll(documents);
        log.debug("已向知识库 [{}] 批量添加 {} 篇文档", knowledgeBaseId, documents.size());
    }

    /**
     * 创建知识库
     */
    public void createKnowledgeBase(String knowledgeBaseId) {
        knowledgeBases.putIfAbsent(knowledgeBaseId, new CopyOnWriteArrayList<>());
        log.info("知识库已创建: {}", knowledgeBaseId);
    }

    /**
     * 获取知识库中的文档数
     */
    public int getDocumentCount(String knowledgeBaseId) {
        List<Document> docs = knowledgeBases.get(knowledgeBaseId);
        return docs != null ? docs.size() : 0;
    }

    /**
     * 获取所有知识库 ID
     */
    public Set<String> getKnowledgeBaseIds() {
        return knowledgeBases.keySet();
    }

    // ======================== RAG 检索 ========================

    @Override
    public RagRetrievalResult retrieve(String query, String knowledgeBaseId, int topK) {
        log.info("RAG 检索: query=[{}], kb=[{}], topK={}", query, knowledgeBaseId, topK);

        if (query == null || query.trim().isEmpty()) {
            return new RagRetrievalResult(false, List.of(), "查询文本不能为空");
        }

        try {
            String kbId = knowledgeBaseId != null ? knowledgeBaseId : DEFAULT_KB;
            List<Document> allDocs = knowledgeBases.getOrDefault(kbId, List.of());

            if (allDocs.isEmpty()) {
                log.warn("知识库 [{}] 中暂无文档", kbId);
                return new RagRetrievalResult(true, List.of(), null);
            }

            // 分词 & 计算 TF 分数
            String[] queryTerms = normalize(query).split("\\s+");
            List<ScoredDocument> scored = new ArrayList<>();

            for (Document doc : allDocs) {
                double score = computeSimilarity(queryTerms, doc.content());
                if (score > 0) {
                    scored.add(new ScoredDocument(doc, score));
                }
            }

            // 按分数降序排列，取 topK
            scored.sort((a, b) -> Double.compare(b.score, a.score));
            List<Document> results = scored.stream()
                    .limit(Math.max(1, topK))
                    .map(sd -> new Document(
                            sd.document.id(),
                            sd.document.content(),
                            sd.score,
                            enrichMetadata(sd.document, sd.score, kbId)
                    ))
                    .collect(Collectors.toList());

            log.info("RAG 检索完成: 匹配 {} 篇, 返回 {} 篇", scored.size(), results.size());
            return new RagRetrievalResult(true, results, null);

        } catch (Exception e) {
            log.error("RAG 检索失败", e);
            return new RagRetrievalResult(false, List.of(), "检索失败: " + e.getMessage());
        }
    }

    @Override
    public RagRetrievalResult retrieve(String query) {
        return retrieve(query, DEFAULT_KB, 5);
    }

    // ======================== 内部实现 ========================

    /**
     * 基于 TF (Term Frequency) 的相似度计算
     */
    private double computeSimilarity(String[] queryTerms, String content) {
        if (queryTerms.length == 0) {
            return 0;
        }
        String normalizedContent = normalize(content);
        int matchCount = 0;
        for (String term : queryTerms) {
            if (term.length() < 1) continue;
            // 统计该 term 在 content 中出现的次数
            int idx = 0;
            int count = 0;
            while ((idx = normalizedContent.indexOf(term, idx)) != -1) {
                count++;
                idx += term.length();
            }
            if (count > 0) {
                matchCount++;
            }
        }
        if (matchCount == 0) {
            return 0;
        }
        // 分数 = 匹配词比例 * 内容长度衰减因子 (短文本得分略高)
        double ratio = (double) matchCount / queryTerms.length;
        double lengthFactor = Math.min(1.0, 200.0 / (normalizedContent.length() + 1));
        return Math.min(0.99, ratio * 0.7 + lengthFactor * 0.3);
    }

    /**
     * 简单规范化：转小写、去标点、按空格分词
     */
    private String normalize(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5\\s]", " ")
                .trim();
    }

    /**
     * 丰富返回的元数据
     */
    private Map<String, Object> enrichMetadata(Document doc, double score, String kbId) {
        Map<String, Object> meta = new HashMap<>();
        if (doc.metadata() != null) {
            meta.putAll(doc.metadata());
        }
        meta.put("source", "kb_" + kbId);
        meta.put("score", score);
        meta.put("retrieved_at", System.currentTimeMillis());
        meta.put("retrieval_method", "in_memory_tf");
        return meta;
    }

    /**
     * 加载内置示例文档
     */
    private void loadDemoDocuments() {
        List<Document> defaultDocs = Arrays.asList(
                new Document("doc_demo_1", "Shiyu AI 是一个智能 Agent 平台，支持多模型接入、RAG 检索、工具调用、记忆管理等能力。",
                        1.0, Map.of("type", "intro", "author", "shiyu")),
                new Document("doc_demo_2", "RAG (Retrieval Augmented Generation) 是一种结合检索与生成的 AI 技术，"
                        + "通过从知识库中检索相关文档来增强大模型的回答质量。",
                        1.0, Map.of("type", "concept", "topic", "rag")),
                new Document("doc_demo_3", "LangGraph4j 是 Shiyu AI 内部实现的有向图编排框架，"
                        + "支持将多个 AI 节点（如意图识别、RAG 检索、LLM 调用、工具调用）组合成灵活的 Agent 工作流。",
                        1.0, Map.of("type", "framework", "topic", "langgraph")),
                new Document("doc_demo_4", "工具调用 (Tool Calling) 允许 Agent 与外部系统交互，"
                        + "例如查询天气、执行计算、调用 API 等。Shiyu AI 支持通过 MCP 协议注册和调用工具。",
                        1.0, Map.of("type", "feature", "topic", "tool")),
                new Document("doc_demo_5", "意图识别 (Intent Recognition) 是 Agent 理解用户需求的第一步，"
                        + "通过 LLM 分析用户输入并将其映射到预定义的意图上。",
                        1.0, Map.of("type", "feature", "topic", "intent")),
                new Document("doc_demo_6", "Shiyu AI 的记忆管理分为短期记忆和长期记忆："
                        + "短期记忆保存最近对话历史，长期记忆存储重要的用户偏好和知识点。",
                        1.0, Map.of("type", "feature", "topic", "memory"))
        );
        knowledgeBases.put(DEFAULT_KB, new CopyOnWriteArrayList<>(defaultDocs));
        log.info("已加载 {} 篇内置示例文档到默认知识库", defaultDocs.size());
    }

    /** 带分数的文档包装 */
    private record ScoredDocument(Document document, double score) {}
}
