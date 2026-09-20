package com.shiyu.ai.iam.implementation.port.repository;

import com.shiyu.ai.iam.implementation.domain.model.DictBO;
import com.shiyu.ai.kernel.context.TenantId;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * 负责 Dict 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface DictRepository {
    /**
     * 查询 Dict 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param pageNo 分页页码，从 1 开始。
     * @param pageSize 每页返回的数据数量。
     * @return 返回总数及当前页数据，左值为总数，右值为数据列表。
     */
    Pair<Long, List<DictBO>> selectPage(TenantId tenantId, Number pageNo, Number pageSize);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     *
     * @return 符合条件的结果集合。
     */
    List<DictBO> selectAll(TenantId tenantId);

    /**
     * 查询 Dict 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 Dict 相关操作生成的结果数据。
     */
    DictBO selectById(TenantId tenantId, Long id);

    /**
     * 查询 Dict 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param dictType 用于完成本次业务处理的 dictType 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<DictBO> selectByDictType(TenantId tenantId, String dictType);

    /**
     * 创建或保存 Dict 相关业务数据，并返回处理结果。
     *
     * @param dictBO 用于完成本次业务处理的 dictBO 参数。
     * @return 返回 Dict 相关操作生成的结果数据。
     */
    DictBO create(DictBO dictBO);

    /**
     * 更新或设置 Dict 相关业务数据，并返回处理结果。
     *
     * @param dictBO 用于完成本次业务处理的 dictBO 参数。
     * @return 返回 Dict 相关操作生成的结果数据。
     */
    DictBO update(DictBO dictBO);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     */
    void deleteById(TenantId tenantId, Long id);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param tenantId 租户标识。
     * @param ids 目标对象标识集合。
     */
    void deleteByIds(TenantId tenantId, List<Long> ids);
}
