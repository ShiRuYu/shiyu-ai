package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.education.domain.AbilityValue;
import com.shiyu.ai.education.domain.BloomTaxonomy;
import com.shiyu.ai.education.domain.model.AbilityBO;
import com.shiyu.ai.education.port.repository.AbilityRepository;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AbilityServiceImplTest {

    private static final ActorContext ACTOR = new ActorContext(new TenantId(9), new UserId(7), false);
    private final AbilityRepository repository = mock(AbilityRepository.class);
    private final AbilityServiceImpl service = new AbilityServiceImpl(repository);

    @Test
    void missingAbilityReturnsAnEmptyValueAndRequiresActor() {
        when(repository.selectByStudentAndKnowledge(ACTOR.tenantId(), 10L, 20L)).thenReturn(null);
        AbilityValue value = service.get(ACTOR, 10L, 20L);
        assertEquals(10L, value.studentId());
        assertEquals(20L, value.knowledgeId());
        assertEquals(0.0, value.overallScore());
        assertThrows(NullPointerException.class, () -> service.get(null, 10L, 20L));
    }

    @Test
    void mapsPersistedAbilityValues() {
        AbilityBO stored = ability(10L, 20L, 12.0);
        when(repository.selectByStudentAndKnowledge(ACTOR.tenantId(), 10L, 20L)).thenReturn(stored);

        AbilityValue value = service.get(ACTOR, 10L, 20L);
        assertEquals(12.0, value.remember());
        assertEquals(13.0, value.understand());
        assertEquals(14.0, value.apply());
        assertEquals(15.0, value.analyze());
        assertEquals(16.0, value.evaluate());
        assertEquals(17.0, value.create());
    }

    @Test
    void insertsNewAbilityAndUpdatesEveryBloomDimension() {
        for (BloomTaxonomy dimension : BloomTaxonomy.values()) {
            when(repository.selectByStudentAndKnowledge(ACTOR.tenantId(), 10L, 20L)).thenReturn(null);
            service.update(ACTOR, 10L, 20L, dimension, 1.0);
        }
        verify(repository, org.mockito.Mockito.times(BloomTaxonomy.values().length))
                .insert(eq(ACTOR.tenantId()), any(AbilityBO.class));

        AbilityBO existing = ability(10L, 20L, 50.0);
        when(repository.selectByStudentAndKnowledge(ACTOR.tenantId(), 10L, 20L)).thenReturn(existing);
        service.update(ACTOR, 10L, 20L, BloomTaxonomy.CREATE, 10.0);
        assertEquals(100.0, existing.getCreateScore());
        verify(repository).update(ACTOR.tenantId(), existing);
        assertThrows(NullPointerException.class,
                () -> service.update(null, 10L, 20L, BloomTaxonomy.APPLY, 1.0));
    }

    private static AbilityBO ability(Long studentId, Long knowledgeId, double base) {
        AbilityBO value = new AbilityBO();
        value.setStudentId(studentId);
        value.setKnowledgeId(knowledgeId);
        value.setRemember(base);
        value.setUnderstand(base + 1);
        value.setApply(base + 2);
        value.setAnalyze(base + 3);
        value.setEvaluate(base + 4);
        value.setCreateScore(base + 5);
        return value;
    }
}
