package com.shiyu.ai.education.implementation.agent.node.creator;

import com.shiyu.ai.education.implementation.agent.graph.ReviewScheduleNode;
import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.agent.contract.node.creator.NodeCreator;
import com.shiyu.ai.education.implementation.domain.port.repository.ReviewTaskRepository;
import com.shiyu.ai.education.implementation.domain.ReviewScheduler;
import com.shiyu.ai.education.implementation.application.ReviewService;
import org.springframework.stereotype.Component;

@Component
public class ReviewScheduleNodeCreator implements NodeCreator {

    private final ReviewScheduler reviewScheduler;
    private final ReviewService reviewService;
    private final ReviewTaskRepository reviewTaskRepository;

    public ReviewScheduleNodeCreator(ReviewScheduler reviewScheduler,
                                     ReviewService reviewService,
                                     ReviewTaskRepository reviewTaskRepository) {
        this.reviewScheduler = reviewScheduler;
        this.reviewService = reviewService;
        this.reviewTaskRepository = reviewTaskRepository;
    }

    @Override
    public NodeType getType() {
        return NodeType.REVIEW_SCHEDULE;
    }

    @Override
    public BaseNode create(NodeConfig config) {
        ReviewScheduleNode node = new ReviewScheduleNode(reviewScheduler, reviewService, reviewTaskRepository);
        node.setConfig(config);
        return node;
    }
}

