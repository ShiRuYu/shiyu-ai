package com.shiyu.ai.agent.contract.node;

import com.shiyu.ai.kernel.context.ActorContext;

import java.util.LinkedHashMap;
import java.util.Map;

/** 在 Agent 图运行期间传递经过认证的操作主体，并隔离对外公开的状态。 */
public final class NodeActorContext {
    /** 图状态中的可信操作主体字段。 */
    public static final String KEY = "__knowledgeAccessContext";

    private NodeActorContext() {}

    /**
     * 从图状态取得经过认证的操作主体。
     *
     * @param state 图节点收到的状态
     * @return 经过认证的操作主体
     * @throws IllegalStateException 当可信上下文缺失或类型无效时
     */
    public static ActorContext require(Map<String, Object> state) {
        Object value = state == null ? null : state.get(KEY);
        if (value instanceof ActorContext actor) return actor;
        throw new IllegalStateException("authenticated actor context is required");
    }

    /**
     * 将当前已授权操作主体绑定到图状态，覆盖旧的身份字段。
     *
     * @param actor 当前已授权操作主体
     * @param state 原始图状态
     * @return 带有可信上下文的新状态副本
     */
    public static Map<String, Object> bind(ActorContext actor, Map<String, Object> state) {
        Map<String, Object> bound = state == null ? new LinkedHashMap<>()
                : new LinkedHashMap<>(state);
        bound.put("tenantId", actor.tenantId().value());
        bound.put("userId", actor.userId().value());
        bound.put(KEY, actor);
        return bound;
    }

    /**
     * 删除图状态中的内部操作主体对象，供持久化和对外响应使用。
     *
     * @param state 图状态
     * @return 不含内部操作主体对象的新状态副本
     */
    public static Map<String, Object> publicData(Map<String, Object> state) {
        if (state == null) return Map.of();
        Map<String, Object> publicData = new LinkedHashMap<>(state);
        publicData.remove(KEY);
        return publicData;
    }
}
