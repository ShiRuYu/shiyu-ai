package com.shiyu.ai.education.implementation.web.dto;

/**
 * {@code OverviewResponse} 封装教育模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param totalStudyDays totalStudyDays 属性，表示该记录组件承载的数据。
 * @param totalKnowledge totalKnowledge 属性，表示该记录组件承载的数据。
 * @param masteredKnowledge masteredKnowledge 属性，表示该记录组件承载的数据。
 * @param totalQuestions totalQuestions 属性，表示该记录组件承载的数据。
 * @param accuracy accuracy 属性，表示该记录组件承载的数据。
 * @param weeklyHours weeklyHours 属性，表示该记录组件承载的数据。
 * @param streakDays streakDays 属性，表示该记录组件承载的数据。
 */
public record OverviewResponse(
        Integer totalStudyDays,
        Integer totalKnowledge,
        Integer masteredKnowledge,
        Integer totalQuestions,
        Double accuracy,
        Double weeklyHours,
        Integer streakDays) {}
