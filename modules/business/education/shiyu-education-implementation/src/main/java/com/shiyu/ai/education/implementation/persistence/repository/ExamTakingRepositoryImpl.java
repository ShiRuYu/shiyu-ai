package com.shiyu.ai.education.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.foundation.utils.MapstructUtils;
import com.shiyu.ai.common.mybatis.tenant.TenantQueryExecutor;
import com.shiyu.ai.education.implementation.domain.model.ExamQuestionBO;
import com.shiyu.ai.education.implementation.domain.model.ExamSectionBO;
import com.shiyu.ai.education.implementation.domain.port.repository.ExamTakingRepository;
import com.shiyu.ai.education.implementation.persistence.dataobject.ExamAttemptDO;
import com.shiyu.ai.education.implementation.persistence.dataobject.ExamAttemptQuestionDO;
import com.shiyu.ai.education.implementation.persistence.dataobject.ExamQuestionDO;
import com.shiyu.ai.education.implementation.persistence.dataobject.ExamSectionDO;
import com.shiyu.ai.education.implementation.persistence.mapper.ExamAttemptMapper;
import com.shiyu.ai.education.implementation.persistence.mapper.ExamAttemptQuestionMapper;
import com.shiyu.ai.education.implementation.persistence.mapper.ExamQuestionMapper;
import com.shiyu.ai.education.implementation.persistence.mapper.ExamSectionMapper;
import com.shiyu.ai.kernel.context.TenantId;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/** Loads only active composition rows from the current tenant. */
@Component
@RequiredArgsConstructor
public class ExamTakingRepositoryImpl implements ExamTakingRepository {
    private final ExamSectionMapper sections;
    private final ExamQuestionMapper questions;
    private final ExamAttemptMapper attempts;
    private final ExamAttemptQuestionMapper attemptQuestions;

    @Override
    public List<ExamSectionBO> sections(TenantId tenantId, Long examId) {
        return MapstructUtils.convert(
                sections.selectListByQuery(
                        QueryWrapper.create()
                                .eq("tenant_id", tenantId.value())
                                .eq("exam_id", examId)
                                .eq("status", 1)
                                .eq("del_flag", 0)
                                .orderBy("order_no", true)),
                ExamSectionBO.class);
    }

    @Override
    public List<ExamQuestionBO> questions(TenantId tenantId, Long examId) {
        return MapstructUtils.convert(
                questions.selectListByQuery(
                        QueryWrapper.create()
                                .eq("tenant_id", tenantId.value())
                                .eq("exam_id", examId)
                                .eq("status", 1)
                                .eq("del_flag", 0)
                                .orderBy("order_no", true)),
                ExamQuestionBO.class);
    }

    @Override
    public void deleteComposition(TenantId tenantId, Long examId) {
        QueryWrapper scope = QueryWrapper.create().eq("tenant_id", tenantId.value())
                .eq("exam_id", examId);
        questions.deleteByQuery(scope);
        sections.deleteByQuery(QueryWrapper.create().eq("tenant_id", tenantId.value())
                .eq("exam_id", examId));
    }

    @Override
    public Long insertSection(TenantId tenantId, Long examId, String name,
            int orderNo, BigDecimal scorePerQuestion) {
        ExamSectionDO row = new ExamSectionDO();
        row.setTenantId(tenantId.value());
        row.setExamId(examId);
        row.setName(name);
        row.setOrderNo(orderNo);
        row.setScorePerQ(scorePerQuestion);
        row.setStatus(1);
        row.setDelFlag(0);
        EducationWriteGuard.require(sections.insert(row), "insert exam section");
        return row.getId();
    }

    @Override
    public void insertQuestionLink(TenantId tenantId, Long examId, Long sectionId,
            Long questionId, int orderNo, BigDecimal score) {
        ExamQuestionDO row = new ExamQuestionDO();
        row.setTenantId(tenantId.value());
        row.setExamId(examId);
        row.setSectionId(sectionId);
        row.setQuestionId(questionId);
        row.setOrderNo(orderNo);
        row.setScore(score);
        row.setStatus(1);
        row.setDelFlag(0);
        EducationWriteGuard.require(questions.insert(row), "insert exam question link");
    }

    @Override
    public Attempt findAttemptByExamStudent(TenantId tenantId, Long examId, Long studentId) {
        return map(
                attempts.selectOneByQuery(
                        QueryWrapper.create()
                                .eq("tenant_id", tenantId.value())
                                .eq("exam_id", examId)
                                .eq("student_id", studentId)));
    }

    @Override
    public Attempt findAttemptById(TenantId tenantId, Long attemptId) {
        return map(
                attempts.selectOneByQuery(
                        QueryWrapper.create()
                                .eq("tenant_id", tenantId.value())
                                .eq("id", attemptId)));
    }

    @Override
    public List<Attempt> findAttemptsByStudent(TenantId tenantId, Long studentId) {
        return attempts.selectListByQuery(QueryWrapper.create()
                        .eq("tenant_id", tenantId.value())
                        .eq("student_id", studentId)
                        .orderBy("started_at", false))
                .stream().map(ExamTakingRepositoryImpl::map).toList();
    }

    @Override
    public List<DueAttempt> findDueAttempts(LocalDateTime now, Long afterAttemptId, int limit) {
        Objects.requireNonNull(now, "now is required");
        if (limit < 1) throw new IllegalArgumentException("limit must be positive");
        QueryWrapper query = QueryWrapper.create()
                .eq("status", "STARTED")
                .le("deadline_at", now);
        if (afterAttemptId != null) query.gt("id", afterAttemptId);
        return TenantQueryExecutor.readAcrossTenants(() ->
                attempts.selectListByQuery(query
                                .orderBy("id", true)
                                .limit(Math.min(limit, 100)))
                        .stream()
                        .map(row -> new DueAttempt(new TenantId(row.getTenantId()), row.getId()))
                        .toList());
    }

    @Override
    public Attempt insertAttempt(TenantId tenantId, Attempt attempt) {
        ExamAttemptDO row = new ExamAttemptDO();
        row.setTenantId(tenantId.value());
        row.setExamId(attempt.examId());
        row.setStudentId(attempt.studentId());
        row.setStatus(attempt.status());
        row.setStartedAt(attempt.startedAt());
        row.setDeadlineAt(attempt.deadlineAt());
        row.setTotalScore(attempt.totalScore());
        EducationWriteGuard.require(attempts.insert(row), "insert exam attempt");
        return map(row);
    }

    @Override
    public void insertAttemptQuestion(TenantId tenantId, AttemptQuestion question) {
        ExamAttemptQuestionDO row = new ExamAttemptQuestionDO();
        row.setTenantId(tenantId.value());
        row.setAttemptId(question.attemptId());
        row.setExamQuestionId(question.examQuestionId());
        row.setQuestionId(question.questionId());
        row.setSectionId(question.sectionId());
        row.setSectionName(question.sectionName());
        row.setSectionOrderNo(question.sectionOrderNo());
        row.setOrderNo(question.orderNo());
        row.setTitle(question.title());
        row.setType(question.type());
        row.setOptionsJson(question.optionsJson());
        row.setCorrectAnswer(question.correctAnswer());
        row.setAnalysis(question.analysis());
        row.setMaxScore(question.maxScore());
        EducationWriteGuard.require(attemptQuestions.insert(row), "insert exam question snapshot");
    }

    @Override
    public List<AttemptQuestion> attemptQuestions(TenantId tenantId, Long attemptId) {
        return attemptQuestions.selectListByQuery(
                        QueryWrapper.create()
                                .eq("tenant_id", tenantId.value())
                                .eq("attempt_id", attemptId)
                                .orderBy("section_order_no", true)
                                .orderBy("order_no", true))
                .stream()
                .map(ExamTakingRepositoryImpl::map)
                .toList();
    }

    @Override
    public int claimAttempt(TenantId tenantId, Long attemptId, String targetStatus,
            LocalDateTime now, boolean beforeDeadline) {
        ExamAttemptDO row = new ExamAttemptDO();
        row.setStatus(targetStatus);
        QueryWrapper scope = QueryWrapper.create()
                .eq("tenant_id", tenantId.value())
                .eq("id", attemptId)
                .eq("status", "STARTED");
        if (beforeDeadline) scope.gt("deadline_at", now);
        else scope.le("deadline_at", now);
        return attempts.updateByQuery(row, scope);
    }

    @Override
    public void saveDraftAnswer(TenantId tenantId, Long attemptQuestionId, String answer) {
        ExamAttemptQuestionDO row = new ExamAttemptQuestionDO();
        row.setAnswer(answer);
        EducationWriteGuard.require(attemptQuestions.updateByQuery(row,
                QueryWrapper.create().eq("tenant_id", tenantId.value())
                        .eq("id", attemptQuestionId)), "save exam draft answer");
    }

    @Override
    public void saveAnswer(
            TenantId tenantId, Long attemptQuestionId, String answer, BigDecimal score) {
        ExamAttemptQuestionDO row = new ExamAttemptQuestionDO();
        row.setAnswer(answer);
        row.setAwardedScore(score);
        EducationWriteGuard.require(
                attemptQuestions.updateByQuery(
                        row,
                        QueryWrapper.create()
                                .eq("tenant_id", tenantId.value())
                                .eq("id", attemptQuestionId)),
                "save exam answer");
    }

    @Override
    public int finishAttempt(
            TenantId tenantId, Long attemptId, BigDecimal score, LocalDateTime submittedAt,
            String finalStatus) {
        ExamAttemptDO row = new ExamAttemptDO();
        row.setStatus(finalStatus);
        row.setScore(score);
        row.setSubmittedAt(submittedAt);
        return attempts.updateByQuery(
                row,
                QueryWrapper.create()
                        .eq("tenant_id", tenantId.value())
                        .eq("id", attemptId)
                        .eq("status", "FINALIZING"));
    }

    private static Attempt map(ExamAttemptDO row) {
        if (row == null) return null;
        return new Attempt(
                row.getId(),
                row.getExamId(),
                row.getStudentId(),
                row.getStatus(),
                row.getStartedAt(),
                row.getDeadlineAt(),
                row.getSubmittedAt(),
                row.getScore(),
                row.getTotalScore());
    }

    private static AttemptQuestion map(ExamAttemptQuestionDO row) {
        return new AttemptQuestion(
                row.getId(),
                row.getAttemptId(),
                row.getExamQuestionId(),
                row.getQuestionId(),
                row.getSectionId(),
                row.getSectionName(),
                row.getSectionOrderNo(),
                row.getOrderNo(),
                row.getTitle(),
                row.getType(),
                row.getOptionsJson(),
                row.getCorrectAnswer(),
                row.getAnalysis(),
                row.getMaxScore(),
                row.getAnswer(),
                row.getAwardedScore());
    }
}
