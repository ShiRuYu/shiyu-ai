package com.shiyu.ai.education.implementation.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.common.foundation.exception.ServiceException;
import com.shiyu.ai.common.foundation.utils.MapstructUtils;
import com.shiyu.ai.education.implementation.domain.model.QuestionBO;
import com.shiyu.ai.education.implementation.domain.model.StudentBO;
import com.shiyu.ai.education.implementation.domain.model.StudyRecordBO;
import com.shiyu.ai.education.implementation.domain.model.WrongQuestionBO;
import com.shiyu.ai.education.implementation.domain.port.repository.CourseRepository;
import com.shiyu.ai.education.implementation.domain.port.repository.CourseChapterRepository;
import com.shiyu.ai.education.implementation.domain.port.repository.CourseSectionRepository;
import com.shiyu.ai.education.implementation.domain.port.repository.QuestionRepository;
import com.shiyu.ai.education.implementation.domain.port.repository.StudentRepository;
import com.shiyu.ai.education.implementation.domain.port.repository.StudyRecordRepository;
import com.shiyu.ai.education.implementation.domain.port.repository.WrongQuestionRepository;
import com.shiyu.ai.education.implementation.web.dto.CourseResponse;
import com.shiyu.ai.education.implementation.web.dto.CourseDetailResponse;
import com.shiyu.ai.education.implementation.web.dto.ExamPaperResponse;
import com.shiyu.ai.education.implementation.web.dto.PracticeCheckResponse;
import com.shiyu.ai.education.implementation.web.dto.PracticeQuestionResponse;
import com.shiyu.ai.education.implementation.web.dto.PracticeSubmissionResponse;
import com.shiyu.ai.education.implementation.web.dto.WrongPracticeResponse;
import com.shiyu.ai.kernel.context.ActorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Narrow student catalog and answer-safe practice access. */
@Service
@RequiredArgsConstructor
public class StudentLearningService {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final TypeReference<List<String>> OPTIONS = new TypeReference<>() {};
    private final CourseRepository courses;
    private final CourseChapterRepository chapters;
    private final CourseSectionRepository sections;
    private final QuestionRepository questions;
    private final StudentRepository students;
    private final StudyRecordRepository records;
    private final WrongQuestionRepository wrongQuestions;

    public PageData<CourseResponse> courses(ActorContext actor, String subjectCode,
            Integer grade, int pageNum, int pageSize) {
        requireStudent(actor);
        if (pageNum < 1 || pageSize < 1 || pageSize > 100) throw new ServiceException("分页参数无效");
        var page = courses.selectCatalogPage(actor.tenantId(), subjectCode,
                grade, pageNum, pageSize);
        return new PageData<>(MapstructUtils.convert(page.getItems(), CourseResponse.class),
                page.getTotal());
    }

    public CourseDetailResponse course(ActorContext actor, Long id) {
        requireStudent(actor);
        var course = courses.selectById(actor.tenantId(), id);
        if (course == null || !Integer.valueOf(1).equals(course.getStatus())) {
            throw new ServiceException("课程不存在或已下架");
        }
        var visibleChapters = chapters.selectByCourseId(actor.tenantId(), id).stream()
                .filter(chapter -> Integer.valueOf(1).equals(chapter.getStatus())).toList();
        var visibleSections = visibleChapters.isEmpty() ? List.<com.shiyu.ai.education.implementation.domain.model.CourseSectionBO>of()
                : sections.selectByChapterIds(actor.tenantId(),
                        visibleChapters.stream().map(chapter -> chapter.getId()).toList()).stream()
                        .filter(section -> Integer.valueOf(1).equals(section.getStatus())).toList();
        return new CourseDetailResponse(MapstructUtils.convert(course, CourseResponse.class),
                visibleChapters.stream().map(chapter -> new CourseDetailResponse.Chapter(
                        chapter.getId(), chapter.getName(), chapter.getOrderNo(),
                        visibleSections.stream()
                                .filter(section -> chapter.getId().equals(section.getChapterId()))
                                .map(section -> new CourseDetailResponse.Section(section.getId(),
                                        section.getName(), section.getOrderNo(), section.getDurationMin()))
                                .toList())).toList());
    }

    public List<PracticeQuestionResponse> questions(ActorContext actor, String subjectCode,
            Integer grade) {
        StudentBO student = requireStudent(actor);
        int selectedGrade = grade == null ? student.getGrade() : grade;
        if (selectedGrade != student.getGrade()) return List.of();
        if (subjectCode == null || subjectCode.isBlank()) throw new ServiceException("学科不能为空");
        return questions.selectBySubjectAndGrade(actor.tenantId(), subjectCode, selectedGrade)
                .stream().filter(q -> Integer.valueOf(1).equals(q.getStatus())
                        && "CHOICE".equals(q.getType())
                        && "PRACTICE".equals(q.getPurpose()))
                .map(StudentLearningService::visible).toList();
    }

    public PracticeQuestionResponse question(ActorContext actor, Long id) {
        return visible(requireQuestion(actor, id));
    }

    /** Lists only active wrong practice questions owned by the current student. */
    public List<WrongPracticeResponse> wrongQuestions(ActorContext actor) {
        StudentBO student = requireStudent(actor);
        List<WrongPracticeResponse> visible = new ArrayList<>();
        for (WrongQuestionBO wrong : wrongQuestions.selectByStudentId(
                actor.tenantId(), student.getId())) {
            if (!Integer.valueOf(1).equals(wrong.getStatus())) continue;
            QuestionBO question = questions.selectById(actor.tenantId(), wrong.getQuestionId());
            if (question == null || !Integer.valueOf(1).equals(question.getStatus())
                    || !"PRACTICE".equals(question.getPurpose())
                    || !"CHOICE".equals(question.getType())
                    || !Objects.equals(student.getGrade(), question.getGrade())) continue;
            visible.add(new WrongPracticeResponse(wrong.getId(), wrong.getQuestionId(),
                    question.getTitle(), wrong.getStudentAnswer(),
                    wrong.getCorrectTimes() == null ? 0 : wrong.getCorrectTimes()));
        }
        return List.copyOf(visible);
    }

    public PracticeCheckResponse check(ActorContext actor, Long id, String answer) {
        QuestionBO question = requireQuestion(actor, id);
        String value = answer == null ? "" : answer.trim().toUpperCase(java.util.Locale.ROOT);
        if (value.isBlank() || options(question.getOptions()).stream()
                .noneMatch(option -> option.value().equals(value))) {
            throw new ServiceException("答案无效");
        }
        return new PracticeCheckResponse(id, value.equalsIgnoreCase(question.getAnswer()),
                question.getAnswer(), question.getAnalysis());
    }

    /** Submits one answer as a trusted, idempotent learning fact. */
    @Transactional(rollbackFor = Exception.class)
    public PracticeSubmissionResponse submit(ActorContext actor, Long id,
            String answer, String submissionKey) {
        StudentBO student = requireStudent(actor);
        QuestionBO question = requireQuestion(actor, id);
        String value = answer == null ? "" : answer.trim().toUpperCase(java.util.Locale.ROOT);
        if (value.isBlank() || options(question.getOptions()).stream()
                .noneMatch(option -> option.value().equals(value))) {
            throw new ServiceException("答案无效");
        }
        if (submissionKey == null || !submissionKey.matches(
                "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-8][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}")) {
            throw new ServiceException("提交标识无效");
        }
        if (!students.lockForStudentWrite(actor.tenantId(), student.getId())) {
            throw new ServiceException("当前学生档案不可用");
        }
        StudyRecordBO existing = records.selectBySubmissionKey(
                actor.tenantId(), student.getId(), submissionKey);
        if (existing != null) {
            if (!id.equals(existing.getQuestionId())
                    || !value.equals(existing.getSelectedAnswer())) {
                throw new ServiceException("提交标识已用于其他答案");
            }
            return submitted(existing);
        }
        boolean correct = value.equalsIgnoreCase(question.getAnswer());
        StudyRecordBO record = new StudyRecordBO();
        record.setStudentId(student.getId());
        record.setKnowledgeId(questions.primaryKnowledgeId(actor.tenantId(), id));
        record.setQuestionId(id);
        record.setRecordType("PRACTICE");
        record.setSubmissionKey(submissionKey);
        record.setSelectedAnswer(value);
        record.setCorrectAnswer(question.getAnswer());
        record.setAnswerAnalysis(question.getAnalysis());
        record.setAbilityDimension(question.getAbilityDimension());
        record.setAnalysisApplied(false);
        record.setScore(correct ? 100D : 0D);
        record.setAccuracy(correct ? 1D : 0D);
        record.setStatus(1);
        records.insert(actor.tenantId(), record);
        updateWrongQuestion(actor, student.getId(), record.getKnowledgeId(), id, value, correct);
        return submitted(record);
    }

    private void updateWrongQuestion(ActorContext actor, Long studentId, Long knowledgeId,
            Long questionId, String answer, boolean correct) {
        WrongQuestionBO wrong = wrongQuestions.selectByStudentAndQuestion(
                actor.tenantId(), studentId, questionId);
        if (wrong == null && correct) return;
        if (wrong == null) {
            wrong = new WrongQuestionBO();
            wrong.setStudentId(studentId);
            wrong.setQuestionId(questionId);
            wrong.setKnowledgeId(knowledgeId);
            wrong.setStudentAnswer(answer);
            wrong.setCorrectTimes(0);
            wrong.setStatus(1);
            wrongQuestions.insert(actor.tenantId(), wrong);
            return;
        }
        wrong.setStudentAnswer(answer);
        wrong.setCorrectTimes(correct ? Math.min(2,
                (wrong.getCorrectTimes() == null ? 0 : wrong.getCorrectTimes()) + 1) : 0);
        wrong.setStatus(correct && wrong.getCorrectTimes() >= 2 ? 0 : 1);
        wrongQuestions.update(actor.tenantId(), wrong);
    }

    private static PracticeSubmissionResponse submitted(StudyRecordBO record) {
        return new PracticeSubmissionResponse(record.getId(), record.getQuestionId(),
                record.getScore() != null && record.getScore() >= 100D,
                record.getCorrectAnswer(), record.getAnswerAnalysis());
    }

    private QuestionBO requireQuestion(ActorContext actor, Long id) {
        StudentBO student = requireStudent(actor);
        QuestionBO question = questions.selectById(actor.tenantId(), id);
        if (question == null || !Integer.valueOf(1).equals(question.getStatus())
                || !"CHOICE".equals(question.getType())
                || !"PRACTICE".equals(question.getPurpose())
                || !student.getGrade().equals(question.getGrade())) {
            throw new ServiceException("练习题不存在");
        }
        return question;
    }

    private StudentBO requireStudent(ActorContext actor) {
        if (actor == null || actor.userId() == null || actor.tenantId() == null) {
            throw new ServiceException("无法识别当前学生");
        }
        StudentBO student = students.selectByUserId(actor.tenantId(), actor.userId().value());
        if (student == null || !Integer.valueOf(1).equals(student.getStatus())) {
            throw new ServiceException("当前账号未关联可用的学生档案");
        }
        return student;
    }

    private static PracticeQuestionResponse visible(QuestionBO question) {
        return new PracticeQuestionResponse(question.getId(), question.getTitle(),
                question.getType(), question.getSubjectCode(), question.getGrade(),
                options(question.getOptions()));
    }

    private static List<ExamPaperResponse.Option> options(String encoded) {
        try {
            List<String> labels = JSON.readValue(encoded, OPTIONS);
            List<ExamPaperResponse.Option> result = new ArrayList<>();
            for (int index = 0; index < labels.size(); index++) {
                result.add(new ExamPaperResponse.Option(String.valueOf((char) ('A' + index)),
                        labels.get(index)));
            }
            return List.copyOf(result);
        } catch (Exception error) {
            throw new ServiceException("题目选项格式无效");
        }
    }
}
