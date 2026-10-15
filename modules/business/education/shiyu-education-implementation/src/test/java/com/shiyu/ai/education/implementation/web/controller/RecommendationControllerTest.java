package com.shiyu.ai.education.implementation.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.education.implementation.application.RecommendationService;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.model.contract.api.ChatEngine;
import com.shiyu.ai.model.contract.model.ChatRequest;
import com.shiyu.ai.model.contract.model.ChatResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

/** 确保混合推荐建议基于当前登录用户，并提供可用的兜底结果。 */
class RecommendationControllerTest {
    private final RecommendationService recommendations = mock(RecommendationService.class);
    private final ChatEngine model = mock(ChatEngine.class);
    private final RecommendationController controller =
            new RecommendationController(recommendations, model);
    private final ActorContext actor =
            new ActorContext(new TenantId(17L), new UserId(23L), false);

    @Test
    void hybridAdviceUsesAuthenticatedModelContextAndMeasuredWeakPointCount() {
        when(recommendations.getWeakKnowledgeIds(actor, 41L)).thenReturn(List.of(3L, 4L));
        when(model.chat(any())).thenReturn(ChatResponse.builder()
                .success(true).content("先巩固有理数").build());

        MockedStatic<ActorContextHttpAdapter> context = actorContext();
        try {
            controller.hybridRecommend(41L);
        } finally {
            context.close();
        }

        ArgumentCaptor<ChatRequest> sent = ArgumentCaptor.forClass(ChatRequest.class);
        verify(model).chat(sent.capture());
        assertEquals(17L, sent.getValue().getTenantId());
        assertEquals(23L, sent.getValue().getUserId());
        String prompt = sent.getValue().getMessages().getFirst().content().getFirst().text();
        assertTrue(prompt.contains("学生 ID: 41"));
        assertTrue(prompt.contains("薄弱知识点数量: 2"));
        verify(recommendations).hybridRecommend(actor, 41L, "先巩固有理数");
    }

    @Test
    void failedModelStillReturnsAdviceBasedOnKnownWeakPoints() {
        when(recommendations.getWeakKnowledgeIds(actor, 41L)).thenReturn(List.of(3L));
        when(model.chat(any())).thenReturn(ChatResponse.builder()
                .success(false).content("不可靠内容").build());

        MockedStatic<ActorContextHttpAdapter> context = actorContext();
        try {
            controller.hybridRecommend(41L);
        } finally {
            context.close();
        }

        ArgumentCaptor<String> advice = ArgumentCaptor.forClass(String.class);
        verify(recommendations).hybridRecommend(eq(actor), eq(41L), advice.capture());
        assertTrue(advice.getValue().contains("1 个薄弱知识点"));
        assertTrue(!advice.getValue().contains("不可靠内容"));
    }

    @Test
    void modelErrorWithNoWeakPointReturnsNeutralAdvice() {
        when(recommendations.getWeakKnowledgeIds(actor, 41L)).thenReturn(List.of());
        when(model.chat(any())).thenThrow(new IllegalStateException("provider unavailable"));

        MockedStatic<ActorContextHttpAdapter> context = actorContext();
        try {
            controller.hybridRecommend(41L);
        } finally {
            context.close();
        }

        ArgumentCaptor<String> advice = ArgumentCaptor.forClass(String.class);
        verify(recommendations).hybridRecommend(eq(actor), eq(41L), advice.capture());
        assertTrue(advice.getValue().contains("暂无薄弱知识点"));
    }

    @Test
    void recommendationQueriesForwardOnlyTheAuthenticatedActor() {
        when(recommendations.recommendKnowledge(actor, 41L, 4)).thenReturn(List.of());
        when(recommendations.recommendQuestions(actor, 41L, 6)).thenReturn(List.of());
        when(recommendations.recommendResources(actor, 41L, 3)).thenReturn(List.of());
        when(recommendations.recommendReviewTasks(actor, 41L, 2)).thenReturn(List.of());

        MockedStatic<ActorContextHttpAdapter> context = actorContext();
        try {
            controller.recommendKnowledge(41L, 4);
            controller.recommendQuestions(41L, 6);
            controller.recommendResources(41L, 3);
            controller.recommendReview(41L, 2);
        } finally {
            context.close();
        }

        verify(recommendations).recommendKnowledge(actor, 41L, 4);
        verify(recommendations).recommendQuestions(actor, 41L, 6);
        verify(recommendations).recommendResources(actor, 41L, 3);
        verify(recommendations).recommendReviewTasks(actor, 41L, 2);
    }

    @Test
    void blankModelAdviceIsNotShownAsARecommendation() {
        when(recommendations.getWeakKnowledgeIds(actor, 41L)).thenReturn(List.of(3L));
        when(model.chat(any())).thenReturn(ChatResponse.builder()
                .success(true).content(" ").build());

        MockedStatic<ActorContextHttpAdapter> context = actorContext();
        try {
            controller.hybridRecommend(41L);
        } finally {
            context.close();
        }

        ArgumentCaptor<String> advice = ArgumentCaptor.forClass(String.class);
        verify(recommendations).hybridRecommend(eq(actor), eq(41L), advice.capture());
        assertTrue(advice.getValue().contains("1 个薄弱知识点"));
    }

    private MockedStatic<ActorContextHttpAdapter> actorContext() {
        MockedStatic<ActorContextHttpAdapter> context =
                mockStatic(ActorContextHttpAdapter.class);
        context.when(ActorContextHttpAdapter::currentActor).thenReturn(actor);
        context.when(ActorContextHttpAdapter::tenantId).thenReturn(17L);
        context.when(ActorContextHttpAdapter::userId).thenReturn(23L);
        return context;
    }
}
