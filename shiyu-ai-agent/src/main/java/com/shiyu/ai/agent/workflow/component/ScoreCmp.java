package com.shiyu.ai.agent.workflow.component;

import com.shiyu.ai.agent.node.llm.LlmCallConfig;
import com.shiyu.ai.agent.node.llm.LlmCallNode;
import com.shiyu.ai.model.chat.ChatEngine;
import com.shiyu.ai.model.chat.ChatRequest;
import com.shiyu.ai.model.chat.ChatResponse;
import com.shiyu.ai.model.adapter.ModelManager;
import com.shiyu.ai.agent.workflow.context.LearningContext;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 评分组件
 *
 * 调用 AI 对学生的练习答案进行自动批改和评分。
 */
@Slf4j
@Component("scoreCmp")
@RequiredArgsConstructor
public class ScoreCmp extends NodeComponent {

    private final ChatEngine chatEngine;

    @Override
    public void process() throws Exception {
        LearningContext ctx = this.getContextBean(LearningContext.class);
        log.info("ScoreCmp: AI 自动批改评分, studentId={}", ctx.getStudentId());

        int questionCount = ctx.getPracticeQuestions() != null
                ? ctx.getPracticeQuestions().size() : 0;

        if (questionCount == 0) {
            ctx.setPracticeScore(60.0);
            ctx.setPracticeAccuracy(0.6);
            log.info("ScoreCmp: 无题目，使用默认评分");
            return;
        }

        String prompt = buildScoringPrompt(ctx);
        ChatResponse resp = chatEngine.chat(ChatRequest.builder().prompt(prompt).build());

        if (resp.isSuccess()) {
            double score = extractScore(resp.getContent(), questionCount);
            ctx.setPracticeScore(score);
            ctx.setPracticeAccuracy(score / 100.0);
            log.info("ScoreCmp: AI 评分完成, 得分={}, 准确率={}", score, ctx.getPracticeAccuracy());
        } else {
            ctx.setPracticeScore(60.0);
            ctx.setPracticeAccuracy(0.6);
            log.warn("ScoreCmp: AI 评分失败，使用默认值: {}", resp.getErrorMessage());
        }
    }

    private String buildScoringPrompt(LearningContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一位K12批改教师，请对学生练习进行评分。\n\n");
        sb.append("知识点：").append(ctx.getKnowledge() != null ? ctx.getKnowledge().name() : "未知").append("\n");
        sb.append("题目数量：").append(ctx.getPracticeQuestions().size()).append(" 道\n\n");
        sb.append("评分标准：\n");
        sb.append("1. 每题满分 100 分\n");
        sb.append("2. 选择题：正确答案得满分，错误得 0 分\n");
        sb.append("3. 填空题：完全正确得满分，部分正确得一半分\n");
        sb.append("4. 解答题：按步骤给分\n\n");
        sb.append("请仅输出最终总分（0-100之间的整数），不要输出其他内容。\n");
        return sb.toString();
    }

    private double extractScore(String content, int questionCount) {
        try {
            String trimmed = content.trim().replaceAll("[^0-9]", "");
            if (!trimmed.isEmpty()) {
                double score = Double.parseDouble(trimmed);
                return Math.max(0, Math.min(100, score));
            }
        } catch (Exception e) {
            log.warn("解析AI评分结果失败: {}", e.getMessage());
        }
        return 60.0;
    }
}
