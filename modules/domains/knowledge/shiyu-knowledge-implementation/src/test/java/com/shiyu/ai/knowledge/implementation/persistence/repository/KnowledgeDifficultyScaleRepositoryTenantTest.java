package com.shiyu.ai.knowledge.implementation.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeDifficultyScaleLevelMapper;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeDifficultyScaleMapper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * 验证 知识 Difficulty Scale Repository 租户 相关功能、边界条件、异常路径和协作行为。
 */
class KnowledgeDifficultyScaleRepositoryTenantTest {
    private static final TenantId TENANT = new TenantId(31);

    @BeforeEach
    void bindTenantScope() {
        TenantScope.set(TENANT);
    }

    @AfterEach
    void clearTenantScope() {
        TenantScope.clear();
    }

    @Test
    void addsTenantPredicateToScaleAndLevelQueries() {
        KnowledgeDifficultyScaleMapper scales = mock(KnowledgeDifficultyScaleMapper.class);
        KnowledgeDifficultyScaleLevelMapper levels =
                mock(KnowledgeDifficultyScaleLevelMapper.class);
        KnowledgeDifficultyScaleRepositoryImpl repository =
                new KnowledgeDifficultyScaleRepositoryImpl(scales, levels);

        repository.findScale(TENANT, 7L);
        repository.findLevels(TENANT, 7L);

        ArgumentCaptor<QueryWrapper> scaleQuery = ArgumentCaptor.forClass(QueryWrapper.class);
        ArgumentCaptor<QueryWrapper> levelQuery = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(scales).selectOneByQuery(scaleQuery.capture());
        verify(levels).selectListByQuery(levelQuery.capture());
        assertTrue(scaleQuery.getValue().toSQL().toLowerCase().contains("tenant_id"));
        assertTrue(levelQuery.getValue().toSQL().toLowerCase().contains("tenant_id"));
    }

    @Test
    void rejectsMissingTenantBeforeQuerying() {
        KnowledgeDifficultyScaleMapper scales = mock(KnowledgeDifficultyScaleMapper.class);
        KnowledgeDifficultyScaleLevelMapper levels =
                mock(KnowledgeDifficultyScaleLevelMapper.class);
        KnowledgeDifficultyScaleRepositoryImpl repository =
                new KnowledgeDifficultyScaleRepositoryImpl(scales, levels);

        assertThrows(IllegalArgumentException.class, () -> repository.findScale(null, 7L));
        assertThrows(IllegalArgumentException.class, () -> repository.findLevels(null, 7L));
        verifyNoInteractions(scales, levels);
    }
}
