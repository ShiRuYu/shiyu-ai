package com.shiyu.ai.education.implementation.web.dto;

import java.util.Map;

/**
 * 封装 Ability Radar 相关的不可变数据及其字段约束。
 */
public record AbilityRadarResponse(
        Long studentId, Long knowledgeId, Map<String, Double> abilities, Double overallMastery) {}
