package com.shiyu.ai.workflow.component;

import com.shiyu.ai.workflow.context.LearningContext;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 评分组件
 *
 * 对学生完成练习后的结果进行评分和统计。
 * 当前为占位实现，后续对接 AI 自动批改或人工评分。
 */
@Slf4j
@Component("scoreCmp")
public class ScoreCmp extends NodeComponent {

    @Override
    public void process() throws Exception {
        LearningContext ctx = this.getContextBean(LearningContext.class);
        log.info("ScoreCmp: 评分, studentId={}, knowledgeId={}",
                ctx.getStudentId(), ctx.getKnowledgeId());

        int questionCount = ctx.getPracticeQuestions() != null
                ? ctx.getPracticeQuestions().size() : 0;

        // 当前默认评分（60% 准确率，模拟值）
        // 后续对接实际答题结果
        ctx.setPracticeScore(60.0);
        ctx.setPracticeAccuracy(0.6);

        log.info("ScoreCmp: 评分完成, 题数={}, 得分={}, 准确率={}",
                questionCount, ctx.getPracticeScore(), ctx.getPracticeAccuracy());
    }
}
