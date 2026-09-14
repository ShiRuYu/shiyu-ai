package com.shiyu.ai.education.implementation.web.dto;

import com.shiyu.ai.education.implementation.domain.model.ReviewTaskBO;

import io.github.linpeilie.annotations.AutoMapper;

import java.time.LocalDateTime;

/**
 * {@code ReviewTaskResponse} 封装教育模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param id 标识，表示该记录组件承载的数据。
 * @param studentId 学生标识，表示该记录组件承载的数据。
 * @param knowledgeId knowledgeId 属性，表示该记录组件承载的数据。
 * @param knowledgeName knowledgeName 属性，表示该记录组件承载的数据。
 * @param reviewRound reviewRound 属性，表示该记录组件承载的数据。
 * @param reviewDate reviewDate 属性，表示该记录组件承载的数据。
 * @param status 状态，表示该记录组件承载的数据。
 * @param statusDesc statusDesc 属性，表示该记录组件承载的数据。
 * @param resultScore resultScore 属性，表示该记录组件承载的数据。
 * @param completedAt completedAt 属性，表示该记录组件承载的数据。
 */
@AutoMapper(target = ReviewTaskBO.class)
public record ReviewTaskResponse(
        Long id,
        Long studentId,
        Long knowledgeId,
        String knowledgeName,
        Integer reviewRound,
        String reviewDate,
        Integer status,
        String statusDesc,
        Double resultScore,
        LocalDateTime completedAt) {}
