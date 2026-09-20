package com.shiyu.ai.education.implementation.agent.graph;

import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeInput;
import com.shiyu.ai.agent.contract.node.NodeInputParam;
import com.shiyu.ai.agent.contract.node.NodeOutput;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.education.implementation.application.AbilityService;
import com.shiyu.ai.education.implementation.domain.AbilityValue;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.contract.api.KnowledgePointPort;
import com.shiyu.ai.knowledge.contract.api.KnowledgeRelationPort;
import com.shiyu.ai.knowledge.contract.model.KnowledgeResponse;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 执行 Ability Query 相关流程节点的输入处理和状态转移。
 */
@Slf4j
@Getter
@Setter
@SuppressWarnings("this-escape")
public class AbilityQueryNode extends BaseNode {

    /**
     * knowledgePointService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgePointPort knowledgePointService;
    /**
     * knowledgeRelationService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeRelationPort knowledgeRelationService;
    /**
     * abilityService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AbilityService abilityService;

    /**
     * 执行 Ability Query 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param knowledgePointService 用于完成本次业务处理的 knowledgePointService 参数。
     * @param knowledgeRelationService 用于完成本次业务处理的 knowledgeRelationService 参数。
     * @param abilityService 用于完成本次业务处理的 abilityService 参数。
     */
    public AbilityQueryNode(
            KnowledgePointPort knowledgePointService,
            KnowledgeRelationPort knowledgeRelationService,
            AbilityService abilityService) {
        super();
        this.getConfig().setNodeType(NodeType.TRANSFORM);
        this.getConfig().setNodeName("abilityQuery");
        this.knowledgePointService = knowledgePointService;
        this.knowledgeRelationService = knowledgeRelationService;
        this.abilityService = abilityService;
    }

    /**
     * 执行 Ability Query 相关业务数据，并返回处理结果。
     *
     * @param input 用于完成本次业务处理的 input 参数。
     * @return 返回 Ability Query 相关操作生成的结果数据。
     */
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
        ActorContext actor = input.getParameter("__knowledgeAccessContext");
        if (actor == null) {
            throw new IllegalStateException("actor context is required");
        }
        KnowledgeResponse knowledge = knowledgePointService.getResponse(actor, knowledgeId);
        if (knowledge == null) {
            NodeOutput err = new NodeOutput();
            err.setSuccess(false);
            err.setMsg("知识点不存在: " + knowledgeId);
            return err;
        }

        // 2. 查询前置知识
        List<KnowledgeResponse> prerequisites =
                knowledgeRelationService.getPrerequisites(actor, knowledgeId);

        // 3. 查询能力值
        AbilityValue ability = abilityService.get(actor, studentId, knowledgeId);
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

        log.info(
                "AbilityQueryNode: 知识点={}, 掌握度={}%",
                knowledge.name(), String.format("%.1f", overallScore));
        return output;
    }

    /**
     * 查询 Ability Query 相关业务数据，并返回处理结果。
     *
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public java.util.List<NodeInputParam> getRequiredInputs() {
        return java.util.List.of(
                NodeInputParam.apiRequired("studentId", "number", "学生 ID"),
                NodeInputParam.apiRequired("knowledgeId", "number", "知识点 ID"));
    }
}
