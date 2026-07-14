package com.shiyu.ai.memory;

import com.shiyu.ai.memory.request.RetrieveMemoryRequest;
import com.shiyu.ai.memory.request.SaveLongTermMemoryRequest;
import com.shiyu.ai.memory.request.SaveMessageRequest;
import com.shiyu.ai.memory.spi.Memory;

import java.util.List;
import java.util.Map;

/**
 * 记忆服务（Memory Center）对外接口
 *
 * <p>职责范围：短期记忆、工作记忆、长期记忆、语义记忆、情景记忆的统一管理。
 * 调用方通过此接口执行记忆的保存、检索、整合和清理。</p>
 *
 * <h3>架构层次</h3>
 * <pre>
 *   MemoryService (编排层) → MemoryStore SPI (存储抽象) → DAL/DB/Vector
 * </pre>
 *
 * <p>内部使用 {@code MemoryStore} SPI 接口隔离底层存储实现，
 * 通过 {@code MemoryRecallStrategy} 实现跨存储层的混合召回。</p>
 */
public interface MemoryService {

    // ========================
    // 短期记忆 (Short-Term)
    // ========================

    /**
     * 保存一条对话消息到短期记忆。
     *
     * @param request 保存消息请求
     */
    void saveMessage(SaveMessageRequest request);

    /**
     * 保存一条对话消息到短期记忆。
     *
     * @param sessionId 会话 ID
     * @param userId    用户 ID（可选）
     * @param agentId   Agent ID（可选）
     * @param role      角色（user / assistant / system）
     * @param content   消息内容
     * @deprecated 请使用 {@link #saveMessage(SaveMessageRequest)}
     */
    @Deprecated
    default void saveMessage(String sessionId, Long userId, String agentId, String role, String content) {
        saveMessage(new SaveMessageRequest(sessionId, userId, agentId, role, content));
    }

    /**
     * 构建会话历史文本，用于构建 LLM 上下文。
     *
     * @param sessionId  会话 ID
     * @param maxMessages 最大消息数
     * @return 格式化后的历史文本
     */
    String buildConversationHistory(String sessionId, int maxMessages);

    /**
     * 检索短期记忆。
     *
     * @param sessionId 会话 ID
     * @param limit     最大条数
     * @return 短期记忆列表
     */
    List<Memory> retrieveShortTerm(String sessionId, int limit);

    // ========================
    // 长期记忆 (Long-Term)
    // ========================

    /**
     * 保存一条长期记忆。
     *
     * @param request 保存长期记忆请求
     */
    void saveLongTermMemory(SaveLongTermMemoryRequest request);

    /**
     * 保存一条长期记忆。
     *
     * @param userId    用户 ID
     * @param agentId   Agent ID
     * @param category  分类
     * @param key       记忆键
     * @param content   记忆内容
     * @param importance 重要度（0-1）
     * @param source    来源
     * @deprecated 请使用 {@link #saveLongTermMemory(SaveLongTermMemoryRequest)}
     */
    @Deprecated
    default void saveLongTermMemory(Long userId, String agentId, String category, String key,
                                    String content, double importance, String source) {
        saveLongTermMemory(SaveLongTermMemoryRequest.builder()
                .userId(userId).agentId(agentId).category(category).memoryKey(key)
                .content(content).importance(importance).source(source).build());
    }

    /**
     * 关键词搜索长期记忆。
     *
     * @param keyword 关键词
     * @param userId  用户 ID
     * @param agentId Agent ID
     * @param topK    最大结果数
     * @return 长期记忆列表（Map 格式，兼容旧调用方）
     * @deprecated 请使用 {@link #retrieve(RetrieveMemoryRequest)}
     */
    @Deprecated
    List<Map<String, Object>> searchLongTermMemory(String keyword, Long userId, String agentId, int topK);

    /**
     * 检索长期记忆（旧版，兼容）。
     *
     * @param query   检索关键词
     * @param userId  用户 ID
     * @param agentId Agent ID
     * @param topK    最大结果数
     * @return 长期记忆列表（Map 格式）
     * @deprecated 请使用 {@link #retrieve(RetrieveMemoryRequest)}
     */
    @Deprecated
    List<Map<String, Object>> retrieveLongTerm(String query, Long userId, String agentId, int topK);

    // ========================
    // 统一检索 (Cross-Store)
    // ========================

    /**
     * 跨记忆类型统一检索，支持按用户、Agent、关键词、类型过滤。
     *
     * <p>内部使用 {@code HybridRecallStrategy} 从多个 MemoryStore 中召回，
     * 按重要度排序并去重。</p>
     *
     * @param request 检索请求
     * @return 检索到的记忆列表
     */
    List<Memory> retrieve(RetrieveMemoryRequest request);

    // ========================
    // 会话摘要
    // ========================

    /**
     * 生成并存储会话摘要（通过 LLM 或截断回退）。
     *
     * @param sessionId 会话 ID
     * @param userId    用户 ID
     * @param agentId   Agent ID
     * @return 生成的摘要文本
     */
    String summarizeSession(String sessionId, Long userId, String agentId);

    /**
     * 获取已有会话摘要。
     *
     * @param sessionId 会话 ID
     * @return 摘要文本，不存在则返回 null
     */
    String getSessionSummary(String sessionId);

    // ========================
    // 生命周期管理
    // ========================

    /**
     * 清理超过指定天数的过期会话消息。
     *
     * @param maxDays 保留天数
     * @return 删除的消息条数
     */
    int cleanupExpiredSessions(int maxDays);

    /**
     * 自动计算并更新长期记忆的重要性分数（时效衰减）。
     *
     * @param userId  用户 ID
     * @param agentId Agent ID
     */
    void recalculateImportance(Long userId, String agentId);
}
