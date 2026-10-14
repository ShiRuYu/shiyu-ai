package com.shiyu.ai.education.implementation.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shiyu.ai.common.foundation.exception.ServiceException;
import com.shiyu.ai.common.foundation.api.PageData;
import com.shiyu.ai.common.foundation.utils.MapstructUtils;
import com.shiyu.ai.education.implementation.domain.model.CourseBO;
import com.shiyu.ai.education.implementation.domain.model.CourseChapterBO;
import com.shiyu.ai.education.implementation.domain.model.CourseSectionBO;
import com.shiyu.ai.education.implementation.domain.model.QuestionBO;
import com.shiyu.ai.education.implementation.domain.model.StudentBO;
import com.shiyu.ai.education.implementation.domain.model.StudyRecordBO;
import com.shiyu.ai.education.implementation.domain.model.WrongQuestionBO;
import com.shiyu.ai.education.implementation.web.dto.CourseResponse;
import com.shiyu.ai.education.implementation.domain.port.repository.CourseChapterRepository;
import com.shiyu.ai.education.implementation.domain.port.repository.CourseRepository;
import com.shiyu.ai.education.implementation.domain.port.repository.CourseSectionRepository;
import com.shiyu.ai.education.implementation.domain.port.repository.QuestionRepository;
import com.shiyu.ai.education.implementation.domain.port.repository.StudentRepository;
import com.shiyu.ai.education.implementation.domain.port.repository.StudyRecordRepository;
import com.shiyu.ai.education.implementation.domain.port.repository.WrongQuestionRepository;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/** Verifies the practice answer boundary for the authenticated student. */
class StudentLearningServiceTest {
    private static final ActorContext ACTOR =
            new ActorContext(new TenantId(9L), new UserId(37L), false);
    private final QuestionRepository questions = mock(QuestionRepository.class);
    private final CourseRepository courses = mock(CourseRepository.class);
    private final CourseChapterRepository chapters = mock(CourseChapterRepository.class);
    private final CourseSectionRepository sections = mock(CourseSectionRepository.class);
    private final StudentRepository students = mock(StudentRepository.class);
    private final StudyRecordRepository records = mock(StudyRecordRepository.class);
    private final WrongQuestionRepository wrong = mock(WrongQuestionRepository.class);
    private final StudentLearningService service = new StudentLearningService(
            courses, chapters, sections, questions, students, records, wrong);

    @Test
    void catalogUsesTheCurrentTenantAndRejectsInvalidPagination() {
        arrangeStudent();
        CourseBO course = new CourseBO();
        course.setId(51L);
        course.setName("七年级数学");
        course.setStatus(1);
        when(courses.selectCatalogPage(ACTOR.tenantId(), "MATH", 7, 2, 10))
                .thenReturn(new PageData<>(List.of(course), 21L));

        try (MockedStatic<MapstructUtils> mapping = mockStatic(MapstructUtils.class)) {
            mapping.when(() -> MapstructUtils.convert(List.of(course), CourseResponse.class))
                    .thenReturn(List.of(response(course)));
            var page = service.courses(ACTOR, "MATH", 7, 2, 10);
            assertEquals(21L, page.getTotal());
            assertEquals(51L, page.getItems().getFirst().id());
        }
        assertThrows(ServiceException.class, () -> service.courses(ACTOR, "MATH", 7, 0, 10));
        assertThrows(ServiceException.class, () -> service.courses(ACTOR, "MATH", 7, 1, 101));
        verify(courses, never()).selectCatalogPage(ACTOR.tenantId(), "MATH", 7, 0, 10);
    }

    @Test
    void courseDetailExcludesUnpublishedChaptersAndSections() {
        arrangeStudent();
        CourseBO course = new CourseBO();
        course.setId(51L);
        course.setName("七年级数学");
        course.setStatus(1);
        CourseChapterBO published = chapter(61L, 1);
        CourseChapterBO draft = chapter(62L, 0);
        CourseSectionBO lesson = section(71L, 61L, 1);
        CourseSectionBO hiddenLesson = section(72L, 61L, 0);
        CourseSectionBO draftChapterLesson = section(73L, 62L, 1);
        when(courses.selectById(ACTOR.tenantId(), 51L)).thenReturn(course);
        when(chapters.selectByCourseId(ACTOR.tenantId(), 51L))
                .thenReturn(List.of(published, draft));
        when(sections.selectByChapterIds(ACTOR.tenantId(), List.of(61L)))
                .thenReturn(List.of(lesson, hiddenLesson, draftChapterLesson));

        try (MockedStatic<MapstructUtils> mapping = mockStatic(MapstructUtils.class)) {
            mapping.when(() -> MapstructUtils.convert(course, CourseResponse.class))
                    .thenReturn(response(course));
            var detail = service.course(ACTOR, 51L);
            assertEquals(List.of(61L), detail.chapters().stream()
                    .map(item -> item.id()).toList());
            assertEquals(List.of(71L), detail.chapters().getFirst().sections().stream()
                    .map(item -> item.id()).toList());
        }
        verify(sections).selectByChapterIds(ACTOR.tenantId(), List.of(61L));
    }

    @Test
    void unpublishedCourseAndEmptyChapterCatalogExposeNoContent() {
        arrangeStudent();
        CourseBO unpublished = new CourseBO();
        unpublished.setId(51L);
        unpublished.setStatus(0);
        when(courses.selectById(ACTOR.tenantId(), 51L)).thenReturn(unpublished);
        assertThrows(ServiceException.class, () -> service.course(ACTOR, 51L));

        unpublished.setStatus(1);
        when(chapters.selectByCourseId(ACTOR.tenantId(), 51L))
                .thenReturn(List.of(chapter(61L, 0)));
        try (MockedStatic<MapstructUtils> mapping = mockStatic(MapstructUtils.class)) {
            mapping.when(() -> MapstructUtils.convert(unpublished, CourseResponse.class))
                    .thenReturn(response(unpublished));
            assertTrue(service.course(ACTOR, 51L).chapters().isEmpty());
        }
        verify(sections, never()).selectByChapterIds(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void confidentialExamQuestionIsInvisibleToAllPracticeOperations() {
        arrangeStudent();
        QuestionBO secret = question("EXAM_PRIVATE");
        when(questions.selectById(ACTOR.tenantId(), 30L)).thenReturn(secret);
        when(questions.selectBySubjectAndGrade(ACTOR.tenantId(), "MATH", 7))
                .thenReturn(List.of(secret));

        assertEquals(List.of(), service.questions(ACTOR, "MATH", null));
        assertThrows(ServiceException.class, () -> service.question(ACTOR, 30L));
        assertThrows(ServiceException.class, () -> service.check(ACTOR, 30L, "A"));
    }

    @Test
    void ordinaryPracticeQuestionStillReturnsItsAnswerAfterSubmission() {
        arrangeStudent();
        when(questions.selectById(ACTOR.tenantId(), 30L))
                .thenReturn(question("PRACTICE"));
        assertEquals("B", service.check(ACTOR, 30L, "A").correctAnswer());
    }

    @Test
    void practiceChoiceNormalizationDoesNotDependOnServerLocale() {
        arrangeStudent();
        QuestionBO practice = question("PRACTICE");
        practice.setOptions("[\"A\",\"B\",\"C\",\"D\",\"E\",\"F\",\"G\",\"H\",\"I\"]");
        practice.setAnswer("I");
        when(questions.selectById(ACTOR.tenantId(), 30L)).thenReturn(practice);
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertTrue(service.check(ACTOR, 30L, "i").correct());
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    void submittingPracticePersistsOneTrustedRecordAndWrongQuestion() {
        arrangeStudent();
        when(questions.selectById(ACTOR.tenantId(), 30L)).thenReturn(question("PRACTICE"));
        when(students.lockForStudentWrite(ACTOR.tenantId(), 4L)).thenReturn(true);
        when(questions.primaryKnowledgeId(ACTOR.tenantId(), 30L)).thenReturn(1001L);
        String key = "94bd0973-cb05-434f-9113-d5a62845671b";

        var result = service.submit(ACTOR, 30L, "A", key);

        assertTrue(!result.correct());
        verify(records).insert(org.mockito.ArgumentMatchers.eq(ACTOR.tenantId()),
                org.mockito.ArgumentMatchers.argThat(record ->
                        "PRACTICE".equals(record.getRecordType())
                        && record.getStudentId().equals(4L)
                        && record.getKnowledgeId().equals(1001L)
                        && record.getScore().equals(0D)
                        && key.equals(record.getSubmissionKey())));
        verify(wrong).insert(org.mockito.ArgumentMatchers.eq(ACTOR.tenantId()),
                org.mockito.ArgumentMatchers.argThat(item ->
                        item.getStudentId().equals(4L)
                        && item.getQuestionId().equals(30L)
                        && "A".equals(item.getStudentAnswer())));
    }

    @Test
    void retryWithSameKeyReturnsOriginalResultWithoutNewWrites() {
        arrangeStudent();
        when(questions.selectById(ACTOR.tenantId(), 30L)).thenReturn(question("PRACTICE"));
        when(students.lockForStudentWrite(ACTOR.tenantId(), 4L)).thenReturn(true);
        StudyRecordBO existing = new StudyRecordBO();
        existing.setId(71L);
        existing.setQuestionId(30L);
        existing.setSelectedAnswer("A");
        existing.setCorrectAnswer("B");
        existing.setAnswerAnalysis("Explanation");
        existing.setScore(0D);
        when(records.selectBySubmissionKey(ACTOR.tenantId(), 4L,
                "94bd0973-cb05-434f-9113-d5a62845671b")).thenReturn(existing);

        var result = service.submit(ACTOR, 30L, "A",
                "94bd0973-cb05-434f-9113-d5a62845671b");
        assertEquals(71L, result.recordId());
        assertEquals("Explanation", result.analysis());
        verify(records, never()).insert(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
        verify(wrong, never()).insert(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void reusedSubmissionKeyCannotChangeTheQuestionOrAnswer() {
        arrangeStudent();
        when(questions.selectById(ACTOR.tenantId(), 30L)).thenReturn(question("PRACTICE"));
        when(students.lockForStudentWrite(ACTOR.tenantId(), 4L)).thenReturn(true);
        StudyRecordBO original = new StudyRecordBO();
        original.setQuestionId(30L);
        original.setSelectedAnswer("A");
        String key = "94bd0973-cb05-434f-9113-d5a62845671b";
        when(records.selectBySubmissionKey(ACTOR.tenantId(), 4L, key)).thenReturn(original);

        assertThrows(ServiceException.class, () -> service.submit(ACTOR, 30L, "B", key));
        verify(records, never()).insert(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void twoCorrectRetriesResolveAnExistingWrongQuestion() {
        arrangeStudent();
        when(questions.selectById(ACTOR.tenantId(), 30L)).thenReturn(question("PRACTICE"));
        when(students.lockForStudentWrite(ACTOR.tenantId(), 4L)).thenReturn(true);
        WrongQuestionBO existing = new WrongQuestionBO();
        existing.setId(11L);
        existing.setCorrectTimes(1);
        existing.setStatus(1);
        when(wrong.selectByStudentAndQuestion(ACTOR.tenantId(), 4L, 30L))
                .thenReturn(existing);

        var result = service.submit(ACTOR, 30L, "B",
                "94bd0973-cb05-434f-9113-d5a62845671b");

        assertTrue(result.correct());
        assertEquals(2, existing.getCorrectTimes());
        assertEquals(0, existing.getStatus());
        verify(wrong).update(ACTOR.tenantId(), existing);
    }

    @Test
    void confidentialQuestionCannotCreatePracticeRecord() {
        arrangeStudent();
        when(questions.selectById(ACTOR.tenantId(), 30L)).thenReturn(question("EXAM_PRIVATE"));
        assertThrows(ServiceException.class, () -> service.submit(ACTOR, 30L, "A",
                "94bd0973-cb05-434f-9113-d5a62845671b"));
        verify(records, never()).insert(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void malformedSubmissionKeyCannotCreateAPracticeFact() {
        arrangeStudent();
        when(questions.selectById(ACTOR.tenantId(), 30L)).thenReturn(question("PRACTICE"));

        assertThrows(ServiceException.class,
                () -> service.submit(ACTOR, 30L, "A", "not-a-uuid"));
        verify(students, never()).lockForStudentWrite(ACTOR.tenantId(), 4L);
        verify(records, never()).insert(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void firstCorrectRetryKeepsWrongQuestionActiveUntilItIsRepeated() {
        arrangeStudent();
        when(questions.selectById(ACTOR.tenantId(), 30L)).thenReturn(question("PRACTICE"));
        when(students.lockForStudentWrite(ACTOR.tenantId(), 4L)).thenReturn(true);
        WrongQuestionBO existing = new WrongQuestionBO();
        existing.setId(11L);
        existing.setCorrectTimes(0);
        existing.setStatus(1);
        when(wrong.selectByStudentAndQuestion(ACTOR.tenantId(), 4L, 30L))
                .thenReturn(existing);

        service.submit(ACTOR, 30L, "B", "94bd0973-cb05-434f-9113-d5a62845671b");

        assertEquals(1, existing.getCorrectTimes());
        assertEquals(1, existing.getStatus());
    }

    @Test
    void incorrectRetryResetsWrongQuestionCorrectionProgress() {
        arrangeStudent();
        when(questions.selectById(ACTOR.tenantId(), 30L)).thenReturn(question("PRACTICE"));
        when(students.lockForStudentWrite(ACTOR.tenantId(), 4L)).thenReturn(true);
        WrongQuestionBO existing = new WrongQuestionBO();
        existing.setId(11L);
        existing.setCorrectTimes(1);
        existing.setStatus(1);
        when(wrong.selectByStudentAndQuestion(ACTOR.tenantId(), 4L, 30L))
                .thenReturn(existing);

        service.submit(ACTOR, 30L, "A", "94bd0973-cb05-434f-9113-d5a62845671b");

        assertEquals(0, existing.getCorrectTimes());
        assertEquals(1, existing.getStatus());
    }

    @Test
    void practiceListDoesNotExposeQuestionsFromAnotherGrade() {
        arrangeStudent();

        assertEquals(List.of(), service.questions(ACTOR, "MATH", 8));
        verify(questions, never()).selectBySubjectAndGrade(ACTOR.tenantId(), "MATH", 8);
    }

    @Test
    void practiceListPublishesOnlyCurrentGradeActiveChoicePracticeQuestions() {
        arrangeStudent();
        QuestionBO visible = question("PRACTICE");
        QuestionBO inactive = question("PRACTICE");
        inactive.setStatus(0);
        QuestionBO essay = question("PRACTICE");
        essay.setType("ESSAY");
        QuestionBO privateExam = question("EXAM_PRIVATE");
        when(questions.selectBySubjectAndGrade(ACTOR.tenantId(), "MATH", 7))
                .thenReturn(List.of(visible, inactive, essay, privateExam));

        var published = service.questions(ACTOR, "MATH", null);

        assertEquals(1, published.size());
        assertEquals(30L, published.getFirst().id());
        assertEquals(List.of("A", "B"), published.getFirst().options().stream()
                .map(option -> option.value()).toList());
        assertThrows(ServiceException.class, () -> service.questions(ACTOR, " ", null));
    }

    @Test
    void correctFirstSubmissionCreatesNoWrongQuestionHistory() {
        arrangeStudent();
        when(questions.selectById(ACTOR.tenantId(), 30L)).thenReturn(question("PRACTICE"));
        when(students.lockForStudentWrite(ACTOR.tenantId(), 4L)).thenReturn(true);

        var result = service.submit(ACTOR, 30L, "B",
                "94bd0973-cb05-434f-9113-d5a62845671b");

        assertTrue(result.correct());
        verify(wrong, never()).insert(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
        verify(wrong, never()).update(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void replayKeyCannotBeReusedForADifferentQuestion() {
        arrangeStudent();
        QuestionBO second = question("PRACTICE");
        second.setId(31L);
        when(questions.selectById(ACTOR.tenantId(), 31L)).thenReturn(second);
        when(students.lockForStudentWrite(ACTOR.tenantId(), 4L)).thenReturn(true);
        StudyRecordBO original = new StudyRecordBO();
        original.setQuestionId(30L);
        original.setSelectedAnswer("B");
        String key = "94bd0973-cb05-434f-9113-d5a62845671b";
        when(records.selectBySubmissionKey(ACTOR.tenantId(), 4L, key)).thenReturn(original);

        assertThrows(ServiceException.class, () -> service.submit(ACTOR, 31L, "B", key));
        verify(records, never()).insert(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void wrongListUsesCurrentStudentAndHidesPrivateQuestions() {
        arrangeStudent();
        WrongQuestionBO publicWrong = new WrongQuestionBO();
        publicWrong.setId(11L);
        publicWrong.setQuestionId(30L);
        publicWrong.setStatus(1);
        publicWrong.setStudentAnswer("A");
        WrongQuestionBO privateWrong = new WrongQuestionBO();
        privateWrong.setId(12L);
        privateWrong.setQuestionId(31L);
        privateWrong.setStatus(1);
        when(wrong.selectByStudentId(ACTOR.tenantId(), 4L))
                .thenReturn(List.of(publicWrong, privateWrong));
        when(questions.selectById(ACTOR.tenantId(), 30L)).thenReturn(question("PRACTICE"));
        when(questions.selectById(ACTOR.tenantId(), 31L)).thenReturn(question("EXAM_PRIVATE"));

        assertEquals(List.of(30L), service.wrongQuestions(ACTOR).stream()
                .map(item -> item.questionId()).toList());
        verify(wrong).selectByStudentId(ACTOR.tenantId(), 4L);
    }

    @Test
    void wrongListExcludesQuestionsOutsideTheStudentsCurrentPracticeGrade() {
        arrangeStudent();
        WrongQuestionBO previousGrade = new WrongQuestionBO();
        previousGrade.setId(13L);
        previousGrade.setQuestionId(30L);
        previousGrade.setStatus(1);
        when(wrong.selectByStudentId(ACTOR.tenantId(), 4L))
                .thenReturn(List.of(previousGrade));
        QuestionBO stale = question("PRACTICE");
        stale.setGrade(8);
        when(questions.selectById(ACTOR.tenantId(), 30L)).thenReturn(stale);

        assertEquals(List.of(), service.wrongQuestions(ACTOR));
    }

    @Test
    void wrongListExcludesQuestionsThatAreNoLongerChoices() {
        arrangeStudent();
        WrongQuestionBO changedType = new WrongQuestionBO();
        changedType.setId(14L);
        changedType.setQuestionId(30L);
        changedType.setStatus(1);
        when(wrong.selectByStudentId(ACTOR.tenantId(), 4L))
                .thenReturn(List.of(changedType));
        QuestionBO stale = question("PRACTICE");
        stale.setType("ESSAY");
        when(questions.selectById(ACTOR.tenantId(), 30L)).thenReturn(stale);

        assertEquals(List.of(), service.wrongQuestions(ACTOR));
    }

    @Test
    void inactiveOrDeletedWrongQuestionNeverAppearsInStudentReview() {
        arrangeStudent();
        WrongQuestionBO retired = new WrongQuestionBO();
        retired.setId(15L);
        retired.setQuestionId(30L);
        retired.setStatus(0);
        WrongQuestionBO deletedSource = new WrongQuestionBO();
        deletedSource.setId(16L);
        deletedSource.setQuestionId(31L);
        deletedSource.setStatus(1);
        when(wrong.selectByStudentId(ACTOR.tenantId(), 4L))
                .thenReturn(List.of(retired, deletedSource));

        assertEquals(List.of(), service.wrongQuestions(ACTOR));
        verify(questions, never()).selectById(ACTOR.tenantId(), 30L);
        verify(questions).selectById(ACTOR.tenantId(), 31L);
    }

    @Test
    void wrongQuestionWithoutCorrectionHistoryStartsAtZero() {
        arrangeStudent();
        WrongQuestionBO item = new WrongQuestionBO();
        item.setId(15L);
        item.setQuestionId(30L);
        item.setStatus(1);
        when(wrong.selectByStudentId(ACTOR.tenantId(), 4L)).thenReturn(List.of(item));
        when(questions.selectById(ACTOR.tenantId(), 30L)).thenReturn(question("PRACTICE"));

        assertEquals(0, service.wrongQuestions(ACTOR).getFirst().correctTimes());
    }

    @Test
    void invalidPracticeAnswerAndLockedStudentCannotCreateLearningFacts() {
        arrangeStudent();
        when(questions.selectById(ACTOR.tenantId(), 30L)).thenReturn(question("PRACTICE"));

        assertThrows(ServiceException.class, () -> service.check(ACTOR, 30L, " "));
        assertThrows(ServiceException.class, () -> service.submit(ACTOR, 30L, "C",
                "94bd0973-cb05-434f-9113-d5a62845671b"));
        assertThrows(ServiceException.class, () -> service.submit(ACTOR, 30L, "A",
                "94bd0973-cb05-434f-9113-d5a62845671b"));
        verify(records, never()).insert(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void signedOutOrInactiveStudentCannotReadPracticeContent() {
        assertThrows(ServiceException.class, () -> service.questions(null, "MATH", 7));
        StudentBO inactive = new StudentBO();
        inactive.setId(4L);
        inactive.setStatus(0);
        when(students.selectByUserId(ACTOR.tenantId(), 37L)).thenReturn(inactive);
        assertThrows(ServiceException.class, () -> service.question(ACTOR, 30L));
        verify(questions, never()).selectById(ACTOR.tenantId(), 30L);
    }

    private void arrangeStudent() {
        StudentBO student = new StudentBO();
        student.setId(4L);
        student.setGrade(7);
        student.setStatus(1);
        when(students.selectByUserId(ACTOR.tenantId(), 37L)).thenReturn(student);
    }

    private static QuestionBO question(String purpose) {
        QuestionBO question = new QuestionBO();
        question.setId(30L);
        question.setType("CHOICE");
        question.setSubjectCode("MATH");
        question.setGrade(7);
        question.setStatus(1);
        question.setOptions("[\"one\",\"two\"]");
        question.setAnswer("B");
        question.setPurpose(purpose);
        return question;
    }

    private static CourseChapterBO chapter(Long id, int status) {
        CourseChapterBO chapter = new CourseChapterBO();
        chapter.setId(id);
        chapter.setCourseId(51L);
        chapter.setName("章节" + id);
        chapter.setOrderNo(id.intValue());
        chapter.setStatus(status);
        return chapter;
    }

    private static CourseSectionBO section(Long id, Long chapterId, int status) {
        CourseSectionBO section = new CourseSectionBO();
        section.setId(id);
        section.setChapterId(chapterId);
        section.setName("课时" + id);
        section.setOrderNo(id.intValue());
        section.setStatus(status);
        return section;
    }

    private static CourseResponse response(CourseBO course) {
        return new CourseResponse(course.getId(), course.getName(), null, null, null,
                null, null, null, null, course.getStatus());
    }
}
