package com.shiyu.ai.model.implementation.infrastructure.gateway;

/**
 * {@code ModelCostSnapshot} 封装模型模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param provider 提供方，表示该记录组件承载的数据。
 * @param model 模型，表示该记录组件承载的数据。
 * @param inputPricePerMillion inputPricePerMillion 属性，表示该记录组件承载的数据。
 * @param outputPricePerMillion outputPricePerMillion 属性，表示该记录组件承载的数据。
 * @param currency currency 属性，表示该记录组件承载的数据。
 */
public record ModelCostSnapshot(
        String provider,
        String model,
        double inputPricePerMillion,
        double outputPricePerMillion,
        String currency) {}
