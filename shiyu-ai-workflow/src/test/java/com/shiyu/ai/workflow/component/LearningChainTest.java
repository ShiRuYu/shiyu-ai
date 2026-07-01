package com.shiyu.ai.workflow.component;

import com.shiyu.ai.workflow.context.LearningContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LearningContext 单元测试
 *
 * 验证 LiteFlow learningChain 各 Context 状态的正确性。
 */
class LearningChainTest {

    @Test
    void testLearningContextInitialState() {
        LearningContext ctx = new LearningContext();

        assertNull(ctx.getStudentId());
        assertNull(ctx.getKnowledgeId());
        assertNull(ctx.getKnowledge());
        assertNull(ctx.getPracticeScore());
        assertNull(ctx.getPracticeAccuracy());
        assertNotNull(ctx.getPrerequisites());
        assertNotNull(ctx.getResources());
        assertNotNull(ctx.getPracticeQuestions());
    }

    @Test
    void testLearningContextFullFlow() {
        LearningContext ctx = new LearningContext();

        // 模拟完整学习流程的数据传递
        ctx.setStudentId(1L);
        ctx.setKnowledgeId(100L);
        ctx.setTeachResponse("绝对值是一个数到原点的距离...");
        ctx.setPracticeScore(85.0);
        ctx.setPracticeAccuracy(0.85);

        assertEquals(1L, ctx.getStudentId());
        assertEquals(100L, ctx.getKnowledgeId());
        assertEquals(85.0, ctx.getPracticeScore());
        assertEquals(0.85, ctx.getPracticeAccuracy());
        assertTrue(ctx.getTeachResponse().contains("绝对值"));
    }

    @Test
    void testLearningContextReviewDates() {
        LearningContext ctx = new LearningContext();

        // 验证复习日期
        assertNotNull(ctx.getReviewDates());
        assertTrue(ctx.getReviewDates().isEmpty());
    }
}
