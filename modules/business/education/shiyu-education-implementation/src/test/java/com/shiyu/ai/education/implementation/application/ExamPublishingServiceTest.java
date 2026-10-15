package com.shiyu.ai.education.implementation.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shiyu.ai.common.foundation.exception.ServiceException;
import com.shiyu.ai.education.implementation.domain.model.ExamBO;
import com.shiyu.ai.education.implementation.domain.model.ExamQuestionBO;
import com.shiyu.ai.education.implementation.domain.model.ExamSectionBO;
import com.shiyu.ai.education.implementation.domain.model.QuestionBO;
import com.shiyu.ai.education.implementation.domain.port.repository.ExamRepository;
import com.shiyu.ai.education.implementation.domain.port.repository.ExamTakingRepository;
import com.shiyu.ai.education.implementation.domain.port.repository.QuestionRepository;
import com.shiyu.ai.education.implementation.web.request.ExamCompositionRequest;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

/** 验证保密题目选取及考试发布边界。 */
class ExamPublishingServiceTest {
    private static final ActorContext ACTOR =
            new ActorContext(new TenantId(7), new UserId(9), false);
    private final ExamRepository exams = org.mockito.Mockito.mock(ExamRepository.class);
    private final ExamTakingRepository composition = org.mockito.Mockito.mock(ExamTakingRepository.class);
    private final QuestionRepository questions = org.mockito.Mockito.mock(QuestionRepository.class);
    private final ExamPublishingService service =
            new ExamPublishingService(exams, composition, questions);

    @Test
    void rejectsPublicPracticeQuestionBeforeReplacingPaper() {
        when(exams.selectById(ACTOR.tenantId(), 10L)).thenReturn(draft());
        QuestionBO publicQuestion = privateQuestion();
        publicQuestion.setPurpose("PRACTICE");
        when(questions.selectById(ACTOR.tenantId(), 21L)).thenReturn(publicQuestion);

        assertThrows(ServiceException.class, () -> service.replaceComposition(ACTOR, 10L,
                request(21L, BigDecimal.valueOf(100))));

        verify(composition, never()).deleteComposition(any(), any());
    }

    @Test
    void rejectsAnswerThatIsNotAmongDisplayedOptions() {
        when(exams.selectById(ACTOR.tenantId(), 10L)).thenReturn(draft());
        QuestionBO invalid = privateQuestion();
        invalid.setAnswer("Z");
        when(questions.selectById(ACTOR.tenantId(), 21L)).thenReturn(invalid);

        assertThrows(ServiceException.class, () -> service.replaceComposition(ACTOR, 10L,
                request(21L, BigDecimal.valueOf(100))));
        verify(composition, never()).deleteComposition(any(), any());
    }

    @Test
    void rejectsQuestionWithMoreChoiceOptionsThanCanBeLabeled() {
        when(exams.selectById(ACTOR.tenantId(), 10L)).thenReturn(draft());
        QuestionBO invalid = privateQuestion();
        invalid.setOptions("[" + "\"Option\",".repeat(26) + "\"Option\"]");
        when(questions.selectById(ACTOR.tenantId(), 21L)).thenReturn(invalid);

        assertThrows(ServiceException.class, () -> service.replaceComposition(ACTOR, 10L,
                request(21L, BigDecimal.valueOf(100))));
        verify(composition, never()).deleteComposition(any(), any());
    }

    @Test
    void rejectsSingleOptionChoiceQuestion() {
        when(exams.selectById(ACTOR.tenantId(), 10L)).thenReturn(draft());
        QuestionBO invalid = privateQuestion();
        invalid.setOptions("[\"Only one\"]");
        when(questions.selectById(ACTOR.tenantId(), 21L)).thenReturn(invalid);

        assertThrows(ServiceException.class, () -> service.replaceComposition(ACTOR, 10L,
                request(21L, BigDecimal.valueOf(100))));
        verify(composition, never()).deleteComposition(any(), any());
    }

    @Test
    void rejectsBlankChoiceLabels() {
        when(exams.selectById(ACTOR.tenantId(), 10L)).thenReturn(draft());
        QuestionBO invalid = privateQuestion();
        invalid.setOptions("[\"A. One\",\"  \"]");
        when(questions.selectById(ACTOR.tenantId(), 21L)).thenReturn(invalid);

        assertThrows(ServiceException.class, () -> service.replaceComposition(ACTOR, 10L,
                request(21L, BigDecimal.valueOf(100))));
        verify(composition, never()).deleteComposition(any(), any());
    }

    @Test
    void acceptsOnlyMatchingConfidentialQuestionsAndKeepsSectionOrder() {
        when(exams.selectById(ACTOR.tenantId(), 10L)).thenReturn(draft());
        when(questions.selectById(ACTOR.tenantId(), 21L)).thenReturn(privateQuestion());
        when(questions.selectById(ACTOR.tenantId(), 22L)).thenReturn(privateQuestion());
        when(composition.insertSection(eq(ACTOR.tenantId()), eq(10L), eq("First"),
                eq(1), eq(BigDecimal.valueOf(40)))).thenReturn(31L);
        when(composition.insertSection(eq(ACTOR.tenantId()), eq(10L), eq("Second"),
                eq(2), eq(BigDecimal.valueOf(60)))).thenReturn(32L);
        var request = new ExamCompositionRequest(List.of(
                new ExamCompositionRequest.Section(" First ", List.of(
                        new ExamCompositionRequest.Question(21L, BigDecimal.valueOf(40)))),
                new ExamCompositionRequest.Section("Second", List.of(
                        new ExamCompositionRequest.Question(22L, BigDecimal.valueOf(60))))));

        service.replaceComposition(ACTOR, 10L, request);

        verify(composition).deleteComposition(ACTOR.tenantId(), 10L);
        verify(composition).insertQuestionLink(ACTOR.tenantId(), 10L, 31L,
                21L, 1, BigDecimal.valueOf(40));
        verify(composition).insertQuestionLink(ACTOR.tenantId(), 10L, 32L,
                22L, 1, BigDecimal.valueOf(60));
    }

    @Test
    void rejectsDuplicateQuestionAndWrongTotalBeforeWriting() {
        when(exams.selectById(ACTOR.tenantId(), 10L)).thenReturn(draft());
        when(questions.selectById(ACTOR.tenantId(), 21L)).thenReturn(privateQuestion());
        var duplicated = new ExamCompositionRequest(List.of(
                new ExamCompositionRequest.Section("One", List.of(
                        new ExamCompositionRequest.Question(21L, BigDecimal.valueOf(50)),
                        new ExamCompositionRequest.Question(21L, BigDecimal.valueOf(50))))));

        assertThrows(ServiceException.class,
                () -> service.replaceComposition(ACTOR, 10L, duplicated));
        assertThrows(ServiceException.class,
                () -> service.replaceComposition(ACTOR, 10L,
                        request(21L, BigDecimal.valueOf(90))));
        verify(composition, never()).deleteComposition(any(), any());
    }

    @Test
    void publicationRechecksConfidentialityAndQuestionTotals() {
        ExamBO exam = draft();
        when(exams.selectById(ACTOR.tenantId(), 10L)).thenReturn(exam);
        ExamSectionBO section = new ExamSectionBO();
        section.setId(31L);
        ExamQuestionBO link = new ExamQuestionBO();
        link.setSectionId(31L);
        link.setQuestionId(21L);
        link.setScore(BigDecimal.valueOf(100));
        when(composition.sections(ACTOR.tenantId(), 10L)).thenReturn(List.of(section));
        when(composition.questions(ACTOR.tenantId(), 10L)).thenReturn(List.of(link));
        QuestionBO source = privateQuestion();
        source.setPurpose("PRACTICE");
        when(questions.selectById(ACTOR.tenantId(), 21L)).thenReturn(source);

        assertThrows(ServiceException.class, () -> service.publish(ACTOR, 10L));
        verify(exams, never()).update(any(), any());

        source.setPurpose("EXAM_PRIVATE");
        source.setAnswer("Z");
        assertThrows(ServiceException.class, () -> service.publish(ACTOR, 10L));
        verify(exams, never()).update(any(), any());

        source.setAnswer("A");
        link.setScore(BigDecimal.valueOf(90));
        assertThrows(ServiceException.class, () -> service.publish(ACTOR, 10L));
        verify(exams, never()).update(any(), any());

        link.setScore(BigDecimal.valueOf(100));
        service.publish(ACTOR, 10L);
        assertEquals(1, exam.getStatus());
        verify(exams).update(ACTOR.tenantId(), exam);
    }

    @Test
    void rejectsExpiredDraftBeforePublishing() {
        ExamBO exam = draft();
        exam.setEndTime(LocalDateTime.now().minusMinutes(1));
        when(exams.selectById(ACTOR.tenantId(), 10L)).thenReturn(exam);

        assertThrows(ServiceException.class, () -> service.publish(ACTOR, 10L));
        verify(composition, never()).sections(any(), any());
    }

    @Test
    void publicationRejectsNonPositiveQuestionScoresEvenIfTheTotalBalances() {
        when(exams.selectById(ACTOR.tenantId(), 10L)).thenReturn(draft());
        ExamSectionBO section = new ExamSectionBO();
        section.setId(31L);
        ExamQuestionBO overvalued = new ExamQuestionBO();
        overvalued.setSectionId(31L);
        overvalued.setQuestionId(21L);
        overvalued.setScore(BigDecimal.valueOf(110));
        ExamQuestionBO negative = new ExamQuestionBO();
        negative.setSectionId(31L);
        negative.setQuestionId(22L);
        negative.setScore(BigDecimal.valueOf(-10));
        when(composition.sections(ACTOR.tenantId(), 10L)).thenReturn(List.of(section));
        when(composition.questions(ACTOR.tenantId(), 10L))
                .thenReturn(List.of(overvalued, negative));
        when(questions.selectById(ACTOR.tenantId(), 21L)).thenReturn(privateQuestion());
        when(questions.selectById(ACTOR.tenantId(), 22L)).thenReturn(privateQuestion());

        assertThrows(ServiceException.class, () -> service.publish(ACTOR, 10L));
        verify(exams, never()).update(any(), any());
    }

    @Test
    void publicationRejectsMissingQuestionScoreAsAnInvalidPaper() {
        when(exams.selectById(ACTOR.tenantId(), 10L)).thenReturn(draft());
        ExamSectionBO section = new ExamSectionBO();
        section.setId(31L);
        ExamQuestionBO unscored = new ExamQuestionBO();
        unscored.setSectionId(31L);
        unscored.setQuestionId(21L);
        when(composition.sections(ACTOR.tenantId(), 10L)).thenReturn(List.of(section));
        when(composition.questions(ACTOR.tenantId(), 10L)).thenReturn(List.of(unscored));

        assertThrows(ServiceException.class, () -> service.publish(ACTOR, 10L));
        verify(exams, never()).update(any(), any());
    }

    @Test
    void invalidSourceQuestionCannotReplaceAnExistingExamComposition() {
        List<Consumer<QuestionBO>> invalidSources = List.of(
                source -> source.setStatus(0),
                source -> source.setSubjectCode("ENGLISH"),
                source -> source.setGrade(6),
                source -> source.setType("ESSAY"),
                source -> source.setAnswer(null),
                source -> source.setAnswer(" "),
                source -> source.setAnswer("AA"),
                source -> source.setOptions(null));
        when(exams.selectById(ACTOR.tenantId(), 10L)).thenReturn(draft());
        for (Consumer<QuestionBO> invalidate : invalidSources) {
            QuestionBO source = privateQuestion();
            invalidate.accept(source);
            when(questions.selectById(ACTOR.tenantId(), 21L)).thenReturn(source);
            assertThrows(ServiceException.class, () -> service.replaceComposition(ACTOR,
                    10L, request(21L, BigDecimal.valueOf(100))));
        }
        verify(composition, never()).deleteComposition(any(), any());
    }

    @Test
    void incompleteCompositionNeverDeletesAnExistingPaper() {
        when(exams.selectById(ACTOR.tenantId(), 10L)).thenReturn(draft());
        assertThrows(ServiceException.class,
                () -> service.replaceComposition(ACTOR, 10L, null));
        assertThrows(ServiceException.class,
                () -> service.replaceComposition(ACTOR, 10L,
                        new ExamCompositionRequest(List.of())));
        assertThrows(ServiceException.class,
                () -> service.replaceComposition(ACTOR, 10L,
                        new ExamCompositionRequest(List.of(new ExamCompositionRequest.Section(
                                "No questions", List.of())))));
        assertThrows(ServiceException.class,
                () -> service.replaceComposition(ACTOR, 10L,
                        new ExamCompositionRequest(List.of(new ExamCompositionRequest.Section(
                                "One", List.of(new ExamCompositionRequest.Question(
                                        21L, BigDecimal.ZERO)))))));
        verify(composition, never()).deleteComposition(any(), any());
    }

    @Test
    void publicationRequiresCompleteLinksAndAValidWindow() {
        ExamBO draft = draft();
        when(exams.selectById(ACTOR.tenantId(), 10L)).thenReturn(draft);
        draft.setDurationMin(0);
        assertThrows(ServiceException.class, () -> service.publish(ACTOR, 10L));
        draft.setDurationMin(60);
        draft.setStartTime(draft.getEndTime());
        assertThrows(ServiceException.class, () -> service.publish(ACTOR, 10L));
        draft.setStartTime(LocalDateTime.now().minusMinutes(5));
        assertThrows(ServiceException.class, () -> service.publish(ACTOR, 10L));

        ExamSectionBO section = new ExamSectionBO();
        section.setId(31L);
        ExamQuestionBO orphan = new ExamQuestionBO();
        orphan.setSectionId(32L);
        orphan.setQuestionId(21L);
        orphan.setScore(BigDecimal.valueOf(100));
        when(composition.sections(ACTOR.tenantId(), 10L)).thenReturn(List.of(section));
        when(composition.questions(ACTOR.tenantId(), 10L)).thenReturn(List.of(orphan));
        assertThrows(ServiceException.class, () -> service.publish(ACTOR, 10L));
        verify(exams, never()).update(any(), any());
    }

    @Test
    void activeOrMissingExamCannotHaveItsPaperReplaced() {
        assertThrows(ServiceException.class,
                () -> service.replaceComposition(ACTOR, 10L,
                        request(21L, BigDecimal.valueOf(100))));
        ExamBO active = draft();
        active.setStatus(1);
        when(exams.selectById(ACTOR.tenantId(), 10L)).thenReturn(active);
        assertThrows(ServiceException.class,
                () -> service.replaceComposition(ACTOR, 10L,
                        request(21L, BigDecimal.valueOf(100))));
        verify(composition, never()).deleteComposition(any(), any());
    }

    @Test
    void publicationRechecksCurrentQuestionSubjectGradeAndAnswer() {
        when(exams.selectById(ACTOR.tenantId(), 10L)).thenReturn(draft());
        ExamSectionBO section = new ExamSectionBO();
        section.setId(31L);
        ExamQuestionBO link = new ExamQuestionBO();
        link.setSectionId(31L);
        link.setQuestionId(21L);
        link.setScore(BigDecimal.valueOf(100));
        when(composition.sections(ACTOR.tenantId(), 10L)).thenReturn(List.of(section));
        when(composition.questions(ACTOR.tenantId(), 10L)).thenReturn(List.of(link));
        List<Consumer<QuestionBO>> invalidSources = List.of(
                source -> source.setStatus(0),
                source -> source.setType("ESSAY"),
                source -> source.setSubjectCode("ENGLISH"),
                source -> source.setGrade(6),
                source -> source.setAnswer(null),
                source -> source.setAnswer("AA"),
                source -> source.setOptions("[\"Only one\"]"));
        for (Consumer<QuestionBO> invalidate : invalidSources) {
            QuestionBO source = privateQuestion();
            invalidate.accept(source);
            when(questions.selectById(ACTOR.tenantId(), 21L)).thenReturn(source);
            assertThrows(ServiceException.class, () -> service.publish(ACTOR, 10L));
        }
        verify(exams, never()).update(any(), any());
    }

    @Test
    void incompleteSectionAndQuestionPayloadsCannotDeleteExistingComposition() {
        when(exams.selectById(ACTOR.tenantId(), 10L)).thenReturn(draft());
        List<ExamCompositionRequest> invalid = List.of(
                new ExamCompositionRequest(java.util.Arrays.asList(
                        (ExamCompositionRequest.Section) null)),
                new ExamCompositionRequest(List.of(
                        new ExamCompositionRequest.Section(" ", List.of(
                                new ExamCompositionRequest.Question(21L, BigDecimal.valueOf(100)))))),
                new ExamCompositionRequest(List.of(
                        new ExamCompositionRequest.Section("One", null))),
                new ExamCompositionRequest(List.of(
                        new ExamCompositionRequest.Section("One", java.util.Arrays.asList(
                                (ExamCompositionRequest.Question) null)))),
                request(null, BigDecimal.valueOf(100)),
                request(21L, null),
                request(21L, BigDecimal.valueOf(-1)));
        for (ExamCompositionRequest payload : invalid) {
            assertThrows(ServiceException.class,
                    () -> service.replaceComposition(ACTOR, 10L, payload));
        }
        verify(composition, never()).deleteComposition(any(), any());
    }

    @Test
    void emptyStoredCompositionCannotBePublished() {
        when(exams.selectById(ACTOR.tenantId(), 10L)).thenReturn(draft());
        assertThrows(ServiceException.class, () -> service.publish(ACTOR, 10L));
        ExamSectionBO section = new ExamSectionBO();
        section.setId(31L);
        when(composition.sections(ACTOR.tenantId(), 10L)).thenReturn(List.of(section));
        assertThrows(ServiceException.class, () -> service.publish(ACTOR, 10L));
        verify(exams, never()).update(any(), any());
    }

    private static ExamCompositionRequest request(Long questionId, BigDecimal score) {
        return new ExamCompositionRequest(List.of(new ExamCompositionRequest.Section(
                "One", List.of(new ExamCompositionRequest.Question(questionId, score)))));
    }

    private static ExamBO draft() {
        ExamBO exam = new ExamBO();
        exam.setId(10L);
        exam.setStatus(0);
        exam.setSubjectCode("MATH");
        exam.setGrade(5);
        exam.setTotalScore(100);
        exam.setDurationMin(60);
        exam.setStartTime(LocalDateTime.now().minusMinutes(5));
        exam.setEndTime(LocalDateTime.now().plusHours(1));
        return exam;
    }

    private static QuestionBO privateQuestion() {
        QuestionBO question = new QuestionBO();
        question.setStatus(1);
        question.setSubjectCode("MATH");
        question.setGrade(5);
        question.setType("CHOICE");
        question.setPurpose("EXAM_PRIVATE");
        question.setAnswer("A");
        question.setOptions("[\"A. One\",\"B. Two\"]");
        return question;
    }
}
