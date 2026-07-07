package com.shiyu.ai.aiagent.education.graph;

import com.shiyu.ai.aiagent.node.NodeInputParam;
import com.shiyu.ai.aiagent.node.BaseNode;
import com.shiyu.ai.aiagent.node.NodeInput;
import com.shiyu.ai.aiagent.node.NodeOutput;
import com.shiyu.ai.aiagent.node.NodeType;
import com.shiyu.ai.dal.dataobject.education.QuestionDO;
import com.shiyu.ai.education.service.AbilityService;
import com.shiyu.ai.education.domain.BloomTaxonomy;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 评分分析节点
 *
 * LangGraph4j 节点，对学生练习结果进行评分，
 * 更新 Bloom 能力值，输出"是否需要重学"的判断。
 *
 * 输入字段：practiceQuestions, overallScore, studentId, knowledgeId
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
        List<QuestionDO> questions = input.getParameter("practiceQuestions", List.of());
        Long studentId = input.getParameter("studentId", null);
        Long knowledgeId = input.getParameter("knowledgeId", null);

        // 模拟评分（60% 准确率，实际应对接答题结果）
        double accuracy = 0.6;
        double score = 60.0;
        int questionCount = questions.size();

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

        log.info("ScoreAnalysisNode: 得分={}, 重学需要={}", score, reviewNeeded);
        return output;
    }

    @Override
    public java.util.List<NodeInputParam> getRequiredInputs() {
        return java.util.List.of(
            NodeInputParam.previous("practiceQuestions", "array", "练习题列表"),
            NodeInputParam.previous("studentId", "number", "学生 ID"),
            NodeInputParam.previous("knowledgeId", "number", "知识点 ID")
        );
    }
}
