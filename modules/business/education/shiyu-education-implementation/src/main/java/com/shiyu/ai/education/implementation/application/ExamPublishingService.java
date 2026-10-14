package com.shiyu.ai.education.implementation.application;

import com.shiyu.ai.common.foundation.exception.ServiceException;
import com.shiyu.ai.education.implementation.domain.model.ExamBO;
import com.shiyu.ai.education.implementation.domain.model.QuestionBO;
import com.shiyu.ai.education.implementation.domain.port.repository.ExamRepository;
import com.shiyu.ai.education.implementation.domain.port.repository.ExamTakingRepository;
import com.shiyu.ai.education.implementation.domain.port.repository.QuestionRepository;
import com.shiyu.ai.education.implementation.web.request.ExamCompositionRequest;
import com.shiyu.ai.kernel.context.ActorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/** Validates and publishes a complete, objectively scored exam. */
@Service
@RequiredArgsConstructor
public class ExamPublishingService {
    private final ExamRepository exams;
    private final ExamTakingRepository composition;
    private final QuestionRepository questions;

    @Transactional(rollbackFor = Exception.class)
    public void replaceComposition(ActorContext actor, Long examId,
            ExamCompositionRequest request) {
        ExamBO exam = draft(actor, examId);
        if (request == null || request.sections() == null || request.sections().isEmpty()) {
            throw new ServiceException("试卷至少需要一道题");
        }
        BigDecimal total = BigDecimal.ZERO;
        Set<Long> seen = new HashSet<>();
        for (var section : request.sections()) {
            if (section == null || section.name() == null || section.name().isBlank()
                    || section.questions() == null || section.questions().isEmpty()) {
                throw new ServiceException("试卷大题配置无效");
            }
            for (var item : section.questions()) {
                if (item == null || item.questionId() == null || item.score() == null
                        || item.score().signum() <= 0 || !seen.add(item.questionId())) {
                    throw new ServiceException("试卷题目或分值无效，题目不可重复");
                }
                QuestionBO source = questions.selectById(actor.tenantId(), item.questionId());
                if (source == null || !Integer.valueOf(1).equals(source.getStatus())
                        || !exam.getSubjectCode().equals(source.getSubjectCode())
                        || !exam.getGrade().equals(source.getGrade())
                        || !"CHOICE".equals(source.getType())
                        || !"EXAM_PRIVATE".equals(source.getPurpose())
                        || source.getAnswer() == null || source.getAnswer().isBlank()
                        || !source.getAnswer().matches("[A-Z]")
                        || source.getOptions() == null || source.getOptions().isBlank()
                        || ExamTakingService.options(source.getOptions()).stream()
                                .noneMatch(option -> option.value().equals(source.getAnswer()))) {
                    throw new ServiceException("试卷仅支持同学科、同年级的有效选择题");
                }
                total = total.add(item.score());
            }
        }
        if (total.compareTo(BigDecimal.valueOf(exam.getTotalScore())) != 0) {
            throw new ServiceException("试卷题目分值之和必须等于总分");
        }
        composition.deleteComposition(actor.tenantId(), examId);
        int sectionOrder = 0;
        for (var section : request.sections()) {
            Long sectionId = composition.insertSection(actor.tenantId(), examId,
                    section.name().trim(), ++sectionOrder, section.questions().get(0).score());
            int questionOrder = 0;
            for (var item : section.questions()) {
                composition.insertQuestionLink(actor.tenantId(), examId, sectionId,
                        item.questionId(), ++questionOrder, item.score());
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void publish(ActorContext actor, Long examId) {
        ExamBO exam = draft(actor, examId);
        if (exam.getDurationMin() == null || exam.getDurationMin() <= 0
                || exam.getStartTime() == null || exam.getEndTime() == null
                || !exam.getStartTime().isBefore(exam.getEndTime())
                || !LocalDateTime.now().isBefore(exam.getEndTime())) {
            throw new ServiceException("考试时间或时长无效");
        }
        var sections = composition.sections(actor.tenantId(), examId);
        var links = composition.questions(actor.tenantId(), examId);
        if (sections.isEmpty() || links.isEmpty()
                || links.stream().anyMatch(link -> sections.stream()
                        .noneMatch(section -> section.getId().equals(link.getSectionId())))) {
            throw new ServiceException("试卷尚未配置完整");
        }
        BigDecimal total = BigDecimal.ZERO;
        for (var link : links) {
            if (link.getScore() == null || link.getScore().signum() <= 0) {
                throw new ServiceException("试卷包含无效分值");
            }
            QuestionBO source = questions.selectById(actor.tenantId(), link.getQuestionId());
            if (source == null || !Integer.valueOf(1).equals(source.getStatus())
                    || !"CHOICE".equals(source.getType())
                    || !"EXAM_PRIVATE".equals(source.getPurpose())
                    || source.getAnswer() == null || !source.getAnswer().matches("[A-Z]")
                    || !exam.getSubjectCode().equals(source.getSubjectCode())
                    || !exam.getGrade().equals(source.getGrade())
                    || ExamTakingService.options(source.getOptions()).stream()
                            .noneMatch(option -> option.value().equals(source.getAnswer()))) {
                throw new ServiceException("试卷包含不可用题目");
            }
            total = total.add(link.getScore());
        }
        if (total.compareTo(BigDecimal.valueOf(exam.getTotalScore())) != 0) {
            throw new ServiceException("试卷题目分值之和必须等于总分");
        }
        exam.setStatus(1);
        exams.update(actor.tenantId(), exam);
    }

    private ExamBO draft(ActorContext actor, Long examId) {
        ExamBO exam = exams.selectById(actor.tenantId(), examId);
        if (exam == null || !Integer.valueOf(0).equals(exam.getStatus())) {
            throw new ServiceException("考试草稿不存在");
        }
        return exam;
    }
}
