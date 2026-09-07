package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.AbilityBO;
import com.shiyu.ai.education.domain.model.StudyRecordBO;
import com.shiyu.ai.education.dto.AbilityRadarResponse;
import com.shiyu.ai.education.dto.OverviewResponse;
import com.shiyu.ai.education.dto.StudyRecordResponse;
import com.shiyu.ai.education.port.repository.AbilityRepository;
import com.shiyu.ai.education.port.repository.StudyRecordRepository;
import com.shiyu.ai.education.request.StudyRecordRequest;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class AnalyticsServiceImplTest {

    private static final ActorContext ACTOR = new ActorContext(new TenantId(9), new UserId(7), false);
    private final StudyRecordRepository records = mock(StudyRecordRepository.class);
    private final AbilityRepository abilities = mock(AbilityRepository.class);
    private final AnalyticsServiceImpl service = new AnalyticsServiceImpl(records, abilities);

    @Test
    void listsAndCreatesRecordsWithinTheActorTenant() {
        StudyRecordBO stored = new StudyRecordBO();
        stored.setStudentId(10L);
        when(records.selectByStudent(ACTOR.tenantId(), 10L)).thenReturn(List.of(stored));
        when(records.selectByStudentAndKnowledge(ACTOR.tenantId(), 10L, 20L)).thenReturn(List.of(stored));
        StudyRecordRequest request = new StudyRecordRequest();
        request.setStudentId(10L);
        request.setKnowledgeId(20L);
        request.setRecordType("PRACTICE");
        request.setAccuracy(0.8);
        request.setDurationSec(30);

        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            StudyRecordResponse response = mock(StudyRecordResponse.class);
            mapper.when(() -> MapstructUtils.convert(any(List.class), eq(StudyRecordResponse.class)))
                    .thenReturn(List.of(response));
            mapper.when(() -> MapstructUtils.convert(any(StudyRecordBO.class), eq(StudyRecordResponse.class)))
                    .thenReturn(response);

            assertEquals(1, service.listRecordsByStudent(ACTOR, 10L).size());
            assertEquals(1, service.listRecordsByStudentAndKnowledge(ACTOR, 10L, 20L).size());
            assertEquals(response, service.createRecord(ACTOR, request));
        }

        verify(records).selectByStudent(ACTOR.tenantId(), 10L);
        verify(records).selectByStudentAndKnowledge(ACTOR.tenantId(), 10L, 20L);
        verify(records).insert(eq(ACTOR.tenantId()), any(StudyRecordBO.class));
    }

    @Test
    void buildsAbilityRadarWithZerosForMissingDimensions() {
        when(abilities.selectByStudentAndKnowledge(ACTOR.tenantId(), 10L, 20L)).thenReturn(null);
        AbilityRadarResponse empty = service.getAbilityRadar(ACTOR, 10L, 20L);
        assertEquals(0.0, empty.overallMastery());
        assertEquals(6, empty.abilities().size());
        assertEquals(0.0, empty.abilities().get("create"));

        AbilityBO ability = new AbilityBO();
        ability.setRemember(10.0);
        ability.setOverallMastery(42.5);
        when(abilities.selectByStudentAndKnowledge(ACTOR.tenantId(), 10L, 20L)).thenReturn(ability);
        assertEquals(42.5, service.getAbilityRadar(ACTOR, 10L, 20L).overallMastery());
        assertEquals(10.0, service.getAbilityRadar(ACTOR, 10L, 20L).abilities().get("remember"));
    }

    @Test
    void aggregatesOverviewWeakPointsAndTrend() {
        when(records.selectByStudent(ACTOR.tenantId(), 10L)).thenReturn(List.of());
        OverviewResponse empty = service.getOverview(ACTOR, 10L);
        assertEquals(0, empty.totalStudyDays());

        StudyRecordBO practice = new StudyRecordBO();
        practice.setKnowledgeId(20L);
        practice.setRecordType("PRACTICE");
        practice.setAccuracy(0.8);
        practice.setDurationSec(1800);
        practice.setCreateTime(LocalDateTime.now());
        StudyRecordBO learn = new StudyRecordBO();
        learn.setKnowledgeId(21L);
        learn.setRecordType("LEARN");
        learn.setAccuracy(1.0);
        learn.setDurationSec(600);
        learn.setCreateTime(LocalDateTime.now());
        StudyRecordBO sparse = new StudyRecordBO();
        sparse.setRecordType("READ");
        when(records.selectByStudent(ACTOR.tenantId(), 10L)).thenReturn(List.of(practice, learn, sparse));
        OverviewResponse overview = service.getOverview(ACTOR, 10L);
        assertEquals(2, overview.totalKnowledge());
        assertEquals(1, overview.masteredKnowledge());
        assertEquals(1, overview.totalQuestions());
        assertEquals(90.0, overview.accuracy());
        assertEquals(0.7, overview.weeklyHours());

        AbilityBO weak = new AbilityBO();
        weak.setKnowledgeId(30L);
        weak.setOverallMastery(20.0);
        AbilityBO strong = new AbilityBO();
        strong.setKnowledgeId(31L);
        strong.setOverallMastery(80.0);
        AbilityBO unknown = new AbilityBO();
        unknown.setKnowledgeId(32L);
        when(abilities.selectByStudent(ACTOR.tenantId(), 10L)).thenReturn(List.of(strong, weak, unknown));
        assertEquals(List.of(20.0), service.getWeakPoints(ACTOR, 10L).stream()
                .map(com.shiyu.ai.education.dto.WeakPointResponse::mastery).toList());
        assertEquals(7, service.getTrend(ACTOR, 10L).dates().size());
        assertEquals(7, service.getTrend(ACTOR, 10L).values().size());
        assertNotNull(service.getTrend(ACTOR, 10L));
    }
}
