package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.*;
import com.shiyu.ai.education.port.repository.*;
import com.shiyu.ai.education.dto.*;
import com.shiyu.ai.education.request.*;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"rawtypes", "unchecked"})
class BasicEducationCrudServiceTest {
    private static final ActorContext ACTOR = new ActorContext(new TenantId(41), new UserId(42), false);

    @Test
    void subjectServiceCoversTenantReadsWritesAndMissingUpdate() {
        SubjectRepository repo = mock(SubjectRepository.class);
        SubjectBO bo = new SubjectBO(); bo.setId(1L);
        when(repo.selectById(ACTOR.tenantId(), 1L)).thenReturn(bo, bo, null);
        when(repo.selectByCode(ACTOR.tenantId(), "math")).thenReturn(bo);
        when(repo.selectByGradeLevel(ACTOR.tenantId(), "5")).thenReturn(List.of(bo));
        when(repo.selectPage(ACTOR.tenantId(), 1, 10)).thenReturn(new PageData<>(List.of(bo), 1));
        when(repo.selectAll(ACTOR.tenantId())).thenReturn(List.of(bo));
        SubjectServiceImpl service = new SubjectServiceImpl(repo);
        SubjectRequest request = new SubjectRequest(); request.setId(1L); request.setCode("math"); request.setName("Math"); request.setGradeLevel("5");
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(any(SubjectBO.class), eq(SubjectResponse.class))).thenReturn(null);
            mapper.when(() -> MapstructUtils.convert(any(List.class), eq(SubjectResponse.class))).thenReturn(List.of());
            assertNull(service.getById(ACTOR, 1L)); assertNull(service.getByCode(ACTOR, "math"));
            assertTrue(service.listByGradeLevel(ACTOR, "5").isEmpty()); assertEquals(1, service.page(ACTOR, 1, 10).getTotal());
            assertNull(service.create(ACTOR, request)); service.update(ACTOR, request); service.update(ACTOR, request);
            service.deleteById(ACTOR, 1L); verify(repo).insert(eq(ACTOR.tenantId()), any()); verify(repo).update(eq(ACTOR.tenantId()), any());
            assertThrows(NullPointerException.class, () -> service.getById(null, 1L));
        }
    }

    @Test
    void wrongQuestionServiceCoversQueriesAndDefaultValues() {
        WrongQuestionRepository repo = mock(WrongQuestionRepository.class); WrongQuestionBO bo = new WrongQuestionBO(); bo.setId(2L);
        when(repo.selectById(ACTOR.tenantId(), 2L)).thenReturn(bo, bo, null);
        when(repo.selectByStudentId(ACTOR.tenantId(), 7L)).thenReturn(List.of(bo)); when(repo.selectByStudentAndQuestion(ACTOR.tenantId(), 7L, 9L)).thenReturn(bo);
        WrongQuestionServiceImpl service = new WrongQuestionServiceImpl(repo); WrongQuestionRequest req = new WrongQuestionRequest();
        req.setId(2L); req.setStudentId(7L); req.setQuestionId(9L);
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(any(WrongQuestionBO.class), eq(WrongQuestionResponse.class))).thenReturn(null);
            mapper.when(() -> MapstructUtils.convert(any(List.class), eq(WrongQuestionResponse.class))).thenReturn(List.of());
            assertNull(service.getById(ACTOR, 2L)); assertTrue(service.listByStudentId(ACTOR, 7L).isEmpty()); assertNull(service.getByStudentAndQuestion(ACTOR, 7L, 9L));
            assertNull(service.create(ACTOR, req)); service.update(ACTOR, req); service.update(ACTOR, req); service.deleteById(ACTOR, 2L);
            verify(repo).insert(eq(ACTOR.tenantId()), any()); verify(repo).update(eq(ACTOR.tenantId()), any());
        }
    }

    @Test
    void studyPlanServiceCoversLifecycleAndTodayTasks() {
        StudyPlanRepository plans = mock(StudyPlanRepository.class); StudyPlanItemRepository items = mock(StudyPlanItemRepository.class);
        StudyPlanBO plan = new StudyPlanBO(); plan.setId(3L); plan.setStudentId(7L); when(plans.selectById(ACTOR.tenantId(), 3L)).thenReturn(plan, plan, null);
        when(plans.selectByStudentId(ACTOR.tenantId(), 7L)).thenReturn(List.of(), List.of(), List.of(plan)); when(plans.selectActiveByStudent(ACTOR.tenantId(), 7L)).thenReturn(List.of(plan));
        StudyPlanItemBO item = new StudyPlanItemBO(); item.setId(4L); item.setKnowledgeId(5L); item.setPlanDate(LocalDate.now()); item.setStatus(1); item.setOrderNo(2); item.setStatusDesc("todo");
        when(items.selectTodayItems(ACTOR.tenantId(), List.of(3L))).thenReturn(List.of(item));
        StudyPlanServiceImpl service = new StudyPlanServiceImpl(plans, items); StudyPlanRequest req = new StudyPlanRequest(); req.setId(3L); req.setStudentId(7L); req.setName("plan"); req.setStatus("2");
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(any(StudyPlanBO.class), eq(StudyPlanResponse.class))).thenReturn(null);
            mapper.when(() -> MapstructUtils.convert(any(List.class), eq(StudyPlanResponse.class))).thenReturn(List.of());
            assertNull(service.getById(ACTOR, 3L)); assertTrue(service.listByStudentId(ACTOR, 7L).isEmpty()); assertTrue(service.listActiveByStudent(ACTOR, 7L).isEmpty());
            assertNull(service.create(ACTOR, req)); service.update(ACTOR, req); service.update(ACTOR, req); service.deleteById(ACTOR, 3L);
            assertTrue(service.getTodayTasks(ACTOR, 7L).isEmpty()); assertEquals(1, service.getTodayTasks(ACTOR, 7L).size());
            verify(plans).insert(eq(ACTOR.tenantId()), any()); verify(plans).update(eq(ACTOR.tenantId()), any());
        }
    }

    @Test
    void textbookResourceAndExamServicesCoverCrudBranches() {
        TextbookRepository textbooks = mock(TextbookRepository.class); TextbookBO tb = new TextbookBO(); tb.setId(5L);
        when(textbooks.selectById(ACTOR.tenantId(), 5L)).thenReturn(tb, tb, null); when(textbooks.selectPage(ACTOR.tenantId(), 1, 10)).thenReturn(new PageData<>(List.of(tb), 1));
        when(textbooks.selectBySubjectAndGrade(ACTOR.tenantId(), "m", 5)).thenReturn(List.of(tb)); when(textbooks.selectAll(ACTOR.tenantId())).thenReturn(List.of(tb));
        TextbookRequest tr = new TextbookRequest(); tr.setId(5L); tr.setName("book"); tr.setSubjectCode("m"); tr.setGrade(5); tr.setPublisher("pub");
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(any(TextbookBO.class), eq(TextbookResponse.class))).thenReturn(null); mapper.when(() -> MapstructUtils.convert(any(List.class), eq(TextbookResponse.class))).thenReturn(List.of());
            TextbookServiceImpl service = new TextbookServiceImpl(textbooks); assertNull(service.getById(ACTOR, 5L)); assertEquals(1, service.page(ACTOR, 1, 10).getTotal()); assertTrue(service.listBySubjectAndGrade(ACTOR, "m", 5).isEmpty()); assertTrue(service.listAll(ACTOR).isEmpty()); assertNull(service.create(ACTOR, tr)); service.update(ACTOR, tr); service.update(ACTOR, tr); service.deleteById(ACTOR, 5L); verify(textbooks).insert(eq(ACTOR.tenantId()), any());
        }

        ResourceRepository resources = mock(ResourceRepository.class); ResourceBO resource = new ResourceBO(); resource.setId(6L);
        when(resources.selectById(ACTOR.tenantId(), 6L)).thenReturn(resource, resource, null); when(resources.selectBySubjectCode(ACTOR.tenantId(), "m")).thenReturn(List.of(resource)); when(resources.selectByType(ACTOR.tenantId(), "video")).thenReturn(List.of(resource)); when(resources.selectPage(ACTOR.tenantId(), 1, 10)).thenReturn(new PageData<>(List.of(resource), 1)); when(resources.selectAll(ACTOR.tenantId())).thenReturn(List.of(resource));
        ResourceRequest rr = new ResourceRequest(); rr.setId(6L); rr.setName("r"); rr.setType("video"); rr.setUrl("u");
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) { mapper.when(() -> MapstructUtils.convert(any(ResourceBO.class), eq(ResourceResponse.class))).thenReturn(null); mapper.when(() -> MapstructUtils.convert(any(List.class), eq(ResourceResponse.class))).thenReturn(List.of()); ResourceServiceImpl service = new ResourceServiceImpl(resources); assertNull(service.getById(ACTOR, 6L)); assertTrue(service.listBySubjectCode(ACTOR, "m").isEmpty()); assertTrue(service.listByType(ACTOR, "video").isEmpty()); assertEquals(1, service.page(ACTOR, 1, 10).getTotal()); assertTrue(service.listAll(ACTOR).isEmpty()); assertNull(service.create(ACTOR, rr)); service.update(ACTOR, rr); service.update(ACTOR, rr); service.deleteById(ACTOR, 6L); verify(resources).insert(eq(ACTOR.tenantId()), any()); }

        ExamRepository exams = mock(ExamRepository.class); ExamBO exam = new ExamBO(); exam.setId(8L);
        when(exams.selectById(ACTOR.tenantId(), 8L)).thenReturn(exam, exam, null); when(exams.selectBySubjectCode(ACTOR.tenantId(), "m")).thenReturn(List.of(exam)); when(exams.selectByTeacherId(ACTOR.tenantId(), 9L)).thenReturn(List.of(exam)); when(exams.selectPage(ACTOR.tenantId(), 1, 10)).thenReturn(new PageData<>(List.of(exam), 1));
        ExamRequest er = new ExamRequest(); er.setId(8L); er.setName("e"); er.setType("quiz"); er.setSubjectCode("m"); er.setGrade(5); er.setDurationMin(30); er.setTotalScore(100);
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) { mapper.when(() -> MapstructUtils.convert(any(ExamBO.class), eq(ExamResponse.class))).thenReturn(null); mapper.when(() -> MapstructUtils.convert(any(List.class), eq(ExamResponse.class))).thenReturn(List.of()); ExamServiceImpl service = new ExamServiceImpl(exams); assertNull(service.getById(ACTOR, 8L)); assertTrue(service.listBySubjectCode(ACTOR, "m").isEmpty()); assertTrue(service.listByTeacherId(ACTOR, 9L).isEmpty()); assertEquals(1, service.page(ACTOR, 1, 10).getTotal()); assertNull(service.submit(ACTOR, 8L, null)); assertNull(service.create(ACTOR, er)); service.update(ACTOR, er); service.update(ACTOR, er); service.deleteById(ACTOR, 8L); verify(exams).insert(eq(ACTOR.tenantId()), any()); }
    }
}
