package com.shiyu.ai.education.implementation.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.shiyu.ai.education.implementation.web.dto.ExamPaperResponse;
import com.shiyu.ai.education.implementation.web.dto.ExamResponse;
import com.shiyu.ai.education.implementation.web.dto.ExamResultResponse;
import com.shiyu.ai.education.implementation.web.dto.ExamSummaryResponse;
import com.shiyu.ai.education.implementation.web.request.SubmitExamRequest;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** 为已认证学生生成不含答案的试卷。 */
@Service
public class ExamTakingService {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {};

    private final ExamRepository exams;
    private final ExamTakingRepository taking;
    private final QuestionRepository questions;
    private final StudentRepository students;
    private final Clock clock;

    @Autowired
    public ExamTakingService(
            ExamRepository exams,
            ExamTakingRepository taking,
            QuestionRepository questions,
            StudentRepository students) {
        this(exams, taking, questions, students, Clock.systemDefaultZone());
    }

    ExamTakingService(
            ExamRepository exams,
            ExamTakingRepository taking,
            QuestionRepository questions,
            StudentRepository students,
            Clock clock) {
        this.exams = exams;
        this.taking = taking;
        this.questions = questions;
        this.students = students;
        this.clock = clock;
    }

    public ExamPaperResponse paper(ActorContext actor, Long examId) {
        StudentBO student = requireStudent(actor);
        ExamBO exam = requireAvailableExam(actor, examId, student);
        List<ExamSectionBO> sections = new ArrayList<>(taking.sections(actor.tenantId(), examId));
        List<ExamQuestionBO> links = new ArrayList<>(taking.questions(actor.tenantId(), examId));
        if (sections.isEmpty() || links.isEmpty()) throw new ServiceException("考试尚未配置题目");
        sections.sort(Comparator.comparing(ExamSectionBO::getOrderNo));
        links.sort(Comparator.comparing(ExamQuestionBO::getOrderNo));

        List<ExamPaperResponse.Section> visibleSections = new ArrayList<>();
        for (ExamSectionBO section : sections) {
            List<ExamPaperResponse.Question> visibleQuestions = new ArrayList<>();
            for (ExamQuestionBO link : links) {
                if (!Objects.equals(link.getSectionId(), section.getId())) continue;
                QuestionBO question = questions.selectById(actor.tenantId(), link.getQuestionId());
                if (question == null || !Objects.equals(question.getStatus(), 1)
                        || !"CHOICE".equals(question.getType())
                        || !"EXAM_PRIVATE".equals(question.getPurpose())
                        || link.getScore() == null || link.getScore().signum() <= 0) {
                    throw new ServiceException("试卷包含不可用题目");
                }
                visibleQuestions.add(
                        new ExamPaperResponse.Question(
                                link.getId(),
                                question.getId(),
                                question.getTitle(),
                                question.getType(),
                                link.getScore(),
                                options(question.getOptions())));
            }
            if (!visibleQuestions.isEmpty()) {
                visibleSections.add(
                        new ExamPaperResponse.Section(
                                section.getId(), section.getName(), section.getOrderNo(), visibleQuestions));
            }
        }
        if (visibleSections.isEmpty()
                || visibleSections.stream().mapToInt(section -> section.questions().size()).sum()
                        != links.size()) {
            throw new ServiceException("试卷题目关联不完整");
        }
        return new ExamPaperResponse(
                exam.getId(),
                null,
                exam.getName(),
                exam.getSubjectCode(),
                exam.getGrade(),
                exam.getDurationMin(),
                exam.getTotalScore(),
                null,
                List.copyOf(visibleSections));
    }

    public PageData<ExamResponse> available(ActorContext actor, String subjectCode,
            int pageNum, int pageSize) {
        StudentBO student = requireStudent(actor);
        if (pageNum < 1 || pageSize < 1 || pageSize > 100) {
            throw new ServiceException("分页参数无效");
        }
        PageData<ExamBO> page = exams.selectAvailable(actor.tenantId(), student.getGrade(),
                subjectCode, LocalDateTime.now(clock), pageNum, pageSize);
        return new PageData<>(MapstructUtils.convert(page.getItems(), ExamResponse.class),
                page.getTotal());
    }

    /** 列出开放中的考试，仅附带已认证学生本人的作答状态。 */
    public PageData<ExamSummaryResponse> availableSummaries(ActorContext actor,
            String subjectCode, int pageNum, int pageSize) {
        StudentBO student = requireStudent(actor);
        if (pageNum < 1 || pageSize < 1 || pageSize > 100) {
            throw new ServiceException("分页参数无效");
        }
        var page = exams.selectAvailable(actor.tenantId(), student.getGrade(),
                subjectCode, LocalDateTime.now(clock), pageNum, pageSize);
        return new PageData<>(page.getItems().stream()
                .map(exam -> summarize(exam, taking.findAttemptByExamStudent(
                        actor.tenantId(), exam.getId(), student.getId())))
                .toList(), page.getTotal());
    }

    /** 即使考试时间窗口已关闭，仍返回学生此前的作答记录。 */
    @Transactional(rollbackFor = Exception.class)
    public PageData<ExamSummaryResponse> history(ActorContext actor, int pageNum, int pageSize) {
        StudentBO student = requireStudent(actor);
        if (pageNum < 1 || pageSize < 1 || pageSize > 100) {
            throw new ServiceException("分页参数无效");
        }
        List<ExamSummaryResponse> entries = taking.findAttemptsByStudent(
                actor.tenantId(), student.getId()).stream()
                .map(attempt -> {
                    ExamBO exam = exams.selectById(actor.tenantId(), attempt.examId());
                    if (exam == null) return null;
                    if ("STARTED".equals(attempt.status()) && attempt.deadlineAt() != null
                            && !LocalDateTime.now(clock).isBefore(attempt.deadlineAt())) {
                        finalizeExpired(actor.tenantId(), attempt, LocalDateTime.now(clock));
                        var settled = taking.findAttemptById(actor.tenantId(), attempt.id());
                        if (settled == null) throw new ServiceException("考试记录不存在");
                        return summarize(exam, settled);
                    }
                    return summarize(exam, attempt);
                })
                .filter(Objects::nonNull).toList();
        int from = (int) Math.min(entries.size(), (long) (pageNum - 1) * pageSize);
        int to = Math.min(entries.size(), from + pageSize);
        return new PageData<>(entries.subList(from, to), entries.size());
    }

    /** 结算由可信后台任务选出的一条到期作答记录。 */
    @Transactional(rollbackFor = Exception.class)
    public void settleExpiredAttempt(TenantId tenantId, Long attemptId) {
        TenantScope.requireMatches(tenantId);
        var attempt = taking.findAttemptById(tenantId, attemptId);
        LocalDateTime now = LocalDateTime.now(clock);
        if (attempt == null || !"STARTED".equals(attempt.status())
                || attempt.deadlineAt() == null || now.isBefore(attempt.deadlineAt())) {
            return;
        }
        finalizeExpired(tenantId, attempt, now);
    }

    private ExamSummaryResponse summarize(ExamBO exam, ExamTakingRepository.Attempt attempt) {
        String state = "NOT_STARTED";
        if (attempt != null) {
            state = "STARTED".equals(attempt.status())
                    && attempt.deadlineAt() != null
                    && !LocalDateTime.now(clock).isBefore(attempt.deadlineAt())
                            ? "EXPIRED" : attempt.status();
            if ("STARTED".equals(state)) state = "IN_PROGRESS";
        }
        return new ExamSummaryResponse(exam.getId(), exam.getName(), exam.getType(),
                exam.getSubjectCode(), exam.getGrade(), exam.getDurationMin(),
                exam.getTotalScore(), attempt == null ? null : attempt.id(), state,
                attempt == null ? null : toInstant(attempt.deadlineAt()),
                attempt == null ? null : attempt.score());
    }

    /** 首次开始考试，或重新打开学生已有的进行中试卷快照。 */
    @Transactional(rollbackFor = Exception.class)
    public ExamPaperResponse start(ActorContext actor, Long examId) {
        StudentBO student = requireStudent(actor);
        if (!students.lockForStudentWrite(actor.tenantId(), student.getId())) {
            throw new ServiceException("当前学生档案不可用");
        }
        ExamBO exam = exams.selectById(actor.tenantId(), examId);
        var existing = taking.findAttemptByExamStudent(actor.tenantId(), examId, student.getId());
        LocalDateTime now = LocalDateTime.now(clock);
        if (existing != null) {
            if (exam == null || !Objects.equals(exam.getGrade(), student.getGrade())) {
                throw new ServiceException("考试记录不存在");
            }
            if ("STARTED".equals(existing.status())
                    && existing.deadlineAt() != null
                    && !now.isBefore(existing.deadlineAt())) {
                finalizeExpired(actor.tenantId(), existing, now);
                return closedPaper(exam, existing, actor);
            }
            if ("SUBMITTED".equals(existing.status()) || "EXPIRED".equals(existing.status())) {
                return closedPaper(exam, existing, actor);
            }
            if (!"STARTED".equals(existing.status()) || existing.deadlineAt() == null) {
                throw new ServiceException("本次考试状态无效");
            }
            return paperFromSnapshot(exam, existing,
                    taking.attemptQuestions(actor.tenantId(), existing.id()));
        }

        exam = requireAvailableExam(actor, examId, student);

        ExamPaperResponse visible = paper(actor, examId);
        LocalDateTime deadline = now.plusMinutes(exam.getDurationMin());
        if (deadline.isAfter(exam.getEndTime())) deadline = exam.getEndTime();
        var attempt = taking.insertAttempt(
                actor.tenantId(),
                new ExamTakingRepository.Attempt(
                        null, examId, student.getId(), "STARTED", now, deadline, null,
                        null, BigDecimal.valueOf(exam.getTotalScore())));
        for (ExamPaperResponse.Section section : visible.sections()) {
            int order = 0;
            for (ExamPaperResponse.Question visibleQuestion : section.questions()) {
                QuestionBO source = questions.selectById(actor.tenantId(), visibleQuestion.questionId());
                if (source == null || source.getAnswer() == null
                        || !"CHOICE".equals(source.getType())
                        || options(source.getOptions()).stream().noneMatch(option ->
                                option.value().equalsIgnoreCase(source.getAnswer()))) {
                    throw new ServiceException("试卷包含缺少答案的题目");
                }
                taking.insertAttemptQuestion(
                        actor.tenantId(),
                        new ExamTakingRepository.AttemptQuestion(
                                null, attempt.id(), visibleQuestion.id(), source.getId(),
                                section.id(), section.name(), section.orderNo(), ++order,
                                source.getTitle(), source.getType(),
                                source.getOptions(), source.getAnswer(), source.getAnalysis(),
                                visibleQuestion.score(), null, null));
            }
        }
        return new ExamPaperResponse(
                visible.examId(), attempt.id(), visible.name(), visible.subjectCode(),
                visible.grade(), visible.durationMin(), visible.totalScore(),
                toInstant(deadline), visible.sections());
    }

    /** 在同一事务中保存所有答案并完成本次作答。 */
    @Transactional(rollbackFor = Exception.class)
    public ExamResultResponse submit(
            ActorContext actor, Long attemptId, SubmitExamRequest request) {
        var attempt = requireOwnedAttempt(actor, attemptId);
        if ("SUBMITTED".equals(attempt.status()) || "EXPIRED".equals(attempt.status())) {
            return completedResult(attempt,
                    taking.attemptQuestions(actor.tenantId(), attemptId));
        }
        LocalDateTime now = LocalDateTime.now(clock);
        if (!"STARTED".equals(attempt.status()) || attempt.deadlineAt() == null) {
            throw new ServiceException("考试状态不可提交");
        }
        if (!now.isBefore(attempt.deadlineAt())) {
            return finalizeExpired(actor.tenantId(), attempt, now);
        }
        if (request == null || request.answers() == null) {
            throw new ServiceException("答题内容不能为空");
        }
        if (taking.claimAttempt(actor.tenantId(), attemptId, "FINALIZING", now, true) != 1) {
            return resolvedAfterRace(actor.tenantId(), attemptId);
        }
        List<ExamTakingRepository.AttemptQuestion> snapshots =
                taking.attemptQuestions(actor.tenantId(), attemptId);
        if (snapshots.isEmpty()) throw new ServiceException("考试题目快照不存在");
        Map<Long, String> answers = new HashMap<>();
        Set<Long> known = new HashSet<>();
        for (var snapshot : snapshots) known.add(snapshot.questionId());
        for (SubmitExamRequest.Answer answer : request.answers()) {
            if (answer == null || answer.questionId() == null
                    || !known.contains(answer.questionId())
                    || answers.containsKey(answer.questionId())) {
                throw new ServiceException("答题包含未知或重复的题目");
            }
            String value = answer.answer() == null ? "" : answer.answer().trim();
            if (value.length() > 10_000) throw new ServiceException("答案内容过长");
            answers.put(answer.questionId(), value);
        }
        BigDecimal total = BigDecimal.ZERO;
        List<ExamResultResponse.QuestionResult> results = new ArrayList<>();
        for (var snapshot : snapshots) {
            String submitted = normalized(answers.get(snapshot.questionId()));
            String answer = submitted.isBlank() ? normalized(snapshot.answer()) : submitted;
            validateAnswer(snapshot, answer);
            BigDecimal score = normalized(answer).equalsIgnoreCase(normalized(snapshot.correctAnswer()))
                    && !answer.isBlank() ? snapshot.maxScore() : BigDecimal.ZERO;
            taking.saveAnswer(actor.tenantId(), snapshot.id(), answer, score);
            total = total.add(score);
            results.add(resultQuestion(snapshot, answer, score));
        }
        LocalDateTime submittedAt = LocalDateTime.now(clock);
        if (taking.finishAttempt(actor.tenantId(), attemptId, total, submittedAt,
                "SUBMITTED") != 1) {
            throw new ServiceException("考试提交状态已改变，请刷新结果");
        }
        return new ExamResultResponse(attemptId, attempt.examId(), "SUBMITTED", total,
                attempt.totalScore(), toInstant(submittedAt), List.copyOf(results));
    }

    @Transactional(rollbackFor = Exception.class)
    public ExamResultResponse result(ActorContext actor, Long attemptId) {
        var attempt = requireOwnedAttempt(actor, attemptId);
        if ("STARTED".equals(attempt.status()) && attempt.deadlineAt() != null
                && !LocalDateTime.now(clock).isBefore(attempt.deadlineAt())) {
            return finalizeExpired(actor.tenantId(), attempt, LocalDateTime.now(clock));
        }
        if (!"SUBMITTED".equals(attempt.status()) && !"EXPIRED".equals(attempt.status())) {
            throw new ServiceException("考试尚未提交");
        }
        return completedResult(attempt, taking.attemptQuestions(actor.tenantId(), attemptId));
    }

    /** 保存学生的一条答案，不暴露正确答案，也不改变分数。 */
    @Transactional(rollbackFor = Exception.class)
    public void saveDraft(ActorContext actor, Long attemptId, SubmitExamRequest.Answer answer) {
        var attempt = requireOwnedAttempt(actor, attemptId);
        LocalDateTime now = LocalDateTime.now(clock);
        if (!"STARTED".equals(attempt.status()) || attempt.deadlineAt() == null
                || !now.isBefore(attempt.deadlineAt())
                || taking.claimAttempt(actor.tenantId(), attemptId, "STARTED", now, true) != 1) {
            throw new ServiceException("考试已过答题期限");
        }
        if (answer == null || answer.questionId() == null) {
            throw new ServiceException("答题题目无效");
        }
        var snapshot = taking.attemptQuestions(actor.tenantId(), attemptId).stream()
                .filter(item -> answer.questionId().equals(item.questionId()))
                .findFirst().orElseThrow(() -> new ServiceException("答题题目无效"));
        String value = normalized(answer.answer());
        validateAnswer(snapshot, value);
        taking.saveDraftAnswer(actor.tenantId(), snapshot.id(), value);
    }

    /** 仅返回学生已保存的选项，不返回试卷快照中的答案。 */
    public Map<Long, String> draft(ActorContext actor, Long attemptId) {
        var attempt = requireOwnedAttempt(actor, attemptId);
        if (!"STARTED".equals(attempt.status()) || attempt.deadlineAt() == null
                || !LocalDateTime.now(clock).isBefore(attempt.deadlineAt())) {
            throw new ServiceException("考试已结束");
        }
        Map<Long, String> saved = new HashMap<>();
        for (var question : taking.attemptQuestions(actor.tenantId(), attemptId)) {
            if (question.answer() != null) saved.put(question.questionId(), question.answer());
        }
        return Map.copyOf(saved);
    }

    private ExamResultResponse finalizeExpired(TenantId tenantId,
            ExamTakingRepository.Attempt attempt, LocalDateTime now) {
        if (taking.claimAttempt(tenantId, attempt.id(), "FINALIZING", now, false) != 1) {
            return resolvedAfterRace(tenantId, attempt.id());
        }
        List<ExamTakingRepository.AttemptQuestion> snapshots =
                taking.attemptQuestions(tenantId, attempt.id());
        if (snapshots.isEmpty()) throw new ServiceException("考试题目快照不存在");
        BigDecimal total = BigDecimal.ZERO;
        List<ExamResultResponse.QuestionResult> results = new ArrayList<>();
        for (var snapshot : snapshots) {
            String answer = normalized(snapshot.answer());
            BigDecimal score = !answer.isBlank()
                    && answer.equalsIgnoreCase(normalized(snapshot.correctAnswer()))
                            ? snapshot.maxScore() : BigDecimal.ZERO;
            taking.saveAnswer(tenantId, snapshot.id(), answer, score);
            total = total.add(score);
            results.add(resultQuestion(snapshot, answer, score));
        }
        if (taking.finishAttempt(tenantId, attempt.id(), total, now,
                "EXPIRED") != 1) {
            throw new ServiceException("考试结算状态已改变，请刷新结果");
        }
        return new ExamResultResponse(attempt.id(), attempt.examId(), "EXPIRED", total,
                attempt.totalScore(), toInstant(now), List.copyOf(results));
    }

    private ExamResultResponse resolvedAfterRace(TenantId tenantId, Long attemptId) {
        var latest = taking.findAttemptById(tenantId, attemptId);
        if (latest == null) throw new ServiceException("考试记录不存在");
        if ("SUBMITTED".equals(latest.status()) || "EXPIRED".equals(latest.status())) {
            return completedResult(latest, taking.attemptQuestions(tenantId, attemptId));
        }
        throw new ServiceException("考试状态已改变，请刷新后重试");
    }

    private ExamTakingRepository.Attempt requireOwnedAttempt(ActorContext actor, Long attemptId) {
        StudentBO student = requireStudent(actor);
        var attempt = taking.findAttemptById(actor.tenantId(), attemptId);
        if (attempt == null || !Objects.equals(attempt.studentId(), student.getId())) {
            throw new ServiceException("考试记录不存在");
        }
        return attempt;
    }

    private ExamResultResponse completedResult(
            ExamTakingRepository.Attempt attempt,
            List<ExamTakingRepository.AttemptQuestion> snapshots) {
        return new ExamResultResponse(
                attempt.id(), attempt.examId(), attempt.status(), attempt.score(),
                attempt.totalScore(), toInstant(attempt.submittedAt()),
                snapshots.stream()
                        .map(snapshot -> resultQuestion(
                                snapshot, snapshot.answer(), snapshot.awardedScore()))
                        .toList());
    }

    private static ExamResultResponse.QuestionResult resultQuestion(
            ExamTakingRepository.AttemptQuestion snapshot, String answer, BigDecimal score) {
        return new ExamResultResponse.QuestionResult(
                snapshot.questionId(), snapshot.title(), answer, snapshot.correctAnswer(),
                snapshot.analysis(), score, snapshot.maxScore());
    }

    private ExamPaperResponse paperFromSnapshot(
            ExamBO exam,
            ExamTakingRepository.Attempt attempt,
            List<ExamTakingRepository.AttemptQuestion> snapshots) {
        if (snapshots.isEmpty()) throw new ServiceException("考试题目快照不存在");
        List<ExamTakingRepository.AttemptQuestion> ordered = new ArrayList<>(snapshots);
        ordered.sort(Comparator
                .comparing(ExamTakingRepository.AttemptQuestion::sectionOrderNo)
                .thenComparing(ExamTakingRepository.AttemptQuestion::orderNo));
        Map<Long, List<ExamPaperResponse.Question>> grouped = new LinkedHashMap<>();
        Map<Long, ExamTakingRepository.AttemptQuestion> sectionHeads = new LinkedHashMap<>();
        for (var snapshot : ordered) {
            sectionHeads.putIfAbsent(snapshot.sectionId(), snapshot);
            grouped.computeIfAbsent(snapshot.sectionId(), ignored -> new ArrayList<>())
                    .add(new ExamPaperResponse.Question(
                            snapshot.examQuestionId(), snapshot.questionId(), snapshot.title(),
                            snapshot.type(), snapshot.maxScore(), options(snapshot.optionsJson())));
        }
        List<ExamPaperResponse.Section> visible = new ArrayList<>();
        for (var entry : grouped.entrySet()) {
            var head = sectionHeads.get(entry.getKey());
            visible.add(new ExamPaperResponse.Section(
                    entry.getKey(), head.sectionName(), head.sectionOrderNo(),
                    List.copyOf(entry.getValue())));
        }
        return new ExamPaperResponse(
                exam.getId(), attempt.id(), exam.getName(), exam.getSubjectCode(),
                exam.getGrade(), exam.getDurationMin(), exam.getTotalScore(),
                toInstant(attempt.deadlineAt()), List.copyOf(visible));
    }

    private ExamPaperResponse closedPaper(ExamBO exam,
            ExamTakingRepository.Attempt attempt, ActorContext actor) {
        var paper = paperFromSnapshot(exam, attempt,
                taking.attemptQuestions(actor.tenantId(), attempt.id()));
        return new ExamPaperResponse(paper.examId(), paper.attemptId(), paper.name(),
                paper.subjectCode(), paper.grade(), paper.durationMin(), paper.totalScore(),
                Instant.EPOCH, paper.sections());
    }

    private static String normalized(String value) {
        return value == null ? "" : value.trim();
    }

    private static void validateAnswer(ExamTakingRepository.AttemptQuestion snapshot,
            String answer) {
        if (answer.length() > 10_000) throw new ServiceException("答案内容过长");
        if ("CHOICE".equals(snapshot.type()) && !answer.isBlank()
                && options(snapshot.optionsJson()).stream()
                        .noneMatch(option -> option.value().equalsIgnoreCase(answer))) {
            throw new ServiceException("选择题答案无效");
        }
    }

    private Instant toInstant(LocalDateTime time) {
        return time == null ? null : time.atZone(clock.getZone()).toInstant();
    }

    private StudentBO requireStudent(ActorContext actor) {
        if (actor == null || actor.userId() == null || actor.tenantId() == null) {
            throw new ServiceException("无法识别当前学生");
        }
        StudentBO student = students.selectByUserId(actor.tenantId(), actor.userId().value());
        if (student == null || student.getId() == null || !Objects.equals(student.getStatus(), 1)) {
            throw new ServiceException("当前账号未关联可用的学生档案");
        }
        return student;
    }

    private ExamBO requireAvailableExam(ActorContext actor, Long examId, StudentBO student) {
        ExamBO exam = exams.selectById(actor.tenantId(), examId);
        LocalDateTime now = LocalDateTime.now(clock);
        if (exam == null
                || !Objects.equals(exam.getStatus(), 1)
                || exam.getStartTime() == null
                || exam.getEndTime() == null
                || now.isBefore(exam.getStartTime())
                || !now.isBefore(exam.getEndTime())
                || !Objects.equals(student.getGrade(), exam.getGrade())) {
            throw new ServiceException("考试当前不可参加");
        }
        return exam;
    }

    static List<ExamPaperResponse.Option> options(String encoded) {
        if (encoded == null || encoded.isBlank()) return List.of();
        try {
            List<String> labels = JSON.readValue(encoded, STRING_LIST);
            if (labels == null || labels.size() < 2 || labels.size() > 26) {
                throw new ServiceException("题目选项数量无效");
            }
            List<ExamPaperResponse.Option> result = new ArrayList<>();
            for (int index = 0; index < labels.size(); index++) {
                String label = labels.get(index);
                if (label == null || label.isBlank()) {
                    throw new ServiceException("题目选项内容无效");
                }
                String value = String.valueOf((char) ('A' + index));
                result.add(new ExamPaperResponse.Option(value, label));
            }
            return List.copyOf(result);
        } catch (Exception error) {
            throw new ServiceException("题目选项格式无效");
        }
    }
}
