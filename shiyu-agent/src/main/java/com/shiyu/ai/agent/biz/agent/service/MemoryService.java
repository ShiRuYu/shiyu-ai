package com.shiyu.ai.agent.biz.agent.service;

import java.util.List;
import java.util.Map;

public interface MemoryService {

    void saveMessage(String sessionId, Long userId, String agentId, String role, String content);

    String buildConversationHistory(String sessionId, int maxMessages);

    void saveLongTermMemory(Long userId, String agentId, String category, String key, String content, double importance, String source);

    List<Map<String, Object>> searchLongTermMemory(String keyword, Long userId, String agentId, int topK);

    List<Map<String, Object>> retrieveShortTerm(String sessionId, int limit);

    List<Map<String, Object>> retrieveLongTerm(String query, Long userId, String agentId, int topK);
}