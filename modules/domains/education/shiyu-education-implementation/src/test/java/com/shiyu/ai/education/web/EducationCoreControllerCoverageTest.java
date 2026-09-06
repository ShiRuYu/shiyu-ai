package com.shiyu.ai.education.web;

import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.education.request.ChapterRequest;
import com.shiyu.ai.education.request.CourseRequest;
import com.shiyu.ai.education.request.ExamRequest;
import com.shiyu.ai.education.request.ResourceRequest;
import com.shiyu.ai.education.request.StudyRecordRequest;
import com.shiyu.ai.education.request.StudentRequest;
import com.shiyu.ai.education.request.SubjectRequest;
import com.shiyu.ai.education.request.TextbookRequest;
import com.shiyu.ai.education.service.AnalyticsService;
import com.shiyu.ai.education.service.ChapterService;
import com.shiyu.ai.education.service.CourseService;
import com.shiyu.ai.education.service.ExamService;
import com.shiyu.ai.education.service.ResourceService;
import com.shiyu.ai.education.service.StudentService;
import com.shiyu.ai.education.service.SubjectService;
import com.shiyu.ai.education.service.TextbookService;
import com.shiyu.ai.kernel.context.ActorContext;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Exercises the concrete education HTTP facades with typed actor context.
 * This deliberately calls named endpoints rather than discovering methods
 * reflectively, so a changed endpoint signature cannot silently pass.
 */
class EducationCoreControllerCoverageTest {

    @Test
    void delegatesCourseExamAnalyticsAndChapterEndpoints() {
        ActorContext actorContext = mock(ActorContext.class);
        CourseService courseService = mock(CourseService.class);
        ExamService examService = mock(ExamService.class);
        AnalyticsService analyticsService = mock(AnalyticsService.class);
        ChapterService chapterService = mock(ChapterService.class);
        StudentService studentService = mock(StudentService.class);
        SubjectService subjectService = mock(SubjectService.class);
        TextbookService textbookService = mock(TextbookService.class);
        ResourceService resourceService = mock(ResourceService.class);

        CourseController course = new CourseController(courseService);
        ExamController exam = new ExamController(examService);
        AnalyticsController analytics = new AnalyticsController(analyticsService);
        ChapterController chapter = new ChapterController(chapterService);
        StudentController student = new StudentController(studentService);
        SubjectController subject = new SubjectController(subjectService);
        TextbookController textbook = new TextbookController(textbookService);
        ResourceController resource = new ResourceController(resourceService);

        when(analyticsService.listRecordsByStudent(actorContext, 11L)).thenReturn(List.of());
        when(analyticsService.listRecordsByStudentAndKnowledge(actorContext, 11L, 22L)).thenReturn(List.of());
        when(analyticsService.getWeakPoints(actorContext, 11L)).thenReturn(List.of());
        when(analyticsService.getTrend(actorContext, 11L)).thenReturn(null);
        when(chapterService.listKnowledgeIds(actorContext, 3L)).thenReturn(List.of());

        try (MockedStatic<ActorContextHttpAdapter> context = mockStatic(ActorContextHttpAdapter.class)) {
            context.when(ActorContextHttpAdapter::currentActor).thenReturn(actorContext);

            assertDoesNotThrow(() -> {
                course.getById(1L);
                course.list(1, 10);
                course.listBySubjectCode("math");
                course.listByGrade(7);
                course.create(new CourseRequest());
                course.update(2L, new CourseRequest());
                course.startLearning(3L, 4L);
                course.delete(5L);

                exam.getById(1L);
                exam.list(1, 10);
                exam.listBySubjectCode("math");
                exam.listByTeacherId(9L);
                exam.create(new ExamRequest());
                exam.update(2L, new ExamRequest());
                exam.delete(5L);

                analytics.listRecordsByStudent(11L);
                analytics.listRecordsByStudentAndKnowledge(11L, 22L);
                analytics.createRecord(new StudyRecordRequest());
                analytics.getAbilityRadar(11L, 22L);
                analytics.getOverview(11L);
                analytics.getWeakPoints(11L);
                analytics.getTrend(11L);

                chapter.getById(1L);
                chapter.listByTextbookId(2L);
                chapter.getChapterTree(2L);
                chapter.listByParentId(3L);
                chapter.create(new ChapterRequest());
                chapter.update(4L, new ChapterRequest());
                chapter.delete(5L);
                chapter.listKnowledgeIds(3L);
                chapter.replaceKnowledgeIds(3L, List.of(7L));

                student.list(1, 10);
                student.getById(1L);
                student.getByUserId(2L);
                student.create(new StudentRequest());
                student.update(3L, new StudentRequest());
                student.delete(4L);

                subject.getById(1L);
                subject.getByCode("math");
                subject.list(1, 10);
                subject.listByGradeLevel("middle");
                subject.create(new SubjectRequest());
                subject.update(2L, new SubjectRequest());
                subject.delete(3L);

                textbook.getById(1L);
                textbook.list(1, 10);
                textbook.listBySubjectAndGrade("math", 7);
                textbook.create(new TextbookRequest());
                textbook.update(2L, new TextbookRequest());
                textbook.delete(3L);

                resource.getById(1L);
                resource.list(1, 10);
                resource.listBySubjectCode("math");
                resource.listByType("video");
                resource.create(new ResourceRequest());
                resource.update(2L, new ResourceRequest());
                resource.delete(3L);
            });
        }
    }
}
