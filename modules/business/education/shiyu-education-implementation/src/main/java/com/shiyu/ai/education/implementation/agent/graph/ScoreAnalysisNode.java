package com.shiyu.ai.education.implementation.agent.graph;

import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeInput;
import com.shiyu.ai.agent.contract.node.NodeInputParam;
import com.shiyu.ai.agent.contract.node.NodeOutput;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.education.implementation.application.TrustedScoreAnalysisService;
import com.shiyu.ai.education.implementation.domain.model.QuestionBO;
import com.shiyu.ai.kernel.context.ActorContext;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 执行 Score Analysis 相关流程节点的输入处理和状态转移。
 */
@Slf4j
@Getter
@Setter
@SuppressWarnings("this-escape")
public class ScoreAnalysisNode extends BaseNode {

    /** Server-side verifier and idempotent ability updater. */
    private final TrustedScoreAnalysisService analysis;

    /**
     * 注入服务端可信判分服务，节点只按已验证的答题记录更新学生能力值。
     *
     * @param analysis 校验答题归属并保证能力值更新幂等的服务
     */
    public ScoreAnalysisNode(TrustedScoreAnalysisService analysis) {
        super();
        this.getConfig().setNodeType(NodeType.TRANSFORM);
        this.getConfig().setNodeName("scoreAnalysis");
        this.analysis = analysis;
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
        NodeOutput output = new NodeOutput();
        Object recordValue = input.getParameter("practiceRecordId");
        if (recordValue instanceof Number number && number.longValue() > 0) {
            Object context = input.getParameter("__knowledgeAccessContext");
            if (!(context instanceof ActorContext actor)) {
                output.setSuccess(false);
                output.setMsg("缺少可信学生上下文");
                output.addData("scoreAnalysisDone", false);
                return output;
            }
            var trusted = analysis.analyze(actor, number.longValue());
            output.setSuccess(true);
            output.setMsg(trusted.applied() ? "服务端判分已应用" : "服务端判分已处理");
            output.addData("practiceRecordId", trusted.recordId());
            output.addData("practiceScore", trusted.score());
            output.addData("knowledgeId", trusted.knowledgeId());
            output.addData("scoreAnalysisDone", true);
            output.addData("analysisApplied", trusted.applied());
            output.addData("reviewNeeded", trusted.score() < 100D);
            return output;
        }
        // Graph inputs may contain caller-controlled `correct` flags. They are never evidence.
        output.setSuccess(true);
        output.setMsg("等待服务端判分记录");
        output.addData("questionCount", questions.size());
        output.addData("reviewNeeded", false);
        output.addData("scoreAnalysisDone", false);
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
                NodeInputParam.apiOptional("practiceRecordId", "number",
                        "服务端练习记录 ID", null),
                NodeInputParam.apiOptional(
                        "answerResults",
                        "array",
                        "旧版客户端答题结果（不作为评分依据）",
                        null));
    }
}
