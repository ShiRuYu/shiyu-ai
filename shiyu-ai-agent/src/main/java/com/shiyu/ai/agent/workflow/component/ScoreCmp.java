package com.shiyu.ai.agent.workflow.component;

import com.shiyu.ai.model.chat.ChatEngine;
import com.shiyu.ai.model.chat.ChatRequest;
import com.shiyu.ai.model.chat.ChatResponse;
import com.shiyu.ai.dal.education.bo.QuestionBO;
import com.shiyu.ai.agent.workflow.context.LearningContext;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 评分组件
 *
 * 调用 AI 对学生的练习答案进行自动批改和评分。
 * 评分 Prompt 中包含题目、学生答案和参考答案。
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

        List<QuestionBO> questions = ctx.getPracticeQuestions();
        int questionCount = questions != null ? questions.size() : 0;

        if (questionCount == 0) {
            ctx.setPracticeScore(60.0);
            ctx.setPracticeAccuracy(0.6);
            log.info("ScoreCmp: 无题目，使用默认评分");
            return;
        }

        String prompt = buildScoringPrompt(ctx, questions);
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

    private String buildScoringPrompt(LearningContext ctx, List<QuestionBO> questions) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一位K12批改教师，请对学生练习进行评分。\n\n");
        sb.append("知识点：").append(ctx.getKnowledge() != null ? ctx.getKnowledge().name() : "未知").append("\n");
        sb.append("题目数量：").append(questions.size()).append(" 道\n\n");

        // 列出题目及参考答案
        sb.append("## 题目列表\n");
        for (int i = 0; i < questions.size(); i++) {
            QuestionBO q = questions.get(i);
            sb.append("题目 ").append(i + 1).append("：").append(q.getTitle()).append("\n");
            sb.append("  类型：").append(q.getType()).append("\n");
            if (q.getOptions() != null && !q.getOptions().isBlank()) {
                sb.append("  选项：").append(q.getOptions()).append("\n");
            }
            if (q.getAnswer() != null && !q.getAnswer().isBlank()) {
                sb.append("  参考答案：").append(q.getAnswer()).append("\n");
            }
        }

        // 如果存在学生答案，加入评分 Prompt
        List<Map<String, Object>> studentAnswers = ctx.getStudentAnswers();
        if (studentAnswers != null && !studentAnswers.isEmpty()) {
            sb.append("\n## 学生答案\n");
            for (Map<String, Object> answer : studentAnswers) {
                Object questionId = answer.get("questionId");
                Object content = answer.get("content");
                sb.append("  题目 ").append(questionId).append(" 答案：").append(content).append("\n");
            }
        } else {
            sb.append("\n（注：未传入学生答案，将仅根据题目参考信息进行评分评估）\n");
        }

        sb.append("\n");
        sb.append("## 评分标准\n");
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
