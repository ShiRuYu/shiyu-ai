package com.shiyu.ai.memory.impl;

import com.shiyu.ai.memory.MemoryService;
import com.yomahub.roguemap.memory.MemoryResult;
import com.yomahub.roguemap.memory.RogueMemory;
import com.yomahub.roguemap.memory.SearchOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@ConditionalOnBean(RogueMemory.class)
public class RogueMemoryServiceImpl implements MemoryService {

    private static final String NS_CONVERSATION = "conversation";
    private static final String NS_LONG_TERM = "longterm";
    private static final String FIELD_SESSION_ID = "sessionId";
    private static final String FIELD_ROLE = "role";
    private static final String FIELD_USER_ID = "userId";
    private static final String FIELD_AGENT_ID = "agentId";
    private static final String FIELD_IMPORTANCE = "importance";
    private static final String FIELD_CATEGORY = "category";

    private final RogueMemory rogueMemory;

    public RogueMemoryServiceImpl(RogueMemory rogueMemory) {
        this.rogueMemory = rogueMemory;
    }

    @Override
    public void saveMessage(String sessionId, Long userId, String agentId, String role, String content) {
        String msg = role + ": " + content;
        Map<String, String> metadata = new HashMap<>();
        metadata.put(FIELD_SESSION_ID, sessionId);
        metadata.put(FIELD_ROLE, role);
        if (userId != null) metadata.put(FIELD_USER_ID, String.valueOf(userId));
        if (agentId != null) metadata.put(FIELD_AGENT_ID, agentId);

        rogueMemory.add(msg, metadata, NS_CONVERSATION);
        log.debug("保存对话消息到 RogueMemory: sessionId={}, role={}", sessionId, role);
    }

    @Override
    public String buildConversationHistory(String sessionId, int maxMessages) {
        List<MemoryResult> results = rogueMemory.search("", maxMessages,
                SearchOptions.builder()
                        .namespace(NS_CONVERSATION)
                        .filter(FIELD_SESSION_ID, sessionId)
                        .build());
        if (results.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (MemoryResult r : results) {
            sb.append(r.getContent()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public void saveLongTermMemory(Long userId, String agentId, String category, String key,
                                    String content, double importance, String source) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put(FIELD_USER_ID, String.valueOf(userId));
        if (agentId != null) metadata.put(FIELD_AGENT_ID, agentId);
        metadata.put(FIELD_CATEGORY, category != null ? category : "general");
        metadata.put(FIELD_IMPORTANCE, String.valueOf(importance));
        if (source != null) metadata.put("source", source);

        rogueMemory.add(content, metadata, NS_LONG_TERM);
        log.info("保存长期记忆到 RogueMemory: userId={}, category={}", userId, category);
    }

    @Override
    public List<Map<String, Object>> searchLongTermMemory(String keyword, Long userId, String agentId, int topK) {
        SearchOptions.Builder opts = SearchOptions.builder()
                .namespace(NS_LONG_TERM);
        if (userId != null) {
            opts.filter(FIELD_USER_ID, String.valueOf(userId));
        }
        List<MemoryResult> results = rogueMemory.search(keyword != null ? keyword : "", topK, opts.build());
        return toResultList(results);
    }

    @Override
    public List<Map<String, Object>> retrieveShortTerm(String sessionId, int limit) {
        List<MemoryResult> results = rogueMemory.search("", limit,
                SearchOptions.builder()
                        .namespace(NS_CONVERSATION)
                        .filter(FIELD_SESSION_ID, sessionId)
                        .build());
        List<Map<String, Object>> list = new ArrayList<>();
        for (MemoryResult r : results) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", r.getId());
            map.put("content", r.getContent());
            map.put("sessionId", r.getMetadata() != null ? r.getMetadata().get(FIELD_SESSION_ID) : null);
            map.put("role", r.getMetadata() != null ? r.getMetadata().get(FIELD_ROLE) : null);
            map.put("score", r.getScore());
            list.add(map);
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> retrieveLongTerm(String query, Long userId, String agentId, int topK) {
        return searchLongTermMemory(query, userId, agentId, topK);
    }

    @Override
    public String summarizeSession(String sessionId, Long userId, String agentId) {
        return ""; // RogueMemory 暂不实现摘要
    }

    @Override
    public String getSessionSummary(String sessionId) {
        return null;
    }

    @Override
    public int cleanupExpiredSessions(int maxDays) {
        return 0; // RogueMemory 暂不实现清理
    }

    @Override
    public void recalculateImportance(Long userId, String agentId) {
        // RogueMemory 暂不实现重要性重算
    }

    private List<Map<String, Object>> toResultList(List<MemoryResult> results) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (MemoryResult r : results) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", r.getId());
            map.put("content", r.getContent());
            map.put("score", r.getScore());
            map.put("metadata", r.getMetadata());
            list.add(map);
        }
        return list;
    }
}
