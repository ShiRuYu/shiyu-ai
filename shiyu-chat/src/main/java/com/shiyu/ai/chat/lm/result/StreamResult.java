package com.shiyu.ai.chat.lm.result;

import lombok.Data;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 流式对话响应结果
 */
@Data
public class StreamResult {
    
    /**
     * 流式回答内容
     */
    private Flux<String> answer;
    
    /**
     * 置信度分数（可选）
     */
    private double score;
    
    /**
     * 链路追踪信息（可选）
     */
    private List<String> traces;

    public StreamResult() {
    }

    public StreamResult(Flux<String> answer) {
        this.answer = answer;
        this.score = 0.0;
    }
    
    public StreamResult(Flux<String> answer, double score) {
        this.answer = answer;
        this.score = score;
    }
    
    public StreamResult(Flux<String> answer, double score, List<String> traces) {
        this.answer = answer;
        this.score = score;
        this.traces = traces;
    }
}
