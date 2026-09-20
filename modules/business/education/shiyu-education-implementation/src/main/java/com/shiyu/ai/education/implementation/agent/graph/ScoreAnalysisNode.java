package com.shiyu.ai.education.implementation.agent.graph;

import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeInput;
import com.shiyu.ai.agent.contract.node.NodeInputParam;
import com.shiyu.ai.agent.contract.node.NodeOutput;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.education.implementation.application.AbilityService;
import com.shiyu.ai.education.implementation.domain.BloomTaxonomy;
import com.shiyu.ai.education.implementation.domain.model.QuestionBO;
import com.shiyu.ai.kernel.context.ActorContext;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 执行 Score Analysis 相关流程节点的输入处理和状态转移。
 */
@Slf4j
@Getter
@Setter
@SuppressWarnings("this-escape")
public class ScoreAnalysisNode extends BaseNode {

    /**
     * abilityService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AbilityService abilityService;

    /**
     * 执行 Score Analysis 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param abilityService 用于完成本次业务处理的 abilityService 参数。
     */
    public ScoreAnalysisNode(AbilityService abilityService) {
        super();
        this.getConfig().setNodeType(NodeType.TRANSFORM);
        this.getConfig().setNodeName("scoreAnalysis");
        this.abilityService = abilityService;
    }

    /**
     * 执行 Score Analysis 相关业务数据，并返回处理结果。
     *
     * @param input 用于完成本次业务处理的 input 参数。
     * @return 返回 Score Analysis 相关操作生成的结果数据。
     */
    @Override
    protected NodeOutput doExecute(NodeInput input) throws Exception {
        log.info("ScoreAnalysisNode: 评分与分析");

        @SuppressWarnings("unchecked")
        List<QuestionBO> questions = input.getParameter("practiceQuestions", List.of());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> answerResults = input.getParameter("answerResults", null);
        Long studentId = input.getParameter("studentId", null);
        Long knowledgeId = input.getParameter("knowledgeId", null);
        ActorContext actor = input.getParameter("__knowledgeAccessContext");

        // 基于答题结果计算准确率；无结果时降级为默认值
        double accuracy;
        double score;
        int questionCount = questions.size();

        if (answerResults != null && !answerResults.isEmpty()) {
            long correctCount =
                    answerResults.stream()
                            .filter(r -> Boolean.TRUE.equals(r.get("correct")))
                            .count();
            questionCount = Math.max(answerResults.size(), questionCount);
            accuracy = questionCount > 0 ? (double) correctCount / questionCount : 0.0;
            score = accuracy * 100.0;
            log.info(
                    "ScoreAnalysisNode: 基于答题结果评分, 正确={}/{}, 准确率={}%, 得分={}",
                    correctCount,
                    questionCount,
                    String.format("%.1f", accuracy * 100),
                    String.format("%.1f", score));
        } else {
            accuracy = 0.6;
            score = 60.0;
            log.warn("ScoreAnalysisNode: 无答题结果(answerResults)，使用默认评分 accuracy=0.6, score=60.0");
        }

        // 更新能力值
        if (studentId != null && knowledgeId != null) {
            if (actor == null) {
                throw new IllegalStateException("actor context is required");
            }
            abilityService.update(actor, studentId, knowledgeId, BloomTaxonomy.APPLY, accuracy);
            abilityService.update(
                    actor,
                    studentId,
                    knowledgeId,
                    BloomTaxonomy.REMEMBER,
                    Math.min(accuracy + 0.2, 1.0));
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

    /**
     * 查询 Score Analysis 相关业务数据，并返回处理结果。
     *
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public java.util.List<NodeInputParam> getRequiredInputs() {
        return java.util.List.of(
                NodeInputParam.previous("practiceQuestions", "array", "练习题列表"),
                NodeInputParam.previous("studentId", "number", "学生 ID"),
                NodeInputParam.previous("knowledgeId", "number", "知识点 ID"),
                NodeInputParam.apiOptional(
                        "answerResults",
                        "array",
                        "答题结果列表（可选），每项含 questionId + correct，为空时使用默认评分",
                        null));
    }
}
