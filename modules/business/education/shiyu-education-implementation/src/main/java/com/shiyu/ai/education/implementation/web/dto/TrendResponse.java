package com.shiyu.ai.education.implementation.web.dto;

import java.util.List;

/**
 * 封装 Trend 相关的不可变数据及其字段约束。
 */
public record TrendResponse(List<String> dates, List<Double> values) {}
