package com.shiyu.ai.knowledge.implementation.persistence.repository;

import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeDifficultyScaleRepository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.foundation.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDifficultyScaleBO;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDifficultyScaleLevelBO;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeDifficultyScaleDO;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeDifficultyScaleLevelDO;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeDifficultyScaleLevelMapper;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeDifficultyScaleMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 负责 知识 Difficulty Scale 的持久化查询、保存和删除，并维护数据访问边界。
 */
@Repository
@RequiredArgsConstructor
public class KnowledgeDifficultyScaleRepositoryImpl
        implements com.shiyu.ai.knowledge.implementation.domain.port.repository
                .KnowledgeDifficultyScaleRepository {

    /**
     * scaleMapper 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeDifficultyScaleMapper scaleMapper;
    /**
     * 级别映射器，表示当前对象中的对应属性。
     */
    private final KnowledgeDifficultyScaleLevelMapper levelMapper;

    public KnowledgeDifficultyScaleBO findScale(TenantId tenantId, Long scaleId) {
        requireTenant(tenantId);
        return MapstructUtils.convert(
                scaleMapper.selectOneByQuery(
                        QueryWrapper.create()
                                .eq(KnowledgeDifficultyScaleDO::getId, scaleId)
                                .eq(KnowledgeDifficultyScaleDO::getTenantId, tenantId.value())
                                .eq(KnowledgeDifficultyScaleDO::getDelFlag, 0)
                                .eq(KnowledgeDifficultyScaleDO::getStatus, 1)),
                KnowledgeDifficultyScaleBO.class);
    }

    public List<KnowledgeDifficultyScaleLevelBO> findLevels(TenantId tenantId, Long scaleId) {
        requireTenant(tenantId);
        return MapstructUtils.convert(
                levelMapper.selectListByQuery(
                        QueryWrapper.create()
                                .eq(KnowledgeDifficultyScaleLevelDO::getScaleId, scaleId)
                                .eq(KnowledgeDifficultyScaleLevelDO::getTenantId, tenantId.value())
                                .eq(KnowledgeDifficultyScaleLevelDO::getDelFlag, 0)
                                .eq(KnowledgeDifficultyScaleLevelDO::getStatus, 1)
                                .orderBy(KnowledgeDifficultyScaleLevelDO::getLevel, true)),
                KnowledgeDifficultyScaleLevelBO.class);
    }

    private static void requireTenant(TenantId tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId must not be null");
        }
        TenantScope.requireMatches(tenantId);
    }
}
