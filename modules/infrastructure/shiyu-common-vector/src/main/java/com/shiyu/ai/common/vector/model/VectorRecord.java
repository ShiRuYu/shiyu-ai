package com.shiyu.ai.common.vector.model;

import java.util.Map;

/**
 * 封装 向量 相关的不可变数据及其字段约束。
 */
public record VectorRecord(String id, float[] vector, Map<String, Object> metadata) {}
