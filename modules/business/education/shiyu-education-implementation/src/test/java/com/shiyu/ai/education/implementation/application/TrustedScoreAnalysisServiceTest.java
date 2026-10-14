package com.shiyu.ai.education.implementation.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shiyu.ai.common.foundation.exception.ServiceException;
import com.shiyu.ai.education.implementation.domain.BloomTaxonomy;
import com.shiyu.ai.education.implementation.domain.model.QuestionBO;
import com.shiyu.ai.education.implementation.domain.model.StudentBO;
import com.shiyu.ai.education.implementation.domain.model.StudyRecordBO;
import com.shiyu.ai.education.implementation.domain.port.repository.QuestionRepository;
import com.shiyu.ai.education.implementation.domain.port.repository.StudentRepository;
import com.shiyu.ai.education.implementation.domain.port.repository.StudyRecordRepository;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;

/** Ensures client flags cannot substitute for a tenant-owned graded practice fact. */
class TrustedScoreAnalysisServiceTest {
    private final StudentRepository students = mock(StudentRepository.class);
    private final QuestionRepository questions = mock(QuestionRepository.class);
    private final StudyRecordRepository records = mock(StudyRecordRepository.class);
    private final AbilityService abilities = mock(AbilityService.class);
    private final TrustedScoreAnalysisService service = new TrustedScoreAnalysisService(
            students, questions, records, abilities);
    private final ActorContext actor = new ActorContext(new TenantId(17L), new UserId(23L), false);

    @Test
    void appliesARealRecordOnlyOnce() {
        arrange(41L, 0D, "A", "B");
        when(records.claimAnalysis(actor.tenantId(), 41L, 71L)).thenReturn(1, 0);

        assertTrue(service.analyze(actor, 71L).applied());
        assertFalse(service.analyze(actor, 71L).applied());
        verify(abilities).update(actor, 41L, 1001L, BloomTaxonomy.APPLY, 0D);
    }

    @Test
    void foreignStudentRecordNeverChangesAbility() {
        arrange(99L, 100D, "B", "B");
        assertThrows(ServiceException.class, () -> service.analyze(actor, 71L));
        verify(abilities, never()).update(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyDouble());
    }

    @Test
    void forgedScoreInsideARecordIsRejected() {
        arrange(41L, 100D, "A", "B");
        assertThrows(ServiceException.class, () -> service.analyze(actor, 71L));
        verify(records, never()).claimAnalysis(actor.tenantId(), 41L, 71L);
    }

    @Test
    void malformedContextOrUnavailableStudentCannotAnalyzeScores() {
        assertThrows(ServiceException.class, () -> service.analyze(null, 71L));
        assertThrows(ServiceException.class, () -> service.analyze(actor, 0L));
        assertThrows(ServiceException.class, () -> service.analyze(actor, null));
        arrange(41L, 100D, "B", "B");
        StudentBO inactive = new StudentBO();
        inactive.setId(41L);
        inactive.setStatus(0);
        when(students.selectByUserId(actor.tenantId(), 23L)).thenReturn(inactive);
        assertThrows(ServiceException.class, () -> service.analyze(actor, 71L));
        verify(records, never()).claimAnalysis(actor.tenantId(), 41L, 71L);
    }

    @Test
    void untrustedSourceQuestionCannotInfluenceTheAbilityProfile() {
        arrange(41L, 100D, "B", "B");
        QuestionBO noLongerPractice = new QuestionBO();
        noLongerPractice.setPurpose("EXAM_PRIVATE");
        when(questions.selectById(actor.tenantId(), 30L)).thenReturn(noLongerPractice);
        assertThrows(ServiceException.class, () -> service.analyze(actor, 71L));
        when(questions.selectById(actor.tenantId(), 30L)).thenReturn(null);
        assertThrows(ServiceException.class, () -> service.analyze(actor, 71L));
        verify(records, never()).claimAnalysis(actor.tenantId(), 41L, 71L);
    }

    @Test
    void incompleteOrUnscoredRecordCannotInfluenceTheAbilityProfile() {
        arrange(41L, 100D, "B", "B");
        StudyRecordBO record = records.selectById(actor.tenantId(), 71L);
        record.setSubmissionKey(" ");
        assertThrows(ServiceException.class, () -> service.analyze(actor, 71L));
        record.setSubmissionKey("94bd0973-cb05-434f-9113-d5a62845671b");
        record.setKnowledgeId(null);
        assertThrows(ServiceException.class, () -> service.analyze(actor, 71L));
        record.setKnowledgeId(1001L);
        record.setAccuracy(null);
        assertThrows(ServiceException.class, () -> service.analyze(actor, 71L));
        verify(records, never()).claimAnalysis(actor.tenantId(), 41L, 71L);
    }

    @Test
    void invalidAbilityDimensionCannotBeAppliedEvenWhenScoreIsTrusted() {
        arrange(41L, 100D, "B", "B");
        records.selectById(actor.tenantId(), 71L).setAbilityDimension("not-a-dimension");
        assertThrows(ServiceException.class, () -> service.analyze(actor, 71L));
        verify(records, never()).claimAnalysis(actor.tenantId(), 41L, 71L);
    }

    private void arrange(Long studentId, Double score, String selected, String correct) {
        StudentBO student = new StudentBO();
        student.setId(41L);
        student.setStatus(1);
        when(students.selectByUserId(actor.tenantId(), 23L)).thenReturn(student);
        StudyRecordBO record = new StudyRecordBO();
        record.setId(71L);
        record.setStudentId(studentId);
        record.setRecordType("PRACTICE");
        record.setSubmissionKey("94bd0973-cb05-434f-9113-d5a62845671b");
        record.setQuestionId(30L);
        record.setKnowledgeId(1001L);
        record.setSelectedAnswer(selected);
        record.setCorrectAnswer(correct);
        record.setScore(score);
        record.setAccuracy(score / 100D);
        record.setAbilityDimension("apply");
        when(records.selectById(actor.tenantId(), 71L)).thenReturn(record);
        QuestionBO question = new QuestionBO();
        question.setPurpose("PRACTICE");
        when(questions.selectById(actor.tenantId(), 30L)).thenReturn(question);
    }
}
