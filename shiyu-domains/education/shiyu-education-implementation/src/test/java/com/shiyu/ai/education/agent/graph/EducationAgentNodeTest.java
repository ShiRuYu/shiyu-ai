package com.shiyu.ai.education.agent.graph;

import com.shiyu.ai.education.domain.AbilityValue;
import com.shiyu.ai.education.domain.ReviewScheduler;
import com.shiyu.ai.education.port.repository.ReviewTaskRepository;
import com.shiyu.ai.education.service.AbilityService;
import com.shiyu.ai.education.service.ReviewService;
import com.shiyu.ai.education.domain.model.QuestionBO;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.knowledge.dto.KnowledgeResponse;
import com.shiyu.ai.knowledge.port.KnowledgePathPort;
import com.shiyu.ai.knowledge.port.KnowledgeRelationPort;
import com.shiyu.ai.knowledge.port.KnowledgePointPort;
import com.shiyu.ai.model.chat.ChatEngine;
import com.shiyu.ai.model.chat.ChatResponse;
import com.shiyu.ai.agent.node.NodeInput;
import com.shiyu.ai.agent.node.NodeOutput;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SuppressWarnings("cast")
class EducationAgentNodeTest {

    private static final KnowledgeResponse KNOWLEDGE = new KnowledgeResponse(
            10L, "MATH-ALG", "一元一次方程", "解方程的基本方法", 2,
            "数学", "[]", List.of(), List.of(), List.of());

    @Test
    void practiceNodeCoversValidationDifficultyAndJsonParsing() throws Exception {
        ChatEngine engine = mock(ChatEngine.class);
        TestPracticeNode node = new TestPracticeNode(engine, 2);

        NodeOutput missing = node.execute(NodeInput.fromMap(Map.of()));
        assertFalse(missing.isSuccess());
        assertEquals("缺少 knowledge 上下文", missing.getMsg());

        when(engine.chat(any())).thenReturn(ChatResponse.builder().success(true)
                .content("```json\n{\"type\":\"CHOICE\",\"title\":\"x\",\"options\":[\"A\",\"B\"],\"answer\":\"A\",\"analysis\":\"ok\",\"ability_dimension\":\"apply\"}\nnot-json\n```").build());
        NodeOutput result = node.execute(NodeInput.fromMap(Map.of(
                "knowledge", KNOWLEDGE, "overallScore", 75.0, "tenantId", 1L, "userId", 2L)));
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData("questionCount", -1));
        assertEquals(3, result.getData("difficultyLevel", -1));
        assertTrue(result.getData("practiceDone", false));
        verify(engine).chat(any());

        when(engine.chat(any())).thenReturn(ChatResponse.builder().success(false)
                .errorMessage("down").build());
        NodeOutput failed = node.execute(NodeInput.fromMap(Map.of(
                "knowledge", KNOWLEDGE, "overallScore", 95.0, "practiceCount", 3,
                "tenantId", 1L, "userId", 2L)));
        assertFalse(failed.isSuccess());
        assertEquals(0, failed.getData("questionCount", -1));
        assertEquals(4, failed.getData("difficultyLevel", -1));

        assertThrows(IllegalArgumentException.class, () -> node.execute(NodeInput.fromMap(Map.of(
                "knowledge", KNOWLEDGE, "overallScore", 10.0, "tenantId", 0L, "userId", 2L))));

        when(engine.chat(any())).thenReturn(ChatResponse.builder().success(true)
                .content("{\"type\":\"FILL\",\"title\":\"x\",\"answer\":\"A\"}").build());
        for (double score : new double[]{10.0, 45.0, 70.0, 90.0}) {
            NodeOutput threshold = node.execute(NodeInput.fromMap(Map.of(
                    "knowledge", new KnowledgeResponse(10L, score < 40 ? "MATH" : score < 70 ? "PHYS" : score < 90 ? "ENG" : "CHN", "name", null, 2, "subject", "[]", List.of(), List.of(), List.of()),
                    "overallScore", score, "tenantId", 1L, "userId", 2L)));
            assertTrue(threshold.isSuccess());
            assertEquals(1, threshold.getData("questionCount", 0));
        }
        when(engine.chat(any())).thenReturn(ChatResponse.builder().success(true)
                .content("{\"type\"}\n{\"type\":\"CHOICE\",\"options\":[A}\n{\"type\":\"FILL\",\"title\":\"unterminated}\nnot-json").build());
        NodeOutput malformed = node.execute(NodeInput.fromMap(Map.of(
                "knowledge", new KnowledgeResponse(10L, "XYZ", "name", "", 2, "subject", "[]", List.of(), List.of(), List.of()),
                "overallScore", 45.0, "tenantId", 1L, "userId", 2L)));
        assertTrue(malformed.isSuccess());
        assertEquals(3, malformed.getData("questionCount", 0));
        assertThrows(IllegalArgumentException.class, () -> node.execute(NodeInput.fromMap(Map.of(
                "knowledge", KNOWLEDGE, "overallScore", 45.0, "tenantId", 1L, "userId", 0L))));
    }

    @Test
    void teachNodeCoversMissingSuccessAndFailure() throws Exception {
        ChatEngine engine = mock(ChatEngine.class);
        TestTeachNode node = new TestTeachNode(engine);
        NodeOutput missing = node.execute(NodeInput.fromMap(Map.of()));
        assertFalse(missing.isSuccess());

        AbilityValue ability = new AbilityValue(2L, 10L, 10, 20, 30, 40, 50, 60, null);
        when(engine.chat(any())).thenReturn(ChatResponse.builder().success(true).content("lesson").build());
        NodeOutput success = node.execute(NodeInput.fromMap(Map.of(
                "knowledge", KNOWLEDGE, "prerequisites", List.of(KNOWLEDGE), "ability", ability,
                "tenantId", 1L, "userId", 2L)));
        assertTrue(success.isSuccess());
        assertEquals("lesson", success.getData("teachContent", null));
        assertTrue(success.getData("teachDone", false));

        when(engine.chat(any())).thenReturn(ChatResponse.builder().success(false)
                .errorMessage("unavailable").build());
        NodeOutput failure = node.execute(NodeInput.fromMap(Map.of(
                "knowledge", KNOWLEDGE, "tenantId", 1L, "userId", 2L)));
        assertFalse(failure.isSuccess());
        assertEquals("AI 教学服务暂时不可用，请稍后重试。", failure.getData("teachContent", null));
        assertThrows(IllegalArgumentException.class, () -> node.execute(NodeInput.fromMap(Map.of(
                "knowledge", KNOWLEDGE, "tenantId", 1L, "userId", -1L))));
    }

    @Test
    void prereqNodeReturnsResultsAndDegradesOptionalLookups() throws Exception {
        KnowledgeRelationPort relation = mock(KnowledgeRelationPort.class);
        KnowledgePathPort path = mock(KnowledgePathPort.class);
        TestPrereqCheckNode node = new TestPrereqCheckNode(relation, path);
        ActorContext actor = new ActorContext(new TenantId(1L), new UserId(2L), false);
        when(relation.getPrerequisites(actor, 10L)).thenReturn(List.of(KNOWLEDGE));
        when(path.findMissingPrerequisites(actor, 10L, java.util.Set.of())).thenReturn(List.of(9L));

        NodeOutput result = node.execute(NodeInput.fromMap(Map.of(
                "knowledgeId", 10L, "studentId", 2L, "__knowledgeAccessContext", actor)));
        assertTrue(result.isSuccess());
        assertTrue(result.getData("hasMissingPrereqs", false));
        assertEquals(List.of(9L), result.getData("missingPrerequisiteIds", List.of()));

        when(relation.getPrerequisites(any(), eq(10L))).thenThrow(new RuntimeException("relation"));
        when(path.findMissingPrerequisites(any(), eq(10L), any())).thenThrow(new RuntimeException("path"));
        NodeOutput degraded = node.execute(NodeInput.fromMap(Map.of(
                "knowledgeId", 10L, "__knowledgeAccessContext", actor)));
        assertTrue(degraded.isSuccess());
        assertFalse(degraded.getData("hasMissingPrereqs", true));
        assertFalse(node.execute(NodeInput.fromMap(Map.of())).isSuccess());
        assertThrows(IllegalStateException.class, () -> node.execute(NodeInput.fromMap(Map.of("knowledgeId", 10L))));
    }

    @Test
    void scoreAnalysisCalculatesAnswersAndRequiresActorForMutation() throws Exception {
        AbilityService abilities = mock(AbilityService.class);
        TestScoreAnalysisNode node = new TestScoreAnalysisNode(abilities);
        QuestionBO question = new QuestionBO();
        NodeOutput defaults = node.execute(NodeInput.fromMap(Map.of("practiceQuestions", List.of(question))));
        assertEquals(60.0, defaults.getData("practiceScore", 0.0));
        assertFalse(defaults.getData("reviewNeeded", true));

        ActorContext actor = new ActorContext(new TenantId(1L), new UserId(2L), false);
        NodeOutput analyzed = node.execute(NodeInput.fromMap(Map.of(
                "practiceQuestions", List.of(question),
                "answerResults", List.of(Map.of("correct", true), Map.of("correct", false)),
                "studentId", 2L, "knowledgeId", 10L, "__knowledgeAccessContext", actor)));
        assertEquals(50.0, analyzed.getData("practiceScore", 0.0));
        assertTrue(analyzed.getData("reviewNeeded", false));
        verify(abilities, times(2)).update(eq(actor), eq(2L), eq(10L), any(), anyDouble());
        assertThrows(IllegalStateException.class, () -> node.execute(NodeInput.fromMap(Map.of(
                "studentId", 2L, "knowledgeId", 10L))));
    }

    @Test
    void reviewSchedulePersistsScheduledTasksAndValidatesInputs() throws Exception {
        ReviewTaskRepository repository = mock(ReviewTaskRepository.class);
        TestReviewScheduleNode node = new TestReviewScheduleNode(new ReviewScheduler(), mock(ReviewService.class), repository);
        ActorContext actor = new ActorContext(new TenantId(1L), new UserId(2L), false);
        NodeOutput missing = node.execute(NodeInput.fromMap(Map.of("__knowledgeAccessContext", actor)));
        assertFalse(missing.isSuccess());
        NodeOutput output = node.execute(NodeInput.fromMap(Map.of(
                "studentId", 2L, "knowledgeId", 10L, "__knowledgeAccessContext", actor)));
        assertTrue(output.isSuccess());
        assertTrue(output.getData("reviewCount", 0) > 0);
        verify(repository, atLeastOnce()).insert(eq(new TenantId(1L)), any());
        assertThrows(IllegalStateException.class, () -> node.execute(NodeInput.fromMap(Map.of(
                "studentId", 2L, "knowledgeId", 10L))));
    }

    @Test
    void abilityQueryRequiresActorAndProjectsKnowledgePrerequisitesAndMastery() throws Exception {
        KnowledgePointPort points = mock(KnowledgePointPort.class); KnowledgeRelationPort relations = mock(KnowledgeRelationPort.class); AbilityService abilities = mock(AbilityService.class);
        TestAbilityQueryNode node = new TestAbilityQueryNode(points, relations, abilities);
        assertFalse(node.execute(NodeInput.fromMap(Map.of())).isSuccess());
        ActorContext actor = new ActorContext(new TenantId(1L), new UserId(2L), false);
        when(points.getResponse(actor, 10L)).thenReturn(KNOWLEDGE); when(relations.getPrerequisites(actor, 10L)).thenReturn(List.of()); when(abilities.get(actor, 2L, 10L)).thenReturn(new AbilityValue(2L, 10L, 100, 100, 100, 100, 100, 100, null));
        NodeOutput output = node.execute(NodeInput.fromMap(Map.of("studentId", 2L, "knowledgeId", 10L, "__knowledgeAccessContext", actor)));
        assertTrue(output.isSuccess()); assertEquals(100.0, output.getData("overallScore", 0.0));
        when(abilities.get(actor, 2L, 10L)).thenReturn(null); assertEquals(0.0, node.execute(NodeInput.fromMap(Map.of("studentId", 2L, "knowledgeId", 10L, "__knowledgeAccessContext", actor))).getData("overallScore", -1.0));
        when(points.getResponse(actor, 10L)).thenReturn(null); assertFalse(node.execute(NodeInput.fromMap(Map.of("studentId", 2L, "knowledgeId", 10L, "__knowledgeAccessContext", actor))).isSuccess());
        assertThrows(IllegalStateException.class, () -> node.execute(NodeInput.fromMap(Map.of("studentId", 2L, "knowledgeId", 10L))));
    }

    private static final class TestPracticeNode extends PracticeNode {
        private TestPracticeNode(ChatEngine engine, int count) { super(engine, count); }
        private NodeOutput execute(NodeInput input) throws Exception { return super.doExecute(input); }
    }

    private static final class TestTeachNode extends TeachNode {
        private TestTeachNode(ChatEngine engine) { super(engine); }
        private NodeOutput execute(NodeInput input) throws Exception { return super.doExecute(input); }
    }

    private static final class TestPrereqCheckNode extends PrereqCheckNode {
        private TestPrereqCheckNode(KnowledgeRelationPort relation, KnowledgePathPort path) { super(relation, path); }
        private NodeOutput execute(NodeInput input) throws Exception { return super.doExecute(input); }
    }

    private static final class TestScoreAnalysisNode extends ScoreAnalysisNode {
        private TestScoreAnalysisNode(AbilityService service) { super(service); }
        private NodeOutput execute(NodeInput input) throws Exception { return super.doExecute(input); }
    }

    private static final class TestReviewScheduleNode extends ReviewScheduleNode {
        private TestReviewScheduleNode(ReviewScheduler scheduler, ReviewService service, ReviewTaskRepository repository) {
            super(scheduler, service, repository);
        }
        private NodeOutput execute(NodeInput input) throws Exception { return super.doExecute(input); }
    }

    private static final class TestAbilityQueryNode extends AbilityQueryNode {
        private TestAbilityQueryNode(KnowledgePointPort points, KnowledgeRelationPort relations, AbilityService abilities) { super(points, relations, abilities); }
        private NodeOutput execute(NodeInput input) throws Exception { return super.doExecute(input); }
    }
}
