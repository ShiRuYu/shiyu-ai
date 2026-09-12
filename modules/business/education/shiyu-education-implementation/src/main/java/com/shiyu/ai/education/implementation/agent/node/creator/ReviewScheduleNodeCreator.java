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
 * {@code ReviewScheduleNodeCreator} 负责创建教育模块中的运行时对象，并集中封装构造规则。
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
     * {@code ReviewScheduleNodeCreator} 创建并初始化当前类型实例。
     *
     * @param reviewScheduler 参数值，用于执行当前操作。
     * @param reviewService 参数值，用于执行当前操作。
     * @param reviewTaskRepository 参数值，用于执行当前操作。
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
     * {@code getType} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public NodeType getType() {
        return EducationNodeTypes.REVIEW_SCHEDULE;
    }

    /**
     * {@code create} 写入或更新当前模块中的业务数据。
     *
     * @param config 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public BaseNode create(NodeConfig config) {
        ReviewScheduleNode node =
                new ReviewScheduleNode(reviewScheduler, reviewService, reviewTaskRepository);
        node.setConfig(config);
        return node;
    }
}
