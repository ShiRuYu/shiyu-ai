package com.shiyu.ai.common.vector.model;

import java.util.Map;

/**
 * 向量记录 — 不可变记录
 *
 * @param id 标识，表示该记录组件承载的数据。
 * @param vector 向量，表示该记录组件承载的数据。
 * @param metadata 元数据，表示该记录组件承载的数据。
 */
public record VectorRecord(String id, float[] vector, Map<String, Object> metadata) {}
