package com.shiyu.ai.chat.liteflow.components;

import com.shiyu.ai.chat.domain.GlobalContext;
import com.shiyu.ai.chat.domain.memory.MemoryContext;
import com.shiyu.ai.chat.service.MemoryService;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 加载记忆上下文
 */
@Slf4j
@LiteflowComponent("MEMORY_LOAD")
public class MemoryLoadCmp extends NodeComponent {
    
    @Resource
    private MemoryService memoryService;
    
    @Override
    public void process() {
        GlobalContext context = this.getContextBean(GlobalContext.class);
        
        String sessionId = context.get(GlobalContext.ChatBizKeyEnum.SESSION_ID.getCode());
        String userId = context.get(GlobalContext.ChatBizKeyEnum.USER_ID.getCode());
        
        if (sessionId == null || userId == null) {
            log.warn("缺少 sessionId 或 userId，跳过记忆加载");
            return;
        }
        
        log.info("加载记忆上下文：sessionId={}, userId={}", sessionId, userId);
        
        try {
            // 获取记忆上下文
            MemoryContext memoryContext = memoryService.getMemoryContext(sessionId, userId);
            
            // 存入全局上下文
            context.set(GlobalContext.ChatBizKeyEnum.MEMORY_CONTEXT.getCode(), memoryContext);
            
            log.info("记忆上下文加载完成，短期记忆={}条，长期记忆={}条，历史={}条",
                    memoryContext.getShortTermMemories().size(),
                    memoryContext.getLongTermMemories().size(),
                    memoryContext.getRecentHistories().size());
            
        } catch (Exception e) {
            log.error("加载记忆失败：{}", e.getMessage(), e);
        }
    }
    
    @Override
    public boolean isContinueOnError() {
        return true;
    }
}
