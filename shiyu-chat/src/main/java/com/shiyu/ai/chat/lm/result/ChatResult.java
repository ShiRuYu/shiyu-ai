package com.shiyu.ai.chat.lm.result;

import lombok.Data;

/**
 * 普通对话响应结果（非流式）
 */
@Data
public class ChatResult {
    
    /**
     * 回答内容
     */
    private String answer;
    
    /**
     * 置信度分数（可选）
     */
    private double score;
    
    public ChatResult() {
    }

    public ChatResult(String answer) {
        this.answer = answer;
        this.score = 0.0;
    }

    public ChatResult(String answer, double score) {
        this.answer = answer;
        this.score = score;
    }
}
