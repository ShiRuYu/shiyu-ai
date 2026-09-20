package com.shiyu.ai.agent.implementation.request;

import lombok.Data;

import java.util.Map;

/**
 * 封装 Graph Preview 操作所需的请求条件和输入数据。
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
