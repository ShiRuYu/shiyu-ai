package com.shiyu.ai.education.implementation.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shiyu.ai.common.foundation.exception.ServiceException;
import com.shiyu.ai.education.implementation.domain.model.StudentBO;
import com.shiyu.ai.education.implementation.domain.model.StudyRecordBO;
import com.shiyu.ai.education.implementation.domain.port.repository.StudentRepository;
import com.shiyu.ai.education.implementation.domain.port.repository.StudyRecordRepository;
import com.shiyu.ai.education.implementation.web.dto.StudentTutorResponse;
import com.shiyu.ai.education.implementation.web.request.StudentTutorRequest;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.knowledge.contract.api.KnowledgePointPort;
import com.shiyu.ai.knowledge.contract.model.KnowledgeResponse;
import com.shiyu.ai.model.contract.api.ChatEngine;
import com.shiyu.ai.model.contract.model.ChatRequest;
import com.shiyu.ai.model.contract.model.ChatResponse;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/** 验证辅导提示词仅使用租户可见资料及当前学生事实。 */
class StudentTutorServiceTest {
    private final StudentRepository students = mock(StudentRepository.class);
    private final StudyRecordRepository records = mock(StudyRecordRepository.class);
    private final KnowledgePointPort points = mock(KnowledgePointPort.class);
    private final ChatEngine model = mock(ChatEngine.class);
    private final StudentTutorService tutor = new StudentTutorService(students, records, points, model);
    private final ActorContext actor = new ActorContext(new TenantId(17L), new UserId(23L), false);

    @BeforeEach
    void setup() {
        StudentBO student = new StudentBO();
        student.setId(41L);
        student.setStatus(1);
        when(students.selectByUserId(actor.tenantId(), 23L)).thenReturn(student);
        when(model.chat(any())).thenReturn(ChatResponse.builder().success(true).content("已生成").build());
    }

    @Test
    void lessonUsesVisibleKnowledgeContentAndActorIdentity() {
        KnowledgeResponse point = new KnowledgeResponse(9L, "K9", "有理数", "整数和分数的统称",
                2, "MATH", "", List.of(), List.of(), List.of());
        when(points.getStudentEducationPoint(actor, 9L)).thenReturn(point);

        assertEquals("有理数", tutor.teach(actor,
                new StudentTutorRequest(9L, "textual", null, null, null, null)).knowledgeName());
        ArgumentCaptor<ChatRequest> sent = ArgumentCaptor.forClass(ChatRequest.class);
        verify(model).chat(sent.capture());
        assertEquals(17L, sent.getValue().getTenantId());
        assertEquals(23L, sent.getValue().getUserId());
        assertEquals("default", sent.getValue().getPlatform());
        assertTrue(sent.getValue().getMessages().getFirst().content().getFirst().text()
                .contains("整数和分数的统称"));
        verify(points).getStudentEducationPoint(actor, 9L);
    }

    @Test
    void emptyReportDoesNotInventMasteryOrCallModel() {
        when(records.selectByStudent(actor.tenantId(), 41L)).thenReturn(List.of());
        var response = tutor.report(actor, new StudentTutorRequest(null, null, null, null, null, "30d"));
        assertTrue(response.insufficientData());
        assertEquals(0, response.recordCount());
        assertTrue(response.sources().isEmpty());
        verify(model, never()).chat(any());
    }

    @Test
    void reportOnlyIncludesCurrentStudentAndSelectedPeriod() {
        StudyRecordBO recent = record(LocalDateTime.now().minusDays(2), 80D);
        recent.setId(101L);
        recent.setRecordType("PRACTICE");
        recent.setQuestionId(9L);
        StudyRecordBO old = record(LocalDateTime.now().minusDays(40), 10D);
        old.setId(102L);
        when(records.selectByStudent(actor.tenantId(), 41L)).thenReturn(List.of(recent, old));
        var response = tutor.report(actor, new StudentTutorRequest(null, null, null, null, null, "7d"));
        assertEquals(1, response.recordCount());
        assertEquals(80D, response.averageScore());
        assertEquals(List.of(101L), response.sources().stream()
                .map(source -> source.recordId()).toList());
        assertEquals(9L, response.sources().getFirst().questionId());
        ArgumentCaptor<ChatRequest> sent = ArgumentCaptor.forClass(ChatRequest.class);
        verify(model).chat(sent.capture());
        String prompt = sent.getValue().getMessages().getFirst().content().getFirst().text();
        assertTrue(prompt.contains("平均分：80.0"));
        assertTrue(!prompt.contains("平均分：10.0"));
        verify(records).selectByStudent(actor.tenantId(), 41L);
    }

    @Test
    void missingStudentCannotUseTutor() {
        when(students.selectByUserId(actor.tenantId(), 23L)).thenReturn(null);
        assertThrows(ServiceException.class, () -> tutor.knowledge(actor));
        verify(points, never()).listStudentEducationPoints(any());
    }

    @Test
    void practiceRejectsInvalidGenerationLimitsBeforeCallingTheModel() {
        when(points.getStudentEducationPoint(actor, 9L)).thenReturn(point());

        assertThrows(ServiceException.class, () -> tutor.practice(actor,
                new StudentTutorRequest(9L, null, 0, 2, null, null)));
        assertThrows(ServiceException.class, () -> tutor.practice(actor,
                new StudentTutorRequest(9L, null, 2, 11, null, null)));
        verify(model, never()).chat(any());
    }

    @Test
    void practiceDefaultsProduceClearlyMarkedSuggestions() {
        when(points.getStudentEducationPoint(actor, 9L)).thenReturn(point());

        var response = tutor.practice(actor,
                new StudentTutorRequest(9L, null, null, null, null, null));

        assertEquals("已生成", response.content());
        ArgumentCaptor<ChatRequest> sent = ArgumentCaptor.forClass(ChatRequest.class);
        verify(model).chat(sent.capture());
        String prompt = sent.getValue().getMessages().getFirst().content().getFirst().text();
        assertTrue(prompt.contains("3道练习题"));
        assertTrue(prompt.contains("难度等级2"));
        assertTrue(prompt.contains("不进入正式题库或成绩"));
    }

    @Test
    void planRejectsPastAndOverDistantTargetsBeforeCallingTheModel() {
        when(points.getStudentEducationPoint(actor, 9L)).thenReturn(point());

        assertThrows(ServiceException.class, () -> tutor.plan(actor,
                new StudentTutorRequest(9L, null, null, null,
                        LocalDate.now().toString(), null)));
        assertThrows(ServiceException.class, () -> tutor.plan(actor,
                new StudentTutorRequest(9L, null, null, null,
                        LocalDate.now().plusYears(1).plusDays(1).toString(), null)));
        verify(model, never()).chat(any());
    }

    @Test
    void reportWithUnscoredRecordsDoesNotInventAnAverage() {
        StudyRecordBO unscored = record(LocalDateTime.now(), null);
        unscored.setId(103L);
        when(records.selectByStudent(actor.tenantId(), 41L)).thenReturn(List.of(unscored));

        var response = tutor.report(actor,
                new StudentTutorRequest(null, null, null, null, null, "30d"));

        assertEquals(1, response.recordCount());
        assertEquals(null, response.averageScore());
        ArgumentCaptor<ChatRequest> sent = ArgumentCaptor.forClass(ChatRequest.class);
        verify(model).chat(sent.capture());
        assertTrue(sent.getValue().getMessages().getFirst().content().getFirst().text()
                .contains("平均分：无数据"));
    }

    @Test
    void reportUsesOnlyPeriodBoundedCurrentStudentSourcesInModelPrompt() {
        StudyRecordBO atStart = record(LocalDate.now().minusDays(29).atStartOfDay(), 70D);
        atStart.setId(201L);
        atStart.setRecordType("PRACTICE");
        atStart.setQuestionId(301L);
        StudyRecordBO today = record(LocalDateTime.now(), 90D);
        today.setId(202L);
        StudyRecordBO beforeStart = record(LocalDate.now().minusDays(30).atStartOfDay(), 1D);
        beforeStart.setId(203L);
        StudyRecordBO undated = record(null, 2D);
        undated.setId(204L);
        when(records.selectByStudent(actor.tenantId(), 41L))
                .thenReturn(List.of(atStart, today, beforeStart, undated));

        var response = tutor.report(actor, null);

        assertEquals("30d", response.period());
        assertEquals(2, response.recordCount());
        assertEquals(80D, response.averageScore());
        assertEquals(List.of(201L, 202L), response.sources().stream()
                .map(StudentTutorResponse.SourceRecord::recordId).toList());
        assertEquals("PRACTICE", response.sources().getFirst().recordType());
        assertEquals(301L, response.sources().getFirst().questionId());
        ArgumentCaptor<ChatRequest> sent = ArgumentCaptor.forClass(ChatRequest.class);
        verify(model).chat(sent.capture());
        assertEquals(17L, sent.getValue().getTenantId());
        assertEquals(23L, sent.getValue().getUserId());
        String prompt = sent.getValue().getMessages().getFirst().content().getFirst().text();
        assertTrue(prompt.contains("当前学生在30d内的服务端学习记录"));
        assertTrue(prompt.contains("学习次数：2"));
        assertTrue(prompt.contains("平均分：80.0"));
        assertTrue(!prompt.contains("平均分：1.0"));
        verify(records).selectByStudent(actor.tenantId(), 41L);
    }

    @Test
    void emptyModelResponseCannotMasqueradeAsACompletedLesson() {
        when(points.getStudentEducationPoint(actor, 9L)).thenReturn(point());
        when(model.chat(any())).thenReturn(ChatResponse.builder()
                .success(true).content("   ").build());

        assertThrows(ServiceException.class, () -> tutor.teach(actor,
                new StudentTutorRequest(9L, "visual", null, null, null, null)));
    }

    @Test
    void invalidStyleOrSourceContentCannotReachTheModel() {
        KnowledgeResponse incomplete = new KnowledgeResponse(9L, "K9", "有理数", " ",
                2, "MATH", "", List.of(), List.of(), List.of());
        when(points.getStudentEducationPoint(actor, 9L)).thenReturn(incomplete);
        assertThrows(ServiceException.class, () -> tutor.teach(actor,
                new StudentTutorRequest(9L, "audio", null, null, null, null)));
        assertThrows(ServiceException.class, () -> tutor.teach(actor,
                new StudentTutorRequest(9L, "textual", null, null, null, null)));
        verify(model, never()).chat(any());
    }

    @Test
    void futureStudyPlanNamesItsDeadlineWithoutInventingStudentMastery() {
        when(points.getStudentEducationPoint(actor, 9L)).thenReturn(point());
        LocalDate deadline = LocalDate.now().plusDays(21);

        var response = tutor.plan(actor, new StudentTutorRequest(9L, null, null, null,
                deadline.toString(), null));

        assertEquals(9L, response.knowledgeId());
        ArgumentCaptor<ChatRequest> sent = ArgumentCaptor.forClass(ChatRequest.class);
        verify(model).chat(sent.capture());
        String prompt = sent.getValue().getMessages().getFirst().content().getFirst().text();
        assertTrue(prompt.contains(deadline.toString()));
        assertTrue(prompt.contains("没有学习记录时不要断言学生的掌握程度"));
    }

    @Test
    void invalidReportPeriodDoesNotReadOrInventLearningRecords() {
        assertThrows(ServiceException.class, () -> tutor.report(actor,
                new StudentTutorRequest(null, null, null, null, null, "365d")));
        verify(records, never()).selectByStudent(any(), any());
        verify(model, never()).chat(any());
    }

    @Test
    void tutoringRequiresAnAuthenticatedStudentAndExplicitVisibleKnowledge() {
        assertThrows(ServiceException.class, () -> tutor.knowledge(null));
        assertThrows(ServiceException.class, () -> tutor.teach(actor, null));
        assertThrows(ServiceException.class, () -> tutor.practice(actor,
                new StudentTutorRequest(null, null, null, null, null, null)));
        verify(points, never()).getStudentEducationPoint(any(), any());
        verify(model, never()).chat(any());
    }

    @Test
    void malformedPlanDateAndFailedModelDoNotCreateStudentGuidance() {
        when(points.getStudentEducationPoint(actor, 9L)).thenReturn(point());
        assertThrows(ServiceException.class, () -> tutor.plan(actor,
                new StudentTutorRequest(9L, null, null, null, "tomorrow", null)));
        verify(model, never()).chat(any());

        when(model.chat(any())).thenReturn(null);
        assertThrows(ServiceException.class, () -> tutor.teach(actor,
                new StudentTutorRequest(9L, null, null, null, null, null)));
        when(model.chat(any())).thenReturn(ChatResponse.builder()
                .success(false).content("不可靠内容").build());
        assertThrows(ServiceException.class, () -> tutor.practice(actor,
                new StudentTutorRequest(9L, null, 2, 2, null, null)));
    }

    private static KnowledgeResponse point() {
        return new KnowledgeResponse(9L, "K9", "有理数", "整数和分数的统称",
                2, "MATH", "", List.of(), List.of(), List.of());
    }

    private static StudyRecordBO record(LocalDateTime time, Double score) {
        StudyRecordBO record = new StudyRecordBO();
        record.setCreateTime(time);
        record.setScore(score);
        return record;
    }
}
