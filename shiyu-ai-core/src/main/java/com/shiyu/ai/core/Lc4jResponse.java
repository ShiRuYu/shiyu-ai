package com.shiyu.ai.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LangChain4j 对话响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lc4jResponse {
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * AI 回复内容
     */
    private String content;
    
    /**
     * 使用的平台
     */
    private String platform;
    
    /**
     * 使用的模型
     */
    private String model;
    
    /**
     * 错误信息（失败时）
     */
    private String errorMessage;
}
