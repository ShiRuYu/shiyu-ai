package com.shiyu.ai.aiagent.request;

import lombok.Data;

import java.util.Map;

@Data
public class GraphPreviewRequest {

    private String graphConfig;

    private Map<String, Object> input;
}
