package com.shiyu.ai.chat.liteflow.components;

import com.shiyu.ai.chat.lm.ChatEngine;
import com.shiyu.ai.chat.domain.GlobalContext;
import com.shiyu.ai.chat.lm.PlatformEnum;
import com.shiyu.ai.chat.lm.request.LmRequest;
import com.shiyu.ai.chat.lm.result.ChatResult;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@LiteflowComponent("CHAT_DIRECT")
public class ChatDirectCmp extends NodeComponent {
    @Resource
    private ChatEngine chatEngine;

    @Override
    public void process() {
        GlobalContext context = this.getContextBean(GlobalContext.class);
        String query = context.get(GlobalContext.ChatBizKeyEnum.QUERY.getCode(), "你能帮我什么？");
        
        // 获取记忆上下文
        Object memoryContextObj = context.get(GlobalContext.ChatBizKeyEnum.MEMORY_CONTEXT.getCode());
        
        log.info("执行直接对话模式：{}", query);
        
        // 构建带记忆的提示词
        String promptWithMemory = buildPromptWithMemory(query, memoryContextObj);
        
        // 直接调用模型回答问题
        LmRequest request = new LmRequest(promptWithMemory, PlatformEnum.SILICON_FLOW.getAdapterName(), null);
        ChatResult result = chatEngine.call(request);
        
        log.info("直接对话完成");
        context.set(GlobalContext.ChatBizKeyEnum.FINAL_ANSWER.getCode(), result.getAnswer());
    }
    
    /**
     * 构建带记忆的提示词
     */
    private String buildPromptWithMemory(String query, Object memoryContextObj) {
        if (memoryContextObj == null) {
            return query;
        }
        
        try {
            com.shiyu.ai.chat.domain.memory.MemoryContext memoryContext = 
                (com.shiyu.ai.chat.domain.memory.MemoryContext) memoryContextObj;
            
            StringBuilder sb = new StringBuilder();
            
            // 添加记忆摘要
            if (memoryContext.getMemorySummary() != null && !memoryContext.getMemorySummary().isEmpty()) {
                sb.append("【相关记忆】\n").append(memoryContext.getMemorySummary()).append("\n\n");
            }
            
            // 添加最近的对话历史
            if (memoryContext.getRecentHistories() != null && !memoryContext.getRecentHistories().isEmpty()) {
                sb.append("【对话历史】\n");
                for (int i = memoryContext.getRecentHistories().size() - 1; i >= 0; i--) {
                    var h = memoryContext.getRecentHistories().get(i);
                    sb.append("用户：").append(h.getUserQuery()).append("\n");
                    sb.append("AI: ").append(h.getAiResponse()).append("\n\n");
                }
            }
            
            // 添加当前问题
            sb.append("【当前问题】\n").append(query);
            
            return sb.toString();
        } catch (Exception e) {
            log.warn("构建记忆提示词失败，使用原始问题：{}", e.getMessage());
            return query;
        }
    }
}
