package com.shiyu.ai.chat.liteflow.components;

import com.shiyu.ai.chat.domain.GlobalContext;
import com.shiyu.ai.chat.lm.ChatEngine;
import com.shiyu.ai.chat.lm.PlatformEnum;
import com.shiyu.ai.chat.lm.request.LmRequest;
import com.shiyu.ai.chat.lm.result.ChatResult;
import com.shiyu.ai.chat.lm.result.StreamResult;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

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
        
        // 判断是否为流式模式
        boolean isStream = "true".equals(context.get(GlobalContext.ChatBizKeyEnum.STREAM_MODE.getCode()));
        
        if (isStream) {
            handleStream(context, query, memoryContextObj);
        } else {
            handleSync(context, query, memoryContextObj);
        }
    }
    
    /**
     * 同步处理
     */
    private void handleSync(GlobalContext context, String query, Object memoryContextObj) {
        log.info("执行直接对话模式（同步）：{}", query);
        
        // 构建带记忆的提示词
        String promptWithMemory = buildPromptWithMemory(query, memoryContextObj);
        
        // 直接调用模型回答问题
        LmRequest request = new LmRequest(promptWithMemory, PlatformEnum.SILICON_FLOW.getAdapterName(), null);
        ChatResult result = chatEngine.call(request);
        
        log.info("直接对话完成（同步）");
        context.set(GlobalContext.ChatBizKeyEnum.FINAL_ANSWER.getCode(), result.getAnswer());
    }
    
    /**
     * 流式处理
     */
    private void handleStream(GlobalContext context, String query, Object memoryContextObj) {
        log.info("执行直接对话模式（流式）：{}", query);
        
        // 构建带记忆的提示词
        String promptWithMemory = buildPromptWithMemory(query, memoryContextObj);
        
        // 构建模型请求
        LmRequest request = new LmRequest(promptWithMemory, PlatformEnum.SILICON_FLOW.getAdapterName(), null);
        
        // 执行流式调用
        StreamResult result = chatEngine.stream(request);
        Flux<String> flux = result.getAnswer();
        
        // 将 Flux 存入全局上下文
        context.set(GlobalContext.ChatBizKeyEnum.STREAM_FLUX.getCode(), flux);
        
        // 收集完整答案用于后续保存记忆（非阻塞，仅订阅）
        flux.reduce((a, b) -> a + b)
            .subscribe(
                fullAnswer -> {
                    context.set(GlobalContext.ChatBizKeyEnum.FINAL_ANSWER.getCode(), fullAnswer);
                    log.info("流式回答完成，已保存完整答案");
                },
                error -> log.error("收集流式答案失败：{}", error.getMessage())
            );
        
        log.info("直接对话完成（流式）");
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
