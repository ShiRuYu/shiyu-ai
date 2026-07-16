package com.shiyu.ai.agent.education.graph;

import com.shiyu.ai.agent.node.NodeInputParam;
import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeInput;
import com.shiyu.ai.agent.node.NodeOutput;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.dal.bo.education.QuestionBO;
import com.shiyu.ai.education.service.AbilityService;
import com.shiyu.ai.education.domain.BloomTaxonomy;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 评分分析节点
 *
 * LangGraph4j 节点，对学生练习结果进行评分，
 * 更新 Bloom 能力值，输出"是否需要重学"的判断。
 *
 * 输入字段：practiceQuestions, overallScore, studentId, knowledgeId, answerResults（可选）
 * 输出字段：practiceScore, practiceAccuracy, updatedAbility, reviewNeeded, afterAbility
 */
@Slf4j
@Getter
@Setter
public class ScoreAnalysisNode extends BaseNode {

    private final AbilityService abilityService;

    public ScoreAnalysisNode(AbilityService abilityService) {
        super();
        this.getConfig().setNodeType(NodeType.TRANSFORM);
        this.getConfig().setNodeName("scoreAnalysis");
        this.abilityService = abilityService;
    }

    @Override
    protected NodeOutput doExecute(NodeInput input) throws Exception {
        log.info("ScoreAnalysisNode: 评分与分析");

        @SuppressWarnings("unchecked")
        List<QuestionBO> questions = input.getParameter("practiceQuestions", List.of());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> answerResults = input.getParameter("answerResults", null);
        Long studentId = input.getParameter("studentId", null);
        Long knowledgeId = input.getParameter("knowledgeId", null);

        // 基于答题结果计算准确率；无结果时降级为默认值
        double accuracy;
        double score;
        int questionCount = questions.size();

        if (answerResults != null && !answerResults.isEmpty()) {
            long correctCount = answerResults.stream()
                    .filter(r -> Boolean.TRUE.equals(r.get("correct")))
                    .count();
            questionCount = Math.max(answerResults.size(), questionCount);
            accuracy = questionCount > 0 ? (double) correctCount / questionCount : 0.0;
            score = accuracy * 100.0;
            log.info("ScoreAnalysisNode: 基于答题结果评分, 正确={}/{}, 准确率={}%, 得分={}",
                    correctCount, questionCount, String.format("%.1f", accuracy * 100), String.format("%.1f", score));
        } else {
            accuracy = 0.6;
            score = 60.0;
            log.warn("ScoreAnalysisNode: 无答题结果(answerResults)，使用默认评分 accuracy=0.6, score=60.0");
        }

        // 更新能力值
        if (studentId != null && knowledgeId != null) {
            abilityService.update(studentId, knowledgeId, BloomTaxonomy.APPLY, accuracy);
            abilityService.update(studentId, knowledgeId, BloomTaxonomy.REMEMBER, Math.min(accuracy + 0.2, 1.0));
        }

        // 判断是否需要重学（score < 60 需要重学）
        boolean reviewNeeded = score < 60.0;

        NodeOutput output = new NodeOutput();
        output.setSuccess(true);
        output.setMsg("评分完成");
        output.addData("practiceScore", score);
        output.addData("practiceAccuracy", accuracy);
        output.addData("questionCount", questionCount);
        output.addData("reviewNeeded", reviewNeeded);
        output.addData("scoreAnalysisDone", true);

        log.info("ScoreAnalysisNode: 最终得分={}, 重学需要={}", String.format("%.1f", score), reviewNeeded);
        return output;
    }

    @Override
    public java.util.List<NodeInputParam> getRequiredInputs() {
        return java.util.List.of(
            NodeInputParam.previous("practiceQuestions", "array", "练习题列表"),
            NodeInputParam.previous("studentId", "number", "学生 ID"),
            NodeInputParam.previous("knowledgeId", "number", "知识点 ID"),
            NodeInputParam.apiOptional("answerResults", "array", "答题结果列表（可选），每项含 questionId + correct，为空时使用默认评分", null)
        );
    }
}
