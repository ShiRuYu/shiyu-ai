package com.shiyu.ai.agent.implementation.runtime.service;

import com.shiyu.ai.agent.contract.runtime.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * 提供 Context Assembly 的查询、创建、更新及调用服务，协调业务变更和领域协作。
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
     * 执行 Context Assembly 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param providers 用于完成本次业务处理的 providers 参数。
     * @param policy 用于完成本次业务处理的 policy 参数。
     */
    @Autowired
    public ContextAssemblyService(List<ContextRetrievalPort> providers, ContextPolicy policy) {
        this.providers = providers == null ? List.of() : List.copyOf(providers);
        this.policy = policy == null ? new DefaultContextPolicy() : policy;
    }

    /**
     * 执行 Context Assembly 相关业务数据，并返回处理结果。
     *
     * @param query 用于筛选目标数据的查询条件。
     * @return 返回 Context Assembly 相关操作生成的结果数据。
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
