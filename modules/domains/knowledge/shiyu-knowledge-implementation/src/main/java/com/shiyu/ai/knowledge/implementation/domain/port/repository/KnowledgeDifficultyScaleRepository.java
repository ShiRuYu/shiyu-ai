package com.shiyu.ai.knowledge.implementation.domain.port.repository;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDifficultyScaleBO;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDifficultyScaleLevelBO;

import java.util.List;

/**
 * 负责 知识 Difficulty Scale 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface KnowledgeDifficultyScaleRepository {
    /**
     * 查询 知识 Difficulty Scale 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param scaleId 用于定位scale的标识。
     * @return 返回 知识 Difficulty Scale 相关操作生成的结果数据。
     */
    KnowledgeDifficultyScaleBO findScale(TenantId tenantId, Long scaleId);

    /**
     * 查询 知识 Difficulty Scale 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param scaleId 用于定位scale的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<KnowledgeDifficultyScaleLevelBO> findLevels(TenantId tenantId, Long scaleId);
}
