package com.shiyu.ai.education.implementation.web.dto;

import java.util.List;

/**
 * {@code TrendResponse} 封装教育模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param dates dates 属性，表示该记录组件承载的数据。
 * @param values values 属性，表示该记录组件承载的数据。
 */
public record TrendResponse(List<String> dates, List<Double> values) {}
