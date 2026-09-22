package com.shiyu.ai.model.implementation.domain.port.repository;

import com.shiyu.ai.common.foundation.vo.IdNameOptionVO;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.model.implementation.domain.model.AiModelBO;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * 负责 AI 模型 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface AiModelRepository {
    /**
     * 查询 AI 模型 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param platformId 用于定位platform的标识。
     * @param pageNo 分页页码，从 1 开始。
     * @param pageSize 每页返回的数据数量。
     * @return 返回总数及当前页数据，左值为总数，右值为数据列表。
     */
    Pair<Long, List<AiModelBO>> selectPage(
            TenantId tenantId, Long platformId, Number pageNo, Number pageSize);

    /**
     * 查询 AI 模型 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param platformId 用于定位platform的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<AiModelBO> selectByPlatformId(TenantId tenantId, Long platformId);

    /**
     * 查询 AI 模型 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 AI 模型 相关操作生成的结果数据。
     */
    AiModelBO selectById(TenantId tenantId, Long id);

    /**
     * 查询 AI 模型 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param platformId 用于定位platform的标识。
     * @return 返回 AI 模型 相关操作生成的结果数据。
     */
    AiModelBO selectDefaultByPlatformId(TenantId tenantId, Long platformId);

    /**
     * 创建或保存 AI 模型 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param bo 用于完成本次业务处理的 bo 参数。
     * @return 返回 AI 模型 相关操作生成的结果数据。
     */
    AiModelBO create(TenantId tenantId, AiModelBO bo);

    /**
     * 更新或设置 AI 模型 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param bo 用于完成本次业务处理的 bo 参数。
     * @return 返回 AI 模型 相关操作生成的结果数据。
     */
    AiModelBO update(TenantId tenantId, AiModelBO bo);

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

    /**
     * 查询 AI 模型 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param platformId 用于定位platform的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<IdNameOptionVO> selectOptions(TenantId tenantId, Long platformId);

    /**
     * 删除或移除 AI 模型 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param platformId 用于定位platform的标识。
     * @param excludeId 用于定位exclude的标识。
     */
    void clearDefaultExcept(TenantId tenantId, Long platformId, Long excludeId);
}
