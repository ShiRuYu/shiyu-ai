package com.shiyu.ai.model.implementation.infrastructure.gateway.model;

/**
 * 封装 模型 Cost Snapshot 相关的不可变数据及其字段约束。
 */
public record ModelCostSnapshot(
        String provider,
        String model,
        double inputPricePerMillion,
        double outputPricePerMillion,
        String currency) {}
