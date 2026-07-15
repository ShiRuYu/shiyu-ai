package com.shiyu.ai.usage.vo;

import lombok.Data;
import java.io.Serial;
import java.io.Serializable;

@Data
public class UsageOverviewVO implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    private Long totalTokens;
    private Long totalCalls;
    private Long todayTokens;
    private Long todayCalls;
    private Integer activeModels;
}
