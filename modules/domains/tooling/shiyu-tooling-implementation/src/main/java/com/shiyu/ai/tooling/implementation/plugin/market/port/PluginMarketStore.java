package com.shiyu.ai.tooling.implementation.plugin.market.port;
import com.shiyu.ai.tooling.implementation.plugin.market.model.PluginMarketEntry;

import java.util.List;
import java.util.Optional;

/**
 * 管理 插件 Market 相关的运行时状态、注册信息或临时数据。
 */
public interface PluginMarketStore {
    /**
     * 创建或保存 插件 Market 相关业务数据，并返回处理结果。
     *
     * @param entry 用于完成本次业务处理的 entry 参数。
     * @return 返回 插件 Market 相关操作生成的结果数据。
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
     * 更新或设置 插件 Market 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param id 用于定位目标业务对象的标识。
     */
    void disable(String id);
}
