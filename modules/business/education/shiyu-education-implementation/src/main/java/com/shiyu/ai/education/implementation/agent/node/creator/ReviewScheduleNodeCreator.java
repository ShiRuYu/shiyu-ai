package com.shiyu.ai.education.implementation.agent.node.creator;

import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.education.contract.agent.EducationNodeTypes;
import com.shiyu.ai.agent.contract.node.creator.NodeCreator;
import com.shiyu.ai.education.implementation.agent.graph.ReviewScheduleNode;
import com.shiyu.ai.education.implementation.application.ReviewService;
import com.shiyu.ai.education.implementation.domain.ReviewScheduler;
import com.shiyu.ai.education.implementation.domain.port.repository.ReviewTaskRepository;

import org.springframework.stereotype.Component;

/**
 * 根据输入配置创建 复习 Schedule Node 相关的流程节点或业务组件。
 */
@Component
public class ReviewScheduleNodeCreator implements NodeCreator {

    /**
     * reviewScheduler 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ReviewScheduler reviewScheduler;
    /**
     * reviewService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ReviewService reviewService;
    /**
     * reviewTaskRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ReviewTaskRepository reviewTaskRepository;

    /**
     * 执行 复习 Schedule Node 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param reviewScheduler 用于完成本次业务处理的 reviewScheduler 参数。
     * @param reviewService 用于完成本次业务处理的 reviewService 参数。
     * @param reviewTaskRepository 用于完成本次业务处理的 reviewTaskRepository 参数。
     */
    public ReviewScheduleNodeCreator(
            ReviewScheduler reviewScheduler,
            ReviewService reviewService,
            ReviewTaskRepository reviewTaskRepository) {
        this.reviewScheduler = reviewScheduler;
        this.reviewService = reviewService;
        this.reviewTaskRepository = reviewTaskRepository;
    }

    /**
     * 查询 复习 Schedule Node 相关业务数据，并返回处理结果。
     *
     * @return 返回 复习 Schedule Node 相关操作生成的结果数据。
     */
    @Override
    public NodeType getType() {
        return EducationNodeTypes.REVIEW_SCHEDULE;
    }

    /**
     * 创建或保存 复习 Schedule Node 相关业务数据，并返回处理结果。
     *
     * @param config 用于完成本次业务处理的 config 参数。
     * @return 返回 复习 Schedule Node 相关操作生成的结果数据。
     */
    @Override
    public BaseNode create(NodeConfig config) {
        ReviewScheduleNode node =
                new ReviewScheduleNode(reviewScheduler, reviewService, reviewTaskRepository);
        node.setConfig(config);
        return node;
    }
}
