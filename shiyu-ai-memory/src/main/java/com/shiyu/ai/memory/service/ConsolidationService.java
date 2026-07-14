package com.shiyu.ai.memory.service;

import com.shiyu.ai.dal.bo.memory.LongTermMemoryBO;
import com.shiyu.ai.dal.repository.ConversationMessageRepository;
import com.shiyu.ai.dal.repository.LongTermMemoryRepository;
import com.shiyu.ai.memory.request.SaveLongTermMemoryRequest;
import com.shiyu.ai.memory.spi.Memory;
import com.shiyu.ai.memory.spi.MemoryType;
import com.shiyu.ai.model.chat.ChatEngine;
import com.shiyu.ai.model.chat.ChatRequest;
import com.shiyu.ai.model.chat.ChatResponse;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 记忆整合服务
 *
 * <p>职责：会话摘要生成、重要性计算与衰减、过期清理等后台维护任务。
 * 从 {@code MemoryServiceImpl} 中剥离，避免 LLM 调用泄漏到编排层。</p>
 */
@Slf4j
public class ConsolidationService {

    private static final String SUMMARY_CATEGORY = "summary";

    private final ConversationMessageRepository conversationMessageRepository;
    private final LongTermMemoryRepository longTermMemoryRepository;
    private final ChatEngine chatEngine;

    public ConsolidationService(ConversationMessageRepository conversationMessageRepository,
                                LongTermMemoryRepository longTermMemoryRepository,
                                ChatEngine chatEngine) {
        this.conversationMessageRepository = conversationMessageRepository;
        this.longTermMemoryRepository = longTermMemoryRepository;
        this.chatEngine = chatEngine;
    }

    /**
     * 生成并存储会话摘要。
     * 优先使用 LLM 生成，失败时回退到截断文本。
     *
     * @param sessionId 会话 ID
     * @param history   对话历史文本
     * @param userId    用户 ID
     * @param agentId   Agent ID
     * @return 摘要内容
     */
    public String summarize(String sessionId, String history, Long userId, String agentId) {
        if (history == null || history.isBlank()) return "";

        // 尝试 LLM 摘要
        try {
            String prompt = "请对以下对话进行简要总结，提取关键信息和学习要点（100字以内）：\n\n" + history;
            ChatResponse resp = chatEngine.chat(ChatRequest.builder().prompt(prompt).build());
            if (resp.isSuccess() && resp.getContent() != null && !resp.getContent().isBlank()) {
                String summary = resp.getContent().trim();
                saveSummary(sessionId, userId, agentId, summary);
                log.info("LLM 会话摘要已生成: sessionId={}, len={}", sessionId, summary.length());
                return summary;
            }
        } catch (Exception e) {
            log.warn("LLM 摘要生成失败，使用截断回退: {}", e.getMessage());
        }

        // 回退：截断前 500 字符
        String fallback = history.length() > 500 ? history.substring(0, 500) + "..." : history;
        saveSummary(sessionId, userId, agentId, fallback);
        log.info("会话摘要（截断回退）已生成: sessionId={}", sessionId);
        return fallback;
    }

    private void saveSummary(String sessionId, Long userId, String agentId, String summary) {
        SaveLongTermMemoryRequest request = SaveLongTermMemoryRequest.builder()
                .userId(userId)
                .agentId(agentId)
                .category(SUMMARY_CATEGORY)
                .memoryKey("session:" + sessionId)
                .content(summary)
                .importance(0.9)
                .source(sessionId)
                .build();
        saveLongTermMemoryDirect(request);
    }

    /**
     * 直接通过 Repository 保存长期记忆（ConsolidationService 内部使用）。
     */
    private void saveLongTermMemoryDirect(SaveLongTermMemoryRequest request) {
        LongTermMemoryBO mem = new LongTermMemoryBO();
        mem.setUserId(request.getUserId());
        mem.setAgentId(request.getAgentId());
        mem.setCategory(request.getCategory() != null ? request.getCategory() : "general");
        mem.setMemoryKey(request.getMemoryKey());
        mem.setContent(request.getContent());
        mem.setImportance(request.getImportance());
        mem.setSource(request.getSource());
        mem.setCreateTime(LocalDateTime.now());
        mem.setUpdateTime(LocalDateTime.now());
        longTermMemoryRepository.insert(mem);
    }

    /**
     * 获取已有会话摘要。
     */
    public String getSessionSummary(String sessionId) {
        List<LongTermMemoryBO> mems = longTermMemoryRepository.searchByKeyword(
                "session:" + sessionId, null, null, 1);
        return mems.isEmpty() ? null : mems.get(0).getContent();
    }

    /**
     * 清理超过指定天数的过期会话消息。
     *
     * @param maxDays 保留天数
     * @return 删除的消息条数
     */
    public int cleanupExpiredSessions(int maxDays) {
        LocalDate deadline = LocalDate.now().minusDays(maxDays);
        int deleted = conversationMessageRepository.deleteBySessionBefore(deadline);
        if (deleted > 0) {
            log.info("清理过期会话消息: {} 条 (截止日期={})", deleted, deadline);
        }
        return deleted;
    }

    /**
     * 重新计算长期记忆的重要性（时效衰减）。
     */
    public void recalculateImportance(Long userId, String agentId) {
        List<LongTermMemoryBO> allMems = longTermMemoryRepository.selectAllByUser(userId, agentId);
        for (LongTermMemoryBO mem : allMems) {
            double baseImportance = mem.getImportance() != null ? mem.getImportance() : 0.5;
            if (mem.getUpdateTime() != null) {
                long daysSinceUpdate = ChronoUnit.DAYS.between(mem.getUpdateTime(), LocalDateTime.now());
                double timeDecay = Math.max(0, 1.0 - daysSinceUpdate * 0.01);
                double newImportance = baseImportance * 0.7 + timeDecay * 0.3;
                newImportance = Math.max(0.1, Math.min(1.0, newImportance));
                if (Math.abs(newImportance - baseImportance) > 0.05) {
                    mem.setImportance(newImportance);
                    longTermMemoryRepository.update(mem);
                }
            }
        }
        log.info("长期记忆重要性已重新计算: userId={}, count={}", userId, allMems.size());
    }

    /**
     * 将短期记忆消息合并为长期记忆条目。
     *
     * @param messages 短期记忆列表
     * @param userId   用户 ID
     * @param agentId  Agent ID
     * @param sessionId 会话 ID
     */
    public void consolidateShortTermToLongTerm(List<Memory> messages, Long userId, String agentId, String sessionId) {
        if (messages == null || messages.isEmpty()) return;

        // 将消息拼接为文本块
        StringBuilder sb = new StringBuilder();
        for (Memory msg : messages) {
            String role = msg.getRole() != null ? msg.getRole() : "unknown";
            sb.append("[").append(role).append("] ").append(msg.getContent()).append("\n");
        }
        String consolidatedContent = sb.toString();

        // 用 LLM 生成摘要作为长期记忆的键，或直接存储拼接文本
        String summary;
        try {
            summary = summarize(sessionId, consolidatedContent, userId, agentId);
        } catch (Exception e) {
            log.warn("短期→长期合并摘要失败，使用截断: {}", e.getMessage());
            summary = consolidatedContent.length() > 500
                    ? consolidatedContent.substring(0, 500) + "..."
                    : consolidatedContent;
        }

        SaveLongTermMemoryRequest request = SaveLongTermMemoryRequest.builder()
                .userId(userId)
                .agentId(agentId)
                .category("consolidated")
                .memoryKey("consolidated:" + sessionId + ":" + System.currentTimeMillis())
                .content(summary)
                .importance(0.7)
                .source(sessionId)
                .build();
        saveLongTermMemoryDirect(request);
        log.info("短期→长期记忆合并完成: sessionId={}, msgCount={}", sessionId, messages.size());
    }

    /**
     * 将工作记忆变量转换为情景记忆数据 — 由 ConsolidationPipeline 使用。
     */
    public Memory buildEpisodicMemoryFromWorkingMemory(String sessionId, Long userId, String agentId,
                                                        java.util.Map<String, Object> vars) {
        if (vars == null || vars.isEmpty()) return null;

        Memory mem = new Memory(MemoryType.EPISODIC, sessionId, "system", vars.toString());
        mem.setUserId(userId);
        mem.setAgentId(agentId);
        mem.setCategory("working_memory_dump");
        mem.setImportance(0.5);
        mem.getMetadata().putAll(vars);
        return mem;
    }
}
