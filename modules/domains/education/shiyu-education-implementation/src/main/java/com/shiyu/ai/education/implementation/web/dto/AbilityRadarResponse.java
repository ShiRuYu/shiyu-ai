package com.shiyu.ai.education.implementation.web.dto;

import java.util.Map;

public record AbilityRadarResponse(
        Long studentId, Long knowledgeId, Map<String, Double> abilities, Double overallMastery) {}
