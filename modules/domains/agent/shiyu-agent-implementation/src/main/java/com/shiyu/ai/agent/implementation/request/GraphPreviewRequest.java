package com.shiyu.ai.agent.implementation.request;

import lombok.Data;

import java.util.Map;

/**
 * {@code GraphPreviewRequest} 表示智能体模块的请求参数，承载调用方提交的输入数据。
 */
@Data
public class GraphPreviewRequest {

    /**
     * 图结构配置，表示当前对象中的对应属性。
     */
    private String graphConfig;

    /**
     * 输入，表示当前对象中的对应属性。
     */
    private Map<String, Object> input;
}
