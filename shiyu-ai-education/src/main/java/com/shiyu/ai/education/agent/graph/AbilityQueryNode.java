package com.shiyu.ai.education.agent.graph;

import com.shiyu.ai.agent.node.NodeInputParam;
import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeInput;
import com.shiyu.ai.agent.node.NodeOutput;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.education.service.AbilityService;
import com.shiyu.ai.education.domain.AbilityValue;
import com.shiyu.ai.knowledge.dto.KnowledgeResponse;
import com.shiyu.ai.knowledge.point.KnowledgePointService;
import com.shiyu.ai.knowledge.service.KnowledgeRelationService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 能力值查询节点
 *
 * LangGraph4j 节点，从 AgentState 读取 studentId/knowledgeId，
 * 查询学生的 Bloom 能力值和知识点详情，写回 State。
 *
 * 输入字段（AgentState）：studentId, knowledgeId
 * 输出字段：knowledge, prerequisites, ability, overallScore
 */
@Slf4j
@Getter
@Setter
public class AbilityQueryNode extends BaseNode {

    private final KnowledgePointService knowledgePointService;
    private final KnowledgeRelationService knowledgeRelationService;
    private final AbilityService abilityService;

    public AbilityQueryNode(KnowledgePointService knowledgePointService,
                            KnowledgeRelationService knowledgeRelationService,
                            AbilityService abilityService) {
        super();
        this.getConfig().setNodeType(NodeType.TRANSFORM);
        this.getConfig().setNodeName("abilityQuery");
        this.knowledgePointService = knowledgePointService;
        this.knowledgeRelationService = knowledgeRelationService;
        this.abilityService = abilityService;
    }

    @Override
    protected NodeOutput doExecute(NodeInput input) throws Exception {
        log.info("AbilityQueryNode: 查询能力值");

        Long studentId = input.getParameter("studentId", null);
        Long knowledgeId = input.getParameter("knowledgeId", null);

        if (studentId == null || knowledgeId == null) {
            NodeOutput err = new NodeOutput();
            err.setSuccess(false);
            err.setMsg("缺少 studentId 或 knowledgeId");
            return err;
        }

        // 1. 查询知识点
        KnowledgeResponse knowledge = knowledgePointService.getResponse(knowledgeId);
        if (knowledge == null) {
            NodeOutput err = new NodeOutput();
            err.setSuccess(false);
            err.setMsg("知识点不存在: " + knowledgeId);
            return err;
        }

        // 2. 查询前置知识
        List<KnowledgeResponse> prerequisites = knowledgeRelationService.getPrerequisites(knowledgeId);

        // 3. 查询能力值
        AbilityValue ability = abilityService.get(studentId, knowledgeId);
        double overallScore = ability != null ? ability.overallScore() : 0.0;

        NodeOutput output = new NodeOutput();
        output.setSuccess(true);
        output.setMsg("能力值查询成功");
        output.addData("knowledge", knowledge);
        output.addData("knowledgeName", knowledge.name());
        output.addData("knowledgeDesc", knowledge.description());
        output.addData("prerequisites", prerequisites);
        output.addData("ability", ability);
        output.addData("overallScore", overallScore);
        output.addData("studentId", studentId);
        output.addData("knowledgeId", knowledgeId);

        log.info("AbilityQueryNode: 知识点={}, 掌握度={}%", knowledge.name(), String.format("%.1f", overallScore));
        return output;
    }

    @Override
    public java.util.List<NodeInputParam> getRequiredInputs() {
        return java.util.List.of(
            NodeInputParam.apiRequired("studentId", "number", "学生 ID"),
            NodeInputParam.apiRequired("knowledgeId", "number", "知识点 ID")
        );
    }
}
