package com.shiyu.ai.chat.lm.result;

import lombok.Data;

import java.util.List;

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
    
    /**
     * 链路追踪信息（可选）
     */
    private List<String> traces;

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
    
    public ChatResult(String answer, double score, List<String> traces) {
        this.answer = answer;
        this.score = score;
        this.traces = traces;
    }
}
