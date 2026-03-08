package com.shiyu.ai.chat.liteflow.components;

import com.shiyu.ai.chat.lm.ChatEngine;
import com.shiyu.ai.chat.domain.GlobalContext;
import com.shiyu.ai.chat.lm.ModelEnum;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@LiteflowComponent("CHAT_COT")
public class ChatCoTCmp extends NodeComponent {
    @Resource
    private ChatEngine chatEngine;

    @Override
    public void process() {
        GlobalContext context = this.getContextBean(GlobalContext.class);
        String query = context.get(GlobalContext.ChatBizKeyEnum.QUERY.getCode(), "你能帮我什么？");
        
        log.info("开始执行 CoT（Chain of Thought）思维链推理：{}", query);
        
        // Step 1: 构建 CoT 提示词，引导模型逐步思考
        String cotPrompt = buildCotPrompt(query);
        
        // Step 2: 调用模型进行逐步推理
        String result = chatEngine.call(cotPrompt, ModelEnum.SILICON_FLOW);
        
        log.info("CoT 推理完成");
        
        // Step 3: 提取最终答案
        String finalAnswer = extractFinalAnswer(result);
        
        context.set(GlobalContext.ChatBizKeyEnum.FINAL_ANSWER.getCode(), finalAnswer);
        context.set(GlobalContext.ChatBizKeyEnum.TOT_FINAL_THOUGHT.getCode(), result);
    }

    /**
     * 构建思维链提示词
     * 引导模型按照"理解问题 -> 分析条件 -> 逐步推导 -> 得出结论"的流程思考
     */
    private String buildCotPrompt(String query) {
        StringBuilder sb = new StringBuilder();
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
