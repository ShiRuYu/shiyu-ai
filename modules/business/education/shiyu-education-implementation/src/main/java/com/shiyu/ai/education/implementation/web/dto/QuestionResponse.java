package com.shiyu.ai.education.implementation.web.dto;

import com.shiyu.ai.education.implementation.domain.model.QuestionBO;

import io.github.linpeilie.annotations.AutoMapper;

/**
 * {@code QuestionResponse} 封装教育模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param id 标识，表示该记录组件承载的数据。
 * @param code 编码，表示该记录组件承载的数据。
 * @param type 类型，表示该记录组件承载的数据。
 * @param subjectCode subjectCode 属性，表示该记录组件承载的数据。
 * @param grade grade 属性，表示该记录组件承载的数据。
 * @param difficulty difficulty 属性，表示该记录组件承载的数据。
 * @param abilityDimension abilityDimension 属性，表示该记录组件承载的数据。
 * @param title 标题，表示该记录组件承载的数据。
 * @param options options 属性，表示该记录组件承载的数据。
 * @param answer answer 属性，表示该记录组件承载的数据。
 * @param analysis analysis 属性，表示该记录组件承载的数据。
 * @param tags tags 属性，表示该记录组件承载的数据。
 * @param usedCount usedCount 属性，表示该记录组件承载的数据。
 */
@AutoMapper(target = QuestionBO.class)
public record QuestionResponse(
        Long id,
        String code,
        String type,
        String subjectCode,
        Integer grade,
        Integer difficulty,
        String abilityDimension,
        String title,
        String options,
        String answer,
        String analysis,
        String tags,
        Long usedCount) {}
