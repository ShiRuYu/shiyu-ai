package com.shiyu.ai.vector.factory;

import com.shiyu.ai.vector.config.VectorStoreProperties;
import com.shiyu.ai.vector.VectorStore;
import com.shiyu.ai.vector.impl.InMemoryVectorStore;
import com.shiyu.ai.vector.impl.JVectorStore;
import lombok.extern.slf4j.Slf4j;

/**
 * VectorStore 工厂
 *
 * <p>根据配置类型创建对应的 VectorStore 实例。
 * 新增实现时只需在此 switch 中添加分支，无需额外接口或注册文件。</p>
 */
@Slf4j
public class VectorStoreFactory {

    public static VectorStore create(String type, VectorStoreProperties properties) {
        VectorStore store = switch (type.toLowerCase()) {
            case "inmemory" -> new InMemoryVectorStore(properties.getDimension());
            case "jvector"  -> new JVectorStore(properties);
            default -> throw new IllegalArgumentException(
                    "未知的 VectorStore 类型: " + type + "，可用类型: inmemory, jvector");
        };
        log.info("VectorStore 已创建: type={}, class={}", store.type(), store.getClass().getSimpleName());
        return store;
    }
}
