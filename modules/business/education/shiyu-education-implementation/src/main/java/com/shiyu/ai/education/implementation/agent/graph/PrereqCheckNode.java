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
 * 前置知识检查节点
 *
 * <p>LangGraph4j 节点，检测学生对目标知识点缺失的前置知识。
 *
 * <p>输入字段：knowledgeId, studentId 输出字段：missingPrerequisites, hasMissingPrereqs
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
     * {@code PrereqCheckNode} 创建并初始化当前类型实例。
     *
     * @param knowledgeRelationService 参数值，用于执行当前操作。
     * @param knowledgePathService 参数值，用于执行当前操作。
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
     * {@code doExecute} 执行当前类型定义的业务操作。
     *
     * @param input 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code getRequiredInputs} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public java.util.List<NodeInputParam> getRequiredInputs() {
        return java.util.List.of(
                NodeInputParam.apiRequired("knowledgeId", "number", "知识点 ID"),
                NodeInputParam.apiOptional("studentId", "number", "学生 ID（可选，仅用于记录）", null));
    }
}
