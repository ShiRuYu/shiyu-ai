package com.shiyu.ai.tool.mcp;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class McpToolQueryRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String category;
    private String tag;
    private String keyword;
}
