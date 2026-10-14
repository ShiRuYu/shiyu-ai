package com.shiyu.ai.education.implementation.domain.port.repository;

import com.shiyu.ai.education.implementation.domain.model.ExamQuestionBO;
import com.shiyu.ai.education.implementation.domain.model.ExamSectionBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Tenant-scoped composition and attempt persistence for student exams. */
public interface ExamTakingRepository {
    /** Load ordered active sections for a tenant's exam. */
    List<ExamSectionBO> sections(TenantId tenantId, Long examId);

    /** Load active question links for a tenant's exam. */
    List<ExamQuestionBO> questions(TenantId tenantId, Long examId);

    /** Replace the composition of an unpublished exam. */
    void deleteComposition(TenantId tenantId, Long examId);

    /** Insert an ordered section and return its identifier. */
    Long insertSection(TenantId tenantId, Long examId, String name, int orderNo,
            BigDecimal scorePerQuestion);

    /** Link one confidential question to a section. */
    void insertQuestionLink(TenantId tenantId, Long examId, Long sectionId,
            Long questionId, int orderNo, BigDecimal score);

    /** Find the student's single attempt for this exam. */
    Attempt findAttemptByExamStudent(TenantId tenantId, Long examId, Long studentId);

    /** Find an attempt within the current tenant. */
    Attempt findAttemptById(TenantId tenantId, Long attemptId);

    /** List the student's attempts, including completed and expired exams. */
    List<Attempt> findAttemptsByStudent(TenantId tenantId, Long studentId);

    /** Find a bounded batch of overdue attempts after an optional global attempt ID cursor. */
    List<DueAttempt> findDueAttempts(LocalDateTime now, Long afterAttemptId, int limit);

    /** Create an attempt before snapshotting its questions. */
    Attempt insertAttempt(TenantId tenantId, Attempt attempt);

    /** Store the answer-key snapshot for one attempt question. */
    void insertAttemptQuestion(TenantId tenantId, AttemptQuestion question);

    /** Read the immutable question snapshots and any saved student answers. */
    List<AttemptQuestion> attemptQuestions(TenantId tenantId, Long attemptId);

    /** Lock a live attempt before saving or finalizing; the deadline predicate is enforced in SQL. */
    int claimAttempt(TenantId tenantId, Long attemptId, String targetStatus,
            LocalDateTime now, boolean beforeDeadline);

    /** Save one unscored answer while its owning attempt is locked and active. */
    void saveDraftAnswer(TenantId tenantId, Long attemptQuestionId, String answer);

    /** Store an answer and its server-calculated awarded score. */
    void saveAnswer(TenantId tenantId, Long attemptQuestionId, String answer, BigDecimal score);

    /** Complete a previously claimed attempt exactly once. */
    int finishAttempt(TenantId tenantId, Long attemptId, BigDecimal score,
            LocalDateTime submittedAt, String finalStatus);

    /** Immutable attempt state scoped by tenant, exam, and student. */
    record Attempt(
            Long id,
            Long examId,
            Long studentId,
            String status,
            LocalDateTime startedAt,
            LocalDateTime deadlineAt,
            LocalDateTime submittedAt,
            BigDecimal score,
            BigDecimal totalScore) {}

    /** Tenant and attempt identity for background expiry settlement. */
    record DueAttempt(TenantId tenantId, Long attemptId) {}

    /** Immutable exam question snapshot with the student's current answer. */
    record AttemptQuestion(
            Long id,
            Long attemptId,
            Long examQuestionId,
            Long questionId,
            Long sectionId,
            String sectionName,
            Integer sectionOrderNo,
            Integer orderNo,
            String title,
            String type,
            String optionsJson,
            String correctAnswer,
            String analysis,
            BigDecimal maxScore,
            String answer,
            BigDecimal awardedScore) {}
}
