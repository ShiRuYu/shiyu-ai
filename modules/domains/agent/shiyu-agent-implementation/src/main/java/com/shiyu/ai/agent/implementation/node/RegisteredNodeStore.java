package com.shiyu.ai.agent.implementation.node;

import com.shiyu.ai.agent.contract.node.BaseNode;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Runtime node instance registry and explicit service injection adapter. */
@Slf4j
final class RegisteredNodeStore {
    private final Map<String, BaseNode> nodes = new ConcurrentHashMap<>();

    void put(String nodeId, BaseNode node) {
        if (nodeId != null && !nodeId.isEmpty()) {
            nodes.put(nodeId, node);
        }
    }

    BaseNode get(String nodeId) {
        return nodes.get(nodeId);
    }

    boolean remove(String nodeId) {
        return nodes.remove(nodeId) != null;
    }

    void clear() {
        nodes.clear();
    }

    Map<String, BaseNode> snapshot() {
        return new HashMap<>(nodes);
    }

    void inject(String nodeId, String serviceName, Object service) {
        BaseNode node = get(nodeId);
        if (node == null) {
            throw new IllegalArgumentException("节点不存在：" + nodeId);
        }
        boolean injected = injectByName(node, serviceName, service);
        if (!injected) {
            injected = injectByType(node, service);
        }
        if (!injected) {
            log.warn("服务注册到节点失败，未找到匹配的字段：nodeIdPresent={}, serviceNamePresent={}, serviceTypePresent={}",
                    nodeId != null, serviceName != null,
                    service != null && service.getClass().getSimpleName() != null);
        }
    }

    private boolean injectByName(BaseNode node, String serviceName, Object service) {
        if (serviceName == null) return false;
        try {
            Field field = node.getClass().getDeclaredField(serviceName);
            field.setAccessible(true);
            if (field.getType().isInstance(service)) {
                field.set(node, service);
                return true;
            }
        } catch (NoSuchFieldException ignored) {
            // Fall through to type matching.
        } catch (Exception exception) {
            log.warn("服务字段注入失败：errorType={}, errorMessageLength={}",
                    exception.getClass().getSimpleName(), messageLength(exception));
        }
        return false;
    }

    private boolean injectByType(BaseNode node, Object service) {
        for (Class<?> type = node.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (!field.getType().isInstance(service)) continue;
                try {
                    field.setAccessible(true);
                    field.set(node, service);
                    return true;
                } catch (Exception exception) {
                    log.warn("按类型匹配注入失败：errorType={}, errorMessageLength={}",
                            exception.getClass().getSimpleName(), messageLength(exception));
                }
            }
        }
        return false;
    }

    private static int messageLength(Exception exception) {
        return exception.getMessage() == null ? 0 : exception.getMessage().length();
    }
}
