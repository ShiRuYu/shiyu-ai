package com.shiyu.ai.agent.implementation.request;

import lombok.Data;

import java.util.Map;

/**
 * 封装 Canvas Config 操作所需的请求条件和输入数据。
 */
@Data
public class CanvasConfigRequest {

    /**
     * zoom 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Double zoom;

    /**
     * offsetX 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Double offsetX;

    /**
     * offsetY 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Double offsetY;

    /**
     * nodePositions 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Map<String, NodePosition> nodePositions;

    /**
     * 实现 Node Position 相关的业务处理、协作逻辑或基础设施能力。
     */
    @Data
    public static class NodePosition {
        /**
         * x 属性，保存当前对象中的业务数据或协作依赖。
         */
        private Double x;
        /**
         * y 属性，保存当前对象中的业务数据或协作依赖。
         */
        private Double y;
    }
}
