package com.shiyu.ai.education.implementation.web.dto;

/**
 * {@code AbilityResponse} 封装教育模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param id 标识，表示该记录组件承载的数据。
 * @param studentId 学生标识，表示该记录组件承载的数据。
 * @param knowledgeId knowledgeId 属性，表示该记录组件承载的数据。
 * @param knowledgeName knowledgeName 属性，表示该记录组件承载的数据。
 * @param overallMastery overallMastery 属性，表示该记录组件承载的数据。
 */
public record AbilityResponse(
        Long id, Long studentId, Long knowledgeId, String knowledgeName, Double overallMastery) {}
