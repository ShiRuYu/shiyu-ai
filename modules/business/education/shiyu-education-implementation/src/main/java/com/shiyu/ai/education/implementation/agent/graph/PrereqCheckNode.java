package com.shiyu.ai.education.implementation.agent.graph;

import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeInput;
import com.shiyu.ai.agent.contract.node.NodeInputParam;
import com.shiyu.ai.agent.contract.node.NodeOutput;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.contract.api.KnowledgePathPort;
import com.shiyu.ai.knowledge.contract.api.KnowledgeRelationPort;
import com.shiyu.ai.knowledge.contract.model.KnowledgeResponse;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 执行 Prereq Check 相关流程节点的输入处理和状态转移。
 */
@Slf4j
@Getter
@Setter
@SuppressWarnings("this-escape")
public class PrereqCheckNode extends BaseNode {

    /**
     * knowledgeRelationService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeRelationPort knowledgeRelationService;
    /**
     * knowledgePathService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgePathPort knowledgePathService;

    /**
     * 执行 Prereq Check 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param knowledgeRelationService 用于完成本次业务处理的 knowledgeRelationService 参数。
     * @param knowledgePathService 用于完成本次业务处理的 knowledgePathService 参数。
     */
    public PrereqCheckNode(
            KnowledgeRelationPort knowledgeRelationService,
            KnowledgePathPort knowledgePathService) {
        super();
        this.getConfig().setNodeType(NodeType.TRANSFORM);
        this.getConfig().setNodeName("prereqCheck");
        this.knowledgeRelationService = knowledgeRelationService;
        this.knowledgePathService = knowledgePathService;
    }

    /**
     * 执行 Prereq Check 相关业务数据，并返回处理结果。
     *
     * @param input 用于完成本次业务处理的 input 参数。
     * @return 返回 Prereq Check 相关操作生成的结果数据。
     */
    @Override
    protected NodeOutput doExecute(NodeInput input) throws Exception {
        log.info("PrereqCheckNode: 检查前置知识");

        Long knowledgeId = input.getParameter("knowledgeId", null);
        Long studentId = input.getParameter("studentId", null);

        if (knowledgeId == null) {
            NodeOutput err = new NodeOutput();
            err.setSuccess(false);
            err.setMsg("缺少 knowledgeId");
            return err;
        }
        ActorContext actor = input.getParameter("__knowledgeAccessContext");
        if (actor == null) {
            throw new IllegalStateException("actor context is required");
        }

        // 1. 获取前置知识点列表
        List<KnowledgeResponse> prerequisites;
        try {
            prerequisites = knowledgeRelationService.getPrerequisites(actor, knowledgeId);
        } catch (Exception e) {
            log.warn("获取前置知识失败", e);
            prerequisites = Collections.emptyList();
        }

        // 2. 检测缺失前置
        List<Long> missing;
        try {
            missing = knowledgePathService.findMissingPrerequisites(actor, knowledgeId, Set.of());
        } catch (Exception e) {
            log.warn("检测缺失前置失败", e);
            missing = Collections.emptyList();
        }

        NodeOutput output = new NodeOutput();
        output.setSuccess(true);
        output.setMsg("前置知识检查完成");
        output.addData("prerequisites", prerequisites);
        output.addData("missingPrerequisiteIds", missing);
        output.addData("hasMissingPrereqs", !missing.isEmpty());

        log.info("PrereqCheckNode: 前置知识={}个, 缺失={}个", prerequisites.size(), missing.size());
        return output;
    }

    /**
     * 查询 Prereq Check 相关业务数据，并返回处理结果。
     *
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public java.util.List<NodeInputParam> getRequiredInputs() {
        return java.util.List.of(
                NodeInputParam.apiRequired("knowledgeId", "number", "知识点 ID"),
                NodeInputParam.apiOptional("studentId", "number", "学生 ID（可选，仅用于记录）", null));
    }
}
