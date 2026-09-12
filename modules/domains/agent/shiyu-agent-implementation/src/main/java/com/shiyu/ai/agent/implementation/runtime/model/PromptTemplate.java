package com.shiyu.ai.agent.implementation.runtime.model;

import com.shiyu.ai.agent.contract.runtime.*;

import java.time.Instant;
import java.util.List;

/**
 * {@code PromptTemplate} 封装智能体模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param id 标识，表示该记录组件承载的数据。
 * @param tenantId 租户标识，表示该记录组件承载的数据。
 * @param ownerUserId 所属用户标识，表示该记录组件承载的数据。
 * @param name 名称，表示该记录组件承载的数据。
 * @param template template 属性，表示该记录组件承载的数据。
 * @param variables 变量集合，表示该记录组件承载的数据。
 * @param status 状态，表示该记录组件承载的数据。
 * @param createdAt 创建时间，表示该记录组件承载的数据。
 * @param updatedAt 更新时间，表示该记录组件承载的数据。
 */
public record PromptTemplate(
        String id,
        long tenantId,
        long ownerUserId,
        String name,
        String template,
        List<String> variables,
        String status,
        Instant createdAt,
        Instant updatedAt) {}
