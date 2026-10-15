package com.shiyu.ai.education.implementation.application;

import com.shiyu.ai.common.foundation.exception.ServiceException;
import com.shiyu.ai.education.implementation.domain.BloomTaxonomy;
import com.shiyu.ai.education.implementation.domain.model.QuestionBO;
import com.shiyu.ai.education.implementation.domain.model.StudentBO;
import com.shiyu.ai.education.implementation.domain.model.StudyRecordBO;
import com.shiyu.ai.education.implementation.domain.port.repository.QuestionRepository;
import com.shiyu.ai.education.implementation.domain.port.repository.StudentRepository;
import com.shiyu.ai.education.implementation.domain.port.repository.StudyRecordRepository;
import com.shiyu.ai.kernel.context.ActorContext;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 仅依据经过核验的服务端评分练习记录，执行一次能力分析。 */
@Service
@RequiredArgsConstructor
public class TrustedScoreAnalysisService {
    private final StudentRepository students;
    private final QuestionRepository questions;
    private final StudyRecordRepository records;
    private final AbilityService abilities;

    /** 分析已登录学生在所属租户内的记录，每条记录只处理一次。 */
    @Transactional(rollbackFor = Exception.class)
    public Analysis analyze(ActorContext actor, Long recordId) {
        if (actor == null || actor.tenantId() == null || actor.userId() == null
                || recordId == null || recordId <= 0) {
            throw new ServiceException("评分上下文无效");
        }
        StudentBO student = students.selectByUserId(actor.tenantId(), actor.userId().value());
        if (student == null || !Integer.valueOf(1).equals(student.getStatus())) {
            throw new ServiceException("当前学生档案不可用");
        }
        StudyRecordBO record = records.selectById(actor.tenantId(), recordId);
        if (record == null || !student.getId().equals(record.getStudentId())
                || !"PRACTICE".equals(record.getRecordType())
                || record.getSubmissionKey() == null || record.getSubmissionKey().isBlank()
                || record.getQuestionId() == null || record.getKnowledgeId() == null
                || record.getSelectedAnswer() == null || record.getCorrectAnswer() == null
                || record.getScore() == null || record.getAccuracy() == null) {
            throw new ServiceException("可信练习记录不存在");
        }
        QuestionBO question = questions.selectById(actor.tenantId(), record.getQuestionId());
        if (question == null || !"PRACTICE".equals(question.getPurpose())) {
            throw new ServiceException("练习题归属无效");
        }
        boolean correct = record.getSelectedAnswer().equalsIgnoreCase(record.getCorrectAnswer());
        if (!record.getScore().equals(correct ? 100D : 0D)
                || !record.getAccuracy().equals(correct ? 1D : 0D)) {
            throw new ServiceException("练习记录判分不一致");
        }
        BloomTaxonomy dimension;
        try {
            dimension = BloomTaxonomy.valueOf(record.getAbilityDimension()
                    .toUpperCase(Locale.ROOT));
        } catch (Exception invalid) {
            throw new ServiceException("练习记录缺少有效能力维度");
        }
        if (records.claimAnalysis(actor.tenantId(), student.getId(), recordId) != 1) {
            return new Analysis(recordId, record.getScore(), record.getKnowledgeId(), false);
        }
        abilities.update(actor, student.getId(), record.getKnowledgeId(),
                dimension, record.getAccuracy());
        return new Analysis(recordId, record.getScore(), record.getKnowledgeId(), true);
    }

    /** 可信分数，以及本次调用是否已将其应用到能力画像。 */
    public record Analysis(Long recordId, Double score, Long knowledgeId, boolean applied) {}
}
