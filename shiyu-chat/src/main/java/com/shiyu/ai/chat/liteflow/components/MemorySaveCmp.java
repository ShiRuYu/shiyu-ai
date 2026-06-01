package com.shiyu.ai.chat.liteflow.components;

import com.shiyu.ai.chat.domain.GlobalContext;
import com.shiyu.ai.chat.service.MemoryService;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

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
                log.info("流式模式：跳过记忆保存（由 Controller 层处理）");
            } else if (finalAnswer == null) {
                log.warn("同步模式下 FINAL_ANSWER 为空，无法保存记忆");
            } else {
                memoryService.saveChatMemory(sessionId, userId, query, finalAnswer, intentType, chainUsed);
            }
        } catch (Exception e) {
            log.error("保存记忆失败：{}", e.getMessage(), e);
        }
    }
    
    @Override
    public boolean isContinueOnError() {
        return true;
    }
}
