package com.shiyu.ai.education.implementation.web.dto;

import java.util.Map;

/**
 * {@code AbilityRadarResponse} 封装教育模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param studentId 学生标识，表示该记录组件承载的数据。
 * @param knowledgeId knowledgeId 属性，表示该记录组件承载的数据。
 * @param abilities abilities 属性，表示该记录组件承载的数据。
 * @param overallMastery overallMastery 属性，表示该记录组件承载的数据。
 */
public record AbilityRadarResponse(
        Long studentId, Long knowledgeId, Map<String, Double> abilities, Double overallMastery) {}
