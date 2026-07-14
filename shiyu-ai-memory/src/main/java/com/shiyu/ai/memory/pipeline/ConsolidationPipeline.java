package com.shiyu.ai.memory.pipeline;
import com.shiyu.ai.memory.service.ConsolidationService;

import com.shiyu.ai.memory.spi.Memory;
import com.shiyu.ai.memory.spi.MemoryQuery;
import com.shiyu.ai.memory.spi.MemoryStore;
import com.shiyu.ai.memory.spi.MemoryType;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 记忆整合管道
 *
 * <p>负责自动化的记忆流转：
 * <ul>
 *   <li><b>STM → LTM</b>：短期记忆溢出时自动合并摘要到长期记忆</li>
 *   <li><b>WM → EPI</b>：工作记忆变量在会话结束时转储为情景记忆</li>
 *   <li><b>SEM → LTM</b>：语义检索结果 enrich 到长期记忆上下文（由 MemoryServiceImpl 编排）</li>
 * </ul>
 * </p>
 */
@Slf4j
public class ConsolidationPipeline {

    /** 触发短期→长期合并的滑动窗口溢出阈值 */
    private static final int STM_TO_LTM_THRESHOLD = 20;

    private final MemoryStore shortTermStore;
    private final MemoryStore longTermStore;
    private final MemoryStore workingStore;
    private final MemoryStore episodicStore;
    private final ConsolidationService consolidationService;

    public ConsolidationPipeline(MemoryStore shortTermStore,
                                 MemoryStore longTermStore,
                                 MemoryStore workingStore,
                                 MemoryStore episodicStore,
                                 ConsolidationService consolidationService) {
        this.shortTermStore = shortTermStore;
        this.longTermStore = longTermStore;
        this.workingStore = workingStore;
        this.episodicStore = episodicStore;
        this.consolidationService = consolidationService;
    }

    /**
     * 检查短期记忆是否需要合并到长期记忆。
     * 当会话消息数超过 {@link #STM_TO_LTM_THRESHOLD} 时触发。
     *
     * @param sessionId 会话 ID
     * @param userId    用户 ID
     * @param agentId   Agent ID
     */
    public void maybeConsolidateShortTermToLongTerm(String sessionId, Long userId, String agentId) {
        if (sessionId == null) return;

        MemoryQuery countQuery = MemoryQuery.builder()
                .sessionId(sessionId)
                .build();
        long msgCount = shortTermStore.count(countQuery);
        if (msgCount < STM_TO_LTM_THRESHOLD) return;

        log.info("短期记忆超阈值 ({} >= {}), 触发 STM→LTM 合并", msgCount, STM_TO_LTM_THRESHOLD);

        MemoryQuery recentQuery = MemoryQuery.builder()
                .sessionId(sessionId)
                .topK(STM_TO_LTM_THRESHOLD)
                .build();
        List<Memory> recentMessages = shortTermStore.query(recentQuery);

        consolidationService.consolidateShortTermToLongTerm(recentMessages, userId, agentId, sessionId);
    }

    /**
     * 将工作记忆转储为情景记忆（通常在 Agent 会话结束时调用）。
     *
     * @param sessionId 会话 ID
     * @param userId    用户 ID
     * @param agentId   Agent ID
     */
    public void dumpWorkingMemoryToEpisodic(String sessionId, Long userId, String agentId) {
        if (sessionId == null) return;

        MemoryQuery query = MemoryQuery.builder()
                .sessionId(sessionId)
                .topK(100)
                .build();
        List<Memory> workingVars = workingStore.query(query);
        if (workingVars.isEmpty()) {
            log.debug("工作记忆为空，跳过 WM→EPI 转储");
            return;
        }

        log.info("工作记忆转储: sessionId={}, varCount={}", sessionId, workingVars.size());

        Map<String, Object> vars = new java.util.LinkedHashMap<>();
        for (Memory var : workingVars) {
            vars.put(var.getMemoryKey(), var.getContent());
        }

        Memory episodicMem = consolidationService.buildEpisodicMemoryFromWorkingMemory(
                sessionId, userId, agentId, vars);
        if (episodicMem != null) {
            episodicStore.save(episodicMem);
            // 清理工作记忆
            workingStore.deleteBySession(sessionId);
            log.info("WM→EPI 转储完成: sessionId={}", sessionId);
        }
    }

    /**
     * 清理过期会话和记忆。
     *
     * @param maxDays 保留天数
     * @return 清理的消息数
     */
    public int cleanup(int maxDays) {
        return consolidationService.cleanupExpiredSessions(maxDays);
    }
}
