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
        
        if (sessionId == null || query == null || finalAnswer == null) {
            log.warn("缺少必要信息，无法保存记忆");
            return;
        }
        
        log.info("保存对话历史和记忆：sessionId={}, query={}", sessionId, query);
        
        try {
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
            
            log.info("对话记忆保存完成");
            
        } catch (Exception e) {
            log.error("保存记忆失败：{}", e.getMessage(), e);
        }
    }
    
    @Override
    public boolean isContinueOnError() {
        return true;
    }
}
