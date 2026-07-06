package com.shiyu.ai.memory;

import java.util.List;
import java.util.Map;

/**
 * Memory 接口
 */

public interface MemoryService {

    /**
     * Save Message
     * @return 处理结果
     */
    void saveMessage(String sessionId, Long userId, String agentId, String role, String content);

    /**
     * Build Conversation History
     * @return 处理结果
     */
    String buildConversationHistory(String sessionId, int maxMessages);

    /**
     * Save Long Term Memory
     * @param double double
     * @return 处理结果
     */
    void saveLongTermMemory(Long userId, String agentId, String category, String key, String content, double importance, String source);

    /**
     * Search Long Term Memory
     * @return 处理结果
     */
    List<Map<String, Object>> searchLongTermMemory(String keyword, Long userId, String agentId, int topK);

    /**
     * Retrieve Short Term
     * @return 处理结果
     */
    List<Map<String, Object>> retrieveShortTerm(String sessionId, int limit);

    /**
     * Retrieve Long Term
     * @return 处理结果
     */
    List<Map<String, Object>> retrieveLongTerm(String query, Long userId, String agentId, int topK);

    /** 生成并存储会话摘要 */
    String summarizeSession(String sessionId, Long userId, String agentId);

    /** 获取会话摘要 */
    String getSessionSummary(String sessionId);

    /** 清理过期会话（删除超过指定天数的消息） */
    int cleanupExpiredSessions(int maxDays);

    /** 自动计算并更新长期记忆重要性 */
    void recalculateImportance(Long userId, String agentId);
}