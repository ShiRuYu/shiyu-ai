package com.shiyu.ai.education.implementation.web.dto;

import com.shiyu.ai.education.implementation.domain.model.StudyPlanBO;

import io.github.linpeilie.annotations.AutoMapper;

import java.util.List;

/**
 * {@code StudyPlanResponse} 封装教育模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param id 标识，表示该记录组件承载的数据。
 * @param studentId 学生标识，表示该记录组件承载的数据。
 * @param name 名称，表示该记录组件承载的数据。
 * @param startDate startDate 属性，表示该记录组件承载的数据。
 * @param endDate endDate 属性，表示该记录组件承载的数据。
 * @param status 状态，表示该记录组件承载的数据。
 * @param statusDesc statusDesc 属性，表示该记录组件承载的数据。
 * @param totalItems totalItems 属性，表示该记录组件承载的数据。
 * @param completedItems completedItems 属性，表示该记录组件承载的数据。
 * @param items items 属性，表示该记录组件承载的数据。
 */
@AutoMapper(target = StudyPlanBO.class)
public record StudyPlanResponse(
        Long id,
        Long studentId,
        String name,
        String startDate,
        String endDate,
        Integer status,
        String statusDesc,
        Integer totalItems,
        Integer completedItems,
        List<DailyTaskResponse> items) {}
