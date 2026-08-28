package com.shiyu.ai.knowledge.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeDifficultyScaleLevelMapper;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeDifficultyScaleMapper;
import com.shiyu.ai.kernel.context.TenantId;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class KnowledgeDifficultyScaleRepositoryTenantTest {
    private static final TenantId TENANT = new TenantId(31);

    @Test
    void addsTenantPredicateToScaleAndLevelQueries() {
        KnowledgeDifficultyScaleMapper scales = mock(KnowledgeDifficultyScaleMapper.class);
        KnowledgeDifficultyScaleLevelMapper levels = mock(KnowledgeDifficultyScaleLevelMapper.class);
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
        KnowledgeDifficultyScaleLevelMapper levels = mock(KnowledgeDifficultyScaleLevelMapper.class);
        KnowledgeDifficultyScaleRepositoryImpl repository =
                new KnowledgeDifficultyScaleRepositoryImpl(scales, levels);

        assertThrows(IllegalArgumentException.class, () -> repository.findScale(null, 7L));
        assertThrows(IllegalArgumentException.class, () -> repository.findLevels(null, 7L));
        verifyNoInteractions(scales, levels);
    }
}
