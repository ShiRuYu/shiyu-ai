package com.shiyu.ai.agent.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@SuppressWarnings("serial")
public class RuntimeExecutionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String executionId;
    private String agentId;
    private Integer status;
    private Map<String, Object> input;
    private Map<String, Object> output;
    private String error;
    private Long startTime;
    private Long endTime;
    private List<String> events;
}
