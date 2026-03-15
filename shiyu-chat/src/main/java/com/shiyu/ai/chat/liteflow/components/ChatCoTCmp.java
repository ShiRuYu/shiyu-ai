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
@LiteflowComponent("CHAT_COT")
public class ChatCoTCmp extends NodeComponent {
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
        log.info("开始执行 CoT（Chain of Thought）思维链推理（同步）：{}", query);
        
        // Step 1: 构建带记忆的 CoT 提示词
        String cotPrompt = buildCotPrompt(query, memoryContextObj);
        
        // Step 2: 调用模型进行逐步推理
        LmRequest request = new LmRequest(cotPrompt, PlatformEnum.SILICON_FLOW.getAdapterName(), null);
        ChatResult result = chatEngine.call(request);
        
        log.info("CoT 推理完成（同步）");
        
        // Step 3: 提取最终答案
        String finalAnswer = extractFinalAnswer(result.getAnswer());
        
        context.set(GlobalContext.ChatBizKeyEnum.FINAL_ANSWER.getCode(), finalAnswer);
        context.set(GlobalContext.ChatBizKeyEnum.TOT_FINAL_THOUGHT.getCode(), result.getAnswer());
    }
    
    /**
     * 流式处理
     */
    private void handleStream(GlobalContext context, String query, Object memoryContextObj) {
        log.info("开始执行 CoT（Chain of Thought）思维链推理（流式）：{}", query);
        
        // Step 1: 构建带记忆的 CoT 提示词
        String cotPrompt = buildCotPrompt(query, memoryContextObj);
        
        // Step 2: 执行流式调用
        LmRequest request = new LmRequest(cotPrompt, PlatformEnum.SILICON_FLOW.getAdapterName(), null);
        StreamResult result = chatEngine.stream(request);
        Flux<String> flux = result.getAnswer();
        
        // 将 Flux 存入全局上下文，由调用方订阅和处理
        context.set(GlobalContext.ChatBizKeyEnum.STREAM_FLUX.getCode(), flux);
        
        log.info("CoT 推理完成（流式），Flux 已传递给调用方");
    }

    /**
     * 构建思维链提示词
     * 引导模型按照"理解问题 -> 分析条件 -> 逐步推导 -> 得出结论"的流程思考
     */
    private String buildCotPrompt(String query, Object memoryContextObj) {
        StringBuilder sb = new StringBuilder();
        
        // 添加记忆上下文
        if (memoryContextObj instanceof com.shiyu.ai.chat.domain.memory.MemoryContext) {
            com.shiyu.ai.chat.domain.memory.MemoryContext memoryContext = 
                (com.shiyu.ai.chat.domain.memory.MemoryContext) memoryContextObj;
            
            if (memoryContext.getMemorySummary() != null && !memoryContext.getMemorySummary().isEmpty()) {
                sb.append("【相关记忆】\n").append(memoryContext.getMemorySummary()).append("\n\n");
            }
            
            if (memoryContext.getRecentHistories() != null && !memoryContext.getRecentHistories().isEmpty()) {
                sb.append("【对话历史】\n");
                for (int i = memoryContext.getRecentHistories().size() - 1; i >= 0; i--) {
                    var h = memoryContext.getRecentHistories().get(i);
                    sb.append("用户：").append(h.getUserQuery()).append("\n");
                    sb.append("AI: ").append(h.getAiResponse()).append("\n\n");
                }
            }
        }
        
        sb.append("请按照以下步骤逐步思考和解答这个问题：\n\n");
        sb.append("【题目】").append(query).append("\n\n");
        sb.append("请按以下结构回答：\n");
        sb.append("1. **理解问题**：明确题目要求什么\n");
        sb.append("2. **分析条件**：列出已知条件和信息\n");
        sb.append("3. **逐步推导**：一步一步地进行推理（这是最重要的部分）\n");
        sb.append("4. **得出结论**：总结最终答案\n\n");
        sb.append("请在最后用'###最终答案：'的格式给出结论。\n");
        
        return sb.toString();
    }

    /**
     * 从 CoT 推理结果中提取最终答案
     */
    private String extractFinalAnswer(String reasoningResult) {
        if (reasoningResult == null || reasoningResult.trim().isEmpty()) {
            return "未能生成有效回答";
        }

        // 尝试查找标记的最终答案
        int answerIndex = reasoningResult.lastIndexOf("###最终答案:");
        if (answerIndex == -1) {
            answerIndex = reasoningResult.lastIndexOf("### 最终答案:");
        }
        
        if (answerIndex != -1 && answerIndex < reasoningResult.length() - 1) {
            return reasoningResult.substring(answerIndex).trim();
        }

        // 如果没有找到标记，返回完整推理过程
        return reasoningResult;
    }
}
