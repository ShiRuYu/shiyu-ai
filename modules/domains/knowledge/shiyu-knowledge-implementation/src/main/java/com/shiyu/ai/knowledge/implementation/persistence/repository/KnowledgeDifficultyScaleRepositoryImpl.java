package com.shiyu.ai.knowledge.implementation.persistence.repository;

import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeDifficultyScaleRepository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
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
 * {@code KnowledgeDifficultyScaleRepositoryImpl} 实现知识模块的持久化端口，负责在领域对象与存储模型之间转换。
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
    }
}
