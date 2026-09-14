package com.shiyu.ai.agent.implementation.request;

import lombok.Data;

import java.util.Map;

/**
 * {@code CanvasConfigRequest} 表示智能体模块的请求参数，承载调用方提交的输入数据。
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
     * {@code NodePosition} 承载智能体模块的领域状态或协作行为，负责维护本类型的职责边界。
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
