package com.shiyu.ai.education.implementation.domain.port.repository;

import com.shiyu.ai.education.implementation.domain.model.ExamQuestionBO;
import com.shiyu.ai.education.implementation.domain.model.ExamSectionBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 按租户隔离学生考试的试卷组成与作答记录的持久化操作。 */
public interface ExamTakingRepository {
    /** 读取指定租户考试中按顺序排列的有效分区。 */
    List<ExamSectionBO> sections(TenantId tenantId, Long examId);

    /** 读取指定租户考试中的有效题目关联。 */
    List<ExamQuestionBO> questions(TenantId tenantId, Long examId);

    /** 清除未发布考试的现有试卷组成，以便重新编排。 */
    void deleteComposition(TenantId tenantId, Long examId);

    /** 插入按顺序排列的分区并返回其标识。 */
    Long insertSection(TenantId tenantId, Long examId, String name, int orderNo,
            BigDecimal scorePerQuestion);

    /** 将一道保密题目关联到分区。 */
    void insertQuestionLink(TenantId tenantId, Long examId, Long sectionId,
            Long questionId, int orderNo, BigDecimal score);

    /** 查询该学生在本次考试中的唯一作答记录。 */
    Attempt findAttemptByExamStudent(TenantId tenantId, Long examId, Long studentId);

    /** 在当前租户内按标识查询作答记录。 */
    Attempt findAttemptById(TenantId tenantId, Long attemptId);

    /** 列出学生的作答记录，包括已完成和已过期的考试。 */
    List<Attempt> findAttemptsByStudent(TenantId tenantId, Long studentId);

    /** 从可选的全局作答记录 ID 游标之后，按数量上限查询逾期作答记录。 */
    List<DueAttempt> findDueAttempts(LocalDateTime now, Long afterAttemptId, int limit);

    /** 创建作答记录，然后保存其题目快照。 */
    Attempt insertAttempt(TenantId tenantId, Attempt attempt);

    /** 保存一道作答题目的标准答案快照。 */
    void insertAttemptQuestion(TenantId tenantId, AttemptQuestion question);

    /** 读取不可变的题目快照及已保存的学生答案。 */
    List<AttemptQuestion> attemptQuestions(TenantId tenantId, Long attemptId);

    /** 保存或结束作答前锁定有效的作答记录；截止时间条件由 SQL 检查。 */
    int claimAttempt(TenantId tenantId, Long attemptId, String targetStatus,
            LocalDateTime now, boolean beforeDeadline);

    /** 在所属作答记录已锁定且有效时，保存一道尚未计分的答案。 */
    void saveDraftAnswer(TenantId tenantId, Long attemptQuestionId, String answer);

    /** 保存答案及服务端计算的得分。 */
    void saveAnswer(TenantId tenantId, Long attemptQuestionId, String answer, BigDecimal score);

    /** 将先前已锁定的作答记录恰好完成一次。 */
    int finishAttempt(TenantId tenantId, Long attemptId, BigDecimal score,
            LocalDateTime submittedAt, String finalStatus);

    /** 按租户、考试和学生限定的不可变作答状态。 */
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

    /** 供后台处理逾期作答使用的租户及作答记录标识。 */
    record DueAttempt(TenantId tenantId, Long attemptId) {}

    /** 包含学生当前答案的不可变考试题目快照。 */
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
