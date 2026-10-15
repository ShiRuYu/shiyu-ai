package com.shiyu.ai.education.implementation.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shiyu.ai.common.foundation.exception.ServiceException;
import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.common.foundation.utils.MapstructUtils;
import com.shiyu.ai.education.implementation.domain.model.ExamBO;
import com.shiyu.ai.education.implementation.domain.model.ExamQuestionBO;
import com.shiyu.ai.education.implementation.domain.model.ExamSectionBO;
import com.shiyu.ai.education.implementation.domain.model.QuestionBO;
import com.shiyu.ai.education.implementation.domain.model.StudentBO;
import com.shiyu.ai.education.implementation.domain.port.repository.ExamRepository;
import com.shiyu.ai.education.implementation.domain.port.repository.ExamTakingRepository;
import com.shiyu.ai.education.implementation.domain.port.repository.QuestionRepository;
import com.shiyu.ai.education.implementation.domain.port.repository.StudentRepository;
import com.shiyu.ai.education.implementation.web.dto.ExamResponse;
import com.shiyu.ai.education.implementation.web.request.SubmitExamRequest;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
import com.shiyu.ai.kernel.context.UserId;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 覆盖学生归属校验、题目快照计分及截止时间状态转换。 */
class ExamTakingServiceTest {
    private static final ActorContext ACTOR =
            new ActorContext(new TenantId(9L), new UserId(37L), false);
    private final ExamRepository exams = mock(ExamRepository.class);
    private final ExamTakingRepository taking = mock(ExamTakingRepository.class);
    private final QuestionRepository questions = mock(QuestionRepository.class);
    private final StudentRepository students = mock(StudentRepository.class);
    private final ExamTakingService service =
            new ExamTakingService(exams, taking, questions, students);

    @Test
    void paperIsOrderedAndNeverContainsTheAnswerOrAnalysis() {
        when(exams.selectById(ACTOR.tenantId(), 5L)).thenReturn(exam(1));
        when(students.selectByUserId(ACTOR.tenantId(), 37L)).thenReturn(student());
        ExamSectionBO section = new ExamSectionBO();
        section.setId(10L);
        section.setExamId(5L);
        section.setName("选择题");
        section.setOrderNo(1);
        section.setScorePerQ(BigDecimal.TEN);
        ExamQuestionBO link = new ExamQuestionBO();
        link.setId(20L);
        link.setExamId(5L);
        link.setSectionId(10L);
        link.setQuestionId(30L);
        link.setOrderNo(1);
        link.setScore(BigDecimal.TEN);
        QuestionBO question = new QuestionBO();
        question.setId(30L);
        question.setTitle("2 + 2?");
        question.setType("CHOICE");
        question.setPurpose("EXAM_PRIVATE");
        question.setOptions("[\"A. 3\",\"B. 4\"]");
        question.setAnswer("B");
        question.setAnalysis("Add the terms");
        question.setStatus(1);
        when(taking.sections(ACTOR.tenantId(), 5L)).thenReturn(List.of(section));
        when(taking.questions(ACTOR.tenantId(), 5L)).thenReturn(List.of(link));
        when(questions.selectById(ACTOR.tenantId(), 30L)).thenReturn(question);

        var paper = service.paper(ACTOR, 5L);

        assertEquals("选择题", paper.sections().getFirst().name());
        assertEquals("A", paper.sections().getFirst().questions().getFirst().options().getFirst().value());
        assertEquals("A. 3", paper.sections().getFirst().questions().getFirst().options().getFirst().label());
        assertFalse(paper.toString().contains("Add the terms"));
        assertFalse(paper.toString().contains("answer="));
    }

    @Test
    void draftExamCannotExposeItsPaper() {
        when(exams.selectById(ACTOR.tenantId(), 5L)).thenReturn(exam(0));
        when(students.selectByUserId(ACTOR.tenantId(), 37L)).thenReturn(student());
        assertThrows(ServiceException.class, () -> service.paper(ACTOR, 5L));
    }

    @Test
    void paperIsUnavailableOutsideTheExamWindowOrStudentsGrade() {
        paperFixture();
        ExamBO outsideGrade = exam(1);
        outsideGrade.setGrade(8);
        when(exams.selectById(ACTOR.tenantId(), 5L)).thenReturn(outsideGrade);
        assertThrows(ServiceException.class, () -> service.paper(ACTOR, 5L));

        ExamBO future = exam(1);
        future.setStartTime(LocalDateTime.now().plusMinutes(10));
        when(exams.selectById(ACTOR.tenantId(), 5L)).thenReturn(future);
        assertThrows(ServiceException.class, () -> service.paper(ACTOR, 5L));

        ExamBO ended = exam(1);
        ended.setEndTime(LocalDateTime.now().minusMinutes(1));
        when(exams.selectById(ACTOR.tenantId(), 5L)).thenReturn(ended);
        assertThrows(ServiceException.class, () -> service.paper(ACTOR, 5L));
    }

    @Test
    void paperRequiresAnExistingExamWithACompleteTimeWindow() {
        paperFixture();
        when(exams.selectById(ACTOR.tenantId(), 5L)).thenReturn(null);
        assertThrows(ServiceException.class, () -> service.paper(ACTOR, 5L));
        ExamBO missingStart = exam(1);
        missingStart.setStartTime(null);
        when(exams.selectById(ACTOR.tenantId(), 5L)).thenReturn(missingStart);
        assertThrows(ServiceException.class, () -> service.paper(ACTOR, 5L));
        ExamBO missingEnd = exam(1);
        missingEnd.setEndTime(null);
        when(exams.selectById(ACTOR.tenantId(), 5L)).thenReturn(missingEnd);
        assertThrows(ServiceException.class, () -> service.paper(ACTOR, 5L));
        verify(taking, never()).insertAttempt(any(), any());
    }

    @Test
    void expiryWorkerSkipsMissingFutureAndMalformedAttempts() {
        TenantScope.withTenant(ACTOR.tenantId(), () -> {
            service.settleExpiredAttempt(ACTOR.tenantId(), 99L);
            when(taking.findAttemptById(ACTOR.tenantId(), 99L)).thenReturn(
                    new ExamTakingRepository.Attempt(99L, 5L, 4L, "STARTED",
                            LocalDateTime.now().minusMinutes(1), null,
                            null, null, BigDecimal.TEN));
            service.settleExpiredAttempt(ACTOR.tenantId(), 99L);
            when(taking.findAttemptById(ACTOR.tenantId(), 99L)).thenReturn(
                    new ExamTakingRepository.Attempt(99L, 5L, 4L, "STARTED",
                            LocalDateTime.now().minusMinutes(1),
                            LocalDateTime.now().plusMinutes(5), null, null, BigDecimal.TEN));
            service.settleExpiredAttempt(ACTOR.tenantId(), 99L);
            return null;
        });
        verify(taking, never()).claimAttempt(any(), any(), any(), any(), anyBoolean());
    }

    @Test
    void publishedPaperRejectsAnAccidentallyLinkedPublicPracticeQuestion() {
        paperFixture();
        QuestionBO publicQuestion = new QuestionBO();
        publicQuestion.setId(30L);
        publicQuestion.setStatus(1);
        publicQuestion.setType("CHOICE");
        publicQuestion.setPurpose("PRACTICE");
        when(questions.selectById(ACTOR.tenantId(), 30L)).thenReturn(publicQuestion);
        assertThrows(ServiceException.class, () -> service.paper(ACTOR, 5L));
    }

    @Test
    void publishedPaperRejectsAQuestionLinkedToNoVisibleSection() {
        paperFixture();
        ExamQuestionBO orphan = new ExamQuestionBO();
        orphan.setId(21L);
        orphan.setExamId(5L);
        orphan.setSectionId(11L);
        orphan.setQuestionId(31L);
        orphan.setOrderNo(2);
        orphan.setScore(BigDecimal.TEN);
        ExamQuestionBO valid = new ExamQuestionBO();
        valid.setId(20L);
        valid.setExamId(5L);
        valid.setSectionId(10L);
        valid.setQuestionId(30L);
        valid.setOrderNo(1);
        valid.setScore(BigDecimal.TEN);
        when(taking.questions(ACTOR.tenantId(), 5L)).thenReturn(List.of(valid, orphan));

        assertThrows(ServiceException.class, () -> service.paper(ACTOR, 5L));
    }

    @Test
    void publishedPaperRejectsAQuestionWithNoPositiveScore() {
        paperFixture();
        ExamQuestionBO unscored = new ExamQuestionBO();
        unscored.setId(20L);
        unscored.setExamId(5L);
        unscored.setSectionId(10L);
        unscored.setQuestionId(30L);
        unscored.setOrderNo(1);
        unscored.setScore(BigDecimal.ZERO);
        when(taking.questions(ACTOR.tenantId(), 5L)).thenReturn(List.of(unscored));

        assertThrows(ServiceException.class, () -> service.paper(ACTOR, 5L));
    }

    @Test
    void publishedPaperRejectsMissingSectionsAndRemovedSourceQuestions() {
        paperFixture();
        when(taking.sections(ACTOR.tenantId(), 5L)).thenReturn(List.of());
        assertThrows(ServiceException.class, () -> service.paper(ACTOR, 5L));
        paperFixture();
        when(questions.selectById(ACTOR.tenantId(), 30L)).thenReturn(null);
        assertThrows(ServiceException.class, () -> service.paper(ACTOR, 5L));
        verify(taking, never()).insertAttempt(any(), any());
    }

    @Test
    void publishedPaperRejectsDeactivatedAndNonChoiceQuestionSources() {
        paperFixture();
        QuestionBO source = questions.selectById(ACTOR.tenantId(), 30L);
        source.setStatus(0);
        assertThrows(ServiceException.class, () -> service.paper(ACTOR, 5L));
        source.setStatus(1);
        source.setType("ESSAY");
        assertThrows(ServiceException.class, () -> service.paper(ACTOR, 5L));
    }

    @Test
    void aUserWithoutAnEnrolledStudentCannotReadThePaper() {
        when(exams.selectById(ACTOR.tenantId(), 5L)).thenReturn(exam(1));
        assertThrows(ServiceException.class, () -> service.paper(ACTOR, 5L));
    }

    @Test
    void startCreatesOneAttemptAndReturnsAnAbsoluteDeadline() {
        paperFixture();
        when(students.lockForStudentWrite(ACTOR.tenantId(), 4L)).thenReturn(true);
        when(taking.insertAttempt(any(), any()))
                .thenAnswer(invocation -> {
                    ExamTakingRepository.Attempt draft = invocation.getArgument(1);
                    return new ExamTakingRepository.Attempt(
                            99L, draft.examId(), draft.studentId(), draft.status(),
                            draft.startedAt(), draft.deadlineAt(), null, null, draft.totalScore());
                });

        var paper = service.start(ACTOR, 5L);

        assertEquals(99L, paper.attemptId());
        assertFalse(paper.deadlineAt() == null);
        verify(taking).insertAttemptQuestion(any(), any());
    }

    @Test
    void startLimitsTheDeadlineToTheExamWindowEnd() {
        paperFixture();
        ExamBO endingSoon = exam(1);
        endingSoon.setEndTime(LocalDateTime.now().plusMinutes(5));
        when(exams.selectById(ACTOR.tenantId(), 5L)).thenReturn(endingSoon);
        when(students.lockForStudentWrite(ACTOR.tenantId(), 4L)).thenReturn(true);
        when(taking.insertAttempt(any(), any())).thenAnswer(invocation -> {
            ExamTakingRepository.Attempt draft = invocation.getArgument(1);
            return new ExamTakingRepository.Attempt(99L, draft.examId(), draft.studentId(),
                    draft.status(), draft.startedAt(), draft.deadlineAt(), null, null,
                    draft.totalScore());
        });

        var started = service.start(ACTOR, 5L);

        assertEquals(endingSoon.getEndTime().atZone(java.time.ZoneId.systemDefault()).toInstant(),
                started.deadlineAt());
    }

    @Test
    void startRejectsAnUnavailableStudentBeforeCreatingAnAttempt() {
        paperFixture();
        assertThrows(ServiceException.class, () -> service.start(ACTOR, 5L));
        verify(taking, never()).insertAttempt(any(), any());
    }

    @Test
    void startRejectsAnAnswerlessSourceBeforeSavingItsSnapshot() {
        paperFixture();
        when(students.lockForStudentWrite(ACTOR.tenantId(), 4L)).thenReturn(true);
        QuestionBO answerless = new QuestionBO();
        answerless.setId(30L);
        answerless.setTitle("2 + 2?");
        answerless.setType("CHOICE");
        answerless.setPurpose("EXAM_PRIVATE");
        answerless.setStatus(1);
        answerless.setOptions("[\"3\",\"4\"]");
        when(questions.selectById(ACTOR.tenantId(), 30L)).thenReturn(answerless);
        when(taking.insertAttempt(any(), any())).thenReturn(new ExamTakingRepository.Attempt(
                99L, 5L, 4L, "STARTED", LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(30), null, null, BigDecimal.TEN));

        assertThrows(ServiceException.class, () -> service.start(ACTOR, 5L));
        verify(taking, never()).insertAttemptQuestion(any(), any());
    }

    @Test
    void anExistingAttemptCannotBeReopenedAfterItsExamOrStudentGradeDisappears() {
        when(students.selectByUserId(ACTOR.tenantId(), 37L)).thenReturn(student());
        when(students.lockForStudentWrite(ACTOR.tenantId(), 4L)).thenReturn(true);
        when(taking.findAttemptByExamStudent(ACTOR.tenantId(), 5L, 4L))
                .thenReturn(new ExamTakingRepository.Attempt(99L, 5L, 4L, "STARTED",
                        LocalDateTime.now().minusMinutes(1),
                        LocalDateTime.now().plusMinutes(20), null, null, BigDecimal.TEN));
        assertThrows(ServiceException.class, () -> service.start(ACTOR, 5L));
        ExamBO changedGrade = exam(1);
        changedGrade.setGrade(8);
        when(exams.selectById(ACTOR.tenantId(), 5L)).thenReturn(changedGrade);
        assertThrows(ServiceException.class, () -> service.start(ACTOR, 5L));
        verify(taking, never()).insertAttempt(any(), any());
    }

    @Test
    void malformedExistingAttemptCannotOpenOrCreateANewPaper() {
        when(students.selectByUserId(ACTOR.tenantId(), 37L)).thenReturn(student());
        when(students.lockForStudentWrite(ACTOR.tenantId(), 4L)).thenReturn(true);
        when(exams.selectById(ACTOR.tenantId(), 5L)).thenReturn(exam(1));
        when(taking.findAttemptByExamStudent(ACTOR.tenantId(), 5L, 4L))
                .thenReturn(new ExamTakingRepository.Attempt(99L, 5L, 4L, "STARTED",
                        LocalDateTime.now().minusMinutes(1), null,
                        null, null, BigDecimal.TEN));

        assertThrows(ServiceException.class, () -> service.start(ACTOR, 5L));
        verify(taking, never()).insertAttempt(any(), any());
    }

    @Test
    void invalidChoiceOptionsCannotProduceMisleadingExamPaperChoices() {
        assertEquals(List.of(), ExamTakingService.options(null));
        assertEquals(List.of(), ExamTakingService.options(" "));
        assertThrows(ServiceException.class, () -> ExamTakingService.options("not json"));
        assertThrows(ServiceException.class, () -> ExamTakingService.options("[\"only one\"]"));
        assertThrows(ServiceException.class,
                () -> ExamTakingService.options("[\"A\",null]"));
        assertThrows(ServiceException.class,
                () -> ExamTakingService.options("[\"A\",\" \"]"));
    }

    @Test
    void pendingAttemptWithoutDeadlineCannotBeSubmittedOrReadAsDraft() {
        when(students.selectByUserId(ACTOR.tenantId(), 37L)).thenReturn(student());
        when(taking.findAttemptById(ACTOR.tenantId(), 99L)).thenReturn(
                new ExamTakingRepository.Attempt(99L, 5L, 4L, "STARTED",
                        LocalDateTime.now().minusMinutes(1), null,
                        null, null, BigDecimal.TEN));

        assertThrows(ServiceException.class, () -> service.submit(ACTOR, 99L,
                new SubmitExamRequest(List.of())));
        assertThrows(ServiceException.class, () -> service.draft(ACTOR, 99L));
        assertThrows(ServiceException.class, () -> service.saveDraft(ACTOR, 99L,
                new SubmitExamRequest.Answer(30L, "A")));
        verify(taking, never()).saveDraftAnswer(any(), any(), any());
    }

    @Test
    void readingExpiredResultSettlesOnlyTheCurrentStudentsAttempt() {
        startedAttempt(LocalDateTime.now().minusSeconds(1));
        when(taking.claimAttempt(any(), any(), any(), any(), anyBoolean())).thenReturn(1);
        when(taking.finishAttempt(any(), any(), any(), any(), any())).thenReturn(1);

        var result = service.result(ACTOR, 99L);

        assertEquals("EXPIRED", result.status());
        assertEquals(BigDecimal.ZERO, result.score());
        verify(taking).finishAttempt(org.mockito.ArgumentMatchers.eq(ACTOR.tenantId()),
                org.mockito.ArgumentMatchers.eq(99L),
                org.mockito.ArgumentMatchers.eq(BigDecimal.ZERO), any(),
                org.mockito.ArgumentMatchers.eq("EXPIRED"));
    }

    @Test
    void lostSubmissionRaceCannotReturnAnUnfinishedOrMissingAttempt() {
        var started = new ExamTakingRepository.Attempt(99L, 5L, 4L, "STARTED",
                LocalDateTime.now().minusMinutes(2), LocalDateTime.now().plusMinutes(20),
                null, null, BigDecimal.TEN);
        when(students.selectByUserId(ACTOR.tenantId(), 37L)).thenReturn(student());
        when(taking.findAttemptById(ACTOR.tenantId(), 99L))
                .thenReturn(started, started);
        assertThrows(ServiceException.class, () -> service.submit(ACTOR, 99L,
                new SubmitExamRequest(List.of())));
        when(taking.findAttemptById(ACTOR.tenantId(), 99L))
                .thenReturn(started, (ExamTakingRepository.Attempt) null);
        assertThrows(ServiceException.class, () -> service.submit(ACTOR, 99L,
                new SubmitExamRequest(List.of())));
        verify(taking, never()).saveAnswer(any(), any(), any(), any());
    }

    @Test
    void submissionScoresTheSnapshotAndCanBeRetrievedByItsOwner() {
        when(students.selectByUserId(ACTOR.tenantId(), 37L)).thenReturn(student());
        var attempt = new ExamTakingRepository.Attempt(
                99L, 5L, 4L, "STARTED", LocalDateTime.now().minusMinutes(2),
                LocalDateTime.now().plusMinutes(20), null, null, BigDecimal.TEN);
        when(taking.findAttemptById(ACTOR.tenantId(), 99L)).thenReturn(attempt);
        when(taking.attemptQuestions(ACTOR.tenantId(), 99L))
                .thenReturn(List.of(new ExamTakingRepository.AttemptQuestion(
                        100L, 99L, 20L, 30L, 10L, "选择题", 1, 1, "2 + 2?", "CHOICE",
                        "[\"A. 3\",\"B. 4\"]", "B", "Add the terms", BigDecimal.TEN,
                        null, null)));
        when(taking.claimAttempt(any(), any(), any(), any(), anyBoolean())).thenReturn(1);
        when(taking.finishAttempt(any(), any(), any(), any(), any())).thenReturn(1);

        var result = service.submit(
                ACTOR, 99L, new SubmitExamRequest(List.of(new SubmitExamRequest.Answer(30L, "B"))));

        assertEquals("SUBMITTED", result.status());
        assertEquals(0, BigDecimal.TEN.compareTo(result.score()));
        verify(taking).saveAnswer(ACTOR.tenantId(), 100L, "B", BigDecimal.TEN);
    }

    @Test
    void anotherStudentCannotReadOrSubmitAnAttempt() {
        when(students.selectByUserId(ACTOR.tenantId(), 37L)).thenReturn(student());
        when(taking.findAttemptById(ACTOR.tenantId(), 99L))
                .thenReturn(new ExamTakingRepository.Attempt(
                        99L, 5L, 44L, "SUBMITTED", LocalDateTime.now().minusHours(1),
                        LocalDateTime.now().plusHours(1), LocalDateTime.now(), BigDecimal.ZERO,
                        BigDecimal.TEN));

        assertThrows(ServiceException.class, () -> service.result(ACTOR, 99L));
        assertThrows(ServiceException.class, () -> service.submit(ACTOR, 99L,
                new SubmitExamRequest(List.of())));
    }

    @Test
    void unknownAndDuplicateQuestionIdsAreRejectedBeforeSavingAnswers() {
        startedAttempt(LocalDateTime.now().plusMinutes(20));

        assertThrows(ServiceException.class, () -> service.submit(ACTOR, 99L,
                new SubmitExamRequest(List.of(new SubmitExamRequest.Answer(999L, "B")))));
        assertThrows(ServiceException.class, () -> service.submit(ACTOR, 99L,
                new SubmitExamRequest(List.of(
                        new SubmitExamRequest.Answer(30L, "B"),
                        new SubmitExamRequest.Answer(30L, "A")))));
        org.mockito.Mockito.verify(taking, org.mockito.Mockito.never())
                .saveAnswer(any(), any(), any(), any());
    }

    @Test
    void submissionRejectsAChoiceNotPresentInTheSnapshot() {
        startedAttempt(LocalDateTime.now().plusMinutes(20));
        when(taking.claimAttempt(any(), any(), any(), any(), anyBoolean())).thenReturn(1);

        assertThrows(ServiceException.class, () -> service.submit(ACTOR, 99L,
                new SubmitExamRequest(List.of(new SubmitExamRequest.Answer(30L, "C")))));
        verify(taking, never()).saveAnswer(any(), any(), any(), any());
    }

    @Test
    void expiredAttemptFinalizesSavedDraftAndRejectsLateAnswers() {
        startedAttempt(LocalDateTime.now().minusSeconds(1));
        when(taking.claimAttempt(any(), any(), any(), any(), anyBoolean())).thenReturn(1);
        when(taking.finishAttempt(any(), any(), any(), any(), any())).thenReturn(1);

        var result = service.submit(ACTOR, 99L,
                new SubmitExamRequest(List.of(new SubmitExamRequest.Answer(30L, "B"))));
        assertEquals("EXPIRED", result.status());
        assertEquals(BigDecimal.ZERO, result.score());
        verify(taking).saveAnswer(ACTOR.tenantId(), 100L, "", BigDecimal.ZERO);
    }

    @Test
    void historyFinalizesExpiredAttemptAndShowsSavedDraftScore() {
        var expired = new ExamTakingRepository.Attempt(
                99L, 5L, 4L, "STARTED", LocalDateTime.now().minusMinutes(2),
                LocalDateTime.now().minusSeconds(1), null, null, BigDecimal.TEN);
        var settled = new ExamTakingRepository.Attempt(
                99L, 5L, 4L, "EXPIRED", expired.startedAt(), expired.deadlineAt(),
                LocalDateTime.now(), BigDecimal.TEN, BigDecimal.TEN);
        when(students.selectByUserId(ACTOR.tenantId(), 37L)).thenReturn(student());
        when(taking.findAttemptsByStudent(ACTOR.tenantId(), 4L)).thenReturn(List.of(expired));
        when(exams.selectById(ACTOR.tenantId(), 5L)).thenReturn(exam(1));
        when(taking.findAttemptById(ACTOR.tenantId(), 99L)).thenReturn(settled);
        when(taking.attemptQuestions(ACTOR.tenantId(), 99L)).thenReturn(List.of(
                new ExamTakingRepository.AttemptQuestion(
                        100L, 99L, 20L, 30L, 10L, "选择题", 1, 1, "2 + 2?", "CHOICE",
                        "[\"A. 3\",\"B. 4\"]", "B", "Add the terms", BigDecimal.TEN,
                        "B", null)));
        when(taking.claimAttempt(any(), any(), any(), any(), anyBoolean())).thenReturn(1);
        when(taking.finishAttempt(any(), any(), any(), any(), any())).thenReturn(1);

        var summary = service.history(ACTOR, 1, 10).getItems().getFirst();

        assertEquals("EXPIRED", summary.attemptStatus());
        assertEquals(BigDecimal.TEN, summary.score());
        verify(taking).saveAnswer(ACTOR.tenantId(), 100L, "B", BigDecimal.TEN);
    }

    @Test
    void backgroundSettlementScoresDraftAndSkipsAnAlreadyCompletedAttempt() {
        var expired = new ExamTakingRepository.Attempt(
                99L, 5L, 4L, "STARTED", LocalDateTime.now().minusMinutes(2),
                LocalDateTime.now().minusSeconds(1), null, null, BigDecimal.TEN);
        when(taking.findAttemptById(ACTOR.tenantId(), 99L)).thenReturn(expired);
        when(taking.attemptQuestions(ACTOR.tenantId(), 99L)).thenReturn(List.of(
                new ExamTakingRepository.AttemptQuestion(
                        100L, 99L, 20L, 30L, 10L, "选择题", 1, 1, "2 + 2?", "CHOICE",
                        "[\"A. 3\",\"B. 4\"]", "B", "Add the terms", BigDecimal.TEN,
                        "B", null)));
        when(taking.claimAttempt(any(), any(), any(), any(), anyBoolean())).thenReturn(1);
        when(taking.finishAttempt(any(), any(), any(), any(), any())).thenReturn(1);

        TenantScope.withTenant(ACTOR.tenantId(), () -> {
            service.settleExpiredAttempt(ACTOR.tenantId(), 99L);
            return null;
        });
        verify(taking).saveAnswer(ACTOR.tenantId(), 100L, "B", BigDecimal.TEN);
        verify(taking).finishAttempt(any(), any(), any(), any(),
                org.mockito.ArgumentMatchers.eq("EXPIRED"));

        when(taking.findAttemptById(ACTOR.tenantId(), 99L)).thenReturn(
                new ExamTakingRepository.Attempt(
                        99L, 5L, 4L, "EXPIRED", expired.startedAt(), expired.deadlineAt(),
                        LocalDateTime.now(), BigDecimal.TEN, BigDecimal.TEN));
        TenantScope.withTenant(ACTOR.tenantId(), () -> {
            service.settleExpiredAttempt(ACTOR.tenantId(), 99L);
            return null;
        });
        org.mockito.Mockito.verify(taking, org.mockito.Mockito.times(1))
                .saveAnswer(ACTOR.tenantId(), 100L, "B", BigDecimal.TEN);
    }

    @Test
    void draftSaveRequiresAnOwnedActiveAttemptAndReturnsPersistedAnswers() {
        startedAttempt(LocalDateTime.now().plusMinutes(20));
        when(taking.claimAttempt(any(), any(), any(), any(), anyBoolean())).thenReturn(1);
        service.saveDraft(ACTOR, 99L, new SubmitExamRequest.Answer(30L, "B"));
        verify(taking).saveDraftAnswer(ACTOR.tenantId(), 100L, "B");
    }

    @Test
    void repeatedSubmissionReturnsTheSavedResultWithoutRescoring() {
        when(students.selectByUserId(ACTOR.tenantId(), 37L)).thenReturn(student());
        when(taking.findAttemptById(ACTOR.tenantId(), 99L))
                .thenReturn(new ExamTakingRepository.Attempt(
                        99L, 5L, 4L, "SUBMITTED", LocalDateTime.now().minusMinutes(2),
                        LocalDateTime.now().plusMinutes(20), LocalDateTime.now(),
                        BigDecimal.TEN, BigDecimal.TEN));
        when(taking.attemptQuestions(ACTOR.tenantId(), 99L))
                .thenReturn(List.of(new ExamTakingRepository.AttemptQuestion(
                        100L, 99L, 20L, 30L, 10L, "选择题", 1, 1, "2 + 2?", "CHOICE",
                        "[\"A. 3\",\"B. 4\"]", "B", "Add the terms", BigDecimal.TEN,
                        "B", BigDecimal.TEN)));

        var result = service.submit(ACTOR, 99L, new SubmitExamRequest(List.of()));

        assertEquals("SUBMITTED", result.status());
        assertEquals(0, BigDecimal.TEN.compareTo(result.score()));
        org.mockito.Mockito.verify(taking, org.mockito.Mockito.never())
                .saveAnswer(any(), any(), any(), any());
    }

    @Test
    void submittingAnUnansweredQuestionPreservesItsConfirmedDraft() {
        when(students.selectByUserId(ACTOR.tenantId(), 37L)).thenReturn(student());
        when(taking.findAttemptById(ACTOR.tenantId(), 99L))
                .thenReturn(new ExamTakingRepository.Attempt(
                        99L, 5L, 4L, "STARTED", LocalDateTime.now().minusMinutes(2),
                        LocalDateTime.now().plusMinutes(20), null, null, BigDecimal.TEN));
        when(taking.attemptQuestions(ACTOR.tenantId(), 99L))
                .thenReturn(List.of(new ExamTakingRepository.AttemptQuestion(
                        100L, 99L, 20L, 30L, 10L, "选择题", 1, 1, "2 + 2?", "CHOICE",
                        "[\"A. 3\",\"B. 4\"]", "B", "Add the terms", BigDecimal.TEN,
                        "B", null)));
        when(taking.claimAttempt(any(), any(), any(), any(), anyBoolean())).thenReturn(1);
        when(taking.finishAttempt(any(), any(), any(), any(), any())).thenReturn(1);

        var result = service.submit(ACTOR, 99L,
                new SubmitExamRequest(List.of(new SubmitExamRequest.Answer(30L, ""))));

        assertEquals(BigDecimal.TEN, result.score());
        verify(taking).saveAnswer(ACTOR.tenantId(), 100L, "B", BigDecimal.TEN);
    }

    @Test
    void explicitAnswerAtSubmissionOverridesAnOlderDraft() {
        when(students.selectByUserId(ACTOR.tenantId(), 37L)).thenReturn(student());
        when(taking.findAttemptById(ACTOR.tenantId(), 99L))
                .thenReturn(new ExamTakingRepository.Attempt(
                        99L, 5L, 4L, "STARTED", LocalDateTime.now().minusMinutes(2),
                        LocalDateTime.now().plusMinutes(20), null, null, BigDecimal.TEN));
        when(taking.attemptQuestions(ACTOR.tenantId(), 99L))
                .thenReturn(List.of(new ExamTakingRepository.AttemptQuestion(
                        100L, 99L, 20L, 30L, 10L, "选择题", 1, 1, "2 + 2?", "CHOICE",
                        "[\"A. 3\",\"B. 4\"]", "B", "Add the terms", BigDecimal.TEN,
                        "B", null)));
        when(taking.claimAttempt(any(), any(), any(), any(), anyBoolean())).thenReturn(1);
        when(taking.finishAttempt(any(), any(), any(), any(), any())).thenReturn(1);

        var result = service.submit(ACTOR, 99L,
                new SubmitExamRequest(List.of(new SubmitExamRequest.Answer(30L, "A"))));

        assertEquals(BigDecimal.ZERO, result.score());
        verify(taking).saveAnswer(ACTOR.tenantId(), 100L, "A", BigDecimal.ZERO);
    }

    @Test
    void availableSummaryShowsOnlyTheCurrentStudentsInProgressAttempt() {
        when(students.selectByUserId(ACTOR.tenantId(), 37L)).thenReturn(student());
        when(exams.selectAvailable(org.mockito.ArgumentMatchers.eq(ACTOR.tenantId()),
                org.mockito.ArgumentMatchers.eq(7), org.mockito.ArgumentMatchers.eq("MATH"),
                any(), org.mockito.ArgumentMatchers.eq(1), org.mockito.ArgumentMatchers.eq(10)))
                .thenReturn(new PageData<>(List.of(exam(1)), 1));
        when(taking.findAttemptByExamStudent(ACTOR.tenantId(), 5L, 4L)).thenReturn(
                new ExamTakingRepository.Attempt(99L, 5L, 4L, "STARTED",
                        LocalDateTime.now().minusMinutes(2),
                        LocalDateTime.now().plusMinutes(20), null, null, BigDecimal.TEN));

        var page = service.availableSummaries(ACTOR, "MATH", 1, 10);

        assertEquals(1, page.getTotal());
        assertEquals("IN_PROGRESS", page.getItems().getFirst().attemptStatus());
        assertEquals(99L, page.getItems().getFirst().attemptId());
        assertThrows(ServiceException.class,
                () -> service.availableSummaries(ACTOR, "MATH", 1, 101));
    }

    @Test
    void restartingAnAttemptUsesItsSavedSnapshotAndDoesNotCreateAnotherAttempt() {
        when(students.selectByUserId(ACTOR.tenantId(), 37L)).thenReturn(student());
        when(students.lockForStudentWrite(ACTOR.tenantId(), 4L)).thenReturn(true);
        when(exams.selectById(ACTOR.tenantId(), 5L)).thenReturn(exam(1));
        when(taking.findAttemptByExamStudent(ACTOR.tenantId(), 5L, 4L)).thenReturn(
                new ExamTakingRepository.Attempt(99L, 5L, 4L, "STARTED",
                        LocalDateTime.now().minusMinutes(2),
                        LocalDateTime.now().plusMinutes(20), null, null, BigDecimal.TEN));
        when(taking.attemptQuestions(ACTOR.tenantId(), 99L)).thenReturn(List.of(
                new ExamTakingRepository.AttemptQuestion(
                        100L, 99L, 20L, 30L, 10L, "选择题", 1, 1, "2 + 2?", "CHOICE",
                        "[\"A. 3\",\"B. 4\"]", "B", "Add the terms", BigDecimal.TEN,
                        "A", null)));

        var reopened = service.start(ACTOR, 5L);

        assertEquals(99L, reopened.attemptId());
        assertEquals("2 + 2?", reopened.sections().getFirst().questions().getFirst().title());
        assertFalse(reopened.toString().contains("Add the terms"));
        verify(taking, never()).insertAttempt(any(), any());
    }

    @Test
    void completedAttemptCanReopenOnlyAnAlreadyClosedPaper() {
        when(students.selectByUserId(ACTOR.tenantId(), 37L)).thenReturn(student());
        when(students.lockForStudentWrite(ACTOR.tenantId(), 4L)).thenReturn(true);
        when(exams.selectById(ACTOR.tenantId(), 5L)).thenReturn(exam(1));
        when(taking.findAttemptByExamStudent(ACTOR.tenantId(), 5L, 4L)).thenReturn(
                new ExamTakingRepository.Attempt(99L, 5L, 4L, "SUBMITTED",
                        LocalDateTime.now().minusHours(1),
                        LocalDateTime.now().minusMinutes(30),
                        LocalDateTime.now().minusMinutes(30), BigDecimal.TEN, BigDecimal.TEN));
        when(taking.attemptQuestions(ACTOR.tenantId(), 99L)).thenReturn(List.of(
                new ExamTakingRepository.AttemptQuestion(
                        100L, 99L, 20L, 30L, 10L, "选择题", 1, 1, "2 + 2?", "CHOICE",
                        "[\"A. 3\",\"B. 4\"]", "B", "Add the terms", BigDecimal.TEN,
                        "B", BigDecimal.TEN)));

        var closed = service.start(ACTOR, 5L);

        assertEquals(99L, closed.attemptId());
        assertEquals(java.time.Instant.EPOCH, closed.deadlineAt());
        assertFalse(closed.toString().contains("Add the terms"));
        verify(taking, never()).insertAttempt(any(), any());
    }

    @Test
    void concurrentSubmissionReturnsThePersistedResultWhenAnotherRequestWins() {
        var started = new ExamTakingRepository.Attempt(
                99L, 5L, 4L, "STARTED", LocalDateTime.now().minusMinutes(2),
                LocalDateTime.now().plusMinutes(20), null, null, BigDecimal.TEN);
        var completed = new ExamTakingRepository.Attempt(
                99L, 5L, 4L, "SUBMITTED", started.startedAt(), started.deadlineAt(),
                LocalDateTime.now(), BigDecimal.TEN, BigDecimal.TEN);
        when(students.selectByUserId(ACTOR.tenantId(), 37L)).thenReturn(student());
        when(taking.findAttemptById(ACTOR.tenantId(), 99L))
                .thenReturn(started, completed);
        when(taking.attemptQuestions(ACTOR.tenantId(), 99L)).thenReturn(List.of(
                new ExamTakingRepository.AttemptQuestion(
                        100L, 99L, 20L, 30L, 10L, "选择题", 1, 1, "2 + 2?", "CHOICE",
                        "[\"A. 3\",\"B. 4\"]", "B", "Add the terms", BigDecimal.TEN,
                        "B", BigDecimal.TEN)));

        var result = service.submit(ACTOR, 99L,
                new SubmitExamRequest(List.of(new SubmitExamRequest.Answer(30L, "B"))));

        assertEquals("SUBMITTED", result.status());
        assertEquals(BigDecimal.TEN, result.score());
        verify(taking, never()).saveAnswer(any(), any(), any(), any());
    }

    @Test
    void draftRejectsAChoiceOutsideTheSnapshottedOptions() {
        startedAttempt(LocalDateTime.now().plusMinutes(20));
        when(taking.claimAttempt(any(), any(), any(), any(), anyBoolean())).thenReturn(1);

        assertThrows(ServiceException.class,
                () -> service.saveDraft(ACTOR, 99L, new SubmitExamRequest.Answer(30L, "C")));
        verify(taking, never()).saveDraftAnswer(any(), any(), any());
    }

    @Test
    void draftReadReturnsSavedChoicesWithoutTheAnswerKey() {
        startedAttempt(LocalDateTime.now().plusMinutes(20));
        when(taking.attemptQuestions(ACTOR.tenantId(), 99L)).thenReturn(List.of(
                new ExamTakingRepository.AttemptQuestion(
                        100L, 99L, 20L, 30L, 10L, "选择题", 1, 1, "2 + 2?", "CHOICE",
                        "[\"A. 3\",\"B. 4\"]", "B", "Add the terms", BigDecimal.TEN,
                        "A", null)));

        assertEquals(java.util.Map.of(30L, "A"), service.draft(ACTOR, 99L));
    }

    @Test
    void resultIsUnavailableUntilTheOwnedAttemptIsSubmitted() {
        startedAttempt(LocalDateTime.now().plusMinutes(20));

        assertThrows(ServiceException.class, () -> service.result(ACTOR, 99L));
    }

    @Test
    void historySkipsDeletedExamsAndPagesRemainingAttempts() {
        when(students.selectByUserId(ACTOR.tenantId(), 37L)).thenReturn(student());
        var deleted = new ExamTakingRepository.Attempt(
                98L, 4L, 4L, "SUBMITTED", LocalDateTime.now().minusHours(1),
                LocalDateTime.now().minusMinutes(30), LocalDateTime.now().minusMinutes(30),
                BigDecimal.ZERO, BigDecimal.TEN);
        var visible = new ExamTakingRepository.Attempt(
                99L, 5L, 4L, "SUBMITTED", LocalDateTime.now().minusHours(1),
                LocalDateTime.now().minusMinutes(30), LocalDateTime.now().minusMinutes(30),
                BigDecimal.TEN, BigDecimal.TEN);
        when(taking.findAttemptsByStudent(ACTOR.tenantId(), 4L))
                .thenReturn(List.of(deleted, visible));
        when(exams.selectById(ACTOR.tenantId(), 5L)).thenReturn(exam(1));

        var page = service.history(ACTOR, 1, 10);

        assertEquals(1, page.getTotal());
        assertEquals(List.of(5L), page.getItems().stream().map(item -> item.examId()).toList());
        assertEquals(List.of(), service.history(ACTOR, 2, 10).getItems());
    }

    @Test
    void historyRejectsInvalidPaginationBeforeReadingStudentAttempts() {
        when(students.selectByUserId(ACTOR.tenantId(), 37L)).thenReturn(student());

        assertThrows(ServiceException.class, () -> service.history(ACTOR, 0, 10));
        assertThrows(ServiceException.class, () -> service.history(ACTOR, 1, 0));
        assertThrows(ServiceException.class, () -> service.history(ACTOR, 1, 101));
        verify(taking, never()).findAttemptsByStudent(any(), any());
    }

    @Test
    void availableExamsAreQueriedByCurrentStudentsTenantAndGrade() {
        when(students.selectByUserId(ACTOR.tenantId(), 37L)).thenReturn(student());
        ExamBO visible = exam(1);
        when(exams.selectAvailable(org.mockito.ArgumentMatchers.eq(ACTOR.tenantId()),
                org.mockito.ArgumentMatchers.eq(7), org.mockito.ArgumentMatchers.eq("MATH"),
                any(), org.mockito.ArgumentMatchers.eq(2),
                org.mockito.ArgumentMatchers.eq(10)))
                .thenReturn(new PageData<>(List.of(visible), 11L));
        ExamResponse response = new ExamResponse(5L, "Algebra", null, "MATH", 7,
                null, 30, 10, 1, null, null);
        try (MockedStatic<MapstructUtils> mapping =
                org.mockito.Mockito.mockStatic(MapstructUtils.class)) {
            mapping.when(() -> MapstructUtils.convert(List.of(visible), ExamResponse.class))
                    .thenReturn(List.of(response));
            var page = service.available(ACTOR, "MATH", 2, 10);
            assertEquals(11L, page.getTotal());
            assertEquals(List.of(5L), page.getItems().stream().map(ExamResponse::id).toList());
        }
        assertThrows(ServiceException.class, () -> service.available(ACTOR, "MATH", 0, 10));
        assertThrows(ServiceException.class, () -> service.available(ACTOR, "MATH", 1, 101));
    }

    @Test
    void submissionWithoutAnswersOrSnapshotCannotFinalizeAnAttempt() {
        startedAttempt(LocalDateTime.now().plusMinutes(20));
        assertThrows(ServiceException.class, () -> service.submit(ACTOR, 99L, null));
        when(taking.claimAttempt(any(), any(), any(), any(), anyBoolean())).thenReturn(1);
        when(taking.attemptQuestions(ACTOR.tenantId(), 99L)).thenReturn(List.of());
        assertThrows(ServiceException.class,
                () -> service.submit(ACTOR, 99L, new SubmitExamRequest(List.of())));
        verify(taking, never()).finishAttempt(any(), any(), any(), any(), any());
    }

    @Test
    void finalizingAttemptRejectsDuplicateNullAndOversizedAnswers() {
        startedAttempt(LocalDateTime.now().plusMinutes(20));
        when(taking.claimAttempt(any(), any(), any(), any(), anyBoolean())).thenReturn(1);
        assertThrows(ServiceException.class, () -> service.submit(ACTOR, 99L,
                new SubmitExamRequest(java.util.Arrays.asList((SubmitExamRequest.Answer) null))));
        assertThrows(ServiceException.class, () -> service.submit(ACTOR, 99L,
                new SubmitExamRequest(List.of(new SubmitExamRequest.Answer(null, "A")))));
        assertThrows(ServiceException.class, () -> service.submit(ACTOR, 99L,
                new SubmitExamRequest(List.of(new SubmitExamRequest.Answer(30L,
                        "A".repeat(10_001))))));
        verify(taking, never()).saveAnswer(any(), any(), any(), any());
    }

    @Test
    void failedFinalWriteReportsStatusChangeInsteadOfClaimingSuccess() {
        startedAttempt(LocalDateTime.now().plusMinutes(20));
        when(taking.claimAttempt(any(), any(), any(), any(), anyBoolean())).thenReturn(1);
        assertThrows(ServiceException.class, () -> service.submit(ACTOR, 99L,
                new SubmitExamRequest(List.of(new SubmitExamRequest.Answer(30L, "B")))));
        verify(taking).saveAnswer(ACTOR.tenantId(), 100L, "B", BigDecimal.TEN);
    }

    @Test
    void invalidDraftQuestionNeverOverwritesAStoredAnswer() {
        startedAttempt(LocalDateTime.now().plusMinutes(20));
        when(taking.claimAttempt(any(), any(), any(), any(), anyBoolean())).thenReturn(1);
        assertThrows(ServiceException.class, () -> service.saveDraft(ACTOR, 99L, null));
        assertThrows(ServiceException.class,
                () -> service.saveDraft(ACTOR, 99L,
                        new SubmitExamRequest.Answer(31L, "A")));
        assertThrows(ServiceException.class,
                () -> service.saveDraft(ACTOR, 99L,
                        new SubmitExamRequest.Answer(30L, "B".repeat(10_001))));
        verify(taking, never()).saveDraftAnswer(any(), any(), any());
    }

    private void startedAttempt(LocalDateTime deadline) {
        when(students.selectByUserId(ACTOR.tenantId(), 37L)).thenReturn(student());
        when(taking.findAttemptById(ACTOR.tenantId(), 99L))
                .thenReturn(new ExamTakingRepository.Attempt(
                        99L, 5L, 4L, "STARTED", LocalDateTime.now().minusMinutes(2),
                        deadline, null, null, BigDecimal.TEN));
        org.mockito.Mockito.lenient().when(taking.attemptQuestions(ACTOR.tenantId(), 99L))
                .thenReturn(List.of(new ExamTakingRepository.AttemptQuestion(
                        100L, 99L, 20L, 30L, 10L, "选择题", 1, 1, "2 + 2?", "CHOICE",
                        "[\"A. 3\",\"B. 4\"]", "B", "Add the terms", BigDecimal.TEN,
                        null, null)));
    }

    private void paperFixture() {
        when(exams.selectById(ACTOR.tenantId(), 5L)).thenReturn(exam(1));
        when(students.selectByUserId(ACTOR.tenantId(), 37L)).thenReturn(student());
        ExamSectionBO section = new ExamSectionBO();
        section.setId(10L);
        section.setExamId(5L);
        section.setName("选择题");
        section.setOrderNo(1);
        ExamQuestionBO link = new ExamQuestionBO();
        link.setId(20L);
        link.setExamId(5L);
        link.setSectionId(10L);
        link.setQuestionId(30L);
        link.setOrderNo(1);
        link.setScore(BigDecimal.TEN);
        QuestionBO question = new QuestionBO();
        question.setId(30L);
        question.setTitle("2 + 2?");
        question.setType("CHOICE");
        question.setPurpose("EXAM_PRIVATE");
        question.setOptions("[\"A. 3\",\"B. 4\"]");
        question.setAnswer("B");
        question.setStatus(1);
        when(taking.sections(ACTOR.tenantId(), 5L)).thenReturn(List.of(section));
        when(taking.questions(ACTOR.tenantId(), 5L)).thenReturn(List.of(link));
        when(questions.selectById(ACTOR.tenantId(), 30L)).thenReturn(question);
    }

    private static ExamBO exam(int status) {
        ExamBO exam = new ExamBO();
        exam.setId(5L);
        exam.setName("Algebra");
        exam.setSubjectCode("MATH");
        exam.setGrade(7);
        exam.setDurationMin(30);
        exam.setTotalScore(10);
        exam.setStatus(status);
        exam.setStartTime(LocalDateTime.now().minusHours(1));
        exam.setEndTime(LocalDateTime.now().plusHours(1));
        return exam;
    }

    private static StudentBO student() {
        StudentBO student = new StudentBO();
        student.setId(4L);
        student.setUserId(37L);
        student.setGrade(7);
        student.setStatus(1);
        return student;
    }
}
