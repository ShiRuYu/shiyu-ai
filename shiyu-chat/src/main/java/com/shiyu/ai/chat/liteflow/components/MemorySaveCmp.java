package com.shiyu.ai.chat.liteflow.components;

import com.shiyu.ai.chat.domain.GlobalContext;
import com.shiyu.ai.chat.domain.memory.ConversationHistory;
import com.shiyu.ai.chat.domain.memory.Memory;
import com.shiyu.ai.chat.service.MemoryService;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

/**
 * 保存对话历史和记忆
 */
@Slf4j
@LiteflowComponent("MEMORY_SAVE")
public class MemorySaveCmp extends NodeComponent {
    
    @Resource
    private MemoryService memoryService;
    
    @Override
    public void process() {
        GlobalContext context = this.getContextBean(GlobalContext.class);
        
        String sessionId = context.get(GlobalContext.ChatBizKeyEnum.SESSION_ID.getCode());
        String userId = context.get(GlobalContext.ChatBizKeyEnum.USER_ID.getCode());
        String query = context.get(GlobalContext.ChatBizKeyEnum.QUERY.getCode());
        String finalAnswer = context.get(GlobalContext.ChatBizKeyEnum.FINAL_ANSWER.getCode());

        Object intentObj = context.get(GlobalContext.ChatBizKeyEnum.INTENT.getCode());
        String intentType = intentObj != null ? JSONUtils.toJsonString(intentObj) : null;
        String chainUsed = context.get(GlobalContext.ChatBizKeyEnum.CHAIN.getCode());
        
        // 判断是否为流式模式
        boolean isStream = "true".equals(context.get(GlobalContext.ChatBizKeyEnum.STREAM_MODE.getCode()));
        
        if (sessionId == null || query == null) {
            log.warn("缺少必要信息，无法保存记忆");
            return;
        }
        
        log.info("保存对话历史和记忆：sessionId={}, query={}, isStream={}", sessionId, query, isStream);
        
        try {
            if (isStream) {
                // 流式模式：从 STREAM_FLUX 中收集完整答案
                handleStreamMemory(context, sessionId, userId, query, intentType, chainUsed);
            } else {
                // 同步模式：直接使用 FINAL_ANSWER
                handleSyncMemory(sessionId, userId, query, finalAnswer, intentType, chainUsed);
            }
            
        } catch (Exception e) {
            log.error("保存记忆失败：{}", e.getMessage(), e);
        }
    }
    
    /**
     * 同步模式保存记忆
     */
    private void handleSyncMemory(String sessionId, String userId, String query, String finalAnswer, 
                                   String intentType, String chainUsed) {
        if (finalAnswer == null) {
            log.warn("同步模式下 FINAL_ANSWER 为空，无法保存记忆");
            return;
        }
        
        log.info("同步模式保存记忆：sessionId={}, query={}", sessionId, query);
        
        // 1. 保存对话历史
        ConversationHistory history = ConversationHistory.builder()
                .sessionId(sessionId)
                .userId(userId != null ? userId : sessionId.split("_")[0])
                .userQuery(query)
                .aiResponse(finalAnswer)
                .intentType(intentType)
                .chainUsed(chainUsed)
                .createdAt(LocalDateTime.now())
                .build();
        
        memoryService.saveConversationHistory(history);
        
        // 2. 添加短期记忆（用户问题和 AI 回答）
        Memory userMemory = Memory.builder()
                .sessionId(sessionId)
                .userId(userId != null ? userId : sessionId.split("_")[0])
                .type(Memory.MemoryType.SHORT_TERM)
                .content("用户：" + query)
                .weight(0.9)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
        
        Memory aiMemory = Memory.builder()
                .sessionId(sessionId)
                .userId(userId != null ? userId : sessionId.split("_")[0])
                .type(Memory.MemoryType.SHORT_TERM)
                .content("AI: " + finalAnswer)
                .weight(0.9)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
        
        memoryService.addShortTermMemory(userMemory);
        memoryService.addShortTermMemory(aiMemory);
        
        // 3. 提取并存储长期记忆
        memoryService.extractAndStoreLongTermMemory(sessionId, query, finalAnswer);
        
        log.info("同步模式对话记忆保存完成");
    }
    
    /**
     * 流式模式保存记忆
     * 注意：流式模式下，Flux 需要传递给客户端消费，不能在这里订阅
     * 记忆保存应该在 Controller 层通过 flux.doOnComplete() 处理
     */
    private void handleStreamMemory(GlobalContext context, String sessionId, String userId, String query,
                                     String intentType, String chainUsed) {
        log.info("流式模式：跳过记忆保存（由 Controller 层处理）");
        
        // 流式模式下，Flux 已经传递给客户端
        // 记忆保存由 Controller 在 flux.doOnComplete() 中处理
        // 这里不做任何操作，避免截胡 Flux 数据
    }
    
    @Override
    public boolean isContinueOnError() {
        return true;
    }
}
