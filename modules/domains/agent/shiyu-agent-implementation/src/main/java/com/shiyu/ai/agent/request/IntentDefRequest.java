package com.shiyu.ai.agent.request;

import com.shiyu.ai.agent.domain.model.IntentDefBO;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@AutoMapper(target = IntentDefBO.class, reverseConvertGenerate = false)
public class IntentDefRequest {
    @NotBlank(message = "Agent标识不能为空")
    private String agentId;
    @NotBlank(message = "意图代码不能为空")
    private String code;
    @NotBlank(message = "意图名称不能为空")
    private String name;
    private String description;
    private String category;
    private Integer priority;
    private Double confidenceThreshold;
    private String targetNode;
    private Integer status;
}
