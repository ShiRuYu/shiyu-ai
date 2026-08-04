package com.shiyu.ai.usage.vo;

import lombok.Data;
import java.io.Serial;
import java.io.Serializable;

@Data
public class UsageAggregateVO implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    private String period;
    private Long totalTokens;
    private Long totalCalls;
    private String modelName;
}
