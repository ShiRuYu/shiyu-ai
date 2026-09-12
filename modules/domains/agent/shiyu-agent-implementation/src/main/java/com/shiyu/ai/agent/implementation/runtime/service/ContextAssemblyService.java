package com.shiyu.ai.agent.implementation.runtime.service;

import com.shiyu.ai.agent.contract.runtime.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * ContextAssemblyService 服务接口，负责执行智能体领域相关业务操作。
 */
@Service
public class ContextAssemblyService implements ContextAssemblyPort {
    /**
     * providers 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final List<ContextRetrievalPort> providers;
    /**
     * 策略，表示当前对象中的对应属性。
     */
    private final ContextPolicy policy;

    /**
     * {@code ContextAssemblyService} 创建并初始化当前类型实例。
     *
     * @param providers 参数值，用于执行当前操作。
     * @param policy 参数值，用于执行当前操作。
     */
    @Autowired
    public ContextAssemblyService(List<ContextRetrievalPort> providers, ContextPolicy policy) {
        this.providers = providers == null ? List.of() : List.copyOf(providers);
        this.policy = policy == null ? new DefaultContextPolicy() : policy;
    }

    /**
     * {@code retrieve} 执行当前类型定义的业务操作。
     *
     * @param query 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public ContextAssemblyPort.ContextResult retrieve(ContextQuery query) {
        List<ContextItem> items =
                providers.stream()
                        .flatMap(provider -> provider.retrieve(query).stream())
                        .filter(item -> policy.canRead(item, query))
                        .sorted(Comparator.comparingDouble(ContextItem::score).reversed())
                        .limit(query.topK())
                        .toList();
        return new ContextAssemblyPort.ContextResult(
                items,
                new ContextTrace(
                        UUID.randomUUID().toString(),
                        query.tenantId(),
                        query.text(),
                        items.stream().map(ContextItem::sourceId).toList(),
                        query.namespace(),
                        Instant.now()));
    }
}
