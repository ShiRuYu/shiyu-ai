 package com.shiyu.ai.chat.lm.result;

import lombok.Data;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 模型响应结果
 */
@Data
public class ModelResult {
    
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
    
    /**
     * 流式回答（可选，与 answer 互斥）
     */
    private Flux<String> answerStream;

    public ModelResult() {
    }

    public ModelResult(String answer) {
        this.answer = answer;
        this.score = 0.0;
    }
    
    public ModelResult(Flux<String> answerStream) {
        this.answerStream = answerStream;
        this.score = 0.0;
    }
    
    public ModelResult(String answer, double score) {
        this.answer = answer;
        this.score = score;
    }
    
    public ModelResult(String answer, double score, List<String> traces) {
        this.answer = answer;
        this.score = score;
        this.traces = traces;
    }
}