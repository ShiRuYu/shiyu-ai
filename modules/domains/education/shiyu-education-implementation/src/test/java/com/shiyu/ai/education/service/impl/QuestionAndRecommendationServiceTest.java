package com.shiyu.ai.education.service.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.education.domain.model.AbilityBO;
import com.shiyu.ai.education.domain.model.QuestionBO;
import com.shiyu.ai.education.dto.QuestionResponse;
import com.shiyu.ai.education.dto.ReviewTaskResponse;
import com.shiyu.ai.education.port.repository.AbilityRepository;
import com.shiyu.ai.education.port.repository.QuestionRepository;
import com.shiyu.ai.education.request.QuestionRequest;
import com.shiyu.ai.education.service.QuestionService;
import com.shiyu.ai.education.service.ReviewService;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"rawtypes", "unchecked"})
class QuestionAndRecommendationServiceTest {
    private static final ActorContext ACTOR = new ActorContext(new TenantId(31), new UserId(32), false);

    @Test
    void questionServiceScopesAllReadsAndWritesToTenant() {
        QuestionRepository repository = mock(QuestionRepository.class);
        QuestionServiceImpl service = new QuestionServiceImpl(repository);
        QuestionBO question = new QuestionBO(); question.setId(9L); question.setStatus(1);
        QuestionResponse response = new QuestionResponse(9L, "q", "SINGLE", "math", 5, 2, null, "title", null, "A", null, null, 0L);
        QuestionRequest request = new QuestionRequest();
        request.setId(9L); request.setCode("q"); request.setType("SINGLE"); request.setSubjectCode("math"); request.setGrade(5);
        request.setDifficulty(2); request.setTitle("title"); request.setAnswer("A");
        when(repository.selectById(ACTOR.tenantId(), 9L)).thenReturn(question);
        when(repository.selectByCode(ACTOR.tenantId(), "q")).thenReturn(question);
        when(repository.selectBySubjectAndGrade(ACTOR.tenantId(), "math", 5)).thenReturn(List.of(question));
        when(repository.selectByDifficulty(ACTOR.tenantId(), 2)).thenReturn(List.of(question));
        when(repository.selectByType(ACTOR.tenantId(), "SINGLE")).thenReturn(List.of(question));
        when(repository.selectPage(ACTOR.tenantId(), 1, 10)).thenReturn(new PageData<>(List.of(question), 1));
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(any(QuestionBO.class), eq(QuestionResponse.class))).thenReturn(response);
            mapper.when(() -> MapstructUtils.convert(any(List.class), eq(QuestionResponse.class))).thenReturn(List.of(response));
            assertEquals(response, service.getById(ACTOR, 9L));
            assertEquals(response, service.getByCode(ACTOR, "q"));
            assertEquals(1, service.listBySubjectAndGrade(ACTOR, "math", 5).size());
            assertEquals(1, service.listByDifficulty(ACTOR, 2).size());
            assertEquals(1, service.listByType(ACTOR, "SINGLE").size());
            assertEquals(1, service.page(ACTOR, 1, 10).getTotal());
            assertEquals(response, service.create(ACTOR, request));
        }
        when(repository.selectById(ACTOR.tenantId(), 9L)).thenReturn(question).thenReturn(null);
        request.setStatus(2); service.update(ACTOR, request);
        service.update(ACTOR, request);
        service.incrementUsedCount(ACTOR, 9L); service.deleteById(ACTOR, 9L);
        verify(repository).insert(eq(ACTOR.tenantId()), any(QuestionBO.class));
        verify(repository).update(eq(ACTOR.tenantId()), any(QuestionBO.class));
        verify(repository).incrementUsedCount(ACTOR.tenantId(), 9L);
        verify(repository).deleteById(ACTOR.tenantId(), 9L);
    }

    @Test
    void recommendationServiceRanksWeakKnowledgeQuestionsAndReviews() {
        AbilityRepository abilities = mock(AbilityRepository.class);
        QuestionService questions = mock(QuestionService.class);
        ReviewService reviews = mock(ReviewService.class);
        RecommendationServiceImpl service = new RecommendationServiceImpl(abilities, questions, reviews);
        AbilityBO weak = new AbilityBO(); weak.setKnowledgeId(101L); weak.setOverallMastery(30D);
        AbilityBO medium = new AbilityBO(); medium.setKnowledgeId(102L); medium.setOverallMastery(50D);
        AbilityBO strong = new AbilityBO(); strong.setKnowledgeId(103L); strong.setOverallMastery(80D);
        AbilityBO unknown = new AbilityBO(); unknown.setKnowledgeId(104L); unknown.setOverallMastery(null);
        when(abilities.selectByStudent(ACTOR.tenantId(), 7L)).thenReturn(List.of(strong, medium, weak, unknown));
        QuestionResponse question = new QuestionResponse(1L, "q", "SINGLE", "math", 5, 1, null, "title", null, "A", null, null, 0L);
        when(questions.listByDifficulty(ACTOR, 1)).thenReturn(List.of(question));
        when(questions.listByDifficulty(ACTOR, 2)).thenReturn(List.of(question));
        ReviewTaskResponse review = new ReviewTaskResponse(1L, 7L, 101L, "math", 1, "today", 0, "due", null, LocalDateTime.now());
        when(reviews.listTodayTasks(ACTOR, 7L)).thenReturn(List.of(review));

        assertEquals(List.of(weak, medium).stream().map(AbilityBO::getKnowledgeId).toList(),
                service.recommendKnowledge(ACTOR, 7L, 5).stream().map(x -> x.knowledgeId()).toList());
        assertEquals(2, service.recommendQuestions(ACTOR, 7L, 2).size());
        assertTrue(service.recommendResources(ACTOR, 7L, 5).isEmpty());
        assertEquals(1, service.recommendReviewTasks(ACTOR, 7L, 5).size());
        assertEquals(2, service.getWeakKnowledgeIds(ACTOR, 7L).size());
        assertEquals(2, service.hybridRecommend(ACTOR, 7L, "keep going").knowledgeTop().size());
        verify(abilities, atLeastOnce()).selectByStudent(ACTOR.tenantId(), 7L);
    }
}
