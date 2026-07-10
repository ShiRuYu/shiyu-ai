package com.shiyu.ai.agent.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AgentRequest {

    @NotBlank(message = "Agent标识不能为空")
    @Pattern(regexp = "^[a-z][a-z0-9-]*$", message = "Agent标识只能包含小写字母、数字和连字符，以字母开头")
    private String agentId;

    @NotBlank(message = "Agent名称不能为空")
    private String name;

    private String description;

    private String status;
}
