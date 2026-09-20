package com.shiyu.ai.education.implementation.agent.node.creator;

import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.education.contract.agent.EducationNodeTypes;
import com.shiyu.ai.agent.contract.node.creator.NodeCreator;
import com.shiyu.ai.education.implementation.agent.graph.ScoreAnalysisNode;
import com.shiyu.ai.education.implementation.application.AbilityService;

import org.springframework.stereotype.Component;

/**
 * 根据输入配置创建 Score Analysis Node 相关的流程节点或业务组件。
 */
@Component
public class ScoreAnalysisNodeCreator implements NodeCreator {

    /**
     * abilityService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AbilityService abilityService;

    /**
     * 执行 Score Analysis Node 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param abilityService 用于完成本次业务处理的 abilityService 参数。
     */
    public ScoreAnalysisNodeCreator(AbilityService abilityService) {
        this.abilityService = abilityService;
    }

    /**
     * 查询 Score Analysis Node 相关业务数据，并返回处理结果。
     *
     * @return 返回 Score Analysis Node 相关操作生成的结果数据。
     */
    @Override
    public NodeType getType() {
        return EducationNodeTypes.SCORE_ANALYSIS;
    }

    /**
     * 创建或保存 Score Analysis Node 相关业务数据，并返回处理结果。
     *
     * @param config 用于完成本次业务处理的 config 参数。
     * @return 返回 Score Analysis Node 相关操作生成的结果数据。
     */
    @Override
    public BaseNode create(NodeConfig config) {
        ScoreAnalysisNode node = new ScoreAnalysisNode(abilityService);
        node.setConfig(config);
        return node;
    }
}
