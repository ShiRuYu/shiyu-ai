package com.shiyu.ai.auth.service;

import com.shiyu.ai.auth.domain.bo.SysTenantBO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * 租户服务层
 *
 * @author shiyu-ai
 */
public interface SysTenantService {

    /**
     * 分页查询租户列表
     *
     * @param pageNumber 页码
     * @param pageSize   每页数量
     * @return 租户列表
     */
    Pair<Long, List<SysTenantBO>> getAll(Number pageNumber, Number pageSize);

    /**
     * 根据 ID 查询租户
     *
     * @param id 租户 ID
     * @return 租户信息
     */
    SysTenantBO getById(Long id);

    /**
     * 创建租户
     *
     * @param sysTenantBO 租户信息
     * @return 创建后的租户信息
     */
    SysTenantBO create(SysTenantBO sysTenantBO);

    /**
     * 更新租户
     *
     * @param sysTenantBO 租户信息
     * @return 更新后的租户信息
     */
    SysTenantBO update(SysTenantBO sysTenantBO);

    /**
     * 删除租户
     *
     * @param id 租户 ID
     */
    void deleteById(Long id);
}
