package com.shiyu.ai.education.implementation.web.dto;

import java.util.List;

/**
 * {@code GenerateQuestionRequest} 封装教育模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param knowledgeIds knowledgeIds 属性，表示该记录组件承载的数据。
 * @param difficulty difficulty 属性，表示该记录组件承载的数据。
 * @param count count 属性，表示该记录组件承载的数据。
 * @param types types 属性，表示该记录组件承载的数据。
 */
public record GenerateQuestionRequest(
        List<Long> knowledgeIds, Integer difficulty, Integer count, List<String> types) {}
