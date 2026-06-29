package com.shiyu.ai.aiagent.request;

import lombok.Data;

import java.util.Map;

@Data
public class CanvasConfigRequest {

    private Double zoom;

    private Double offsetX;

    private Double offsetY;

    private Map<String, NodePosition> nodePositions;

    @Data
    public static class NodePosition {
        private Double x;
        private Double y;
    }
}
