package com.shiyu.ai.agent.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
public class ExecuteRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Agent ID 不能为空")
    private String agentId;

    private Map<String, Object> input;
}
