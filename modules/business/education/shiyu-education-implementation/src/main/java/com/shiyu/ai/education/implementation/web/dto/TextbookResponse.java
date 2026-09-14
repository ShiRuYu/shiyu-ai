package com.shiyu.ai.education.implementation.web.dto;

import com.shiyu.ai.education.implementation.domain.model.TextbookBO;

import io.github.linpeilie.annotations.AutoMapper;

/**
 * {@code TextbookResponse} 封装教育模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param id 标识，表示该记录组件承载的数据。
 * @param name 名称，表示该记录组件承载的数据。
 * @param subjectCode subjectCode 属性，表示该记录组件承载的数据。
 * @param grade grade 属性，表示该记录组件承载的数据。
 * @param publisher publisher 属性，表示该记录组件承载的数据。
 * @param isbn isbn 属性，表示该记录组件承载的数据。
 */
@AutoMapper(target = TextbookBO.class)
public record TextbookResponse(
        Long id, String name, String subjectCode, Integer grade, String publisher, String isbn) {}
