package com.shiyu.ai.memory.impl;

import com.shiyu.ai.memory.MemoryService;
import com.shiyu.ai.memory.request.RetrieveMemoryRequest;
import com.shiyu.ai.memory.request.SaveLongTermMemoryRequest;
import com.shiyu.ai.memory.request.SaveMessageRequest;
import com.shiyu.ai.memory.spi.Memory;
import com.shiyu.ai.memory.spi.MemoryQuery;
import com.shiyu.ai.memory.spi.MemoryStore;
import com.shiyu.ai.memory.spi.MemoryType;
import com.shiyu.ai.memory.service.ConsolidationService;
import com.shiyu.ai.memory.recall.HybridRecallStrategy;
import com.shiyu.ai.memory.recall.MemoryRecallStrategy;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 记忆服务实现
 *
 * <p>使用 {@link MemoryStore SPI} 隔离底层存储，通过 {@link MemoryRecallStrategy}
 * 实现跨存储层的混合召回。不再直接依赖 DAL Repository 和 ChatEngine。</p>
 */
@Slf4j
public class MemoryServiceImpl implements MemoryService {

    private static final String ROLE_USER = "user";
    private static final String ROLE_ASSISTANT = "assistant";

    private final List<MemoryStore> stores;
    private final MemoryRecallStrategy recallStrategy;
    private final ConsolidationService consolidationService;

    /** 按类型查找 Store 的快捷引用 */
    private MemoryStore shortTermStore;
    private MemoryStore longTermStore;
    private MemoryStore workingStore;
    private MemoryStore semanticStore;
    private MemoryStore episodicStore;

    public MemoryServiceImpl(List<MemoryStore> stores,
                             MemoryRecallStrategy recallStrategy,
                             ConsolidationService consolidationService) {
        this.stores = stores;
        this.recallStrategy = recallStrategy;
        this.consolidationService = consolidationService;
        initStoreReferences();
    }

    private void initStoreReferences() {
        for (MemoryStore store : stores) {
            // 通过 query 一个空查询来试探类型 — 更好的方式是通过 Bean 名称或类型注入，
            // 这里用简单试探。实际部署通过 AutoConfiguration 传入具名引用。
            MemoryQuery probe = MemoryQuery.builder().sessionId("__probe__").topK(1).build();
            List<Memory> result = store.query(probe);
            // 根据 store 的行为判断类型（生产环境建议使用 NamedBean 方式）
            if (shortTermStore == null && store.count(probe) == 0) {
                // 试探后无副作用，暂且赋值；由 AutoConfiguration 确保正确绑定
            }
        }
        // AutoConfiguration 中通过构造函数直接注入各 Store 的引用
    }

    /**
     * 设置各 Store 的显式引用（由 AutoConfiguration 调用）。
     */
    public void setStoreReferences(MemoryStore shortTermStore, MemoryStore longTermStore,
                                    MemoryStore workingStore, MemoryStore semanticStore,
                                    MemoryStore episodicStore) {
        this.shortTermStore = shortTermStore;
        this.longTermStore = longTermStore;
        this.workingStore = workingStore;
        this.semanticStore = semanticStore;
        this.episodicStore = episodicStore;
    }

    // ========================
    // 短期记忆
    // ========================

    @Override
    public void saveMessage(SaveMessageRequest request) {
        if (request.getSessionId() == null || request.getContent() == null) return;

        Memory memory = new Memory(MemoryType.SHORT_TERM, request.getSessionId(),
                request.getRole(), request.getContent());
        memory.setUserId(request.getUserId());
        memory.setAgentId(request.getAgentId());

        if (shortTermStore != null) {
            shortTermStore.save(memory);
        }
        log.debug("保存对话消息: sessionId={}, role={}, len={}",
                request.getSessionId(), request.getRole(), request.getContent().length());
    }

    @Override
    public String buildConversationHistory(String sessionId, int maxMessages) {
        if (shortTermStore == null) return "";

        MemoryQuery query = MemoryQuery.builder()
                .sessionId(sessionId)
                .topK(maxMessages)
                .build();
        List<Memory> messages = shortTermStore.query(query);

        if (messages.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (Memory msg : messages) {
            String role = msg.getRole();
            String content = msg.getContent();
            if (ROLE_USER.equals(role)) {
                sb.append("用户: ").append(content).append("\n");
            } else if (ROLE_ASSISTANT.equals(role)) {
                sb.append("助手: ").append(content).append("\n");
            } else {
                sb.append(role).append(": ").append(content).append("\n");
            }
        }
        return sb.toString();
    }

    @Override
    public List<Memory> retrieveShortTerm(String sessionId, int limit) {
        if (shortTermStore == null) return List.of();

        MemoryQuery query = MemoryQuery.builder()
                .sessionId(sessionId)
                .topK(limit)
                .build();
        return shortTermStore.query(query);
    }

    // ========================
    // 长期记忆
    // ========================

    @Override
    public void saveLongTermMemory(SaveLongTermMemoryRequest request) {
        if (request.getContent() == null || request.getContent().isBlank()) return;

        Memory memory = new Memory(MemoryType.LONG_TERM, null, "system", request.getContent());
        memory.setUserId(request.getUserId());
        memory.setAgentId(request.getAgentId());
        memory.setCategory(request.getCategory() != null ? request.getCategory() : "general");
        memory.setMemoryKey(request.getMemoryKey());
        memory.setImportance(request.getImportance());
        memory.setSource(request.getSource());

        if (longTermStore != null) {
            longTermStore.save(memory);
        }
        log.info("保存长期记忆: userId={}, category={}, importance={}",
                request.getUserId(), request.getCategory(), request.getImportance());
    }

    @Override
    public List<Map<String, Object>> searchLongTermMemory(String keyword, Long userId, String agentId, int topK) {
        if (longTermStore == null) return List.of();

        MemoryQuery query = MemoryQuery.builder()
                .keyword(keyword)
                .userId(userId)
                .agentId(agentId)
                .topK(topK)
                .build();
        List<Memory> memories = longTermStore.query(query);
        return toMapList(memories);
    }

    @Override
    public List<Map<String, Object>> retrieveLongTerm(String query, Long userId, String agentId, int topK) {
        return searchLongTermMemory(query, userId, agentId, topK);
    }

    // ========================
    // 统一检索
    // ========================

    @Override
    public List<Memory> retrieve(RetrieveMemoryRequest request) {
        MemoryQuery query = MemoryQuery.builder()
                .sessionId(request.getSessionId())
                .userId(request.getUserId())
                .agentId(request.getAgentId())
                .keyword(request.getKeyword() != null ? request.getKeyword() : request.getQuery())
                .category(request.getCategory())
                .topK(request.getTopK())
                .minImportance(request.getMinImportance())
                .types(request.getTypes())
                .build();

        // 如果指定了类型，只从对应 Store 查询；否则使用混合召回
        if (request.getTypes() != null && !request.getTypes().isEmpty()) {
            List<Memory> results = new ArrayList<>();
            for (MemoryStore store : stores) {
                List<Memory> batch = store.query(query);
                results.addAll(batch);
            }
            // 按重要性去重排序
            return deduplicateAndSort(results, request.getTopK());
        }

        return recallStrategy.recall(query);
    }

    // ========================
    // 会话摘要
    // ========================

    @Override
    public String summarizeSession(String sessionId, Long userId, String agentId) {
        String history = buildConversationHistory(sessionId, 50);
        return consolidationService.summarize(sessionId, history, userId, agentId);
    }

    @Override
    public String getSessionSummary(String sessionId) {
        return consolidationService.getSessionSummary(sessionId);
    }

    // ========================
    // 生命周期
    // ========================

    @Override
    public int cleanupExpiredSessions(int maxDays) {
        return consolidationService.cleanupExpiredSessions(maxDays);
    }

    @Override
    public void recalculateImportance(Long userId, String agentId) {
        consolidationService.recalculateImportance(userId, agentId);
    }

    // ========================
    // 内部工具
    // ========================

    private List<Map<String, Object>> toMapList(List<Memory> memories) {
        return memories.stream().map(m -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", m.getMemoryId());
            map.put("content", m.getContent());
            map.put("category", m.getCategory());
            map.put("importance", m.getImportance());
            map.put("memoryKey", m.getMemoryKey());
            map.put("timestamp", m.getCreatedAt());
            return map;
        }).collect(Collectors.toList());
    }

    private List<Memory> deduplicateAndSort(List<Memory> results, int topK) {
        java.util.Set<String> seen = new java.util.HashSet<>();
        List<Memory> deduped = new ArrayList<>();
        for (Memory mem : results) {
            String key = mem.getSessionId() + ":" + mem.getContent();
            if (seen.add(key)) {
                deduped.add(mem);
            }
        }
        deduped.sort((a, b) -> Double.compare(b.getImportance(), a.getImportance()));
        return deduped.stream().limit(topK).collect(Collectors.toList());
    }
}
