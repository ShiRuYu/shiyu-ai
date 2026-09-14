package com.shiyu.ai.education.implementation.web.dto;

import java.util.List;

/**
 * {@code GenerateExamRequest} 封装教育模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param subjectCode subjectCode 属性，表示该记录组件承载的数据。
 * @param grade grade 属性，表示该记录组件承载的数据。
 * @param knowledgeIds knowledgeIds 属性，表示该记录组件承载的数据。
 * @param durationMin durationMin 属性，表示该记录组件承载的数据。
 * @param difficultyDistribution difficultyDistribution 属性，表示该记录组件承载的数据。
 */
public record GenerateExamRequest(
        String subjectCode,
        Integer grade,
        List<Long> knowledgeIds,
        Integer durationMin,
        String difficultyDistribution) {}
