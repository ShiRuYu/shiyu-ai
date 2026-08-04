package com.shiyu.ai.tool.mcp;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
public class McpToolExecuteRequest implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    @NotBlank(message = "工具名称不能为空")
    private String name;
    private Map<String, Object> params;
}
