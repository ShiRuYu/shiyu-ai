package com.shiyu.ai.common.vector.model;

import java.util.Map;

/** 向量记录 — 不可变记录 */
public record VectorRecord(String id, float[] vector, Map<String, Object> metadata) {}
