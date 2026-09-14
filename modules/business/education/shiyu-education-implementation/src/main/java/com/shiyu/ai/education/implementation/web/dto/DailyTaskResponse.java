package com.shiyu.ai.education.implementation.web.dto;

/**
 * {@code DailyTaskResponse} 封装教育模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param id 标识，表示该记录组件承载的数据。
 * @param knowledgeId knowledgeId 属性，表示该记录组件承载的数据。
 * @param knowledgeName knowledgeName 属性，表示该记录组件承载的数据。
 * @param planDate planDate 属性，表示该记录组件承载的数据。
 * @param status 状态，表示该记录组件承载的数据。
 * @param statusDesc statusDesc 属性，表示该记录组件承载的数据。
 * @param orderNo orderNo 属性，表示该记录组件承载的数据。
 */
public record DailyTaskResponse(
        Long id,
        Long knowledgeId,
        String knowledgeName,
        String planDate,
        Integer status,
        String statusDesc,
        Integer orderNo) {}
