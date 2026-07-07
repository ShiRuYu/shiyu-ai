package com.shiyu.ai.aiagent.workflow.context;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class AbilityContext {
    private Long studentId;
    private Map<String, Double> abilities;
    private Double overallMastery;
    private String report;
}
