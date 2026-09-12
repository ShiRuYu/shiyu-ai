package com.shiyu.ai.education.implementation.web.dto;

import com.shiyu.ai.education.implementation.domain.model.ResourceBO;

import io.github.linpeilie.annotations.AutoMapper;

/**
 * {@code ResourceResponse} 封装教育模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param id 标识，表示该记录组件承载的数据。
 * @param name 名称，表示该记录组件承载的数据。
 * @param type 类型，表示该记录组件承载的数据。
 * @param url url 属性，表示该记录组件承载的数据。
 * @param subjectCode subjectCode 属性，表示该记录组件承载的数据。
 * @param grade grade 属性，表示该记录组件承载的数据。
 * @param difficulty difficulty 属性，表示该记录组件承载的数据。
 * @param coverUrl coverUrl 属性，表示该记录组件承载的数据。
 * @param description 描述，表示该记录组件承载的数据。
 * @param viewCount viewCount 属性，表示该记录组件承载的数据。
 */
@AutoMapper(target = ResourceBO.class)
public record ResourceResponse(
        Long id,
        String name,
        String type,
        String url,
        String subjectCode,
        Integer grade,
        Integer difficulty,
        String coverUrl,
        String description,
        Long viewCount) {}
