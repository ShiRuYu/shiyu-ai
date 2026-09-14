package com.shiyu.ai.education.implementation.web.dto;

/**
 * {@code WeakPointResponse} 封装教育模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param knowledgeId knowledgeId 属性，表示该记录组件承载的数据。
 * @param knowledgeName knowledgeName 属性，表示该记录组件承载的数据。
 * @param mastery mastery 属性，表示该记录组件承载的数据。
 */
public record WeakPointResponse(Long knowledgeId, String knowledgeName, Double mastery) {}
