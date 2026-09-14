package com.shiyu.ai.education.implementation.web.dto;

import com.shiyu.ai.education.implementation.domain.model.StudyRecordBO;

import io.github.linpeilie.annotations.AutoMapper;

import java.time.LocalDateTime;

/**
 * {@code StudyRecordResponse} 封装教育模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param id 标识，表示该记录组件承载的数据。
 * @param studentId 学生标识，表示该记录组件承载的数据。
 * @param knowledgeId knowledgeId 属性，表示该记录组件承载的数据。
 * @param recordType recordType 属性，表示该记录组件承载的数据。
 * @param questionId 题目标识，表示该记录组件承载的数据。
 * @param score 分数，表示该记录组件承载的数据。
 * @param accuracy accuracy 属性，表示该记录组件承载的数据。
 * @param durationSec durationSec 属性，表示该记录组件承载的数据。
 * @param createTime createTime 属性，表示该记录组件承载的数据。
 */
@AutoMapper(target = StudyRecordBO.class)
public record StudyRecordResponse(
        Long id,
        Long studentId,
        Long knowledgeId,
        String recordType,
        Long questionId,
        Double score,
        Double accuracy,
        Integer durationSec,
        LocalDateTime createTime) {}
