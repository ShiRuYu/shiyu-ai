package com.shiyu.ai.tooling.implementation.plugin.market.port;
import com.shiyu.ai.tooling.implementation.plugin.market.model.PluginMarketEntry;

import java.util.List;
import java.util.Optional;

/**
 * PluginMarketStore 接口，定义工具模块的能力边界。
 */
public interface PluginMarketStore {
    /**
     * 保存或更新业务对象。
     *
     * @param entry 方法参数。
     *
     * @return 操作结果。
     */
    PluginMarketEntry save(PluginMarketEntry entry);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @return 符合条件的结果集合。
     */
    List<PluginMarketEntry> list();

    /**
     * 根据标识查询对应的数据。
     *
     * @param id 目标对象标识。
     *
     * @return 查询到的结果；未找到时为空。
     */
    Optional<PluginMarketEntry> find(String id);

    /**
     * 执行 {@code disable} 定义的接口操作。
     *
     * @param id 目标对象标识。
     */
    void disable(String id);
}
